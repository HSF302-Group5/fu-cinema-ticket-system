package com.fu.cinema.desktop.component;

import com.fu.cinema.desktop.model.SeatMapData;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SeatMapViewTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            latch.await(3, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // Toolkit đã chạy hoặc môi trường headless
        }
    }

    @Test
    @DisplayName("SeatMapView khởi tạo ban đầu danh sách ghế trống")
    void testInitialSelection() {
        SeatMapView view = new SeatMapView();
        assertNotNull(view.getSelectedSeats());
        assertTrue(view.getSelectedSeats().isEmpty());
        assertEquals(8, SeatMapView.MAX_SEATS, "Giới hạn tối đa phải là 8 theo BR-10");
    }

    @Test
    @DisplayName("SeatMapData kiểm tra ghế đã bán chính xác")
    void testSeatMapData() {
        SeatMapData data = new SeatMapData(List.of("A", "B"), 10, Set.of("A1", "A2"));
        assertTrue(data.isSold("A1"));
        assertTrue(data.isSold("A2"));
        assertFalse(data.isSold("A3"));
        assertEquals(2, data.rowLabels().size());
        assertEquals(10, data.seatsPerRow());
    }

    @Test
    @DisplayName("SeatMapView nạp dữ liệu SeatMapData thành công")
    void testSetSeatMap() {
        SeatMapView view = new SeatMapView();
        SeatMapData data = new SeatMapData(List.of("A", "B", "C"), 12, Set.of("A1", "B5"));
        view.setSeatMap(data);
        assertTrue(view.getSelectedSeats().isEmpty());
    }
}
