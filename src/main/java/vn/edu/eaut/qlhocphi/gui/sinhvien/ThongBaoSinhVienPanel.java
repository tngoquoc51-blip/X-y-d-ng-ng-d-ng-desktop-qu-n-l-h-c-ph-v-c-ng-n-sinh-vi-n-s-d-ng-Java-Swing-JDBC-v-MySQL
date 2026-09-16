package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.bus.AdminConfigService;
import vn.edu.eaut.qlhocphi.bus.ThongBaoService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.AutoRefreshTimer;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.ThongBao;
import vn.edu.eaut.qlhocphi.model.ThongBaoBroadcast;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Thong bao & Tin tuc – dung khung:
 * [Tieu de + nut] → [3 the thong ke] → [Tab + Tim kiem] → [Danh sach | Chi tiet]
 * Phoi mau diem nhan theo tung loai chuc nang.
 */
public class ThongBaoSinhVienPanel extends JPanel {

    private final TaiKhoan taiKhoan;
    private final ThongBaoService thongBaoService = new ThongBaoService();
    private final AdminConfigService adminConfigService = new AdminConfigService();
    private final Runnable onBadgeChanged;
    private final Consumer<String> onDieuHuong;

    private final JPanel listPanel = new JPanel();
    private final JPanel detailPanel = new JPanel(new BorderLayout());
    private final JLabel lblCount = new JLabel("0 mục");
    private final JTextField txtTim = new JTextField();

    private final JLabel statTong = new JLabel("0");
    private final JLabel statChuaDoc = new JLabel("0");
    private final JLabel statTruong = new JLabel("0");

    private final JToggleButton tabTatCa = new JToggleButton("Tất cả");
    private final JToggleButton tabChuaDoc = new JToggleButton("Chưa đọc");
    private final JToggleButton tabTinTruong = new JToggleButton("Tin trường");

    private boolean chiChuaDoc = false;
    private boolean chiTinTruong = false;
    private String tuKhoa = "";

    private List<ThongBaoBroadcast> cacheBroadcast = new ArrayList<>();
    private List<ThongBao> cacheThongBao = new ArrayList<>();
    private JPanel selectedRow;

    // ===== Bang mau diem nhan (chuan he thong truong) =====
    private static final Color C_PRIMARY   = new Color(0x02, 0x84, 0xC7); // xanh he thong
    private static final Color C_HOADON    = new Color(0x7C, 0x3A, 0xED); // tim – hoa don
    private static final Color C_THANHTOAN = new Color(0x05, 0x96, 0x69); // xanh la – thanh toan
    private static final Color C_VI        = new Color(0xD9, 0x77, 0x06); // cam – vi
    private static final Color C_CONGNO    = new Color(0xDC, 0x26, 0x26); // do – cong no
    private static final Color C_TINCHI    = new Color(0x02, 0x84, 0xC7); // xanh – tin chi
    private static final Color C_CHATBOT   = new Color(0x8B, 0x5C, 0xF6); // tim nhat – AI
    private static final Color C_CANHAN    = new Color(0x47, 0x55, 0x69); // xam – ca nhan
    private static final Color C_TRUONG    = new Color(0x03, 0x69, 0xA1); // xanh dam – tin truong
    private static final Color C_WARN      = new Color(0xD9, 0x77, 0x06);
    private static final Color C_DANGER    = new Color(0xDC, 0x26, 0x26);

    private static final Color BG_CARD  = Color.WHITE;
    private static final Color BORDER   = new Color(0xE2, 0xE8, 0xF0);
    private static final Color MUTED    = new Color(0x64, 0x74, 0x8B);
    private static final Color TEXT     = new Color(0x0F, 0x17, 0x2A);
    private static final Color UNREAD   = new Color(0xF0, 0xF9, 0xFF);
    private static final Color SELECT   = new Color(0xDB, 0xEA, 0xFE);
    private static final Color FOOT_BG  = new Color(0xF8, 0xFA, 0xFC);

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ThongBaoSinhVienPanel(TaiKhoan taiKhoan) {
        this(taiKhoan, null, null);
    }

    public ThongBaoSinhVienPanel(TaiKhoan taiKhoan, Runnable onBadgeChanged) {
        this(taiKhoan, onBadgeChanged, null);
    }

