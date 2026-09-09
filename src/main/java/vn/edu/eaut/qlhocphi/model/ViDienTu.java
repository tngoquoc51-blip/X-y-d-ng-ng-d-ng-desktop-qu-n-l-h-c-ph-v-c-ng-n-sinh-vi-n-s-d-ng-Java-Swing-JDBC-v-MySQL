package vn.edu.eaut.qlhocphi.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Vi hoc phi dien tu cua 1 sinh vien - noi giu so du de he thong tu dong tru khi thu hoc phi. */
public class ViDienTu {
    private String maSV;
    private BigDecimal soDu = BigDecimal.ZERO;
    private LocalDateTime ngayCapNhat;

    public ViDienTu() {}

    public String getMaSV() { return maSV; }
    public void setMaSV(String maSV) { this.maSV = maSV; }

    public BigDecimal getSoDu() { return soDu; }
    public void setSoDu(BigDecimal soDu) { this.soDu = soDu; }

    public LocalDateTime getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(LocalDateTime ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }
}