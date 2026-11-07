package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum AuctionAxeType implements DisplayNamed {
    EFFICIENCY(Lang.AUCTION_HELPER_AXES_EFFICIENCY),
    FORTUNE(Lang.AUCTION_HELPER_AXES_FORTUNE),
    MENDING(Lang.AUCTION_HELPER_AXES_MENDING),
    PINGER(Lang.AUCTION_HELPER_AXES_PINGER),
    MAGNET(Lang.AUCTION_HELPER_AXES_MAGNET),
    LUMBERJACK(Lang.AUCTION_HELPER_AXES_LUMBERJACK),
    BULLDOZING(Lang.AUCTION_HELPER_SHOVELS_BULLDOZING),
    UNBREAKING(Lang.AUCTION_HELPER_AXES_UNBREAKING);

    final Translation displayName;

    AuctionAxeType(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
