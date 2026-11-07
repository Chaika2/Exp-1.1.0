package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum UiColorMode implements DisplayNamed {
    DEFAULT(Translation.clearText("По умолчанию")),
    WHITE(Translation.clearText("Белый")),
    GOLD(Translation.clearText("Золотой")),
    RED(Translation.clearText("Красный")),
    GREEN(Translation.clearText("Зелёный")),
    BLUE(Translation.clearText("Синий")),
    PURPLE(Translation.clearText("Фиолетовый")),
    PINK(Translation.clearText("Розовый")),
    CYAN(Translation.clearText("Голубой")),
    CUSTOM(Translation.clearText("Свой"));

    private final Translation displayName;

    UiColorMode(Translation translation) {
        this.displayName = translation;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
