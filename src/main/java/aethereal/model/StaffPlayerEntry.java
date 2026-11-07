package aethereal.model;
import aethereal.type.StaffStatus;
import aethereal.render.ToggleAnimator;

import net.minecraft.client.texture.AbstractTexture;

public class StaffPlayerEntry {
    public final AbstractTexture headTexture;
    public final String name;
    public final String prefix;
    public final StaffStatus status;
    public final ToggleAnimator stateAnimation;

    public StaffPlayerEntry() {
        this.headTexture = null;
        this.name = null;
        this.prefix = null;
        this.status = null;
        this.stateAnimation = null;
    }

    public StaffPlayerEntry(AbstractTexture abstractTexture, String str, String str2, StaffStatus class119Var, ToggleAnimator class323Var) {
        this.headTexture = abstractTexture;
        this.prefix = str;
        this.name = str2;
        this.status = class119Var;
        this.stateAnimation = class323Var;
    }

    public AbstractTexture headTexture() {
        return this.headTexture;
    }

    public String name() {
        return this.name;
    }

    public String prefix() {
        return this.prefix;
    }

    public ToggleAnimator stateAnimation() {
        return this.stateAnimation;
    }

    public StaffStatus status() {
        return this.status;
    }
}
