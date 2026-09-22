package vn.edu.eaut.qlhocphi.gui.admin;

import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.util.BackupService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Sao lưu / Phục hồi CSDL – giao diện nâng cấp cho Admin.
 * Giữ nguyên BackupService (mysqldump/mysql) + SwingWorker.
 */
public class BackupRestorePanel extends JPanel {
    private static final DateTimeFormatter DINH_DANG_TEN_FILE = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter DINH_DANG_HIEN_THI = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final int CHU_KY_TU_DONG_PHUT = 15;

    private final BackupService backupService = new BackupService();

    private JLabel lblTrangThai;
    private JLabel lblTinhTrangTuDong;
    private JLabel lblThuMucTuDong;
    private JButton btnSaoLuu, btnPhucHoi;
    private JToggleButton btnTuDongSaoLuu;
    private JPanel khoiLichSu;
    private final List<String[]> lichSuThaoTac = new ArrayList<>();

    private File thuMucTuDongSaoLuu;
    private Timer timerTuDong;

    public BackupRestorePanel() {
        setLayout(new BorderLayout(0, 14));
        setOpaque(false);
        setBorder(new EmptyBorder(4, 4, 4, 4));

        JPanel giua = new JPanel();
        giua.setOpaque(false);
        giua.setLayout(new BoxLayout(giua, BoxLayout.Y_AXIS));

        giua.add(buildHeader());
        giua.add(Box.createRigidArea(new Dimension(0, 14)));
        giua.add(buildThongTinKetNoiRow());
        giua.add(Box.createRigidArea(new Dimension(0, 14)));

        JPanel haiCot = new JPanel(new GridLayout(1, 2, 14, 0));
        haiCot.setOpaque(false);
        haiCot.setAlignmentX(Component.LEFT_ALIGNMENT);
        haiCot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        haiCot.add(buildBackupCard());
        haiCot.add(buildRestoreCard());
        giua.add(haiCot);

        giua.add(Box.createRigidArea(new Dimension(0, 14)));
        giua.add(buildTrangThaiBar());
        giua.add(Box.createRigidArea(new Dimension(0, 12)));
        giua.add(buildLichSuCard());

        JScrollPane scroll = new JScrollPane(giua);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    // ================== HEADER ==================

    private JPanel buildHeader() {
        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout(12, 0));
        banner.setBorder(new EmptyBorder(16, 20, 16, 20));
        banner.setPreferredSize(new Dimension(10, 84));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tieuDe = new JLabel("Sao lưu & Phục hồi CSDL");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);

        JLabel phu = new JLabel("Bảo vệ dữ liệu học phí · Xuất .sql thủ công hoặc tự động · Phục hồi khẩn cấp");
        phu.setFont(UITheme.FONT_BASE);
        phu.setForeground(new Color(255, 255, 255, 210));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(tieuDe);
        text.add(Box.createRigidArea(new Dimension(0, 4)));
        text.add(phu);

