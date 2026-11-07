package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.ui.setting.BooleanSetting;
import aethereal.resource.ClasspathResource;
import aethereal.ui.setting.ColorSetting;
import aethereal.render.ColorStack;
import aethereal.internal.DirectionSwitchMap;
import aethereal.util.DrawEngine;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.util.IteratorUtil;
import aethereal.Lang;
import aethereal.util.MathUtil;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.util.PlayerSnapshotManager;
import aethereal.math.ProjectileTrajectory;
import aethereal.util.ProjectionUtil;
import aethereal.util.RaycastUtil;
import aethereal.event.Render2DEvent;
import aethereal.math.Rotation;
import aethereal.util.RotationManager;
import aethereal.util.ServerUtil;
import aethereal.render.ShapeRenderer;
import aethereal.type.ShapeType;
import aethereal.util.StencilBufferUtil;
import aethereal.render.ThemePalette;
import aethereal.model.TrajectoryPoint;
import aethereal.event.WorldRenderEvent;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.TridentItem;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;
import org.joml.Vector2f;

@Aliases(aliases = {"Pearl Prediction", "Projectile Prediction", "Arrow Prediction"})
public class ProjectilePredictionModule extends Module {
    public final Mc mc;
    public final ColorSetting color;
    public final GlTexture clockTexture;
    public final BooleanSetting handPrediction;
    public final BooleanSetting areaPrediction;

