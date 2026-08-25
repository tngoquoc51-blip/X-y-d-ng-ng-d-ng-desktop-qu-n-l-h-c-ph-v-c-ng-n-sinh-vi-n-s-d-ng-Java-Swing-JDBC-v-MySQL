package vn.edu.eaut.qlhocphi.ai;

import java.time.LocalDateTime;

/** 1 tin nhan trong hop thoai voi Chatbot. */
public class ChatMessage {
    public enum Nguon { NGUOI_DUNG, CHATBOT }

    private final Nguon nguon;
    private final String noiDung;
    private final LocalDateTime thoiGian;

    public ChatMessage(Nguon nguon, String noiDung) {
        this.nguon = nguon;
        this.noiDung = noiDung;
        this.thoiGian = LocalDateTime.now();
    }

    public Nguon getNguon() { return nguon; }
    public String getNoiDung() { return noiDung; }
    public LocalDateTime getThoiGian() { return thoiGian; }
}
