package aethereal.ui;
import aethereal.type.Mc;
import aethereal.util.FullDropAll;
import aethereal.util.PlayerActionUtil;
import aethereal.math.Stopwatch;

import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

public class ContainerActionsScreen extends GenericContainerScreen {
    public static final Stopwatch cooldown = new Stopwatch();
    public VanillaButton dropAllButton;
    public VanillaButton takeAllButton;
    public VanillaButton depositAllButton;

    public boolean isCurrentHandler() {
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        return player != null && player.currentScreenHandler == getScreenHandler();
    }

    public ContainerActionsScreen(GenericContainerScreenHandler genericContainerScreenHandler, PlayerInventory playerInventory, Text text) {
        super(genericContainerScreenHandler, playerInventory, text);
    }

    public void init() {
        super.init();
        VanillaButton class700Var = new VanillaButton((this.x + (this.backgroundWidth / 2)) - (90 / 2), (this.y - 20) - 10, 90, 20, Text.of("Выбросить все"), buttonWidget -> {
            dropAll();
        });
        this.dropAllButton = class700Var;
        addDrawableChild(class700Var);
        int i = this.y;
        VanillaButton class700Var2 = new VanillaButton(this.x + this.backgroundWidth + 5, i, 90, 20, Text.of("Забрать все"), buttonWidget2 -> {
            takeAll();
        });
        this.takeAllButton = class700Var2;
        addDrawableChild(class700Var2);
        VanillaButton class700Var3 = new VanillaButton(this.x + this.backgroundWidth + 5, i + 25, 90, 20, Text.of("Сложить все"), buttonWidget3 -> {
            depositAll();
        });
        this.depositAllButton = class700Var3;
        addDrawableChild(class700Var3);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (this.dropAllButton == null || this.takeAllButton == null || this.depositAllButton == null) {
            return;
        }
        boolean zHasElapsed = cooldown.hasElapsed(600L, TimeUnit.MILLISECONDS);
        this.dropAllButton.active = hasAnyItems();
        this.takeAllButton.active = containerHasItems() && zHasElapsed;
        this.depositAllButton.active = playerHasItems() && zHasElapsed;
    }

    public boolean containerHasItems() {
        for (int i = 0; i < getScreenHandler().getInventory().size(); i++) {
            if (!((Slot) getScreenHandler().slots.get(i)).getStack().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean playerHasItems() {
        int size = getScreenHandler().getInventory().size();
        for (int i = size; i < size + 36; i++) {
            if (!((Slot) getScreenHandler().slots.get(i)).getStack().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyItems() {
        for (int i = 0; i < getScreenHandler().slots.size(); i++) {
            if (!((Slot) getScreenHandler().slots.get(i)).getStack().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void dropAll() {
        if (isCurrentHandler()) {
            FullDropAll.dropAll(getScreenHandler(), this);
        }
    }

    public void takeAll() {
        if (isCurrentHandler() && cooldown.hasElapsed(600L, TimeUnit.MILLISECONDS)) {
            cooldown.reset();
            DefaultedList defaultedList = getScreenHandler().slots;
            int size = getScreenHandler().getInventory().size();
            moveItems(0, size, size, defaultedList.size(), (slot, slot2) -> {
                return slot2.canInsert(slot.getStack());
            });
        }
    }

    public void depositAll() {
        if (isCurrentHandler() && cooldown.hasElapsed(600L, TimeUnit.MILLISECONDS)) {
            cooldown.reset();
            DefaultedList defaultedList = getScreenHandler().slots;
            int size = getScreenHandler().getInventory().size();
            moveItems(size, defaultedList.size(), 0, size, (slot, slot2) -> {
                return slot2.canInsert(slot.getStack());
            });
        }
    }

    public void moveItems(int i, int i2, int i3, int i4, BiPredicate<Slot, Slot> biPredicate) {
        for (int i5 = i; i5 < i2; i5++) {
            Slot slot = (Slot) getScreenHandler().slots.get(i5);
            if (!slot.getStack().isEmpty()) {
                for (int i6 = i3; i6 < i4; i6++) {
                    Slot slot2 = (Slot) getScreenHandler().slots.get(i6);
                    if (slot2.getStack().isEmpty() && biPredicate.test(slot, slot2)) {
                        PlayerActionUtil.INSTANCE.windowClick(SlotActionType.QUICK_MOVE, i5, 1);
                        break;
                    }
                }
            }
        }
    }
}
