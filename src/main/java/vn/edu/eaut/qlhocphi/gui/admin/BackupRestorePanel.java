package vn.edu.eaut.qlhocphi.gui.admin;

import vn.edu.eaut.qlhocphi.config.AppConfig;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.util.BackupService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Man hinh sao luu / phuc hoi CSDL - ban "desktop quan ly" day du: banner
 * dong bo mau, the thong tin ket noi CSDL hien tai, bat/tat tu dong sao luu
 * dinh ky (javax.swing.Timer), va lich su thao tac trong phien lam viec.
 * Chi danh cho vai tro ADMIN. Cac thao tac goi tien trinh ngoai (mysqldump/
 * mysql) deu chay qua SwingWorker de khong lam treo giao dien.
 */
public class BackupRestorePanel extends JPanel {
    private static final DateTimeFormatter DINH_DANG_TEN_FILE = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter DINH_DANG_HIEN_THI = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final int CHU_KY_TU_DONG_PHUT = 15;

    private final BackupService backupService = new BackupService();

    private JLabel lblTrangThai;
    private JButton btnSaoLuu, btnPhucHoi;
    private JToggleButton btnTuDongSaoLuu;
    private JLabel lblThuMucTuDong;
    private JLabel lblTinhTrangTuDong;

    private JPanel khoiLichSu;
    private final List<String[]> lichSuThaoTac = new ArrayList<>(); // [loai, gio, duongDan, trangThai]

    private File thuMucTuDongSaoLuu;
    private Timer timerTuDong;

    public BackupRestorePanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        JPanel giua = new JPanel();
        giua.setOpaque(false);
        giua.setLayout(new BoxLayout(giua, BoxLayout.Y_AXIS));
        giua.add(buildHeader());
        giua.add(Box.createRigidArea(new Dimension(0, 16)));
        giua.add(buildThongTinKetNoiRow());
        giua.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel haiCot = new JPanel(new GridLayout(1, 2, 16, 0));
        haiCot.setOpaque(false);
        haiCot.setAlignmentX(Component.LEFT_ALIGNMENT);
        haiCot.add(buildBackupCard());
        haiCot.add(buildRestoreCard());
        giua.add(haiCot);
        giua.add(Box.createRigidArea(new Dimension(0, 16)));

        lblTrangThai = new JLabel(" ");
        lblTrangThai.setFont(UITheme.FONT_BASE);
        lblTrangThai.setAlignmentX(Component.LEFT_ALIGNMENT);
        giua.add(lblTrangThai);
        giua.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel lichSuCard = buildLichSuCard();
        lichSuCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        giua.add(lichSuCard);

