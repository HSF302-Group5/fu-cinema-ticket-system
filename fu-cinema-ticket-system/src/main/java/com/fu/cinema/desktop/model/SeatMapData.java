package com.fu.cinema.desktop.model;

import com.fu.cinema.dto.SeatMap;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record SeatMapData(
        List<String> rowLabels,
        int seatsPerRow,
        Set<String> soldSeats
) {
    public SeatMapData {
        rowLabels = rowLabels == null ? List.of() : List.copyOf(rowLabels);
        soldSeats = soldSeats == null ? Set.of() : Collections.unmodifiableSet(new HashSet<>(soldSeats));
    }

    public boolean isSold(String seatCode) {
        return soldSeats.contains(seatCode);
    }

    public static SeatMapData from(SeatMap map) {
        if (map == null) {
            return new SeatMapData(List.of(), 0, Set.of());
        }
        return new SeatMapData(map.getRowLabels(), map.getSeatsPerRow(), map.getSoldSeats());
    }
}
