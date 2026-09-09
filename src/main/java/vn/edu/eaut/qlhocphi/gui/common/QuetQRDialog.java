package vn.edu.eaut.qlhocphi.gui.common;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.util.QrCodeUtils;
import vn.edu.eaut.qlhocphi.util.TheSinhVienQrUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Dialog dung chung: bat camera, LIEN TUC quet tung frame tim ma QR (VD in tren
 * the sinh vien). Ngay khi doc duoc 1 ma QR hop le, dialog tu dong dong lai va
 * tra ve noi dung da doc qua layKetQua(). Co nut du phong "Tai anh QR len" cho
 * may khong co webcam hoac muon quet tu anh chup san.
 *
 * Cach dung:
 *   QuetQRDialog dlg = new QuetQRDialog(chaMe, "Quet the sinh vien");
 *   dlg.setVisible(true);           // dialog la modal, dung o day toi khi dong
 *   String maSV = dlg.layKetQua();  // null neu nguoi dung bam Huy / dong cua so
 */
public class QuetQRDialog extends JDialog {

    private static final int CHU_KY_QUET_MS = 350; // quet 1 frame moi 350ms - du muot, khong ngop CPU

    private Webcam webcam;
    private javax.swing.Timer timerQuet;
    private BufferedImage frameHienTai;
    private String ketQua;

    private JPanel oXemTruoc;
    private JLabel lblTrangThai;

    public QuetQRDialog(Window chaMe, String tieuDe) {
        super(chaMe, tieuDe, ModalityType.APPLICATION_MODAL);
        setSize(480, 460);
        setMinimumSize(new Dimension(420, 400));
        setLocationRelativeTo(chaMe);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UITheme.BG_CARD);
        setLayout(new BorderLayout(0, 0));