        JScrollPane scroll = new JScrollPane(bocNgoai(giua));
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    /** Bam sat chieu rong khung cuon, khong de trong khoang trang lech ben phai. */
    private JPanel bocNgoai(JPanel noiDung) {
        JPanel wrap = new KhungCuonToanChieuRong(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(noiDung, BorderLayout.NORTH);
        return wrap;
    }

    private static class KhungCuonToanChieuRong extends JPanel implements Scrollable {
        KhungCuonToanChieuRong(LayoutManager lm) { super(lm); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int huong, int dir) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int huong, int dir) { return 120; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    // ================== HEADER (banner + logo) ==================

    private JPanel buildHeader() {
        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout(14, 0));
        banner.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        banner.setPreferredSize(new Dimension(10, 90));

        JPanel trai = new JPanel(new BorderLayout(14, 0));
        trai.setOpaque(false);
        trai.add(logoBadge(), BorderLayout.WEST);

        JPanel chuText = new JPanel();
        chuText.setOpaque(false);
        chuText.setLayout(new BoxLayout(chuText, BoxLayout.Y_AXIS));
        JLabel tieuDe = new JLabel("Sao luu & Phuc hoi CSDL");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        JLabel phu = new JLabel("Bao ve du lieu he thong: sao luu thu cong, tu dong dinh ky va phuc hoi khan cap");
        phu.setFont(UITheme.FONT_BASE);
        phu.setForeground(new Color(255, 255, 255, 210));
        chuText.add(tieuDe);
        chuText.add(Box.createRigidArea(new Dimension(0, 4)));
        chuText.add(phu);
        trai.add(chuText, BorderLayout.CENTER);
        banner.add(trai, BorderLayout.WEST);

        return banner;
    }

    private JComponent logoBadge() {
        JComponent badge = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 55));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
                FontMetrics fm = g2.getFontMetrics();
                String icon = "\uD83D\uDDC4";
                int x = (getWidth() - fm.stringWidth(icon)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(icon, x, y);
                g2.dispose();
            }
        };
        badge.setPreferredSize(new Dimension(52, 52));
        badge.setOpaque(false);
        return badge;
    }

    // ================== THONG TIN KET NOI CSDL ==================

    private JPanel buildThongTinKetNoiRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);

        String[] tt = tachThongTinKetNoi();
        row.add(thongKeCard("May chu CSDL", tt[0] + ":" + tt[1], UITheme.PRIMARY));
        row.add(thongKeCard("Ten co so du lieu", tt[2], UITheme.TEXT_VIOLET));

        lblTinhTrangTuDong = new JLabel("Dang tat");
        row.add(thongKeCardVoiNhan("Tu dong sao luu", lblTinhTrangTuDong, UITheme.WARNING));

        return row;
    }

    private JPanel thongKeCard(String tieuDe, String giaTri, Color mauNhan) {
        JLabel lbl = new JLabel(giaTri);
        return thongKeCardVoiNhan(tieuDe, lbl, mauNhan);
    }

    private JPanel thongKeCardVoiNhan(String tieuDe, JLabel giaTri, Color mauNhan) {
        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel l1 = new JLabel(tieuDe);
        l1.setFont(UITheme.FONT_BASE);
        l1.setForeground(UITheme.TEXT_MUTED);
        l1.setAlignmentX(Component.LEFT_ALIGNMENT);
        giaTri.setFont(new Font("Segoe UI", Font.BOLD, 17));
        giaTri.setForeground(mauNhan);
        giaTri.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(l1);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(giaTri);
        return card;
    }

    /** Tach host/port/database tu chuoi db.url - chi de HIEN THI, khong dung de ket noi (BackupService tu lam viec nay). */
    private String[] tachThongTinKetNoi() {
        String url = AppConfig.get("db.url");
        String host = "localhost", port = "3306", db = "qlhocphi";
        if (url != null) {
            Pattern p = Pattern.compile("jdbc:mysql://([^:/]+)(:(\\d+))?/([^?]+)");
            Matcher m = p.matcher(url);
            if (m.find()) {
                host = m.group(1);
                if (m.group(3) != null) port = m.group(3);
                db = m.group(4);
            }
        }
        return new String[]{host, port, db};
    }

    // ================== SAO LUU (thu cong + tu dong dinh ky) ==================

    private JPanel buildBackupCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(UITheme.sectionLabel("Sao luu du lieu"));
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel mota = new JLabel("<html>Xuat toan bo CSDL ra 1 file .sql. "
                + "Nen sao luu dinh ky (VD: hang tuan) va luu tru o noi an toan, tach biet may chu.</html>");
        mota.setFont(UITheme.FONT_BASE);
        mota.setForeground(UITheme.TEXT_MUTED);
        mota.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(mota);
        card.add(Box.createRigidArea(new Dimension(0, 16)));

        btnSaoLuu = UITheme.primaryButton("Sao luu ngay");
        btnSaoLuu.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSaoLuu.addActionListener(e -> thucHienSaoLuu());
        card.add(btnSaoLuu);

        card.add(Box.createRigidArea(new Dimension(0, 16)));
        card.add(new JSeparator());
        card.add(Box.createRigidArea(new Dimension(0, 16)));

        JLabel tieuDeTuDong = new JLabel("Tu dong sao luu dinh ky (" + CHU_KY_TU_DONG_PHUT + " phut/lan)");
        tieuDeTuDong.setFont(UITheme.FONT_BOLD);
        tieuDeTuDong.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(tieuDeTuDong);
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        lblThuMucTuDong = new JLabel("Chua chon thu muc luu tu dong");
        lblThuMucTuDong.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblThuMucTuDong.setForeground(UITheme.TEXT_MUTED);
        lblThuMucTuDong.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblThuMucTuDong);
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        btnTuDongSaoLuu = new JToggleButton("Bat tu dong sao luu");
        btnTuDongSaoLuu.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnTuDongSaoLuu.setFont(UITheme.FONT_BASE);
        btnTuDongSaoLuu.addActionListener(e -> chuyenDoiTuDongSaoLuu());
        card.add(btnTuDongSaoLuu);

        return card;
    }

    private void chuyenDoiTuDongSaoLuu() {
        if (btnTuDongSaoLuu.isSelected()) {
            if (thuMucTuDongSaoLuu == null) {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Chon thu muc de luu file sao luu tu dong");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
                    btnTuDongSaoLuu.setSelected(false);
                    return;
                }
                thuMucTuDongSaoLuu = chooser.getSelectedFile();
            }
            lblThuMucTuDong.setText("Luu vao: " + thuMucTuDongSaoLuu.getAbsolutePath());
            lblTinhTrangTuDong.setText("Dang bat");
            lblTinhTrangTuDong.setForeground(UITheme.SUCCESS);
            btnTuDongSaoLuu.setText("Tat tu dong sao luu");

            timerTuDong = new Timer(CHU_KY_TU_DONG_PHUT * 60 * 1000, e -> thucHienSaoLuuTuDong());
            timerTuDong.start();
        } else {
            if (timerTuDong != null) timerTuDong.stop();
            lblTinhTrangTuDong.setText("Dang tat");
            lblTinhTrangTuDong.setForeground(UITheme.WARNING);
            btnTuDongSaoLuu.setText("Bat tu dong sao luu");
        }
    }

    private void thucHienSaoLuuTuDong() {
        File fileDich = new File(thuMucTuDongSaoLuu,
                "auto_backup_" + LocalDateTime.now().format(DINH_DANG_TEN_FILE) + ".sql");

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
                    themLichSu("Sao luu tu dong", fileDich.getAbsolutePath(), true);
                    lblTrangThai.setForeground(UITheme.SUCCESS);
                    lblTrangThai.setText("Sao luu tu dong thanh cong: " + fileDich.getName());
                } catch (Exception ex) {
                    themLichSu("Sao luu tu dong", fileDich.getAbsolutePath(), false);
                    baoLoi(ex);
                }
            }
        };
        worker.execute();
    }

    // ================== PHUC HOI ==================

    private JPanel buildRestoreCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(UITheme.sectionLabel("Phuc hoi du lieu"));
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel mota = new JLabel("<html><b>Canh bao:</b> phuc hoi se GHI DE toan bo du lieu hien tai "
                + "bang noi dung trong file .sql duoc chon. Hay chac chan ban da sao luu du lieu hien tai truoc.</html>");
        mota.setFont(UITheme.FONT_BASE);
        mota.setForeground(UITheme.DANGER);
        mota.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(mota);
        card.add(Box.createRigidArea(new Dimension(0, 16)));

        btnPhucHoi = UITheme.dangerButton("Chon file va phuc hoi");
        btnPhucHoi.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPhucHoi.addActionListener(e -> thucHienPhucHoi());
        card.add(btnPhucHoi);

        return card;
    }

    // ================== LICH SU THAO TAC (trong phien lam viec) ==================

    private JPanel buildLichSuCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.add(UITheme.sectionLabel("Lich su thao tac (trong phien lam viec nay)"), BorderLayout.NORTH);

        khoiLichSu = new JPanel();
        khoiLichSu.setOpaque(false);
        khoiLichSu.setLayout(new BoxLayout(khoiLichSu, BoxLayout.Y_AXIS));
        khoiLichSu.add(dongTrongLichSu());
        card.add(khoiLichSu, BorderLayout.CENTER);

        return card;
    }

    private JLabel dongTrongLichSu() {
        JLabel trong = new JLabel("Chua co thao tac nao trong phien lam viec nay");
        trong.setFont(UITheme.FONT_BASE);
        trong.setForeground(UITheme.TEXT_MUTED);
        return trong;
    }

    private void themLichSu(String loai, String duongDan, boolean thanhCong) {
        lichSuThaoTac.add(0, new String[]{loai, LocalDateTime.now().format(DINH_DANG_HIEN_THI), duongDan,
                thanhCong ? "Thanh cong" : "That bai"});

        khoiLichSu.removeAll();
        for (String[] dong : lichSuThaoTac) {
            khoiLichSu.add(dongLichSu(dong[0], dong[1], dong[2], dong[3]));
        }
        khoiLichSu.revalidate();
        khoiLichSu.repaint();
    }

    private JPanel dongLichSu(String loai, String gio, String duongDan, String trangThai) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF0, 0xF2, 0xF6)),
                BorderFactory.createEmptyBorder(10, 4, 10, 4)));

        JPanel trai = new JPanel();
        trai.setOpaque(false);
        trai.setLayout(new BoxLayout(trai, BoxLayout.Y_AXIS));
        JLabel lblLoai = new JLabel(loai + "  -  " + gio);
        lblLoai.setFont(UITheme.FONT_BOLD);
        lblLoai.setForeground(UITheme.TEXT_PRIMARY);
        JLabel lblDuongDan = new JLabel(duongDan);
        lblDuongDan.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDuongDan.setForeground(UITheme.TEXT_MUTED);
        trai.add(lblLoai);
        trai.add(lblDuongDan);
        row.add(trai, BorderLayout.CENTER);

        boolean ok = "Thanh cong".equals(trangThai);
        JLabel pill = UITheme.pill(trangThai, ok ? UITheme.TINT_GREEN : new Color(0xFC, 0xE4, 0xE4),
                ok ? UITheme.TEXT_GREEN : UITheme.DANGER);
        JPanel phaiWrap = new JPanel(new GridBagLayout());
        phaiWrap.setOpaque(false);
        phaiWrap.add(pill);
        row.add(phaiWrap, BorderLayout.EAST);

        return row;
    }

    // ================== THUC HIEN SAO LUU / PHUC HOI (thu cong) ==================

    private void thucHienSaoLuu() {
        JFileChooser chooser = new JFileChooser();
        String tenFileGoiY = "backup_qlhocphi_" + LocalDateTime.now().format(DINH_DANG_TEN_FILE) + ".sql";
        chooser.setSelectedFile(new File(tenFileGoiY));
        int ketQua = chooser.showSaveDialog(this);
        if (ketQua != JFileChooser.APPROVE_OPTION) return;
        File fileDich = chooser.getSelectedFile();

        capNhatTrangThaiDangXuLy("Dang sao luu du lieu...");
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
                    themLichSu("Sao luu thu cong", fileDich.getAbsolutePath(), true);
                    lblTrangThai.setForeground(UITheme.SUCCESS);
                    lblTrangThai.setText("Sao luu thanh cong: " + fileDich.getAbsolutePath());
                } catch (Exception ex) {
                    themLichSu("Sao luu thu cong", fileDich.getAbsolutePath(), false);
                    baoLoi(ex);
                }
            }
        };
        worker.execute();
    }

    private void thucHienPhucHoi() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("SQL files (*.sql)", "sql"));
        int ketQua = chooser.showOpenDialog(this);
        if (ketQua != JFileChooser.APPROVE_OPTION) return;
        File fileNguon = chooser.getSelectedFile();

        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Ban chac chan muon GHI DE toan bo du lieu hien tai bang file:\n" + fileNguon.getName() + " ?",
                "Xac nhan phuc hoi", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (xacNhan != JOptionPane.YES_OPTION) return;

        capNhatTrangThaiDangXuLy("Dang phuc hoi du lieu...");
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
                    themLichSu("Phuc hoi du lieu", fileNguon.getAbsolutePath(), true);
                    lblTrangThai.setForeground(UITheme.SUCCESS);
                    lblTrangThai.setText("Phuc hoi thanh cong tu file: " + fileNguon.getName()
                            + ". Vui long khoi dong lai ung dung de dam bao du lieu duoc lam moi.");
                } catch (Exception ex) {
                    themLichSu("Phuc hoi du lieu", fileNguon.getAbsolutePath(), false);
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
            msg = "Khong tim thay lenh mysqldump/mysql. Vui long cai MySQL client va them vao PATH he thong.";
        }
        lblTrangThai.setText("Loi: " + msg);
    }
}