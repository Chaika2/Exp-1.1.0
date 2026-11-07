package aethereal.ui;
import aethereal.render.ColorStack;
import aethereal.model.DragRenderContext;
import aethereal.util.DrawEngine;
import aethereal.math.Easings;
import aethereal.render.GlTexture;
import aethereal.type.Mc;
import aethereal.model.MouseButtonInput2;
import aethereal.util.StencilBufferUtil;
import aethereal.render.ToggleAnimator;
import aethereal.util.WeightedEngine;

import net.minecraft.client.Mouse;

public class IconButton {
    public final ToggleAnimator hoverAnimator = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);
    public final ToggleAnimator pressAnimator = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);
    public final Runnable onClick;
    public float width;
    public float height;
    public int color;
    public boolean pressed;
    public GlTexture icon;
    public float x;
    public float y;

    public IconButton(Runnable runnable, GlTexture class073Var, float f, float f2) {
        this.onClick = runnable;
        this.icon = class073Var;
        this.width = f;
        this.height = f2;
    }

    public void render(DragRenderContext class809Var) {
        DrawEngine class154VarDrawEngine = class809Var.drawEngine();
        ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
        class154VarDrawEngine.texture(class809Var.matrixStack().peek().getPositionMatrix(), this.x, this.y, this.width, this.height, class154VarDrawEngine.bindTexture(this.icon.textureWithSTB()), class115VarColorStack.interpolate(class115VarColorStack.computeColor(this.color), class115VarColorStack.computeColor(StencilBufferUtil.STENCIL_MASK, 200, 200, 200), this.hoverAnimator));
    }

    public void animate(WeightedEngine class141Var) {
        Mouse mouse = Mc.INSTANCE.getMouse();
        this.hoverAnimator.state(contains((float) mouse.getX(), (float) mouse.getY())).animate(class141Var);
        this.pressAnimator.state(this.pressed).animate(class141Var);
    }

    public void switchTexture(GlTexture class073Var) {
        this.icon = class073Var;
    }

    public boolean click(MouseButtonInput2 class807Var, boolean z) {
        float fMouseX = class807Var.mouseX();
        float fMouseY = class807Var.mouseY();
        if (class807Var.button() != 0) {
            return false;
        }
        if (class807Var.press() && contains(fMouseX, fMouseY) && !class807Var.intercepted()) {
            this.pressed = true;
            return true;
        }
        if (!class807Var.release()) {
            return false;
        }
        this.pressed = false;
        if (!contains(fMouseX, fMouseY) || class807Var.intercepted()) {
            return false;
        }
        this.onClick.run();
        return true;
    }

    public boolean contains(float f, float f2) {
        return f > this.x && f2 > this.y && f < this.x + this.width && f2 < this.y + this.height;
    }

    public IconButton position(float f, float f2) {
        this.x = f;
        this.y = f2;
        return this;
    }

    public IconButton size(float f) {
        this.width = f;
        this.height = f;
        return this;
    }

    public IconButton color(int i) {
        this.color = i;
        return this;
    }

    public IconButton width(float f) {
        this.width = f;
        return this;
    }

    public IconButton height(float f) {
        this.height = f;
        return this;
    }

    public IconButton clicked(boolean z) {
        this.pressed = z;
        return this;
    }

    public IconButton icon(GlTexture class073Var) {
        this.icon = class073Var;
        return this;
    }

    public IconButton x(float f) {
        this.x = f;
        return this;
    }

    public IconButton y(float f) {
        this.y = f;
        return this;
    }

    public float width() {
        return this.width;
    }

    public float height() {
        return this.height;
    }
}
