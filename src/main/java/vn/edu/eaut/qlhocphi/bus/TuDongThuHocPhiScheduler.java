package vn.edu.eaut.qlhocphi.bus;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Chay nen trong suot vong doi ung dung (khong phu thuoc ai dang dang nhap),
 * dinh ky quet cac "uy quyen trich no tu dong" (LichThuTuDong) dang cho.
 * Khoi dong 1 lan duy nhat trong App.main(), sau khi da kiem tra ket noi CSDL.
 */
public class TuDongThuHocPhiScheduler {
    private static final TuDongThuHocPhiScheduler INSTANCE = new TuDongThuHocPhiScheduler();

    /** Chu ky quet - 15 phut. Co the giam xuong de demo (VD: 1 phut) trong luc bao ve do an. */
    private static final long CHU_KY_PHUT = 15;

    private final ThuTuDongService thuTuDongService = new ThuTuDongService();
    private ScheduledExecutorService executor;

    private TuDongThuHocPhiScheduler() {}

    public static TuDongThuHocPhiScheduler getInstance() {
        return INSTANCE;
    }

    public synchronized void start() {
        if (executor != null && !executor.isShutdown()) return; // da chay roi
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "tu-dong-thu-hoc-phi");
            t.setDaemon(true); // khong giu JVM song khi dong toan bo cua so
            return t;
        });
        executor.scheduleAtFixedRate(this::quetAnToan, 0, CHU_KY_PHUT, TimeUnit.MINUTES);
    }

    public synchronized void stop() {
        if (executor != null) executor.shutdownNow();
    }

    private void quetAnToan() {
        try {
            thuTuDongService.quetMotLan();
        } catch (Exception ex) {
            // Khong duoc de loi lam chet luong nen - chi log ra console va thu lai vao chu ky sau
            System.err.println("Loi khi quet thu hoc phi tu dong: " + ex.getMessage());
        }
    }
}