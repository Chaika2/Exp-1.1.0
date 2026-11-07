package aethereal.net;

public class ReconnectException extends Exception {
    public ReconnectException(String str) {
        super(str);
    }

    public ReconnectException(String str, Throwable th) {
        super(str, th);
    }
}
