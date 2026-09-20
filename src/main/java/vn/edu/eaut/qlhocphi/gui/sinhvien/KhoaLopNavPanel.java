package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.model.SinhVien;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * Điều hướng 3 cấp: Khoa → Năm học → Lớp (chuẩn quản lý SV đại học).
 */
public class KhoaLopNavPanel extends JPanel {

    private static final String CHON_KHOA = "-- Chọn Khoa --";
    private static final String CHON_NAM = "-- Chọn Năm học --";
    private static final String CHON_LOP = "-- Chọn Lớp --";
    private static final String TAT_CA_SV = "★ Tất cả sinh viên";
    private static final String TAT_CA_NAM = "Tất cả năm";
    private static final String TAT_CA_LOP = "Tất cả lớp";

    public static class LuaChon {
        public final String khoa;
        public final Integer namHoc; // null = tất cả năm
        public final String lop;     // null = tất cả lớp

        public LuaChon(String khoa, Integer namHoc, String lop) {
            this.khoa = khoa;
            this.namHoc = namHoc;
            this.lop = lop;
        }

        public static final LuaChon TAT_CA = new LuaChon("__TAT_CA__", null, null);
    }

    private final JComboBox<String> cboKhoa = new JComboBox<>();
    private final JComboBox<String> cboNam = new JComboBox<>();
    private final JComboBox<String> cboLop = new JComboBox<>();
    private final JLabel lblThongKe = new JLabel(" ");
    private Consumer<LuaChon> khiChon;

    private final Map<String, Map<Integer, TreeSet<String>>> banDo = new LinkedHashMap<>();
    private final Map<String, Map<Integer, Integer>> demSV = new LinkedHashMap<>();
    private boolean dangNap = false;

