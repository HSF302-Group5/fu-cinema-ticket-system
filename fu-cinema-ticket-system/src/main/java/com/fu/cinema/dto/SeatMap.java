package com.fu.cinema.dto;

import com.fu.cinema.entity.Showtime;
import lombok.Getter;

import java.util.List;
import java.util.Set;

// Dữ liệu phục vụ cho màn hình sơ đồ chọn ghế (TODO 23 & TODO 28)
@Getter
public class SeatMap {

    private final Showtime showtime;
    private final List<String> rowLabels; // A, B, C...
    private final int seatsPerRow;
    private final Set<String> soldSeats;

    public SeatMap(Showtime showtime, List<String> rowLabels, int seatsPerRow, Set<String> soldSeats) {
        this.showtime = showtime;
        this.rowLabels = rowLabels;
        this.seatsPerRow = seatsPerRow;
        this.soldSeats = soldSeats;
    }

    public boolean isSold(String seatCode) {
        return soldSeats != null && soldSeats.contains(seatCode);
    }
}
