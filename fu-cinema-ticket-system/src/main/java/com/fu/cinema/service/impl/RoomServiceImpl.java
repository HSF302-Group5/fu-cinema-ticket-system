package com.fu.cinema.service.impl;

import com.fu.cinema.dto.RoomForm;
import com.fu.cinema.entity.Room;
import com.fu.cinema.enums.RoomStatus;
import com.fu.cinema.exception.BusinessException;
import com.fu.cinema.exception.ResourceNotFoundException;
import com.fu.cinema.repository.RoomRepository;
import com.fu.cinema.repository.ShowtimeRepository;
import com.fu.cinema.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final ShowtimeRepository showtimeRepository;

    @Override
    public List<Room> findAll() {
        return roomRepository.findAll(Sort.by(Sort.Direction.ASC, "roomName"));
    }

    @Override
    public List<Room> findActive() {
        return roomRepository.findByStatusOrderByRoomNameAsc(RoomStatus.ACTIVE);
    }

    @Override
    public RoomForm getForm(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng chiếu", roomId));

        RoomForm form = new RoomForm();
        form.setRoomId(room.getRoomId());
        form.setRoomName(room.getRoomName());
        form.setRoomType(room.getRoomType());
        form.setTotalRows(room.getTotalRows());
        form.setSeatsPerRow(room.getSeatsPerRow());
        form.setStatus(room.getStatus());
        return form;
    }

    @Override
    @Transactional
    public void create(RoomForm form) {
        String trimmedName = form.getRoomName().trim();
        if (roomRepository.existsByRoomNameIgnoreCase(trimmedName)) {
            throw new BusinessException("Tên phòng chiếu '" + trimmedName + "' đã tồn tại");
        }

        Room room = new Room(
                trimmedName,
                form.getRoomType(),
                form.getTotalRows(),
                form.getSeatsPerRow(),
                form.getStatus());
        roomRepository.save(room);
    }

    @Override
    @Transactional
    public void update(Long roomId, RoomForm form) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng chiếu", roomId));

        String trimmedName = form.getRoomName().trim();
        if (roomRepository.existsByRoomNameIgnoreCaseAndRoomIdNot(trimmedName, roomId)) {
            throw new BusinessException("Tên phòng chiếu '" + trimmedName + "' đã được sử dụng bởi phòng khác");
        }

        room.setRoomName(trimmedName);
        room.setRoomType(form.getRoomType());
        room.setTotalRows(form.getTotalRows());
        room.setSeatsPerRow(form.getSeatsPerRow());
        room.setStatus(form.getStatus());
        roomRepository.save(room);
    }

    @Override
    @Transactional
    public boolean delete(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng chiếu", roomId));

        // Ràng buộc TODO 22: Nếu phòng đã có suất chiếu thì không cho xoá mà chuyển
        // MAINTENANCE
        if (showtimeRepository.existsByRoom_RoomId(roomId)) {
            room.setStatus(RoomStatus.MAINTENANCE);
            roomRepository.save(room);
            return false; // Trả về false biểu thị chỉ chuyển trạng thái bảo trì
        }

        roomRepository.delete(room);
        return true; // Trả về true biểu thị đã xoá hẳn khỏi DB
    }
}
