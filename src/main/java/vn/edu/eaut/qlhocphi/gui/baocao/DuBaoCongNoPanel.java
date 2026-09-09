package vn.edu.eaut.qlhocphi.gui.baocao;

import vn.edu.eaut.qlhocphi.bus.DuBaoCongNoService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.RuiRoSinhVien;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * "Dự báo công nợ bằng AI" - phân tích lịch sử thanh toán của toàn trường,
 * xếp hạng sinh viên theo mức độ rủi ro trễ hạn kỳ tới (Cao/Trung bình/Thấp),
 * giúp Kế toán/Admin chủ động nhắc nợ SỚM cho nhóm nguy cơ cao thay vì đợi
 * đến khi quá hạn mới phát hiện (khác với màn hình Công Nợ hiện tại chỉ nhìn
 * được trạng thái HIỆN TẠI, không dự báo được TƯƠNG LAI).
 */
public class DuBaoCongNoPanel extends JPanel {
    private final DuBaoCongNoService duBaoService = new DuBaoCongNoService();
    private final java.util.function.BiConsumer<String, String> dieuHuongTimKiem;

    private JLabel lblTongSV, lblRuiRoCao, lblRuiRoTrungBinh;
    private DefaultTableModel tableModel;
    private JTable table;

    public DuBaoCongNoPanel(java.util.function.BiConsumer<String, String> dieuHuongTimKiem) {
        this.dieuHuongTimKiem = dieuHuongTimKiem;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(buildHeader());
        north.add(Box.createRigidArea(new Dimension(0, 16)));
        north.add(buildKpiRow());
        add(north, BorderLayout.NORTH);

        add(buildTableCard(), BorderLayout.CENTER);

        taiDuLieu();
    }

    private JPanel buildHeader() {
        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout(14, 0));
        banner.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        banner.setPreferredSize(new Dimension(10, 90));

