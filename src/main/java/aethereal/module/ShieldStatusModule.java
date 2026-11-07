package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.event.PlayerTickEvent;
import aethereal.event.SoundPlayEvent;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.ColorSetting;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

@Aliases(aliases = {"Shield Status", "Shield Break", "Shield Indicator", "Broken Shield"})
public class ShieldStatusModule extends Module {
    private static final long SHIELD_BREAK_DURATION = 5000L;
    private static final double SHIELD_BREAK_RADIUS = 5.0d;
    private static final Identifier SHIELD_BREAK_SOUND = Identifier.ofVanilla("item.shield_break");

    public final ColorSetting activeColor;
    public final ColorSetting disabledColor;
    public final Map<UUID, Long> brokenShields;
    public final Mc mc;

    public ShieldStatusModule() {
        super(ModuleTab.RENDER, "ShieldStatus");
        this.mc = Mc.INSTANCE;
        this.activeColor = new ColorSetting(Lang.SHIELDSTATUS_ACTIVE).setColor(0xFF8073E1);
        this.disabledColor = new ColorSetting(Lang.SHIELDSTATUS_DISABLED).setColor(0xFFEF4836);
        this.brokenShields = new ConcurrentHashMap<>();
        addSettings(this.activeColor, this.disabledColor);
        register(SoundPlayEvent.class, class017Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                onShieldBreakSound(class017Var.getSoundInstance().getId(), class017Var.getSoundInstance().getX(), class017Var.getSoundInstance().getY(), class017Var.getSoundInstance().getZ());
            }
        });
        register(PlayerTickEvent.class, class130Var -> {
            if (!this.brokenShields.isEmpty()) {
                long currentTime = System.currentTimeMillis();
                this.brokenShields.values().removeIf(t -> currentTime - t > SHIELD_BREAK_DURATION);
            }
        });
    }

    public void onShieldBreakSound(Identifier identifier, double d, double e, double f) {
        if (!SHIELD_BREAK_SOUND.equals(identifier) || this.mc.getWorld() == null) {
            return;
        }
        PlayerEntity playerEntity = this.mc.getWorld().getClosestPlayer(d, e, f, SHIELD_BREAK_RADIUS, entity -> true);
        if (playerEntity != null) {
            this.brokenShields.put(playerEntity.getUuid(), System.currentTimeMillis());
        }
    }

    public boolean isShieldDisabled(PlayerEntity playerEntity) {
        if (playerEntity.getItemCooldownManager().isCoolingDown(Items.SHIELD.getDefaultStack())) {
            return true;
        }
        Long brokenAt = this.brokenShields.get(playerEntity.getUuid());
        return brokenAt != null && System.currentTimeMillis() - brokenAt <= SHIELD_BREAK_DURATION;
    }

    public boolean localShieldDisabled() {
        PlayerEntity playerEntity = this.mc.getPlayer();
        return playerEntity != null && isShieldDisabled(playerEntity);
    }

    public int currentShieldColor() {
        return localShieldDisabled() ? this.disabledColor.getColor() : this.activeColor.getColor();
    }
}