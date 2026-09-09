package vn.edu.eaut.qlhocphi.bus;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Chay nen trong suot vong doi ung dung, dinh ky quet toan bo hoa don qua han de
 * tu dong gui nhac no (SV qua email -> Phu huynh qua SMS khi qua han lau). Chay
 * moi 24h/lan. Khoi dong 1 lan duy nhat trong App.main(), tuong tu cach lam voi
 * TuDongThuHocPhiScheduler.
 */
public class NhacNoTuDongScheduler {
    private static final NhacNoTuDongScheduler INSTANCE = new NhacNoTuDongScheduler();

    public static NhacNoTuDongScheduler getInstance() {
        return INSTANCE;
    }

    private final NhacNoTuDongService nhacNoTuDongService = new NhacNoTuDongService();
    private ScheduledExecutorService executor;

    private NhacNoTuDongScheduler() {}

    public synchronized void start() {
        if (executor != null && !executor.isShutdown()) return; // da chay roi
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "nhac-no-tu-dong");
            t.setDaemon(true); // khong giu JVM song khi dong toan bo cua so
            return t;
        });
        executor.scheduleAtFixedRate(this::quetAnToan, 0, 1, TimeUnit.MINUTES);
    }

    public synchronized void stop() {
        if (executor != null) executor.shutdownNow();
    }

    private void quetAnToan() {
        try {
            nhacNoTuDongService.quetVaGuiNhacNo();
        } catch (Exception ex) {
            // Khong duoc de loi lam chet luong nen - chi log ra console va thu lai vao chu ky sau
            System.err.println("Loi khi quet nhac no tu dong: " + ex.getMessage());
        }
    }
}