package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum AuctionSwordType implements DisplayNamed {
    SHARPNESS(Lang.AUCTION_HELPER_SWORDS_SHARPNESS),
    UNBREAKING(Lang.AUCTION_HELPER_SWORDS_UNBREAKING),
    POISON(Lang.AUCTION_HELPER_SWORDS_POISON),
    DETECTION(Lang.AUCTION_HELPER_SWORDS_DETECTION),
    OXIDATION(Lang.AUCTION_HELPER_SWORDS_OXIDATION),
    VAMPIRISM(Lang.AUCTION_HELPER_SWORDS_VAMPIRISM),
    WITHOUT_KNOCKBACK(Lang.AUCTION_HELPER_SWORDS_WITHOUT_KNOCKBACK);

    final Translation displayName;

    AuctionSwordType(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
