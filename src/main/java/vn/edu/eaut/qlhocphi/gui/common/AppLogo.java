package vn.edu.eaut.qlhocphi.gui.common;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * Logo he thong QL Hoc Phi.
 * - Uu tien load /logo.png trong resources
 * - Khong co anh thi ve vector
 */
public final class AppLogo {

    private static BufferedImage cachedImage;

    private AppLogo() {}

    public static JComponent taoHeader() {
        return tao(52);
    }

    public static JComponent taoSidebar() {
        return tao(44);
    }

    public static JComponent tao(int size) {
        JComponent c = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                BufferedImage img = loadImage();
                if (img != null) {
                    int pad = Math.max(1, size / 18);
                    g2.drawImage(img, pad, pad, getWidth() - pad * 2, getHeight() - pad * 2, null);
                } else {
                    veVector(g2, getWidth(), getHeight());
                }
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(size, size);
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        c.setOpaque(false);
        return c;
    }

    private static BufferedImage loadImage() {
        if (cachedImage != null) return cachedImage;
        try (InputStream in = AppLogo.class.getResourceAsStream("/logo.png")) {
            if (in != null) {
                cachedImage = ImageIO.read(in);
                return cachedImage;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /** Fallback ve vector neu khong co logo.png */
    private static void veVector(Graphics2D g0, int w, int h) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int s = Math.min(w, h);
        g.translate((w - s) / 2, (h - s) / 2);

        g.setPaint(new GradientPaint(0, 0, new Color(0x02, 0x84, 0xC7), s, s, new Color(0x38, 0xBD, 0xF8)));
        g.fillRoundRect(0, 0, s - 1, s - 1, s / 3, s / 3);

        // Sach don gian
        g.setColor(Color.WHITE);
        int bx = s / 5, by = s / 4, bw = s * 3 / 5, bh = s / 2;
        g.fillRoundRect(bx, by, bw / 2 - 2, bh, 6, 6);
        g.fillRoundRect(bx + bw / 2 + 2, by, bw / 2 - 2, bh, 6, 6);

        // Xu
        int cr = s / 6;
        int cx = s - cr - 4, cy = s - cr - 4;
        g.setColor(new Color(0xF5, 0x9E, 0x0B));
        g.fillOval(cx - cr, cy - cr, cr * 2, cr * 2);
        g.setColor(new Color(0x92, 0x40, 0x0E));
        g.setFont(new Font("Segoe UI", Font.BOLD, Math.max(10, cr)));
        FontMetrics fm = g.getFontMetrics();
        g.drawString("đ", cx - fm.stringWidth("đ") / 2, cy + fm.getAscent() / 3);

        g.dispose();
    }
}