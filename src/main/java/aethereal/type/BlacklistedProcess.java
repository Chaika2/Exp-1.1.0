package aethereal.type;

public enum BlacklistedProcess {
    IDEA("idea64.exe", "SunAwtFrame"),
    CALCULATOR("CalculatorApp.exe", "MSCTFIME UI"),
    VISUAL_STUDIO("devenv.exe", "MSCTFIME UI"),
    IDA("ida.exe", "Qt5153QTQWindowIcon"),
    X64DBG("x64dbg.exe", null),
    X32DBG("x32dbg.exe", null),
    WIRESHARK("Wireshark.exe", null),
    FIDDLE("Fiddler.exe", null),
    POSTMAN("Postman.exe", null),
    HTTP_TOOLKIT("httptoolkit.exe", null),
    HTTP_DEBUGGER("HTTPDebuggerUI.exe", null),
    WINDBG("windbg.exe", null),
    OLLYDBG("ollydbg.exe", null),
    PYCHARM("pycharm64.exe", "SunAwtFrame"),
    VS_CODE("Code.exe", "Electron_SystemPreferencesHostWindow");

    public final String processName;
    public final String className;

    BlacklistedProcess(String str, String str2) {
        this.processName = str;
        this.className = str2;
    }
}
