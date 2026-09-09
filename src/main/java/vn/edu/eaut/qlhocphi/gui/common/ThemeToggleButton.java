package vn.edu.eaut.qlhocphi.gui.common;

import vn.edu.eaut.qlhocphi.config.ThemeMode;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Cong tac chuyen doi Sang/Toi kieu "iOS switch" - ve tay bang Graphics2D,
 * khong can icon file ngoai. Bam vao se doi ThemeMode va goi callback de
 * MainFrame tu rebuild lai giao dien voi mau moi.
 */
public class ThemeToggleButton extends JComponent {
    private static final int RONG = 56, CAO = 28;
    private final Runnable khiDoiTheme;

    public ThemeToggleButton(Runnable khiDoiTheme) {
        this.khiDoiTheme = khiDoiTheme;
        setPreferredSize(new Dimension(RONG, CAO));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setToolTipText("Chuyen giao dien Sang / Toi");
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                ThemeMode.doiCheDo();
                repaint();
                if (khiDoiTheme != null) khiDoiTheme.run();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean toi = ThemeMode.layHienTai().laToi();

        Color nenTat = new Color(255, 255, 255, 60);
        Color nenBat = new Color(0x3B, 0x82, 0xF6);
        g2.setColor(toi ? nenBat : nenTat);
        g2.fill(new RoundRectangle2D.Float(0, 0, RONG, CAO, CAO, CAO));

        int duongKinhNut = CAO - 6;
        int xNut = toi ? RONG - duongKinhNut - 3 : 3;
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Float(xNut, 3, duongKinhNut, duongKinhNut));

        g2.setColor(toi ? nenBat : new Color(0xF5, 0x9E, 0x0B));
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        String icon = toi ? "\uD83C\uDF19" : "\u2600";
        FontMetrics fm = g2.getFontMetrics();
        int xIcon = toi ? 6 : RONG - fm.stringWidth(icon) - 6;
        int yIcon = (CAO - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(icon, xIcon, yIcon);

        g2.dispose();
    }
}