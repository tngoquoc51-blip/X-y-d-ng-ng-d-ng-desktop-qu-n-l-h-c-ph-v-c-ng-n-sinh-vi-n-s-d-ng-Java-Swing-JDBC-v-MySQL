package vn.edu.eaut.qlhocphi.gui.common;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
/**
 * Component avatar tron dung chung cho toan he thong Sinh vien: hien anh that
 * neu co (avatars/<maSV>.<duoi>), khong thi hien chu cai dau ten tren nen mau.
 * Dung chung giua ThongTinCaNhanPanel va SinhVienCongNoPanel de dam bao dong bo.
 */
public class AvatarComponent extends JComponent {
    private String hoTen;
    private BufferedImage anh;
    private final int size;

    public AvatarComponent(String hoTen, int size) {
        this.hoTen = hoTen;
        this.size = size;
        setPreferredSize(new Dimension(size, size));
        setOpaque(false);
    }

    public void capNhatTen(String hoTenMoi) {
        this.hoTen = hoTenMoi;
        repaint();
    }

    public void taiAnh(String duongDan) {
        anh = null;
        if (duongDan != null && !duongDan.isBlank()) {
            try {
                File f = new File(duongDan);
                // Path cũ / không tồn tại → tìm trong thư mục cố định
                if (!f.exists()) {
                    File base = new File(System.getProperty("user.home"),
                            ".qlhocphi" + File.separator + "avatars");
                    f = new File(base, new File(duongDan).getName());
                }
                if (f.exists()) {
                    anh = ImageIO.read(f);
                }
            } catch (IOException ignored) {
                // Đọc lỗi → hiện chữ cái đầu tên
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();
        // Chừa 2px cho viền + bóng
        float pad = 2f;
        Ellipse2D.Float oval = new Ellipse2D.Float(pad, pad, w - pad * 2, h - pad * 2);

        // Bóng nhẹ phía dưới
        g2.setColor(new Color(0, 0, 0, 35));
        g2.fill(new Ellipse2D.Float(pad + 1, pad + 2, w - pad * 2, h - pad * 2));

        if (anh != null) {
            // Crop giữa ảnh (cover) để không bị méo
            int iw = anh.getWidth();
            int ih = anh.getHeight();
            double scale = Math.max((double) (w - pad * 2) / iw, (double) (h - pad * 2) / ih);
            int dw = (int) Math.round(iw * scale);
            int dh = (int) Math.round(ih * scale);
            int dx = (int) Math.round(pad + ((w - pad * 2) - dw) / 2.0);
            int dy = (int) Math.round(pad + ((h - pad * 2) - dh) / 2.0);

            g2.setClip(oval);
            g2.drawImage(anh, dx, dy, dw, dh, null);
            g2.setClip(null);
        } else {
            g2.setColor(new Color(255, 255, 255, 55));
            g2.fill(oval);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(14, size / 2 - 2)));
            String chu = (hoTen == null || hoTen.isBlank()) ? "?"
                    : hoTen.trim().substring(0, 1).toUpperCase();
            FontMetrics fm = g2.getFontMetrics();
            int x = (w - fm.stringWidth(chu)) / 2;
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(chu, x, y);
        }

        // Viền trắng rõ
        g2.setStroke(new BasicStroke(2.5f));
        g2.setColor(new Color(255, 255, 255, 220));
        g2.draw(oval);

        g2.dispose();
    }
}