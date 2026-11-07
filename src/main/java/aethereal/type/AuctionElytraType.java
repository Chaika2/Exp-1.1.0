package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum AuctionElytraType implements DisplayNamed {
    UNBREAKING5(Lang.AUCTION_HELPER_ELYTRA_UNBREAKING5),
    MENDING(Lang.AUCTION_HELPER_ELYTRA_MENDING);

    final Translation displayName;

    AuctionElytraType(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
