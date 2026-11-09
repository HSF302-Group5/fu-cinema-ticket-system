package com.fu.cinema.desktop.facade;

import com.fu.cinema.desktop.model.MovieItem;
import com.fu.cinema.desktop.model.PageResult;
import com.fu.cinema.desktop.model.RoomItem;
import com.fu.cinema.desktop.model.RoomRow;
import com.fu.cinema.desktop.model.ShowtimeRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ScheduleFacadeTest {

    @Autowired
    private RoomFacade roomFacade;

    @Autowired
    private ShowtimeFacade showtimeFacade;

    @Test
    @DisplayName("RoomFacade: Lấy danh sách phòng chiếu thành công")
    void roomFacade_findAll_shouldReturnRooms() {
        List<RoomRow> rooms = roomFacade.findAll();
        assertNotNull(rooms, "Danh sách phòng không được null");
        assertFalse(rooms.isEmpty(), "Phải có dữ liệu phòng mẫu");
        RoomRow first = rooms.get(0);
        assertTrue(first.capacity() > 0, "Sức chứa phòng phải > 0");
    }

    @Test
    @DisplayName("RoomFacade: Lấy danh sách phòng ACTIVE")
    void roomFacade_findActive_shouldReturnActiveRooms() {
        List<RoomItem> rooms = roomFacade.findActive();
        assertNotNull(rooms);
    }

    @Test
    @DisplayName("ShowtimeFacade: Tìm kiếm suất chiếu phân trang thành công")
    void showtimeFacade_search_shouldReturnPageResult() {
        PageResult<ShowtimeRow> result = showtimeFacade.search(null, null, null, 0);
        assertNotNull(result, "Kết quả tìm kiếm không được null");
        assertNotNull(result.items(), "Items không được null");
    }

    @Test
    @DisplayName("ShowtimeFacade: Lấy danh sách phim có thể xếp lịch")
    void showtimeFacade_schedulableMovies_shouldReturnMovies() {
        List<MovieItem> movies = showtimeFacade.schedulableMovies();
        assertNotNull(movies, "Danh sách phim không được null");
    }
}
