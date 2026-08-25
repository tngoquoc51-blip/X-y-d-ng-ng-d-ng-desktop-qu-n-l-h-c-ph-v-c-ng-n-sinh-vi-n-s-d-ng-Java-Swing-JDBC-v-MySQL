package vn.edu.eaut.qlhocphi.util;

import vn.edu.eaut.qlhocphi.config.AppConfig;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Gui email thong bao (nhac hoc phi, bien lai...) bang JavaMail.
 * Can khai bao trong application.properties: mail.host, mail.port, mail.username, mail.password
 * Yeu cau dependency com.sun.mail:javax.mail da khai bao trong pom.xml.
 */
public class EmailUtils {

    private static Session buildSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", AppConfig.get("mail.host", "smtp.gmail.com"));
        props.put("mail.smtp.port", AppConfig.get("mail.port", "587"));

        String username = AppConfig.get("mail.username");
        String password = AppConfig.get("mail.password");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    /** Gui email dang text thuan. Tra ve true neu gui thanh cong. */
    public static boolean sendMail(String toEmail, String subject, String noiDung) {
        try {
            Session session = buildSession();
            String from = AppConfig.get("mail.username");

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(noiDung);

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            System.err.println("Gui email that bai: " + e.getMessage());
            return false;
        }
    }

    /** Gui email nhac hoc phi cho sinh vien. */
    public static boolean guiNhacHocPhi(String toEmail, String hoTenSV, String tenHocKy, String soTienConNo) {
        String subject = "Nhac nho dong hoc phi - " + tenHocKy;
        String noiDung = "Chao " + hoTenSV + ",\n\n"
                + "Ban con no hoc phi " + soTienConNo + " d cho " + tenHocKy + ".\n"
                + "Vui long hoan tat thanh toan truoc han de tranh anh huong ket qua hoc tap.\n\n"
                + "Tran trong,\nPhong Ke toan.";
        return sendMail(toEmail, subject, noiDung);
    }
}