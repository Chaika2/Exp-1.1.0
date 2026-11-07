package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.ui.setting.ColorSetting;
import aethereal.ui.setting.ExpandableSetting;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.NumberSetting;
import aethereal.event.PacketReceiveEvent;
import aethereal.type.WorldTime;

import java.util.List;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

@Aliases(aliases = {"World Tweaks", "Ambience", "Fog Color", "Sky Color", "World Time"})
public class WorldTweaksModule extends Module {
    public final ExpandableSetting changeTime;
    public final ModeSetting<WorldTime> timeOfDay;
    public final ExpandableSetting changeFogColor;
    public final NumberSetting fogDistance;
    public final Mc mc;
    public final ColorSetting fogColor;

    public WorldTweaksModule() {
        super(ModuleTab.RENDER, "World Tweaks");
        this.changeTime = new ExpandableSetting(Lang.WORLD_TWEAKS_CHANGE_TIME, Lang.WORLD_TWEAKS_CHANGE_TIME_DESC);
        this.timeOfDay = new ModeSetting(Lang.WORLD_TWEAKS_TIME_OF_DAY).values(WorldTime.class).currentValue(WorldTime.NIGHT);
        this.changeFogColor = new ExpandableSetting(Lang.WORLD_TWEAKS_FOG_COLOR, Lang.WORLD_TWEAKS_FOG_COLOR_DESC);
        this.fogDistance = new NumberSetting(Lang.WORLD_TWEAKS_FOG_COLOR_DISTANCE, Lang.WORLD_TWEAKS_FOG_COLOR_DISTANCE_DESC).currentValue(90.0f).range(10.0f, 256.0f).step(1.0f);
        this.mc = Mc.INSTANCE;
        this.fogColor = new ColorSetting(Lang.WORLD_TWEAKS_FOG_COLOR_COLOR, Lang.WORLD_TWEAKS_FOG_COLOR_COLOR_DESC).setColor(7238883);
        this.changeTime.setSubSettings(List.of(this.timeOfDay));
        this.changeFogColor.setSubSettings(List.of(this.fogColor, this.fogDistance));
        addSettings(this.changeTime, this.changeFogColor);
        register(PacketReceiveEvent.class, class051Var -> {
            if (isState() && this.mc.isWorldLoaded() && this.changeTime.isValue()) {
                if ((class051Var.getPacket()) instanceof WorldTimeUpdateS2CPacket packet ) {
                    WorldTimeUpdateS2CPacket worldTimeUpdateS2CPacket = packet;
                    WorldTime class637Var = (WorldTime) this.timeOfDay.currentValue();
                    if (class637Var.ticks() != -1) {
                        worldTimeUpdateS2CPacket.timeOfDay = class637Var.ticks();
                    }
                }
            }
        });
    }

    @Override
    public void activate() {
        super.activate();
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    public void reload() {
        WorldRenderer worldRenderer;
        if (Mc.INSTANCE.isWorldLoaded() && (worldRenderer = Mc.INSTANCE.getMinecraft().worldRenderer) != null && isState()) {
            worldRenderer.reload();
        }
    }

    public ExpandableSetting changeFogColor() {
        return this.changeFogColor;
    }

    public NumberSetting fogDistance() {
        return this.fogDistance;
    }

    public ColorSetting fogColor() {
        return this.fogColor;
    }
}
