package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.util.BlockUtil;
import aethereal.ui.setting.BooleanSetting;
import aethereal.type.ChunkLoadingRule;
import aethereal.type.CreeperApproachRule;
import aethereal.type.CreeperFarmApproachPhase;
import aethereal.util.CreeperFarmJumper;
import aethereal.type.CreeperFarmLootPhase;
import aethereal.type.CreeperFarmPatrolPhase;
import aethereal.util.CreeperFarmPhaseController;
import aethereal.util.CreeperFarmStatsHandler;
import aethereal.type.CreeperFarmUnloadPhase;
import aethereal.type.CreeperLootRule;
import aethereal.type.CreeperUnloadRule;
import aethereal.Expensive;
import aethereal.event.HandledScreenRenderEvent;
import aethereal.util.IteratorUtil;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.PacketReceiveEvent;
import aethereal.event.PlayerTickEvent;
import aethereal.event.Render2DEvent;
import aethereal.type.TpLootStage;
import aethereal.internal.TpLootStageSwitchMap;
import aethereal.model.Translation;
import aethereal.event.WorldLoadEvent;
import aethereal.event.WorldRenderEvent;

import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@Aliases(aliases = {"Auto Creeper Farm", "Creeper Bot", "Fun Time", "Spooky Time"})
public class CreeperFarmModule extends Module {
    public final Mc mc;
    public final BooleanSetting unloadSetting;
    public final BlockPos regionMin;
    public final BlockPos regionMax;
    public final CreeperFarmPhaseController phaseManager;

    public final CreeperFarmStatsHandler statsHandler;

    public final CreeperFarmUnloadPhase unloadPhase;

    public final CreeperFarmLootPhase lootPhase;

    public final CreeperFarmApproachPhase approachPhase;

    public final CreeperFarmPatrolPhase patrolPhase;

    public final CreeperFarmJumper jumper;

    public CreeperFarmModule() {
        super(ModuleTab.MISC, "Creeper Farm");
        this.mc = Mc.INSTANCE;
        this.unloadSetting = new BooleanSetting(Translation.clearText("Unload Gunpowder"));
        this.regionMin = new BlockPos(2220, 14, 2000);
        this.regionMax = new BlockPos(2320, 15, 2100);
        this.phaseManager = new CreeperFarmPhaseController(this);
        this.statsHandler = new CreeperFarmStatsHandler();
        this.unloadPhase = new CreeperFarmUnloadPhase(this);
        this.lootPhase = new CreeperFarmLootPhase(this);
        this.approachPhase = new CreeperFarmApproachPhase();
        this.patrolPhase = new CreeperFarmPatrolPhase(this);
        this.jumper = new CreeperFarmJumper();
        this.phaseManager.addRule(new CreeperLootRule(this.lootPhase));
        this.phaseManager.addRule(new CreeperUnloadRule(this.unloadPhase));
        this.phaseManager.addRule(new CreeperApproachRule(this));
        this.phaseManager.addRule(new ChunkLoadingRule());
        register(PacketReceiveEvent.class, class051Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded()) {
                this.statsHandler.handlePacket(class051Var);
                this.unloadPhase.handlePacket(class051Var);
            }
        });
        register(WorldRenderEvent.class, class016Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                Expensive.INSTANCE.drawEngine().colorStack();
                if (BlockUtil.distanceToRegion(this.mc.getPlayer().getBlockPos(), this.regionMin, this.regionMax) < 30.0d) {
                    new Box(new Vec3d(this.regionMin.getX(), this.regionMin.getY(), this.regionMin.getZ()), new Vec3d(this.regionMax.getX() + 1, this.regionMax.getY() + 1, this.regionMax.getZ() + 1));
                }
            }
        });
        register(WorldLoadEvent.class, class086Var -> {
            if (isState()) {
                this.jumper.reset();
            }
        });
        register(Render2DEvent.class, class311Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                this.statsHandler.drawOverlay(class311Var);
            }
        });
        register(HandledScreenRenderEvent.class, class015Var -> {
            if (isState() && this.mc.isWorldLoaded() && this.phaseManager.isPhase(TpLootStage.UNLOADING)) {
                if ((this.mc.getCurrentScreen()) instanceof GenericContainerScreen currentScreen ) {
                    GenericContainerScreen genericContainerScreen = currentScreen;
                    this.unloadPhase.handleContainer(genericContainerScreen.getScreenHandler(), StringHelper.stripTextFormat(genericContainerScreen.getTitle().getString()));
                }
            }
        });
        register(PlayerTickEvent.class, class130Var -> {
            ClientPlayerEntity player = this.mc.getPlayer();
            if (isState() && this.mc.isWorldLoaded()) {
                List<CreeperEntity> creepersSortedByDistance = getCreepersSortedByDistance();
                if (BlockUtil.isWithinRegion(player.getBlockPos(), this.regionMin, this.regionMax)) {
                    this.phaseManager.tickPhaseRules();
                    this.jumper.tick(player);
                    CreeperEntity creeperEntity = !creepersSortedByDistance.isEmpty() ? (CreeperEntity) creepersSortedByDistance.getFirst() : null;
                    if (creeperEntity != null && player.getAttackCooldownProgress(0.4f) >= 0.9f && player.distanceTo(creeperEntity) < 5.0f) {
                        this.mc.getInteractionManager().attackEntity(player, creeperEntity);
                        player.swingHand(Hand.MAIN_HAND);
                    }
                    switch (TpLootStageSwitchMap.ordinalMap[this.phaseManager.getCurrentPhase().ordinal()]) {
                        case 1:
                            this.approachPhase.tickApproachPhase(player, creepersSortedByDistance);
                            break;
                        case 2:
                            this.patrolPhase.tickPatrolPhase(player);
                            break;
                        case 3:
                            this.lootPhase.tickLootingPhase(player);
                            break;
                        case 4:
                            this.unloadPhase.tickUnloadPhase(player);
                            break;
                    }
                }
            }
        });
    }

    public List<CreeperEntity> getCreepersSortedByDistance() {
        return IteratorUtil.toList(this.mc.getWorld().getEntities().iterator()).stream().filter(entity -> {
            if (entity instanceof CreeperEntity) {
                CreeperEntity creeperEntity = (CreeperEntity) entity;
                if (creeperEntity.getHealth() > 0.0f && creeperEntity.isAlive()) {
                    return true;
                }
            }
            return false;
        }).map(entity2 -> {
            return (CreeperEntity) entity2;
        }).filter(creeperEntity -> {
            return BlockUtil.isWithinRegion(creeperEntity.getBlockPos(), this.regionMin, this.regionMax);
        }).sorted(Comparator.comparingDouble(creeperEntity2 -> {
            return creeperEntity2.distanceTo(this.mc.getPlayer());
        })).toList();
    }

    @Override
    public void activate() {
        super.activate();
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    public BlockPos getRegionMin() {
        return this.regionMin;
    }

    public BlockPos getRegionMax() {
        return this.regionMax;
    }

    public CreeperFarmPhaseController getPhaseManager() {
        return this.phaseManager;
    }

    public CreeperFarmStatsHandler getStatsHandler() {
        return this.statsHandler;
    }
}
