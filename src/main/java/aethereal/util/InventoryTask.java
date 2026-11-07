package aethereal.util;
import aethereal.math.Rotation;
import aethereal.model.SlotSearchResult2;

import java.util.function.Predicate;

public class InventoryTask {
    public Predicate predicate;
    public SlotSearchResult2 result;
    public boolean needStop;
    public boolean flag2;
    public boolean flag3;
    public Rotation rotation;

    public InventoryTask() {
    }

    public InventoryTask(Predicate predicate, SlotSearchResult2 class329Var, boolean z, boolean z2, boolean z3) {
        this.predicate = predicate;
        this.result = class329Var;
        this.needStop = z;
        this.flag2 = z2;
        this.flag3 = z3;
    }

    public static InventoryTask create(Predicate predicate, SlotSearchResult2 class329Var, boolean z, boolean z2, boolean z3) {
        return new InventoryTask(predicate, class329Var, z, z2, z3);
    }

    public boolean needStop() {
        return this.needStop;
    }

    public SlotSearchResult2 result() {
        return this.result;
    }

    public Rotation rotation() {
        return this.rotation != null ? this.rotation : RotationManager.INSTANCE.getCurrentRotation();
    }

    public InventoryTask withRotation(Rotation class007Var) {
        this.rotation = class007Var;
        return this;
    }
}
