package vn.edu.eaut.qlhocphi.gui.admin;

import vn.edu.eaut.qlhocphi.bus.AdminConfigService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.SchedulerTrangThai;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Giám sát 3 scheduler: Thu tự động, Nhắc nợ, Backup. */
public class SchedulerHealthPanel extends JPanel {
    private final AdminConfigService service = new AdminConfigService();
    private final DefaultTableModel model;
    private final JTable table;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public SchedulerHealthPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(4, 4, 4, 4));

        JLabel title = new JLabel("Giám sát tiến trình nền (Scheduler Health)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UITheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Trạng thái Thu tự động · Nhắc nợ · Sao lưu — lần chạy gần nhất & kết quả");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(UITheme.TEXT_MUTED);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(title);
        text.add(Box.createRigidArea(new Dimension(0, 4)));
        text.add(sub);
        head.add(text, BorderLayout.WEST);
        JButton btnRefresh = UITheme.primaryButton("Làm mới");
        btnRefresh.addActionListener(e -> taiDuLieu());
        head.add(btnRefresh, BorderLayout.EAST);
        add(head, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Mã", "Tên tiến trình", "Trạng thái", "Lần chạy gần nhất", "Kết quả gần nhất"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(40);
        table.setFont(UITheme.FONT_BASE);
        table.getTableHeader().setFont(UITheme.FONT_BOLD);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                lbl.setHorizontalAlignment(CENTER);
                lbl.setOpaque(true);
                String s = String.valueOf(v);
                if ("DANG_CHAY".equals(s)) {
                    lbl.setText("Đang chạy");
                    lbl.setBackground(UITheme.TINT_GREEN);
                    lbl.setForeground(UITheme.TEXT_GREEN);
                } else if ("LOI".equals(s)) {
                    lbl.setText("Lỗi");
                    lbl.setBackground(UITheme.TINT_RED);
                    lbl.setForeground(UITheme.TEXT_RED);
                } else {
                    lbl.setText("Dừng");
                    lbl.setBackground(new Color(0xF1, 0xF5, 0xF9));
                    lbl.setForeground(UITheme.TEXT_MUTED);
                }
                if (sel) { lbl.setBackground(UITheme.SIDEBAR_ACTIVE); }
                return lbl;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        add(scroll, BorderLayout.CENTER);

        JLabel note = new JLabel("Ghi chú: Scheduler ghi nhận trạng thái khi chạy. Có thể tích hợp ghi log từ NhacNoTuDongScheduler / TuDongThuHocPhiScheduler / BackupService.");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setForeground(UITheme.TEXT_MUTED);
        add(note, BorderLayout.SOUTH);

        taiDuLieu();
    }

    private void taiDuLieu() {
        SwingWorker<List<SchedulerTrangThai>, Void> w = new SwingWorker<>() {
            @Override protected List<SchedulerTrangThai> doInBackground() throws Exception {
                return service.layTrangThaiScheduler();
            }
            @Override protected void done() {
                try {
                    model.setRowCount(0);
                    for (SchedulerTrangThai s : get()) {
                        model.addRow(new Object[]{
                                s.getMaScheduler(),
                                s.getTenHienThi(),
                                s.getTrangThai(),
                                s.getLanChayGanNhat() == null ? "—" : s.getLanChayGanNhat().format(FMT),
                                s.getKetQuaGanNhat() == null ? "—" : s.getKetQuaGanNhat()
                        });
                    }
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(SchedulerHealthPanel.this, "Không tải được: " + ex.getMessage());
                }
            }
        };
        w.execute();
    }
}