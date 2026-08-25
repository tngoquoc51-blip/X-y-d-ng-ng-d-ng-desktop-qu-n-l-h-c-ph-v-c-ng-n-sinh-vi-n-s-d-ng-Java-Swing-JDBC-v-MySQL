package vn.edu.eaut.qlhocphi.gui.chatbot;

import vn.edu.eaut.qlhocphi.ai.ChatMessage;
import vn.edu.eaut.qlhocphi.ai.ChatbotService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Man hinh Tro ly AI (Chatbot) - ban nang cap "desktop": banner gradient dong
 * bo mau toan he thong, bong bong chat bo goc that su (ve bang Graphics2D)
 * kem avatar rieng cho nguoi dung/bot va gio gui, hang cau hoi goi y bam la
 * gui luon, va nut xoa hoi thoai. Goi ChatbotService.traLoi(...) trong
 * SwingWorker de khong treo giao dien khi cho phan hoi.
 */
public class ChatbotPanel extends JPanel {
    private final ChatbotService chatbotService = new ChatbotService();
    private static final DateTimeFormatter GIO = DateTimeFormatter.ofPattern("HH:mm");

    private static final String[] CAU_HOI_GOI_Y = {
            "Cong no cua SV001 con bao nhieu?",
            "Cach thanh toan hoc phi?",
            "Xin chao"
    };

    private JPanel khungHoiThoai;
    private JScrollPane scrollPane;
    private JTextField txtNhap;
    private JButton btnGui;

