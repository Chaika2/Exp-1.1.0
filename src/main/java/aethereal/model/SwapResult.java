package aethereal.model;

public class SwapResult {
    public final boolean success;

    public SwapResult() {
        this.success = false;
    }

    public SwapResult(boolean z) {
        this.success = z;
    }

    public boolean success() {
        return this.success;
    }
}
