package aethereal.ui;
import aethereal.util.FullDropAll;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class InventoryActionsScreen extends InventoryScreen {
    public VanillaButton dropAllButton;

    public InventoryActionsScreen(PlayerEntity playerEntity) {
        super(playerEntity);
    }

    public void init() {
        super.init();
        this.dropAllButton = new VanillaButton((this.x + (this.backgroundWidth / 2)) - (90 / 2), (this.y - 20) - 10, 90, 20, Text.of("Выбросить все"), buttonWidget -> {
            dropAll();
        });
        addDrawableChild(this.dropAllButton);
    }

    public void updateButton() {
        if (this.dropAllButton != null) {
            this.dropAllButton.setPosition((this.x + (this.backgroundWidth / 2)) - (90 / 2), (this.y - 20) - 10);
            this.dropAllButton.active = getScreenHandler().slots.stream().anyMatch(slot -> {
                return !slot.getStack().isEmpty();
            });
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        updateButton();
    }

    public void dropAll() {
        FullDropAll.dropAll(getScreenHandler(), this);
    }
}
