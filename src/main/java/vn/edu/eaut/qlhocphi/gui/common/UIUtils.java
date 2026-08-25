package vn.edu.eaut.qlhocphi.gui.common;

import vn.edu.eaut.qlhocphi.config.UITheme;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;

/** Ham tien ich dung chung cho cac man hinh GUI (tao bang, o nhap co nhan...). */
public class UIUtils {

    public static JTextField textField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(UITheme.FONT_BASE);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return tf;
    }

    public static JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(UITheme.FONT_BASE);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(0xE6, 0xF0, 0xFE));
        table.setSelectionForeground(UITheme.TEXT_PRIMARY);
        table.setGridColor(UITheme.BORDER);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        JTableHeader header = table.getTableHeader();
        header.setFont(UITheme.FONT_BOLD);
        header.setBackground(new Color(0xF0, 0xF3, 0xF9));
        header.setForeground(UITheme.TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(0, 36));
    }

    public static void thongBaoLoi(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Loi", JOptionPane.ERROR_MESSAGE);
    }

    public static void thongBao(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Thong bao", JOptionPane.INFORMATION_MESSAGE);
    }
}
