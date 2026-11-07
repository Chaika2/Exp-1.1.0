package aethereal.util;
import aethereal.render.ColorStack;
import aethereal.model.CreeperFarmStats;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.render.MsdfFont;
import aethereal.type.NotificationType;
import aethereal.event.PacketReceiveEvent;
import aethereal.event.Render2DEvent;
import aethereal.render.ScreenResolution;
import aethereal.type.TpLootStage;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.text.Text;

public class CreeperFarmStatsHandler {
    public final Mc mc = Mc.INSTANCE;
    public CreeperFarmStats stats;

    public void init() {
        this.stats = new CreeperFarmStats(System.currentTimeMillis(), 0, 0, 0.0d, TpLootStage.APPROACH);
    }

    public void reset() {
        this.stats = null;
    }

    public void handlePacket(PacketReceiveEvent class051Var) {
        ClientWorld world = this.mc.getWorld();
        net.minecraft.network.packet.Packet<?> packet = class051Var.getPacket();
        Objects.requireNonNull(packet);
        if (packet instanceof ItemPickupAnimationS2CPacket) {
            ItemPickupAnimationS2CPacket itemPickupAnimationS2CPacket = (ItemPickupAnimationS2CPacket) packet;
            if (itemPickupAnimationS2CPacket.getCollectorEntityId() == this.mc.getPlayer().getId()) {
                if ((world.getEntityById(itemPickupAnimationS2CPacket.getEntityId())) instanceof ItemEntity entityById ) {
                    ItemStack stack = entityById.getStack();
                    if (stack.getItem() == Items.GUNPOWDER) {
                        this.stats.gunpowder += stack.getCount();
                    }
                }
            }
        } else if (packet instanceof EntityStatusS2CPacket) {
            EntityStatusS2CPacket entityStatusS2CPacket = (EntityStatusS2CPacket) packet;
            if (entityStatusS2CPacket.getStatus() == 3 && (entityStatusS2CPacket.getEntity(world) instanceof CreeperEntity)) {
                this.stats.kills++;
            }
        } else if (packet instanceof GameMessageS2CPacket) {
            String string = (String) (((GameMessageS2CPacket) packet).content().getString());
            if (string.contains("Успешная авто-продажа Порох")) {
                parsePrice(string).ifPresent(d -> {
                    this.stats.money += d;
                });
                parseGunpowderCount(string).ifPresent(i -> {
                    this.stats.gunpowder += i;
                });
            } else if (string.contains("Вы подобрали $") || string.contains("У Вас купили Порох за $")) {
                parsePrice(string).ifPresent(d2 -> {
                    this.stats.money += d2;
                });
            }
        }
    }

