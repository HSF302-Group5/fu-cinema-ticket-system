package com.fu.cinema.desktop.facade;

import com.fu.cinema.desktop.model.RoomItem;
import com.fu.cinema.desktop.model.RoomRow;
import com.fu.cinema.desktop.session.DesktopSession;
import com.fu.cinema.dto.RoomForm;
import com.fu.cinema.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoomFacade {

    private final RoomService roomService;
    private final DesktopSession session;

    public List<RoomRow> findAll() {
        return roomService.findAll().stream()
                .map(RoomRow::from)
                .toList();
    }

    public List<RoomItem> findActive() {
        return roomService.findActive().stream()
                .map(r -> new RoomItem(r.getRoomId(), r.getRoomName()))
                .toList();
    }

    public RoomForm getForm(Long id) {
        return roomService.getForm(id);
    }

    @Transactional
    public void save(RoomForm form) {
        session.requireAdmin();
        if (form.getRoomId() == null) {
            roomService.create(form);
        } else {
            roomService.update(form.getRoomId(), form);
        }
    }

    @Transactional
    public boolean delete(Long id) {
        session.requireAdmin();
        return roomService.delete(id);
    }
}
