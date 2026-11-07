package aethereal.command;
import aethereal.util.ChatUtil;
import aethereal.model.CommandContext;
import aethereal.model.TranslatedException;
import aethereal.model.Translation;
import aethereal.Expensive;
import aethereal.Lang;
import aethereal.event.PlayerTickEvent;
import aethereal.type.Mc;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class VClipGrimCommand implements ClientCommand {
    private static final double STEP_MIN = 7.0D;
    private static final double STEP_MAX = 9.5D;
    private final Mc mc = Mc.INSTANCE;
    private Vec3d destination;
    private boolean active;
    private int waitTicks;
    private int stepCount;

    public VClipGrimCommand() {
        Expensive.INSTANCE.eventDispatcher().register(PlayerTickEvent.class, class130Var -> {
            if (class130Var.isPost() && this.active) {
                this.tick();
            }
        });
    }

    @Override
    public String getName() {
        return "vclipgrim";
    }

    @Override
    public Translation getDescription() {
        return Lang.COMMAND_VCLIPGRIM_DESC;
    }

    @Override
    public String getUsage() {
        return ".vclipgrim -[blocks], .vclipgrim [blocks]";
    }

    @Override
    public List<String> getAliases() {
        return List.of("vgrim", "vg");
    }

    @Override
    public void execute(CommandContext class392Var) throws TranslatedException {
        String[] strArrArgs = class392Var.args();
        if (!this.mc.isWorldLoaded()) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_WORLD_NOT_LOADED.effective()));
        }
        if (strArrArgs.length == 0) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_DIRECTION_OR_DISTANCE.effective().replace("{usage}", getUsage())));
        }
        if (strArrArgs.length > 1) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_TOO_MANY_ARGUMENTS.effective().replace("{usage}", getUsage())));
        }
        if (this.active) {
            this.active = false;
            ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_VCLIPGRIM_STOPPED.effective()).formatted(Formatting.GRAY));
            return;
        }
        try {
            double dBlocks = Double.parseDouble(strArrArgs[0]);
            ClientPlayerEntity player = this.mc.getPlayer();
            if (dBlocks == 0.0d) {
                throw new TranslatedException(Translation.clearText(Lang.COMMAND_NOT_A_NUMBER.effective().replace("{value}", strArrArgs[0]).replace("{usage}", getUsage())));
            }
            this.destination = player.getPos().add(0.0d, dBlocks, 0.0d);
            this.active = true;
            this.waitTicks = 0;
            this.stepCount = 0;
            ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_VCLIPGRIM_STARTED.effective().replace("{blocks}", String.valueOf(dBlocks))).formatted(Formatting.GRAY));
        } catch (NumberFormatException e) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_NOT_A_NUMBER.effective().replace("{value}", strArrArgs[0]).replace("{usage}", getUsage())));
        }
    }

    public void tick() {
        if (!this.mc.isWorldLoaded() || this.destination == null) {
            this.active = false;
            return;
        }
        ClientPlayerEntity player = this.mc.getPlayer();
        if (player == null) {
            this.active = false;
            return;
        }
        if (this.waitTicks > 0) {
            this.waitTicks--;
            sendCurrentPosition(player);
            player.setVelocity(0.0d, 0.0d, 0.0d);
            return;
        }
        Vec3d current = player.getPos();
        double dRemainingY = this.destination.y - current.y;
        if (Math.abs(dRemainingY) < 0.5d) {
            this.active = false;
            player.setVelocity(0.0d, 0.0d, 0.0d);
            player.fallDistance = 0.0F;
            ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_VCLIPGRIM_ARRIVED.effective().replace("{steps}", String.valueOf(this.stepCount))).formatted(Formatting.GRAY));
            return;
        }
        double dMoveDist = Math.min(Math.abs(dRemainingY), ThreadLocalRandom.current().nextDouble(STEP_MIN, STEP_MAX));
        double dNewY = current.y + (dRemainingY > 0.0d ? dMoveDist : -dMoveDist);
        performMove(player, new Vec3d(current.x, dNewY, current.z));
        this.stepCount++;
        if (this.stepCount % 4 == 0) {
            this.waitTicks = ThreadLocalRandom.current().nextInt(12, 18);
        } else {
            this.waitTicks = ThreadLocalRandom.current().nextInt(2, 4);
        }
    }

    public void performMove(ClientPlayerEntity player, Vec3d vec3d) {
        player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(vec3d.x, vec3d.y, vec3d.z, player.getYaw(), player.getPitch(), true, player.horizontalCollision));
        player.setPosition(vec3d);
        player.setVelocity(0.0d, 0.0d, 0.0d);
        player.fallDistance = 0.0F;
    }

    public void sendCurrentPosition(ClientPlayerEntity player) {
        player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch(), true, player.horizontalCollision));
    }

    @Override
    public List<String> getSuggestions(String[] strArr, int i) {
        return List.of();
    }
}