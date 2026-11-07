package aethereal.natives;

public interface Kernel32Native {
    Kernel32Native INSTANCE = null;
    Object OpenProcess(int i, boolean z, int i2);
    boolean QueryFullProcessImageNameW(Object handle, int i, char[] cArr, Object intByReference);
    boolean CloseHandle(Object handle);
}
