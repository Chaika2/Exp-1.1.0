package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum AuctionShovelType implements DisplayNamed {
    BULLDOZING(Lang.AUCTION_HELPER_SHOVELS_BULLDOZING),
    MAGNET(Lang.AUCTION_HELPER_SHOVELS_MAGNET),
    UNBREAKING(Lang.AUCTION_HELPER_SHOVELS_UNBREAKING),
    MENDING(Lang.AUCTION_HELPER_SHOVELS_MENDING),
    FORTUNE(Lang.AUCTION_HELPER_SHOVELS_FORTUNE),
    EFFICIENCY(Lang.AUCTION_HELPER_SHOVELS_EFFICIENCY);

    final Translation displayName;

    AuctionShovelType(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
