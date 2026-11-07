package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.facade.RoomFacade;
import com.fu.cinema.desktop.facade.ShowtimeFacade;
import com.fu.cinema.desktop.model.MovieItem;
import com.fu.cinema.desktop.model.RoomItem;
import com.fu.cinema.desktop.model.RoomRow;
import com.fu.cinema.desktop.model.ShowtimeRow;
import com.fu.cinema.desktop.util.AlertUtil;
import com.fu.cinema.desktop.util.FxAsync;
import com.fu.cinema.desktop.util.FxValidator;
import com.fu.cinema.desktop.util.MoneyUtil;
import com.fu.cinema.dto.RoomForm;
import com.fu.cinema.dto.ShowtimeForm;
import com.fu.cinema.enums.RoomStatus;
import com.fu.cinema.enums.RoomType;
import com.fu.cinema.enums.ShowtimeStatus;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ScheduleController {

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("HH:mm  dd/MM/yyyy");

    private final RoomFacade roomFacade;
    private final ShowtimeFacade showtimeFacade;
    private final FxAsync fxAsync;
    private final FxValidator validator;

    // === TAB PHÒNG CHIẾU ===
    @FXML private TableView<RoomRow> tableRooms;
    @FXML private TableColumn<RoomRow, String> colRoomName;
    @FXML private TableColumn<RoomRow, String> colRoomType;
    @FXML private TableColumn<RoomRow, Integer> colRoomRows;
    @FXML private TableColumn<RoomRow, Integer> colRoomSeatsPerRow;
    @FXML private TableColumn<RoomRow, Integer> colRoomCapacity;
    @FXML private TableColumn<RoomRow, RoomStatus> colRoomStatus;

    @FXML private VBox formRoomBox;
    @FXML private TextField tfRoomName;
    @FXML private ComboBox<RoomType> cbRoomType;
    @FXML private Spinner<Integer> spRoomRows;
    @FXML private Spinner<Integer> spRoomSeatsPerRow;
    @FXML private ComboBox<RoomStatus> cbRoomStatus;
    @FXML private Button btnSaveRoom;
    @FXML private Button btnNewRoom;
    @FXML private Button btnDeleteRoom;

    private Long editingRoomId = null;
    private final ObservableList<RoomRow> roomRows = FXCollections.observableArrayList();

    // === TAB SUẤT CHIẾU ===
    @FXML private ComboBox<MovieItem> cbFilterMovie;
    @FXML private ComboBox<RoomItem> cbFilterRoom;
    @FXML private DatePicker dpFilterDate;
    @FXML private TableView<ShowtimeRow> tableShowtimes;
    @FXML private TableColumn<ShowtimeRow, String> colStMovie;
    @FXML private TableColumn<ShowtimeRow, String> colStRoom;
    @FXML private TableColumn<ShowtimeRow, String> colStStart;
    @FXML private TableColumn<ShowtimeRow, String> colStEnd;
    @FXML private TableColumn<ShowtimeRow, String> colStPrice;
    @FXML private TableColumn<ShowtimeRow, ShowtimeStatus> colStStatus;
    @FXML private Pagination paginationShowtimes;

    @FXML private VBox formShowtimeBox;
    @FXML private ComboBox<MovieItem> cbStMovie;
    @FXML private ComboBox<RoomItem> cbStRoom;
    @FXML private DatePicker dpStDate;
    @FXML private Spinner<Integer> spStHour;
    @FXML private Spinner<Integer> spStMinute;
    @FXML private TextField tfStPrice;
    @FXML private Label lblEndSuggestion;
    @FXML private Button btnSaveShowtime;
    @FXML private Button btnNewShowtime;
    @FXML private Button btnDeleteShowtime;

    private Long editingShowtimeId = null;
    private final ObservableList<ShowtimeRow> showtimeRows = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        initRoomTab();
        initShowtimeTab();
    }

    // ==========================================
    // KHỞI TẠO TAB PHÒNG CHIẾU
    // ==========================================
    private void initRoomTab() {
        colRoomName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        colRoomType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().type().name()));
        colRoomRows.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().rows()));
        colRoomSeatsPerRow.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().seatsPerRow()));
        colRoomCapacity.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().capacity()));
        colRoomStatus.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().status()));

        tableRooms.setItems(roomRows);

        cbRoomType.getItems().setAll(RoomType.values());
        cbRoomStatus.getItems().setAll(RoomStatus.values());

        spRoomRows.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 26, 8));
        spRoomSeatsPerRow.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 12));

        tableRooms.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, row) -> {
            if (row != null) {
                fillRoomForm(row);
            }
        });

        btnDeleteRoom.disableProperty().bind(tableRooms.getSelectionModel().selectedItemProperty().isNull());
        btnSaveRoom.disableProperty().bind(Bindings.createBooleanBinding(
                () -> tfRoomName.getText() == null || tfRoomName.getText().trim().isEmpty(),
                tfRoomName.textProperty()
        ));

        reloadRooms();
    }

    private void reloadRooms() {
        fxAsync.run(roomFacade::findAll, roomRows::setAll);
    }

    private void fillRoomForm(RoomRow r) {
        editingRoomId = r.id();
        tfRoomName.setText(r.name());
        cbRoomType.setValue(r.type());
        spRoomRows.getValueFactory().setValue(r.rows());
        spRoomSeatsPerRow.getValueFactory().setValue(r.seatsPerRow());
        cbRoomStatus.setValue(r.status());
    }

    @FXML
    private void onNewRoom() {
        editingRoomId = null;
        tfRoomName.clear();
        cbRoomType.setValue(RoomType.TWO_D);
        spRoomRows.getValueFactory().setValue(8);
        spRoomSeatsPerRow.getValueFactory().setValue(12);
        cbRoomStatus.setValue(RoomStatus.ACTIVE);
        tableRooms.getSelectionModel().clearSelection();
    }

    @FXML
    private void onSaveRoom() {
        RoomForm form = new RoomForm();
        form.setRoomId(editingRoomId);
        form.setRoomName(tfRoomName.getText().trim());
        form.setRoomType(cbRoomType.getValue());
        form.setTotalRows(spRoomRows.getValue());
        form.setSeatsPerRow(spRoomSeatsPerRow.getValue());
        form.setStatus(cbRoomStatus.getValue());

        if (!validator.mark(validator.validate(form), Map.of(
                "roomName", tfRoomName,
                "roomType", cbRoomType,
                "totalRows", spRoomRows,
                "seatsPerRow", spRoomSeatsPerRow,
                "status", cbRoomStatus
        ))) {
            return;
        }

        fxAsync.runVoid(() -> roomFacade.save(form), () -> {
            AlertUtil.info("Đã lưu thông tin phòng chiếu thành công!");
            onNewRoom();
            reloadRooms();
            reloadShowtimeDropdowns();
        }, formRoomBox);
    }

    @FXML
    private void onDeleteRoom() {
        RoomRow row = tableRooms.getSelectionModel().getSelectedItem();
        if (row == null) return;

        if (AlertUtil.confirm("Bạn có chắc chắn muốn xoá phòng chiếu \"" + row.name() + "\"?")) {
            fxAsync.run(() -> roomFacade.delete(row.id()), deleted -> {
                if (deleted) {
                    AlertUtil.info("Đã xoá phòng chiếu thành công.");
                } else {
                    AlertUtil.info("Phòng chiếu đã có suất chiếu -> Đã chuyển sang MAINTENANCE.");
                }
                onNewRoom();
                reloadRooms();
                reloadShowtimeDropdowns();
            }, formRoomBox);
        }
    }

    // ==========================================
    // KHỞI TẠO TAB SUẤT CHIẾU
    // ==========================================
    private void initShowtimeTab() {
        colStMovie.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().movieTitle()));
        colStRoom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().roomName()));
        colStStart.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().start().format(DATETIME_FMT)));
        colStEnd.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().end().format(DATETIME_FMT)));
        colStPrice.setCellValueFactory(c -> new SimpleStringProperty(MoneyUtil.format(c.getValue().price())));
        colStStatus.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().status()));

        tableShowtimes.setItems(showtimeRows);

        spStHour.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 19));
        spStMinute.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 55, 0, 5));

        // Không cho chọn ngày trong quá khứ
        dpStDate.setDayCellFactory(p -> new DateCell() {
            @Override
            public void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setDisable(empty || d.isBefore(LocalDate.now()));
            }
        });
        dpStDate.setValue(LocalDate.now());

        tfStPrice.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d{0,9}") ? c : null));
        tfStPrice.setText("85000");

        // GỢI Ý ENDTIME BẰNG BINDING (Start + Duration + 15' dọn phòng) - TC-D12
        lblEndSuggestion.textProperty().bind(Bindings.createStringBinding(() -> {
            LocalDateTime end = calculateSuggestedEnd();
            return end == null ? "—" : "Kết thúc (gợi ý): " + end.format(DATETIME_FMT);
        }, cbStMovie.valueProperty(), dpStDate.valueProperty(), spStHour.valueProperty(), spStMinute.valueProperty()));

        // Filter listeners
        cbFilterMovie.valueProperty().addListener((o, a, b) -> searchShowtimes());
        cbFilterRoom.valueProperty().addListener((o, a, b) -> searchShowtimes());
        dpFilterDate.valueProperty().addListener((o, a, b) -> searchShowtimes());
        paginationShowtimes.currentPageIndexProperty().addListener((o, a, b) -> loadShowtimes(b.intValue()));

        tableShowtimes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, row) -> {
            if (row != null) {
                fillShowtimeForm(row);
            }
        });

        btnDeleteShowtime.disableProperty().bind(tableShowtimes.getSelectionModel().selectedItemProperty().isNull());

        reloadShowtimeDropdowns();
        loadShowtimes(0);
    }

    private LocalDateTime getSelectedStartTime() {
        LocalDate date = dpStDate.getValue();
        if (date == null) return null;
        return LocalDateTime.of(date, LocalTime.of(spStHour.getValue(), spStMinute.getValue()));
    }

    private LocalDateTime calculateSuggestedEnd() {
        LocalDateTime start = getSelectedStartTime();
        MovieItem movie = cbStMovie.getValue();
        if (start == null || movie == null) return null;
        return start.plusMinutes(movie.duration() + 15L); // Start + Duration + 15'
    }

    private void reloadShowtimeDropdowns() {
        fxAsync.run(showtimeFacade::schedulableMovies, movies -> {
            cbStMovie.getItems().setAll(movies);
            cbFilterMovie.getItems().setAll(movies);
        });

        fxAsync.run(showtimeFacade::activeRooms, rooms -> {
            cbStRoom.getItems().setAll(rooms);
            cbFilterRoom.getItems().setAll(rooms);
        });
    }

    private void searchShowtimes() {
        if (paginationShowtimes.getCurrentPageIndex() != 0) {
            paginationShowtimes.setCurrentPageIndex(0);
        } else {
            loadShowtimes(0);
        }
    }

    private void loadShowtimes(int page) {
        Long movieId = cbFilterMovie.getValue() == null ? null : cbFilterMovie.getValue().id();
        Long roomId = cbFilterRoom.getValue() == null ? null : cbFilterRoom.getValue().id();
        LocalDate date = dpFilterDate.getValue();

        fxAsync.run(() -> showtimeFacade.search(movieId, roomId, date, page), result -> {
            showtimeRows.setAll(result.items());
            paginationShowtimes.setPageCount(result.totalPages());
        });
    }

    private void fillShowtimeForm(ShowtimeRow r) {
        editingShowtimeId = r.id();
        dpStDate.setValue(r.start().toLocalDate());
        spStHour.getValueFactory().setValue(r.start().getHour());
        spStMinute.getValueFactory().setValue(r.start().getMinute());
        tfStPrice.setText(r.price() != null ? r.price().toPlainString() : "");

        for (MovieItem m : cbStMovie.getItems()) {
            if (m.title().equals(r.movieTitle())) {
                cbStMovie.setValue(m);
                break;
            }
        }

        for (RoomItem room : cbStRoom.getItems()) {
            if (room.name().equals(r.roomName())) {
                cbStRoom.setValue(room);
                break;
            }
        }
    }

    @FXML
    private void onNewShowtime() {
        editingShowtimeId = null;
        cbStMovie.setValue(null);
        cbStRoom.setValue(null);
        dpStDate.setValue(LocalDate.now());
        spStHour.getValueFactory().setValue(19);
        spStMinute.getValueFactory().setValue(0);
        tfStPrice.setText("85000");
        tableShowtimes.getSelectionModel().clearSelection();
    }

    @FXML
    private void onSaveShowtime() {
        ShowtimeForm form = new ShowtimeForm();
        form.setShowtimeId(editingShowtimeId);
        form.setMovieId(cbStMovie.getValue() == null ? null : cbStMovie.getValue().id());
        form.setRoomId(cbStRoom.getValue() == null ? null : cbStRoom.getValue().id());
        form.setStartTime(getSelectedStartTime());
        form.setEndTime(calculateSuggestedEnd());
        form.setTicketPrice(tfStPrice.getText().isBlank() ? null : new BigDecimal(tfStPrice.getText().trim()));

        if (!validator.mark(validator.validate(form), Map.of(
                "movieId", cbStMovie,
                "roomId", cbStRoom,
                "startTime", dpStDate,
                "ticketPrice", tfStPrice
        ))) {
            return;
        }

        fxAsync.runVoid(() -> showtimeFacade.save(form), () -> {
            AlertUtil.info("Đã lưu suất chiếu thành công!");
            onNewShowtime();
            loadShowtimes(paginationShowtimes.getCurrentPageIndex());
        }, formShowtimeBox);
    }

    @FXML
    private void onDeleteShowtime() {
        ShowtimeRow r = tableShowtimes.getSelectionModel().getSelectedItem();
        if (r == null) return;

        if (AlertUtil.confirm("Bạn có chắc chắn muốn xoá suất chiếu #" + r.id() + " (" + r.movieTitle() + ")?")) {
            fxAsync.run(() -> showtimeFacade.delete(r.id()), deleted -> {
                if (deleted) {
                    AlertUtil.info("Đã xoá suất chiếu thành công.");
                } else {
                    AlertUtil.info("Suất chiếu đã có đơn đặt vé -> Đã huỷ suất chiếu và các đơn đặt vé liên quan (BR-09).");
                }
                onNewShowtime();
                loadShowtimes(paginationShowtimes.getCurrentPageIndex());
            }, formShowtimeBox);
        }
    }
}
