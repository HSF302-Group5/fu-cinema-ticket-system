package com.fu.cinema.desktop.facade;

import com.fu.cinema.desktop.model.MovieItem;
import com.fu.cinema.desktop.model.PageResult;
import com.fu.cinema.desktop.model.RoomItem;
import com.fu.cinema.desktop.model.ShowtimeRow;
import com.fu.cinema.desktop.session.DesktopSession;
import com.fu.cinema.dto.ShowtimeForm;
import com.fu.cinema.service.MovieService;
import com.fu.cinema.service.RoomService;
import com.fu.cinema.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShowtimeFacade {

    private final ShowtimeService showtimeService;
    private final MovieService movieService;
    private final RoomService roomService;
    private final DesktopSession session;

    public PageResult<ShowtimeRow> search(Long movieId, Long roomId, LocalDate date, int page) {
        return PageResult.of(
                showtimeService.search(movieId, roomId, date, page, 10),
                ShowtimeRow::from
        );
    }

    public List<MovieItem> schedulableMovies() {
        return movieService.findSchedulable().stream()
                .map(m -> new MovieItem(m.getMovieId(), m.getTitle(), m.getDuration(), m.getAgeRating()))
                .toList();
    }

    public List<RoomItem> activeRooms() {
        return roomService.findActive().stream()
                .map(r -> new RoomItem(r.getRoomId(), r.getRoomName()))
                .toList();
    }

    public ShowtimeForm getForm(Long id) {
        return showtimeService.getForm(id);
    }

    @Transactional
    public void save(ShowtimeForm form) {
        session.requireAdmin();
        if (form.getShowtimeId() == null) {
            showtimeService.create(form);
        } else {
            showtimeService.update(form.getShowtimeId(), form);
        }
    }

    @Transactional
    public boolean delete(Long id) {
        session.requireAdmin();
        return showtimeService.delete(id);
    }
}
