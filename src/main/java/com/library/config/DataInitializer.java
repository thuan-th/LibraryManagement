//package com.library.config;
//
//import com.library.module.user.entity.User;
//import com.library.module.book.entity.Book;
//import com.library.module.book.repository.BookRepository;
//import com.library.module.user.repository.UserRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.*;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Configuration
//public class DataInitializer {
//
//    @Bean
//    public CommandLineRunner initSampleData(UserRepository userRepository,
//                                            PasswordEncoder passwordEncoder,
//                                            BookRepository bookRepository) {
//        return args -> {
//            User admin = userRepository.findByEmail("admin@test.com");
//            if (admin == null) {
//                admin = new User();
//                admin.setName("AdminTest");
//                admin.setEmail("admin@test.com");
//                admin.setPassword(passwordEncoder.encode("12345"));
//                admin.setRole("ROLE_ADMIN");
//                admin.setIsEnable(true);
//                admin.setAccountNonLocked(true);
//                admin.setFailedAttempt(0);
//                admin.setLockTime(null);
//                userRepository.save(admin);
//            }
//
//            User user = userRepository.findByEmail("user@test.com");
//            if (user == null) {
//                user = new User();
//                user.setName("UserTest");
//                user.setEmail("user@test.com");
//                user.setPassword(passwordEncoder.encode("12345"));
//                user.setRole("ROLE_USER");
//                user.setIsEnable(true);
//                user.setAccountNonLocked(true);
//                user.setFailedAttempt(0);
//                user.setLockTime(null);
//                userRepository.save(user);
//            }
//
//            if (bookRepository.count() == 0) {
//                Book b1 = new Book();
//                b1.setBookName("Clean Code");
//                b1.setAuthor("Robert C. Martin");
////                b1.setCategory("Programming");
////                b1.setPublisher("Prentice Hall");
//                b1.setDescription("A Handbook of Agile Software Craftsmanship.");
//                b1.setPrice(300000);
//                b1.setDiscount(10);
//                b1.calculateDiscountPrice();
//                b1.setStock(20);
//                b1.setIsActive(true);
//                b1.setCreatedDate(LocalDateTime.now());
//                b1.setImage("https://salt.tikicdn.com/cache/750x750/ts/product/5f/d7/35/d6a086d2450d364198cd07ebef63d8a7.jpg.webp");
//                b1.setIsbn("9780132350884");
//
//                Book b2 = new Book();
//                b2.setBookName("The Pragmatic Programmer");
//                b2.setAuthor("Andrew Hunt, David Thomas");
////                b2.setCategory("Programming");
////                b2.setPublisher("Addison-Wesley");
//                b2.setDescription("Your journey to mastery.");
//                b2.setPrice(200000);
//                b2.setDiscount(15);
//                b2.calculateDiscountPrice();
//                b2.setStock(15);
//                b2.setIsActive(true);
//                b2.setCreatedDate(LocalDateTime.now());
//                b2.setImage("https://salt.tikicdn.com/cache/750x750/ts/product/26/0a/27/c8e87ec1405dacc5be3939074541906e.jpg.webp");
//                b2.setIsbn("9780201616224");
//
//                Book b3 = new Book();
//                b3.setBookName("Đi Trốn");
//                b3.setAuthor("Bình Ca");
////                b3.setCategory("Văn học Việt Nam");
////                b3.setPublisher("Nhã Nam");
//                b3.setDescription("Một hành trình đi trốn của tuổi trẻ, sâu lắng và đầy cảm xúc.");
//                b3.setPrice(150000);
//                b3.setDiscount(10);
//                b3.calculateDiscountPrice();
//                b3.setStock(30);
//                b3.setIsActive(true);
//                b3.setCreatedDate(LocalDateTime.now());
//                b3.setImage("https://down-vn.img.susercontent.com/file/b3e8d09b5f616aafddefed0ad52cf8c0");
//                b3.setIsbn("8935235222341");
//
//                Book b4 = new Book();
//                b4.setBookName("Cho tôi xin một vé đi tuổi thơ");
//                b4.setAuthor("Nguyễn Nhật Ánh");
////                b4.setCategory("Thiếu nhi");
////                b4.setPublisher("NXB Trẻ");
//                b4.setDescription("Một vé trở về tuổi thơ đầy hoài niệm và trong trẻo.");
//                b4.setPrice(180000);
//                b4.setDiscount(10);
//                b4.calculateDiscountPrice();
//                b4.setStock(50);
//                b4.setIsActive(true);
//                b4.setCreatedDate(LocalDateTime.now());
//                b4.setImage("https://www.netabooks.vn/Data/Sites/1/Product/17150/cho-toi-xin-mot-ve-di-tuoi-tho-tai-ban-2022.jpg");
//                b4.setIsbn("9786042098323");
//
//                bookRepository.saveAll(List.of(b1, b2, b3, b4));
//            }
//        };
//    }
//}
