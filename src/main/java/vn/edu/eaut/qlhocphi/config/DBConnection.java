package vn.edu.eaut.qlhocphi.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Quan ly ket noi toi MySQL. Moi loi goi tra ve 1 Connection moi (dung try-with-resources o DAO). */
public class DBConnection {

    public static Connection getConnection() throws SQLException {
        String url = AppConfig.get("db.url");
        String user = AppConfig.get("db.username");
        String pass = AppConfig.get("db.password");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Khong tim thay MySQL JDBC Driver trong classpath", e);
        }
        return DriverManager.getConnection(url, user, pass);
    }
}