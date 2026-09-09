package vn.edu.eaut.qlhocphi.config;

/** Luu chon theme dang ap dung (toan cuc, dung chung cho ca app trong 1 lan chay). */
public enum ThemeMode {
    SANG,
    TOI;

    private static ThemeMode hienTai = SANG;

    public static ThemeMode layHienTai() {
        return hienTai;
    }

    public static void datHienTai(ThemeMode mode) {
        hienTai = mode;
    }

    public static void doiCheDo() {
        hienTai = (hienTai == SANG) ? TOI : SANG;
    }

    public boolean laToi() {
        return this == TOI;
    }
}