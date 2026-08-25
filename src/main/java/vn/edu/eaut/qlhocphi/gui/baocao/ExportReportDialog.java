package vn.edu.eaut.qlhocphi.gui.baocao;

import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.dal.HoaDonDAO;
import vn.edu.eaut.qlhocphi.dal.SinhVienDAO;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.SinhVien;
import vn.edu.eaut.qlhocphi.util.ExcelExporter;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;
import vn.edu.eaut.qlhocphi.util.PDFExporter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Hop thoai xuat bao cao ra file Excel hoac PDF.
 * Cho phep chon loai bao cao (Sinh vien / Hoa don / Cong no) va dinh dang xuat.
 */
public class ExportReportDialog extends JDialog {
    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final SinhVienDAO sinhVienDAO = new SinhVienDAO();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();

    private final JComboBox<String> cboLoaiBaoCao;
    private final JRadioButton rdoExcel;
    private final JRadioButton rdoPdf;
    private final JLabel lblTrangThai;

    public ExportReportDialog(Frame owner) {
        super(owner, "Xuat bao cao", true);
        setSize(420, 260);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        root.setBackground(UITheme.BG_MAIN);
        setContentPane(root);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JLabel lblLoai = UIUtils.formLabel("Loai bao cao");
        lblLoai.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblLoai);
        form.add(Box.createVerticalStrut(6));

        cboLoaiBaoCao = new JComboBox<>(new String[]{
                "Danh sach sinh vien",
                "Danh sach hoa don hoc phi",
                "Danh sach cong no con lai"
        });
        cboLoaiBaoCao.setAlignmentX(Component.LEFT_ALIGNMENT);
        cboLoaiBaoCao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        form.add(cboLoaiBaoCao);
        form.add(Box.createVerticalStrut(16));

        JLabel lblDinhDang = UIUtils.formLabel("Dinh dang xuat");
        lblDinhDang.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblDinhDang);
        form.add(Box.createVerticalStrut(6));

        rdoExcel = new JRadioButton("Excel (.xlsx)", true);
        rdoPdf = new JRadioButton("PDF (.pdf)");
        rdoExcel.setOpaque(false);
        rdoPdf.setOpaque(false);
        rdoExcel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rdoPdf.setAlignmentX(Component.LEFT_ALIGNMENT);
        ButtonGroup group = new ButtonGroup();
        group.add(rdoExcel);
        group.add(rdoPdf);
        form.add(rdoExcel);
        form.add(rdoPdf);

        root.add(form, BorderLayout.CENTER);

        lblTrangThai = new JLabel(" ");
        lblTrangThai.setFont(UITheme.FONT_BASE);
        lblTrangThai.setForeground(UITheme.TEXT_MUTED);

        JPanel duoi = new JPanel(new BorderLayout());
        duoi.setOpaque(false);
        duoi.add(lblTrangThai, BorderLayout.WEST);

        JButton btnXuat = UITheme.primaryButton("Xuat file");
        btnXuat.addActionListener(e -> chonFileVaXuat());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(btnXuat);
        duoi.add(actions, BorderLayout.EAST);

        root.add(duoi, BorderLayout.SOUTH);
    }

    private void chonFileVaXuat() {
        boolean dungExcel = rdoExcel.isSelected();
        String duoiFile = dungExcel ? "xlsx" : "pdf";
        String tenGoiY = tenFileGoiY() + "." + duoiFile;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(tenGoiY));
        int ketQua = fileChooser.showSaveDialog(this);
        if (ketQua != JFileChooser.APPROVE_OPTION) return;

        String duongDan = fileChooser.getSelectedFile().getAbsolutePath();
        if (!duongDan.toLowerCase().endsWith("." + duoiFile)) {
            duongDan = duongDan + "." + duoiFile;
        }
        final String filePath = duongDan;

        lblTrangThai.setText("Dang xuat...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                String tieuDe = (String) cboLoaiBaoCao.getSelectedItem();
                String[] headers = layHeaders();
                List<String[]> rows = layDuLieu();

                if (dungExcel) {
                    ExcelExporter.export(filePath, "BaoCao", headers, rows);
                } else {
                    PDFExporter.exportBangDuLieu(filePath, tieuDe, headers, rows);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    lblTrangThai.setText(" ");
                    UIUtils.thongBao(ExportReportDialog.this, "Da xuat bao cao thanh cong:\n" + filePath);
                    dispose();
                } catch (Exception ex) {
                    lblTrangThai.setText(" ");
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    UIUtils.thongBaoLoi(ExportReportDialog.this,
                            "Xuat bao cao that bai.\n" + (cause.getMessage() != null ? cause.getMessage() : cause.toString()));
                }
            }
        };
        worker.execute();
    }

    private String tenFileGoiY() {
        int idx = cboLoaiBaoCao.getSelectedIndex();
        String ngay = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        switch (idx) {
            case 0: return "DanhSachSinhVien_" + ngay;
            case 1: return "DanhSachHoaDon_" + ngay;
            default: return "DanhSachCongNo_" + ngay;
        }
    }

    private String[] layHeaders() {
        int idx = cboLoaiBaoCao.getSelectedIndex();
        switch (idx) {
            case 0:
                return new String[]{"Ma SV", "Ho ten", "Lop", "Khoa", "Email", "SDT"};
            case 1:
                return new String[]{"Ma hoa don", "Ma SV", "Hoc ky", "So tien", "Da nop", "Con no", "Han thanh toan"};
            default:
                return new String[]{"Ma hoa don", "Ma SV", "Hoc ky", "So tien", "Da nop", "Con no"};
        }
    }

    private List<String[]> layDuLieu() throws Exception {
        int idx = cboLoaiBaoCao.getSelectedIndex();
        List<String[]> rows = new ArrayList<>();

        if (idx == 0) {
            List<SinhVien> ds = sinhVienDAO.layTatCa();
            for (SinhVien sv : ds) {
                rows.add(new String[]{
                        sv.getMaSV(), sv.getHoTen(), sv.getLop(), sv.getKhoa(),
                        sv.getEmail(), sv.getSoDienThoai()
                });
            }
        } else if (idx == 1) {
            List<HoaDonHocPhi> ds = hoaDonDAO.layTatCa();
            for (HoaDonHocPhi hd : ds) {
                rows.add(new String[]{
                        String.valueOf(hd.getMaHoaDon()), hd.getMaSV(), hd.getTenHocKy(),
                        MoneyUtils.format(hd.getSoTien()), MoneyUtils.format(hd.getDaNop()),
                        MoneyUtils.format(hd.tinhConNo()),
                        hd.getHanThanhToan() != null ? hd.getHanThanhToan().format(DMY) : ""
                });
            }
        } else {
            List<HoaDonHocPhi> ds = hoaDonDAO.layTatCa();
            for (HoaDonHocPhi hd : ds) {
                if (hd.tinhConNo().signum() <= 0) continue;
                rows.add(new String[]{
                        String.valueOf(hd.getMaHoaDon()), hd.getMaSV(), hd.getTenHocKy(),
                        MoneyUtils.format(hd.getSoTien()), MoneyUtils.format(hd.getDaNop()),
                        MoneyUtils.format(hd.tinhConNo())
                });
            }
        }
        return rows;
    }
}
