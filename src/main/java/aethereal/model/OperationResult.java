package aethereal.model;


public final class OperationResult {
    public final boolean successfully;
    public final String errorMessage;

    public OperationResult(boolean z, String str) {
        this.successfully = z;
        this.errorMessage = str;
    }

    public static OperationResult success() {
        return new OperationResult(true, "");
    }

    public static OperationResult failure(String str) {
        return new OperationResult(false, str);
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "successfully=" + this.successfully + ", " + "errorMessage=" + this.errorMessage + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.successfully, this.errorMessage);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof OperationResult)) return false;
        OperationResult o = (OperationResult) obj;
        return java.util.Objects.equals(this.successfully, o.successfully) && java.util.Objects.equals(this.errorMessage, o.errorMessage);
    }
public boolean successfully() {
        return this.successfully;
    }

    public String errorMessage() {
        return this.errorMessage;
    }
}
