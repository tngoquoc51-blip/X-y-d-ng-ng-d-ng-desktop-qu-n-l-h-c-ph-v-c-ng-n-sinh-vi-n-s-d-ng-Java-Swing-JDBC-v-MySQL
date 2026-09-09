package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.bus.ViDienTuService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.AutoRefreshTimer;
import vn.edu.eaut.qlhocphi.model.LichSuNapVi;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Man hinh "Vi Hoc Phi Dien Tu" cho sinh vien: xem so du, nap tien, xem lich su nap. */
public class ViDienTuPanel extends JPanel {
    private final ViDienTuService viDienTuService = new ViDienTuService();
    private final TaiKhoan taiKhoan;
    private final String maSV;

    private JLabel lblSoDu;
    private DefaultTableModel tableModel;
    private static final DateTimeFormatter DINH_DANG = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ViDienTuPanel(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        this.maSV = taiKhoan.getMaSV(); // dieu chinh neu ma SV lay tu truong khac trong TaiKhoan
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        add(buildBanner(), BorderLayout.NORTH);
        add(buildLichSuCard(), BorderLayout.CENTER);

        taiDuLieu();
        AutoRefreshTimer.gan(this, 15, this::taiDuLieu);
    }

    private JPanel buildBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(new Color(0x1E, 0x3A, 0x8A));
        banner.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel trai = new JPanel();
        trai.setOpaque(false);
        trai.setLayout(new BoxLayout(trai, BoxLayout.Y_AXIS));
        JLabel lblTieuDe = new JLabel("Ví Học Phí Điện Tử");
        lblTieuDe.setFont(UITheme.FONT_TITLE);
        lblTieuDe.setForeground(Color.WHITE);
        JLabel lblGhiChu = new JLabel("Nạp tiền vào ví để hệ thống tự động trừ học phí khi đến kỳ hạn - không cần nộp tay");
        lblGhiChu.setFont(UITheme.FONT_BASE);
        lblGhiChu.setForeground(new Color(255, 255, 255, 210));
        lblSoDu = new JLabel("0 đ");
        lblSoDu.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblSoDu.setForeground(Color.WHITE);
        lblSoDu.setBorder(new EmptyBorder(10, 0, 0, 0));
        trai.add(lblTieuDe);
        trai.add(lblGhiChu);
        trai.add(lblSoDu);

        JButton btnNap = UITheme.secondaryButton("+ Nạp Tiền Vào Ví");
        btnNap.addActionListener(e -> new NapViDialog((Window) SwingUtilities.getWindowAncestor(this), maSV, this::taiDuLieu).setVisible(true));

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.CENTER));
        phai.setOpaque(false);
        phai.add(btnNap);

        banner.add(trai, BorderLayout.WEST);
        banner.add(phai, BorderLayout.EAST);
        return banner;
    }

    private JPanel buildLichSuCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)));

        JLabel lblTitle = new JLabel("Lịch Sử Nạp Tiền");
        lblTitle.setFont(UITheme.FONT_H2);
        lblTitle.setBorder(new EmptyBorder(0, 0, 12, 0));

        String[] cot = {"Ngày Nạp", "Số Tiền", "Hình Thức", "Mã Giao Dịch"};
        tableModel = new DefaultTableModel(cot, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(UITheme.FONT_BASE);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private void taiDuLieu() {
        try {
            BigDecimal soDu = viDienTuService.laySoDu(maSV);
            lblSoDu.setText(MoneyUtils.format(soDu));

            List<LichSuNapVi> lichSu = viDienTuService.layLichSuNap(maSV);
            tableModel.setRowCount(0);
            for (LichSuNapVi ls : lichSu) {
                tableModel.addRow(new Object[]{
                        ls.getNgayNap() != null ? DINH_DANG.format(ls.getNgayNap()) : "",
                        MoneyUtils.format(ls.getSoTien()),
                        ls.getHinhThuc(),
                        ls.getMaGiaoDichCong() != null ? ls.getMaGiaoDichCong() : "-"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải dữ liệu ví: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}