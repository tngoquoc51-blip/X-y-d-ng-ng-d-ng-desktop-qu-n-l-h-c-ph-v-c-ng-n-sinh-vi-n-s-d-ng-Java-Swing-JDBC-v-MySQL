package vn.edu.eaut.qlhocphi.ai;

/** Ket qua tra loi cua chatbot: van ban + duong dan anh dinh kem (co the null neu khong co anh). */
public class KetQuaTraLoi {
    private final String vanBan;
    private final String duongDanAnh;

    public KetQuaTraLoi(String vanBan, String duongDanAnh) {
        this.vanBan = vanBan;
        this.duongDanAnh = duongDanAnh;
    }

    public String getVanBan() { return vanBan; }
    public String getDuongDanAnh() { return duongDanAnh; }
}