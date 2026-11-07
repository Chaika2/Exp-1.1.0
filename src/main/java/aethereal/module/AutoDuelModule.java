package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.type.AutoDuelArmorType;
import aethereal.type.AutoDuelOffhandItem;
import aethereal.model.DuelContext;
import aethereal.type.DuelKitType;
import aethereal.event.HandledScreenRenderEvent;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.setting.ModeSetting;
import aethereal.event.ModuleDeactivateCallback;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.ui.setting.NumberSetting;
import aethereal.event.PacketReceiveEvent;
import aethereal.event.PlayerTickEvent;
import aethereal.type.ServerType;
import aethereal.type.SettingUnit;

import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.StringHelper;

@Aliases(aliases = {"Auto Duel", "Fun Time", "Really World"})
public class AutoDuelModule extends Module implements ModuleDeactivateCallback {
    public final ModeSetting<ServerType> serverSetting;
    public final MultiSelectSetting<DuelKitType> selectedKits;
    public final ModeSetting<AutoDuelArmorType> armorType;
    public final ModeSetting<AutoDuelOffhandItem> offhandItem;
    public final NumberSetting nextDuelDelay;

    public AutoDuelModule() {
        super(ModuleTab.MISC, "Auto Duel");
        this.serverSetting = new ModeSetting(Lang.SERVER).values(ServerType.class);
        this.selectedKits = new MultiSelectSetting(Lang.AUTODUEL_KIT).values(DuelKitType.class).visible(() -> {
            return Boolean.valueOf(this.serverSetting.isSelected(ServerType.REALLYWORLD));
        });
        this.armorType = new ModeSetting(Lang.AUTODUEL_ARMOR_TYPE).values(AutoDuelArmorType.class).visible(() -> {
            return Boolean.valueOf(this.serverSetting.isSelected(ServerType.FUNTIME));
        });
        this.offhandItem = new ModeSetting(Lang.AUTODUEL_OFFHAND_ITEM, Lang.AUTODUEL_OFFHAND_ITEM_DESC).values(AutoDuelOffhandItem.class).visible(() -> {
            return Boolean.valueOf(this.serverSetting.isSelected(ServerType.FUNTIME));
        });
        this.nextDuelDelay = new NumberSetting(Lang.AUTODUEL_NEXT_DUEL_DELAY).currentValue(1000.0f).range(100.0f, 10000.0f).step(50.0f).unit(SettingUnit.MILLISECONDS);
        addSettings(this.serverSetting, this.selectedKits, this.armorType, this.offhandItem, this.nextDuelDelay);
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded() && class130Var.isPre()) {
                ((ServerType) this.serverSetting.currentValue()).strategy().doDuelLogic(new DuelContext(this.selectedKits, this.nextDuelDelay, (AutoDuelArmorType) this.armorType.currentValue(), (AutoDuelOffhandItem) this.offhandItem.currentValue(), this));
            }
        });
        register(HandledScreenRenderEvent.class, class015Var -> {
            DuelContext class445Var = new DuelContext(this.selectedKits, this.nextDuelDelay, (AutoDuelArmorType) this.armorType.currentValue(), (AutoDuelOffhandItem) this.offhandItem.currentValue(), this);
            if (isState() && Mc.INSTANCE.isWorldLoaded()) {
                ((ServerType) this.serverSetting.currentValue()).strategy().onScreen(class015Var, class445Var);
            }
        });
        register(PacketReceiveEvent.class, class051Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded()) {
                if ((class051Var.getPacket()) instanceof GameMessageS2CPacket packet ) {
                    GameMessageS2CPacket gameMessageS2CPacket = packet;
                    DuelContext class445Var = new DuelContext(this.selectedKits, this.nextDuelDelay, (AutoDuelArmorType) this.armorType.currentValue(), (AutoDuelOffhandItem) this.offhandItem.currentValue(), this);
                    ((ServerType) this.serverSetting.currentValue()).strategy().onChat(StringHelper.stripTextFormat(gameMessageS2CPacket.content().getString().toLowerCase()), class445Var);
                }
            }
        });
    }

    @Override
    public void deactivate() {
        ((ServerType) this.serverSetting.currentValue()).strategy().deactivate();
        super.deactivate();
    }

    @Override
    public void deactivateModule() {
        switchState();
    }
}