    public ProjectilePredictionModule() {
        super(ModuleTab.RENDER, "Projectile Prediction");
        this.mc = Mc.INSTANCE;
        this.color = new ColorSetting(Lang.PROJECTILE_PREDICTION_COLOR);
        this.clockTexture = new GlTexture(new ClasspathResource("/icons/trajectory/clock.png"));
        this.handPrediction = new BooleanSetting(Lang.PROJECTILE_PREDICTION_HAND, Lang.PROJECTILE_PREDICTION_HAND_DESC).setValue(true);
        this.areaPrediction = new BooleanSetting(Lang.PROJECTILE_PREDICTION_AREA, Lang.PROJECTILE_PREDICTION_AREA_DESC).setValue(true);
        addSettings(this.handPrediction, this.areaPrediction, this.color);
        register(Render2DEvent.class, class311Var -> {
            if (this.mc.isWorldLoaded() && isState() && class311Var.isPre()) {
                DrawEngine class154VarDrawEngine = Expensive.INSTANCE.drawEngine();
                ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
                Matrix4f positionMatrix = class311Var.matrixStack().peek().getPositionMatrix();
                boolean z = false;
                for (ProjectileTrajectory class371Var : PlayerSnapshotManager.INSTANCE.getProjectiles()) {
                    List listSteps = class371Var.steps();
                    Vec3d vec3dPos = ((TrajectoryPoint) listSteps.getLast()).pos();
                    if (!z) {
                        class154VarDrawEngine.begin();
                        z = true;
                    }
                    Optional<Vector2f> optionalWorldToScreen = ProjectionUtil.worldToScreen(vec3dPos);
                    if (optionalWorldToScreen.isPresent()) {
                        Vector2f vector2f = optionalWorldToScreen.get();
                        String str = String.format("%.1f", Double.valueOf(listSteps.size() / 20.0f));
                        int width = (int) Fonts.INTER_SEMIBOLD.get().getWidth(str, 12.0f);
                        int iX = (int) (vector2f.x() + 5.0f);
                        int iY = (int) (vector2f.y() - 20.0f);
                        class154VarDrawEngine.roundedRectangle(positionMatrix, iX, iY, width + 10 + 6 + (6 * 2), 21, 4.0f, 2.5f, class115VarColorStack.computeColor(0.12f, 129, 135, StencilBufferUtil.STENCIL_MASK), class115VarColorStack.computeColor(0.7f, 6, 7, 18));
                        int i = iX + 6;
                        int i2 = (int) (iY + (21 / 2.0f));
                        class154VarDrawEngine.msdfFontVerticalC(positionMatrix, Fonts.INTER_SEMIBOLD.get(), str, iX + 6, i2, 12.0f, 0.0f, class115VarColorStack.computeColor(13948641));
                        class154VarDrawEngine.texture(positionMatrix, i + width + 6, (int) (i2 - (10 / 2.0f)), 10, 10, class154VarDrawEngine.bindTexture(this.clockTexture.textureWithSTB()), class115VarColorStack.white());
                        int i3 = (iY - 3) - 20;
                        class154VarDrawEngine.roundedRectangle(positionMatrix, iX, i3, 20.0f, 20.0f, 4.0f, 2.5f, class115VarColorStack.computeColor(0.12f, 129, 135, StencilBufferUtil.STENCIL_MASK), class115VarColorStack.computeColor(0.7f, 6, 7, 18));
                        class154VarDrawEngine.itemStack(positionMatrix, class371Var.stack(), (iX + 10) - 6, (i3 + 10) - 6, 0.375f, 1.0f);
                    }
                }
                if (z) {
                    class154VarDrawEngine.end();
                }
            }
        });
        register(WorldRenderEvent.class, class016Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                MatrixStack matrixStack = class016Var.matrixStack();
                ColorStack class115VarColorStack = Expensive.INSTANCE.drawEngine().colorStack();
                Rotation interpolatedRotation = RotationManager.INSTANCE.getInterpolatedRotation();
                if (this.handPrediction.isValue()) {
                    Iterator it = this.mc.getPlayer().getHandItems().iterator();
                    while (it.hasNext()) {
                        drawPredictionItem(matrixStack, (ItemStack) it.next(), interpolatedRotation);
                    }
                }
                for (ProjectileTrajectory class371Var : PlayerSnapshotManager.INSTANCE.getProjectiles()) {
                    List listSteps = class371Var.steps();
                    if (listSteps != null && listSteps.size() >= 2) {
                        ItemStack itemStackStack = class371Var.stack();
                        for (int i = 1; i < listSteps.size(); i++) {
                            TrajectoryPoint class372Var = (TrajectoryPoint) listSteps.get(i - 1);
                            TrajectoryPoint class372Var2 = (TrajectoryPoint) listSteps.get(i);
                            ShapeRenderer.INSTANCE.addLine(matrixStack.peek().getPositionMatrix(), class372Var.pos(), class372Var2.pos(), class115VarColorStack.computeColor(this.color.getColor(), MathUtil.clamp((class372Var2.tick() - this.mc.getPlayer().age) / 25.0f, 0.0f, 1.0f)), 3.0f);
                        }
                        Vec3d vec3dPos = ((TrajectoryPoint) listSteps.getLast()).pos();
                        if (this.areaPrediction.isValue()) {
                            int iComputeColor = class115VarColorStack.computeColor(this.color.getColor(), 0.5f);
                            if ((itemStackStack.getItem() instanceof LingeringPotionItem) || (itemStackStack.getItem() instanceof SplashPotionItem)) {
                                drawAreaCircle(matrixStack, vec3dPos, 3.0f, iComputeColor);
                            } else if (isSpecialSnowball(itemStackStack)) {
                                drawAreaCircle(matrixStack, vec3dPos, 7.0f, iComputeColor);
                            }
                        }
                    }
                }
            }
        });
    }

    public void drawPredictionItem(MatrixStack matrixStack, ItemStack itemStack, Rotation class007Var) {
        if ((itemStack.getItem() instanceof SplashPotionItem) || (itemStack.getItem() instanceof LingeringPotionItem)) {
            drawPotionPrediction(matrixStack, itemStack, class007Var);
            return;
        }
        List<BlockHitResult> trajectoryResult = getTrajectoryResult(itemStack, class007Var);
        if (trajectoryResult == null || trajectoryResult.isEmpty()) {
            return;
        }
        renderProjectileResults(matrixStack, trajectoryResult, itemStack);
    }

    public void drawPotionPrediction(MatrixStack matrixStack, ItemStack itemStack, Rotation class007Var) {
        ClientPlayerEntity player = this.mc.getPlayer();
        PotionEntity potionEntity = new PotionEntity(this.mc.getWorld(), player, itemStack);
        Vec3d directionVector = class007Var.getDirectionVector();
        Vec3d vec3d = new Vec3d(directionVector.x, -MathHelper.sin((class007Var.getPitch() - 20.0f) * 0.017453292f), directionVector.z);
        Vec3d vec3dAdd = player.getEyePos().add(MathUtil.interpolate(player).subtract(player.getPos()));
        Vec3d vec3dAdd2 = vec3d.multiply(0.5d / ((double) MathHelper.sqrt(vec3d.toVector3f().lengthSquared()))).add(getMotion(potionEntity));
        List<Box> list = IteratorUtil.toList(this.mc.getWorld().getEntities().iterator()).stream().filter(entity -> {
            return entity.canBeHitByProjectile() && entity != potionEntity.getOwner();
        }).map(entity2 -> {
            return entity2.getBoundingBox().expand(0.30000001192092896d);
        }).toList();
        ArrayList arrayList = new ArrayList();
        arrayList.add(vec3dAdd);
        Vec3d vec3d2 = vec3dAdd;
        BlockHitResult blockHitResult = null;
        for (int i = 0; i < 300; i++) {
            Vec3d vec3dAdd3 = calculateMotion(potionEntity, vec3d2, vec3dAdd2).add(0.0d, -potionEntity.getFinalGravity(), 0.0d);
            vec3dAdd2 = vec3dAdd3;
            Vec3d vec3dAdd4 = vec3d2.add(vec3dAdd3);
            BlockHitResult blockHitResultMethod004 = raycastStep(potionEntity, list, vec3d2, vec3dAdd4);
            if (blockHitResultMethod004 != null) {
                arrayList.add(blockHitResultMethod004.getPos());
                blockHitResult = blockHitResultMethod004;
                break;
            } else {
                arrayList.add(vec3dAdd4);
                vec3d2 = vec3dAdd4;
            }
        }
        if (blockHitResult == null) {
            blockHitResult = new BlockHitResult(BlockPos.ORIGIN.toCenterPos(), Direction.DOWN, BlockPos.ORIGIN.down(999), true);
        }
        matrixStack.peek().getPositionMatrix();
        this.color.getColor();
        Vec3d pos = blockHitResult.getPos();
        int iArgb = blockHitResult.getBlockPos().equals(BlockPos.ORIGIN) ? ThemePalette.darkRed.argb() : this.color.getColor();
        drawLandingMarker(matrixStack, pos, blockHitResult.getSide(), iArgb);
        if (this.areaPrediction.isValue()) {
            drawAreaCircle(matrixStack, pos, itemStack.getItem() instanceof LingeringPotionItem ? 4.0f : 3.0f, iArgb);
        }
    }

    public List<BlockHitResult> getTrajectoryResult(ItemStack itemStack, Rotation class007Var) {
        Item item = this.mc.getPlayer().getActiveItem().getItem();
        Item item2 = itemStack.getItem();
        Objects.requireNonNull(item2);
        if (item2 instanceof TridentItem) {
            if (((TridentItem) item2).equals(item) && this.mc.getPlayer().getItemUseTime() >= 10) {
                return checkTrajectory(new TridentEntity(this.mc.getWorld(), this.mc.getPlayer(), itemStack), 2.5d, class007Var);
            }
            return new ArrayList();
        }
        if (item2 instanceof SnowballItem) {
            return checkTrajectory(new SnowballEntity(this.mc.getWorld(), this.mc.getPlayer(), itemStack), 1.5d, class007Var);
        }
        if (item2 instanceof EggItem) {
            return checkTrajectory(new EggEntity(this.mc.getWorld(), this.mc.getPlayer(), itemStack), 1.5d, class007Var);
        }
        if (item2 instanceof EnderPearlItem) {
            return checkTrajectory(new EnderPearlEntity(this.mc.getWorld(), this.mc.getPlayer(), itemStack), 1.5d, class007Var);
        }
        if (item2 instanceof BowItem) {
            if (((BowItem) item2).equals(item) && this.mc.getPlayer().isUsingItem()) {
                return checkTrajectory(new ArrowEntity(this.mc.getWorld(), this.mc.getPlayer(), itemStack, itemStack), 3.0f * MathUtil.clamp((this.mc.getPlayer().getItemUseTime() + this.mc.getTickDelta()) / 20.0f, 0.0f, 1.0f), class007Var);
            }
            return new ArrayList();
        }
        if (item2 instanceof SplashPotionItem) {
            return getPotionTrajectory(itemStack, class007Var);
        }
        if (item2 instanceof LingeringPotionItem) {
            return getPotionTrajectory(itemStack, class007Var);
        }
        if (item2 instanceof CrossbowItem) {
            if (CrossbowItem.isCharged(itemStack)) {
                ChargedProjectilesComponent chargedProjectilesComponent = (ChargedProjectilesComponent) itemStack.get(DataComponentTypes.CHARGED_PROJECTILES);
                ArrayList arrayList = new ArrayList();
                if (chargedProjectilesComponent != null && !chargedProjectilesComponent.isEmpty()) {
                    float f = ((ItemStack) chargedProjectilesComponent.getProjectiles().getFirst()).isOf(Items.FIREWORK_ROCKET) ? 1.6f : 3.15f;
                    arrayList.add(checkTrajectory(class007Var.getDirectionVector(), new ArrowEntity(this.mc.getWorld(), this.mc.getPlayer(), itemStack, itemStack), f, false));
                    if (chargedProjectilesComponent.getProjectiles().size() > 1) {
                        arrayList.add(checkTrajectory(class007Var.add(-10.0f, 0.0f).getDirectionVector(), new ArrowEntity(this.mc.getWorld(), this.mc.getPlayer(), itemStack, itemStack), f, false));
                        arrayList.add(checkTrajectory(class007Var.add(10.0f, 0.0f).getDirectionVector(), new ArrowEntity(this.mc.getWorld(), this.mc.getPlayer(), itemStack, itemStack), f, false));
                    }
                }
                return arrayList;
            }
            return new ArrayList();
        }
        return new ArrayList();
    }

    public void renderProjectileResults(MatrixStack matrixStack, List<BlockHitResult> list, ItemStack itemStack) {
        for (BlockHitResult blockHitResult : list) {
            Vec3d pos = blockHitResult.getPos();
            int iArgb = blockHitResult.getBlockPos().equals(BlockPos.ORIGIN) ? ThemePalette.darkRed.argb() : this.color.getColor();
            if (this.areaPrediction.isValue()) {
                if ((itemStack.getItem() instanceof SplashPotionItem) || (itemStack.getItem() instanceof LingeringPotionItem)) {
                    drawAreaCircle(matrixStack, pos, 3.0f, iArgb);
                } else if (isSpecialSnowball(itemStack)) {
                    drawAreaCircle(matrixStack, pos, 7.0f, iArgb);
                }
            }
            drawLandingMarker(matrixStack, pos, blockHitResult.getSide(), iArgb);
        }
    }

    public void drawLandingMarker(MatrixStack matrixStack, Vec3d vec3d, Direction direction, int i) {
        Vec3d vec3dAdd = vec3d.subtract(this.mc.getEntityRenderDispatcher().camera.getPos()).add(Vec3d.of(direction.getVector()).multiply(0.001d));
        matrixStack.push();
        matrixStack.translate(vec3dAdd.x, vec3dAdd.y, vec3dAdd.z);
        switch (DirectionSwitchMap.directionOrdinals[direction.ordinal()]) {
            case 1:
            case 2:
                matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0f));
                break;
            case 3:
            case 4:
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
                break;
        }
        Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
        for (int i2 = 0; i2 < 48; i2++) {
            float f = (float) (((((double) i2) * 3.141592653589793d) * 2.0d) / ((double) 48));
            float f2 = (float) (((((double) (i2 + 1)) * 3.141592653589793d) * 2.0d) / ((double) 48));
            float fCos = (float) Math.cos(f);
            float fSin = (float) Math.sin(f);
            float fCos2 = (float) Math.cos(f2);
            float fSin2 = (float) Math.sin(f2);
            float f3 = 0.35f - 0.02f;
            float f4 = 0.35f + 0.02f;
            ShapeRenderer.INSTANCE.addFilledQuad(positionMatrix, new Vec3d(fCos * f3, 0.0d, fSin * f3), new Vec3d(fCos * f4, 0.0d, fSin * f4), new Vec3d(fCos2 * f4, 0.0d, fSin2 * f4), new Vec3d(fCos2 * f3, 0.0d, fSin2 * f3), i);
        }
        float f5 = 0.35f * 0.6f;
        ShapeRenderer.INSTANCE.addFilledQuad(positionMatrix, new Vec3d(-f5, 0.0d, -0.02f), new Vec3d(-f5, 0.0d, 0.02f), new Vec3d(f5, 0.0d, 0.02f), new Vec3d(f5, 0.0d, -0.02f), i);
        ShapeRenderer.INSTANCE.addFilledQuad(positionMatrix, new Vec3d(-0.02f, 0.0d, -f5), new Vec3d(-0.02f, 0.0d, f5), new Vec3d(0.02f, 0.0d, f5), new Vec3d(0.02f, 0.0d, -f5), i);
        matrixStack.pop();
    }

    public void drawAreaCircle(MatrixStack matrixStack, Vec3d vec3d, float f, int i) {
        Vec3d vec3dSubtract = vec3d.subtract(this.mc.getEntityRenderDispatcher().camera.getPos());
        matrixStack.push();
        matrixStack.translate(vec3dSubtract.x, vec3dSubtract.y + 0.01d, vec3dSubtract.z);
        Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
        for (int i2 = 0; i2 < 64; i2++) {
            float f2 = (float) (((((double) i2) * 3.141592653589793d) * 2.0d) / ((double) 64));
            float f3 = (float) (((((double) (i2 + 1)) * 3.141592653589793d) * 2.0d) / ((double) 64));
            float fCos = (float) Math.cos(f2);
            float fSin = (float) Math.sin(f2);
            float fCos2 = (float) Math.cos(f3);
            float fSin2 = (float) Math.sin(f3);
            float f4 = f - 0.03f;
            float f5 = f + 0.03f;
            ShapeRenderer.INSTANCE.addFilledQuad(positionMatrix, new Vec3d(fCos * f4, 0.0d, fSin * f4), new Vec3d(fCos * f5, 0.0d, fSin * f5), new Vec3d(fCos2 * f5, 0.0d, fSin2 * f5), new Vec3d(fCos2 * f4, 0.0d, fSin2 * f4), i);
        }
        matrixStack.pop();
    }

    public List<BlockHitResult> getPotionTrajectory(ItemStack itemStack, Rotation class007Var) {
        Vec3d directionVector = class007Var.getDirectionVector();
        return List.of(checkTrajectory(new Vec3d(directionVector.x, -MathHelper.sin((class007Var.getPitch() - 20.0f) * 0.017453292f), directionVector.z), new PotionEntity(this.mc.getWorld(), this.mc.getPlayer(), itemStack), 0.5d, true));
    }

    public List<BlockHitResult> checkTrajectory(ProjectileEntity projectileEntity, double d, Rotation class007Var) {
        return List.of(checkTrajectory(class007Var.getDirectionVector(), projectileEntity, d, true));
    }

    public BlockHitResult checkTrajectory(Vec3d vec3d, ProjectileEntity projectileEntity, double d, boolean z) {
        return checkTrajectory(this.mc.getPlayer().getEyePos(), vec3d, projectileEntity, d, z);
    }

    public BlockHitResult checkTrajectory(Vec3d vec3d, Vec3d vec3d2, ProjectileEntity projectileEntity, double d, boolean z) {
        ClientPlayerEntity player = this.mc.getPlayer();
        this.mc.getTickDelta();
        return traceTrajectory(vec3d.add(MathUtil.interpolate(player).subtract(player.getPos())), vec3d2.multiply(d / ((double) MathHelper.sqrt(vec3d2.toVector3f().lengthSquared()))).add(getMotion(projectileEntity)), projectileEntity, 300);
    }

    public BlockHitResult traceTrajectory(Vec3d vec3d, Vec3d vec3d2, ProjectileEntity projectileEntity, int i) {
        return traceTrajectory(vec3d, vec3d2, projectileEntity, i, IteratorUtil.toList(this.mc.getWorld().getEntities().iterator()).stream().filter(entity -> {
            return entity.canBeHitByProjectile() && entity != projectileEntity.getOwner();
        }).map(entity2 -> {
            return entity2.getBoundingBox().expand(0.30000001192092896d);
        }).toList());
    }

    public BlockHitResult traceTrajectory(Vec3d vec3d, Vec3d vec3d2, ProjectileEntity projectileEntity, int i, List<Box> list) {
        Objects.requireNonNull(projectileEntity);
        if (projectileEntity instanceof ThrownItemEntity) {
            ThrownItemEntity thrownItemEntity = (ThrownItemEntity) projectileEntity;
            for (int i2 = 0; i2 < i; i2++) {
                Vec3d vec3dAdd = calculateMotion(thrownItemEntity, vec3d, vec3d2).add(0.0d, -thrownItemEntity.getFinalGravity(), 0.0d);
                vec3d2 = vec3dAdd;
                Vec3d vec3dAdd2 = vec3d.add(vec3dAdd);
                BlockHitResult blockHitResultMethod004 = raycastStep(thrownItemEntity, list, vec3d, vec3dAdd2);
                if (blockHitResultMethod004 != null) {
                    return blockHitResultMethod004;
                }
                vec3d = vec3dAdd2;
            }
        } else {
            for (int i3 = 0; i3 < i; i3++) {
                Vec3d vec3dAdd3 = vec3d.add(vec3d2);
                BlockHitResult blockHitResultMethod005 = raycastStep(projectileEntity, list, vec3d, vec3dAdd3);
                if (blockHitResultMethod005 != null) {
                    return blockHitResultMethod005;
                }
                vec3d2 = calculateMotion(projectileEntity, vec3d, vec3d2.add(0.0d, -projectileEntity.getFinalGravity(), 0.0d));
                vec3d = vec3dAdd3;
            }
        }
        return new BlockHitResult(BlockPos.ORIGIN.toCenterPos(), Direction.DOWN, BlockPos.ORIGIN.down(999), true);
    }

    public BlockHitResult raycastStep(Entity entity, List<Box> list, Vec3d vec3d, Vec3d vec3d2) {
        BlockHitResult blockHitResultRaycast = RaycastUtil.raycast(vec3d, vec3d2, RaycastContext.ShapeType.COLLIDER, entity);
        BlockHitResult blockHitResultRaycast2 = Box.raycast(list, vec3d, blockHitResultRaycast.getPos(), BlockPos.ORIGIN);
        if (blockHitResultRaycast2 != null && blockHitResultRaycast2.getType() != HitResult.Type.MISS) {
            return blockHitResultRaycast2;
        }
        if (blockHitResultRaycast.getType() != HitResult.Type.MISS || vec3d2.y < -128.0d) {
            return blockHitResultRaycast;
        }
        return null;
    }

    public Pair<List<ChunkPos>, List<TrajectoryPoint>> predictEntity(Entity entity, Vec3d vec3d, Vec3d vec3d2, boolean z) {
        ArrayList arrayList = new ArrayList();
        HashSet hashSet = new HashSet();
        for (int i = 0; i < 1000; i++) {
            if (z) {
                vec3d = calculateMotion(entity, vec3d2, vec3d.add(0.0d, -entity.getFinalGravity(), 0.0d));
            }
            Vec3d vec3dAdd = vec3d2.add(vec3d);
            BlockHitResult blockHitResultRaycast = RaycastUtil.raycast(vec3d2, vec3dAdd, RaycastContext.ShapeType.COLLIDER, entity);
            arrayList.add(new TrajectoryPoint(this.mc.getPlayer().age + i, vec3d, vec3d2, blockHitResultRaycast.getPos()));
            hashSet.add(new ChunkPos(BlockPos.ofFloored(vec3d2)));
            if (blockHitResultRaycast.getType() != HitResult.Type.MISS || vec3dAdd.y < -128.0d) {
                break;
            }
            if (!z) {
                vec3d = calculateMotion(entity, vec3d2, vec3d).add(0.0d, -entity.getFinalGravity(), 0.0d);
            }
            vec3d2 = vec3dAdd;
        }
        return new Pair<>(new ArrayList(hashSet), arrayList);
    }

    public Vec3d calculateMotion(Entity entity, Vec3d vec3d, Vec3d vec3d2) {
        double d;
        boolean zIsIn = this.mc.getWorld().getBlockState(BlockPos.ofFloored(vec3d)).getFluidState().isIn(FluidTags.WATER);
        Objects.requireNonNull(entity);
        if (entity instanceof TridentEntity) {
            d = 0.99d;
        } else if ((entity instanceof PersistentProjectileEntity) && zIsIn) {
            d = 0.6d;
        } else {
            d = !zIsIn ? 0.99d : 0.8d;
        }
        return vec3d2.multiply(d);
    }

    public Vec3d getMotion(ProjectileEntity projectileEntity) {
        int protocolVersion = ServerUtil.getProtocolVersion();
        if (((projectileEntity instanceof ArrowEntity) && ((ArrowEntity) projectileEntity).getItemStack().isOf(Items.CROSSBOW)) || ((projectileEntity instanceof ThrownItemEntity) && protocolVersion > 754 && protocolVersion < 767 && !ServerUtil.isConnectedToServer("holyworld"))) {
            return Vec3d.ZERO;
        }
        Vec3d[] vec3dArr = (Vec3d[]) PlayerSnapshotManager.INSTANCE.getSnapshots(this.mc.getPlayer(), 2).map(class373Var -> {
            return class373Var.pos;
        }).toList().toArray(i -> {
            return new Vec3d[i];
        });
        return vec3dArr.length < 3 ? Vec3d.ZERO : MathUtil.interpolate(vec3dArr[1].subtract(vec3dArr[2]), vec3dArr[0].subtract(vec3dArr[1]));
    }

    public boolean isSpecialSnowball(ItemStack itemStack) {
        return (itemStack.getItem() instanceof SnowballItem) && ServerUtil.isConnectedToAllFuntimeServers() && ((NbtComponent) itemStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT)).copyNbt().contains("don-item");
    }
}
