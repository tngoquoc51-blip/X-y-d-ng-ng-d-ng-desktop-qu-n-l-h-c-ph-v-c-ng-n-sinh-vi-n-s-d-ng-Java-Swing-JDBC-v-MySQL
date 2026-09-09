package vn.edu.eaut.qlhocphi.util;

import vn.edu.eaut.qlhocphi.gui.common.ErrorScreens;

import javax.swing.*;
import java.awt.*;

/** Bat toan bo loi ngoai du kien tren luong Swing (EDT), hien man hinh loi than thien. */
public class GlobalExceptionHandler {

    public static void cauHinh() {
        System.setProperty("sun.awt.exception.handler", GlobalExceptionHandler.class.getName());
        Thread.setDefaultUncaughtExceptionHandler((thread, loi) -> xuLy(loi));
    }

    /** Duoc goi tu dong boi Swing khi co loi khong bat duoc trong 1 su kien EDT (co che rieng cua AWT). */
    public void handle(Throwable loi) {
        xuLy(loi);
    }

    private static void xuLy(Throwable loi) {
        loi.printStackTrace();
        SwingUtilities.invokeLater(() -> {
            Window active = mostRecentActiveWindow();
            ErrorScreens.hienLoiHeThong(active != null ? active : new JPanel(), loi);
        });
    }

    private static Window mostRecentActiveWindow() {
        for (Window w : Window.getWindows()) {
            if (w.isActive()) return w;
        }
        Window[] all = Window.getWindows();
        return all.length > 0 ? all[all.length - 1] : null;
    }
}