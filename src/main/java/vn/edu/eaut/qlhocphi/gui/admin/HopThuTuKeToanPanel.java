package vn.edu.eaut.qlhocphi.gui.admin;

import vn.edu.eaut.qlhocphi.bus.ThongBaoService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.ThongBao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Phòng Đào tạo – hộp thư nhận từ Phòng Kế toán.
 */
public class HopThuTuKeToanPanel extends JPanel {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String MAN_HINH = GuiGuiPhongDaoTaoPanel.MAN_HINH_KEY;

    private final TaiKhoan taiKhoan;
    private final ThongBaoService service = new ThongBaoService();
    private final Runnable onBadgeChange;

    private final JPanel listBox = new JPanel();
    private final JLabel lblSoLuong = new JLabel(" ");

    public HopThuTuKeToanPanel(TaiKhoan taiKhoan) {
        this(taiKhoan, null);
    }

    public HopThuTuKeToanPanel(TaiKhoan taiKhoan, Runnable onBadgeChange) {
        this.taiKhoan = taiKhoan;
        this.onBadgeChange = onBadgeChange;
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);

        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 14, 0));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Hộp thư từ Phòng Kế toán");
        t.setFont(UITheme.FONT_TITLE);
        t.setForeground(UITheme.TEXT_PRIMARY);
        JLabel s = new JLabel("Các yêu cầu, báo cáo công nợ và đề nghị liên thông nội bộ.");
        s.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        s.setForeground(UITheme.TEXT_MUTED);
        titles.add(t);
        titles.add(Box.createVerticalStrut(4));
        titles.add(s);
        top.add(titles, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        lblSoLuong.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSoLuong.setForeground(UITheme.TEXT_MUTED);
        JButton btnLamMoi = UITheme.secondaryButton("Làm mới");
        btnLamMoi.addActionListener(e -> taiDuLieu());
        JButton btnDocHet = UITheme.primaryButton("Đánh dấu đã đọc hết");
        btnDocHet.addActionListener(e -> danhDauHet());
        actions.add(lblSoLuong);
        actions.add(btnLamMoi);
        actions.add(btnDocHet);
        top.add(actions, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        listBox.setOpaque(false);
        listBox.setLayout(new BoxLayout(listBox, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(listBox);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1));
        scroll.getViewport().setBackground(UITheme.BG_MAIN);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        add(scroll, BorderLayout.CENTER);

        taiDuLieu();
    }

    private void taiDuLieu() {
        SwingWorker<List<ThongBao>, Void> w = new SwingWorker<>() {
            @Override
            protected List<ThongBao> doInBackground() throws Exception {
                return service.layHopThuTheoVaiTro(taiKhoan, MAN_HINH);
            }

            @Override
            protected void done() {
                try {
                    hienDanhSach(get());
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(HopThuTuKeToanPanel.this,
                            "Không tải được hộp thư.\n" + ex.getMessage());
                }
            }
        };
        w.execute();
    }

    private void hienDanhSach(List<ThongBao> ds) {
        listBox.removeAll();
        long chuaDoc = ds.stream().filter(t -> !t.isDaDoc()).count();
        lblSoLuong.setText(chuaDoc > 0
                ? chuaDoc + " chưa đọc / " + ds.size() + " thư"
                : ds.size() + " thư");

        if (ds.isEmpty()) {
            JPanel empty = new JPanel(new GridBagLayout());
            empty.setOpaque(false);
            empty.setPreferredSize(new Dimension(10, 200));
            JLabel e = new JLabel("Chưa có thông tin nào từ Phòng Kế toán");
            e.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            e.setForeground(UITheme.TEXT_MUTED);
            empty.add(e);
            listBox.add(empty);
        } else {
            for (ThongBao tb : ds) {
                listBox.add(taoTheThu(tb));
                listBox.add(Box.createVerticalStrut(10));
            }
        }
        listBox.revalidate();
        listBox.repaint();
        try {
            service.danhDauDaDoc(taiKhoan, MAN_HINH);
        } catch (Exception ignored) {
        }
        if (onBadgeChange != null) onBadgeChange.run();
    }

    private JPanel taoTheThu(ThongBao tb) {
        JPanel card = new JPanel(new BorderLayout(12, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                if (!tb.isDaDoc()) {
                    g2.setColor(UITheme.PRIMARY);
                    g2.fillRoundRect(0, 0, 4, getHeight() - 1, 4, 4);
                }
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tieuDe = new JLabel(tb.getTieuDe() != null ? tb.getTieuDe() : "(Không tiêu đề)");
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tieuDe.setForeground(UITheme.TEXT_PRIMARY);

        String tg = tb.getThoiGianTao() != null ? tb.getThoiGianTao().format(FMT) : "";
        JLabel meta = new JLabel((tb.isDaDoc() ? "Đã đọc" : "Chưa đọc") + "  ·  " + tg);
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        meta.setForeground(tb.isDaDoc() ? UITheme.TEXT_MUTED : UITheme.PRIMARY);

        JTextArea nd = new JTextArea(tb.getNoiDung() != null ? tb.getNoiDung() : "");
        nd.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nd.setForeground(UITheme.TEXT_PRIMARY);
        nd.setOpaque(false);
        nd.setEditable(false);
        nd.setLineWrap(true);
        nd.setWrapStyleWord(true);
        nd.setBorder(null);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(tieuDe, BorderLayout.CENTER);
        north.add(meta, BorderLayout.EAST);

        card.add(north, BorderLayout.NORTH);
        card.add(nd, BorderLayout.CENTER);
        return card;
    }

    private void danhDauHet() {
        try {
            service.danhDauDaDoc(taiKhoan, MAN_HINH);
            taiDuLieu();
            if (onBadgeChange != null) onBadgeChange.run();
        } catch (Exception ex) {
            UIUtils.thongBaoLoi(this, ex.getMessage());
        }
    }
}