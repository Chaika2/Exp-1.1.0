package aethereal.model;
import aethereal.ui.FrameElementColumn;
import aethereal.render.GlTexture;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class ActionDialogData {
    public final Translation title;
    public final Translation description;
    public final Translation confirmLabel;
    public final GlTexture icon;
    public final boolean needsChangePlaceholderColor;
    public final int placeHolderColor;
    public final Translation placeholderText;
    public final Consumer<FrameElementColumn> contentConfigurator;
    public final BooleanSupplier enableConfirmButton;
    public final float width;

    public final Runnable onConfirm;

    public ActionDialogData(Translation class254Var, Translation class254Var2, Translation class254Var3, GlTexture class073Var, boolean z, int i, Translation class254Var4, Consumer<FrameElementColumn> consumer, BooleanSupplier booleanSupplier, float f, Runnable runnable) {
        Objects.requireNonNull(class254Var, "Title cannot be null");
        Objects.requireNonNull(class073Var, "Icon cannot be null");
        Objects.requireNonNull(class254Var4, "Placeholder text cannot be null");
        this.title = class254Var;
        this.description = class254Var2;
        this.confirmLabel = class254Var3;
        this.icon = class073Var;
        this.needsChangePlaceholderColor = z;
        this.placeHolderColor = i;
        this.placeholderText = class254Var4;
        this.contentConfigurator = consumer;
        this.enableConfirmButton = booleanSupplier;
        this.width = f;
        this.onConfirm = runnable;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "title=" + this.title + ", " + "description=" + this.description + ", " + "confirmLabel=" + this.confirmLabel + ", " + "icon=" + this.icon + ", " + "needsChangePlaceholderColor=" + this.needsChangePlaceholderColor + ", " + "placeHolderColor=" + this.placeHolderColor + ", " + "placeholderText=" + this.placeholderText + ", " + "contentConfigurator=" + this.contentConfigurator + ", " + "enableConfirmButton=" + this.enableConfirmButton + ", " + "width=" + this.width + ", " + "onConfirm=" + this.onConfirm + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.title, this.description, this.confirmLabel, this.icon, this.needsChangePlaceholderColor, this.placeHolderColor, this.placeholderText, this.contentConfigurator, this.enableConfirmButton, this.width, this.onConfirm);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ActionDialogData)) return false;
        ActionDialogData o = (ActionDialogData) obj;
        return java.util.Objects.equals(this.title, o.title) && java.util.Objects.equals(this.description, o.description) && java.util.Objects.equals(this.confirmLabel, o.confirmLabel) && java.util.Objects.equals(this.icon, o.icon) && java.util.Objects.equals(this.needsChangePlaceholderColor, o.needsChangePlaceholderColor) && java.util.Objects.equals(this.placeHolderColor, o.placeHolderColor) && java.util.Objects.equals(this.placeholderText, o.placeholderText) && java.util.Objects.equals(this.contentConfigurator, o.contentConfigurator) && java.util.Objects.equals(this.enableConfirmButton, o.enableConfirmButton) && java.util.Objects.equals(this.width, o.width) && java.util.Objects.equals(this.onConfirm, o.onConfirm);
    }
public Translation title() {
        return this.title;
    }

    public Translation description() {
        return this.description;
    }

    public Translation confirmLabel() {
        return this.confirmLabel;
    }

    public GlTexture icon() {
        return this.icon;
    }

    public boolean needsChangePlaceholderColor() {
        return this.needsChangePlaceholderColor;
    }

    public int placeHolderColor() {
        return this.placeHolderColor;
    }

    public Translation placeholderText() {
        return this.placeholderText;
    }

    public Consumer<FrameElementColumn> contentConfigurator() {
        return this.contentConfigurator;
    }

    public BooleanSupplier enableConfirmButton() {
        return this.enableConfirmButton;
    }

    public float width() {
        return this.width;
    }

    public Runnable onConfirm() {
        return this.onConfirm;
    }
}
