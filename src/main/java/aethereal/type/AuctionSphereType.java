package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum AuctionSphereType implements DisplayNamed {
    CHIMERA(Lang.AUCTION_HELPER_SPHERES_CHIMERA),
    ANDROMEDA(Lang.AUCTION_HELPER_SPHERES_ANDROMEDA),
    PANDORA(Lang.AUCTION_HELPER_SPHERES_PANDORA),
    TITAN(Lang.AUCTION_HELPER_SPHERES_TITAN);

    final Translation displayName;

    AuctionSphereType(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
