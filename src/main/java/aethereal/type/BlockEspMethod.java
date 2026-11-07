package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum BlockEspMethod implements DisplayNamed {
    SCAN(Lang.BLOCK_ESP_METHOD_SCAN),
    PACKET(Lang.BLOCK_ESP_METHOD_PACKET);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }

    BlockEspMethod(Translation class254Var) {
        this.displayName = class254Var;
    }
}