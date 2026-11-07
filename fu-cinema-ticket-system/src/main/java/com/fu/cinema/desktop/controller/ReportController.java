package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.facade.ReportFacade;
import com.fu.cinema.desktop.model.ReportBookingRow;
import com.fu.cinema.desktop.model.ReportData;
import com.fu.cinema.desktop.model.RevenueRow;
import com.fu.cinema.desktop.util.AlertUtil;
import com.fu.cinema.desktop.util.CsvExporter;
import com.fu.cinema.desktop.util.FxAsync;
import com.fu.cinema.desktop.util.MoneyUtil;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ReportController {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("HH:mm  dd/MM/yyyy");

    private final ReportFacade facade;
    private final FxAsync fxAsync;

    @FXML private DatePicker dpStart;
    @FXML private DatePicker dpEnd;
    @FXML private Button btnGenerate;
    @FXML private Button btnExport;
    @FXML private Label lblEmpty;
    @FXML private Label lblTotals;
    @FXML private BarChart<String, Number> chartRevenue;
    @FXML private PieChart pieTickets;
    @FXML private TableView<ReportBookingRow> tableBookings;
    @FXML private TableColumn<ReportBookingRow, Long> colId;
    @FXML private TableColumn<ReportBookingRow, String> colBookingDate;
    @FXML private TableColumn<ReportBookingRow, String> colCustomer;
    @FXML private TableColumn<ReportBookingRow, String> colMovie;
    @FXML private TableColumn<ReportBookingRow, String> colTotal;

    private final ObjectProperty<ReportData> currentData = new SimpleObjectProperty<>();
    private final ObservableList<ReportBookingRow> bookingRows = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // 1. Thiết lập ngày mặc định: Đầu tháng hiện tại đến ngày hôm nay
        dpStart.setValue(LocalDate.now().withDayOfMonth(1));
        dpEnd.setValue(LocalDate.now());

        // Chặn không cho chọn End < Start trên giao diện UI
        dpEnd.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || (dpStart.getValue() != null && date.isBefore(dpStart.getValue())));
            }
        });

        // 2. Tắt animation biểu đồ để vẽ mượt và tránh xung đột rendering
        chartRevenue.setAnimated(false);
        pieTickets.setAnimated(false);

        // 3. Cấu hình bảng danh sách đơn đặt vé qua Lambda
        colId.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().id()));
        colBookingDate.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().bookingDate() != null ? c.getValue().bookingDate().format(DATE_TIME) : ""));
        colCustomer.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().customerName()));
        colMovie.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().movieTitle()));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(MoneyUtil.format(c.getValue().total())));

        tableBookings.setItems(bookingRows);

        // 4. Binding nút Xuất CSV: Tự động vô hiệu hoá khi báo cáo không có dữ liệu
        btnExport.disableProperty().bind(Bindings.createBooleanBinding(
                () -> currentData.get() == null || currentData.get().isEmpty(), currentData));

        lblEmpty.managedProperty().bind(lblEmpty.visibleProperty());

        // 5. Tự động tải báo cáo lần đầu
        onGenerate();
    }

    @FXML
    private void onGenerate() {
        LocalDate start = dpStart.getValue();
        LocalDate end = dpEnd.getValue();

        fxAsync.run(() -> facade.generate(start, end), this::renderReport, btnGenerate);
    }

    private void renderReport(ReportData d) {
        currentData.set(d);
        lblEmpty.setVisible(d.isEmpty());

        bookingRows.setAll(d.bookings());
        lblTotals.setText("Tổng kết: %d đơn  ·  %d vé  ·  %s".formatted(
                d.totalBookings(), d.totalTickets(), MoneyUtil.format(d.totalRevenue())));

        // Vẽ BarChart doanh thu theo phim
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");
        for (RevenueRow r : d.revenue()) {
            series.getData().add(new XYChart.Data<>(r.title(), r.revenue()));
        }
        chartRevenue.getData().setAll(List.of(series));

        // Cài đặt Tooltip chi tiết cho từng cột của BarChart
        Platform.runLater(() -> {
            for (int i = 0; i < series.getData().size() && i < d.revenue().size(); i++) {
                RevenueRow r = d.revenue().get(i);
                XYChart.Data<String, Number> item = series.getData().get(i);
                if (item.getNode() != null) {
                    Tooltip.install(item.getNode(), new Tooltip(
                            "%s: %d vé – %s".formatted(r.title(), r.tickets(), MoneyUtil.format(r.revenue()))));
                }
            }
        });

        // Vẽ PieChart tỷ trọng số vé
        pieTickets.getData().setAll(d.revenue().stream()
                .map(r -> new PieChart.Data(r.title() + " (" + r.tickets() + " vé)", r.tickets()))
                .toList());
    }

    @FXML
    private void onExport() {
        ReportData snapshot = currentData.get();
        if (snapshot == null || snapshot.isEmpty()) {
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu báo cáo doanh thu CSV");
        fc.setInitialFileName("BaoCaoDoanhThu_%s_%s.csv".formatted(snapshot.start(), snapshot.end()));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tệp CSV (*.csv)", "*.csv"));

        File file = fc.showSaveDialog(btnExport.getScene().getWindow());
        if (file == null) {
            return; // Người dùng bấm Cancel -> không làm gì, không văng lỗi
        }

        // Xuất file chạy trên Virtual Thread qua FxAsync (TECH-15)
        fxAsync.runVoid(
                () -> CsvExporter.write(file.toPath(), snapshot),
                () -> AlertUtil.info("Xuất file CSV thành công: " + file.getName()),
                btnExport
        );
    }
}