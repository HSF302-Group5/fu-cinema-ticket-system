package com.fu.cinema.dto;

import com.fu.cinema.enums.RoomStatus;
import com.fu.cinema.enums.RoomType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomForm {

    private Long roomId;

    @NotBlank(message = "Tên phòng chiếu không được để trống")
    @Size(max = 50, message = "Tên phòng chiếu tối đa 50 ký tự")
    private String roomName;

    @NotNull(message = "Vui lòng chọn loại phòng chiếu")
    private RoomType roomType;

    @NotNull(message = "Vui lòng nhập số hàng ghế")
    @Min(value = 1, message = "Số hàng ghế tối thiểu là 1")
    @Max(value = 26, message = "Số hàng ghế tối đa là 26 (tương ứng hàng A đến Z)")
    private Integer totalRows;

    @NotNull(message = "Vui lòng nhập số ghế mỗi hàng")
    @Min(value = 1, message = "Số ghế mỗi hàng tối thiểu là 1")
    @Max(value = 30, message = "Số ghế mỗi hàng tối đa là 30")
    private Integer seatsPerRow;

    @NotNull(message = "Vui lòng chọn trạng thái phòng")
    private RoomStatus status = RoomStatus.ACTIVE;
}
