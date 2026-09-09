package vn.edu.eaut.qlhocphi.gui.common;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import vn.edu.eaut.qlhocphi.config.UITheme;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Dialog dung chung: chup 1 anh (bang webcam) HOAC tai 1 anh co san len, dung cho
 * cac tinh nang can "1 buc anh de xu ly AI" nhu OCR bien lai giay. Khac voi
 * QuetQRDialog (quet LIEN TUC tim QR), dialog nay CHUP 1 LAN theo y nguoi dung
 * bam nut, vi anh bien lai can nguoi dung tu can chinh/lay net truoc khi chup.
 *
 * Cach dung:
 *   ChupAnhDialog dlg = new ChupAnhDialog(chaMe, "Chup anh bien lai");
 *   dlg.setVisible(true);
 *   BufferedImage anh = dlg.layAnhDaChon();  // null neu nguoi dung huy
 */
public class ChupAnhDialog extends JDialog {

    private Webcam webcam;
    private javax.swing.Timer timerXemTruoc;
    private BufferedImage frameHienTai;   // frame dang xem truoc (lien tuc cap nhat)
    private BufferedImage anhDaChon;      // anh nguoi dung da chot (chup hoac tai len)

    private JPanel oXemTruoc;
    private JLabel lblTrangThai;
    private JButton btnChup;
    private JButton btnDungAnh, btnChupLai;

    public ChupAnhDialog(Window chaMe, String tieuDe) {
        super(chaMe, tieuDe, ModalityType.APPLICATION_MODAL);
        setSize(520, 500);
        setMinimumSize(new Dimension(460, 440));
        setLocationRelativeTo(chaMe);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UITheme.BG_CARD);
        setLayout(new BorderLayout());

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
        wrap.add(UITheme.sectionLabel("Dat bien lai vao khung hinh, giu phang va du sang"), BorderLayout.NORTH);

        oXemTruoc = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                BufferedImage anhVe = anhDaChon != null ? anhDaChon : frameHienTai;
                if (anhVe != null) {
                    double tyLe = Math.min((double) getWidth() / anhVe.getWidth(), (double) getHeight() / anhVe.getHeight());
                    int w = (int) (anhVe.getWidth() * tyLe);
                    int h = (int) (anhVe.getHeight() * tyLe);
                    g2.drawImage(anhVe, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
                } else {
                    g2.setColor(UITheme.TEXT_MUTED);
                    g2.setFont(UITheme.FONT_BASE);
                    String txt = "Dang mo camera...";
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2, getHeight() / 2);
                }
                g2.dispose();
            }
        };
        oXemTruoc.setOpaque(true);
        oXemTruoc.setBackground(Color.BLACK);
        oXemTruoc.setPreferredSize(new Dimension(460, 340));
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

        JButton btnTaiAnh = UITheme.secondaryButton("Tai anh len");
        btnTaiAnh.addActionListener(e -> taiAnhLen());

        btnChup = UITheme.primaryButton("Chup anh");
        btnChup.setEnabled(false);
        btnChup.addActionListener(e -> chupAnh());

        btnChupLai = UITheme.secondaryButton("Chup lai");
        btnChupLai.setVisible(false);
        btnChupLai.addActionListener(e -> chupLai());

        btnDungAnh = UITheme.primaryButton("Dung anh nay");
        btnDungAnh.setVisible(false);
        btnDungAnh.addActionListener(e -> { dongCamera(); dispose(); });

        JButton btnHuy = UITheme.secondaryButton("Huy");
        btnHuy.addActionListener(e -> { anhDaChon = null; dongCamera(); dispose(); });

        hang.add(btnTaiAnh);
        hang.add(btnChup);
        hang.add(btnChupLai);
        hang.add(btnDungAnh);
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
                    try { wc.setViewSize(WebcamResolution.VGA.getSize()); } catch (Exception ignore) { }
                    wc.open();
                    return wc;
                } catch (Exception ex) {
                    return null;
                }
            }

            @Override
            protected void done() {
                try { webcam = get(); } catch (Exception ignored) { webcam = null; }
                if (webcam == null) {
                    lblTrangThai.setText("Khong tim thay camera. Vui long bam 'Tai anh len' de chon anh da chup san.");
                    return;
                }
                lblTrangThai.setText("San sang - bam 'Chup anh' khi da can chinh xong");
                btnChup.setEnabled(true);
                timerXemTruoc = new javax.swing.Timer(120, e -> capNhatXemTruoc());
                timerXemTruoc.start();
            }
        };
        worker.execute();
    }

    private void capNhatXemTruoc() {
        if (webcam == null || !webcam.isOpen() || anhDaChon != null) return;
        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() {
                try { return webcam.getImage(); } catch (Exception ex) { return null; }
            }
            @Override
            protected void done() {
                try {
                    BufferedImage anh = get();
                    if (anh != null) { frameHienTai = anh; oXemTruoc.repaint(); }
                } catch (Exception ignored) { }
            }
        };
        worker.execute();
    }

    private void chupAnh() {
        if (frameHienTai == null) return;
        anhDaChon = frameHienTai;
        if (timerXemTruoc != null) timerXemTruoc.stop();
        oXemTruoc.repaint();
        chuyenSangCheDoDaChup();
    }

    private void taiAnhLen() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chon anh bien lai");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Anh (jpg, png, jpeg)", "jpg", "jpeg", "png"));
        int rs = fc.showOpenDialog(this);
        if (rs != JFileChooser.APPROVE_OPTION) return;
        try {
            BufferedImage anh = ImageIO.read(fc.getSelectedFile());
            if (anh == null) {
                JOptionPane.showMessageDialog(this, "Khong doc duoc file anh nay.", "Loi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            anhDaChon = anh;
            if (timerXemTruoc != null) timerXemTruoc.stop();
            oXemTruoc.repaint();
            chuyenSangCheDoDaChup();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Loi doc anh: " + ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void chupLai() {
        anhDaChon = null;
        oXemTruoc.repaint();
        btnChup.setVisible(true);
        btnChup.setEnabled(webcam != null && webcam.isOpen());
        btnChupLai.setVisible(false);
        btnDungAnh.setVisible(false);
        lblTrangThai.setText(webcam != null && webcam.isOpen() ? "San sang - bam 'Chup anh' khi da can chinh xong"
                : "Vui long bam 'Tai anh len' de chon anh khac");
        if (webcam != null && webcam.isOpen() && timerXemTruoc != null) timerXemTruoc.start();
    }

    private void chuyenSangCheDoDaChup() {
        btnChup.setVisible(false);
        btnChupLai.setVisible(true);
        btnDungAnh.setVisible(true);
        lblTrangThai.setText("Da chon anh - bam 'Dung anh nay' de tiep tuc, hoac 'Chup lai' neu chua ung y");
    }

    private void dongCamera() {
        if (timerXemTruoc != null) { timerXemTruoc.stop(); timerXemTruoc = null; }
        if (webcam != null && webcam.isOpen()) {
            new Thread(() -> { try { webcam.close(); } catch (Exception ignored) { } }).start();
        }
    }

    /** Anh nguoi dung da chon (chup hoac tai len), hoac null neu bam Huy. */
    public BufferedImage layAnhDaChon() {
        return anhDaChon;
    }
}
