package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.ui.setting.BooleanSetting;
import aethereal.event.EntityHitboxEvent;
import aethereal.util.FriendManager;
import aethereal.type.HitBoxTargetType;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.ui.setting.NumberSetting;
import aethereal.type.SettingUnit;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

@Aliases(aliases = {"Hit Boxes", "Hit Box", "Player HitBox", "Hit Range", "Reach Expand", "Combat HitBox", "Box Control"})
public class HitBoxesModule extends Module {
    public final NumberSetting xzExpandSetting;
    public final NumberSetting yExpandSetting;

    public final BooleanSetting ignoreFriendsSetting;
    public final MultiSelectSetting<HitBoxTargetType> targetSetting;
    public final Mc mc;

    public HitBoxesModule() {
        super(ModuleTab.COMBAT, "Hit Boxes");
        this.xzExpandSetting = new NumberSetting(Lang.HITBOX_XZ_EXPAND).currentValue(0.2f).range(0.0f, 3.0f).unit(SettingUnit.BLOCKS).step(0.05f);
        this.yExpandSetting = new NumberSetting(Lang.HITBOX_Y_EXPAND).currentValue(0.0f).range(0.0f, 3.0f).unit(SettingUnit.BLOCKS).step(0.05f);
        this.ignoreFriendsSetting = new BooleanSetting(Lang.HITBOX_IGNORE_FRIENDS).setValue(true);
        this.targetSetting = new MultiSelectSetting(Lang.HITBOX_TARGET).values(HitBoxTargetType.class).select(HitBoxTargetType.PLAYERS);
        this.mc = Mc.INSTANCE;
        addSettings(this.xzExpandSetting, this.yExpandSetting, this.targetSetting, this.ignoreFriendsSetting);
        register(EntityHitboxEvent.class, class099Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                Box box = class099Var.getBox();
                Entity entity = class099Var.getEntity();
                float fCurrentValue = this.xzExpandSetting.currentValue();
                float fCurrentValue2 = this.yExpandSetting.currentValue();
                Box box2 = new Box(box.minX - ((double) (fCurrentValue / 2.0f)), box.minY - ((double) (fCurrentValue2 / 2.0f)), box.minZ - ((double) (fCurrentValue / 2.0f)), box.maxX + ((double) (fCurrentValue / 2.0f)), box.maxY + ((double) (fCurrentValue2 / 2.0f)), box.maxZ + ((double) (fCurrentValue / 2.0f)));
                if (shouldExpand(entity)) {
                    class099Var.setChangedBox(box2);
                    class099Var.cancel();
                }
            }
        });
    }

    public boolean shouldExpand(Entity entity) {
        ClientPlayerEntity player = this.mc.getPlayer();
        if (player != null && player.getId() == entity.getId()) {
            return false;
        }
        if (FriendManager.isFriend(entity.getName().getString()) && this.ignoreFriendsSetting.isValue()) {
            return false;
        }
        if (this.targetSetting.isSelected(HitBoxTargetType.PLAYERS) && (entity instanceof PlayerEntity)) {
            return true;
        }
        if (this.targetSetting.isSelected(HitBoxTargetType.MOBS) && (entity instanceof HostileEntity)) {
            return true;
        }
        return this.targetSetting.isSelected(HitBoxTargetType.ANIMALS) && (entity instanceof AnimalEntity);
    }
}