    public ThongBaoSinhVienPanel(TaiKhoan taiKhoan, Runnable onBadgeChanged, Consumer<String> onDieuHuong) {
        this.taiKhoan = taiKhoan;
        this.onBadgeChanged = onBadgeChanged;
        this.onDieuHuong = onDieuHuong;

        setLayout(new BorderLayout(0, 12));
        setOpaque(false);

        add(buildTop(), BorderLayout.NORTH);
        add(buildMain(), BorderLayout.CENTER);

        showPlaceholder();
        taiDuLieu();
        AutoRefreshTimer.gan(this, 30, this::taiDuLieu);
    }

    // =====================================================================
    // TOP: tieu de + 3 the thong ke + tab + tim kiem
    // =====================================================================

    private JPanel buildTop() {
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        // --- Hang 1: tieu de + nut ---
        JPanel row1 = new JPanel(new BorderLayout(12, 0));
        row1.setOpaque(false);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel title = new JLabel("Thông báo & Tin tức nhà trường");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);
        JButton btnRefresh = softBtn("Làm mới");
        btnRefresh.addActionListener(e -> taiDuLieu());
        JButton btnReadAll = primaryBtn("Đánh dấu đã đọc hết");
        btnReadAll.addActionListener(e -> danhDauTatCa());
        acts.add(btnRefresh);
        acts.add(btnReadAll);

        row1.add(title, BorderLayout.WEST);
        row1.add(acts, BorderLayout.EAST);

        // --- Hang 2: 3 the thong ke ---
        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setOpaque(false);
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        stats.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        stats.setBorder(new EmptyBorder(12, 0, 0, 0));
        stats.add(statCard("Tổng thông báo", statTong, C_PRIMARY));
        stats.add(statCard("Chưa đọc", statChuaDoc, C_DANGER));
        stats.add(statCard("Tin toàn trường", statTruong, C_HOADON));

        // --- Hang 3: tab + tim kiem ---
        JPanel row3 = new JPanel(new BorderLayout(12, 0));
        row3.setOpaque(false);
        row3.setAlignmentX(Component.LEFT_ALIGNMENT);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        row3.setBorder(new EmptyBorder(14, 0, 0, 0));

        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        tabs.setOpaque(false);
        ButtonGroup g = new ButtonGroup();
        styleTab(tabTatCa);
        styleTab(tabChuaDoc);
        styleTab(tabTinTruong);
        g.add(tabTatCa);
        g.add(tabChuaDoc);
        g.add(tabTinTruong);
        tabTatCa.setSelected(true);
        tabTatCa.addActionListener(e -> { chiChuaDoc = false; chiTinTruong = false; veDanhSach(); });
        tabChuaDoc.addActionListener(e -> { chiChuaDoc = true; chiTinTruong = false; veDanhSach(); });
        tabTinTruong.addActionListener(e -> { chiChuaDoc = false; chiTinTruong = true; veDanhSach(); });
        tabs.add(tabTatCa);
        tabs.add(tabChuaDoc);
        tabs.add(tabTinTruong);

