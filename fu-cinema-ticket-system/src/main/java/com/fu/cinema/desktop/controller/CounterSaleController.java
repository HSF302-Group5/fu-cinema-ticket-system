package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.component.SeatMapView;
import com.fu.cinema.desktop.facade.CounterSaleFacade;
import com.fu.cinema.desktop.model.CustomerItem;
import com.fu.cinema.desktop.model.MovieItem;
import com.fu.cinema.desktop.model.SeatMapData;
import com.fu.cinema.desktop.model.ShowtimeItem;
import com.fu.cinema.desktop.util.AlertUtil;
import com.fu.cinema.desktop.util.FxAsync;
import com.fu.cinema.desktop.util.MoneyUtil;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class CounterSaleController {

    private final CounterSaleFacade facade;
    private final FxAsync fxAsync;

    @FXML private TextField tfCustomer;
    @FXML private ListView<CustomerItem> lvCustomers;
    @FXML private ComboBox<MovieItem> cbMovie;
    @FXML private ComboBox<ShowtimeItem> cbShowtime;
    @FXML private SeatMapView seatMap;
    @FXML private Label lblSeats;
    @FXML private Label lblTotal;
    @FXML private CheckBox chkPaid;
    @FXML private Button btnSell;

    private final ObjectProperty<BigDecimal> price = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final BooleanProperty busy = new SimpleBooleanProperty(false);
    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));

    @FXML
    private void initialize() {
        // (1) Tìm kiếm khách hàng thành viên có debounce 300ms
        debounce.setOnFinished(e -> fxAsync.run(
                () -> facade.searchCustomers(tfCustomer.getText()),
                lvCustomers.getItems()::setAll
        ));
        tfCustomer.textProperty().addListener((o, a, b) -> debounce.playFromStart());

        // (2) Chuỗi phụ thuộc: Chọn Phim -> Xoá suất chiếu & sơ đồ ghế cũ -> Nạp suất chiếu mới
        cbMovie.valueProperty().addListener((o, a, movie) -> {
            cbShowtime.getItems().clear();
            clearSeatMap();
            if (movie != null) {
                fxAsync.run(() -> facade.showtimes(movie.id()), cbShowtime.getItems()::setAll);
            }
        });

        // Chọn Suất chiếu -> Xoá ghế cũ -> Tải sơ đồ ghế mới
        cbShowtime.valueProperty().addListener((o, a, st) -> {
            clearSeatMap();
            if (st != null) {
                price.set(st.price());
                reloadSeatMap();
            }
        });

        // (3) Tính tiền thời gian thực: Tổng tiền = Giá vé * Số ghế đang chọn (Binding)
        ObjectBinding<BigDecimal> total = Bindings.createObjectBinding(
                () -> price.get() == null ? BigDecimal.ZERO
                        : price.get().multiply(BigDecimal.valueOf(seatMap.getSelectedSeats().size())),
                price, seatMap.getSelectedSeats()
        );
        lblTotal.textProperty().bind(Bindings.createStringBinding(
                () -> "Tổng tiền: " + MoneyUtil.format(total.get()), total));
        lblSeats.textProperty().bind(Bindings.createStringBinding(
                () -> seatMap.getSelectedSeats().isEmpty() ? "Ghế đang chọn: (Chưa chọn)"
                        : "Ghế đang chọn: " + String.join(", ", seatMap.getSelectedSeats().stream().sorted(CounterSaleFacade.SEAT_ORDER).toList()),
                seatMap.getSelectedSeats()));

        // (4) Điều kiện kích hoạt nút Bán vé: Đủ Khách + Suất chiếu + ít nhất 1 ghế và không bận (busy)
        btnSell.disableProperty().bind(
                lvCustomers.getSelectionModel().selectedItemProperty().isNull()
                        .or(cbShowtime.valueProperty().isNull())
                        .or(Bindings.isEmpty(seatMap.getSelectedSeats()))
                        .or(busy)
        );

        // (5) Tải danh sách phim và khách ban đầu
        fxAsync.run(facade::movies, cbMovie.getItems()::setAll);
        fxAsync.run(() -> facade.searchCustomers(""), lvCustomers.getItems()::setAll);
    }

    @FXML
    private void onSell() {
        CustomerItem customer = lvCustomers.getSelectionModel().getSelectedItem();
        ShowtimeItem showtime = cbShowtime.getValue();
        List<String> seats = List.copyOf(seatMap.getSelectedSeats());
        boolean paid = chkPaid.isSelected();

        if (!AlertUtil.confirm("Xác nhận bán %d vé (%s) cho khách hàng \"%s\"?".formatted(
                seats.size(), String.join(", ", seats), customer.fullName()))) {
            return;
        }

        busy.set(true);
        fxAsync.run(
                () -> facade.sell(customer.id(), showtime.id(), seats, paid),
                result -> {
                    busy.set(false);
                    AlertUtil.info("Bán vé thành công!\nMã đơn: #%d\nGhế: %s\nTổng tiền: %s\nTrạng thái: %s"
                            .formatted(result.bookingId(), String.join(", ", result.seats()),
                                    MoneyUtil.format(result.total()), result.status()));
                    reloadSeatMap();
                },
                ex -> {
                    busy.set(false);
                    AlertUtil.showError(ex); // Bắt lỗi vi phạm tuổi BR-11 (TC-D15) hoặc tranh chấp ghế TC-D16
                    reloadSeatMap();         // Tự động tải lại sơ đồ ghế để cập nhật ghế vừa bị người khác đặt thành đã bán
                },
                null
        );
    }

    private void reloadSeatMap() {
        ShowtimeItem st = cbShowtime.getValue();
        if (st == null) return;
        fxAsync.run(() -> facade.seatMap(st.id()), data -> seatMap.setSeatMap(data));
    }

    private void clearSeatMap() {
        price.set(BigDecimal.ZERO);
        seatMap.clearSelection();
        seatMap.setSeatMap((SeatMapData) null);
    }
}