package com.fu.cinema.controller.customer;

import com.fu.cinema.enums.BookingStatus;
import com.fu.cinema.service.BookingService;
import com.fu.cinema.util.PageUtils;
import com.fu.cinema.util.SessionUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerBookingController {

    private final BookingService bookingService;

    @GetMapping("/bookings")
    public String history(@RequestParam(required = false) BookingStatus status,
                          @RequestParam(defaultValue = "0") int page,
                          HttpSession session, Model model) {
        model.addAttribute("bookings", bookingService.findByCustomer(
                SessionUtils.getCurrentCustomerId(session), status, page, PageUtils.DEFAULT_SIZE));
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("status", status);
        model.addAttribute("pageUrl", PageUtils.currentUrlWithoutPage());
        return "customer/bookings";
    }
}
