package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.navigation.SceneManager;
import com.fu.cinema.desktop.navigation.View;
import com.fu.cinema.desktop.session.DesktopSession;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class MainController {

    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss  dd/MM/yyyy");

    private final SceneManager sceneManager;
    private final DesktopSession session;

    @FXML private BorderPane root;
    @FXML private Label lblUser;
    @FXML private Label lblClock;

    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavCounterSale;
    @FXML private Button btnNavBooking;
    @FXML private Button btnNavGenre;
    @FXML private Button btnNavMovie;
    @FXML private Button btnNavSchedule;
    @FXML private Button btnNavReport;

    private Timeline clock;

    @FXML
    private void initialize() {
        lblUser.textProperty().bind(Bindings.concat("Nhân viên: ", session.displayNameProperty()));
        clock = new Timeline(new KeyFrame(Duration.seconds(1),
                e -> lblClock.setText(LocalDateTime.now().format(CLOCK))));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
        onDashboard();
    }

    private void setActiveNav(Button activeBtn, View view) {
        List<Button> navButtons = List.of(
                btnNavDashboard, btnNavCounterSale, btnNavBooking,
                btnNavGenre, btnNavMovie, btnNavSchedule, btnNavReport
        );
        for (Button btn : navButtons) {
            if (btn != null) {
                btn.getStyleClass().remove("active");
            }
        }
        if (activeBtn != null && !activeBtn.getStyleClass().contains("active")) {
            activeBtn.getStyleClass().add("active");
        }
        sceneManager.showIn(root, view);
    }

    @FXML private void onDashboard()   { setActiveNav(btnNavDashboard, View.DASHBOARD); }
    @FXML private void onCounterSale() { setActiveNav(btnNavCounterSale, View.COUNTER_SALE); }
    @FXML private void onBooking()     { setActiveNav(btnNavBooking, View.BOOKING); }
    @FXML private void onGenre()       { setActiveNav(btnNavGenre, View.GENRE); }
    @FXML private void onMovie()       { setActiveNav(btnNavMovie, View.MOVIE); }
    @FXML private void onSchedule()    { setActiveNav(btnNavSchedule, View.SCHEDULE); }
    @FXML private void onReport()      { setActiveNav(btnNavReport, View.REPORT); }

    @FXML
    private void onLogout() {
        clock.stop();
        session.logout();
        sceneManager.switchTo(View.LOGIN);
    }
}
