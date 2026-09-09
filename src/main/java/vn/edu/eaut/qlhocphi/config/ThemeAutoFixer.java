package vn.edu.eaut.qlhocphi.config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;

/**
 * "Va" tu dong cac mau NEN/CHU bi HARDCODE CUNG (vd Color.WHITE, cac ma hex
 * sang mau) rai rac trong hang chuc file dialog/panel cu cua du an - MA KHONG
 * CAN SUA TUNG FILE THU CONG.
 *
 * CO CHE: dang ky 1 AWTEventListener toan cuc (o cap Toolkit, khong gan vao
 * bat ky cua so cu the nao), lang nghe su kien WINDOW_OPENED cua BAT KY
 * JFrame/JDialog nao trong toan bo ung dung. Ngay khi 1 cua so vua hien ra
 * (MainFrame moi sau khi doi theme, hoac bat ky Dialog nao nguoi dung mo -
 * vd SinhVienFormDialog, TaiKhoanFormDialog, PaymentMethodDialog...), ham nay
 * se DUYET DE QUY toan bo cay component ben trong cua so do, va DOI nhung mau
 * CU KHOP CHINH XAC voi 1 trong cac "mau hardcode da biet" (xem BANG_MAU_NEN /
 * BANG_MAU_CHU ben duoi) sang mau THEO THEME HIEN TAI tuong ung (BG_CARD,
 * BG_MAIN, TINT_VIOLET, TEXT_PRIMARY...) - doc SONG tu UITheme moi lan chay,
 * nen luon dung voi che do Sang/Toi dang chon tai thoi diem cua so do mo ra.
 *
 * VI SAO AN TOAN: cac mau nhu Color.WHITE hay cac ma hex sang duoc dung XUYEN
 * SUOT du an CHI VOI 1 Y NGHIA DUY NHAT la "nen the/dialog mau trang" hoac
 * "nen phu mau xam nhat" - khong co truong hop nao trong code hien tai "co y"
 * giu nguyen mau trang vinh vien bat ke theme dang chon. Vi vay doi hang loat
 * theo dung ban do anh xa la an toan va dung y do thiet ke ban dau.
 *
 * CACH KICH HOAT (CHI 1 DONG DUY NHAT, dat trong UITheme.apply()):
 * KHONG CAN SUA BAT KY FILE DIALOG/PANEL NAO KHAC.
 *
 *   public static void apply() {
 *       ...
 *       ThemeAutoFixer.kichHoat();   // <-- them dung 1 dong nay
 *       apDungTheoCheDo();
 *   }
 *
 * Neu muon mo rong ban do anh xa (vd phat hien them 1 mau hardcode moi o file
 * nao do sau nay), chi can them 1 dong "case 0x......" vao anhXaMauNen() hoac
 * anhXaMauChu() ben duoi - van khong can dong vao file dialog/panel goc.
 */
public final class ThemeAutoFixer {
    private ThemeAutoFixer() {}

    private static boolean daKichHoat = false;

    /** Goi 1 lan duy nhat luc khoi dong app. Goi lai nhieu lan (vo tinh) cung khong sao, tu bo qua. */
    public static void kichHoat() {
        if (daKichHoat) return;
        daKichHoat = true;

        AWTEventListener listener = event -> {
            if (event.getID() == WindowEvent.WINDOW_OPENED && event instanceof WindowEvent we) {
                Window cuaSo = we.getWindow();
                // Doi mau ngay sau khi cua so da layout xong, tranh xung dot voi
                // qua trinh khoi tao component cua chinh no dang chay do.
                SwingUtilities.invokeLater(() -> vaMauDeQuy(cuaSo));
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.WINDOW_EVENT_MASK);
    }

    /** Duyet de quy 1 cay component, va lai mau nen/chu neu khop voi ban do anh xa mau cu -> moi. */
    private static void vaMauDeQuy(Component c) {
        Color nenMoi = anhXaMauNen(c.getBackground());
        if (nenMoi != null) c.setBackground(nenMoi);

        Color chuMoi = anhXaMauChu(c.getForeground());
        if (chuMoi != null) c.setForeground(chuMoi);

        if (c instanceof Container container) {
            for (Component con : container.getComponents()) {
                vaMauDeQuy(con);
            }
        }
        c.repaint();
    }

    /**
     * BANG ANH XA MAU NEN da hardcode -> field UITheme tuong ung (doc SONG,
     * luon dung theo theme dang chon tai thoi diem goi). Them "case" moi neu
     * phat hien them 1 mau hardcode khac chua duoc liet ke o day.
     */
    private static Color anhXaMauNen(Color mauCu) {
        if (mauCu == null) return null;
        int rgb = mauCu.getRGB() & 0xFFFFFF;
        return switch (rgb) {
            case 0xFFFFFF -> UITheme.BG_CARD;      // Color.WHITE - nen the/dialog (da tim thay ~35 vi tri)
            case 0xF0F3F9 -> UITheme.BG_MAIN;      // header bang kieu cu
            case 0xF3F4FB -> UITheme.BG_MAIN;      // textfield / nen phu mau xam nhat
            case 0xE6F0FE -> UITheme.TINT_VIOLET;  // mau chon dong (selection) kieu cu
            case 0xF5F5F5 -> UITheme.BG_MAIN;      // khung chi tiet loi (ErrorScreens)
            case 0xF7F9FC -> UITheme.BG_MAIN;      // mau hover dong trong bao cao
            default -> null;                       // khong nhan dien -> giu nguyen, khong dong den
        };
    }

    /** BANG ANH XA MAU CHU da hardcode tu cac ban UITheme rat cu (truoc khi co Dark mode). */
    private static Color anhXaMauChu(Color mauCu) {
        if (mauCu == null) return null;
        int rgb = mauCu.getRGB() & 0xFFFFFF;
        return switch (rgb) {
            case 0x181A2E -> UITheme.TEXT_PRIMARY; // L_TEXT_PRIMARY hien tai (Indigo Ink)
            case 0x1F2937 -> UITheme.TEXT_PRIMARY; // TEXT_PRIMARY ban rat cu (truoc khi doi bang mau)
            default -> null;
        };
    }
}