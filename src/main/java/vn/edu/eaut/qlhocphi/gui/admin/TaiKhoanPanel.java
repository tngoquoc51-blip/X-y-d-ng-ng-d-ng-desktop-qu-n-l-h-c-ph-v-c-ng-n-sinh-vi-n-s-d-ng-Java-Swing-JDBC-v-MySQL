package vn.edu.eaut.qlhocphi.gui.admin;

import vn.edu.eaut.qlhocphi.bus.TaiKhoanService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.VaiTro;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Màn hình quản lý tài khoản hệ thống - bản "desktop quản lý" đầy đủ:
 * banner đồng bộ màu (xanh dương, cùng tông với header), 4 thẻ KPI theo vai
 * trò, tìm kiếm/lọc theo vai trò/trạng thái (toolbar GridBagLayout - không
 * bao giờ wrap/chồng đè), vai trò và trạng thái hiển thị dạng thẻ màu (pill).
 * Chỉ dành cho vai trò ADMIN.
 */
public class TaiKhoanPanel extends JPanel {
    private final TaiKhoanService taiKhoanService = new TaiKhoanService();
    private final TaiKhoan taiKhoanDangDangNhap;

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField txtTimKiem;
    private JComboBox<String> cboVaiTro;
    private JComboBox<String> cboTrangThai;
    private JLabel lblSoLuong;
    private JLabel lblDaChon;

    private JLabel lblTongTK, lblSoAdmin, lblSoPdt, lblSoKeToan, lblSoSinhVien;

    public TaiKhoanPanel(TaiKhoan taiKhoanDangDangNhap) {
        this.taiKhoanDangDangNhap = taiKhoanDangDangNhap;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(buildHeader());
        north.add(Box.createRigidArea(new Dimension(0, 16)));
        north.add(buildKpiRow());
        north.add(Box.createRigidArea(new Dimension(0, 16)));
        north.add(buildToolbar());
        add(north, BorderLayout.NORTH);

        add(buildTableCard(), BorderLayout.CENTER);

        taiDuLieu();
    }

    // ================== HEADER (banner + logo) ==================

    private JPanel buildHeader() {
        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout(14, 0));
        banner.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        banner.setPreferredSize(new Dimension(10, 90));

        JPanel trai = new JPanel(new BorderLayout(14, 0));
        trai.setOpaque(false);
        trai.add(logoBadge(), BorderLayout.WEST);

        JPanel chuText = new JPanel();
        chuText.setOpaque(false);
        chuText.setLayout(new BoxLayout(chuText, BoxLayout.Y_AXIS));
        JLabel tieuDe = new JLabel("Quản Lý Tài Khoản");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        JLabel phu = new JLabel("Tạo, phân quyền, khóa/mở khóa và quản lý tài khoản hệ thống");
        phu.setFont(UITheme.FONT_BASE);
        phu.setForeground(new Color(255, 255, 255, 210));
        chuText.add(tieuDe);
        chuText.add(Box.createRigidArea(new Dimension(0, 4)));
        chuText.add(phu);
        trai.add(chuText, BorderLayout.CENTER);
        banner.add(trai, BorderLayout.WEST);

        JButton btnThem = new JButton("+ Thêm Tài Khoản");
        btnThem.setFont(UITheme.FONT_BOLD);
        btnThem.setBackground(Color.WHITE);
        btnThem.setForeground(UITheme.PRIMARY_DARK);
        btnThem.setFocusPainted(false);
        btnThem.setBorderPainted(false);
        btnThem.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnThem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnThem.addActionListener(e -> moFormThem());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(btnThem);
        banner.add(actions, BorderLayout.EAST);

