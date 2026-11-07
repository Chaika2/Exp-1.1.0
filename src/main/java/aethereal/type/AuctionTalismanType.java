package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

public enum AuctionTalismanType implements DisplayNamed {
    ECHIDNA(Lang.AUCTION_HELPER_TALISMANS_ECHIDNA),
    DAEDALUS(Lang.AUCTION_HELPER_TALISMANS_DAEDALUS),
    DESTROYER(Lang.AUCTION_HELPER_TALISMANS_DESTROYER),
    PUNISHER(Lang.AUCTION_HELPER_TALISMANS_PUNISHER);

    final Translation displayName;

    AuctionTalismanType(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
