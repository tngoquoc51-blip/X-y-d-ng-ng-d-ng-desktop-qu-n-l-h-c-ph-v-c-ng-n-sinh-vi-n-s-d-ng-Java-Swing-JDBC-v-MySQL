package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.bus.TinChiService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.TinChiSinhVien;
import vn.edu.eaut.qlhocphi.model.TinChiTheoNam;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Locale;

/**
 * Tiến độ học tập & tín chỉ — tổng CT mặc định 183 TC, điểm TB hệ 10/4.
 */
public class HocTapTinChiPanel extends JPanel {
    private static final int TONG_TC_MAC_DINH = 183;

    private final TaiKhoan taiKhoan;
    private final TinChiService service = new TinChiService();

    private JLabel lblNamThu, lblNamNhap, lblTrangThai;
    private JLabel lblTongCT, lblDaHoc, lblTichLuy, lblConThieu;
    private JLabel lblBiRut, lblDangKy, lblHieuLuc;
    private JLabel lblTB10, lblTB4, lblTBTL10, lblTBTL4;
    private JProgressBar barTienDo;
    private DefaultTableModel modelNam;

    public HocTapTinChiPanel(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(4, 4, 4, 4));

        add(buildBanner(), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(buildTheTienDo());
        body.add(Box.createRigidArea(new Dimension(0, 14)));
        body.add(buildTheTinChiChinh());
        body.add(Box.createRigidArea(new Dimension(0, 14)));
        body.add(buildTheTinChiPhu());
        body.add(Box.createRigidArea(new Dimension(0, 14)));
        body.add(buildTheDiem());
        body.add(Box.createRigidArea(new Dimension(0, 14)));
        body.add(buildBangTheoNam());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        taiDuLieu();
    }

    private JPanel buildBanner() {
        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout());
        banner.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        banner.setPreferredSize(new Dimension(10, 88));

