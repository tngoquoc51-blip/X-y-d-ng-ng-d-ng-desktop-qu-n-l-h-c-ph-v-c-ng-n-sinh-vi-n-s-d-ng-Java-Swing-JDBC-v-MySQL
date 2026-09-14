package vn.edu.eaut.qlhocphi.gui.admin;

import vn.edu.eaut.qlhocphi.bus.ThongBaoService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Man hinh Phong Dao Tao / Admin: gui thong bao vao hop thu sinh vien
 * (bang ThongBao, VaiTroNhan = SINHVIEN).
 */
public class GuiThongBaoSinhVienPanel extends JPanel {

    private final TaiKhoan taiKhoan;
    private final ThongBaoService service = new ThongBaoService();

    private final JTextField txtTieuDe = new JTextField();
    private final JTextArea txtNoiDung = new JTextArea(8, 40);
    private final JTextField txtMaSV = new JTextField();
    private final JCheckBox chkTatCa = new JCheckBox("Gửi tất cả sinh viên", true);
    private final JLabel lblTrangThai = new JLabel(" ");

    public GuiThongBaoSinhVienPanel(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(16, 20, 16, 20));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel t = new JLabel("Gửi thông báo cho sinh viên");
        t.setFont(UITheme.FONT_TITLE != null ? UITheme.FONT_TITLE : new Font("Segoe UI", Font.BOLD, 20));
        t.setForeground(UITheme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Thông báo sẽ xuất hiện trong mục \"Thông Báo\" trên giao diện sinh viên.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(UITheme.TEXT_MUTED);
        p.add(t);
        p.add(Box.createVerticalStrut(4));
        p.add(sub);
        return p;
    }

    private JPanel buildForm() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5, 0xE7, 0xEB)),
                new EmptyBorder(20, 24, 20, 24)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 0, 6, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weightx = 1;

        c.gridy = 0;
        card.add(label("Tiêu đề *"), c);
        c.gridy = 1;
        txtTieuDe.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        card.add(txtTieuDe, c);

        c.gridy = 2;
        card.add(label("Nội dung *"), c);
        c.gridy = 3;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        txtNoiDung.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNoiDung.setLineWrap(true);
        txtNoiDung.setWrapStyleWord(true);
        card.add(new JScrollPane(txtNoiDung), c);

        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 4;
        chkTatCa.setOpaque(false);
        chkTatCa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkTatCa.addActionListener(e -> {
            txtMaSV.setEnabled(!chkTatCa.isSelected());
            if (chkTatCa.isSelected()) txtMaSV.setText("");
        });
        card.add(chkTatCa, c);

        c.gridy = 5;
        card.add(label("Mã sinh viên (nếu chỉ gửi 1 người)"), c);
        c.gridy = 6;
        txtMaSV.setEnabled(false);
        txtMaSV.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        card.add(txtMaSV, c);

        c.gridy = 7;
        JPanel acts = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        acts.setOpaque(false);
        JButton btnGui = new JButton("Gửi thông báo");
        btnGui.setBackground(UITheme.PRIMARY);
        btnGui.setForeground(Color.WHITE);
        btnGui.setFocusPainted(false);
        btnGui.setBorderPainted(false);
        btnGui.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGui.setPreferredSize(new Dimension(160, 40));
        btnGui.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnGui.addActionListener(e -> gui());
        acts.add(btnGui);
        lblTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        acts.add(lblTrangThai);
        card.add(acts, c);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(card, BorderLayout.NORTH);
        return wrap;
    }

    private JLabel label(String s) {
        JLabel l = new JLabel(s);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }

    private void gui() {
        String tieuDe = txtTieuDe.getText() != null ? txtTieuDe.getText().trim() : "";
        String noiDung = txtNoiDung.getText() != null ? txtNoiDung.getText().trim() : "";
        if (tieuDe.isEmpty() || noiDung.isEmpty()) {
            lblTrangThai.setForeground(UITheme.DANGER);
            lblTrangThai.setText("Vui lòng nhập tiêu đề và nội dung.");
            return;
        }
        String maSV = null;
        if (!chkTatCa.isSelected()) {
            maSV = txtMaSV.getText() != null ? txtMaSV.getText().trim() : "";
            if (maSV.isEmpty()) {
                lblTrangThai.setForeground(UITheme.DANGER);
                lblTrangThai.setText("Nhập mã sinh viên hoặc chọn gửi tất cả.");
                return;
            }
        }

        final String maSVFinal = maSV;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                service.guiThongBaoSinhVien(maSVFinal, tieuDe, noiDung);
                String ai = taiKhoan.getTenDangNhap() != null ? taiKhoan.getTenDangNhap() : "pdt";
                System.out.println("[ThongBao] " + ai + " gui TB SV=" + (maSVFinal == null ? "ALL" : maSVFinal) + " | " + tieuDe);
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                lblTrangThai.setForeground(new Color(0x05, 0x96, 0x69));
                lblTrangThai.setText(maSVFinal == null
                        ? "Đã gửi thông báo tới tất cả sinh viên."
                        : "Đã gửi thông báo tới SV " + maSVFinal + ".");
                txtTieuDe.setText("");
                txtNoiDung.setText("");
            }
        }.execute();
    }
}