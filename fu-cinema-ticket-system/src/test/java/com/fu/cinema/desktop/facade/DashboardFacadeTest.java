package com.fu.cinema.desktop.facade;

import com.fu.cinema.desktop.model.DashboardData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DashboardFacadeTest {

    @Autowired
    private DashboardFacade dashboardFacade;

    @Test
    @DisplayName("DashboardFacade load data thành công với đầy đủ các chỉ số KPI và biểu đồ phòng")
    void load_shouldReturnDashboardData() {
        DashboardData data = dashboardFacade.load();

        assertNotNull(data, "DashboardData không được null");
        assertTrue(data.showingMovies() >= 0, "Số phim đang chiếu phải >= 0");
        assertTrue(data.customers() >= 0, "Số khách hàng phải >= 0");
        assertTrue(data.pendingBookings() >= 0, "Số đơn pending phải >= 0");
        assertNotNull(data.todayByRoom(), "Danh sách suất chiếu theo phòng không được null");
    }
}