    public ChatbotPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildChatCard(), BorderLayout.CENTER);

        themTinNhan(new ChatMessage(ChatMessage.Nguon.CHATBOT,
                "Xin chao! Toi la tro ly ao ho tro tra cuu cong no va hoc phi. "
                        + "Ban co the hoi vi du: \"Cong no cua SV001 con bao nhieu?\""));
    }

    // ================== HEADER (banner + logo, dong bo cac trang khac) ==================

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
        JLabel tieuDe = new JLabel("Tro ly AI (Chatbot)");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        JLabel phu = new JLabel("Hoi dap nhanh ve cong no, hoc phi va cach thanh toan");
        phu.setFont(UITheme.FONT_BASE);
        phu.setForeground(new Color(255, 255, 255, 210));
        chuText.add(tieuDe);
        chuText.add(Box.createRigidArea(new Dimension(0, 4)));
        chuText.add(phu);
        trai.add(chuText, BorderLayout.CENTER);
        banner.add(trai, BorderLayout.WEST);

        JButton btnXoa = new JButton("Xoa hoi thoai");
        btnXoa.setFont(UITheme.FONT_BOLD);
        btnXoa.setBackground(Color.WHITE);
        btnXoa.setForeground(UITheme.PRIMARY_DARK);
        btnXoa.setFocusPainted(false);
        btnXoa.setBorderPainted(false);
        btnXoa.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnXoa.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnXoa.addActionListener(e -> xoaHoiThoai());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(btnXoa);
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
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
                FontMetrics fm = g2.getFontMetrics();
                String icon = "\uD83E\uDD16";
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

    private void xoaHoiThoai() {
        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Xoa toan bo hoi thoai hien tai?", "Xac nhan",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (xacNhan != JOptionPane.YES_OPTION) return;
        khungHoiThoai.removeAll();
        themTinNhan(new ChatMessage(ChatMessage.Nguon.CHATBOT,
                "Hoi thoai da duoc lam moi. Toi co the giup gi cho ban?"));
    }

    // ================== KHUNG CHAT ==================

    private JPanel buildChatCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 12));

        khungHoiThoai = new JPanel();
        khungHoiThoai.setLayout(new BoxLayout(khungHoiThoai, BoxLayout.Y_AXIS));
        khungHoiThoai.setBackground(Color.WHITE);
        khungHoiThoai.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        scrollPane = new JScrollPane(khungHoiThoai);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        card.add(scrollPane, BorderLayout.CENTER);

        JPanel duoiCung = new JPanel();
        duoiCung.setOpaque(false);
        duoiCung.setLayout(new BoxLayout(duoiCung, BoxLayout.Y_AXIS));

        // Hang cau hoi goi y - bam la gui luon, khong can go tay
        JPanel hangGoiY = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        hangGoiY.setOpaque(false);
        for (String cauHoi : CAU_HOI_GOI_Y) {
            hangGoiY.add(taoChipGoiY(cauHoi));
        }
        duoiCung.add(hangGoiY);
        duoiCung.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);
        txtNhap = UIUtils.textField(20);
        txtNhap.setToolTipText("Nhap cau hoi, VD: Cong no cua SV001 con bao nhieu?");
        btnGui = UITheme.primaryButton("Gui");

        txtNhap.addActionListener(e -> guiCauHoi(txtNhap.getText()));
        btnGui.addActionListener(e -> guiCauHoi(txtNhap.getText()));

        inputRow.add(txtNhap, BorderLayout.CENTER);
        inputRow.add(btnGui, BorderLayout.EAST);
        duoiCung.add(inputRow);

        card.add(duoiCung, BorderLayout.SOUTH);

        return card;
    }

    /** Chip bo tron mau tim nhat - bam vao la gui thang cau hoi goi y, khong can go tay. */
    private JButton taoChipGoiY(String cauHoi) {
        JButton chip = new JButton(cauHoi) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? UITheme.TEXT_VIOLET : UITheme.TINT_VIOLET);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chip.setForeground(UITheme.TEXT_VIOLET);
        chip.setContentAreaFilled(false);
        chip.setBorderPainted(false);
        chip.setFocusPainted(false);
        chip.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        chip.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chip.addActionListener(e -> guiCauHoi(cauHoi));
        return chip;
    }

    // ================== GUI / NHAN TIN NHAN ==================

    private void guiCauHoi(String cauHoiTho) {
        String cauHoi = cauHoiTho.trim();
        if (cauHoi.isEmpty()) return;

        themTinNhan(new ChatMessage(ChatMessage.Nguon.NGUOI_DUNG, cauHoi));
        txtNhap.setText("");
        txtNhap.setEnabled(false);
        btnGui.setEnabled(false);

        JPanel dongDangTra = themDongDangTraLoi();

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return chatbotService.traLoi(cauHoi);
            }

            @Override
            protected void done() {
                khungHoiThoai.remove(dongDangTra);
                try {
                    String traLoi = get();
                    themTinNhan(new ChatMessage(ChatMessage.Nguon.CHATBOT, traLoi));
                } catch (Exception ex) {
                    themTinNhan(new ChatMessage(ChatMessage.Nguon.CHATBOT,
                            "Xin loi, da co loi xay ra khi xu ly cau hoi cua ban."));
                }
                txtNhap.setEnabled(true);
                btnGui.setEnabled(true);
                txtNhap.requestFocusInWindow();
            }
        };
        worker.execute();
    }

    private JPanel themDongDangTraLoi() {
        JPanel dong = dongTinNhan(false, "Dang tra loi...", null);
        khungHoiThoai.add(dong);
        khungHoiThoai.add(Box.createRigidArea(new Dimension(0, 10)));
        lamMoiCuon();
        return dong;
    }

    private void themTinNhan(ChatMessage msg) {
        boolean nguoiDung = msg.getNguon() == ChatMessage.Nguon.NGUOI_DUNG;
        String gio = msg.getThoiGian() != null ? msg.getThoiGian().format(GIO) : "";
        JPanel dong = dongTinNhan(nguoiDung, msg.getNoiDung(), gio);
        khungHoiThoai.add(dong);
        khungHoiThoai.add(Box.createRigidArea(new Dimension(0, 10)));
        lamMoiCuon();
    }

    /** 1 dong chat hoan chinh: avatar + bong bong bo goc + gio, can trai (bot) hoac phai (nguoi dung). */
    private JPanel dongTinNhan(boolean nguoiDung, String noiDung, String gio) {
        JPanel dong = new JPanel(new FlowLayout(nguoiDung ? FlowLayout.RIGHT : FlowLayout.LEFT, 8, 0));
        dong.setOpaque(false);
        dong.setAlignmentX(Component.LEFT_ALIGNMENT);
        dong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JComponent avatar = nguoiDung ? avatarNguoiDung() : avatarBot();

        JPanel cot = new JPanel();
        cot.setOpaque(false);
        cot.setLayout(new BoxLayout(cot, BoxLayout.Y_AXIS));

        RoundedBubble bubble = new RoundedBubble(nguoiDung ? UITheme.PRIMARY : UITheme.BG_MAIN);
        bubble.setLayout(new BorderLayout());
        bubble.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        bubble.setAlignmentX(nguoiDung ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        JLabel label = new JLabel("<html><body style='width: 300px'>" + escapeHtml(noiDung) + "</body></html>");
        label.setFont(UITheme.FONT_BASE);
        label.setForeground(nguoiDung ? Color.WHITE : UITheme.TEXT_PRIMARY);
        bubble.add(label, BorderLayout.CENTER);
        cot.add(bubble);

        if (gio != null && !gio.isEmpty()) {
            JLabel lblGio = new JLabel(gio);
            lblGio.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblGio.setForeground(UITheme.TEXT_MUTED);
            lblGio.setBorder(BorderFactory.createEmptyBorder(3, nguoiDung ? 0 : 4, 0, nguoiDung ? 4 : 0));
            lblGio.setAlignmentX(nguoiDung ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
            cot.add(lblGio);
        }

        if (nguoiDung) {
            dong.add(cot);
            dong.add(avatar);
        } else {
            dong.add(avatar);
            dong.add(cot);
        }
        return dong;
    }

    private JComponent avatarBot() {
        return avatarTron("\uD83E\uDD16", UITheme.TEXT_VIOLET);
    }

    private JComponent avatarNguoiDung() {
        return avatarTron("\uD83D\uDC64", UITheme.PRIMARY);
    }

    private JComponent avatarTron(String icon, Color mauNen) {
        JComponent avatar = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(mauNen);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(icon)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(icon, x, y);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(32, 32));
        avatar.setOpaque(false);
        return avatar;
    }

    /** Bong bong chat bo goc that su, ve bang Graphics2D thay vi JLabel nen vuong nhu ban cu. */
    private static class RoundedBubble extends JPanel {
        private final Color mauNen;
        RoundedBubble(Color mauNen) { this.mauNen = mauNen; setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(mauNen);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }

    private void lamMoiCuon() {
        khungHoiThoai.revalidate();
        khungHoiThoai.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar vBar = scrollPane.getVerticalScrollBar();
            vBar.setValue(vBar.getMaximum());
        });
    }
}