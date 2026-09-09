package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.model.SinhVien;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * Panel dieu huong dang 2 NUT XO (JComboBox) rieng biet: 1 nut chon Khoa, 1
 * nut chon Lop. Mac dinh Lop bi khoa (disable) cho toi khi da chon 1 Khoa cu
 * the. Du lieu sinh vien CHI hien len bang khi da chon xong CA Khoa LAN Lop -
 * chua chon du ca 2 thi bang van an, dung yeu cau.
 */
public class KhoaLopNavPanel extends JPanel {

    private static final String CHON_KHOA = "-- Chon Khoa --";
    private static final String CHON_LOP = "-- Chon Lop --";
    private static final String TAT_CA_SV = "\u2605 Tat ca sinh vien (moi Khoa)";

    /** Ket qua 1 lan chon: null (truyen ve callback) = "chua chon du Khoa+Lop" (an du lieu). */
    public static class LuaChon {
        public final String khoa;
        public final String lop;
        public LuaChon(String khoa, String lop) { this.khoa = khoa; this.lop = lop; }
        public static final LuaChon TAT_CA = new LuaChon("__TAT_CA__", null);
    }

    private final JComboBox<String> cboKhoa = new JComboBox<>();
    private final JComboBox<String> cboLop = new JComboBox<>();
    private Consumer<LuaChon> khiChon;

    /** Khoa -> danh sach ten Lop cua Khoa do - dung de nap lai cboLop moi khi doi Khoa. */
    private final Map<String, List<String>> banDoKhoaLop = new LinkedHashMap<>();
    private boolean dangNapDuLieu = false; // chan khong cho fire su kien khi dang set item bang code