        JPanel trai = new JPanel(new BorderLayout(14, 0));
        trai.setOpaque(false);
        JComponent logo = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 55));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
                FontMetrics fm = g2.getFontMetrics();
                String icon = "\uD83E\uDDE0";
                g2.drawString(icon, (getWidth() - fm.stringWidth(icon)) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        logo.setPreferredSize(new Dimension(52, 52));
        logo.setOpaque(false);
        trai.add(logo, BorderLayout.WEST);

        JPanel chuText = new JPanel();
        chuText.setOpaque(false);
        chuText.setLayout(new BoxLayout(chuText, BoxLayout.Y_AXIS));
        JLabel tieuDe = new JLabel("Dự Báo Công Nợ Bằng AI");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        JLabel phu = new JLabel("Phân tích lịch sử thanh toán, xếp hạng sinh viên có nguy cơ trễ hạn kỳ tới");
        phu.setFont(UITheme.FONT_BASE);
        phu.setForeground(new Color(255, 255, 255, 210));
        chuText.add(tieuDe);
        chuText.add(Box.createRigidArea(new Dimension(0, 4)));
        chuText.add(phu);
        trai.add(chuText, BorderLayout.CENTER);
        banner.add(trai, BorderLayout.WEST);

        JButton btnLamMoi = new JButton("Phân Tích Lại");
        btnLamMoi.setFont(UITheme.FONT_BOLD);
        btnLamMoi.setBackground(Color.WHITE);
        btnLamMoi.setForeground(UITheme.PRIMARY_DARK);
        btnLamMoi.setFocusPainted(false);
        btnLamMoi.setBorderPainted(false);
        btnLamMoi.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnLamMoi.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLamMoi.addActionListener(e -> taiDuLieu());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(btnLamMoi);
        banner.add(actions, BorderLayout.EAST);

        return banner;
    }

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);
        lblTongSV = new JLabel("0");
        lblRuiRoCao = new JLabel("0");
        lblRuiRoTrungBinh = new JLabel("0");
        row.add(thongKeCard("Tổng SV Được Phân Tích", lblTongSV, UITheme.PRIMARY));
        row.add(thongKeCard("Nguy Cơ CAO - Cần Nhắc Nợ Sớm", lblRuiRoCao, UITheme.DANGER));
        row.add(thongKeCard("Nguy Cơ Trung Bình", lblRuiRoTrungBinh, UITheme.WARNING));
        return row;
    }

    private JPanel thongKeCard(String tieuDe, JLabel giaTri, Color mauNhan) {
        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel l1 = new JLabel(tieuDe);
        l1.setFont(UITheme.FONT_BASE);
        l1.setForeground(UITheme.TEXT_MUTED);
        l1.setAlignmentX(Component.LEFT_ALIGNMENT);
        giaTri.setFont(new Font("Segoe UI", Font.BOLD, 22));
        giaTri.setForeground(mauNhan);
        giaTri.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(l1);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(giaTri);
        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.add(UITheme.sectionLabel("Xếp Hạng Rủi Ro Trễ Hạn (Dựa Trên Lịch Sử Thanh Toán Thực Tế)"), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{
                "Mã SV", "Họ Tên", "Tổng Hóa Đơn Có Hạn", "Số Lần Trễ (Lịch Sử)", "Tỷ Lệ Trễ", "Số Ngày Đang Trễ", "Mức Độ Rủi Ro"
        }, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.styleTable(table);
        table.getColumnModel().getColumn(6).setCellRenderer(rrRenderer());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0 && dieuHuongTimKiem != null) {
                    String maSV = (String) tableModel.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 0);
                    dieuHuongTimKiem.accept("congno", maSV);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        JLabel ghiChu = new JLabel("Nhấp đôi (double-click) 1 dòng để xem chi tiết công nợ của sinh viên đó.");
        ghiChu.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ghiChu.setForeground(UITheme.TEXT_MUTED);
        card.add(ghiChu, BorderLayout.SOUTH);

        return card;
    }

    private DefaultTableCellRenderer rrRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(UITheme.FONT_BOLD);
                label.setOpaque(true);
                label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                String text = value == null ? "" : value.toString();
                Color bg, fg;
                switch (text) {
                    case "Nguy cơ CAO" -> { bg = UITheme.TINT_RED; fg = UITheme.TEXT_RED; }
                    case "Trung bình" -> { bg = new Color(0xFD, 0xF3, 0xDA); fg = UITheme.WARNING; }
                    case "Thấp" -> { bg = UITheme.TINT_GREEN; fg = UITheme.TEXT_GREEN; }
                    default -> { bg = new Color(0xEC, 0xEE, 0xF2); fg = UITheme.TEXT_MUTED; }
                }
                if (!isSelected) { label.setBackground(bg); label.setForeground(fg); }
                return label;
            }
        };
    }

    private String nhanMucDo(String ma) {
        return switch (ma) {
            case "CAO" -> "Nguy cơ CAO";
            case "TRUNG_BINH" -> "Trung bình";
            case "THAP" -> "Thấp";
            default -> "Chưa đủ dữ liệu";
        };
    }

    private void taiDuLieu() {
        SwingWorker<List<RuiRoSinhVien>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<RuiRoSinhVien> doInBackground() throws Exception {
                return duBaoService.phanTichToanBo();
            }

            @Override
            protected void done() {
                try {
                    List<RuiRoSinhVien> list = get();
                    tableModel.setRowCount(0);
                    int soCao = 0, soTrungBinh = 0;
                    for (RuiRoSinhVien rr : list) {
                        tableModel.addRow(new Object[]{
                                rr.getMaSV(), rr.getTenSV(), rr.getTongHoaDon(), rr.getSoLanTre(),
                                rr.getTyLeTre() + "%",
                                rr.getSoNgayTreDangNo() > 0 ? rr.getSoNgayTreDangNo() + " ngày" : "-",
                                nhanMucDo(rr.getMucDoRuiRo())
                        });
                        if ("CAO".equals(rr.getMucDoRuiRo())) soCao++;
                        else if ("TRUNG_BINH".equals(rr.getMucDoRuiRo())) soTrungBinh++;
                    }
                    lblTongSV.setText(String.valueOf(list.size()));
                    lblRuiRoCao.setText(String.valueOf(soCao));
                    lblRuiRoTrungBinh.setText(String.valueOf(soTrungBinh));
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(DuBaoCongNoPanel.this, "Không thể phân tích dự báo công nợ.\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private String rootMessage(Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }
}