package com.fu.cinema.desktop.util;

import javafx.concurrent.Task;
import javafx.scene.Node;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Component
public class FxAsync implements DisposableBean {

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public <T> Task<T> run(Callable<T> work, Consumer<T> onSuccess) {
        return run(work, onSuccess, AlertUtil::showError, null);
    }

    public <T> Task<T> run(Callable<T> work, Consumer<T> onSuccess, Node busy) {
        return run(work, onSuccess, AlertUtil::showError, busy);
    }

    /**
     * @param busy node bị khoá trong lúc chạy (nút, form…), có thể null
     */
    public <T> Task<T> run(Callable<T> work, Consumer<T> onSuccess, Consumer<Throwable> onError, Node busy) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return work.call(); // background — KHÔNG đụng UI ở đây
            }
        };
        if (busy != null) {
            busy.setDisable(true);
        }
        task.setOnSucceeded(e -> { // FX thread
            if (busy != null) busy.setDisable(false);
            onSuccess.accept(task.getValue());
        });
        task.setOnFailed(e -> { // FX thread
            if (busy != null) busy.setDisable(false);
            onError.accept(task.getException());
        });
        executor.submit(task);
        return task;
    }

    /** Cho thao tác không trả kết quả (xoá, xác nhận…). */
    public Task<Void> runVoid(Runnable work, Runnable onSuccess, Node busy) {
        return run(() -> { work.run(); return null; }, v -> onSuccess.run(), AlertUtil::showError, busy);
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
    }
}
