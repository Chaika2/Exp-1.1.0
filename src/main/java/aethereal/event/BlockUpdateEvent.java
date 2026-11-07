package aethereal.event;
import aethereal.model.BlockUpdateEntry;
import aethereal.type.BlockUpdateType;

import java.util.List;

public final class BlockUpdateEvent implements Event {
    public final List<BlockUpdateEntry> list;
    public final BlockUpdateType type;

    public BlockUpdateEvent(List<BlockUpdateEntry> list, BlockUpdateType class191Var) {
        this.list = list;
        this.type = class191Var;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "list=" + this.list + ", " + "type=" + this.type + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.list, this.type);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BlockUpdateEvent)) return false;
        BlockUpdateEvent o = (BlockUpdateEvent) obj;
        return java.util.Objects.equals(this.list, o.list) && java.util.Objects.equals(this.type, o.type);
    }
public List<BlockUpdateEntry> list() {
        return this.list;
    }

    public BlockUpdateType type() {
        return this.type;
    }
}