        JLabel title = new JLabel("Tiến độ học tập & Tín chỉ");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Tổng CT 183 TC · Tín chỉ tích lũy · Điểm TB hệ 10 / hệ 4");
        sub.setFont(UITheme.FONT_BASE);
        sub.setForeground(new Color(255, 255, 255, 210));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(Box.createRigidArea(new Dimension(0, 4)));
        text.add(sub);
        banner.add(text, BorderLayout.WEST);
        return banner;
    }

    private JPanel buildTheTienDo() {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JPanel c1 = UITheme.statCard("Năm học thứ", "—", UITheme.TINT_BLUE, UITheme.TEXT_BLUE);
        JPanel c2 = UITheme.statCard("Năm nhập học", "—", UITheme.TINT_VIOLET, UITheme.TEXT_VIOLET);
        JPanel c3 = UITheme.statCard("Trạng thái", "—", UITheme.TINT_GREEN, UITheme.TEXT_GREEN);
        row.add(c1); row.add(c2); row.add(c3);
        lblNamThu = findGiaTri(c1);
        lblNamNhap = findGiaTri(c2);
        lblTrangThai = findGiaTri(c3);
        return row;
    }

    private JPanel buildTheTinChiChinh() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        card.add(UITheme.sectionLabel("Tín chỉ chương trình"), BorderLayout.NORTH);

        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        JPanel a = UITheme.statCard("Tổng TC chương trình", String.valueOf(TONG_TC_MAC_DINH),
                new Color(0xEF, 0xF6, 0xFF), new Color(0x1D, 0x4E, 0xD8));
        JPanel b = UITheme.statCard("TC đã học", "0",
                new Color(0xF0, 0xFD, 0xFA), new Color(0x0F, 0x76, 0x6E));
        JPanel c = UITheme.statCard("TC đã tích lũy", "0",
                new Color(0xEC, 0xFD, 0xF5), new Color(0x05, 0x96, 0x69));
        JPanel d = UITheme.statCard("Còn thiếu", "0",
                new Color(0xFF, 0xF7, 0xED), new Color(0xEA, 0x58, 0x0C));
        row.add(a); row.add(b); row.add(c); row.add(d);
        lblTongCT = findGiaTri(a);
        lblDaHoc = findGiaTri(b);
        lblTichLuy = findGiaTri(c);
        lblConThieu = findGiaTri(d);

        barTienDo = new JProgressBar(0, TONG_TC_MAC_DINH);
        barTienDo.setStringPainted(true);
        barTienDo.setString("0 / " + TONG_TC_MAC_DINH);
        barTienDo.setFont(UITheme.FONT_BOLD);
        barTienDo.setForeground(new Color(0x05, 0x96, 0x69));
        barTienDo.setBackground(new Color(0xE5, 0xE7, 0xEB));
        barTienDo.setPreferredSize(new Dimension(10, 20));
        barTienDo.setBorderPainted(false);

        JPanel mid = new JPanel(new BorderLayout(0, 8));
        mid.setOpaque(false);
        mid.add(row, BorderLayout.CENTER);
        mid.add(barTienDo, BorderLayout.SOUTH);
        card.add(mid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildTheTinChiPhu() {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        JPanel a = UITheme.statCard("TC bị rút", "0", new Color(0xFE, 0xF2, 0xF2), new Color(0xDC, 0x26, 0x26));
        JPanel b = UITheme.statCard("Đang đăng ký", "0", new Color(0xEF, 0xF6, 0xFF), new Color(0x1D, 0x4E, 0xD8));
        JPanel c = UITheme.statCard("TC có hiệu lực", "0", new Color(0xF5, 0xF3, 0xFF), new Color(0x7C, 0x3A, 0xED));
        row.add(a); row.add(b); row.add(c);
        lblBiRut = findGiaTri(a);
        lblDangKy = findGiaTri(b);
        lblHieuLuc = findGiaTri(c);
        return row;
    }

    private JPanel buildTheDiem() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.add(UITheme.sectionLabel("Điểm trung bình"), BorderLayout.NORTH);

        JPanel row = new JPanel(new GridLayout(1, 4, 12, 0));
        row.setOpaque(false);
        JPanel a = UITheme.statCard("ĐTB hệ 10", "—", new Color(0xEF, 0xF6, 0xFF), new Color(0x25, 0x63, 0xEB));
        JPanel b = UITheme.statCard("ĐTB hệ 4", "—", new Color(0xF5, 0xF3, 0xFF), new Color(0x7C, 0x3A, 0xED));
        JPanel c = UITheme.statCard("ĐTB tích lũy hệ 10", "—", new Color(0xEC, 0xFD, 0xF5), new Color(0x05, 0x96, 0x69));
        JPanel d = UITheme.statCard("ĐTB tích lũy hệ 4", "—", new Color(0xFE, 0xF3, 0xC7), new Color(0xD9, 0x77, 0x06));
        row.add(a); row.add(b); row.add(c); row.add(d);
        lblTB10 = findGiaTri(a);
        lblTB4 = findGiaTri(b);
        lblTBTL10 = findGiaTri(c);
        lblTBTL4 = findGiaTri(d);
        card.add(row, BorderLayout.CENTER);
        return card;
    }

    private JLabel findGiaTri(JPanel the) {
        for (Component comp : the.getComponents()) {
            if (comp instanceof JLabel && "giaTri".equals(comp.getName())) return (JLabel) comp;
        }
        JLabel found = null;
        for (Component comp : the.getComponents()) {
            if (comp instanceof JLabel lbl) {
                if (found == null || lbl.getFont().getSize() > found.getFont().getSize()) found = lbl;
            }
        }
        return found != null ? found : new JLabel("—");
    }

    private JPanel buildBangTheoNam() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.add(UITheme.sectionLabel("Chi tiết tín chỉ theo từng năm học"), BorderLayout.NORTH);

        modelNam = new DefaultTableModel(new String[]{"Năm học", "Tín chỉ đạt", "Tín chỉ rút", "Ghi chú"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(modelNam);
        table.setRowHeight(36);
        table.setFont(UITheme.FONT_BASE);
        table.getTableHeader().setFont(UITheme.FONT_BOLD);
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(10, 200));
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private void taiDuLieu() {
        String maSV = taiKhoan.getMaSV() != null && !taiKhoan.getMaSV().isBlank()
                ? taiKhoan.getMaSV() : taiKhoan.getTenDangNhap();
        SwingWorker<Object[], Void> w = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                return new Object[]{service.layTienDo(maSV), service.layLichSuNam(maSV)};
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] kq = get();
                    TinChiSinhVien tc = (TinChiSinhVien) kq[0];
                    List<TinChiTheoNam> ls = (List<TinChiTheoNam>) kq[1];
                    if (tc == null) {
                        UIUtils.thongBaoLoi(HocTapTinChiPanel.this,
                                "Không tìm thấy hồ sơ tín chỉ. Kiểm tra mã SV liên kết tài khoản.");
                        return;
                    }
                    if (lblNamThu != null)
                        lblNamThu.setText(tc.getNamThu() != null ? ("Năm " + tc.getNamThu()) : "—");
                    if (lblNamNhap != null)
                        lblNamNhap.setText(tc.getNamNhapHoc() != null ? String.valueOf(tc.getNamNhapHoc()) : "—");
                    if (lblTrangThai != null)
                        lblTrangThai.setText(nhanTT(tc.getTrangThaiHoc()));

                    int tong = tc.getTongTcChuongTrinh() > 0 ? tc.getTongTcChuongTrinh() : TONG_TC_MAC_DINH;
                    int tichLuy = tc.getTinChiTichLuy();
                    int daHoc = tc.getTinChiDaHoc() > 0 ? tc.getTinChiDaHoc() : tichLuy;
                    int conThieu = Math.max(0, tong - tichLuy);
                    int pct = tong > 0 ? (int) Math.round(tichLuy * 100.0 / tong) : 0;

                    setTxt(lblTongCT, String.valueOf(tong));
                    setTxt(lblDaHoc, String.valueOf(daHoc));
                    setTxt(lblTichLuy, String.valueOf(tichLuy));
                    setTxt(lblConThieu, String.valueOf(conThieu));
                    setTxt(lblBiRut, String.valueOf(tc.getTinChiBiRut()));
                    setTxt(lblDangKy, String.valueOf(tc.getTinChiDangKy()));
                    setTxt(lblHieuLuc, String.valueOf(tc.getTinChiConHieuLuc()));

                    setTxt(lblTB10, fmt(tc.getDiemTB10()));
                    setTxt(lblTB4, fmt(tc.getDiemTB4()));
                    setTxt(lblTBTL10, fmt(tc.getDiemTBTichLuy10()));
                    setTxt(lblTBTL4, fmt(tc.getDiemTBTichLuy4()));

                    if (barTienDo != null) {
                        barTienDo.setMaximum(tong);
                        barTienDo.setValue(Math.min(tichLuy, tong));
                        barTienDo.setString(tichLuy + " / " + tong + " TC (" + pct + "%)");
                    }

                    modelNam.setRowCount(0);
                    if (ls != null) {
                        for (TinChiTheoNam n : ls) {
                            modelNam.addRow(new Object[]{
                                    n.getNamHoc(), n.getTinChiDat(), n.getTinChiRut(),
                                    n.getGhiChu() != null ? n.getGhiChu() : ""
                            });
                        }
                    }
                    if (modelNam.getRowCount() == 0) {
                        modelNam.addRow(new Object[]{"—", "—", "—", "Chưa có lịch sử theo năm"});
                    }
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(HocTapTinChiPanel.this, ex.getMessage());
                }
            }
        };
        w.execute();
    }

    private static void setTxt(JLabel l, String s) {
        if (l != null) l.setText(s);
    }

    private static String fmt(double d) {
        if (d <= 0) return "—";
        return String.format(Locale.US, "%.2f", d);
    }

    private static String nhanTT(String tt) {
        if (tt == null) return "—";
        return switch (tt) {
            case "DANG_HOC" -> "Đang học";
            case "RA_TRUONG" -> "Ra trường";
            case "THOI_HOC" -> "Thôi học";
            default -> tt;
        };
    }
}