        return banner;
    }

    private JComponent logoBadge() {
        JComponent badge = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 55));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
                FontMetrics fm = g2.getFontMetrics();
                String icon = "\uD83D\uDD10";
                int x = (getWidth() - fm.stringWidth(icon)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(icon, x, y);
                g2.dispose();
            }
        };
        badge.setPreferredSize(new Dimension(52, 52));
        badge.setOpaque(false);
        return badge;
    }

    // ================== KPI THEO VAI TRÒ ==================

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        lblTongTK = new JLabel("0");
        lblSoAdmin = new JLabel("0");
        lblSoPdt = new JLabel("0");
        lblSoKeToan = new JLabel("0");
        lblSoSinhVien = new JLabel("0");
        row.add(thongKeCard("Tổng Tài Khoản", lblTongTK, UITheme.PRIMARY));
        row.add(thongKeCard("Admin Hệ Thống", lblSoAdmin, UITheme.TEXT_VIOLET));
        row.add(thongKeCard("Phòng Đào Tạo", lblSoPdt, UITheme.PRIMARY));
        row.add(thongKeCard("Kế Toán", lblSoKeToan, UITheme.WARNING));
        row.add(thongKeCard("Sinh Viên", lblSoSinhVien, UITheme.SUCCESS));
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

    // ================== TOOLBAR: GridBagLayout (không wrap/chồng đè) ==================

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new GridBagLayout());
        toolbar.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 8);
        int col = 0;

        gbc.gridx = col++;
        toolbar.add(UIUtils.formLabel("Tìm kiếm:"), gbc);

        txtTimKiem = UIUtils.textField(16);
        txtTimKiem.setToolTipText("Tìm theo tên đăng nhập hoặc họ tên");
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { apDungBoLoc(); }
            @Override public void removeUpdate(DocumentEvent e) { apDungBoLoc(); }
            @Override public void changedUpdate(DocumentEvent e) { apDungBoLoc(); }
        });
        gbc.gridx = col++;
        toolbar.add(txtTimKiem, gbc);

        gbc.gridx = col++;
        toolbar.add(UIUtils.formLabel("Vai trò:"), gbc);

        cboVaiTro = new JComboBox<>(new String[]{"Tất cả vai trò", "ADMIN", "PHONGDAOTAO", "KETOAN", "SINHVIEN"});
        cboVaiTro.setFont(UITheme.FONT_BASE);
        cboVaiTro.addActionListener(e -> apDungBoLoc());
        gbc.gridx = col++;
        toolbar.add(cboVaiTro, gbc);

        gbc.gridx = col++;
        toolbar.add(UIUtils.formLabel("Trạng thái:"), gbc);

        cboTrangThai = new JComboBox<>(new String[]{"Tất cả trạng thái", "Hoạt động", "Đã khóa"});
        cboTrangThai.setFont(UITheme.FONT_BASE);
        cboTrangThai.addActionListener(e -> apDungBoLoc());
        gbc.gridx = col++;
        gbc.insets = new Insets(0, 0, 0, 0);
        toolbar.add(cboTrangThai, gbc);

        return toolbar;
    }

    // ================== BẢNG DỮ LIỆU (7 cột, có Gmail liên kết) ==================

    private JPanel buildTableCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));

        tableModel = new DefaultTableModel(
                new Object[]{"Mã TK", "Tên Đăng Nhập", "Họ Tên", "Vai Trò",
                        "Mã SV Liên Kết", "Gmail Liên Kết", "Trạng Thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        table.getColumnModel().getColumn(3).setCellRenderer(vaiTroCellRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(trangThaiCellRenderer());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            lblDaChon.setText(row < 0 ? "Chưa chọn tài khoản nào"
                    : "Đã chọn: " + table.getValueAt(row, 1) + " - " + table.getValueAt(row, 2));
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        JPanel footerTrai = new JPanel();
        footerTrai.setOpaque(false);
        footerTrai.setLayout(new BoxLayout(footerTrai, BoxLayout.Y_AXIS));
        lblSoLuong = new JLabel("Hiển thị 0 / 0 tài khoản");
        lblSoLuong.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSoLuong.setForeground(UITheme.TEXT_MUTED);
        lblDaChon = new JLabel("Chưa chọn tài khoản nào");
        lblDaChon.setFont(UITheme.FONT_BASE);
        lblDaChon.setForeground(UITheme.TEXT_MUTED);
        footerTrai.add(lblSoLuong);
        footerTrai.add(lblDaChon);
        footer.add(footerTrai, BorderLayout.WEST);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        toolbar.setOpaque(false);
        JButton btnSua = mauButton("Sửa Thông Tin", UITheme.PRIMARY);
        JButton btnDoiMatKhau = mauButton("Đổi Mật Khẩu", new Color(0x0E, 0xA5, 0xE9)); // xanh dương trời - riêng biệt
        JButton btnKhoaMoKhoa = mauButton("Khóa / Mở Khóa", UITheme.WARNING);
        JButton btnXoa = UITheme.dangerButton("Xóa Tài Khoản");

        btnSua.addActionListener(e -> moFormSua());
        btnDoiMatKhau.addActionListener(e -> doiMatKhau());
        btnKhoaMoKhoa.addActionListener(e -> khoaMoKhoa());
        btnXoa.addActionListener(e -> xoaTaiKhoan());

        JButton btnTheQR = mauButton("The QR dang nhap", new Color(0x7C, 0x3A, 0xED));
        btnTheQR.addActionListener(e -> moTheQR());
        toolbar.add(btnSua);
        toolbar.add(btnDoiMatKhau);
        toolbar.add(btnKhoaMoKhoa);
        toolbar.add(btnXoa);
        toolbar.add(btnTheQR);
        footer.add(toolbar, BorderLayout.EAST);

        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    /** Vai trò hiển thị dạng thẻ màu (pill) thay vì chữ enum thô: ADMIN/KETOAN/SINHVIEN. */
    private DefaultTableCellRenderer vaiTroCellRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(UITheme.FONT_BOLD);
                label.setOpaque(true);
                label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

                VaiTro vt = value instanceof VaiTro ? (VaiTro) value : null;
                String nhan;
                Color bg, fg;
                if (vt == VaiTro.ADMIN) {
                    nhan = "Admin Hệ Thống"; bg = UITheme.TINT_VIOLET; fg = UITheme.TEXT_VIOLET;
                } else if (vt == VaiTro.PHONGDAOTAO) {
                    nhan = "Phòng Đào Tạo"; bg = new Color(0xE0, 0xF2, 0xFE); fg = UITheme.PRIMARY;
                } else if (vt == VaiTro.KETOAN) {
                    nhan = "Kế toán"; bg = new Color(0xFD, 0xF3, 0xDA); fg = UITheme.WARNING;
                } else {
                    nhan = "Sinh viên"; bg = UITheme.TINT_GREEN; fg = UITheme.TEXT_GREEN;
                }
                label.setText(nhan);
                if (!isSelected) {
                    label.setBackground(bg);
                    label.setForeground(fg);
                }
                return label;
            }
        };
    }

    /** Trạng thái hiển thị dạng thẻ màu (pill): xanh = Hoạt động, đỏ = Đã khóa. */
    private DefaultTableCellRenderer trangThaiCellRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(UITheme.FONT_BOLD);
                label.setOpaque(true);
                label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                boolean hoatDong = "Hoạt động".equals(value);
                if (!isSelected) {
                    label.setBackground(hoatDong ? UITheme.TINT_GREEN : new Color(0xFC, 0xE4, 0xE4));
                    label.setForeground(hoatDong ? UITheme.TEXT_GREEN : UITheme.DANGER);
                }
                return label;
            }
        };
    }

    // ================== TÌM KIẾM / LỌC ==================

    private void apDungBoLoc() {
        List<RowFilter<Object, Object>> danhSachLoc = new ArrayList<>();

        String tuKhoa = txtTimKiem.getText().trim();
        if (!tuKhoa.isEmpty()) {
            danhSachLoc.add(RowFilter.regexFilter("(?i)" + Pattern.quote(tuKhoa), 1, 2));
        }
        String vaiTro = (String) cboVaiTro.getSelectedItem();
        if (vaiTro != null && !vaiTro.equals("Tất cả vai trò")) {
            danhSachLoc.add(RowFilter.regexFilter("^" + Pattern.quote(vaiTro) + "$", 3));
        }
        String trangThai = (String) cboTrangThai.getSelectedItem();
        if (trangThai != null && !trangThai.equals("Tất cả trạng thái")) {
            danhSachLoc.add(RowFilter.regexFilter("^" + Pattern.quote(trangThai) + "$", 6));
        }

        sorter.setRowFilter(danhSachLoc.isEmpty() ? null : RowFilter.andFilter(danhSachLoc));
        capNhatSoLuongHienThi();
    }

    private void capNhatSoLuongHienThi() {
        lblSoLuong.setText("Hiển thị " + table.getRowCount() + " / " + tableModel.getRowCount() + " tài khoản");
    }

    // ================== TẢI DỮ LIỆU ==================

    private void taiDuLieu() {
        SwingWorker<List<TaiKhoan>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TaiKhoan> doInBackground() throws Exception {
                return taiKhoanService.layTatCa();
            }

            @Override
            protected void done() {
                try {
                    List<TaiKhoan> list = get();
                    tableModel.setRowCount(0);
                    int soAdmin = 0, soPdt = 0, soKeToan = 0, soSinhVien = 0;
                    for (TaiKhoan tk : list) {
                        tableModel.addRow(new Object[]{
                                tk.getMaTK(), tk.getTenDangNhap(), tk.getHoTen(), tk.getVaiTro(),
                                tk.getMaSV() == null ? "" : tk.getMaSV(),
                                tk.getGoogleEmail() == null ? "" : tk.getGoogleEmail(),
                                tk.isTrangThai() ? "Hoạt động" : "Đã khóa"
                        });
                        if (tk.getVaiTro() == VaiTro.ADMIN) soAdmin++;
                        else if (tk.getVaiTro() == VaiTro.PHONGDAOTAO) soPdt++;
                        else if (tk.getVaiTro() == VaiTro.KETOAN) soKeToan++;
                        else soSinhVien++;
                    }
                    lblTongTK.setText(String.valueOf(list.size()));
                    lblSoAdmin.setText(String.valueOf(soAdmin));
                    lblSoPdt.setText(String.valueOf(soPdt));
                    lblSoKeToan.setText(String.valueOf(soKeToan));
                    lblSoSinhVien.setText(String.valueOf(soSinhVien));

                    apDungBoLoc();
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(TaiKhoanPanel.this, "Không thể tải danh sách tài khoản.");
                }
            }
        };
        worker.execute();
    }

    private TaiKhoan layTaiKhoanDangChon() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            UIUtils.thongBaoLoi(this, "Vui lòng chọn 1 tài khoản trong bảng");
            return null;
        }
        int row = table.convertRowIndexToModel(viewRow);
        TaiKhoan tk = new TaiKhoan();
        tk.setMaTK((int) tableModel.getValueAt(row, 0));
        tk.setTenDangNhap((String) tableModel.getValueAt(row, 1));
        tk.setHoTen((String) tableModel.getValueAt(row, 2));
        tk.setVaiTro((VaiTro) tableModel.getValueAt(row, 3));
        String maSV = (String) tableModel.getValueAt(row, 4);
        tk.setMaSV(maSV == null || maSV.isBlank() ? null : maSV);
        String googleEmail = (String) tableModel.getValueAt(row, 5);
        tk.setGoogleEmail(googleEmail == null || googleEmail.isBlank() ? null : googleEmail);
        tk.setTrangThai("Hoạt động".equals(tableModel.getValueAt(row, 6)));
        return tk;
    }

    // ================== THÊM / SỬA / ĐỔI MẬT KHẨU / KHÓA / XÓA (giữ nguyên logic cũ) ==================

    private void moFormThem() {
        TaiKhoanFormDialog dialog = new TaiKhoanFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), null,
                (tenDangNhap, matKhau, hoTen, vaiTro, maSV, googleEmail) -> {
                    SwingWorker<Void, Void> worker = new SwingWorker<>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            taiKhoanService.themTaiKhoan(tenDangNhap, matKhau, hoTen, vaiTro, maSV, googleEmail);
                            return null;
                        }

                        @Override
                        protected void done() {
                            try {
                                get();
                                taiDuLieu();
                                UIUtils.thongBao(TaiKhoanPanel.this, "Đã thêm tài khoản " + tenDangNhap);
                            } catch (Exception ex) {
                                UIUtils.thongBaoLoi(TaiKhoanPanel.this, rootMessage(ex));
                            }
                        }
                    };
                    worker.execute();
                },
                null);
        dialog.setVisible(true);
    }

    private void moFormSua() {
        TaiKhoan tk = layTaiKhoanDangChon();
        if (tk == null) return;

        TaiKhoanFormDialog dialog = new TaiKhoanFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), tk,
                null,
                (maTK, hoTen, vaiTro, maSV, googleEmail, trangThai) -> {
                    SwingWorker<Void, Void> worker = new SwingWorker<>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            taiKhoanService.capNhatThongTin(maTK, hoTen, vaiTro, maSV, googleEmail, trangThai);
                            return null;
                        }

                        @Override
                        protected void done() {
                            try {
                                get();
                                taiDuLieu();
                                UIUtils.thongBao(TaiKhoanPanel.this, "Đã cập nhật tài khoản");
                            } catch (Exception ex) {
                                UIUtils.thongBaoLoi(TaiKhoanPanel.this, rootMessage(ex));
                            }
                        }
                    };
                    worker.execute();
                });
        dialog.setVisible(true);
    }
    private void moTheQR() {
        TaiKhoan tk = layTaiKhoanDangChon();
        if (tk == null) return;
        if (tk.getVaiTro() != vn.edu.eaut.qlhocphi.model.VaiTro.SINHVIEN) {
            UIUtils.thongBaoLoi(this, "The QR dang nhap chi ap dung cho tai khoan Sinh vien.");
            return;
        }
        new TheQRDangNhapDialog((Frame) SwingUtilities.getWindowAncestor(this), tk).setVisible(true);
    }

    private void doiMatKhau() {
        TaiKhoan tk = layTaiKhoanDangChon();
        if (tk == null) return;

        AdminDoiMatKhauDialog dialog = new AdminDoiMatKhauDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), tk.getTenDangNhap(), matKhauMoi -> {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    taiKhoanService.doiMatKhau(tk.getMaTK(), matKhauMoi);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        UIUtils.thongBao(TaiKhoanPanel.this, "Đã đổi mật khẩu cho " + tk.getTenDangNhap());
                    } catch (Exception ex) {
                        UIUtils.thongBaoLoi(TaiKhoanPanel.this, rootMessage(ex));
                    }
                }
            };
            worker.execute();
        });
        dialog.setVisible(true);
    }

    private void khoaMoKhoa() {
        TaiKhoan tk = layTaiKhoanDangChon();
        if (tk == null) return;

        if (tk.getMaTK() == taiKhoanDangDangNhap.getMaTK()) {
            UIUtils.thongBaoLoi(this, "Không thể tự khóa tài khoản đang đăng nhập");
            return;
        }

        boolean trangThaiMoi = !tk.isTrangThai();
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                taiKhoanService.doiTrangThai(tk, trangThaiMoi);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    taiDuLieu();
                    UIUtils.thongBao(TaiKhoanPanel.this,
                            trangThaiMoi ? "Đã mở khóa tài khoản" : "Đã khóa tài khoản");
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(TaiKhoanPanel.this, rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private void xoaTaiKhoan() {
        TaiKhoan tk = layTaiKhoanDangChon();
        if (tk == null) return;

        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Xóa vĩnh viễn tài khoản " + tk.getTenDangNhap() + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (xacNhan != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                taiKhoanService.xoaTaiKhoan(tk.getMaTK(), taiKhoanDangDangNhap.getMaTK());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    taiDuLieu();
                    UIUtils.thongBao(TaiKhoanPanel.this, "Đã xóa tài khoản " + tk.getTenDangNhap());
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(TaiKhoanPanel.this, rootMessage(ex));
                }
            }
        };
        worker.execute();
    }


    /** Tạo 1 nút màu tùy chỉnh (không có sẵn trong UITheme) - dùng để mỗi nút hành động
     *  có 1 màu riêng biệt, dễ phân biệt nhanh: Sửa (xanh dậm), Đổi mật khẩu (xanh trời),
     *  Khóa/Mở khóa (cam), Xóa (đỏ - dùng sẵn UITheme.dangerButton). */
    private JButton mauButton(String text, Color mau) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(mau);
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private String rootMessage(Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }
}