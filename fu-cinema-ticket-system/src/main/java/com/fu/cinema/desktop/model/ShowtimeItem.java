package com.fu.cinema.desktop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public record ShowtimeItem(Long id, LocalDateTime start, String roomName, BigDecimal price) {

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm EEE dd/MM", Locale.of("vi", "VN"));
        return (start != null ? start.format(fmt) : "") + " · " + roomName;
    }
}