    public KhoaLopNavPanel() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);

        JLabel tieuDe = new JLabel("DIEU HUONG THEO KHOA / LOP");
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tieuDe.setForeground(UITheme.TEXT_MUTED);
        add(tieuDe, BorderLayout.NORTH);

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        JLabel lblKhoa = nhanNho("Khoa");
        box.add(lblKhoa);
        box.add(Box.createRigidArea(new Dimension(0, 4)));
        styleCombo(cboKhoa, UITheme.PRIMARY);
        box.add(cboKhoa);

        box.add(Box.createRigidArea(new Dimension(0, 16)));

        JLabel lblLop = nhanNho("Lop");
        box.add(lblLop);
        box.add(Box.createRigidArea(new Dimension(0, 4)));
        styleCombo(cboLop, UITheme.ACCENT_TEAL);
        cboLop.setEnabled(false); // Lop bi khoa cho toi khi da chon Khoa
        box.add(cboLop);

        add(box, BorderLayout.CENTER);

        cboKhoa.addActionListener(e -> { if (!dangNapDuLieu) xuLyDoiKhoa(); });
        cboLop.addActionListener(e -> { if (!dangNapDuLieu) xuLyDoiLop(); });
    }

    public void setKhiChon(Consumer<LuaChon> khiChon) {
        this.khiChon = khiChon;
    }

    /** Xay lai danh sach Khoa (va ban do Khoa->Lop) tu du lieu sinh vien hien co. */
    public void capNhatDuLieu(List<SinhVien> danhSach) {
        String khoaDangChon = (String) cboKhoa.getSelectedItem();
        String lopDangChon = (String) cboLop.getSelectedItem();

        Map<String, List<String>> gomNhom = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (danhSach != null) {
            for (SinhVien sv : danhSach) {
                String khoa = trongRong(sv.getKhoa()) ? "(Chua phan khoa)" : sv.getKhoa().trim();
                String lop = trongRong(sv.getLop()) ? "(Chua phan lop)" : sv.getLop().trim();
                gomNhom.computeIfAbsent(khoa, k -> new ArrayList<>());
                if (!gomNhom.get(khoa).contains(lop)) gomNhom.get(khoa).add(lop);
            }
        }
        for (List<String> ds : gomNhom.values()) ds.sort(String.CASE_INSENSITIVE_ORDER);

        banDoKhoaLop.clear();
        banDoKhoaLop.putAll(gomNhom);

        dangNapDuLieu = true;
        try {
            cboKhoa.removeAllItems();
            cboKhoa.addItem(CHON_KHOA);
            cboKhoa.addItem(TAT_CA_SV);
            for (String khoa : banDoKhoaLop.keySet()) cboKhoa.addItem(khoa);

            if (khoaDangChon != null && banDoKhoaLop.containsKey(khoaDangChon)) {
                cboKhoa.setSelectedItem(khoaDangChon);
                napComboLop(khoaDangChon);
                if (lopDangChon != null && banDoKhoaLop.get(khoaDangChon).contains(lopDangChon)) {
                    cboLop.setSelectedItem(lopDangChon);
                }
            } else {
                cboKhoa.setSelectedItem(CHON_KHOA);
                napComboLop(null);
            }
        } finally {
            dangNapDuLieu = false;
        }
    }

    private boolean trongRong(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** Nap lai cboLop theo Khoa dang chon. Neu khoa == null, Lop bi khoa (disable) va rong. */
    private void napComboLop(String khoa) {
        cboLop.removeAllItems();
        cboLop.addItem(CHON_LOP);
        if (khoa != null && banDoKhoaLop.containsKey(khoa)) {
            for (String lop : banDoKhoaLop.get(khoa)) cboLop.addItem(lop);
            cboLop.setEnabled(true);
        } else {
            cboLop.setEnabled(false);
        }
        cboLop.setSelectedItem(CHON_LOP);
    }

    private void xuLyDoiKhoa() {
        String khoa = (String) cboKhoa.getSelectedItem();

        // Chon "Tat ca sinh vien": bo qua yeu cau phai chon them Lop, hien du lieu NGAY LAP TUC -
        // day la tinh nang moi cho phep Admin xem toan bo sinh vien moi Khoa cung 1 luc.
        if (TAT_CA_SV.equals(khoa)) {
            dangNapDuLieu = true;
            try {
                napComboLop(null); // Lop khong con y nghia trong che do "Tat ca" -> khoa lai cho ro rang
            } finally {
                dangNapDuLieu = false;
            }
            if (khiChon != null) khiChon.accept(LuaChon.TAT_CA);
            return;
        }

        boolean coKhoaThat = khoa != null && !CHON_KHOA.equals(khoa) && !TAT_CA_SV.equals(khoa);

        dangNapDuLieu = true;
        try {
            napComboLop(coKhoaThat ? khoa : null);
        } finally {
            dangNapDuLieu = false;
        }
        // Vua doi Khoa xong, Lop bi reset ve rong -> chua du dieu kien de hien du lieu.
        baoAn();
    }

    private void xuLyDoiLop() {
        String khoa = (String) cboKhoa.getSelectedItem();
        String lop = (String) cboLop.getSelectedItem();
        boolean coKhoaThat = khoa != null && !CHON_KHOA.equals(khoa);
        boolean coLopThat = lop != null && !CHON_LOP.equals(lop);

        if (coKhoaThat && coLopThat) {
            if (khiChon != null) khiChon.accept(new LuaChon(khoa, lop));
        } else {
            baoAn();
        }
    }

    /** Bao ve man hinh chinh: chua chon du Khoa+Lop, an du lieu di. */
    private void baoAn() {
        if (khiChon != null) khiChon.accept(null);
    }

    private JLabel nhanNho(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(UITheme.TEXT_PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    /** To vien mau rieng cho tung combo (Khoa = xanh duong PRIMARY, Lop = xanh ngoc ACCENT_TEAL). */
    private void styleCombo(JComboBox<String> combo, Color mauVien) {
        combo.setFont(UITheme.FONT_BASE);
        combo.setBackground(Color.WHITE);
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(mauVien, 2, true),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        combo.setFocusable(false);
    }
}