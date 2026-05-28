(function($) {

    "use strict";

    var searchPopup = function() {
      // open search box
      $('#header-nav').on('click', '.search-button', function(e) {
        $('.search-popup').toggleClass('is-visible');
      });

      $('#header-nav').on('click', '.btn-close-search', function(e) {
        $('.search-popup').toggleClass('is-visible');
      });
      
      $(".search-popup-trigger").on("click", function(b) {
          b.preventDefault();
          $(".search-popup").addClass("is-visible"),
          setTimeout(function() {
              $(".search-popup").find("#search-popup").focus()
          }, 350)
      }),
      $(".search-popup").on("click", function(b) {
          ($(b.target).is(".search-popup-close") || $(b.target).is(".search-popup-close svg") || $(b.target).is(".search-popup-close path") || $(b.target).is(".search-popup")) && (b.preventDefault(),
          $(this).removeClass("is-visible"))
      }),
      $(document).keyup(function(b) {
          "27" === b.which && $(".search-popup").removeClass("is-visible")
      })
    }

    var countdownTimer = function() {
      function getTimeRemaining(endtime) {
        const total = Date.parse(endtime) - Date.parse(new Date());
        const seconds = Math.floor((total / 1000) % 60);
        const minutes = Math.floor((total / 1000 / 60) % 60);
        const hours = Math.floor((total / (1000 * 60 * 60)) % 24);
        const days = Math.floor(total / (1000 * 60 * 60 * 24));
        return {
          total,
          days,
          hours,
          minutes,
          seconds
        };
      }
  
      function initializeClock(id, endtime) {
        const clock = document.getElementById(id);
        if(!clock) return;
        const daysSpan = clock.querySelector('.days');
        const hoursSpan = clock.querySelector('.hours');
        const minutesSpan = clock.querySelector('.minutes');
        const secondsSpan = clock.querySelector('.seconds');
  
        function updateClock() {
          const t = getTimeRemaining(endtime);
          daysSpan.innerHTML = t.days;
          hoursSpan.innerHTML = ('0' + t.hours).slice(-2);
          minutesSpan.innerHTML = ('0' + t.minutes).slice(-2);
          secondsSpan.innerHTML = ('0' + t.seconds).slice(-2);
          if (t.total <= 0) {
            clearInterval(timeinterval);
          }
        }
        updateClock();
        const timeinterval = setInterval(updateClock, 1000);
      }
  
      $('#countdown-clock').each(function(){
        const deadline = new Date(Date.parse(new Date()) + 28 * 24 * 60 * 60 * 1000);
        initializeClock('countdown-clock', deadline);
      });
    }

    var initProductQty = function(){

      $('.product-qty').each(function(){

        var $el_product = $(this);
        let max = parseInt($el_product.find('#quantity').attr("max")) || 999;

        $el_product.find('.quantity-right-plus').click(function(e){
          e.preventDefault();

          let input = $el_product.find('#quantity');
          let quantity = parseInt(input.val()) || 1;

          if (quantity >= max) {
            showToast("Đã đạt tối đa tồn kho", false);
            input.val(max);
            return;
          }

          input.val(quantity + 1);
        });

        $el_product.find('.quantity-left-minus').click(function(e){
          e.preventDefault();

          let input = $el_product.find('#quantity');
          let quantity = parseInt(input.val()) || 1;

          if (quantity <= 1) {
            input.val(1);
            return;
          }

          input.val(quantity - 1);
        });

      });

    }

    $(document).ready(function() {

      searchPopup();
      initProductQty();
      countdownTimer();

      /* Video */
      var $videoSrc;  
        $('.play-btn').click(function() {
          $videoSrc = $(this).data( "src" );
        });

        $('#myModal').on('shown.bs.modal', function (e) {

        $("#video").attr('src',$videoSrc + "?autoplay=1&amp;modestbranding=1&amp;showinfo=0" ); 
      })

      $('#myModal').on('hide.bs.modal', function (e) {
        $("#video").attr('src',$videoSrc); 
      })

      var mainSwiper = new Swiper(".main-swiper", {
        speed: 500,
        navigation: {
          nextEl: ".main-slider-button-next",
          prevEl: ".main-slider-button-prev",
        },
      });

      // var productSwiper = new Swiper(".product-swiper", {
      //   spaceBetween: 20,
      //   navigation: {
      //     nextEl: ".product-slider-button-next",
      //     prevEl: ".product-slider-button-prev",
      //   },
      //   breakpoints: {
      //     0: {
      //       slidesPerView: 1,
      //     },
      //     660: {
      //       slidesPerView: 3,
      //     },
      //     980: {
      //       slidesPerView: 4,
      //     },
      //     1500: {
      //       slidesPerView: 5,
      //     }
      //   },
      // });
      function initCommonSwiper(selector, nextEl, prevEl) {
        return new Swiper(selector, {
          spaceBetween: 20,
          navigation: {
            nextEl: nextEl,
            prevEl: prevEl,
          },
          breakpoints: {
            0: { slidesPerView: 1 },
            660: { slidesPerView: 3 },
            980: { slidesPerView: 4 },
            1500: { slidesPerView: 5 }
          }
        });
      }

      var productSwiper = initCommonSwiper(
          ".product-swiper",
          ".product-slider-button-next",
          ".product-slider-button-prev"
      );

      var publisherSwiper = initCommonSwiper(
          ".publisher-swiper",
          ".publisher-slider-button-next",
          ".publisher-slider-button-prev"
      );

      var testimonialSwiper = new Swiper(".testimonial-swiper", {
        slidesPerView: 1,
        spaceBetween: 20,
        navigation: {
          nextEl: ".testimonial-button-next",
          prevEl: ".testimonial-button-prev",
        },
      });

      // var thumb_slider = new Swiper(".thumb-swiper", {
      //   slidesPerView: 1,
      // });
      // var large_slider = new Swiper(".large-swiper", {
      //   spaceBetween: 10,
      //   effect: 'fade',
        // thumbs: {
        //   swiper: thumb_slider,
        // },
      // });
      if (document.querySelector(".large-swiper")) {

        var config = {
          spaceBetween: 10,
          effect: 'fade',
        };

        if (document.querySelector(".thumb-swiper")) {
          var thumb_slider = new Swiper(".thumb-swiper", {
            slidesPerView: 1,
          });

          config.thumbs = {
            swiper: thumb_slider,
          };
        }

        var large_slider = new Swiper(".large-swiper", config);
      }
    }); // End of a document ready

    window.addEventListener("load", function () {
      const preloader = document.getElementById("preloader");
      if (preloader) {
        preloader.classList.add("hide-preloader");
      }
    });

  document.addEventListener("DOMContentLoaded", function () {

    document.querySelectorAll(".add-to-cart-btn").forEach(btn => {

      btn.addEventListener("click", function (e) {
        e.preventDefault();
        // e.stopPropagation();

        console.log("Click add cart");

        let productId = this.getAttribute("data-id");

        let quantityInput = document.getElementById("quantity");
        let quantity = quantityInput ? parseInt(quantityInput.value) : 1

        let max = quantityInput ? parseInt(quantityInput.getAttribute("max")) : 999;
        if (!quantity || quantity <= 0) {
          showToast("Số lượng không hợp lệ", false);
          return;
        }

        if (quantity > max) {
          showToast("Vượt quá số lượng tồn kho (" + max + ")", false);
          quantityInput.value = max;
          return;
        }
        let csrfMeta = document.querySelector('meta[name="_csrf"]');
        let headerMeta = document.querySelector('meta[name="_csrf_header"]');

        if (!csrfMeta || !headerMeta) {
          console.error("CSRF missing");
          return;
        }

        let token = csrfMeta.getAttribute('content');
        let header = headerMeta.getAttribute('content');

        fetch("/user/cart/add", {
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            [header]: token
          },
          body: "pid=" + productId + "&quantity=" + quantity
        })
            .then(res => res.json())
            .then(data => {
              console.log(data);
              if (data.success) {
                showToast("Đã thêm vào giỏ hàng");
                fetch("/user/cart/count")
                    .then(res => res.json())
                    .then(res => {
                      updateCartCountUI(res.count);
                    });
              } else {
                showToast(data.message || "Có lỗi xảy ra", false);
              }
            })
            .catch(err => console.error(err));
      });
    });

  });

  function showToast(message, success = true) {

    let toast = document.createElement("div");

    toast.innerText = message;
    toast.style.position = "fixed";
    toast.style.top = "20px";
    toast.style.right = "20px";
    toast.style.padding = "12px 20px";
    toast.style.background = success ? "#28a745" : "#dc3545";
    toast.style.color = "white";
    toast.style.borderRadius = "5px";
    toast.style.zIndex = "9999";

    document.body.appendChild(toast);

    setTimeout(() => toast.remove(), 2000);
  }

  document.addEventListener("DOMContentLoaded", function () {

    document.addEventListener("click", function (e) {

      let btn = e.target.closest(".qty-btn");
      if (!btn) return;

      e.preventDefault();
      // e.stopPropagation();

      console.log("CLICK + -");

      let itemId = btn.getAttribute("data-id");
      let type = btn.getAttribute("data-type");

      let csrfMeta = document.querySelector('meta[name="_csrf"]');
      let headerMeta = document.querySelector('meta[name="_csrf_header"]');

      if (!csrfMeta || !headerMeta) {
        console.error("CSRF missing");
        return;
      }

      let token = csrfMeta.getAttribute("content");
      let header = headerMeta.getAttribute("content");

      fetch("/user/cart/update-ajax", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
          [header]: token
        },
        body: "itemId=" + itemId + "&sy=" + type
      })
      .then(res => res.json())
      .then(data => {

        console.log("RESPONSE:", data);

        if (data.success) {
          let row = btn.closest(".cart-item");

          if (data.deleted) {
            if (row) {
              row.remove();
            }
            let cartTotal = document.getElementById("cart-total-price");
            if (cartTotal) {
              cartTotal.innerText = formatCurrency(data.cartTotal);
            }
            if (document.querySelectorAll(".cart-item").length === 0) {
              location.reload();
            }
            return;
          }

          // update quantity
          let qtyEl = btn.parentElement.querySelector(".quantity-text");
          if (qtyEl) qtyEl.innerText = data.quantity;

          // update item total
          if (row) {
            let totalEl = row.querySelector(".item-total");
            if (totalEl) {
              totalEl.innerText = formatCurrency(data.totalPrice);
              totalEl.style.transition = "0.2s";
              totalEl.style.transform = "scale(1.1)";
              setTimeout(() => {
                totalEl.style.transform = "scale(1)";
              }, 200);
            }
          }

          // update cart total
          let cartTotal = document.getElementById("cart-total-price");
          if (cartTotal) cartTotal.innerText = data.cartTotal.toLocaleString("vi-VN");

        } else {
          console.error(data.message);
        }
      })
      .catch(err => console.error(err));
    });
  });

  function formatCurrency(number) {
    return number.toLocaleString("vi-VN") + "đ";
  }

  function updateCartCountUI(count) {
    let badge = document.getElementById("cart-count");

    if (badge) {
      badge.innerText = count;

      badge.style.transform = "scale(1.2)";
      setTimeout(() => {
        badge.style.transform = "scale(1)";
      }, 200);
    }
  }
})(jQuery);

