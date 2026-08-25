package vn.edu.eaut.qlhocphi.util;

import vn.edu.eaut.qlhocphi.config.AppConfig;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sao luu / phuc hoi CSDL MySQL bang cach goi cong cu dong hanh mysqldump / mysql
 * (yeu cau may chay ung dung da cai MySQL client va co trong PATH).
 *
 * Day la cach lam pho bien, don gian cho ung dung desktop noi bo; voi he thong
 * quy mo lon hon nen chuyen sang co che backup tu dong cua chinh MySQL Server.
 */
public class BackupService {

    private static class ThongTinKetNoi {
        String host = "localhost";
        String port = "3306";
        String database = "qlhocphi";
    }

    /** Sao luu toan bo CSDL ra 1 file .sql. */
    public void saoLuu(File fileDich) throws IOException, InterruptedException {
        ThongTinKetNoi tt = docThongTinKetNoi();
        String user = AppConfig.get("db.username");
        String pass = AppConfig.get("db.password");

        ProcessBuilder pb = new ProcessBuilder(
                "mysqldump",
                "-h", tt.host,
                "-P", tt.port,
                "-u", user,
                "-p" + pass,
                "--databases", tt.database,
                "--result-file=" + fileDich.getAbsolutePath()
        );
        pb.redirectErrorStream(false);
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String loi = new String(process.getErrorStream().readAllBytes());
            throw new IOException("mysqldump ket thuc voi ma loi " + exitCode + (loi.isBlank() ? "" : (": " + loi)));
        }
    }

    /** Phuc hoi CSDL tu 1 file .sql (chay lai toan bo script, se ghi de du lieu hien tai). */
    public void phucHoi(File fileNguon) throws IOException, InterruptedException {
        ThongTinKetNoi tt = docThongTinKetNoi();
        String user = AppConfig.get("db.username");
        String pass = AppConfig.get("db.password");

        ProcessBuilder pb = new ProcessBuilder(
                "mysql",
                "-h", tt.host,
                "-P", tt.port,
                "-u", user,
                "-p" + pass
        );
        pb.redirectInput(fileNguon);
        pb.redirectErrorStream(false);
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String loi = new String(process.getErrorStream().readAllBytes());
            throw new IOException("mysql ket thuc voi ma loi " + exitCode + (loi.isBlank() ? "" : (": " + loi)));
        }
    }

    /** Tach host/port/database tu chuoi db.url dang jdbc:mysql://host:port/dbname?... */
    private ThongTinKetNoi docThongTinKetNoi() {
        ThongTinKetNoi tt = new ThongTinKetNoi();
        String url = AppConfig.get("db.url");
        Pattern pattern = Pattern.compile("jdbc:mysql://([^:/]+)(:(\\d+))?/([^?]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            tt.host = matcher.group(1);
            if (matcher.group(3) != null) tt.port = matcher.group(3);
            tt.database = matcher.group(4);
        }
        return tt;
    }
}
