document.addEventListener("DOMContentLoaded", async function () {
    let provincesPromise = null;

    function loadProvinces() {
        if (!provincesPromise) {
            provincesPromise = fetch("/data/vietnam-addresses.json", {
                headers: {
                    "Accept": "application/json"
                }
            }).then(function (response) {
                if (!response.ok) {
                    throw new Error("Cannot load vietnam-addresses.json");
                }

                return response.json();
            });
        }

        return provincesPromise;
    }

    function appendOption(select, value, text, code) {
        const option = document.createElement("option");
        option.value = value;
        option.textContent = text;

        if (code) {
            option.dataset.code = code;
        }

        select.appendChild(option);
    }

    function resetSelect(select, placeholder, disabled) {
        select.innerHTML = "";
        appendOption(select, "", placeholder);
        select.disabled = disabled;
    }

    function normalizeValue(value) {
        return String(value || "").trim();
    }

    async function initAddressSelect(citySelect, wardSelect) {
        if (!citySelect || !wardSelect) {
            return;
        }

        const selectedCity = normalizeValue(citySelect.dataset.selectedCity || citySelect.value);
        const selectedWard = normalizeValue(wardSelect.dataset.selectedWard || wardSelect.value);

        resetSelect(citySelect, "Chọn tỉnh/thành phố", false);
        resetSelect(wardSelect, "Chọn xã/phường", true);

        try {
            const provinces = await loadProvinces();

            provinces.forEach(function (province) {
                appendOption(citySelect, province.name, province.name, province.code);
            });

            if (selectedCity) {
                citySelect.value = selectedCity;
                renderWards(provinces, citySelect, wardSelect, selectedWard);
            }

            citySelect.addEventListener("change", function () {
                renderWards(provinces, citySelect, wardSelect, "");
            });
        } catch (error) {
            console.error(error);

            resetSelect(citySelect, "Không thể tải danh sách tỉnh/thành phố", true);
            resetSelect(wardSelect, "Không thể tải danh sách xã/phường", true);
        }
    }

    function renderWards(provinces, citySelect, wardSelect, selectedWard) {
        const selectedCity = citySelect.value;

        resetSelect(wardSelect, "Chọn xã/phường", true);

        if (!selectedCity) {
            return;
        }

        const province = provinces.find(function (item) {
            return item.name === selectedCity;
        });

        if (!province || !Array.isArray(province.wards)) {
            return;
        }

        province.wards.forEach(function (ward) {
            appendOption(wardSelect, ward.name, ward.name, ward.code);
        });

        wardSelect.disabled = false;

        if (selectedWard) {
            wardSelect.value = selectedWard;
        }
    }

    const initializedPairs = new Set();

    document.querySelectorAll(".vietnam-city-select[data-ward-select-id]").forEach(function (citySelect) {
        const wardSelectId = citySelect.dataset.wardSelectId;
        const wardSelect = document.getElementById(wardSelectId);

        if (!wardSelect) {
            return;
        }

        initializedPairs.add(citySelect.id + "::" + wardSelect.id);
        initAddressSelect(citySelect, wardSelect);
    });

    const checkoutCitySelect = document.getElementById("city");
    const checkoutWardSelect = document.getElementById("district");

    if (checkoutCitySelect && checkoutWardSelect) {
        const key = checkoutCitySelect.id + "::" + checkoutWardSelect.id;

        if (!initializedPairs.has(key)) {
            initAddressSelect(checkoutCitySelect, checkoutWardSelect);
        }
    }
});