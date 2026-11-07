package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum AuctionPotionType implements DisplayNamed {
    KILLER(Lang.AUCTION_HELPER_POTIONS_KILLER),
    WINNER(Lang.AUCTION_HELPER_POTIONS_WINNER),
    MEDIC(Lang.AUCTION_HELPER_POTIONS_MEDIC),
    INVISIBILITY(Lang.AUCTION_HELPER_POTIONS_INVISIBILITY),
    HEALING(Lang.AUCTION_HELPER_POTIONS_HEALING),
    STRENGTH(Lang.AUCTION_HELPER_POTIONS_STRENGTH),
    SWIFTNESS(Lang.AUCTION_HELPER_POTIONS_SWIFTNESS);

    final Translation displayName;

    AuctionPotionType(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
