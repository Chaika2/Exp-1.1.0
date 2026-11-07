package aethereal.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class TexturedButton extends ButtonWidget {
    public final ButtonTextures textures;
    public final Text label;

    public TexturedButton(int i, int i2, int i3, int i4, Text text, ButtonTextures buttonTextures, ButtonWidget.PressAction pressAction) {
        super(i, i2, i3, i4, Text.empty(), pressAction, DEFAULT_NARRATION_SUPPLIER);
        this.label = text;
        this.textures = buttonTextures;
    }

    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
    }
}
