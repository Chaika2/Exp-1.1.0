package aethereal.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

public class VanillaButton extends ButtonWidget {
    public static final ButtonTextures buttonTextures = new ButtonTextures(Identifier.ofVanilla("widget/button"), Identifier.ofVanilla("widget/button_disabled"), Identifier.ofVanilla("widget/button_highlighted"));

    public VanillaButton(int i, int i2, int i3, int i4, Text text, ButtonWidget.PressAction pressAction) {
        super(i, i2, i3, i4, text, pressAction, DEFAULT_NARRATION_SUPPLIER);
    }

    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        context.drawGuiTexture(RenderLayer::getGuiTextured, buttonTextures.get(this.active, isSelected()), getX(), getY(), getWidth(), getHeight(), ColorHelper.getWhite(this.alpha));
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        drawMessage(context, minecraftClient.textRenderer, (this.active ? 16777215 : 10526880) | (MathHelper.ceil(this.alpha * 255.0f) << 24));
    }
}
