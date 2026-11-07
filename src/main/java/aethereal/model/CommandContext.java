package aethereal.model;
import aethereal.util.ArgumentParser;
import aethereal.Lang;


public final class CommandContext {
    public final String[] args;

    public CommandContext(String[] strArr) {
        this.args = strArr;
    }

    public <T> T getArgument(int i, ArgumentParser<T> class071Var) throws TranslatedException {
        if (i >= this.args.length) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_MISSING_ARGUMENT.effective().replace("{index}", String.valueOf(i))));
        }
        return class071Var.parse(this.args[i]);
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "args=" + this.args + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.args);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CommandContext)) return false;
        CommandContext o = (CommandContext) obj;
        return java.util.Objects.equals(this.args, o.args);
    }
public String[] args() {
        return this.args;
    }
}
