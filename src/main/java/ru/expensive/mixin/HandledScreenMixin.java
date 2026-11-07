package ru.expensive.mixin;

import aethereal.event.HandledScreenRenderEvent;
import aethereal.Expensive;
import aethereal.util.ColorUtil;
import aethereal.event.FocusedSlotEvent;
import aethereal.util.ServerUtil;
import aethereal.event.SlotScrollEvent;
import aethereal.type.Mc;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({HandledScreen.class})
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen implements ScreenHandlerProvider<T> {

    @Shadow
    protected Slot field_2787;

    @Shadow
    public int field_2792;

    @Shadow
    public int field_2779;

    @Shadow
    protected abstract void method_30107(int i);

    @Shadow
    protected abstract boolean method_2387(Slot slot, double d, double d2);

    protected HandledScreenMixin(Text text) {
        super(text);
    }

    @Inject(method = {"keyPressed"}, at = {@At("HEAD")})
    private void onKeyPressed(int i, int i2, int i3, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        Expensive.INSTANCE.eventDispatcher().dispatch(new FocusedSlotEvent(this.field_2787));
    }

    @Inject(method = {"render"}, at = {@At("RETURN")})
    public void onRender(DrawContext drawContext, int i, int i2, float f, CallbackInfo callbackInfo) {
        Expensive.INSTANCE.eventDispatcher().dispatch(new HandledScreenRenderEvent(drawContext, this.field_2792, this.field_2779));
    }

    @Inject(method = {"render"}, at = {@At("HEAD")})
    private void render(DrawContext drawContext, int i, int i2, float f, CallbackInfo callbackInfo) {
        ScreenHandler screenHandler = Mc.INSTANCE.getPlayer().currentScreenHandler;
        for (int i3 = 0; i3 < screenHandler.slots.size(); i3++) {
            Slot slot = (Slot) screenHandler.slots.get(i3);
            if (method_2387(slot, i, i2) && slot.isEnabled()) {
                Expensive.INSTANCE.eventDispatcher().dispatch(new SlotScrollEvent(slot, slot.id));
            }
        }
    }

    @Inject(method = {"render"}, at = {@At("RETURN")})
    public void renderReturn(DrawContext drawContext, int i, int i2, float f, CallbackInfo callbackInfo) {
        if ((Object) this instanceof HandledScreen) {
            Optional.ofNullable(((HandledScreen) (Object) this).focusedSlot).ifPresent(slot -> {
                drawShulkerItems(slot, drawContext, i, i2);
            });
        }
    }

    @Unique
    public void drawShulkerItems(Slot slot, DrawContext drawContext, int i, int i2) {
        ItemStack stack = slot.getStack();
        Item item = stack.getItem();
        if (item instanceof BlockItem) {
            BlockItem blockItem = (BlockItem) item;
            if (blockItem.getBlock() instanceof ShulkerBoxBlock) {
                List<ItemStack> containerStacks = getContainerStacks(stack);
                if (containerStacks.isEmpty()) {
                    return;
                }
                int iMultBright = ColorUtil.multBright(ColorUtil.replAlpha(blockItem.getBlock().getDefaultMapColor().color, 1.0f), 1.0f);
                MatrixStack matrices = drawContext.getMatrices();
                int i3 = 7;
                int i4 = 6;
                matrices.push();
                matrices.translate(i + 8, i2 - 84, 1000.0f);
                GlStateManager._enableBlend();
                GlStateManager._disableDepthTest();
                drawContext.drawTexture(RenderLayer::getGuiTextured, Identifier.of("expensive", "textures/container.png"), 0, 0, 0.0f, 0.0f, 176, 67, 176, 67, iMultBright);
                if (containerStacks.size() > 27) {
                    drawContext.enableScissor(0, -18, 176, 5);
                    drawContext.drawTexture(RenderLayer::getGuiTextured, Identifier.of("expensive", "textures/container.png"), 0, -18, 0.0f, 0.0f, 176, 67, 176, 67, iMultBright);
                    drawContext.disableScissor();
                    i4 = 6 - 18;
                }
                for (ItemStack itemStack : containerStacks) {
                    drawContext.drawItem(itemStack, i3, i4);
                    drawContext.drawStackOverlay(this.textRenderer, itemStack, i3, i4);
                    i3 += 18;
                    if (i3 >= 165) {
                        i4 += 18;
                        i3 = 7;
                    }
                }
                GlStateManager._disableBlend();
                matrices.pop();
            }
        }
    }

    @Unique
    public List<ItemStack> getContainerStacks(ItemStack itemStack) {
        ArrayList arrayList = new ArrayList(((ContainerComponent) itemStack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT)).stream().toList());
        if (!arrayList.isEmpty()) {
            return arrayList;
        }
        if (ServerUtil.isConnectedToServer("holyworld")) {
            NbtCompound nbtCompoundCopyNbt = ((NbtComponent) itemStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT)).copyNbt();
            Optional.ofNullable(nbtCompoundCopyNbt.getList("backpack-inventory", 10)).ifPresent(nbtList -> {
                HashMap map = new HashMap();
                Iterator it = nbtList.iterator();
                while (it.hasNext()) {
                    NbtElement nbtElement = (NbtElement) it.next();
                    if (nbtElement instanceof NbtCompound) {
                        NbtCompound nbtCompound2 = (NbtCompound) nbtElement;
                        NbtCompound compound = nbtCompound2.getCompound("item");
                        map.put(Integer.valueOf(nbtCompound2.getInt("slot")), new ItemStack((ItemConvertible) Registries.ITEM.get(Identifier.of(compound.getString("id"))), compound.getInt("Count")));
                    }
                }
                if (map.isEmpty()) {
                    return;
                }
                IntStream.range(0, Math.max(maxBackPackSlots(nbtCompoundCopyNbt), 27)).forEach(i -> {
                    arrayList.add((ItemStack) map.getOrDefault(Integer.valueOf(i), Items.AIR.getDefaultStack()));
                });
            });
        }
        return arrayList;
    }

    @Unique
    public int maxBackPackSlots(NbtCompound nbtCompound) {
        switch (nbtCompound.getCompound("PublicBukkitValues").getString("litebackpacks:backpack").replace("\"", "")) {
            case "infinity":
                return 36;
            case "huge":
                return 27;
            case "big":
                return 21;
            case "normal":
                return 15;
            case "mini":
                return 9;
            default:
                return -1;
        }
    }
}
