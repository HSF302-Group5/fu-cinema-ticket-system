package com.fu.cinema.desktop.model;

import com.fu.cinema.entity.Room;
import com.fu.cinema.enums.RoomStatus;
import com.fu.cinema.enums.RoomType;

public record RoomRow(
        Long id,
        String name,
        RoomType type,
        int rows,
        int seatsPerRow,
        RoomStatus status
) {
    public int capacity() {
        return rows * seatsPerRow;
    }

    public static RoomRow from(Room r) {
        return new RoomRow(
                r.getRoomId(),
                r.getRoomName(),
                r.getRoomType(),
                r.getTotalRows(),
                r.getSeatsPerRow(),
                r.getStatus()
        );
    }
}