        txtTim.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTim.setPreferredSize(new Dimension(260, 34));
        txtTim.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(6, 12, 6, 12)));
        txtTim.putClientProperty("JTextField.placeholderText", "Tìm theo tiêu đề, nội dung...");
        txtTim.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applySearch(); }
            public void removeUpdate(DocumentEvent e) { applySearch(); }
            public void changedUpdate(DocumentEvent e) { applySearch(); }
            private void applySearch() {
                tuKhoa = txtTim.getText() == null ? "" : txtTim.getText().trim().toLowerCase();
                veDanhSach();
            }
        });
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchBox.setOpaque(false);
        searchBox.add(txtTim);

        row3.add(tabs, BorderLayout.WEST);
        row3.add(searchBox, BorderLayout.EAST);

        top.add(row1);
        top.add(stats);
        top.add(row3);
        return top;
    }

    private JPanel statCard(String label, JLabel valueLbl, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel bar = new JPanel();
        bar.setPreferredSize(new Dimension(4, 44));
        bar.setBackground(accent);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(MUTED);

        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLbl.setForeground(accent);

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        texts.add(lbl);
        texts.add(Box.createVerticalStrut(2));
        texts.add(valueLbl);

        JPanel inner = new JPanel(new BorderLayout(12, 0));
        inner.setOpaque(false);
        inner.add(bar, BorderLayout.WEST);
        inner.add(texts, BorderLayout.CENTER);
        card.add(inner);
        return card;
    }

    // =====================================================================
    // MAIN: danh sach | chi tiet
    // =====================================================================

    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout(14, 0));
        main.setOpaque(false);

        // LEFT
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(430, 100));

        JPanel listHead = new JPanel(new BorderLayout());
        listHead.setBackground(BG_CARD);
        listHead.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 0, 1, BORDER),
                new EmptyBorder(10, 14, 10, 14)));
        JLabel h = new JLabel("Hộp thư");
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setForeground(TEXT);
        lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCount.setForeground(MUTED);
        listHead.add(h, BorderLayout.WEST);
        listHead.add(lblCount, BorderLayout.EAST);

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(BG_CARD);
        listPanel.setBorder(new EmptyBorder(0, 0, 6, 0));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(BG_CARD);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        left.add(listHead, BorderLayout.NORTH);
        left.add(scroll, BorderLayout.CENTER);

        // RIGHT
        detailPanel.setBackground(BG_CARD);
        detailPanel.setBorder(BorderFactory.createLineBorder(BORDER));

        main.add(left, BorderLayout.WEST);
        main.add(detailPanel, BorderLayout.CENTER);
        return main;
    }

    // =====================================================================
    // DATA
    // =====================================================================

    public void taiDuLieu() {
        new SwingWorker<Void, Void>() {
            List<ThongBaoBroadcast> broadcasts = new ArrayList<>();
            List<ThongBao> thongBaos = new ArrayList<>();

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    broadcasts = adminConfigService.layBroadcastDangHieuLuc();
                } catch (Exception ignored) {
                    broadcasts = new ArrayList<>();
                }
                thongBaos = thongBaoService.layTatCaChoSinhVien(taiKhoan);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    cacheBroadcast = broadcasts != null ? broadcasts : new ArrayList<>();
                    cacheThongBao = thongBaos != null ? thongBaos : new ArrayList<>();
                    int chua = 0;
                    for (ThongBao tb : cacheThongBao) if (!tb.isDaDoc()) chua++;
                    statTong.setText(String.valueOf(cacheThongBao.size() + cacheBroadcast.size()));
                    statChuaDoc.setText(String.valueOf(chua));
                    statTruong.setText(String.valueOf(cacheBroadcast.size()));
                    veDanhSach();
                    if (onBadgeChanged != null) onBadgeChanged.run();
                } catch (Exception ex) {
                    listPanel.removeAll();
                    listPanel.add(emptyBox("Không tải được dữ liệu.\n" + ex.getMessage()));
                    listPanel.revalidate();
                    listPanel.repaint();
                }
            }
        }.execute();
    }

    private void veDanhSach() {
        listPanel.removeAll();
        selectedRow = null;
        int dem = 0;

        // Tin toan truong
        if (!chiChuaDoc) {
            boolean header = false;
            for (ThongBaoBroadcast b : cacheBroadcast) {
                if (!match(b.getTieuDe(), b.getNoiDung())) continue;
                if (!header) {
                    listPanel.add(sectionLabel("TIN TOÀN TRƯỜNG"));
                    header = true;
                }
                listPanel.add(rowBroadcast(b));
                dem++;
            }
        }

        // Thong bao ca nhan
        if (!chiTinTruong) {
            boolean header = false;
            for (ThongBao tb : cacheThongBao) {
                if (chiChuaDoc && tb.isDaDoc()) continue;
                if (!match(tb.getTieuDe(), tb.getNoiDung())) continue;
                if (!header) {
                    listPanel.add(sectionLabel(chiChuaDoc ? "CHƯA ĐỌC" : "THÔNG BÁO CÁ NHÂN"));
                    header = true;
                }
                listPanel.add(rowThongBao(tb));
                dem++;
            }
        }

        if (dem == 0) {
            listPanel.add(emptyBox(chiChuaDoc
                    ? "Không còn thông báo chưa đọc."
                    : chiTinTruong
                    ? "Chưa có tin tức toàn trường."
                    : tuKhoa.isEmpty()
                    ? "Hộp thư trống.\nKhi nhà trường gửi thông báo, bạn sẽ thấy tại đây."
                    : "Không tìm thấy kết quả phù hợp."));
        }

        lblCount.setText(dem + " mục");
        listPanel.add(Box.createVerticalGlue());
        listPanel.revalidate();
        listPanel.repaint();
    }

    private boolean match(String tieuDe, String noiDung) {
        if (tuKhoa == null || tuKhoa.isEmpty()) return true;
        String a = tieuDe != null ? tieuDe.toLowerCase() : "";
        String b = noiDung != null ? noiDung.toLowerCase() : "";
        return a.contains(tuKhoa) || b.contains(tuKhoa);
    }

    // =====================================================================
    // LIST ROWS
    // =====================================================================

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(MUTED);
        l.setBorder(new EmptyBorder(12, 14, 4, 14));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return l;
    }

    private JPanel rowThongBao(ThongBao tb) {
        boolean unread = !tb.isDaDoc();
        LoaiTin loai = phanLoai(tb.getManHinhKey(), tb.getTieuDe());

        JPanel row = baseRow(unread ? UNREAD : BG_CARD);
        row.add(iconBox(loai.mau, loai), BorderLayout.WEST);
        row.add(textBlock(
                safe(tb.getTieuDe(), "(Không tiêu đề)"),
                safe(tb.getNoiDung(), ""),
                loai.nhan + "  ·  " + relativeTime(tb.getThoiGianTao()),
                unread
        ), BorderLayout.CENTER);
        if (unread) row.add(unreadDot(), BorderLayout.EAST);

        row.addMouseListener(clickRow(row, () -> {
            showDetailThongBao(tb, loai);
            if (unread) markOne(tb);
        }, unread));
        return row;
    }

    private JPanel rowBroadcast(ThongBaoBroadcast b) {
        Color muc = switch (b.getMucDo() != null ? b.getMucDo() : "THONG_TIN") {
            case "KHAN_CAP" -> C_DANGER;
            case "CANH_BAO" -> C_WARN;
            default -> C_TRUONG;
        };
        String nhan = switch (b.getMucDo() != null ? b.getMucDo() : "THONG_TIN") {
            case "KHAN_CAP" -> "Khẩn cấp";
            case "CANH_BAO" -> "Cảnh báo";
            default -> "Tin trường";
        };

        JPanel row = baseRow(BG_CARD);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 1, 0, muc),
                new EmptyBorder(12, 12, 12, 12)));
        row.add(iconBox(muc, LoaiTin.TRUONG), BorderLayout.WEST);
        row.add(textBlock(
                safe(b.getTieuDe(), "(Không tiêu đề)"),
                safe(b.getNoiDung(), ""),
                nhan + "  ·  " + relativeTime(b.getHienThiTu()),
                true
        ), BorderLayout.CENTER);

        row.addMouseListener(clickRow(row, () -> showDetailBroadcast(b, nhan, muc), false));
        return row;
    }

    private JPanel baseRow(Color bg) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        row.setBackground(bg);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(12, 14, 12, 12)));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return row;
    }

    private JPanel textBlock(String title, String preview, String meta, boolean bold) {
        JPanel mid = new JPanel();
        mid.setOpaque(false);
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(ellipsis(title, 44));
        t.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 13));
        t.setForeground(TEXT);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel p = new JLabel(ellipsis(preview, 50));
        p.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.setForeground(MUTED);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(new EmptyBorder(3, 0, 0, 0));

        JLabel m = new JLabel(meta);
        m.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        m.setForeground(new Color(0x94, 0xA3, 0xB8));
        m.setAlignmentX(Component.LEFT_ALIGNMENT);
        m.setBorder(new EmptyBorder(3, 0, 0, 0));

        mid.add(t);
        mid.add(p);
        mid.add(m);
        return mid;
    }

    private JPanel unreadDot() {
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        wrap.setPreferredSize(new Dimension(14, 40));
        JComponent d = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_PRIMARY);
                g2.fillOval(3, (getHeight() - 8) / 2, 8, 8);
                g2.dispose();
            }
        };
        d.setPreferredSize(new Dimension(14, 40));
        wrap.add(d);
        return wrap;
    }

    private MouseAdapter clickRow(JPanel row, Runnable onClick, boolean unread) {
        return new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (selectedRow != null) selectedRow.setBackground(BG_CARD);
                selectedRow = row;
                row.setBackground(SELECT);
                onClick.run();
            }
            @Override public void mouseEntered(MouseEvent e) {
                if (row != selectedRow) row.setBackground(unread ? SELECT : new Color(0xF8, 0xFA, 0xFC));
            }
            @Override public void mouseExited(MouseEvent e) {
                if (row != selectedRow) row.setBackground(unread ? UNREAD : BG_CARD);
            }
        };
    }

    // =====================================================================
    // DETAIL PANEL
    // =====================================================================

    private void showPlaceholder() {
        detailPanel.removeAll();
        JPanel empty = new JPanel(new GridBagLayout());
        empty.setBackground(BG_CARD);
        JLabel l = new JLabel("<html><div style='text-align:center;color:#94A3B8;line-height:1.8'>"
                + "<div style='font-size:34px;margin-bottom:12px'>📬</div>"
                + "<b style='color:#64748B;font-size:14px'>Chọn thông báo để xem chi tiết</b><br>"
                + "Nội dung đầy đủ và nút mở trang liên quan sẽ hiện tại đây"
                + "</div></html>", SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        empty.add(l);
        detailPanel.add(empty, BorderLayout.CENTER);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private void showDetailThongBao(ThongBao tb, LoaiTin loai) {
        detailPanel.removeAll();

        JPanel head = new JPanel();
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setBackground(BG_CARD);
        head.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(20, 24, 16, 24)));

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chips.setOpaque(false);
        chips.setAlignmentX(Component.LEFT_ALIGNMENT);
        chips.add(chip(loai.nhan, loai.mau));
        chips.add(chip(tb.isDaDoc() ? "Đã đọc" : "Chưa đọc",
                tb.isDaDoc() ? C_THANHTOAN : C_DANGER));

        JLabel title = new JLabel("<html>" + escape(safe(tb.getTieuDe(), "")) + "</html>");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(new EmptyBorder(12, 0, 6, 0));

        JLabel meta = new JLabel(
                (tb.getThoiGianTao() != null ? FMT.format(tb.getThoiGianTao()) : "")
                        + "   ·   " + relativeTime(tb.getThoiGianTao())
                        + "   ·   Gửi tới sinh viên");
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        meta.setForeground(MUTED);
        meta.setAlignmentX(Component.LEFT_ALIGNMENT);

        head.add(chips);
        head.add(title);
        head.add(meta);

        JTextArea body = new JTextArea(safe(tb.getNoiDung(), ""));
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        body.setForeground(TEXT);
        body.setBackground(BG_CARD);
        body.setBorder(new EmptyBorder(20, 24, 20, 24));
        JScrollPane sp = new JScrollPane(body);
        sp.setBorder(null);
        sp.getViewport().setBackground(BG_CARD);

        JPanel foot = new JPanel(new BorderLayout());
        foot.setBackground(FOOT_BG);
        foot.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(12, 20, 12, 20)));
        JLabel hint = new JLabel("Hệ thống Quản lý Học phí & Công nợ Sinh viên");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(new Color(0x94, 0xA3, 0xB8));
        foot.add(hint, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        String dich = loai.manHinh != null ? loai.manHinh : mapManHinh(tb.getManHinhKey());
        if (dich != null && onDieuHuong != null) {
            JButton go = primaryBtn("Mở trang " + loai.nhan + "  →");
            go.setBackground(loai.mau); // mau nut theo loai – diem nhan
            final String target = dich;
            go.addActionListener(e -> onDieuHuong.accept(target));
            btns.add(go);
        }
        foot.add(btns, BorderLayout.EAST);

        detailPanel.add(head, BorderLayout.NORTH);
        detailPanel.add(sp, BorderLayout.CENTER);
        detailPanel.add(foot, BorderLayout.SOUTH);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private void showDetailBroadcast(ThongBaoBroadcast b, String nhan, Color muc) {
        detailPanel.removeAll();

        JPanel head = new JPanel();
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setBackground(BG_CARD);
        head.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(20, 24, 16, 24)));

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chips.setOpaque(false);
        chips.setAlignmentX(Component.LEFT_ALIGNMENT);
        chips.add(chip(nhan, muc));
        chips.add(chip("Toàn trường", C_PRIMARY));

        JLabel title = new JLabel("<html>" + escape(safe(b.getTieuDe(), "")) + "</html>");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(new EmptyBorder(12, 0, 6, 0));

        String metaStr = (b.getHienThiTu() != null ? FMT.format(b.getHienThiTu()) : "")
                + (b.getNguoiTao() != null ? "   ·   Người gửi: " + b.getNguoiTao()
                : "   ·   Ban giám hiệu / Phòng đào tạo");
        JLabel meta = new JLabel(metaStr);
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        meta.setForeground(MUTED);
        meta.setAlignmentX(Component.LEFT_ALIGNMENT);

        head.add(chips);
        head.add(title);
        head.add(meta);

        JTextArea body = new JTextArea(safe(b.getNoiDung(), ""));
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        body.setForeground(TEXT);
        body.setBackground(BG_CARD);
        body.setBorder(new EmptyBorder(20, 24, 20, 24));
        JScrollPane sp = new JScrollPane(body);
        sp.setBorder(null);

        detailPanel.add(head, BorderLayout.NORTH);
        detailPanel.add(sp, BorderLayout.CENTER);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    // =====================================================================
    // PHAN LOAI + MAU DIEM NHAN
    // =====================================================================

    private enum LoaiTin {
        HOADON("Hóa đơn", C_HOADON, "hoadon_sv"),
        THANHTOAN("Thanh toán", C_THANHTOAN, "lichsu_sv"),
        VI("Ví học phí", C_VI, "vidientu"),
        CONGNO("Công nợ", C_CONGNO, "tongquan"),
        TINCHI("Tín chỉ", C_TINCHI, "tinchi"),
        CHATBOT("Trợ lý AI", C_CHATBOT, "chatbot"),
        CANHAN("Cá nhân", C_CANHAN, "thongtin"),
        TRUONG("Tin trường", C_TRUONG, null),
        KHAC("Thông báo", MUTED, null);

        final String nhan;
        final Color mau;
        final String manHinh;

        LoaiTin(String nhan, Color mau, String manHinh) {
            this.nhan = nhan;
            this.mau = mau;
            this.manHinh = manHinh;
        }
    }

    private LoaiTin phanLoai(String key, String tieuDe) {
        String k = key != null ? key.toLowerCase() : "";
        String t = tieuDe != null ? tieuDe.toLowerCase() : "";
        if (k.contains("hoadon") || t.contains("hóa đơn") || t.contains("hoa don")) return LoaiTin.HOADON;
        if (k.contains("lichsu") || k.contains("thanhtoan") || t.contains("thanh toán") || t.contains("nộp"))
            return LoaiTin.THANHTOAN;
        if (k.contains("vidientu") || t.contains("ví")) return LoaiTin.VI;
        if (k.contains("congno") || t.contains("công nợ") || t.contains("cảnh báo nợ") || t.contains("quá hạn"))
            return LoaiTin.CONGNO;
        if (k.contains("tinchi") || t.contains("tín chỉ") || t.contains("học kỳ")) return LoaiTin.TINCHI;
        if (k.contains("chatbot") || t.contains("chatbot") || t.contains("trợ lý")) return LoaiTin.CHATBOT;
        if (k.contains("thongtin") || t.contains("email") || t.contains("cập nhật thông tin")) return LoaiTin.CANHAN;
        return LoaiTin.KHAC;
    }

    private String mapManHinh(String key) {
        if (key == null) return null;
        return switch (key.toLowerCase()) {
            case "hoadon", "hocphi", "hoadon_sv" -> "hoadon_sv";
            case "thanhtoan", "lichsu", "lichsu_sv" -> "lichsu_sv";
            case "vidientu", "vi" -> "vidientu";
            case "tinchi", "hocky" -> "tinchi";
            case "thongtin", "canhan" -> "thongtin";
            case "chatbot", "bot" -> "chatbot";
            case "congno", "tongquan" -> "tongquan";
            default -> null;
        };
    }

    // =====================================================================
    // WIDGETS
    // =====================================================================

    private JComponent iconBox(Color bg, LoaiTin loai) {
        return new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), 230));
                g2.fillRoundRect(0, 0, 40, 40, 10, 10);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                switch (loai) {
                    case HOADON -> {
                        g2.drawRoundRect(10, 9, 20, 22, 3, 3);
                        g2.drawLine(14, 20, 26, 20);
                        g2.drawLine(14, 26, 24, 26);
                    }
                    case THANHTOAN, VI -> {
                        g2.drawRoundRect(9, 14, 22, 14, 4, 4);
                        g2.fillRect(9, 18, 22, 3);
                    }
                    case CONGNO -> {
                        g2.drawOval(10, 10, 20, 20);
                        g2.drawLine(20, 16, 20, 24);
                        g2.fillOval(19, 26, 3, 3);
                    }
                    case TINCHI -> {
                        g2.drawLine(12, 30, 12, 12);
                        g2.drawLine(20, 30, 20, 10);
                        g2.drawLine(28, 30, 28, 14);
                    }
                    case TRUONG -> {
                        g2.drawLine(10, 18, 20, 10);
                        g2.drawLine(30, 18, 20, 10);
                        g2.drawRect(12, 18, 16, 12);
                    }
                    default -> {
                        g2.drawOval(10, 10, 20, 20);
                        g2.drawLine(20, 16, 20, 22);
                        g2.fillOval(19, 25, 3, 3);
                    }
                }
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(40, 40); }
        };
    }

    private JLabel chip(String text, Color bg) {
        JLabel l = new JLabel(" " + text + " ");
        l.setOpaque(true);
        l.setBackground(bg);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setBorder(new EmptyBorder(3, 8, 3, 8));
        return l;
    }

    private void styleTab(JToggleButton b) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(100, 34));
        b.addChangeListener(e -> {
            b.setOpaque(true);
            if (b.isSelected()) {
                b.setBackground(C_PRIMARY);
                b.setForeground(Color.WHITE);
            } else {
                b.setBackground(new Color(0xF1, 0xF5, 0xF9));
                b.setForeground(TEXT);
            }
        });
        b.setOpaque(true);
        b.setBackground(new Color(0xF1, 0xF5, 0xF9));
        b.setForeground(TEXT);
    }

    private JButton primaryBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(C_PRIMARY);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorder(new EmptyBorder(8, 14, 8, 14));
        return b;
    }

    private JButton softBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(new Color(0xF1, 0xF5, 0xF9));
        b.setForeground(TEXT);
        b.setOpaque(true);
        b.setBorder(new EmptyBorder(8, 14, 8, 14));
        return b;
    }

    private JPanel emptyBox(String msg) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(300, 160));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel("<html><div style='text-align:center;color:#94A3B8;line-height:1.6'>"
                + msg.replace("\n", "<br>") + "</div></html>", SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(l);
        return p;
    }

    private void markOne(ThongBao tb) {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                thongBaoService.danhDauDaDocMot(taiKhoan, tb.getMaThongBao());
                return null;
            }
            @Override protected void done() { taiDuLieu(); }
        }.execute();
    }

    private void danhDauTatCa() {
        if (JOptionPane.showConfirmDialog(this,
                "Đánh dấu tất cả thông báo là đã đọc?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                thongBaoService.danhDauTatCaDaDocSinhVien(taiKhoan);
                return null;
            }
            @Override protected void done() { taiDuLieu(); }
        }.execute();
    }

    private static String relativeTime(LocalDateTime t) {
        if (t == null) return "";
        long phut = ChronoUnit.MINUTES.between(t, LocalDateTime.now());
        if (phut < 1) return "Vừa xong";
        if (phut < 60) return phut + " phút trước";
        long gio = ChronoUnit.HOURS.between(t, LocalDateTime.now());
        if (gio < 24) return gio + " giờ trước";
        long ngay = ChronoUnit.DAYS.between(t, LocalDateTime.now());
        if (ngay < 7) return ngay + " ngày trước";
        return FMT.format(t);
    }

    private static String ellipsis(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private static String safe(String s, String def) {
        return s == null || s.isBlank() ? def : s;
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}