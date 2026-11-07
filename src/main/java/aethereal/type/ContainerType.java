package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.util.StencilBufferUtil;
import aethereal.model.Translation;

import java.util.Optional;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;

public enum ContainerType implements DisplayNamed {
    CHEST(Lang.CONTAINER_ESP_CHEST, ChestBlockEntity.class, StencilBufferUtil.STENCIL_MASK, 140, 0),
    ENDER_CHEST(Lang.CONTAINER_ESP_ENDER_CHEST, EnderChestBlockEntity.class, 120, 0, 180),
    FURNACE(Lang.CONTAINER_ESP_FURNACE, AbstractFurnaceBlockEntity.class, 90, 90, 90),
    SHULKER_BOX(Lang.CONTAINER_ESP_SHULKER_BOX, ShulkerBoxBlockEntity.class, 180, 0, 180),
    BARREL(Lang.CONTAINER_ESP_BARREL, BarrelBlockEntity.class, 160, 82, 45),
    HOPPER(Lang.CONTAINER_ESP_HOPPER, HopperBlockEntity.class, 50, 50, 50),
    DISPENSER(Lang.CONTAINER_ESP_DISPENSER, DispenserBlockEntity.class, 160, 0, 0),
    DECORATED_POT(Lang.CONTAINER_ESP_DECORATED_POT, DecoratedPotBlockEntity.class, 210, 105, 30);

    public final Class<? extends BlockEntity> blockEntityClass;
    public final int red;
    public final int green;
    public final int blue;
    final Translation displayName;

    ContainerType(Translation class254Var, Class cls, int i, int i2, int i3) {
        this.displayName = class254Var;
        this.blockEntityClass = cls;
        this.red = i;
        this.green = i2;
        this.blue = i3;
    }

    public static Optional<ContainerType> fromBlockEntity(BlockEntity blockEntity) {
        for (ContainerType class544Var : values()) {
            if (class544Var.blockEntityClass.isInstance(blockEntity)) {
                return Optional.of(class544Var);
            }
        }
        return Optional.empty();
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public int getRed() {
        return this.red;
    }

    public int getGreen() {
        return this.green;
    }

    public int getBlue() {
        return this.blue;
    }
}
