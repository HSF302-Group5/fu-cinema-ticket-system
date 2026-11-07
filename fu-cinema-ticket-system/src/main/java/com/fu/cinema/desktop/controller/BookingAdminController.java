package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.facade.BookingFacade;
import com.fu.cinema.desktop.model.BookingDetail;
import com.fu.cinema.desktop.model.BookingRow;
import com.fu.cinema.desktop.model.PageResult;
import com.fu.cinema.desktop.util.AlertUtil;
import com.fu.cinema.desktop.util.FxAsync;
import com.fu.cinema.desktop.util.MoneyUtil;
import com.fu.cinema.enums.BookingStatus;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class BookingAdminController {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("HH:mm  dd/MM/yyyy");

    private final BookingFacade facade;
    private final FxAsync fxAsync;

    @FXML private Label lblCount;
    @FXML private ComboBox<String> cbStatus;
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;
    @FXML private TextField tfKeyword;
    @FXML private TableView<BookingRow> table;
    @FXML private TableColumn<BookingRow, Long> colId;
    @FXML private TableColumn<BookingRow, String> colCustomer;
    @FXML private TableColumn<BookingRow, String> colMovie;
    @FXML private TableColumn<BookingRow, String> colRoom;
    @FXML private TableColumn<BookingRow, String> colStartTime;
    @FXML private TableColumn<BookingRow, Integer> colSeatCount;
    @FXML private TableColumn<BookingRow, String> colTotal;
    @FXML private TableColumn<BookingRow, String> colBookingDate;
    @FXML private TableColumn<BookingRow, BookingStatus> colStatus;
    @FXML private Pagination pagination;
    @FXML private Button btnConfirm;
    @FXML private Button btnCancel;

    private final ObservableList<BookingRow> rows = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // 1. Khởi tạo ComboBox trạng thái (có "Tất cả")
        cbStatus.getItems().addAll("Tất cả", BookingStatus.PENDING.name(), BookingStatus.CONFIRMED.name(), BookingStatus.CANCELLED.name());
        cbStatus.setValue("Tất cả");

        // 2. Khởi tạo các cột bảng qua Lambda (không dùng PropertyValueFactory)
        colId.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().id()));
        colCustomer.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().customerName()));
        colMovie.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().movieTitle()));
        colRoom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().roomName()));
        colStartTime.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().startTime() != null ? c.getValue().startTime().format(DATE_TIME) : ""));
        colSeatCount.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().seatCount()));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(MoneyUtil.format(c.getValue().total())));
        colBookingDate.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().bookingDate() != null ? c.getValue().bookingDate().format(DATE_TIME) : ""));
        colStatus.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().status()));

        // 3. Tô màu trực quan cho trạng thái
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BookingStatus st, boolean empty) {
                super.updateItem(st, empty);
                getStyleClass().removeAll("st-pending", "st-confirmed", "st-cancelled");
                if (empty || st == null) {
                    setText(null);
                } else {
                    setText(st.name());
                    getStyleClass().add("st-" + st.name().toLowerCase());
                }
            }
        });

        table.setItems(rows);

        // 4. JavaFX Binding: Tự động điều khiển bật/tắt nút tác vụ (TC-D17, TC-D18)
        ReadOnlyObjectProperty<BookingRow> sel = table.getSelectionModel().selectedItemProperty();
        btnConfirm.disableProperty().bind(Bindings.createBooleanBinding(
                () -> sel.get() == null || sel.get().status() != BookingStatus.PENDING, sel));
        btnCancel.disableProperty().bind(Bindings.createBooleanBinding(
                () -> sel.get() == null || sel.get().status() == BookingStatus.CANCELLED, sel));

        // 5. Bắt sự kiện double-click trên dòng để xem chi tiết
        table.setRowFactory(tv -> {
            TableRow<BookingRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openDetail(row.getItem().id());
                }
            });
            return row;
        });

        // 6. Lắng nghe thay đổi trang của Pagination
        pagination.currentPageIndexProperty().addListener((obs, oldIdx, newIdx) -> loadPage(newIdx.intValue()));

        // Tải trang đầu tiên khi mở màn hình
        loadPage(0);
    }

    private void loadPage(int page) {
        BookingStatus status = getSelectedStatus();
        LocalDate from = dpFrom.getValue();
        LocalDate to = dpTo.getValue();
        String kw = tfKeyword.getText() != null ? tfKeyword.getText().trim() : "";

        fxAsync.run(() -> facade.search(status, from, to, kw, page), this::renderPage, table);
    }

    private void renderPage(PageResult<BookingRow> result) {
        rows.setAll(result.items());
        pagination.setPageCount(Math.max(1, result.totalPages()));
        pagination.setCurrentPageIndex(result.page());
        lblCount.setText(result.totalElements() + " đơn đặt vé");
    }

    private BookingStatus getSelectedStatus() {
        String val = cbStatus.getValue();
        if (val == null || "Tất cả".equals(val)) return null;
        return BookingStatus.valueOf(val);
    }

    @FXML
    private void onSearch() {
        // Đổi bộ lọc tự động quay về trang 1
        if (pagination.getCurrentPageIndex() == 0) {
            loadPage(0);
        } else {
            pagination.setCurrentPageIndex(0); // Listener sẽ tự gọi loadPage(0)
        }
    }

    @FXML
    private void onReset() {
        cbStatus.setValue("Tất cả");
        dpFrom.setValue(null);
        dpTo.setValue(null);
        tfKeyword.clear();
        onSearch();
    }

    @FXML
    private void onConfirm() {
        BookingRow r = table.getSelectionModel().getSelectedItem();
        if (r == null) return;
        if (AlertUtil.confirm("Xác nhận đơn đặt vé #" + r.id() + " của khách \"" + r.customerName() + "\"?")) {
            fxAsync.runVoid(
                    () -> facade.confirm(r.id()),
                    () -> {
                        AlertUtil.info("Đã xác nhận đơn hàng #" + r.id() + " thành công!");
                        loadPage(pagination.getCurrentPageIndex());
                    },
                    btnConfirm
            );
        }
    }

    @FXML
    private void onCancel() {
        BookingRow r = table.getSelectionModel().getSelectedItem();
        if (r == null) return;
        if (AlertUtil.confirm("Bạn có chắc chắn muốn huỷ đơn đặt vé #" + r.id() + " không?")) {
            fxAsync.runVoid(
                    () -> facade.cancel(r.id()),
                    () -> {
                        AlertUtil.info("Đã huỷ đơn hàng #" + r.id() + " thành công!");
                        loadPage(pagination.getCurrentPageIndex());
                    },
                    btnCancel
            );
        }
    }

    private void openDetail(Long id) {
        fxAsync.run(() -> facade.detail(id), this::showDetailDialog, table);
    }

    private void showDetailDialog(BookingDetail d) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết đơn đặt vé #" + d.id());
        dialog.setHeaderText("Thông tin chi tiết đơn hàng");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(16));

        grid.add(new Label("Mã đơn:"), 0, 0);
        grid.add(new Label("#" + d.id()), 1, 0);

        grid.add(new Label("Khách hàng:"), 0, 1);
        grid.add(new Label(d.customerName() + " (" + d.mobile() + ")"), 1, 1);

        grid.add(new Label("Phim:"), 0, 2);
        grid.add(new Label(d.movieTitle()), 1, 2);

        grid.add(new Label("Phòng chiếu:"), 0, 3);
        grid.add(new Label(d.roomName()), 1, 3);

        grid.add(new Label("Giờ chiếu:"), 0, 4);
        grid.add(new Label(d.startTime() != null ? d.startTime().format(DATE_TIME) : ""), 1, 4);

        grid.add(new Label("Danh sách ghế:"), 0, 5);
        grid.add(new Label(String.join(", ", d.seats())), 1, 5);

        grid.add(new Label("Tổng tiền:"), 0, 6);
        Label lblTotal = new Label(MoneyUtil.format(d.total()));
        lblTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #e50914;");
        grid.add(lblTotal, 1, 6);

        grid.add(new Label("Trạng thái:"), 0, 7);
        Label lblStatus = new Label(d.status().name());
        lblStatus.getStyleClass().add("st-" + d.status().name().toLowerCase());
        grid.add(lblStatus, 1, 7);

        grid.add(new Label("Thời gian đặt:"), 0, 8);
        grid.add(new Label(d.bookingDate() != null ? d.bookingDate().format(DATE_TIME) : ""), 1, 8);

        dialog.getDialogPane().setContent(new VBox(grid));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}