        add(buildNoiDung(), BorderLayout.CENTER);
        add(buildHangNut(), BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { dongCamera(); }
        });

        moCamera();
    }

    private JPanel buildNoiDung() {
        JPanel wrap = new JPanel(new BorderLayout(0, 10));
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(16, 16, 8, 16));

        JLabel lblTieuDe = UITheme.sectionLabel("Dua ma QR vao khung hinh de quet");
        wrap.add(lblTieuDe, BorderLayout.NORTH);

        oXemTruoc = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                if (frameHienTai != null) {
                    // Ve anh camera vua theo ty le, canh giua khung xem truoc
                    double tyLe = Math.min((double) getWidth() / frameHienTai.getWidth(),
                            (double) getHeight() / frameHienTai.getHeight());
                    int w = (int) (frameHienTai.getWidth() * tyLe);
                    int h = (int) (frameHienTai.getHeight() * tyLe);
                    int x = (getWidth() - w) / 2, y = (getHeight() - h) / 2;
                    g2.drawImage(frameHienTai, x, y, w, h, null);
                } else {
                    g2.setColor(UITheme.TEXT_MUTED);
                    g2.setFont(UITheme.FONT_BASE);
                    String txt = "Dang mo camera...";
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2, getHeight() / 2);
                }
                // Khung vuong huong dan dat ma QR vao giua
                int kt = Math.min(getWidth(), getHeight()) - 40;
                g2.setColor(UITheme.PRIMARY);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect((getWidth() - kt) / 2, (getHeight() - kt) / 2, kt, kt, 18, 18);
                g2.dispose();
            }
        };
        oXemTruoc.setOpaque(true);
        oXemTruoc.setBackground(Color.BLACK);
        oXemTruoc.setPreferredSize(new Dimension(420, 300));
        oXemTruoc.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        wrap.add(oXemTruoc, BorderLayout.CENTER);

        lblTrangThai = new JLabel("Dang khoi dong camera...", SwingConstants.CENTER);
        lblTrangThai.setFont(UITheme.FONT_BASE);
        lblTrangThai.setForeground(UITheme.TEXT_MUTED);
        lblTrangThai.setBorder(new EmptyBorder(8, 0, 0, 0));
        wrap.add(lblTrangThai, BorderLayout.SOUTH);

        return wrap;
    }

    private JPanel buildHangNut() {
        JPanel hang = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        hang.setOpaque(false);
        hang.setBorder(new EmptyBorder(0, 16, 14, 16));

        JButton btnTaiAnh = UITheme.secondaryButton("Tai anh QR len");
        btnTaiAnh.addActionListener(e -> taiAnhLen());

        JButton btnHuy = UITheme.secondaryButton("Huy");
        btnHuy.addActionListener(e -> { dongCamera(); dispose(); });

        hang.add(btnTaiAnh);
        hang.add(btnHuy);
        return hang;
    }

    private void moCamera() {
        SwingWorker<Webcam, Void> worker = new SwingWorker<>() {
            @Override
            protected Webcam doInBackground() {
                try {
                    Webcam wc = Webcam.getDefault();
                    if (wc == null) return null;
                    try { wc.setViewSize(WebcamResolution.VGA.getSize()); } catch (Exception ignore) { /* dung kich thuoc mac dinh */ }
                    wc.open();
                    return wc;
                } catch (Exception ex) {
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    webcam = get();
                } catch (Exception ignored) {
                    webcam = null;
                }
                if (webcam == null) {
                    lblTrangThai.setText("Khong tim thay camera. Vui long bam 'Tai anh QR len' de quet tu anh co san.");
                    return;
                }
                lblTrangThai.setText("Dang quet...");
                timerQuet = new javax.swing.Timer(CHU_KY_QUET_MS, e -> quetMotFrame());
                timerQuet.start();
            }
        };
        worker.execute();
    }

    /** Chay tren EDT (Timer callback) nhung viec doc anh tu webcam co the hoi cham -> dua vao SwingWorker rieng tung lan de khong giat UI. */
    private void quetMotFrame() {
        if (webcam == null || !webcam.isOpen()) return;
        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() {
                try { return webcam.getImage(); } catch (Exception ex) { return null; }
            }

            @Override
            protected void done() {
                try {
                    BufferedImage anh = get();
                    if (anh == null) return;
                    frameHienTai = anh;
                    oXemTruoc.repaint();

                    String noiDung = QrCodeUtils.docQR(anh);
                    if (noiDung != null) {
                        ketQua = TheSinhVienQrUtils.trichMaSV(noiDung);
                        lblTrangThai.setText("Da quet duoc: " + ketQua);
                        dongCamera();
                        dispose();
                    }
                } catch (Exception ignored) {
                    // Bo qua frame loi, thu tiep frame sau
                }
            }
        };
        worker.execute();
    }

    private void taiAnhLen() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chon anh chua ma QR");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Anh (jpg, png, jpeg)", "jpg", "jpeg", "png"));
        int rs = fc.showOpenDialog(this);
        if (rs != JFileChooser.APPROVE_OPTION) return;

        try {
            BufferedImage anh = ImageIO.read(fc.getSelectedFile());
            String noiDung = anh != null ? QrCodeUtils.docQR(anh) : null;
            if (noiDung == null) {
                JOptionPane.showMessageDialog(this, "Khong tim thay ma QR trong anh nay. Vui long thu anh khac.",
                        "Khong doc duoc", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ketQua = TheSinhVienQrUtils.trichMaSV(noiDung);
            dongCamera();
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Loi doc anh: " + ex.getMessage(),
                    "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void dongCamera() {
        if (timerQuet != null) { timerQuet.stop(); timerQuet = null; }
        if (webcam != null && webcam.isOpen()) {
            new Thread(() -> { try { webcam.close(); } catch (Exception ignored) { } }).start();
        }
    }

    /** Noi dung ma QR da quet duoc, hoac null neu nguoi dung huy / dong cua so ma chua quet duoc gi. */
    public String layKetQua() {
        return ketQua;
    }
}