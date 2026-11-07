package aethereal.model;

public final class ProcessWindowInfo {
    public final Object hWnd;
    public final String className;
    public final String windowText;
    public final String processName;
    public final int pid;

    public ProcessWindowInfo(Object hwnd, String str, String str2, String str3, int i) {
        this.hWnd = hwnd;
        this.className = str;
        this.windowText = str2;
        this.processName = str3;
        this.pid = i;
    }
}
