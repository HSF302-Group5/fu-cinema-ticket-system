package com.fu.cinema.desktop.model;

import com.fu.cinema.entity.Showtime;
import com.fu.cinema.enums.ShowtimeStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShowtimeRow(
        Long id,
        String movieTitle,
        String roomName,
        LocalDateTime start,
        LocalDateTime end,
        BigDecimal price,
        ShowtimeStatus status
) {
    public static ShowtimeRow from(Showtime s) {
        return new ShowtimeRow(
                s.getShowtimeId(),
                s.getMovie().getTitle(),
                s.getRoom().getRoomName(),
                s.getStartTime(),
                s.getEndTime(),
                s.getTicketPrice(),
                s.getStatus()
        );
    }
}
