package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.component.SeatMapView;
import com.fu.cinema.desktop.model.SeatMapData;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class CounterSaleController {

    @FXML private SeatMapView seatMapView;
    @FXML private Label lblSelectedSeats;
    @FXML private Label lblSeatCount;
    @FXML private Button btnClearSeats;

    @FXML
    private void initialize() {
        // Dữ liệu mẫu phòng chiếu 8 hàng (A -> H) x 12 ghế = 96 ghế (TC-D13)
        List<String> rows = List.of("A", "B", "C", "D", "E", "F", "G", "H");
        Set<String> sold = Set.of("A3", "A4", "B6", "C5", "C6", "D7", "F8");
        SeatMapData sampleData = new SeatMapData(rows, 12, sold);

        seatMapView.setSeatMap(sampleData);

        // Lắng nghe danh sách ghế đang chọn từ component SeatMapView
        seatMapView.getSelectedSeats().addListener((ListChangeListener<String>) c -> updateSelectionDisplay());

        updateSelectionDisplay();
    }

    private void updateSelectionDisplay() {
        var seats = seatMapView.getSelectedSeats();
        if (seats.isEmpty()) {
            lblSelectedSeats.setText("Chưa chọn ghế nào");
            lblSeatCount.setText("0 / 8");
            btnClearSeats.setDisable(true);
        } else {
            lblSelectedSeats.setText(String.join(", ", seats));
            lblSeatCount.setText(seats.size() + " / 8");
            btnClearSeats.setDisable(false);
        }
    }

    @FXML
    private void onClearSeats() {
        seatMapView.clearSelection();
    }
}
