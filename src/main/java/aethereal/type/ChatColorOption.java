package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum ChatColorOption implements DisplayNamed {
    BLACK("&0", "Чёрный"),
    DARK_BLUE("&1", "Тёмно-синий"),
    DARK_GREEN("&2", "Тёмно-зелёный"),
    DARK_AQUA("&3", "Бирюзовый"),
    DARK_RED("&4", "Тёмно-красный"),
    DARK_PURPLE("&5", "Фиолетовый"),
    GOLD("&6", "Золотой"),
    GRAY("&7", "Серый"),
    DARK_GRAY("&8", "Тёмно-серый"),
    BLUE("&9", "Синий"),
    GREEN("&a", "Зелёный"),
    AQUA("&b", "Голубой"),
    RED("&c", "Красный"),
    LIGHT_PURPLE("&d", "Розовый"),
    YELLOW("&e", "Жёлтый"),
    WHITE("&f", "Белый");

    public final String code;
    public final Translation displayName;

    ChatColorOption(String str, String str2) {
        this.code = str;
        this.displayName = Translation.clearText(str2);
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}