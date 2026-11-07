package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum HudInfoType implements DisplayNamed {
    NICKNAME(Lang.WIDGET_WATERMARK_LINE_NICKNAME),
    LATENCY(Lang.WIDGET_WATERMARK_LINE_LATENCY),
    FRAMERATE(Lang.WIDGET_WATERMARK_LINE_FRAMERATE),
    CURRENT_TIME(Lang.WIDGET_WATERMARK_LINE_CURRENT_TIME),
    SERVER_NAME(Lang.WIDGET_WATERMARK_LINE_SERVER_NAME);

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    HudInfoType(Translation class254Var) {
        this.displayName = class254Var;
    }

    public Translation displayName() {
        return this.displayName;
    }
}
