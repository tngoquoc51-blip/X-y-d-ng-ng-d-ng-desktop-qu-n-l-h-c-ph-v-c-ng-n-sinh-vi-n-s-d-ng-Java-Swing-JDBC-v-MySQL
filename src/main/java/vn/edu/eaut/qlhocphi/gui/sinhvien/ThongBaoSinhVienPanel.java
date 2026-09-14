package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.bus.AdminConfigService;
import vn.edu.eaut.qlhocphi.bus.ThongBaoService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.AutoRefreshTimer;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.ThongBao;
import vn.edu.eaut.qlhocphi.model.ThongBaoBroadcast;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Hop thu thong bao sinh vien – giao dien kieu tin tuc / notification center.
 */
public class ThongBaoSinhVienPanel extends JPanel {

    private final TaiKhoan taiKhoan;
    private final ThongBaoService thongBaoService = new ThongBaoService();
    private final AdminConfigService adminConfigService = new AdminConfigService();
    private final Runnable onBadgeChanged;

    private final JPanel listPanel = new JPanel();
    private final JLabel lblThongKe = new JLabel(" ");
    private final JToggleButton tabTatCa = new JToggleButton("Tất cả");
    private final JToggleButton tabChuaDoc = new JToggleButton("Chưa đọc");
    private boolean chiChuaDoc = false;

    private List<ThongBaoBroadcast> cacheBroadcast = new ArrayList<>();
    private List<ThongBao> cacheThongBao = new ArrayList<>();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color BG_UNREAD = new Color(0xEF, 0xF6, 0xFF);
    private static final Color ACCENT = new Color(0x25, 0x63, 0xEB);
    private static final Color BORDER = new Color(0xE5, 0xE7, 0xEB);
    private static final Color MUTED = new Color(0x6B, 0x72, 0x80);

    public ThongBaoSinhVienPanel(TaiKhoan taiKhoan) {
        this(taiKhoan, null);
    }

    public ThongBaoSinhVienPanel(TaiKhoan taiKhoan, Runnable onBadgeChanged) {
        this.taiKhoan = taiKhoan;
        this.onBadgeChanged = onBadgeChanged;
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);

        add(buildTopBar(), BorderLayout.NORTH);

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.setBorder(new EmptyBorder(8, 0, 16, 0));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        add(scroll, BorderLayout.CENTER);

