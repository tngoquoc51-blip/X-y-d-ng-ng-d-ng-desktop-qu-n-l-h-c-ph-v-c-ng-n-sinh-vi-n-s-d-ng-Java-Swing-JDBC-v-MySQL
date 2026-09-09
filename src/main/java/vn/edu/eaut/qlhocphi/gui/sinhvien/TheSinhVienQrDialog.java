package vn.edu.eaut.qlhocphi.gui.sinhvien;

import com.google.zxing.WriterException;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.SinhVien;
import vn.edu.eaut.qlhocphi.util.QrCodeUtils;
import vn.edu.eaut.qlhocphi.util.TheSinhVienQrUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;

/**
 * Dialog hien "the sinh vien QR" TU DONG SINH RA ngay sau khi admin them 1 sinh
 * vien / cap tai khoan moi. QR ma hoa DAY DU: Ma SV, Ho ten, Lop, Khoa (xem
 * TheSinhVienQrUtils) - sau nay quet lai o man Cong no / Tra cuu la ra dung
 * sinh vien ngay, khong can go tay.
 *
 * Cach dung - GOI NGAY SAU KHI LUU THANH CONG 1 SINH VIEN MOI:
 *   new TheSinhVienQrDialog(chaMe, sinhVienVuaTao).setVisible(true);
 */
public class TheSinhVienQrDialog extends JDialog {

    private final SinhVien sinhVien;
    private BufferedImage anhQR;

    public TheSinhVienQrDialog(Window chaMe, SinhVien sv) {
        super(chaMe, "The sinh vien - Ma QR", ModalityType.APPLICATION_MODAL);
        this.sinhVien = sv;
        setSize(420, 560);
        setLocationRelativeTo(chaMe);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UITheme.BG_CARD);
        setLayout(new BorderLayout());

        try {
            String noiDung = TheSinhVienQrUtils.taoNoiDungThe(sv);
            anhQR = QrCodeUtils.taoAnhQR(noiDung, 360);
        } catch (WriterException ex) {
            anhQR = null;
        }

        add(buildNoiDung(), BorderLayout.CENTER);
        add(buildHangNut(), BorderLayout.SOUTH);
    }

    private JPanel buildNoiDung() {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel tieuDe = UITheme.sectionLabel("Da tao ma QR cho sinh vien nay");
        tieuDe.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrap.add(tieuDe);
        wrap.add(Box.createRigidArea(new Dimension(0, 14)));

        JLabel lblQR = new JLabel();
        lblQR.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (anhQR != null) {
            lblQR.setIcon(new ImageIcon(anhQR));
        } else {
            lblQR.setText("Khong tao duoc ma QR");
            lblQR.setForeground(UITheme.DANGER);
        }
        lblQR.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        wrap.add(lblQR);
        wrap.add(Box.createRigidArea(new Dimension(0, 16)));

        wrap.add(dongThongTin("Ma SV", sinhVien.getMaSV()));
        wrap.add(dongThongTin("Ho ten", sinhVien.getHoTen()));
        wrap.add(dongThongTin("Lop", sinhVien.getLop()));
        wrap.add(dongThongTin("Khoa", sinhVien.getKhoa()));

        wrap.add(Box.createRigidArea(new Dimension(0, 10)));
        JLabel ghiChu = new JLabel("<html><div style='text-align:center;width:340px'>"
                + "In hoac luu anh nay dan len the sinh vien. Sau nay chi can dua the vao "
                + "camera o man Cong no / Tra cuu la tra ra du lieu ngay.</div></html>");
        ghiChu.setFont(UITheme.FONT_BASE);
        ghiChu.setForeground(UITheme.TEXT_MUTED);
        ghiChu.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrap.add(ghiChu);

        return wrap;
    }

    private JPanel dongThongTin(String nhan, String giaTri) {
        JPanel dong = new JPanel(new BorderLayout(10, 0));
        dong.setOpaque(false);
        dong.setAlignmentX(Component.CENTER_ALIGNMENT);
        dong.setMaximumSize(new Dimension(340, 24));
        JLabel lblNhan = new JLabel(nhan + ":");
        lblNhan.setFont(UITheme.FONT_BASE);
        lblNhan.setForeground(UITheme.TEXT_MUTED);
        JLabel lblGiaTri = new JLabel(giaTri == null || giaTri.isBlank() ? "-" : giaTri);
        lblGiaTri.setFont(UITheme.FONT_BOLD);
        lblGiaTri.setForeground(UITheme.TEXT_PRIMARY);
        dong.add(lblNhan, BorderLayout.WEST);
        dong.add(lblGiaTri, BorderLayout.EAST);
        return dong;
    }

    private JPanel buildHangNut() {
        JPanel hang = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
        hang.setOpaque(false);
        hang.setBorder(new EmptyBorder(0, 16, 14, 16));

        JButton btnLuu = UITheme.secondaryButton("Luu anh QR (PNG)");
        btnLuu.setEnabled(anhQR != null);
        btnLuu.addActionListener(e -> luuAnh());

        JButton btnIn = UITheme.primaryButton("In the");
        btnIn.setEnabled(anhQR != null);
        btnIn.addActionListener(e -> inThe());

        JButton btnDong = UITheme.secondaryButton("Dong");
        btnDong.addActionListener(e -> dispose());

        hang.add(btnLuu);
        hang.add(btnIn);
        hang.add(btnDong);
        return hang;
    }

    private void luuAnh() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Luu anh QR");
        fc.setSelectedFile(new File("qr_the_sv_" + sinhVien.getMaSV() + ".png"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String path = fc.getSelectedFile().getAbsolutePath();
        if (!path.toLowerCase().endsWith(".png")) path += ".png";
        try {
            ImageIO.write(anhQR, "png", new File(path));
            UIUtils.thongBao(this, "Da luu anh QR:\n" + path);
        } catch (Exception ex) {
            UIUtils.thongBaoLoi(this, "Loi luu anh: " + ex.getMessage());
        }
    }

    private void inThe() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
                if (pageIndex > 0) return NO_SUCH_PAGE;
                Graphics2D g2 = (Graphics2D) g;
                g2.translate(pf.getImageableX(), pf.getImageableY());

                int qrSize = 260;
                g2.drawImage(anhQR, 20, 20, qrSize, qrSize, null);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                int textY = qrSize + 60;
                g2.drawString("Ma SV: " + sinhVien.getMaSV(), 20, textY);
                g2.drawString("Ho ten: " + sinhVien.getHoTen(), 20, textY + 24);
                g2.drawString("Lop: " + (sinhVien.getLop() == null ? "-" : sinhVien.getLop()), 20, textY + 48);
                g2.drawString("Khoa: " + (sinhVien.getKhoa() == null ? "-" : sinhVien.getKhoa()), 20, textY + 72);
                return PAGE_EXISTS;
            }
        });
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                UIUtils.thongBaoLoi(this, "Loi in: " + ex.getMessage());
            }
        }
    }
}
