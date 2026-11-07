package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum ReallyWorldServer implements DisplayNamed {
    REALLYWORLD(Translation.clearText("ReallyWorld"));

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }

    ReallyWorldServer(Translation class254Var) {
        this.displayName = class254Var;
    }
}
