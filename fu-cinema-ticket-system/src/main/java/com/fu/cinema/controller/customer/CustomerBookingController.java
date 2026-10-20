package com.fu.cinema.controller.customer;

import com.fu.cinema.dto.BookingRequest;
import com.fu.cinema.entity.Booking;
import com.fu.cinema.enums.BookingStatus;
import com.fu.cinema.exception.BusinessException;
import com.fu.cinema.service.BookingService;
import com.fu.cinema.service.ShowtimeService;
import com.fu.cinema.util.PageUtils;
import com.fu.cinema.util.SessionUtils;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer")
public class CustomerBookingController {

    private final ShowtimeService showtimeService;
    private final BookingService bookingService;

    public CustomerBookingController(ShowtimeService showtimeService, BookingService bookingService) {
        this.showtimeService = showtimeService;
        this.bookingService = bookingService;
    }

    // 1. Sơ đồ ghế
    @GetMapping("/showtimes/{id}/seats")
    public String seats(@PathVariable Long id, Model model) {
        model.addAttribute("seatMap", showtimeService.getSeatMap(id));
        return "customer/seats";
    }

    // 2. Đặt vé chọn ghế
    @PostMapping("/bookings")
    public String book(@Valid @ModelAttribute BookingRequest request,
                       BindingResult br,
                       HttpSession session,
                       RedirectAttributes ra) {
        String back = "redirect:/customer/showtimes/" + request.getShowtimeId() + "/seats";
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", br.getAllErrors().get(0).getDefaultMessage());
            return back;
        }
        try {
            Booking booking = bookingService.create(SessionUtils.getCurrentCustomerId(session), request);
            ra.addFlashAttribute("success", "Đặt vé thành công! Mã đơn #" + booking.getBookingId());
            return "redirect:/customer/bookings/" + booking.getBookingId();
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return back;
        }
    }

    // 3. Lịch sử đặt vé
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

    /**
     * Xem chi tiết đơn vé.
     * Chống lỗi bảo mật IDOR: getDetailForCustomer ném ResourceNotFoundException (404)
     * nếu đơn không thuộc về khách hàng đang đăng nhập, không tiết lộ sự tồn tại của đơn.
     */
    // 4. Chi tiết đơn đặt vé của khách
    @GetMapping("/bookings/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        model.addAttribute("booking", bookingService.getDetailForCustomer(id, SessionUtils.getCurrentCustomerId(session)));
        return "customer/booking-detail";
    }

    /**
     * Huỷ đơn vé theo quy tắc nghiệp vụ BR-14 (trước giờ chiếu ít nhất 2 giờ).
     * Kiểm tra quyền sở hữu đơn trước khi huỷ.
     */
    // 5. Khách huỷ đơn (trước giờ chiếu >= 2 tiếng)
    @PostMapping("/bookings/{id}/cancel")
    public String cancel(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        try {
            bookingService.cancelByCustomer(id, SessionUtils.getCurrentCustomerId(session));
            ra.addFlashAttribute("success", "Đã huỷ đơn #" + id);
        } catch (BusinessException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customer/bookings/" + id;
    }
}
