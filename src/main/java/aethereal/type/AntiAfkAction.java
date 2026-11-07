package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum AntiAfkAction implements DisplayNamed {
    TURN_HEAD(Lang.ANTIAFK_ACTION_TURN_HEAD),
    JUMP(Lang.ANTIAFK_ACTION_JUMP),
    SEND_CHAT_MESSAGE(Lang.ANTIAFK_ACTION_SEND_CHAT_MESSAGE);

    final Translation displayName;

    AntiAfkAction(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
