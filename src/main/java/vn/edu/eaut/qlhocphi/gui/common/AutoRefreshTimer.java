package vn.edu.eaut.qlhocphi.gui.common;

import javax.swing.JComponent;
import javax.swing.Timer;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Tien ich DUNG CHUNG cho toan bo man hinh (Admin/Ke toan/Sinh vien): tu dong
 * goi lai 1 Runnable (thuong la ham taiDuLieu() cua man hinh) sau moi khoang
 * thoi gian co dinh, de man hinh luon hien du lieu moi nhat ma KHONG can nguoi
 * dung bam "Lam moi" tay - giai quyet dung loi "thanh toan roi nhung khong
 * cap nhat" vi truoc day moi man hinh chi tai du lieu dung 1 lan luc mo trang.
 *
 * CHI goi lai khi component dang THUC SU hien tren man hinh (isShowing()) -
 * tranh lang phi goi CSDL lien tuc khi nguoi dung dang o man hinh/tab khac.
 *
 * Cach dung trong bat ky JPanel nao (vi du trong constructor, SAU dong goi
 * taiDuLieu() lan dau):
 *   AutoRefreshTimer.gan(this, 15, this::taiDuLieu);
 *
 * 15 la so giay giua 2 lan tu lam moi - co the chinh tuy man hinh (man hinh
 * cang quan trong theo doi tien/no thi nen dat cang ngan, vi du 10-15s; man
 * hinh it thay doi (VD Bao cao thong ke) co the dat dai hon, vi du 30-60s de
 * do tai CSDL).
 */
public final class AutoRefreshTimer {
    private AutoRefreshTimer() {}

    // Luu Timer gan voi tung Component (WeakHashMap: tu dong don rac khi
    // component bi Swing giai phong, khong can nguoi dung phai nho goi huy()).
    private static final Map<JComponent, Timer> DANG_CHAY = new WeakHashMap<>();

    /** Gan 1 bo dem tu dong goi lai hanhDong moi soGiay giay, chi khi component dang hien. */
    public static void gan(JComponent component, int soGiay, Runnable hanhDong) {
        huy(component); // tranh gan trung neu constructor lo goi gan() nhieu lan
        Timer t = new Timer(soGiay * 1000, e -> {
            if (component.isShowing()) {
                hanhDong.run();
            }
        });
        t.setRepeats(true);
        t.start();
        DANG_CHAY.put(component, t);
    }

    /** Dung va go bo bo dem gan voi component - goi khi man hinh khong con dung nua (VD dang xuat). */
    public static void huy(JComponent component) {
        Timer t = DANG_CHAY.remove(component);
        if (t != null) t.stop();
    }
}