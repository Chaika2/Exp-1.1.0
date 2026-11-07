package aethereal.util;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;

public class TrayNotifier {
    public static void send(String str, String str2) {
        if (SystemTray.isSupported()) {
            try {
                SystemTray systemTray = SystemTray.getSystemTray();
                TrayIcon trayIcon = new TrayIcon(new BufferedImage(1, 1, 2));
                systemTray.add(trayIcon);
                trayIcon.displayMessage(str, str2, TrayIcon.MessageType.NONE);
                systemTray.remove(trayIcon);
            } catch (Exception e) {
            }
        }
    }
}
