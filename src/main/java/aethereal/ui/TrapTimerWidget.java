package aethereal.ui;
import aethereal.render.AnimatedFloat;
import aethereal.render.ColorStack;
import aethereal.model.DragRenderContext;
import aethereal.util.DrawEngine;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.module.FTHelperModule;
import aethereal.render.Fonts;
import aethereal.Lang;
import aethereal.util.MathUtil;
import aethereal.type.Mc;
import aethereal.model.MouseButtonInput2;
import aethereal.model.MouseMoveInput;
import aethereal.render.MsdfFont;
import aethereal.util.ServerUtil;
import aethereal.render.ThemePalette;
import aethereal.util.WeightedEngine;

import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;

public class TrapTimerWidget extends Draggable {
    static final float padding = 8.0f;
    static final float iconSize = 12.0f;
    static final float iconGap = 4.0f;
    static final float arcRadius = 6.0f;
    static final float arcThickness = 2.0f;
    static final float cornerRadius = 7.0f;
    public float width;
    public float height;
    public double remainingTime;
    public double maxTime;
    public final AnimatedFloat visibilityAnimation;
    public final AnimatedFloat progressAnimation;

    public TrapTimerWidget(BooleanSupplier booleanSupplier) {
        super("TrapTimer", booleanSupplier);
        this.visibilityAnimation = new AnimatedFloat(250, Easings.LINEAR);
        this.progressAnimation = new AnimatedFloat(250, Easings.LINEAR);
        this.x = 10.0f;
        this.y = 50.0f;
    }

    @Override
    public void layout(DragRenderContext class809Var) {
    }

    @Override
    public void render(DragRenderContext class809Var) {
        float fAnimatedValue = this.visibilityAnimation.animatedValue();
        if (fAnimatedValue <= 0.01f) {
            this.height = 0.0f;
            this.width = 0.0f;
            return;
        }
        MsdfFont class161Var = Fonts.INTER_EXTRA_BOLD.get();
        MatrixStack matrixStack = class809Var.matrixStack();
        DrawEngine class154VarDrawEngine = class809Var.drawEngine();
        ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
        String strReplace = this.remainingTime > 0.0d ? Lang.WIDGET_TRAP_TIMER_ACTIVE.effective().replace("{time}", String.valueOf(MathUtil.round(this.remainingTime, 0.10000000149011612d))) : Lang.WIDGET_TRAP_TIMER_INACTIVE.effective();
        float width = class161Var.getWidth(strReplace, 11.0f);
        float height = class161Var.getHeight(11.0f);
        float f = 24.0f + width + 19.0f + padding;
        float fMax = Math.max(iconSize, height) + iconSize;
        this.width = f;
        this.height = fMax;
        ThemePalette class764VarPalette = Expensive.INSTANCE.theme().palette();
        class115VarColorStack.push();
        class115VarColorStack.alpha(fAnimatedValue);
        class154VarDrawEngine.roundedRectangle(matrixStack.peek().getPositionMatrix(), this.x, this.y, this.width, this.height, cornerRadius, 2.5f, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(700).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(801).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(801).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb()));
        class154VarDrawEngine.itemStack(matrixStack.peek().getPositionMatrix(), Items.NETHERITE_SCRAP.getDefaultStack(), this.x + padding, this.y + ((this.height - iconSize) / arcThickness), 0.375f, fAnimatedValue);
        class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), class161Var, strReplace, this.x + padding + iconSize + iconGap, this.y + ((this.height - height) / arcThickness), 11.0f, 0.0f, class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb()));
        float fClamp = MathUtil.clamp(this.progressAnimation.animatedValue(), 0.0f, 1.0f);
        float f2 = ((this.x + this.width) - padding) - arcRadius;
        float f3 = this.y + (this.height / arcThickness);
        int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(300).argb());
        int iComputeColor2 = class115VarColorStack.computeColor(class764VarPalette.accent().argb());
        class154VarDrawEngine.arc(matrixStack.peek().getPositionMatrix(), f2, f3, arcRadius, 0.0f, 360.0f, arcThickness, iComputeColor);
        float f4 = 270.0f - (360.0f * fClamp);
        if (f4 >= 0.0f) {
            class154VarDrawEngine.arc(matrixStack.peek().getPositionMatrix(), f2, f3, arcRadius, f4, 270.0f, arcThickness, iComputeColor2);
        } else {
            class154VarDrawEngine.arc(matrixStack.peek().getPositionMatrix(), f2, f3, arcRadius, 360.0f + f4, 360.0f, arcThickness, iComputeColor2);
            class154VarDrawEngine.arc(matrixStack.peek().getPositionMatrix(), f2, f3, arcRadius, 0.0f, 270.0f, arcThickness, iComputeColor2);
        }
        class115VarColorStack.pop();
    }

    public double computeRemainingTime() {
        ClientPlayerEntity player;
        FTHelperModule class492Var = (FTHelperModule) Expensive.INSTANCE.moduleRepository().get(FTHelperModule.class);
        if (class492Var == null || (player = Mc.INSTANCE.getPlayer()) == null) {
            return 0.0d;
        }
        long jCurrentTimeMillis = System.currentTimeMillis();
        return class492Var.getStructures().stream().filter(class493Var -> {
            return class493Var.item() == Items.NETHERITE_SCRAP && class493Var.anarchy() == ServerUtil.getAnarchy() && ServerUtil.getWorldType().equals(class493Var.world()) && class493Var.vec().distanceTo(player.getPos()) <= 3.0d;
        }).mapToDouble(class493Var2 -> {
            return (class493Var2.time() - jCurrentTimeMillis) / 1000.0d;
        }).filter(d -> {
            return d > 0.0d;
        }).max().orElse(0.0d);
    }

    @Override
    public boolean click(MouseButtonInput2 class807Var, boolean z) {
        return false;
    }

    @Override
    public boolean cursor(MouseMoveInput class808Var, boolean z) {
        return false;
    }

    @Override
    public void animate(WeightedEngine class141Var) {
        this.remainingTime = computeRemainingTime();
        boolean z = Mc.INSTANCE.getCurrentScreen() instanceof ChatScreen;
        boolean z2 = this.remainingTime > 0.0d;
        this.visibilityAnimation.destination(z || z2 ? 1.0f : 0.0f);
        this.visibilityAnimation.animate(class141Var);
        if (z2) {
            this.maxTime = Math.max(this.maxTime, this.remainingTime);
            this.progressAnimation.destination((float) MathUtil.clamp(this.remainingTime / Math.max(this.maxTime, 0.001d), 0.0d, 1.0d));
        } else {
            this.maxTime = 0.0d;
            this.progressAnimation.destination((float) (0.5d - (0.5d * Math.cos((((double) ((System.currentTimeMillis() % 3000) / 3000.0f)) * 3.141592653589793d) * 2.0d))));
        }
        this.progressAnimation.animate(class141Var);
    }

    @Override
    public void update() {
    }

    @Override
    public float width() {
        return this.width;
    }

    @Override
    public float height() {
        return this.height;
    }
}
