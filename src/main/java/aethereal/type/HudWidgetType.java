package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum HudWidgetType implements DisplayNamed {
    WATERMARK(Translation.clearText("Watermark")),
    POTION_LIST(Translation.clearText("Potion List")),
    TARGET_HUD(Translation.clearText("Target Hud")),
    HOTKEYS(Translation.clearText("Hotkeys")),
    COORDS(Translation.clearText("Coords")),
    ITEM_BIND(Translation.clearText("Item Bind")),
    STAFF_LIST(Translation.clearText("Staff List")),
    NOTIFICATION(Translation.clearText("Notifications")),
    TRAP_TIMER(Translation.clearText("Trap Timer")),
    EVENT_TIMER(Translation.clearText("Event Timer")),
    SCOREBOARD(Translation.clearText("Scoreboard")),
    HOTBAR(Translation.clearText("Hotbar")),
    ARMOR_DURABILITY(Translation.clearText("Armor Durability"));

    public final Translation displayName;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }

    HudWidgetType(Translation class254Var) {
        this.displayName = class254Var;
    }
}