        taiDuLieu();
        AutoRefreshTimer.gan(this, 30, this::taiDuLieu);
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout(16, 0));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 4, 12, 4));

        JPanel trai = new JPanel();
        trai.setOpaque(false);
        trai.setLayout(new BoxLayout(trai, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Thông báo");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblThongKe.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblThongKe.setForeground(MUTED);
        lblThongKe.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblThongKe.setBorder(new EmptyBorder(4, 0, 0, 0));

        trai.add(title);
        trai.add(lblThongKe);

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        phai.setOpaque(false);

        ButtonGroup group = new ButtonGroup();
        styleTab(tabTatCa);
        styleTab(tabChuaDoc);
        group.add(tabTatCa);
        group.add(tabChuaDoc);
        tabTatCa.setSelected(true);
        tabTatCa.addActionListener(e -> { chiChuaDoc = false; veDanhSach(); });
        tabChuaDoc.addActionListener(e -> { chiChuaDoc = true; veDanhSach(); });

        JButton btnLamMoi = pillButton("Làm mới", false);
        btnLamMoi.addActionListener(e -> taiDuLieu());

        JButton btnDocTatCa = pillButton("Đánh dấu đã đọc hết", true);
        btnDocTatCa.addActionListener(e -> danhDauTatCa());

        phai.add(tabTatCa);
        phai.add(tabChuaDoc);
        phai.add(Box.createHorizontalStrut(8));
        phai.add(btnLamMoi);
        phai.add(btnDocTatCa);

        top.add(trai, BorderLayout.WEST);
        top.add(phai, BorderLayout.EAST);
        return top;
    }

    private void styleTab(JToggleButton b) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(96, 34));
        b.setBorder(new EmptyBorder(6, 14, 6, 14));
        capNhatMauTab(b);
        b.addChangeListener(e -> capNhatMauTab(b));
    }

    private void capNhatMauTab(JToggleButton b) {
        if (b.isSelected()) {
            b.setBackground(ACCENT);
            b.setForeground(Color.WHITE);
            b.setOpaque(true);
        } else {
            b.setBackground(new Color(0xF3, 0xF4, 0xF6));
            b.setForeground(UITheme.TEXT_PRIMARY);
            b.setOpaque(true);
        }
    }

    private JButton pillButton(String text, boolean primary) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(primary ? 170 : 100, 34));
        if (primary) {
            b.setBackground(ACCENT);
            b.setForeground(Color.WHITE);
        } else {
            b.setBackground(new Color(0xF3, 0xF4, 0xF6));
            b.setForeground(UITheme.TEXT_PRIMARY);
        }
        b.setOpaque(true);
        return b;
    }

    public void taiDuLieu() {
        new SwingWorker<Void, Void>() {
            List<ThongBaoBroadcast> broadcasts = new ArrayList<>();
            List<ThongBao> thongBaos = new ArrayList<>();

            @Override
            protected Void doInBackground() throws Exception {
                broadcasts = adminConfigService.layBroadcastDangHieuLuc();
                thongBaos = thongBaoService.layTatCaChoSinhVien(taiKhoan);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    cacheBroadcast = broadcasts != null ? broadcasts : new ArrayList<>();
                    cacheThongBao = thongBaos != null ? thongBaos : new ArrayList<>();
                    int chuaDoc = 0;
                    for (ThongBao tb : cacheThongBao) if (!tb.isDaDoc()) chuaDoc++;
                    lblThongKe.setText(String.format(
                            "%d thông báo toàn trường · %d thông báo · %d chưa đọc",
                            cacheBroadcast.size(), cacheThongBao.size(), chuaDoc));
                    veDanhSach();
                    if (onBadgeChanged != null) onBadgeChanged.run();
                } catch (Exception ex) {
                    listPanel.removeAll();
                    listPanel.add(emptyState("Không tải được thông báo.\n" + ex.getMessage()));
                    listPanel.revalidate();
                    listPanel.repaint();
                }
            }
        }.execute();
    }

    private void veDanhSach() {
        listPanel.removeAll();
        boolean coDuLieu = false;

        if (!cacheBroadcast.isEmpty() && !chiChuaDoc) {
            listPanel.add(sectionHeader("THÔNG BÁO TOÀN TRƯỜNG"));
            for (ThongBaoBroadcast b : cacheBroadcast) {
                listPanel.add(rowBroadcast(b));
                listPanel.add(Box.createVerticalStrut(2));
                coDuLieu = true;
            }
            listPanel.add(Box.createVerticalStrut(10));
        }

        List<ThongBao> hienThi = new ArrayList<>();
        for (ThongBao tb : cacheThongBao) {
            if (!chiChuaDoc || !tb.isDaDoc()) hienThi.add(tb);
        }

        if (!hienThi.isEmpty()) {
            listPanel.add(sectionHeader(chiChuaDoc ? "CHƯA ĐỌC" : "THÔNG BÁO CỦA BẠN"));
            for (ThongBao tb : hienThi) {
                listPanel.add(rowThongBao(tb));
                listPanel.add(Box.createVerticalStrut(2));
                coDuLieu = true;
            }
        }

        if (!coDuLieu) {
            listPanel.add(emptyState(chiChuaDoc
                    ? "Không còn thông báo chưa đọc."
                    : "Chưa có thông báo nào.\nKhi nhà trường gửi, bạn sẽ thấy tại đây."));
        }

        listPanel.add(Box.createVerticalGlue());
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JLabel sectionHeader(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(MUTED);
        l.setBorder(new EmptyBorder(8, 8, 6, 8));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return l;
    }

    private JPanel rowThongBao(ThongBao tb) {
        boolean unread = !tb.isDaDoc();

        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        row.setBackground(unread ? BG_UNREAD : Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(12, 14, 12, 14)
        ));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel leftDot = new JPanel(new GridBagLayout());
        leftDot.setOpaque(false);
        leftDot.setPreferredSize(new Dimension(14, 40));
        if (unread) {
            JComponent dot = new JComponent() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(ACCENT);
                    g2.fillOval(2, (getHeight() - 8) / 2, 8, 8);
                    g2.dispose();
                }
            };
            dot.setPreferredSize(new Dimension(12, 40));
            leftDot.add(dot);
        }
        row.add(leftDot, BorderLayout.WEST);

        JPanel mid = new JPanel();
        mid.setOpaque(false);
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));

        JLabel tieuDe = new JLabel(safe(tb.getTieuDe(), "(Không tiêu đề)"));
        tieuDe.setFont(new Font("Segoe UI", unread ? Font.BOLD : Font.PLAIN, 14));
        tieuDe.setForeground(UITheme.TEXT_PRIMARY);
        tieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);

        String preview = safe(tb.getNoiDung(), "");
        if (preview.length() > 120) preview = preview.substring(0, 117) + "...";
        JLabel noiDung = new JLabel(preview);
        noiDung.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noiDung.setForeground(MUTED);
        noiDung.setAlignmentX(Component.LEFT_ALIGNMENT);
        noiDung.setBorder(new EmptyBorder(3, 0, 0, 0));

        mid.add(tieuDe);
        mid.add(noiDung);
        row.add(mid, BorderLayout.CENTER);

        JLabel time = new JLabel(tb.getThoiGianTao() != null ? FMT.format(tb.getThoiGianTao()) : "");
        time.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        time.setForeground(MUTED);
        time.setVerticalAlignment(SwingConstants.TOP);
        time.setBorder(new EmptyBorder(2, 8, 0, 0));
        row.add(time, BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                moChiTietThongBao(tb);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                row.setBackground(unread ? new Color(0xDB, 0xEA, 0xFE) : new Color(0xF9, 0xFA, 0xFB));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                row.setBackground(unread ? BG_UNREAD : Color.WHITE);
            }
        });

        return row;
    }

    private JPanel rowBroadcast(ThongBaoBroadcast b) {
        Color mucDoColor = switch (b.getMucDo() != null ? b.getMucDo() : "THONG_TIN") {
            case "KHAN_CAP" -> new Color(0xDC, 0x26, 0x26);
            case "CANH_BAO" -> new Color(0xD9, 0x77, 0x06);
            default -> ACCENT;
        };
        String mucDoText = switch (b.getMucDo() != null ? b.getMucDo() : "THONG_TIN") {
            case "KHAN_CAP" -> "Khẩn cấp";
            case "CANH_BAO" -> "Cảnh báo";
            default -> "Toàn trường";
        };

        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 1, 0, mucDoColor),
                new EmptyBorder(12, 14, 12, 14)
        ));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel mid = new JPanel();
        mid.setOpaque(false);
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel badge = new JLabel(" " + mucDoText + " ");
        badge.setOpaque(true);
        badge.setBackground(mucDoColor);
        badge.setForeground(Color.WHITE);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setBorder(new EmptyBorder(2, 6, 2, 6));

        JLabel tieuDe = new JLabel(safe(b.getTieuDe(), "(Không tiêu đề)"));
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tieuDe.setForeground(UITheme.TEXT_PRIMARY);

        titleRow.add(badge);
        titleRow.add(tieuDe);

        String preview = safe(b.getNoiDung(), "");
        if (preview.length() > 120) preview = preview.substring(0, 117) + "...";
        JLabel noiDung = new JLabel(preview);
        noiDung.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noiDung.setForeground(MUTED);
        noiDung.setAlignmentX(Component.LEFT_ALIGNMENT);
        noiDung.setBorder(new EmptyBorder(3, 0, 0, 0));

        mid.add(titleRow);
        mid.add(noiDung);
        row.add(mid, BorderLayout.CENTER);

        JLabel time = new JLabel(b.getHienThiTu() != null ? FMT.format(b.getHienThiTu()) : "");
        time.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        time.setForeground(MUTED);
        time.setVerticalAlignment(SwingConstants.TOP);
        row.add(time, BorderLayout.EAST);

        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                moChiTietBroadcast(b);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                row.setBackground(new Color(0xF9, 0xFA, 0xFB));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                row.setBackground(Color.WHITE);
            }
        });

        return row;
    }

    private JPanel emptyState(String msg) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(400, 200));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel("<html><div style='text-align:center;color:#9CA3AF;line-height:1.5'>"
                + msg.replace("\n", "<br>") + "</div></html>", SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        p.add(l);
        return p;
    }

    private void moChiTietThongBao(ThongBao tb) {
        if (!tb.isDaDoc()) {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    thongBaoService.danhDauDaDocMot(taiKhoan, tb.getMaThongBao());
                    return null;
                }

                @Override
                protected void done() {
                    taiDuLieu();
                }
            }.execute();
        }

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Chi tiết thông báo", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(520, 360);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(0, 0));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(20, 24, 16, 24));
        body.setBackground(Color.WHITE);

        JLabel tieuDe = new JLabel("<html>" + escape(safe(tb.getTieuDe(), "")) + "</html>");
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tieuDe.setForeground(UITheme.TEXT_PRIMARY);
        tieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel time = new JLabel(tb.getThoiGianTao() != null ? FMT.format(tb.getThoiGianTao()) : "");
        time.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        time.setForeground(MUTED);
        time.setBorder(new EmptyBorder(6, 0, 12, 0));
        time.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea noiDung = new JTextArea(safe(tb.getNoiDung(), ""));
        noiDung.setEditable(false);
        noiDung.setLineWrap(true);
        noiDung.setWrapStyleWord(true);
        noiDung.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        noiDung.setForeground(UITheme.TEXT_PRIMARY);
        noiDung.setBackground(Color.WHITE);
        noiDung.setBorder(null);
        noiDung.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(tieuDe);
        body.add(time);
        JScrollPane sp = new JScrollPane(noiDung);
        sp.setBorder(null);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(sp);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new EmptyBorder(8, 16, 12, 16));
        JButton btnDong = pillButton("Đóng", true);
        btnDong.addActionListener(e -> dlg.dispose());
        footer.add(btnDong);

        dlg.add(body, BorderLayout.CENTER);
        dlg.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void moChiTietBroadcast(ThongBaoBroadcast b) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Thông báo toàn trường", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(520, 360);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(20, 24, 16, 24));
        body.setBackground(Color.WHITE);

        JLabel tieuDe = new JLabel("<html>" + escape(safe(b.getTieuDe(), "")) + "</html>");
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);

        String meta = (b.getHienThiTu() != null ? FMT.format(b.getHienThiTu()) : "")
                + (b.getNguoiTao() != null ? "  ·  " + b.getNguoiTao() : "");
        JLabel time = new JLabel(meta);
        time.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        time.setForeground(MUTED);
        time.setBorder(new EmptyBorder(6, 0, 12, 0));
        time.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea noiDung = new JTextArea(safe(b.getNoiDung(), ""));
        noiDung.setEditable(false);
        noiDung.setLineWrap(true);
        noiDung.setWrapStyleWord(true);
        noiDung.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        noiDung.setBorder(null);
        noiDung.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(tieuDe);
        body.add(time);
        JScrollPane sp = new JScrollPane(noiDung);
        sp.setBorder(null);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(sp);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(Color.WHITE);
        JButton btnDong = pillButton("Đóng", true);
        btnDong.addActionListener(e -> dlg.dispose());
        footer.add(btnDong);

        dlg.add(body, BorderLayout.CENTER);
        dlg.add(footer, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void danhDauTatCa() {
        int chon = JOptionPane.showConfirmDialog(this,
                "Đánh dấu tất cả thông báo là đã đọc?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (chon != JOptionPane.YES_OPTION) return;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                thongBaoService.danhDauTatCaDaDocSinhVien(taiKhoan);
                return null;
            }

            @Override
            protected void done() {
                taiDuLieu();
            }
        }.execute();
    }

    private static String safe(String s, String def) {
        return s == null || s.isBlank() ? def : s;
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}