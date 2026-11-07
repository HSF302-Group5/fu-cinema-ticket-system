package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.model.GenreItem;
import com.fu.cinema.desktop.util.FxValidator;
import com.fu.cinema.dto.MovieForm;
import com.fu.cinema.enums.AgeRating;
import com.fu.cinema.enums.MovieStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class MovieDialogController {

    private final FxValidator validator;

    @FXML private GridPane root;
    @FXML private TextField tfTitle, tfDirector, tfPoster;
    @FXML private Spinner<Integer> spDuration;
    @FXML private DatePicker dpRelease;
    @FXML private ComboBox<AgeRating> cbAgeRating;
    @FXML private ComboBox<GenreItem> cbGenre;
    @FXML private ComboBox<MovieStatus> cbStatus;
    @FXML private TextArea taDescription;

    private Long movieId;

    @FXML
    private void initialize() {
        spDuration.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 120));
        spDuration.setEditable(true);
        // Spinner editable không tự commit giá trị gõ tay khi mất focus
        spDuration.focusedProperty().addListener((o, was, is) -> {
            if (!is) {
                commitSpinner();
            }
        });
        cbAgeRating.getItems().setAll(AgeRating.values());
        cbStatus.getItems().setAll(MovieStatus.values());
    }

    public Optional<MovieForm> open(Window owner, MovieForm form, List<GenreItem> genres) {
        cbGenre.getItems().setAll(genres);
        fill(form);

        Dialog<MovieForm> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle(form.getMovieId() == null ? "Thêm phim" : "Sửa phim");
        dialog.getDialogPane().setContent(root);
        dialog.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/desktop/css/desktop.css")).toExternalForm()
        );

        ButtonType save = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        // Chặn đóng dialog khi dữ liệu không hợp lệ (event filter — Javafx_exercises 1.5)
        Node saveButton = dialog.getDialogPane().lookupButton(save);
        if (saveButton != null) {
            saveButton.addEventFilter(ActionEvent.ACTION, e -> {
                if (!validate()) {
                    e.consume();
                }
            });
        }
        dialog.setResultConverter(bt -> bt == save ? toForm() : null);
        return dialog.showAndWait();
    }

    private boolean validate() {
        commitSpinner();
        return validator.mark(validator.validate(toForm()), Map.of(
                "title", tfTitle,
                "director", tfDirector,
                "duration", spDuration,
                "releaseDate", dpRelease,
                "ageRating", cbAgeRating,
                "genreId", cbGenre,
                "status", cbStatus,
                "posterUrl", tfPoster,
                "description", taDescription
        ));
    }

    private MovieForm toForm() {
        MovieForm f = new MovieForm();
        f.setMovieId(movieId);
        f.setTitle(tfTitle.getText() == null ? "" : tfTitle.getText().trim());
        f.setDirector(tfDirector.getText() == null ? "" : tfDirector.getText().trim());
        f.setDuration(spDuration.getValue());
        f.setReleaseDate(dpRelease.getValue());
        f.setAgeRating(cbAgeRating.getValue());
        f.setGenreId(cbGenre.getValue() == null ? null : cbGenre.getValue().id());
        f.setStatus(cbStatus.getValue());
        f.setPosterUrl(tfPoster.getText() == null ? "" : tfPoster.getText().trim());
        f.setDescription(taDescription.getText() == null ? "" : taDescription.getText().trim());
        return f;
    }

    private void fill(MovieForm f) {
        this.movieId = f.getMovieId();
        tfTitle.setText(f.getTitle() != null ? f.getTitle() : "");
        tfDirector.setText(f.getDirector() != null ? f.getDirector() : "");
        spDuration.getValueFactory().setValue(f.getDuration() != null ? f.getDuration() : 120);
        dpRelease.setValue(f.getReleaseDate());
        cbAgeRating.setValue(f.getAgeRating());
        if (f.getGenreId() != null) {
            cbGenre.getItems().stream()
                    .filter(g -> g.id().equals(f.getGenreId()))
                    .findFirst()
                    .ifPresent(cbGenre::setValue);
        } else {
            cbGenre.setValue(null);
        }
        cbStatus.setValue(f.getStatus() != null ? f.getStatus() : MovieStatus.COMING_SOON);
        tfPoster.setText(f.getPosterUrl() != null ? f.getPosterUrl() : "");
        taDescription.setText(f.getDescription() != null ? f.getDescription() : "");

        // Xoá trạng thái lỗi cũ
        validator.mark(Map.of(), Map.of(
                "title", tfTitle,
                "director", tfDirector,
                "duration", spDuration,
                "releaseDate", dpRelease,
                "ageRating", cbAgeRating,
                "genreId", cbGenre,
                "status", cbStatus,
                "posterUrl", tfPoster,
                "description", taDescription
        ));
    }

    private void commitSpinner() {
        try {
            spDuration.getValueFactory().setValue(Integer.parseInt(spDuration.getEditor().getText().trim()));
        } catch (NumberFormatException ignored) {
            spDuration.getEditor().setText(String.valueOf(spDuration.getValue()));
        }
    }
}
