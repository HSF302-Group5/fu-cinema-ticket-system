package com.fu.cinema.desktop.facade;

import com.fu.cinema.desktop.model.CustomerItem;
import com.fu.cinema.desktop.model.MovieItem;
import com.fu.cinema.desktop.model.SaleResult;
import com.fu.cinema.desktop.model.SeatMapData;
import com.fu.cinema.desktop.model.ShowtimeItem;
import com.fu.cinema.desktop.session.DesktopSession;
import com.fu.cinema.dto.BookingRequest;
import com.fu.cinema.entity.Booking;
import com.fu.cinema.enums.BookingStatus;
import com.fu.cinema.service.BookingService;
import com.fu.cinema.service.CustomerService;
import com.fu.cinema.service.MovieService;
import com.fu.cinema.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CounterSaleFacade {

    private final CustomerService customerService;
    private final MovieService movieService;
    private final ShowtimeService showtimeService;
    private final BookingService bookingService;
    private final DesktopSession session;

    public List<CustomerItem> searchCustomers(String keyword) {
        return customerService.search(keyword, 0, 20).map(CustomerItem::from).getContent();
    }

    public List<MovieItem> movies() {
        return movieService.findSchedulable().stream()
                .map(m -> new MovieItem(m.getMovieId(), m.getTitle(), m.getDuration(), m.getAgeRating()))
                .toList();
    }

    public List<ShowtimeItem> showtimes(Long movieId) {
        return showtimeService.findUpcomingByMovie(movieId).stream()
                .map(s -> new ShowtimeItem(s.getShowtimeId(), s.getStartTime(), s.getRoom().getRoomName(), s.getTicketPrice()))
                .toList();
    }

    public SeatMapData seatMap(Long showtimeId) {
        return SeatMapData.from(showtimeService.getSeatMap(showtimeId));
    }

    @Transactional // ⚠ Ghi đè readOnly: đảm bảo create và confirm cùng 1 transaction
    public SaleResult sell(Long customerId, Long showtimeId, List<String> seats, boolean paid) {
        session.requireAdmin();

        BookingRequest request = new BookingRequest();
        request.setShowtimeId(showtimeId);
        request.setSeatCodes(new ArrayList<>(seats));

        // BookingService.create thực hiện khoá bi quan PESSIMISTIC_WRITE để chống tranh chấp ghế (TC-D16)
        Booking booking = bookingService.create(customerId, request);

        // Quy tắc BR-D2: Nếu tick "Đã thanh toán" -> chuyển ngay sang CONFIRMED
        if (paid) {
            bookingService.confirm(booking.getBookingId());
        }

        BookingStatus finalStatus = paid ? BookingStatus.CONFIRMED : booking.getStatus();
        return new SaleResult(
                booking.getBookingId(),
                seats.stream().sorted(SEAT_ORDER).toList(),
                booking.getTotalAmount(),
                finalStatus
        );
    }

    public static final Comparator<String> SEAT_ORDER = Comparator
            .comparing((String s) -> s.charAt(0))
            .thenComparingInt(s -> Integer.parseInt(s.substring(1)));
}
