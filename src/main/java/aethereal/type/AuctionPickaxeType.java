package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum AuctionPickaxeType implements DisplayNamed {
    MENDING(Lang.AUCTION_HELPER_PICKAXES_MENDING),
    EFFICIENCY(Lang.AUCTION_HELPER_PICKAXES_EFFICIENCY),
    FORTUNE(Lang.AUCTION_HELPER_PICKAXES_FORTUNE),
    UNBREAKING(Lang.AUCTION_HELPER_PICKAXES_UNBREAKING),
    SILK_TOUCH(Lang.AUCTION_HELPER_PICKAXES_SILK_TOUCH),
    SMELTING(Lang.AUCTION_HELPER_PICKAXES_SMELTING),
    MAGNET(Lang.AUCTION_HELPER_PICKAXES_MAGNET),
    BULLDOZING(Lang.AUCTION_HELPER_PICKAXES_BULLDOZING),
    WITHOUT_HEAVY(Lang.AUCTION_HELPER_PICKAXES_WITHOUT_HEAVY);

    final Translation displayName;

    AuctionPickaxeType(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
