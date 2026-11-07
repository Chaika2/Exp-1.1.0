package aethereal.model;

public class TranslatedException extends RuntimeException {
    public final Translation text;

    public TranslatedException(Translation class254Var) {
        super(class254Var.effective());
        this.text = class254Var;
    }

    public TranslatedException(Translation class254Var, Throwable th) {
        super(class254Var.effective(), th);
        this.text = class254Var;
    }

    public Translation getText() {
        return this.text;
    }

    @Override
    public String getMessage() {
        return this.text.effective();
    }
}
