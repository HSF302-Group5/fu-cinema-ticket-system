package com.fu.cinema.controller.customer;

import com.fu.cinema.service.BookingService;
import com.fu.cinema.service.ShowtimeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customer")
public class CustomerBookingController {

    private final ShowtimeService showtimeService;
    private final BookingService bookingService;

    public CustomerBookingController(ShowtimeService showtimeService, BookingService bookingService) {
        this.showtimeService = showtimeService;
        this.bookingService = bookingService;
    }

    // Lấy sơ đồ ghế và danh sách ghế đã bán của suất chiếu
    @GetMapping("/showtimes/{id}/seats")
    public String seats(@PathVariable Long id, Model model) {
        model.addAttribute("seatMap", showtimeService.getSeatMap(id));
        return "customer/seats";
    }
}