    public KhoaLopNavPanel() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);

        JLabel tieuDe = new JLabel("ĐIỀU HƯỚNG KHOA / NĂM / LỚP");
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tieuDe.setForeground(UITheme.TEXT_MUTED);
        add(tieuDe, BorderLayout.NORTH);

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        box.add(nhanNho("1. Khoa"));
        box.add(Box.createRigidArea(new Dimension(0, 4)));
        styleCombo(cboKhoa, UITheme.PRIMARY);
        box.add(cboKhoa);

        box.add(Box.createRigidArea(new Dimension(0, 12)));
        box.add(nhanNho("2. Năm học"));
        box.add(Box.createRigidArea(new Dimension(0, 4)));
        styleCombo(cboNam, new Color(0x0E, 0xA5, 0xE9));
        box.add(cboNam);

        box.add(Box.createRigidArea(new Dimension(0, 12)));
        box.add(nhanNho("3. Lớp"));
        box.add(Box.createRigidArea(new Dimension(0, 4)));
        styleCombo(cboLop, UITheme.ACCENT_TEAL);
        box.add(cboLop);

        box.add(Box.createRigidArea(new Dimension(0, 14)));
        lblThongKe.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblThongKe.setForeground(UITheme.TEXT_MUTED);
        lblThongKe.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(lblThongKe);

        add(box, BorderLayout.CENTER);

        cboNam.setEnabled(false);
        cboLop.setEnabled(false);

        cboKhoa.addActionListener(e -> { if (!dangNap) xuLyDoiKhoa(); });
        cboNam.addActionListener(e -> { if (!dangNap) xuLyDoiNam(); });
        cboLop.addActionListener(e -> { if (!dangNap) xuLyDoiLop(); });
    }

    public void setKhiChon(Consumer<LuaChon> khiChon) {
        this.khiChon = khiChon;
    }

    public void capNhatDuLieu(List<SinhVien> danhSach) {
        banDo.clear();
        demSV.clear();
        if (danhSach != null) {
            for (SinhVien sv : danhSach) {
                String khoa = (sv.getKhoa() == null || sv.getKhoa().isBlank())
                        ? "(Chưa phân khoa)" : sv.getKhoa().trim();
                int nam = sv.getNamHoc() > 0 ? sv.getNamHoc() : 1;
                String lop = (sv.getLop() == null || sv.getLop().isBlank())
                        ? "(Chưa phân lớp)" : sv.getLop().trim();

                banDo.computeIfAbsent(khoa, k -> new TreeMap<>())
                        .computeIfAbsent(nam, n -> new TreeSet<>())
                        .add(lop);
                demSV.computeIfAbsent(khoa, k -> new TreeMap<>())
                        .merge(nam, 1, Integer::sum);
            }
        }

        dangNap = true;
        try {
            cboKhoa.removeAllItems();
            cboKhoa.addItem(CHON_KHOA);
            cboKhoa.addItem(TAT_CA_SV);
            for (String k : banDo.keySet()) cboKhoa.addItem(k);

            cboNam.removeAllItems();
            cboNam.addItem(CHON_NAM);
            cboNam.setEnabled(false);

            cboLop.removeAllItems();
            cboLop.addItem(CHON_LOP);
            cboLop.setEnabled(false);

            lblThongKe.setText("Chọn Khoa để xem năm / lớp");
        } finally {
            dangNap = false;
        }
    }

    private void xuLyDoiKhoa() {
        String khoa = (String) cboKhoa.getSelectedItem();

        if (TAT_CA_SV.equals(khoa)) {
            dangNap = true;
            try {
                cboNam.removeAllItems();
                cboNam.addItem(CHON_NAM);
                cboNam.setEnabled(false);
                cboLop.removeAllItems();
                cboLop.addItem(CHON_LOP);
                cboLop.setEnabled(false);
            } finally {
                dangNap = false;
            }
            lblThongKe.setText("Đang xem: toàn bộ sinh viên");
            if (khiChon != null) khiChon.accept(LuaChon.TAT_CA);
            return;
        }

        boolean coKhoa = khoa != null && !CHON_KHOA.equals(khoa);
        dangNap = true;
        try {
            cboNam.removeAllItems();
            cboNam.addItem(CHON_NAM);
            if (coKhoa) {
                cboNam.addItem(TAT_CA_NAM);
                Map<Integer, TreeSet<String>> theoNam = banDo.getOrDefault(khoa, Map.of());
                for (Integer n : theoNam.keySet()) {
                    int so = demSV.getOrDefault(khoa, Map.of()).getOrDefault(n, 0);
                    cboNam.addItem("Năm " + n + "  (" + so + " SV)");
                }
                cboNam.setEnabled(true);
            } else {
                cboNam.setEnabled(false);
            }
            cboLop.removeAllItems();
            cboLop.addItem(CHON_LOP);
            cboLop.setEnabled(false);
        } finally {
            dangNap = false;
        }
        lblThongKe.setText(coKhoa ? "Chọn Năm học của khoa " + khoa : "Chọn Khoa để tiếp tục");
        if (khiChon != null) khiChon.accept(null);
    }

    private void xuLyDoiNam() {
        String khoa = (String) cboKhoa.getSelectedItem();
        String namStr = (String) cboNam.getSelectedItem();
        boolean coKhoa = khoa != null && !CHON_KHOA.equals(khoa) && !TAT_CA_SV.equals(khoa);
        boolean coNam = namStr != null && !CHON_NAM.equals(namStr);

        if (!coKhoa || !coNam) {
            cboLop.setEnabled(false);
            if (khiChon != null) khiChon.accept(null);
            return;
        }

        if (TAT_CA_NAM.equals(namStr)) {
            dangNap = true;
            try {
                cboLop.removeAllItems();
                cboLop.addItem(CHON_LOP);
                cboLop.addItem(TAT_CA_LOP);
                Map<Integer, TreeSet<String>> theoNam = banDo.getOrDefault(khoa, Map.of());
                TreeSet<String> allLop = new TreeSet<>();
                for (TreeSet<String> s : theoNam.values()) allLop.addAll(s);
                for (String l : allLop) cboLop.addItem(l);
                cboLop.setEnabled(true);
            } finally {
                dangNap = false;
            }
            lblThongKe.setText("Khoa " + khoa + " · Tất cả năm");
            if (khiChon != null) khiChon.accept(new LuaChon(khoa, null, null));
            return;
        }

        Integer nam = trichNam(namStr);
        dangNap = true;
        try {
            cboLop.removeAllItems();
            cboLop.addItem(CHON_LOP);
            cboLop.addItem(TAT_CA_LOP);
            TreeSet<String> lops = banDo.getOrDefault(khoa, Map.of())
                    .getOrDefault(nam, new TreeSet<>());
            for (String l : lops) cboLop.addItem(l);
            cboLop.setEnabled(true);
        } finally {
            dangNap = false;
        }
        int so = demSV.getOrDefault(khoa, Map.of()).getOrDefault(nam, 0);
        lblThongKe.setText("Khoa " + khoa + " · Năm " + nam + " · " + so + " SV");
        if (khiChon != null) khiChon.accept(new LuaChon(khoa, nam, null));
    }

    private void xuLyDoiLop() {
        String khoa = (String) cboKhoa.getSelectedItem();
        String namStr = (String) cboNam.getSelectedItem();
        String lop = (String) cboLop.getSelectedItem();

        boolean coKhoa = khoa != null && !CHON_KHOA.equals(khoa) && !TAT_CA_SV.equals(khoa);
        boolean coNam = namStr != null && !CHON_NAM.equals(namStr);
        boolean coLop = lop != null && !CHON_LOP.equals(lop);

        if (!coKhoa || !coNam || !coLop) {
            if (khiChon != null) khiChon.accept(null);
            return;
        }

        Integer nam = TAT_CA_NAM.equals(namStr) ? null : trichNam(namStr);
        String lopFilter = TAT_CA_LOP.equals(lop) ? null : lop;

        lblThongKe.setText("Khoa " + khoa
                + (nam != null ? " · Năm " + nam : " · Tất cả năm")
                + (lopFilter != null ? " · Lớp " + lopFilter : " · Tất cả lớp"));
        if (khiChon != null) khiChon.accept(new LuaChon(khoa, nam, lopFilter));
    }

    private Integer trichNam(String s) {
        if (s == null) return null;
        try {
            String p = s.replace("Năm ", "").trim().split("\\s+")[0];
            return Integer.parseInt(p);
        } catch (Exception e) {
            return null;
        }
    }

    private JLabel nhanNho(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(UITheme.TEXT_PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

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