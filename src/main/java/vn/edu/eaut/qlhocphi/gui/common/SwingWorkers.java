package vn.edu.eaut.qlhocphi.gui.common;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Tiện ích SwingWorker dùng chung toàn hệ thống.
 * <p>
 * Mục đích (theo yêu cầu đồ án):
 * - Mọi thao tác CSDL / mạng / xử lý nặng KHÔNG chạy trên EDT
 *   (Event Dispatch Thread) để tránh treo giao diện.
 * - Cập nhật UI chỉ thực hiện trong callback trên EDT.
 * <p>
 * Cách dùng:
 * <pre>
 *   SwingWorkers.chay(
 *       () -> service.layDuLieu(),           // nền
 *       data -> capNhatBang(data),          // xong – EDT
 *       err  -> UIUtils.thongBaoLoi(this, err.getMessage()),
 *       parent
 *   );
 * </pre>
 */
public final class SwingWorkers {

    private SwingWorkers() {}

    /**
     * Chạy tác vụ nền, nhận kết quả trên EDT khi xong.
     *
     * @param tacVuNen   logic nặng (DB, API…) – chạy nền
     * @param khiXong    nhận kết quả – chạy trên EDT
     * @param khiLoi     xử lý lỗi – chạy trên EDT (có thể null)
     * @param parent     component cha (để disable tạm, có thể null)
     */
    public static <T> void chay(Callable<T> tacVuNen,
                                Consumer<T> khiXong,
                                Consumer<Exception> khiLoi,
                                Component parent) {
        if (parent != null) parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<T, Void> worker = new SwingWorker<>() {
            @Override
            protected T doInBackground() throws Exception {
                return tacVuNen.call();
            }

            @Override
            protected void done() {
                if (parent != null) parent.setCursor(Cursor.getDefaultCursor());
                try {
                    T ketQua = get();
                    if (khiXong != null) khiXong.accept(ketQua);
                } catch (Exception ex) {
                    Exception root = ex;
                    if (ex.getCause() instanceof Exception c) root = c;
                    if (khiLoi != null) khiLoi.accept(root);
                    else if (parent != null) {
                        JOptionPane.showMessageDialog(parent,
                                root.getMessage() != null ? root.getMessage() : "Có lỗi xảy ra",
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
        worker.execute();
    }

    /**
     * Chạy tác vụ nền không trả về dữ liệu (INSERT/UPDATE/DELETE).
     */
    public static void chayVoid(Runnable tacVuNen,
                                Runnable khiXong,
                                Consumer<Exception> khiLoi,
                                Component parent) {
        chay(() -> {
            tacVuNen.run();
            return null;
        }, ignored -> {
            if (khiXong != null) khiXong.run();
        }, khiLoi, parent);
    }
}