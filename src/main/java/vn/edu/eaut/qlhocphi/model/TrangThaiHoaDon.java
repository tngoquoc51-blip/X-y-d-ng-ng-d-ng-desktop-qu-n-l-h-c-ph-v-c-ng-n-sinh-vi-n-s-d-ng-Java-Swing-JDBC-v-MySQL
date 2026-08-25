package vn.edu.eaut.qlhocphi.model;

/** Trang thai thanh toan cua mot hoa don hoc phi. */
public enum TrangThaiHoaDon {
    CHUA_DONG("Chua dong"),
    DONG_MOT_PHAN("Dong mot phan"),
    DA_DONG_DU("Da dong du"),
    QUA_HAN("Qua han");

    private final String nhan;
    TrangThaiHoaDon(String nhan) { this.nhan = nhan; }
    public String getNhan() { return nhan; }
}