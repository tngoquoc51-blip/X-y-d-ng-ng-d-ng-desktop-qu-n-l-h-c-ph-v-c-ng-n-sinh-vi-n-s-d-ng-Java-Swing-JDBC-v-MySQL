package vn.edu.eaut.qlhocphi.gui.thanhtoan;

import vn.edu.eaut.qlhocphi.ai.KetQuaOcrBienLai;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Sau khi AI doc xong anh bien lai, dialog nay HIEN THI LAI cac truong da doc duoc
 * (mo hoac sai deu co the sua tay o day) de ke toan KIEM TRA VA XAC NHAN truoc khi
 * dien vao form ghi nhan thanh toan. Khong bao gio tu dong ghi thang vao CSDL tu ket
 * qua AI - luon can nguoi xac nhan cuoi cung, dam bao an toan du lieu tai chinh.
 */
public class XacNhanOcrBienLaiDialog extends JDialog {

    private JTextField txtMaHoaDon, txtSoTien;
    private JComboBox<String> cboHinhThuc;
    private JLabel lblMaSV, lblHoTen, lblNgay, lblGhiChu;
    private boolean xacNhan = false;

    public XacNhanOcrBienLaiDialog(Window chaMe, KetQuaOcrBienLai ocr) {
        super(chaMe, "AI da doc duoc tu bien lai - kiem tra lai truoc khi dung", ModalityType.APPLICATION_MODAL);
        setSize(460, 460);
        setLocationRelativeTo(chaMe);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UITheme.BG_CARD);
        setLayout(new BorderLayout());

        add(buildNoiDung(ocr), BorderLayout.CENTER);
        add(buildHangNut(), BorderLayout.SOUTH);
    }

    private JPanel buildNoiDung(KetQuaOcrBienLai ocr) {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBorder(new EmptyBorder(18, 20, 10, 20));

        if (ocr.getCanhBao() != null) {
            JLabel lblCanhBao = new JLabel("<html><body style='width:380px'>&#9888; " + ocr.getCanhBao() + "</body></html>");
            lblCanhBao.setForeground(UITheme.WARNING);
            lblCanhBao.setFont(UITheme.FONT_BASE);
            lblCanhBao.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrap.add(lblCanhBao);
            wrap.add(Box.createRigidArea(new Dimension(0, 12)));
        }

        wrap.add(dong("Ma hoa don (neu doc duoc)", txtMaHoaDon = UIUtils.textField(10)));
        txtMaHoaDon.setText(ocr.getMaHoaDon() != null ? String.valueOf(ocr.getMaHoaDon()) : "");
        wrap.add(Box.createRigidArea(new Dimension(0, 10)));

        wrap.add(dong("So tien (VND)", txtSoTien = UIUtils.textField(10)));
        txtSoTien.setText(ocr.getSoTien() != null ? ocr.getSoTien().toBigInteger().toString() : "");
        wrap.add(Box.createRigidArea(new Dimension(0, 10)));

        cboHinhThuc = new JComboBox<>(new String[]{"TIEN_MAT", "CHUYEN_KHOAN"});
        if (ocr.getHinhThuc() != null) cboHinhThuc.setSelectedItem(ocr.getHinhThuc());
        wrap.add(dong("Hinh thuc", cboHinhThuc));
        wrap.add(Box.createRigidArea(new Dimension(0, 14)));

        JPanel boxThamKhao = new JPanel();
        boxThamKhao.setLayout(new BoxLayout(boxThamKhao, BoxLayout.Y_AXIS));
        boxThamKhao.setBackground(UITheme.TINT_BLUE);
        boxThamKhao.setOpaque(true);
        boxThamKhao.setAlignmentX(Component.LEFT_ALIGNMENT);
        boxThamKhao.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        boxThamKhao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        JLabel tieuDeThamKhao = new JLabel("Thong tin them AI doc duoc (de doi chieu, khong tu dien vao form)");
        tieuDeThamKhao.setFont(UITheme.FONT_BOLD);
        tieuDeThamKhao.setForeground(UITheme.TEXT_PRIMARY);
        boxThamKhao.add(tieuDeThamKhao);
        boxThamKhao.add(Box.createRigidArea(new Dimension(0, 4)));

        lblMaSV = dongThamKhao("Ma SV: " + (ocr.getMaSV() != null ? ocr.getMaSV() : "-"));
        lblHoTen = dongThamKhao("Ho ten: " + (ocr.getHoTen() != null ? ocr.getHoTen() : "-"));
        lblNgay = dongThamKhao("Ngay tren bien lai: " + (ocr.getNgay() != null ? ocr.getNgay() : "-"));
        lblGhiChu = dongThamKhao("Ghi chu: " + (ocr.getGhiChu() != null ? ocr.getGhiChu() : "-"));
        boxThamKhao.add(lblMaSV);
        boxThamKhao.add(lblHoTen);
        boxThamKhao.add(lblNgay);
        boxThamKhao.add(lblGhiChu);
        wrap.add(boxThamKhao);

        return wrap;
    }

    private JPanel dong(String nhan, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        JLabel lbl = UIUtils.formLabel(nhan);
        p.add(lbl, BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        return p;
    }

    private JLabel dongThamKhao(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BASE);
        l.setForeground(UITheme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JPanel buildHangNut() {
        JPanel hang = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        hang.setOpaque(false);
        hang.setBorder(new EmptyBorder(0, 20, 16, 20));

        JButton btnHuy = UITheme.secondaryButton("Bo qua");
        btnHuy.addActionListener(e -> dispose());

        JButton btnDung = UITheme.primaryButton("Dien vao form");
        btnDung.addActionListener(e -> { xacNhan = true; dispose(); });

        hang.add(btnHuy);
        hang.add(btnDung);
        return hang;
    }

    public boolean daXacNhan() { return xacNhan; }

    /** Ma hoa don da sua (neu co), null neu de trong hoac khong phai so. */
    public Integer layMaHoaDon() {
        String t = txtMaHoaDon.getText().trim();
        if (t.isEmpty()) return null;
        try { return Integer.parseInt(t); } catch (NumberFormatException ex) { return null; }
    }

    /** So tien da sua (neu co), null neu de trong hoac khong phai so. */
    public BigDecimal laySoTien() {
        String t = txtSoTien.getText().trim().replaceAll("[^0-9]", "");
        if (t.isEmpty()) return null;
        try { return new BigDecimal(t); } catch (NumberFormatException ex) { return null; }
    }

    public String layHinhThuc() {
        return (String) cboHinhThuc.getSelectedItem();
    }
}
