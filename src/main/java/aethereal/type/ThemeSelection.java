package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;
import aethereal.render.Theme;
import aethereal.render.ThemeManager;

public enum ThemeSelection implements DisplayNamed {
    DARK(Translation.clearText("Тёмная")),
    LIGHT(Translation.clearText("Светлая")),
    AZURE(Translation.clearText("Лазурная")),
    ROSE(Translation.clearText("Розовая")),
    EMERALD(Translation.clearText("Изумрудная")),
    AMBER(Translation.clearText("Янтарная")),
    VIOLET(Translation.clearText("Фиалковая")),
    CUSTOM(Translation.clearText("Свой"));

    private final Translation displayName;

    ThemeSelection(Translation translation) {
        this.displayName = translation;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Theme resolveBase() {
        if (this == CUSTOM) {
            return ThemeManager.INSTANCE.findById("expensive-dark");
        }
        return ThemeManager.INSTANCE.findById(ThemeManager.idFor(this));
    }
}
