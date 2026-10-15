package com.fu.cinema.config;

import com.fu.cinema.entity.*;
import com.fu.cinema.enums.*;
import com.fu.cinema.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final GenreRepository genreRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final ShowtimeRepository showtimeRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (accountRepository.count() > 0) {
            return;                                                          // chỉ seed khi DB rỗng
        }
        log.info(">>> Seeding sample data...");

        // ---- Accounts & Customers ----
        accountRepository.save(new Account("admin@fucinema.vn", passwordEncoder.encode("Admin@123"),
                Role.ADMIN, AccountStatus.ACTIVE));
        Customer an = customer("Nguyen Van An", "an.nv@gmail.com", "0901234567", LocalDate.of(2000, 5, 12), "079200001234");
        Customer binh = customer("Tran Thi Binh", "binh.tt@gmail.com", "0912345678", LocalDate.now().minusYears(15), "079209005678");

        // ---- Genres ----
        Genre action = genreRepository.save(new Genre("Hành động", "Phim hành động, kịch tính"));
        Genre anime = genreRepository.save(new Genre("Hoạt hình", "Phim hoạt hình cho mọi lứa tuổi"));
        Genre horror = genreRepository.save(new Genre("Kinh dị", "Phim kinh dị, giật gân"));
        Genre romance = genreRepository.save(new Genre("Tình cảm", "Phim tình cảm, lãng mạn"));
        genreRepository.save(new Genre("Khoa học viễn tưởng", "Phim khoa học viễn tưởng"));

        // ---- Movies ----
        Movie m1 = movie("Biệt Đội Siêu Tốc", "Lê Minh", 120, AgeRating.T13, action, MovieStatus.SHOWING, -10);
        Movie m2 = movie("Chú Mèo Phiêu Lưu", "Trần Hà", 95, AgeRating.P, anime, MovieStatus.SHOWING, -20);
        Movie m3 = movie("Ngôi Nhà Bí Ẩn", "Phạm Quân", 105, AgeRating.T18, horror, MovieStatus.SHOWING, -5);
        movie("Mùa Hè Năm Ấy", "Đỗ Lan", 110, AgeRating.T16, romance, MovieStatus.COMING_SOON, 14);
        movie("Vũ Trụ Song Song", "Ngô Tùng", 140, AgeRating.T13, action, MovieStatus.COMING_SOON, 30);
        Movie m6 = movie("Huyền Thoại Cũ", "Vũ Bình", 100, AgeRating.P, anime, MovieStatus.STOPPED, -120);
    }

    private Customer customer(String name, String email, String mobile, LocalDate birthday, String cccd) {
        Account acc = accountRepository.save(new Account(email, passwordEncoder.encode("Customer@123"),
                Role.CUSTOMER, AccountStatus.ACTIVE));
        Customer c = new Customer();
        c.setFullName(name);
        c.setEmail(email);
        c.setMobile(mobile);
        c.setBirthday(birthday);
        c.setIdentityCard(cccd);
        c.setAccount(acc);
        return customerRepository.save(c);
    }

    private Movie movie(String title, String director, int duration, AgeRating rating, Genre genre,
                        MovieStatus status, int releaseOffsetDays) {
        Movie m = new Movie();
        m.setTitle(title);
        m.setDirector(director);
        m.setDuration(duration);
        m.setAgeRating(rating);
        m.setGenre(genre);
        m.setStatus(status);
        m.setReleaseDate(LocalDate.now().plusDays(releaseOffsetDays));
        m.setDescription("Nội dung phim " + title + ".");
        m.setPosterUrl("https://placehold.co/300x450?text=" + title.replace(' ', '+'));
        return movieRepository.save(m);
    }
}