    public void drawOverlay(Render2DEvent class311Var) {
        if (this.stats == null) {
            return;
        }
        MatrixStack matrixStack = class311Var.matrixStack();
        DrawEngine class154VarDrawEngine = Expensive.INSTANCE.drawEngine();
        ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
        MsdfFont class161Var = Fonts.INTER_SEMIBOLD.get();
        int iScreenWidth = ScreenResolution.resolution().screenWidth();
        float height = class161Var.getHeight(16.0f);
        float size = this.mc.getInGameHud().getBossBarHud().bossBars.size() * 45;
        class154VarDrawEngine.begin();
        float f = size + height;
        class154VarDrawEngine.drawLine(matrixStack, class161Var, 16, iScreenWidth, f, Lang.CREEPERFARM_STATS_UPTIME.effective() + " ", this.stats.formattedTime(), class115VarColorStack.computeColor(StencilBufferUtil.STENCIL_MASK, 230, 230, 230), class115VarColorStack.computeColor(StencilBufferUtil.STENCIL_MASK, 170, 170, 170));
        float f2 = f + height;
        NumberFormat integerInstance = NumberFormat.getIntegerInstance(Locale.US);
        class154VarDrawEngine.drawLine(matrixStack, class161Var, 16, iScreenWidth, f2, Lang.CREEPERFARM_STATS_MONEY.effective() + " ", integerInstance.format((long) this.stats.money) + "$", class115VarColorStack.computeColor(StencilBufferUtil.STENCIL_MASK, 230, 230, 230), class115VarColorStack.computeColor(StencilBufferUtil.STENCIL_MASK, 170, 170, 170));
        float f3 = f2 + height;
        int i = this.stats.gunpowder;
        class154VarDrawEngine.drawLine(matrixStack, class161Var, 16, iScreenWidth, f3, Lang.CREEPERFARM_STATS_GUNPOWDER.effective() + " ", i + " (" + String.format(Lang.CREEPERFARM_GUNPOWDER_STACKS.effective(), String.format("%.1f", Double.valueOf(((double) i) / 64.0d))) + ")", class115VarColorStack.computeColor(StencilBufferUtil.STENCIL_MASK, 230, 230, 230), class115VarColorStack.computeColor(StencilBufferUtil.STENCIL_MASK, 170, 170, 170));
        float f4 = f3 + height;
        class154VarDrawEngine.drawLine(matrixStack, class161Var, 16, iScreenWidth, f4, Lang.CREEPERFARM_STATS_PHASE.effective() + " ", this.stats.currentPhase.getName().effective(), class115VarColorStack.computeColor(StencilBufferUtil.STENCIL_MASK, 230, 230, 230), class115VarColorStack.computeColor(StencilBufferUtil.STENCIL_MASK, 170, 170, 170));
        float f5 = f4 + height;
        class154VarDrawEngine.drawLine(matrixStack, class161Var, 16, iScreenWidth, f5, Lang.CREEPERFARM_STATS_KILLS.effective() + " ", String.valueOf(this.stats.kills), class115VarColorStack.computeColor(StencilBufferUtil.STENCIL_MASK, 230, 230, 230), class115VarColorStack.computeColor(StencilBufferUtil.STENCIL_MASK, 170, 170, 170));
        float f6 = f5 + height;
        long uptimeSeconds = this.stats.getUptimeSeconds();
        class154VarDrawEngine.drawLine(matrixStack, class161Var, 16, iScreenWidth, f6, Lang.CREEPERFARM_STATS_INCOME_PER_HOUR.effective() + " ", uptimeSeconds > 0 ? integerInstance.format((long) (this.stats.money / (uptimeSeconds / 3600.0d))) + "$" : Lang.CREEPERFARM_STATS_CALCULATING.effective(), class115VarColorStack.computeColor(StencilBufferUtil.STENCIL_MASK, 230, 230, 230), class115VarColorStack.computeColor(StencilBufferUtil.STENCIL_MASK, 170, 170, 170));
        class154VarDrawEngine.end();
    }

    public OptionalDouble parsePrice(String str) {
        try {
            Matcher matcher = Pattern.compile("([\\d.,]+)\\$|\\$([\\d.,]+)").matcher(str);
            if (matcher.find()) {
                return OptionalDouble.of(Double.parseDouble((matcher.group(1) != null ? matcher.group(1) : matcher.group(2)).replace(",", "")));
            }
        } catch (Exception e) {
            Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal("Ошибка парсинга цены: " + e.getMessage()), 3L, TimeUnit.SECONDS);
        }
        return OptionalDouble.empty();
    }

    public OptionalInt parseGunpowderCount(String str) {
        try {
            Matcher matcher = Pattern.compile("Порох (\\d+) шт\\.").matcher(str);
            if (matcher.find()) {
                return OptionalInt.of(Integer.parseInt(matcher.group(1)));
            }
        } catch (Exception e) {
            Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal("Ошибка парсинга количества пороха: " + e.getMessage()), 3L, TimeUnit.SECONDS);
        }
        return OptionalInt.empty();
    }

    public CreeperFarmStats getStats() {
        return this.stats;
    }
}
