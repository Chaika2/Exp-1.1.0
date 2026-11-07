package aethereal.type;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.equipment.EquipmentModelLoader;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.session.Session;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.hit.HitResult;

public enum Mc {
    INSTANCE;

    public static final EquipmentModelLoader equipmentModelLoader = new EquipmentModelLoader();
    public MinecraftClient minecraft;

    public ClientWorld getWorld() {
        ensureClient();
        return this.minecraft.world;
    }

    public InGameHud getInGameHud() {
        ensureClient();
        return this.minecraft.inGameHud;
    }

    public ServerInfo getServerInfo() {
        ensureClient();
        return this.minecraft.getCurrentServerEntry();
    }

    public Session getSession() {
        ensureClient();
        return this.minecraft.getSession();
    }

    public ClientPlayerEntity getPlayer() {
        ensureClient();
        return this.minecraft.player;
    }

    public int getCurrentFps() {
        return this.minecraft.getCurrentFps();
    }

    public HitResult getCrosshairTarget() {
        ensureClient();
        return this.minecraft.crosshairTarget;
    }

    public RenderTickCounter getRenderTickCounter() {
        ensureClient();
        return this.minecraft.getRenderTickCounter();
    }

    public ClientPlayerInteractionManager getInteractionManager() {
        ensureClient();
        return this.minecraft.interactionManager;
    }

    public GameOptions getGameOptions() {
        ensureClient();
        return this.minecraft.options;
    }

    public GameRenderer getGameRenderer() {
        ensureClient();
        return this.minecraft.gameRenderer;
    }

    public Mouse getMouse() {
        ensureClient();
        return this.minecraft.mouse;
    }

    public PlayerSkinProvider getSkinProvider() {
        ensureClient();
        return this.minecraft.getSkinProvider();
    }

    public Camera getCamera() {
        return getGameRenderer().getCamera();
    }

    public Entity getCameraEntity() {
        ensureClient();
        return this.minecraft.cameraEntity;
    }

    public boolean isWorldLoaded() {
        Mc class815Var = INSTANCE;
        return (class815Var.getWorld() == null || class815Var.getPlayer() == null || class815Var.getNetworkHandler() == null || class815Var.getInteractionManager() == null) ? false : true;
    }

    public ResourceManager getResourceManager() {
        ensureClient();
        return this.minecraft.getResourceManager();
    }

    public TextureManager getTextureManager() {
        ensureClient();
        return this.minecraft.getTextureManager();
    }

    public Tessellator getTesselator() {
        ensureClient();
        return Tessellator.getInstance();
    }

    public ItemRenderer getItemRenderer() {
        ensureClient();
        return this.minecraft.getItemRenderer();
    }

    public TextRenderer getTextRenderer() {
        ensureClient();
        return this.minecraft.textRenderer;
    }

    public Window getWindow() {
        ensureClient();
        return this.minecraft.getWindow();
    }

    public ServerInfo getCurrentServer() {
        ensureClient();
        return this.minecraft.getCurrentServerEntry();
    }

    public Screen getCurrentScreen() {
        ensureClient();
        return this.minecraft.currentScreen;
    }

    public ScreenHandler getCurrentScreenHandler() {
        return getPlayer().currentScreenHandler;
    }

    public Framebuffer getFramebuffer() {
        ensureClient();
        return this.minecraft.getFramebuffer();
    }

    public ItemModelManager getItemModelManager() {
        ensureClient();
        return this.minecraft.getItemModelManager();
    }

    public EntityRenderDispatcher getEntityRenderDispatcher() {
        ensureClient();
        return this.minecraft.getEntityRenderDispatcher();
    }

    public ClientPlayNetworkHandler getNetworkHandler() {
        ensureClient();
        return this.minecraft.getNetworkHandler();
    }

    public float getTickDelta() {
        ensureClient();
        return this.minecraft.getRenderTickCounter().getTickDelta(false);
    }

    public boolean isSingleplayer() {
        ensureClient();
        return this.minecraft.isInSingleplayer();
    }

    public void setCurrentScreen(Screen screen) {
        ensureClient();
        this.minecraft.setScreen(screen);
    }

    public void ensureClient() {
        if (this.minecraft == null) {
            this.minecraft = MinecraftClient.getInstance();
        }
    }

    public EntityRendererFactory.Context createEntityRendererContext() {
        return new EntityRendererFactory.Context(getEntityRenderDispatcher(), this.minecraft.getItemModelManager(), this.minecraft.getMapRenderer(), this.minecraft.getBlockRenderManager(), getResourceManager(), this.minecraft.getLoadedEntityModels(), equipmentModelLoader, getTextRenderer());
    }

    public MinecraftClient getMinecraft() {
        return this.minecraft;
    }
}
