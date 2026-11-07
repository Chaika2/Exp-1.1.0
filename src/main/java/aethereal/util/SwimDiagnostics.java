package aethereal.util;

import aethereal.math.Rotation;
import aethereal.type.Mc;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.FluidTags;

/**
 * Диагностика поведения в воде.
 *
 * Серверное предупреждение "Проверьте настройки клиента. FastMath или
 * несовместимые модификации..." - это prediction-чек движения: сервер
 * пересчитывает позицию игрока и видит расхождение с клиентом. В воде
 * расхождение почти всегда означает, что клиент отправил не то состояние
 * (поворот / спринт / input / использование предмета) или двигался быстрее,
 * чем сервер умеет предсказать (Depth Strider).
 *
 * Пишем строку раз в 4 тика, только пока игрок в воде. Файл:
 * run/logs/swim_debug.log
 */
public final class SwimDiagnostics {
    private static long lastWriteTick = -1000L;
    private static int tickCounter = 0;
    private static double lastX = Double.NaN;
    private static double lastZ = Double.NaN;

    private SwimDiagnostics() {
    }

    public static void tick() {
        Mc mc = Mc.INSTANCE;
        ClientPlayerEntity player = mc.getPlayer();
        if (player == null || !mc.isWorldLoaded()) {
            return;
        }
        // Фактическое смещение за последний клиентский тик: главный вопрос
        // диагностики - сколько метров в тик клиент реально прошёл в воде.
        double step = 0.0d;
        if (!Double.isNaN(lastX)) {
            double d = player.getX() - lastX;
            double e = player.getZ() - lastZ;
            step = Math.sqrt(d * d + e * e);
        }
        lastX = player.getX();
        lastZ = player.getZ();

        // Счётчик тиков клиента, чтобы не зависеть от age (он растёт и вне мира).
        tickCounter++;
        if (!player.isTouchingWater() && !player.isSwimming() && !player.isInLava()) {
            lastWriteTick = -1000L;
            return;
        }
        if (tickCounter - lastWriteTick < 4) {
            return;
        }
        lastWriteTick = tickCounter;
        try {
            Rotation sent = RotationManager.INSTANCE.getCurrentRotationOriginal();
            Rotation camera = Rotation.playerRotation();
            PlayerInventory inventory = player.getInventory();
            ItemStack active = player.getActiveItem();
            ItemStack feet = player.getEquippedStack(EquipmentSlot.FEET);
            String depthStrider = "?";
            try {
                Registry<Enchantment> registry = player.getWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
                depthStrider = String.valueOf(EnchantmentHelper.getLevel(registry.getOrThrow(Enchantments.DEPTH_STRIDER), feet));
            } catch (Throwable ignored) {
            }
            String line = "[t=" + tickCounter + "]"
                    + " time=" + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))
                    + " server=" + ServerUtil.getServer()
                    + " water=" + player.isTouchingWater()
                    + " swim=" + player.isSwimming()
                    + " submerged=" + player.isSubmergedInWater()
                    + " lava=" + player.isInLava()
                    + " onGround=" + player.isOnGround()
                    + " sprint=" + player.isSprinting()
                    + " inputSprint=" + player.input.playerInput.sprint()
                    + " keySprint=" + mc.getMinecraft().options.sprintKey.isPressed()
                    + " jump=" + player.input.playerInput.jump()
                    + " fwd=" + player.input.movementForward
                    + " side=" + player.input.movementSideways
                    + " step=" + fmt(step)
                    + " vel=" + fmt(player.getVelocity().x) + "," + fmt(player.getVelocity().y) + "," + fmt(player.getVelocity().z)
                    + " pos=" + fmt(player.getX()) + "/" + fmt(player.getY()) + "/" + fmt(player.getZ())
                    + " fh=" + fmt(player.getFluidHeight(FluidTags.WATER))
                    + " wme=" + fmt(player.getAttributeValue(EntityAttributes.WATER_MOVEMENT_EFFICIENCY))
                    + " eff=" + fmt(aethereal.module.WaterFixModule.lastWaterEfficiency)
                    + " ms=" + fmt(player.getAttributeValue(EntityAttributes.MOVEMENT_SPEED))
                    + " gms=" + fmt(player.getMovementSpeed())
                    + " ds=" + depthStrider
                    + " feet=" + feet.getItem()
                    + " cam=" + fmt(camera.getYaw()) + "/" + fmt(camera.getPitch())
                    + " sent=" + (sent == null ? "null" : fmt(sent.getYaw()) + "/" + fmt(sent.getPitch()))
                    + " useItem=" + player.isUsingItem()
                    + " hand=" + player.getActiveHand()
                    + " useTime=" + player.getItemUseTime()
                    + " activeSlot=" + inventory.selectedSlot
                    + " activeItem=" + active.getItem()
                    + " jumpCooldown=" + player.jumpingCooldown;

            java.io.File dir = new java.io.File(mc.getMinecraft().runDirectory, "logs");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            java.io.File file = new java.io.File(dir, "swim_debug.log");
            java.nio.file.Files.writeString(file.toPath(), line + System.lineSeparator(),
                    java.nio.charset.StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception ignored) {
        }
    }

    private static String fmt(double d) {
        return String.format(java.util.Locale.ROOT, "%.3f", d);
    }
}
