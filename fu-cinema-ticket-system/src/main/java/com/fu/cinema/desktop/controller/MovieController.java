package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.facade.MovieFacade;
import com.fu.cinema.desktop.model.GenreItem;
import com.fu.cinema.desktop.model.MovieRow;
import com.fu.cinema.desktop.navigation.SceneManager;
import com.fu.cinema.desktop.navigation.View;
import com.fu.cinema.desktop.util.AlertUtil;
import com.fu.cinema.desktop.util.FxAsync;
import com.fu.cinema.enums.MovieStatus;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class MovieController {

    private static final PseudoClass STOPPED = PseudoClass.getPseudoClass("stopped");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MovieFacade facade;
    private final FxAsync fxAsync;
    private final SceneManager sceneManager;

    @FXML private TextField tfSearch;
    @FXML private ComboBox<GenreItem> cbGenre;
    @FXML private ComboBox<MovieStatus> cbStatus;
    @FXML private ComboBox<String> cbSort;
    @FXML private TableView<MovieRow> table;
    @FXML private TableColumn<MovieRow, String> colTitle;
    @FXML private TableColumn<MovieRow, String> colGenre;
    @FXML private TableColumn<MovieRow, Integer> colDuration;
    @FXML private TableColumn<MovieRow, LocalDate> colRelease;
    @FXML private TableColumn<MovieRow, String> colAge;
    @FXML private TableColumn<MovieRow, String> colStatus;
    @FXML private Pagination pagination;
    @FXML private Label lblCount;
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private final ObservableList<MovieRow> rows = FXCollections.observableArrayList();
    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));

    @FXML
    private void initialize() {
        // Cấu hình TableColumn bằng lambda (TECH-17)
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().title()));
        colGenre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().genreName()));

        colDuration.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().duration()));
        colDuration.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v + " phút");
            }
        });

        colRelease.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().releaseDate()));
        colRelease.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.format(DATE_FMT));
            }
        });

        colAge.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().ageRating() != null ? c.getValue().ageRating().name() : ""));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().status() != null ? c.getValue().status().name() : ""));

        // Gán PseudoClass :stopped để tô xám dòng phim ngừng chiếu & double click để sửa
        table.setRowFactory(tv -> {
            TableRow<MovieRow> row = new TableRow<>() {
                @Override
                protected void updateItem(MovieRow m, boolean empty) {
                    super.updateItem(m, empty);
                    pseudoClassStateChanged(STOPPED, !empty && m != null && m.status() == MovieStatus.STOPPED);
                }
            };
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openDialog(row.getItem().id());
                }
            });
            return row;
        });
        table.setItems(rows);

        btnEdit.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        btnDelete.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        // Khởi tạo ComboBox trạng thái (có "Tất cả")
        List<MovieStatus> statuses = new ArrayList<>();
        statuses.add(null);
        statuses.addAll(Arrays.asList(MovieStatus.values()));
        cbStatus.getItems().setAll(statuses);
        cbStatus.setButtonCell(new StatusCell());
        cbStatus.setCellFactory(lv -> new StatusCell());

        // Khởi tạo ComboBox sắp xếp
        cbSort.getItems().setAll("releaseDate", "title");
        cbSort.setButtonCell(new SortCell());
        cbSort.setCellFactory(lv -> new SortCell());
        cbSort.setValue("releaseDate");

        // Lắng nghe sự kiện tìm kiếm & lọc
        debounce.setOnFinished(e -> search());
        tfSearch.textProperty().addListener((o, a, b) -> debounce.playFromStart());
        cbGenre.valueProperty().addListener((o, a, b) -> search());
        cbStatus.valueProperty().addListener((o, a, b) -> search());
        cbSort.valueProperty().addListener((o, a, b) -> search());

        pagination.currentPageIndexProperty().addListener((o, a, b) -> load(b.intValue()));

        // Tải danh sách thể loại nạp vào ComboBox lọc
        fxAsync.run(facade::genres, g -> {
            List<GenreItem> genreList = new ArrayList<>();
            genreList.add(null); // null = "Tất cả thể loại"
            genreList.addAll(g);
            cbGenre.getItems().setAll(genreList);
            cbGenre.setButtonCell(new GenreCell());
            cbGenre.setCellFactory(lv -> new GenreCell());
            load(0);
        });
    }

    /** Đổi bộ lọc -> tự động quay về trang 1 (TC-D08) */
    private void search() {
        if (pagination.getCurrentPageIndex() != 0) {
            pagination.setCurrentPageIndex(0); // listener sẽ kích hoạt load(0)
        } else {
            load(0);
        }
    }

    private void load(int page) {
        String kw = tfSearch.getText() != null ? tfSearch.getText().trim() : "";
        Long genreId = cbGenre.getValue() == null ? null : cbGenre.getValue().id();
        fxAsync.run(() -> facade.search(kw, genreId, cbStatus.getValue(), cbSort.getValue(), page), result -> {
            rows.setAll(result.items());
            pagination.setPageCount(Math.max(1, result.totalPages()));
            lblCount.setText(result.totalElements() + " phim");
        });
    }

    @FXML
    private void onAdd() {
        openDialog(null);
    }

    @FXML
    private void onEdit() {
        MovieRow sel = table.getSelectionModel().getSelectedItem();
        if (sel != null) {
            openDialog(sel.id());
        }
    }

    private void openDialog(Long movieId) {
        Window owner = table.getScene().getWindow();
        fxAsync.run(() -> facade.getDialogData(movieId), data -> {
            SceneManager.Loaded<MovieDialogController> dlg = sceneManager.load(View.MOVIE_DIALOG);
            dlg.controller().open(owner, data.form(), data.genres()).ifPresent(form ->
                    fxAsync.runVoid(
                            () -> facade.save(form),
                            () -> {
                                AlertUtil.info("Đã lưu phim");
                                load(pagination.getCurrentPageIndex());
                            },
                            null
                    )
            );
        });
    }

    @FXML
    private void onDelete() {
        MovieRow m = table.getSelectionModel().getSelectedItem();
        if (m == null || !AlertUtil.confirm("Xoá phim \"" + m.title() + "\"?")) return;
        fxAsync.run(() -> facade.delete(m.id()), deleted -> {
            AlertUtil.info(deleted ? "Đã xoá phim" : "Phim đã có suất chiếu → đã chuyển sang STOPPED"); // BR-06
            load(pagination.getCurrentPageIndex());
        });
    }

    // Các Cell hiển thị văn bản đẹp mắt cho ComboBox có mục "Tất cả"
    private static class StatusCell extends ListCell<MovieStatus> {
        @Override
        protected void updateItem(MovieStatus item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : (item == null ? "Tất cả trạng thái" : item.name()));
        }
    }

    private static class GenreCell extends ListCell<GenreItem> {
        @Override
        protected void updateItem(GenreItem item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : (item == null ? "Tất cả thể loại" : item.name()));
        }
    }

    private static class SortCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else if ("releaseDate".equals(item)) {
                setText("Ngày khởi chiếu (Mới nhất)");
            } else {
                setText("Tên phim (A - Z)");
            }
        }
    }
}