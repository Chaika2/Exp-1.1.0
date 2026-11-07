package aethereal.event;

import net.minecraft.client.network.PlayerListEntry;

public final class TabListEntryEvent implements Event {
    public final PlayerListEntry currentEntry;

    public TabListEntryEvent(PlayerListEntry playerListEntry) {
        this.currentEntry = playerListEntry;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "currentEntry=" + this.currentEntry + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.currentEntry);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TabListEntryEvent)) return false;
        TabListEntryEvent o = (TabListEntryEvent) obj;
        return java.util.Objects.equals(this.currentEntry, o.currentEntry);
    }
public PlayerListEntry currentEntry() {
        return this.currentEntry;
    }
}
