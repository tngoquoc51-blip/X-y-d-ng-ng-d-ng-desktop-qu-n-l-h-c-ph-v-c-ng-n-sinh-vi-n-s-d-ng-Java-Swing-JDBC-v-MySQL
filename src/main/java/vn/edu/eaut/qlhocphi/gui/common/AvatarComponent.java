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
                if (f.exists()) anh = ImageIO.read(f);
            } catch (IOException ignored) {
                // Neu doc anh loi, cu de anh = null de fallback ve chu cai dau ten
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Ellipse2D clip = new Ellipse2D.Float(0, 0, getWidth(), getHeight());

        if (anh != null) {
            g2.setClip(clip);
            g2.drawImage(anh, 0, 0, getWidth(), getHeight(), null);
            g2.setClip(null);
        } else {
            g2.setColor(new Color(255, 255, 255, 55));
            g2.fill(clip);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(12, size / 2 - 4)));
            String chu = (hoTen == null || hoTen.isBlank()) ? "?" : hoTen.trim().substring(0, 1).toUpperCase();
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(chu)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(chu, x, y);
        }

        g2.setColor(new Color(255, 255, 255, 160));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(new Ellipse2D.Float(1, 1, getWidth() - 2, getHeight() - 2));
        g2.dispose();
    }
}