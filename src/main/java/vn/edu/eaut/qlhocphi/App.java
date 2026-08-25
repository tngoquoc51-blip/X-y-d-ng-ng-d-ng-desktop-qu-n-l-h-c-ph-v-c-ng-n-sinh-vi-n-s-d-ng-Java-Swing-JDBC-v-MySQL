package vn.edu.eaut.qlhocphi;

import vn.edu.eaut.qlhocphi.config.AppConfig;
import vn.edu.eaut.qlhocphi.config.DBConnection;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.LoginFrame;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Diem khoi dong (entry point) cua ung dung Quan ly hoc phi va cong no sinh vien.
 *
 * Trinh tu khoi dong:
 * 1) Ap dung UITheme (mau sac, font) cho toan bo giao dien Swing.
 * 2) Kiem tra ket noi CSDL truoc, neu loi thi bao ro va thoat (tranh mo LoginFrame
 *    roi moi bao loi kho hieu khi bam dang nhap).
 * 3) Mo LoginFrame. Sau khi dang nhap thanh cong, LoginFrame se tu mo MainFrame.
 */
public class App {

    public static void main(String[] args) {
        // UI phai duoc ap dung tren Event Dispatch Thread truoc khi tao bat ky JFrame nao
        SwingUtilities.invokeLater(() -> {
            UITheme.apply();

            if (!kiemTraKetNoiCSDL()) {
                return; // da bao loi cho nguoi dung, khong mo tiep giao dien
            }

            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setLocationRelativeTo(null);
            loginFrame.setVisible(true);
        });
    }

    /**
     * Kiem tra ket noi toi MySQL truoc khi mo giao dien.
     * Neu that bai, hien thi thong bao loi kem huong dan (kiem tra application.properties)
     * va tra ve false de dung qua trinh khoi dong.
     */
    private static boolean kiemTraKetNoiCSDL() {
        try (Connection conn = DBConnection.getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            String dbUrl = AppConfig.get("db.url");
            JOptionPane.showMessageDialog(
                    null,
                    "Khong the ket noi toi co so du lieu.\n\n"
                            + "URL: " + dbUrl + "\n"
                            + "Loi: " + e.getMessage() + "\n\n"
                            + "Vui long kiem tra:\n"
                            + "- MySQL da duoc khoi dong chua?\n"
                            + "- Da chay file database/schema.sql chua?\n"
                            + "- Thong tin db.username / db.password trong\n"
                            + "  src/main/resources/application.properties da dung chua?",
                    "Loi ket noi co so du lieu",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
    }
}