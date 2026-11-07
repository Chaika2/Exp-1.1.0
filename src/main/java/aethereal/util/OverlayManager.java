package aethereal.util;
import aethereal.render.DrawCtx;
import aethereal.model.InputEventContext;
import aethereal.ui.OverlayWidget;

import java.util.LinkedHashSet;
import java.util.Set;

public class OverlayManager {
    public final Set<OverlayWidget> popups = new LinkedHashSet();
    public final Set<OverlayWidget> modals = new LinkedHashSet();

    public void registerModal(OverlayWidget class804Var) {
        if (class804Var != null) {
            this.modals.add(class804Var);
        }
    }

    public void registerPopup(OverlayWidget class804Var) {
        if (class804Var != null) {
            this.popups.add(class804Var);
        }
    }

    public void renderPopup(DrawCtx class699Var) {
        this.popups.stream().filter((v0) -> {
            return v0.isOpen();
        }).forEach(class804Var -> {
            class804Var.render(class699Var);
        });
    }

    public void renderModal(DrawCtx class699Var) {
        this.modals.stream().filter((v0) -> {
            return v0.isOpen();
        }).forEach(class804Var -> {
            class804Var.render(class699Var);
        });
    }

    public boolean handlePopupInput(InputEventContext class688Var, boolean z) {
        boolean zHandleInput = z;
        for (OverlayWidget class804Var : this.popups) {
            if (class804Var.isOpen()) {
                zHandleInput |= class804Var.handleInput(class688Var, zHandleInput);
            }
        }
        return zHandleInput;
    }

    public boolean handleModalInput(InputEventContext class688Var, boolean z) {
        boolean zHandleInput = z;
        for (OverlayWidget class804Var : this.modals) {
            if (class804Var.isOpen()) {
                zHandleInput |= class804Var.handleInput(class688Var, zHandleInput);
            }
        }
        return zHandleInput;
    }
}
