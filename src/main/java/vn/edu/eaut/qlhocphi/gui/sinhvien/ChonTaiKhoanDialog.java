package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Hop thoai nho "Chon tai khoan" - dung khi 1 Gmail dang duoc gan chung cho NHIEU
 * tai khoan (vi du 1 nguoi vua co tai khoan Sinh vien vua co tai khoan Ke toan, dung
 * chung Gmail ca nhan). Hien 1 danh sach nut, moi nut la 1 tai khoan; bam vao nut nao
 * thi callback duoc goi voi dung TaiKhoan do.
 *
 * Dung chung cho ca luong "Dang nhap bang Google" (StudentLoginFrame) - luong "Quen
 * mat khau" tu ve UI rieng ngay trong QuenMatKhauDialog vi da co san khung wizard.
 */
public class ChonTaiKhoanDialog extends JDialog {

    public ChonTaiKhoanDialog(Window chaMe, List<TaiKhoan> danhSach, Consumer<TaiKhoan> khiChon) {
        super(chaMe, "Chọn tài khoản", ModalityType.APPLICATION_MODAL);
        setSize(420, 120 + danhSach.size() * 66);
        setMinimumSize(new Dimension(380, 220));
        setLocationRelativeTo(chaMe);
        setResizable(false);
        getContentPane().setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JPanel wrap = new JPanel();
        wrap.setBackground(Color.WHITE);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBorder(new EmptyBorder(22, 24, 20, 24));

        JLabel tieuDe = new JLabel("Gmail này dùng chung cho nhiều tài khoản");
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tieuDe.setForeground(UITheme.TEXT_PRIMARY);
        tieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel moTa = new JLabel("Chọn đúng tài khoản bạn muốn đăng nhập:");
        moTa.setFont(UITheme.FONT_BASE);
        moTa.setForeground(UITheme.TEXT_MUTED);
        moTa.setAlignmentX(Component.LEFT_ALIGNMENT);
        moTa.setBorder(new EmptyBorder(4, 0, 16, 0));

        wrap.add(tieuDe);
        wrap.add(moTa);

        for (TaiKhoan tk : danhSach) {
            JButton btn = taoNutTaiKhoan(tk, khiChon);
            wrap.add(btn);
            wrap.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        JScrollPane scroll = new JScrollPane(wrap);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JButton taoNutTaiKhoan(TaiKhoan tk, Consumer<TaiKhoan> khiChon) {
        String nhanVaiTro = nhanTheoVaiTro(tk);
        JButton btn = new JButton("<html><div style='padding:4px 0'>"
                + "<b style='font-size:13px'>" + tk.getHoTen() + "</b><br>"
                + "<span style='color:#6B7280;font-size:11px'>" + nhanVaiTro + " · " + tk.getTenDangNhap() + "</span>"
                + "</div></html>");
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            dispose();
            khiChon.accept(tk);
        });
        return btn;
    }

    private String nhanTheoVaiTro(TaiKhoan tk) {
        if (tk.getVaiTro() == null) return "Tài khoản";
        switch (tk.getVaiTro()) {
            case ADMIN: return "Quản trị viên";
            case KETOAN: return "Kế toán";
            case SINHVIEN: return "Sinh viên";
            default: return tk.getVaiTro().name();
        }
    }
}