        banner.add(text, BorderLayout.CENTER);
        return banner;
    }

    // ================== THÔNG TIN KẾT NỐI ==================

    private JPanel buildThongTinKetNoiRow() {
        String host = "localhost";
        String port = "3310";
        String db = "qlhocphi";
        try {
            String url = vn.edu.eaut.qlhocphi.config.AppConfig.get("db.url");
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("jdbc:mysql://([^:/]+)(:(\\d+))?/([^?]+)")
                    .matcher(url != null ? url : "");
            if (matcher.find()) {
                host = matcher.group(1);
                if (matcher.group(3) != null) port = matcher.group(3);
                db = matcher.group(4);
            }
        } catch (Exception ignored) {}

        JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));

        row.add(theInfo("Máy chủ CSDL", host + ":" + port, UITheme.TEXT_BLUE != null ? UITheme.TEXT_BLUE : new Color(0x1D, 0x4E, 0xD8)));
        row.add(theInfo("Cơ sở dữ liệu", db, UITheme.PRIMARY));
        lblTinhTrangTuDong = new JLabel("Đang tắt");
        lblTinhTrangTuDong.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTinhTrangTuDong.setForeground(UITheme.WARNING != null ? UITheme.WARNING : new Color(0xD9, 0x77, 0x06));
        row.add(theInfoVoiLabel("Tự động sao lưu", lblTinhTrangTuDong));

        return row;
    }

    private JPanel theInfo(String tieuDe, String giaTri, Color mau) {
        JPanel p = UITheme.card();
        p.setLayout(new BorderLayout(0, 4));
        p.setBorder(new EmptyBorder(12, 14, 12, 14));
        JLabel td = new JLabel(tieuDe);
        td.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        td.setForeground(UITheme.TEXT_MUTED);
        JLabel gt = new JLabel(giaTri);
        gt.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gt.setForeground(mau);
        p.add(td, BorderLayout.NORTH);
        p.add(gt, BorderLayout.CENTER);
        return p;
    }

    private JPanel theInfoVoiLabel(String tieuDe, JLabel giaTri) {
        JPanel p = UITheme.card();
        p.setLayout(new BorderLayout(0, 4));
        p.setBorder(new EmptyBorder(12, 14, 12, 14));
        JLabel td = new JLabel(tieuDe);
        td.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        td.setForeground(UITheme.TEXT_MUTED);
        p.add(td, BorderLayout.NORTH);
        p.add(giaTri, BorderLayout.CENTER);
        return p;
    }

    // ================== CARD SAO LƯU ==================

    private JPanel buildBackupCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel td = new JLabel("Sao lưu dữ liệu");
        td.setFont(new Font("Segoe UI", Font.BOLD, 16));
        td.setForeground(UITheme.TEXT_PRIMARY);
        td.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel moTa = new JLabel("<html>Xuất toàn bộ CSDL ra file <b>.sql</b>. Nên sao lưu định kỳ<br>"
                + "(hàng tuần) và lưu trữ ngoài máy chủ.</html>");
        moTa.setFont(UITheme.FONT_BASE);
        moTa.setForeground(UITheme.TEXT_MUTED);
        moTa.setAlignmentX(Component.LEFT_ALIGNMENT);
        moTa.setBorder(new EmptyBorder(8, 0, 12, 0));

        btnSaoLuu = UITheme.primaryButton("⬇  Sao lưu ngay");
        btnSaoLuu.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSaoLuu.setMaximumSize(new Dimension(220, 40));
        btnSaoLuu.addActionListener(e -> thucHienSaoLuu());

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tdAuto = new JLabel("Tự động sao lưu định kỳ (" + CHU_KY_TU_DONG_PHUT + " phút/lần)");
        tdAuto.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tdAuto.setForeground(UITheme.TEXT_PRIMARY);
        tdAuto.setAlignmentX(Component.LEFT_ALIGNMENT);
        tdAuto.setBorder(new EmptyBorder(12, 0, 4, 0));

        lblThuMucTuDong = new JLabel("Chưa chọn thư mục lưu tự động");
        lblThuMucTuDong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblThuMucTuDong.setForeground(UITheme.TEXT_MUTED);
        lblThuMucTuDong.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnTuDongSaoLuu = new JToggleButton("Bật tự động sao lưu");
        btnTuDongSaoLuu.setFont(UITheme.FONT_BOLD);
        btnTuDongSaoLuu.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnTuDongSaoLuu.setMaximumSize(new Dimension(220, 36));
        btnTuDongSaoLuu.addActionListener(e -> chuyenDoiTuDongSaoLuu());

        card.add(td);
        card.add(moTa);
        card.add(btnSaoLuu);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(sep);
        card.add(tdAuto);
        card.add(lblThuMucTuDong);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(btnTuDongSaoLuu);
        card.add(Box.createVerticalGlue());
        return card;
    }

    // ================== CARD PHỤC HỒI ==================

    private JPanel buildRestoreCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel td = new JLabel("Phục hồi dữ liệu");
        td.setFont(new Font("Segoe UI", Font.BOLD, 16));
        td.setForeground(UITheme.TEXT_PRIMARY);
        td.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel canhBao = new JLabel("<html><b style='color:#B91C1C'>Cảnh báo:</b> Phục hồi sẽ <b>GHI ĐÈ</b> toàn bộ dữ liệu<br>"
                + "hiện tại bằng nội dung file .sql. Hãy sao lưu trước khi phục hồi.</html>");
        canhBao.setFont(UITheme.FONT_BASE);
        canhBao.setForeground(UITheme.TEXT_MUTED);
        canhBao.setAlignmentX(Component.LEFT_ALIGNMENT);
        canhBao.setBorder(new EmptyBorder(8, 0, 14, 0));

        btnPhucHoi = UITheme.dangerButton("⬆  Chọn file và phục hồi");
        btnPhucHoi.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPhucHoi.setMaximumSize(new Dimension(240, 40));
        btnPhucHoi.addActionListener(e -> thucHienPhucHoi());

        JLabel goiY = new JLabel("<html><br>Gợi ý vận hành:<br>"
                + "• Chỉ phục hồi khi có sự cố hoặc chuyển máy<br>"
                + "• Kiểm tra file .sql còn nguyên vẹn<br>"
                + "• Khởi động lại app sau khi phục hồi thành công</html>");
        goiY.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        goiY.setForeground(UITheme.TEXT_MUTED);
        goiY.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(td);
        card.add(canhBao);
        card.add(btnPhucHoi);
        card.add(goiY);
        card.add(Box.createVerticalGlue());
        return card;
    }

    // ================== THANH TRẠNG THÁI ==================

    private JPanel buildTrangThaiBar() {
        JPanel bar = UITheme.card();
        bar.setLayout(new BorderLayout());
        bar.setBorder(new EmptyBorder(10, 14, 10, 14));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        lblTrangThai = new JLabel("Sẵn sàng – chọn Sao lưu ngay hoặc Phục hồi khi cần");
        lblTrangThai.setFont(UITheme.FONT_BASE);
        lblTrangThai.setForeground(UITheme.TEXT_MUTED);
        bar.add(lblTrangThai, BorderLayout.CENTER);
        return bar;
    }

    // ================== LỊCH SỬ ==================

    private JPanel buildLichSuCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JLabel td = new JLabel("Lịch sử thao tác (phiên làm việc này)");
        td.setFont(new Font("Segoe UI", Font.BOLD, 14));
        td.setForeground(UITheme.TEXT_PRIMARY);

        khoiLichSu = new JPanel();
        khoiLichSu.setOpaque(false);
        khoiLichSu.setLayout(new BoxLayout(khoiLichSu, BoxLayout.Y_AXIS));
        khoiLichSu.add(dongTrong());

        JScrollPane sp = new JScrollPane(khoiLichSu);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setPreferredSize(new Dimension(100, 140));

        card.add(td, BorderLayout.NORTH);
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JLabel dongTrong() {
        JLabel l = new JLabel("Chưa có thao tác nào trong phiên làm việc này");
        l.setFont(UITheme.FONT_BASE);
        l.setForeground(UITheme.TEXT_MUTED);
        l.setBorder(new EmptyBorder(6, 0, 6, 0));
        return l;
    }

    private void themLichSu(String loai, String duongDan, boolean thanhCong) {
        String gio = LocalDateTime.now().format(DINH_DANG_HIEN_THI);
        String tt = thanhCong ? "Thành công" : "Thất bại";
        lichSuThaoTac.add(0, new String[]{loai, gio, duongDan, tt});
        khoiLichSu.removeAll();
        int max = Math.min(lichSuThaoTac.size(), 8);
        for (int i = 0; i < max; i++) {
            String[] d = lichSuThaoTac.get(i);
            khoiLichSu.add(dongLichSu(d[0], d[1], d[2], d[3]));
            khoiLichSu.add(Box.createRigidArea(new Dimension(0, 4)));
        }
        khoiLichSu.revalidate();
        khoiLichSu.repaint();
    }

    private JPanel dongLichSu(String loai, String gio, String duongDan, String trangThai) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(true);
        p.setBackground(new Color(0xF8, 0xFA, 0xFC));
        p.setBorder(new EmptyBorder(8, 10, 8, 10));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel lbl = new JLabel(gio + "  ·  " + loai + "  ·  " + duongDan);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(UITheme.TEXT_PRIMARY);

        JLabel st = new JLabel(trangThai);
        st.setFont(new Font("Segoe UI", Font.BOLD, 12));
        st.setForeground("Thành công".equals(trangThai) ? UITheme.SUCCESS : UITheme.DANGER);

        p.add(lbl, BorderLayout.CENTER);
        p.add(st, BorderLayout.EAST);
        return p;
    }

    // ================== TỰ ĐỘNG ==================

    private void chuyenDoiTuDongSaoLuu() {
        if (btnTuDongSaoLuu.isSelected()) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Chọn thư mục lưu file sao lưu tự động");
            if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
                btnTuDongSaoLuu.setSelected(false);
                return;
            }
            thuMucTuDongSaoLuu = chooser.getSelectedFile();
            lblThuMucTuDong.setText("Thư mục: " + thuMucTuDongSaoLuu.getAbsolutePath());
            timerTuDong = new Timer(CHU_KY_TU_DONG_PHUT * 60 * 1000, e -> thucHienSaoLuuTuDong());
            timerTuDong.start();
            btnTuDongSaoLuu.setText("Đang bật tự động...");
            lblTinhTrangTuDong.setText("Đang bật (" + CHU_KY_TU_DONG_PHUT + " phút)");
            lblTinhTrangTuDong.setForeground(UITheme.SUCCESS);
        } else {
            if (timerTuDong != null) timerTuDong.stop();
            btnTuDongSaoLuu.setText("Bật tự động sao lưu");
            lblThuMucTuDong.setText("Chưa chọn thư mục lưu tự động");
            lblTinhTrangTuDong.setText("Đang tắt");
            lblTinhTrangTuDong.setForeground(UITheme.WARNING != null ? UITheme.WARNING : new Color(0xD9, 0x77, 0x06));
        }
    }

    private void thucHienSaoLuuTuDong() {
        if (thuMucTuDongSaoLuu == null) return;
        File fileDich = new File(thuMucTuDongSaoLuu,
                "backup_auto_" + LocalDateTime.now().format(DINH_DANG_TEN_FILE) + ".sql");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                backupService.saoLuu(fileDich);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    themLichSu("Sao lưu tự động", fileDich.getAbsolutePath(), true);
                    lblTrangThai.setForeground(UITheme.SUCCESS);
                    lblTrangThai.setText("Tự động sao lưu OK: " + fileDich.getName());
                } catch (Exception ex) {
                    themLichSu("Sao lưu tự động", fileDich.getAbsolutePath(), false);
                    baoLoi(ex);
                }
            }
        };
        worker.execute();
    }

    // ================== SAO LƯU / PHỤC HỒI THỦ CÔNG ==================

    private void thucHienSaoLuu() {
        JFileChooser chooser = new JFileChooser();
        String tenFileGoiY = "backup_qlhocphi_" + LocalDateTime.now().format(DINH_DANG_TEN_FILE) + ".sql";
        chooser.setSelectedFile(new File(tenFileGoiY));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File fileDich = chooser.getSelectedFile();

        capNhatTrangThaiDangXuLy("Đang sao lưu dữ liệu...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                backupService.saoLuu(fileDich);
                return null;
            }

            @Override
            protected void done() {
                enableButtons(true);
                try {
                    get();
                    themLichSu("Sao lưu thủ công", fileDich.getAbsolutePath(), true);
                    lblTrangThai.setForeground(UITheme.SUCCESS);
                    lblTrangThai.setText("Sao lưu thành công: " + fileDich.getAbsolutePath());
                } catch (Exception ex) {
                    themLichSu("Sao lưu thủ công", fileDich.getAbsolutePath(), false);
                    baoLoi(ex);
                }
            }
        };
        worker.execute();
    }

    private void thucHienPhucHoi() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Tập tin SQL (*.sql)", "sql"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File fileNguon = chooser.getSelectedFile();

        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Bạn chắc chắn muốn GHI ĐÈ toàn bộ dữ liệu hiện tại bằng file:\n" + fileNguon.getName() + " ?",
                "Xác nhận phục hồi", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (xacNhan != JOptionPane.YES_OPTION) return;

        capNhatTrangThaiDangXuLy("Đang phục hồi dữ liệu...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                backupService.phucHoi(fileNguon);
                return null;
            }

            @Override
            protected void done() {
                enableButtons(true);
                try {
                    get();
                    themLichSu("Phục hồi dữ liệu", fileNguon.getAbsolutePath(), true);
                    lblTrangThai.setForeground(UITheme.SUCCESS);
                    lblTrangThai.setText("Phục hồi thành công từ: " + fileNguon.getName()
                            + ". Nên khởi động lại ứng dụng.");
                } catch (Exception ex) {
                    themLichSu("Phục hồi dữ liệu", fileNguon.getAbsolutePath(), false);
                    baoLoi(ex);
                }
            }
        };
        worker.execute();
    }

    private void capNhatTrangThaiDangXuLy(String text) {
        enableButtons(false);
        lblTrangThai.setForeground(UITheme.TEXT_MUTED);
        lblTrangThai.setText(text);
    }

    private void enableButtons(boolean enabled) {
        btnSaoLuu.setEnabled(enabled);
        btnPhucHoi.setEnabled(enabled);
    }

    private void baoLoi(Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        lblTrangThai.setForeground(UITheme.DANGER);
        String msg = cause.getMessage() != null ? cause.getMessage() : cause.toString();
        if (msg.contains("Cannot run program") || msg.contains("error=2")) {
            msg = "Không tìm thấy mysqldump/mysql. Cài MySQL client và thêm vào PATH.";
        }
        lblTrangThai.setText("Lỗi: " + msg);
    }
}