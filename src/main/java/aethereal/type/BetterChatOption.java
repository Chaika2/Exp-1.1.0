package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum BetterChatOption implements DisplayNamed {
    ANTI_SPAM(Lang.BETTERCHAT_CHAT_IMPROVEMENTS_ANTI_SPAM),
    ANTI_CLEAR(Lang.BETTERCHAT_CHAT_IMPROVEMENTS_ANTI_CLEAR),
    INFINITY(Lang.BETTERCHAT_CHAT_IMPROVEMENTS_INFINITY);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }

    BetterChatOption(Translation class254Var) {
        this.displayName = class254Var;
    }
}
