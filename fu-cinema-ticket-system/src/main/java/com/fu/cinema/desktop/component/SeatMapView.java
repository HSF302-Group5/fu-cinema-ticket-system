package com.fu.cinema.desktop.component;

import com.fu.cinema.desktop.model.SeatMapData;
import com.fu.cinema.desktop.util.AlertUtil;
import com.fu.cinema.dto.SeatMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.*;

/**
 * Component độc lập hiển thị sơ đồ ghế và xử lý chọn ghế (TODO 42 - BR-10).
 */
public class SeatMapView extends VBox {

    public static final int MAX_SEATS = 8;

    private static final PseudoClass SOLD_PSEUDO = PseudoClass.getPseudoClass("sold");
    private static final PseudoClass SELECTED_PSEUDO = PseudoClass.getPseudoClass("selected");

    private final ObservableList<String> selectedSeats = FXCollections.observableArrayList();
    private final ObservableList<String> unmodifiableSelectedSeats = FXCollections.unmodifiableObservableList(selectedSeats);

    private final Map<String, Button> seatButtons = new HashMap<>();

    private final VBox screenBox = new VBox();
    private final GridPane gridPane = new GridPane();
    private final HBox legendBox = new HBox();

    /**
     * Constructor rỗng bắt buộc để nhúng trực tiếp trong FXML.
     */
    public SeatMapView() {
        setAlignment(Pos.TOP_CENTER);
        setSpacing(20);
        getStyleClass().add("seat-map-view");

        initScreenBanner();
        initGrid();
        initLegend();

        getChildren().addAll(screenBox, gridPane, legendBox);
    }

    public ObservableList<String> getSelectedSeats() {
        return unmodifiableSelectedSeats;
    }

    public void clearSelection() {
        List<String> current = new ArrayList<>(selectedSeats);
        selectedSeats.clear();
        for (String code : current) {
            Button btn = seatButtons.get(code);
            if (btn != null) {
                updateSeatState(btn, false, false);
            }
        }
    }

    public void setSeatMap(SeatMap map) {
        setSeatMap(SeatMapData.from(map));
    }

    public void setSeatMap(SeatMapData data) {
        clearSelection();
        gridPane.getChildren().clear();
        seatButtons.clear();

        if (data == null || data.rowLabels().isEmpty() || data.seatsPerRow() <= 0) {
            return;
        }

        List<String> rows = data.rowLabels();
        int seatsPerRow = data.seatsPerRow();
        Set<String> sold = data.soldSeats();

        for (int r = 0; r < rows.size(); r++) {
            String rowLabel = rows.get(r);

            // Nhãn hàng bên trái
            Label lblLeft = new Label(rowLabel);
            lblLeft.getStyleClass().add("seat-row-label");
            lblLeft.setMinSize(28, 32);
            lblLeft.setAlignment(Pos.CENTER);
            gridPane.add(lblLeft, 0, r);

            // Các ghế trong hàng
            for (int c = 1; c <= seatsPerRow; c++) {
                String seatCode = rowLabel + c;
                Button seatBtn = new Button(String.valueOf(c));
                seatBtn.getStyleClass().add("seat");
                seatBtn.setMinSize(36, 32);
                seatBtn.setPrefSize(36, 32);
                seatBtn.setMaxSize(36, 32);
                seatBtn.setTooltip(new Tooltip("Ghế " + seatCode));

                boolean isSold = sold != null && sold.contains(seatCode);
                updateSeatState(seatBtn, false, isSold);

                if (isSold) {
                    seatBtn.setDisable(true);
                } else {
                    seatBtn.setOnAction(e -> handleSeatClick(seatCode, seatBtn));
                }

                seatButtons.put(seatCode, seatBtn);
                gridPane.add(seatBtn, c, r);
            }

            // Nhãn hàng bên phải
            Label lblRight = new Label(rowLabel);
            lblRight.getStyleClass().add("seat-row-label");
            lblRight.setMinSize(28, 32);
            lblRight.setAlignment(Pos.CENTER);
            gridPane.add(lblRight, seatsPerRow + 1, r);
        }
    }

    private void handleSeatClick(String seatCode, Button btn) {
        if (selectedSeats.contains(seatCode)) {
            // Đang chọn -> Bỏ chọn
            selectedSeats.remove(seatCode);
            updateSeatState(btn, false, false);
        } else {
            // Chưa chọn -> Kiểm tra giới hạn 8 ghế theo quy tắc BR-10 (TC-D14)
            if (selectedSeats.size() >= MAX_SEATS) {
                AlertUtil.warn("Mỗi đơn tối đa 8 ghế");
                return;
            }
            selectedSeats.add(seatCode);
            updateSeatState(btn, true, false);
        }
    }

    private void updateSeatState(Button btn, boolean isSelected, boolean isSold) {
        btn.pseudoClassStateChanged(SELECTED_PSEUDO, isSelected);
        btn.pseudoClassStateChanged(SOLD_PSEUDO, isSold);

        btn.getStyleClass().removeAll("seat-selected", "seat-sold");
        if (isSold) {
            btn.getStyleClass().add("seat-sold");
        } else if (isSelected) {
            btn.getStyleClass().add("seat-selected");
        }
    }

    private void initScreenBanner() {
        screenBox.setAlignment(Pos.CENTER);
        screenBox.setSpacing(4);

        Region screenBar = new Region();
        screenBar.getStyleClass().add("screen-bar");
        screenBar.setPrefWidth(480);
        screenBar.setPrefHeight(6);

        Label lblScreen = new Label("MÀN HÌNH / SCREEN");
        lblScreen.getStyleClass().add("screen-label");

        screenBox.getChildren().addAll(screenBar, lblScreen);
    }

    private void initGrid() {
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(8);
        gridPane.setVgap(8);
    }

    private void initLegend() {
        legendBox.setAlignment(Pos.CENTER);
        legendBox.setSpacing(24);
        legendBox.getStyleClass().add("seat-legend-box");

        legendBox.getChildren().addAll(
                createLegendItem("seat-sample-available", "Ghế trống"),
                createLegendItem("seat-sample-selected", "Đang chọn"),
                createLegendItem("seat-sample-sold", "Đã bán")
        );
    }

    private HBox createLegendItem(String sampleClass, String text) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER);

        Region sample = new Region();
        sample.setMinSize(22, 22);
        sample.setPrefSize(22, 22);
        sample.setMaxSize(22, 22);
        sample.getStyleClass().addAll("seat-sample", sampleClass);

        Label label = new Label(text);
        label.getStyleClass().add("legend-label");

        box.getChildren().addAll(sample, label);
        return box;
    }
}
