package com.fu.cinema.dto;

import com.fu.cinema.enums.ShowtimeStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ShowtimeForm {

    private Long showtimeId;

    @NotNull(message = "Vui lòng chọn phim")
    private Long movieId;

    @NotNull(message = "Vui lòng chọn phòng chiếu")
    private Long roomId;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") // Khớp định dạng của <input type="datetime-local">
    private LocalDateTime startTime;

    // Để trống -> Hệ thống tự động tính = StartTime + Duration + 15 phút dọn phòng
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endTime;

    @NotNull(message = "Giá vé không được để trống")
    @DecimalMin(value = "1000", message = "Giá vé tối thiểu 1.000đ")
    private BigDecimal ticketPrice;

    @NotNull(message = "Vui lòng chọn trạng thái")
    private ShowtimeStatus status = ShowtimeStatus.OPEN;
}
