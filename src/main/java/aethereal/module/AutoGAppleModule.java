package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.util.ItemUseController;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.NumberSetting;
import aethereal.event.PlayerTickEvent;
import aethereal.type.SettingUnit;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

@Aliases(aliases = {"Auto GApple", "Golden Apple", "Auto Eat Apple", "Auto Eat GApple", "Auto Eat", "GApple", "Golden Apple"})
public class AutoGAppleModule extends Module {
    public final NumberSetting healthThreshold;
    public boolean eating;
    public final Mc mc;

    public AutoGAppleModule() {
        super(ModuleTab.COMBAT, "Auto GApple");
        this.healthThreshold = new NumberSetting(Lang.COMBAT_AUTOGAPPLE_HEALTH, Lang.COMBAT_AUTOGAPPLE_HEALTH_DESC).range(4.0f, 20.0f).currentValue(15.0f).unit(SettingUnit.HITPOINTS);
        this.mc = Mc.INSTANCE;
        addSettings(this.healthThreshold);
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && this.mc.isWorldLoaded() && class130Var.isPre()) {
                if (shouldEat(this.mc.getPlayer())) {
                    eat(this.mc.getPlayer());
                } else if (this.eating) {
                    this.eating = false;
                }
            }
        });
    }

    public boolean shouldEat(ClientPlayerEntity clientPlayerEntity) {
        return clientPlayerEntity.getHealth() + clientPlayerEntity.getAbsorptionAmount() <= this.healthThreshold.currentValue() && hasGoldenApple(clientPlayerEntity) && !isOnCooldown(clientPlayerEntity);
    }

    public boolean isOnCooldown(ClientPlayerEntity clientPlayerEntity) {
        return clientPlayerEntity.getItemCooldownManager().isCoolingDown(Items.GOLDEN_APPLE.getDefaultStack());
    }

    public boolean hasGoldenApple(ClientPlayerEntity clientPlayerEntity) {
        return isGoldenApple(clientPlayerEntity, Hand.MAIN_HAND) || isGoldenApple(clientPlayerEntity, Hand.OFF_HAND);
    }

    public Hand findGoldenAppleHand(ClientPlayerEntity clientPlayerEntity) {
        if (isGoldenApple(clientPlayerEntity, Hand.OFF_HAND)) {
            return Hand.OFF_HAND;
        }
        if (isGoldenApple(clientPlayerEntity, Hand.MAIN_HAND)) {
            return Hand.MAIN_HAND;
        }
        return null;
    }

    public boolean isGoldenApple(ClientPlayerEntity clientPlayerEntity, Hand hand) {
        ItemStack stackInHand = clientPlayerEntity.getStackInHand(hand);
        return stackInHand.getItem() == Items.GOLDEN_APPLE || stackInHand.getItem() == Items.ENCHANTED_GOLDEN_APPLE;
    }

    public void eat(ClientPlayerEntity clientPlayerEntity) {
        Hand handMethod002 = findGoldenAppleHand(clientPlayerEntity);
        if (handMethod002 != null) {
            ItemUseController.INSTANCE.useHand(handMethod002);
            this.eating = true;
        }
    }

    @Override
    public void deactivate() {
        this.eating = false;
        super.deactivate();
    }
}
