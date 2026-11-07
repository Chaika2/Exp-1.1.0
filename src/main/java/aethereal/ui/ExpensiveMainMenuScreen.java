package aethereal.ui;

import aethereal.Expensive;
import aethereal.math.DeltaTimeTracker;
import aethereal.math.Easings;
import aethereal.model.AltAccount;
import aethereal.model.LayoutScaleContext;
import aethereal.model.PixelPoint;
import aethereal.render.AnimationStack2;
import aethereal.render.AnimatedFloat;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.render.MsdfFont;
import aethereal.render.ScreenResolution;
import aethereal.render.SpringAnimator;
import aethereal.render.ThemePalette;
import aethereal.resource.ClasspathResource;
import aethereal.type.Mc;
import aethereal.util.AltManager;
import aethereal.util.DrawEngine;
import aethereal.util.MathUtil;
import aethereal.util.WeightedEngine;
import aethereal.util.WindowController;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ExpensiveMainMenuScreen extends Screen {
    private static final String TYPE_CRACK = "Crack";
    private static final String TYPE_MICROSOFT = "Microsoft";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static final String[] WALLPAPER_PATHS = {"/textures/background.png", "/textures/backgrounds/wall_0.jpg", "/textures/backgrounds/wall_1.jpg", "/textures/backgrounds/wall_2.jpg", "/textures/backgrounds/wall_3.jpg"};
    private final Identifier[] wallpaperIds = new Identifier[WALLPAPER_PATHS.length];
    private final int[][] wallpaperSize = new int[WALLPAPER_PATHS.length][2];
    private final GlTexture logoTexture = new GlTexture(new ClasspathResource("/textures/zort.png"));
    private final GlTexture arrowLeft = new GlTexture(new ClasspathResource("/icons/menu/new/arrow_left.png"));
    private final GlTexture arrowRight = new GlTexture(new ClasspathResource("/icons/menu/new/arrow_right.png"));
    private final SpringAnimator arrowHoverLeft = new SpringAnimator(0.0f, 1.0f, 45.0f, 14.0f, 1.0f, 300, Easings.LINEAR);
    private final SpringAnimator arrowHoverRight = new SpringAnimator(0.0f, 1.0f, 45.0f, 14.0f, 1.0f, 300, Easings.LINEAR);
    private int backgroundIndex;
    private int backgroundTarget = -1;
    private float bgDirection = 1.0f;
    private final AnimatedFloat bgTransition = new AnimatedFloat(700, Easings.EASE_OUT_CUBIC);
    private float backgroundSwitchTime = -1.0f;
    private final GlTexture iconSingle = new GlTexture(new ClasspathResource("/icons/menu/new/gamepad.png"));
    private final GlTexture iconMulti = new GlTexture(new ClasspathResource("/icons/menu/new/wifi.png"));
    private final GlTexture iconSettings = new GlTexture(new ClasspathResource("/icons/menu/settings.png"));
    private final GlTexture iconAlt = new GlTexture(new ClasspathResource("/icons/menu/new/users.png"));
    private final GlTexture iconQuit = new GlTexture(new ClasspathResource("/icons/menu/new/cross.png"));
    private final GlTexture iconPower = new GlTexture(new ClasspathResource("/icons/menu/new/power.png"));
    private final AltManager alts;

    private boolean altPanelOpen;
    private final List<AltAccount> rows = new ArrayList();
    private String inputText = "";
    private boolean inputFocused;
    private float listScroll;
    private int hoveredRow = -1;

    private boolean unlocked;
    private boolean dragging;
    private boolean dragged;
    private double dragStartY;
    private float pull;

    private final DeltaTimeTracker deltaTracker = new DeltaTimeTracker();
    private final AnimationStack2 animationStack = new AnimationStack2();
    private final AnimatedFloat unlockAnimation = new AnimatedFloat(850, Easings.EASE_OUT_CUBIC);
    private final SpringAnimator[] hoverAnimators = new SpringAnimator[]{new SpringAnimator(0.0f, 1.0f, 45.0f, 14.0f, 1.0f, 300, Easings.LINEAR), new SpringAnimator(0.0f, 1.0f, 45.0f, 14.0f, 1.0f, 300, Easings.LINEAR), new SpringAnimator(0.0f, 1.0f, 45.0f, 14.0f, 1.0f, 300, Easings.LINEAR), new SpringAnimator(0.0f, 1.0f, 45.0f, 14.0f, 1.0f, 300, Easings.LINEAR), new SpringAnimator(0.0f, 1.0f, 45.0f, 14.0f, 1.0f, 300, Easings.LINEAR)};
    private float backgroundTime;
    private final float[] starPhases = new float[]{0.0f, 0.3f, 0.6f, 0.9f, 1.2f, 1.5f, 1.8f, 2.1f, 2.4f, 2.7f};

    private final String[] iconTooltips = {"Одиночная игра", "Сетевая игра", "Настройки", "Alt Manager", "Выход из игры"};

    public ExpensiveMainMenuScreen() {
        super(Text.literal("ZortexDLC"));
        this.alts = Expensive.INSTANCE.configManager().altManager();
        for (int i = 0; i < WALLPAPER_PATHS.length; i++) {
            try (InputStream wallpaperStream = new ClasspathResource(WALLPAPER_PATHS[i]).stream()) {
                NativeImage wallpaperImage = NativeImage.read(wallpaperStream);
                this.wallpaperSize[i][0] = wallpaperImage.getWidth();
                this.wallpaperSize[i][1] = wallpaperImage.getHeight();
                Identifier wallpaperId = Identifier.of("expensive", "wallpaper_" + i);
                Mc.INSTANCE.getTextureManager().registerTexture(wallpaperId, new NativeImageBackedTexture(wallpaperImage));
                this.wallpaperIds[i] = wallpaperId;
                Expensive.LOGGER.info("[bg] registered wallpaper {} size={}x{}", WALLPAPER_PATHS[i], Integer.valueOf(wallpaperImage.getWidth()), Integer.valueOf(wallpaperImage.getHeight()));
            } catch (IOException | RuntimeException e) {
                Expensive.LOGGER.error("[bg] failed to load wallpaper {}", WALLPAPER_PATHS[i], e);
            }
        }
    }

    @Override
    public void onDisplayed() {
        refreshRows();
        this.unlocked = false;
        this.dragging = false;
        this.dragged = false;
        this.dragStartY = 0.0f;
        this.pull = 0.0f;
        this.unlockAnimation.set(0.0f);
        for (SpringAnimator animator : this.hoverAnimators) {
            animator.target(0.0f);
        }
        int saved = Expensive.INSTANCE.configManager().menuStateConfig().loadMainMenuWallpaper();
        this.backgroundIndex = saved >= 0 && saved < this.wallpaperIds.length ? saved : 0;
        this.backgroundTarget = -1;
        this.backgroundSwitchTime = -1.0f;
        this.bgTransition.set(1.0f);
        Expensive.LOGGER.info("[bg] initial index={} wallpapers={}", this.backgroundIndex, Integer.valueOf(this.wallpaperIds.length));
    }

    private void refreshRows() {
        this.rows.clear();
        this.rows.addAll(this.alts.accounts());
        float max = listMaxScroll();
        if (this.listScroll > max) {
            this.listScroll = max;
        }
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(null);
        }
    }

    public boolean shouldPause() {
        return false;
    }

    public boolean shouldCloseOnEsc() {
        return this.altPanelOpen;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient mc = this.client;
        Window window = mc.getWindow();
        WindowController controller = Expensive.INSTANCE.windowController();
        ScreenResolution res = new ScreenResolution(this.width, this.height);
        float guiScale = (float) window.getScaleFactor();
        LayoutScaleContext lc = new LayoutScaleContext(res, guiScale);
        DrawEngine engine = Expensive.INSTANCE.drawEngine();
        ColorStack cs = engine.colorStack();
        MatrixStack ms = new MatrixStack();
        PixelPoint mp = new PixelPoint(mouseX, mouseY);
        DrawCtx ctx = new DrawCtx(controller.window(), res, ms, mp, engine, cs, mp, Expensive.INSTANCE.theme(), lc);

        engine.begin();
        animateMenu();
        ctx.fillVerticalGradientRect(0, 0, this.width, this.height, 0xD9090C17, 0xD9212A45);
        engine.end();
        drawWallpaperLayer(context);
        context.draw();
        engine.begin();
        drawLivingBackground(ctx, cs);
        ctx.fillRect(0, 0, this.width, this.height, cs.computeColor(0x22090C17));
        drawBrand(ctx, cs);
        drawLockView(ctx, cs);
        drawIconMenu(ctx, cs, mouseX, mouseY);
        drawBackgroundArrows(ctx, cs, mouseX, mouseY);
        drawUserName(ctx, cs);
        if (this.altPanelOpen) {
            drawAltPanel(ctx, cs, mouseX, mouseY);
        }
        engine.end();
    }

    private void animateMenu() {
        float delta = this.deltaTracker.elapsedUnit();
        this.backgroundTime += delta;
        WeightedEngine engine = new WeightedEngine(delta, this.animationStack);
        this.animationStack.begin();
        this.unlockAnimation.animate(engine);
        for (SpringAnimator animator : this.hoverAnimators) {
            animator.animate(engine);
        }
        this.arrowHoverLeft.animate(engine);
        this.arrowHoverRight.animate(engine);
        this.bgTransition.animate(engine);
        this.animationStack.end();
    }

    private float unlockProgress() {
        return Math.max(this.unlockAnimation.animatedValue(), this.pull);
    }

    private void drawWallpaperLayer(DrawContext context) {
        float p = Math.min(1.0f, Math.max(MathUtil.clamp(this.bgTransition.animatedValue(), 0.0f, 1.0f), bgSwitchProgress()));
        float t = this.backgroundTime;
        if (p >= 0.999f) {
            if (this.backgroundTarget >= 0 && this.backgroundTarget != this.backgroundIndex) {
                this.backgroundIndex = this.backgroundTarget;
            }
            this.backgroundTarget = -1;
            drawWallpaper(context, this.backgroundIndex, 0.0f, t, 0.62f);
            return;
        }
        if (this.backgroundTarget < 0 || this.backgroundTarget == this.backgroundIndex) {
            drawWallpaper(context, this.backgroundIndex, 0.0f, t, 0.62f);
            return;
        }
        float dir = this.bgDirection;
        drawWallpaper(context, this.backgroundIndex, -dir * p * this.width, t, 0.62f * (1.0f - p));
        drawWallpaper(context, this.backgroundTarget, dir * (1.0f - p) * this.width, t, 0.62f * p);
    }

    private void drawWallpaper(DrawContext context, int index, float offsetX, float t, float alpha) {
        if (index < 0 || index >= this.wallpaperIds.length) {
            return;
        }
        Identifier id = this.wallpaperIds[index];
        if (id == null || alpha <= 0.01f) {
            return;
        }
        int texWidth = this.wallpaperSize[index][0];
        int texHeight = this.wallpaperSize[index][1];
        if (texWidth <= 0 || texHeight <= 0) {
            return;
        }
        float zoom = 1.04f + 0.07f * (0.5f + 0.5f * (float) Math.sin(t * 0.11f));
        float panX = (float) Math.sin(t * 0.05f) * this.width * 0.02f;
        float panY = (float) Math.cos(t * 0.038f) * this.height * 0.02f;
        float texAspect = (float) texWidth / texHeight;
        float scrAspect = (float) this.width / this.height;
        float dw;
        float dh;
        if (scrAspect > texAspect) {
            dw = this.width * zoom;
            dh = dw / texAspect;
        } else {
            dh = this.height * zoom;
            dw = dh * texAspect;
        }
        float x = (this.width - dw) / 2.0f + panX + offsetX;
        float y = (this.height - dh) / 2.0f + panY;
        int color = (Math.round(MathUtil.clamp(alpha, 0.0f, 1.0f) * 255.0f) << 24) | 0xFFFFFF;
        context.drawTexture(RenderLayer::getGuiTextured, id, Math.round(x), Math.round(y), 0.0f, 0.0f, Math.round(dw), Math.round(dh), texWidth, texHeight, texWidth, texHeight, color);
    }

    private float bgSwitchProgress() {
        if (this.backgroundSwitchTime < 0.0f) {
            return 1.0f;
        }
        return MathUtil.clamp((this.backgroundTime - this.backgroundSwitchTime) / 0.7f, 0.0f, 1.0f);
    }

    private void drawLivingBackground(DrawCtx ctx, ColorStack cs) {
        ThemePalette palette = ctx.theme().palette();
        float t = this.backgroundTime;
        float w = this.width;
        float h = this.height;
        float[][] blobs = {{0.22f, 0.30f, 0.55f, 0.35f, 0.11f, 0.13f}, {0.78f, 0.24f, 0.50f, 0.33f, 0.09f, 0.15f}, {0.66f, 0.78f, 0.62f, 0.40f, 0.07f, 0.11f}, {0.15f, 0.72f, 0.45f, 0.30f, 0.13f, 0.09f}, {0.50f, 0.52f, 0.90f, 0.55f, 0.05f, 0.17f}};
        int[] colors = {palette.accentBright().argb(), palette.accent().argb(), ThemePalette.deepGreen.argb(), ThemePalette.blue.argb(), palette.surfaceOutline().tone(600).argb()};
        for (int i = 0; i < blobs.length; i++) {
            float[] b = blobs[i];
            float cx = w * b[0] + (float) Math.sin((t * b[4]) + (i * 1.7f)) * w * 0.10f;
            float cy = h * b[1] + (float) Math.cos((t * b[5]) + (i * 2.3f)) * h * 0.08f;
            float cw = w * b[2];
            float chh = h * b[3];
            int col = colors[i % colors.length];
            ctx.radialRoundedRectangle(cx - (cw / 2.0f), cy - (chh / 2.0f), cw, chh, cw * 0.5f, cs.computeColor(col, 0.055f), cs.computeColor(col, 0.0f));
        }
        drawFloatingStars(ctx, cs, palette, t, w, h);
    }

    private void drawFloatingStars(DrawCtx ctx, ColorStack cs, ThemePalette palette, float t, float w, float h) {
        for (int i = 0; i < this.starPhases.length; i++) {
            float phase = this.starPhases[i];
            float speed = 0.02f + ((i % 3) * 0.008f);
            float progress = (t * speed) + phase;
            float py = h + ((float) (progress % 1.0f) * -h);
            float px = ((w / this.starPhases.length) * i) + ((float) Math.sin((t * 0.4f) + (i * 1.3f)) * 30.0f) + (w * 0.05f);
            float twinkle = 0.5f + (0.5f * (float) Math.sin((t * 1.5f) + (i * 2.1f)));
            float size = 1.5f + ((i % 4) * 0.5f);
            ctx.circle(px, py, size, cs.computeColor(palette.accentBright().argb(), Math.round(60.0f + (90.0f * twinkle))));
        }
    }

    private void drawBrand(DrawCtx ctx, ColorStack cs) {
        ThemePalette palette = ctx.theme().palette();
        float progress = unlockProgress();
        float baseX = 16.0f;
        float baseY = 12.0f;
        float logoH = 30.0f;
        float logoW = logoH;
        if (this.logoTexture.textureWithSTB() != 0 && this.logoTexture.width > 0 && this.logoTexture.height > 0) {
            logoW = logoH * this.logoTexture.width / this.logoTexture.height;
        }
        ctx.texture(this.logoTexture, baseX, baseY, logoW, logoH,
                cs.computeColor(0xFFFFFFFF, Math.round(MathUtil.interpolate(150.0f, 255.0f, progress))));
        float textX = baseX + logoW + 10.0f;
        float textY = baseY + (logoH - Fonts.INTER_BOLD.get().getHeight(15)) / 2.0f - 2.0f;
        ctx.text(Fonts.INTER_BOLD.get(), "ZortexDLC", 15, textX, textY,
                cs.computeColor(palette.text().tone(600).argb(), Math.round(MathUtil.interpolate(90.0f, 160.0f, progress))));
        ctx.text(Fonts.INTER_MEDIUM.get(), "Minecraft 1.21.4", 10, textX, textY + 19.0f,
                cs.computeColor(palette.text().tone(500).argb(), Math.round(MathUtil.interpolate(70.0f, 130.0f, progress))));
    }

    private void drawLockView(DrawCtx ctx, ColorStack cs) {
        ThemePalette palette = ctx.theme().palette();
        float progress = MathUtil.clamp(unlockProgress(), 0.0f, 1.0f);

        float clockSize = MathUtil.interpolate(78.0f, 30.0f, progress);
        float timeY = MathUtil.interpolate(this.height * 0.42f, this.height * 0.06f, progress);
        int timeColor = cs.computeColor(0xFFF2F5FF, Math.round(MathUtil.interpolate(255.0f, 205.0f, progress)));
        drawTime(ctx, clockSize, this.width / 2.0f, timeY, timeColor);

        float dateY = timeY + ((1.0f - progress) * -74.0f) + (progress * 44.0f);
        int dateSize = Math.max(1, Math.round(MathUtil.interpolate(16.0f, 13.0f, progress)));
        String date = dateLabel();
        float dateW = ctx.textWidthPhysical(Fonts.INTER_MEDIUM.get(), date, dateSize);
        ctx.text(Fonts.INTER_MEDIUM.get(), date, dateSize, (this.width - dateW) / 2.0f, dateY,
                cs.computeColor(palette.text().tone(400).argb(), Math.round(MathUtil.interpolate(205.0f, 150.0f, progress))));

        drawSwipeHint(ctx, cs, palette, progress);
    }

    private void drawTime(DrawCtx ctx, float size, float centerX, float topY, int color) {
        MsdfFont font = Fonts.INTER_EXTRA_BOLD.get();
        String time = currentTime();
        int colonIdx = time.indexOf(':');
        if (colonIdx < 0) {
            float w = ctx.textWidthPhysical(font, time, Math.max(1, Math.round(size)));
            ctx.text(font, time, Math.max(1, Math.round(size)), centerX - w / 2.0f, topY, color);
            return;
        }
        String left = time.substring(0, colonIdx);
        String colon = ":";
        String right = time.substring(colonIdx + 1);
        float leftW = ctx.textWidthPhysical(font, left, Math.max(1, Math.round(size)));
        float colonW = ctx.textWidthPhysical(font, colon, Math.max(1, Math.round(size)));
        float rightW = ctx.textWidthPhysical(font, right, Math.max(1, Math.round(size)));
        float totalW = leftW + colonW + rightW;
        float startX = centerX - totalW / 2.0f;
        int fs = Math.max(1, Math.round(size));
        ctx.text(font, left, fs, startX, topY, color);
        ctx.text(font, colon, fs, startX + leftW, topY - size * 0.045f, color);
        ctx.text(font, right, fs, startX + leftW + colonW, topY, color);
    }

    private void drawSwipeHint(DrawCtx ctx, ColorStack cs, ThemePalette palette, float progress) {
        float hintAlpha = MathUtil.clamp((1.0f - progress * 2.0f) * 1.3f, 0.0f, 1.0f);
        if (hintAlpha <= 0.03f) {
            return;
        }
        float bounce = 3.0f * (0.5f + 0.5f * (float) Math.sin(this.backgroundTime * 2.4f));
        String label = "Проведите вверх";
        int fs = 14;
        float tw = ctx.textWidthPhysical(Fonts.INTER_MEDIUM.get(), label, fs);
        float pillW = tw + 60.0f;
        float pillH = 40.0f;
        float pillX = (this.width - pillW) / 2.0f;
        float pillY = this.height - 96.0f - bounce;
        int bg = cs.computeColor(palette.surfaceBackground().tone(900).argb(), Math.round(150.0f * hintAlpha));
        int outline = cs.computeColor(palette.surfaceOutline().tone(600).argb(), Math.round(120.0f * hintAlpha));
        ctx.fillOutlinedRoundedRect(pillX, pillY, pillW, pillH, pillH / 2.0f, 1.0f, outline, bg);
        ctx.text(Fonts.INTER_MEDIUM.get(), label, fs, (this.width - tw) / 2.0f,
                pillY + (pillH - Fonts.INTER_MEDIUM.get().getHeight(fs)) / 2.0f,
                cs.computeColor(palette.text().tone(400).argb(), Math.round(235.0f * hintAlpha)));

        float barW = 110.0f;
        float barY = this.height - 18.0f;
        ctx.fillRoundedRect((this.width - barW) / 2.0f, barY, barW, 5.0f, 2.5f,
                cs.computeColor(palette.surfaceBackground().tone(700).argb(), Math.round(110.0f * hintAlpha)));
        float dotW = 26.0f + 10.0f * (0.5f + 0.5f * (float) Math.sin(this.backgroundTime * 2.2f));
        ctx.fillRoundedRect(this.width / 2.0f - dotW / 2.0f, barY, dotW, 5.0f, 2.5f,
                cs.computeColor(palette.accent().argb(), Math.round(210.0f * hintAlpha)));
    }

    private void drawIconMenu(DrawCtx ctx, ColorStack cs, int mouseX, int mouseY) {
        ThemePalette palette = ctx.theme().palette();
        GlTexture[] textures = {this.iconSingle, this.iconMulti, this.iconSettings, this.iconAlt, this.iconPower};
        float progress = MathUtil.clamp(unlockProgress(), 0.0f, 1.0f);
        float s = iconSize();
        float dockY = iconDockY();
        boolean interactive = progress > 0.55f;
        for (int i = 0; i < textures.length; i++) {
            float stagger = MathUtil.clamp(progress * 1.3f - (i * 0.09f), 0.0f, 1.0f);
            float eased = Easings.EASE_OUT_CUBIC.ease(stagger);
            SpringAnimator hover = this.hoverAnimators[i];
            float xBase = iconX(i);
            float hoverValue = hover.value();
            float lift = hoverValue * 4.0f;
            float scale = (0.6f + 0.4f * eased) * (1.0f + hoverValue * 0.05f);
            float x = xBase + ((s - (s * scale)) / 2.0f);
            float y = dockY + ((1.0f - eased) * 64.0f) - lift;
            float ds = s * scale;
            float d = 16.0f * scale;
            boolean hovered = interactive && mouseX >= x && mouseX <= x + ds && mouseY >= y - 2.0f && mouseY <= y + ds + 2.0f;
            hover.target(hovered ? 1.0f : 0.0f);
            float alpha = MathUtil.clamp(eased * 1.6f, 0.0f, 1.0f);

            int bg = hovered ? cs.computeColor(palette.surfaceBackground().tone(800).argb(), Math.round(230.0f * alpha)) : cs.computeColor(palette.surfaceBackground().tone(700).argb(), Math.round(120.0f * alpha));
            ctx.fillOutlinedRoundedRect(x, y, ds, ds, 12.0f, 1.5f, hovered ? cs.computeColor(palette.accentBright().argb(), Math.round(210.0f * alpha)) : cs.computeColor(palette.surfaceOutline().tone(600).argb(), Math.round(255.0f * alpha)), bg);
            int iconColor = hovered ? cs.computeColor(0xFFFFFFFF, Math.round(255.0f * alpha)) : cs.computeColor(palette.text().tone(400).argb(), Math.round(255.0f * alpha));
            ctx.texture(textures[i], x + (ds - d) / 2.0f, y + (ds - d) / 2.0f, d, d, iconColor);
        }
    }

    private void drawBackgroundArrows(DrawCtx ctx, ColorStack cs, int mouseX, int mouseY) {
        ThemePalette palette = ctx.theme().palette();
        float progress = MathUtil.clamp(unlockProgress(), 0.0f, 1.0f);
        float a = 0.45f + 0.55f * progress;
        float size = 40.0f;
        float y = (this.height - size) / 2.0f;
        drawArrowButton(ctx, cs, palette, 16.0f, y, size, this.arrowLeft, overArrow(mouseX, mouseY, true), this.arrowHoverLeft, a);
        drawArrowButton(ctx, cs, palette, this.width - 16.0f - size, y, size, this.arrowRight, overArrow(mouseX, mouseY, false), this.arrowHoverRight, a);
    }

    private void drawArrowButton(DrawCtx ctx, ColorStack cs, ThemePalette palette, float x, float y, float size, GlTexture icon, boolean hovered, SpringAnimator animator, float a) {
        animator.target(hovered ? 1.0f : 0.0f);
        float hoverValue = animator.value();
        float s = size * (1.0f + hoverValue * 0.07f);
        float ox = x + (size - s) / 2.0f;
        float oy = y + (size - s) / 2.0f;
        int bg = hovered ? cs.computeColor(palette.surfaceBackground().tone(800).argb(), Math.round(230.0f * a)) : cs.computeColor(palette.surfaceBackground().tone(700).argb(), Math.round(75.0f * a));
        int outline = hovered ? cs.computeColor(palette.accentBright().argb(), Math.round(215.0f * a)) : cs.computeColor(palette.surfaceOutline().tone(600).argb(), Math.round(175.0f * a));
        ctx.fillOutlinedRoundedRect(ox, oy, s, s, 12.0f, 1.5f, outline, bg);
        float iconD = 24.0f;
        int iconColor = hovered ? cs.computeColor(0xFFFFFFFF, Math.round(255.0f * a)) : cs.computeColor(palette.text().tone(400).argb(), Math.round(255.0f * a));
        ctx.texture(icon, ox + (s - iconD) / 2.0f, oy + (s - iconD) / 2.0f, iconD, iconD, iconColor);
    }

    private boolean overArrow(double mouseX, double mouseY, boolean left) {
        float size = 40.0f;
        float cx = left ? 16.0f + size / 2.0f : this.width - 16.0f - size / 2.0f;
        float cy = this.height / 2.0f;
        float half = size / 2.0f + 12.0f;
        return mouseX >= cx - half && mouseX <= cx + half && mouseY >= cy - half && mouseY <= cy + half;
    }

    private void switchBackground(int direction) {
        if (direction == 0) {
            return;
        }
        if (this.backgroundTarget >= 0 && !this.bgTransition.isOne()) {
            this.backgroundIndex = this.backgroundTarget;
        }
        int next = (this.backgroundIndex + direction + this.wallpaperIds.length) % this.wallpaperIds.length;
        Expensive.LOGGER.info("[bg] switch dir={} index={} tex={}x{}", direction, Integer.valueOf(next), Integer.valueOf(this.wallpaperSize[next][0]), Integer.valueOf(this.wallpaperSize[next][1]));
        this.backgroundTarget = next;
        this.bgDirection = direction;
        this.bgTransition.set(0.0f);
        this.bgTransition.destination(1.0f);
        this.backgroundSwitchTime = this.backgroundTime;
        Expensive.INSTANCE.configManager().menuStateConfig().setMainMenuWallpaper(next);
        try {
            Expensive.INSTANCE.configManager().saveMenuState();
        } catch (IOException e) {
            Expensive.LOGGER.warn("Failed to save wallpaper state", e);
        }
    }

    private void drawUserName(DrawCtx ctx, ColorStack cs) {
        float user = MathUtil.clamp(unlockProgress() * 1.6f - 0.4f, 0.0f, 1.0f);
        if (user <= 0.02f) {
            return;
        }
        ThemePalette palette = ctx.theme().palette();
        String nick = "—";
        try {
            if (Mc.INSTANCE.getSession() != null) {
                nick = Mc.INSTANCE.getSession().getUsername();
            }
        } catch (Exception ignored) {
        }
        int fs = 13;
        float tw = ctx.textWidthPhysical(Fonts.INTER_MEDIUM.get(), nick, fs);
        ctx.text(Fonts.INTER_MEDIUM.get(), nick, fs, (this.width - tw) / 2.0f, iconDockY() + iconSize() + 28.0f,
                cs.computeColor(palette.text().tone(500).argb(), Math.round(230.0f * user)));
    }

    private float iconSize() {
        return 30.0f;
    }

    private float iconGap() {
        return 20.0f;
    }

    private float iconStartX() {
        float total = (5 * iconSize()) + (4 * iconGap());
        return (this.width - total) / 2.0f;
    }

    private float iconX(int index) {
        return iconStartX() + index * (iconSize() + iconGap());
    }

    private float iconDockY() {
        return MathUtil.interpolate(this.height + 160.0f, (this.height / 2.0f) - (iconSize() / 2.0f), MathUtil.clamp(unlockProgress(), 0.0f, 1.0f));
    }

    private String currentTime() {
        return LocalTime.now().format(TIME_FORMAT);
    }

    private String dateLabel() {
        LocalDate currentDate = LocalDate.now();
        String[] days = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"};
        String[] months = {"января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря"};
        return days[currentDate.getDayOfWeek().getValue() - 1] + ", " + currentDate.getDayOfMonth() + " " + months[currentDate.getMonthValue() - 1];
    }

    private void drawAltPanel(DrawCtx ctx, ColorStack cs, int mouseX, int mouseY) {
        ThemePalette palette = ctx.theme().palette();
        float panelW = this.width * 0.34f;
        float padX = 18.0f;
        float panelX = this.width - panelW - this.width * 0.03f;
        float top = this.height * 0.03f;
        float panelH = this.height - top * 2.0f;
        float panelY = top;
        float fieldW = panelW - padX * 2.0f;
        float half = (fieldW - 8.0f) / 2.0f;

        ctx.fillOutlinedRoundedRect(panelX, panelY, panelW, panelH, 12.0f, 2.0f, cs.computeColor(palette.surfaceOutline().tone(600).argb()), cs.computeColor(palette.surfaceBackground().tone(900).argb(), 0.94f));
        ctx.fillRect(panelX, panelY, panelW, 2.0f, cs.computeColor(palette.accent().argb(), 200));

        float y = panelY + 16.0f;
        ctx.text(Fonts.INTER_EXTRA_BOLD.get(), "Alt Manager", 20, panelX + padX, y, cs.computeColor(palette.text().tone(700).argb()));
        boolean overClose = mouseX >= panelX + panelW - 34.0f && mouseX <= panelX + panelW - 8.0f && mouseY >= panelY + 8.0f && mouseY <= panelY + 34.0f;
        ctx.fillRoundedRect(panelX + panelW - 34.0f, panelY + 8.0f, 26.0f, 26.0f, 8.0f, cs.computeColor(overClose ? palette.surfaceBackground().tone(800).argb() : palette.surfaceBackground().tone(700).argb()));
        ctx.textureVerticalCHorizontalC(this.iconQuit, panelX + panelW - 21.0f, panelY + 21.0f, 14, 14, cs.computeColor(palette.text().tone(500).argb()));
        y += 36.0f;

        String nick = "—";
        try {
            if (Mc.INSTANCE.getSession() != null) {
                nick = Mc.INSTANCE.getSession().getUsername();
            }
        } catch (Exception ignored) {
        }
        ctx.text(Fonts.INTER_MEDIUM.get(), "Текущий ник: " + nick, 13, panelX + padX, y, cs.computeColor(palette.text().tone(400).argb()));
        y += 28.0f;

        drawTypeToggle(ctx, cs, panelX, panelY, panelW, y, mouseX, mouseY);
        y += 38.0f;

        ctx.fillRoundedRect(panelX + padX, y, fieldW, 32.0f, 9.0f, cs.computeColor(palette.surfaceBackground().tone(700).argb()));
        ctx.text(Fonts.INTER_MEDIUM.get(), this.inputText.isEmpty() ? "Введите ник..." : this.inputText, 13, panelX + padX + 12.0f, y + 8.0f, cs.computeColor(this.inputText.isEmpty() ? palette.text().tone(600).argb() : palette.text().tone(700).argb()));
        y += 40.0f;

        ctx.text(Fonts.INTER_MEDIUM.get(), "Сохранённые аккаунты (" + this.rows.size() + ")", 12, panelX + padX, y, cs.computeColor(palette.text().tone(500).argb()));
        y += 22.0f;

        float listTop = y;
        float listBottom = panelY + panelH - 58.0f;
        float rowH = 36.0f;
        float rowGap = 6.0f;
        this.listScroll = Math.max(0.0f, Math.min(this.listScroll, listMaxScroll()));

        int hover = -1;
        for (int i = 0; i < this.rows.size(); i++) {
            AltAccount acc = this.rows.get(i);
            float ry = listTop + i * (rowH + rowGap) - this.listScroll;
            if (ry + rowH < listTop || ry > listBottom) {
                continue;
            }
            boolean over = mouseX >= panelX + padX && mouseX <= panelX + panelW - padX && mouseY >= ry && mouseY <= ry + rowH;
            if (over) {
                hover = i;
            }
            int rowBg = over ? cs.computeColor(palette.surfaceBackground().tone(800).argb(), 170) : cs.computeColor(palette.surfaceBackground().tone(700).argb(), 70);
            ctx.fillRoundedRect(panelX + padX, ry, fieldW, rowH, 8.0f, rowBg);
            ctx.fillRoundedRect(panelX + padX, ry, 3.0f, rowH, 1.5f, cs.computeColor(TYPE_MICROSOFT.equals(acc.type) ? ThemePalette.blue.argb() : ThemePalette.deepGreen.argb()));
            ctx.text(Fonts.INTER_MEDIUM.get(), acc.name, 13, panelX + padX + 14.0f, ry + (rowH - Fonts.INTER_MEDIUM.get().getHeight(13)) / 2.0f, cs.computeColor(palette.text().tone(700).argb()));
            ctx.text(Fonts.INTER_MEDIUM.get(), acc.type, 11, panelX + panelW - padX - 70.0f, ry + (rowH - Fonts.INTER_MEDIUM.get().getHeight(11)) / 2.0f, cs.computeColor(TYPE_MICROSOFT.equals(acc.type) ? ThemePalette.blue.argb() : ThemePalette.deepGreen.argb()));
        }
        this.hoveredRow = hover;

        float by = listBottom + 6.0f;
        boolean overEnter = mouseX >= panelX + padX && mouseX <= panelX + padX + half && mouseY >= by && mouseY <= by + 34.0f;
        boolean overAdd = mouseX >= panelX + padX + half + 8.0f && mouseX <= panelX + panelW - padX && mouseY >= by && mouseY <= by + 34.0f;
        drawCtaButton(ctx, cs, panelX + padX, by, half, 34.0f, "Войти", TYPE_CRACK.equals(currentType()) ? ThemePalette.deepGreen.argb() : ThemePalette.blue.argb(), overEnter);
        drawCtaButton(ctx, cs, panelX + padX + half + 8.0f, by, half, 34.0f, "Добавить", palette.accentBright().argb(), overAdd);
    }

    private void drawTypeToggle(DrawCtx ctx, ColorStack cs, float panelX, float panelY, float panelW, float y, int mouseX, int mouseY) {
        ThemePalette palette = ctx.theme().palette();
        float padX = 18.0f;
        float fieldW = panelW - padX * 2.0f;
        float half = (fieldW - 8.0f) / 2.0f;
        boolean crack = TYPE_CRACK.equals(currentType());
        boolean overCrack = mouseX >= panelX + padX && mouseX <= panelX + padX + half && mouseY >= y && mouseY <= y + 32.0f;
        boolean overMs = mouseX >= panelX + padX + half + 8.0f && mouseX <= panelX + panelW - padX && mouseY >= y && mouseY <= y + 32.0f;

        int crackBg = crack ? cs.computeColor(ThemePalette.deepGreen.argb(), 210) : cs.computeColor(palette.surfaceBackground().tone(700).argb(), 110);
        int msBg = !crack ? cs.computeColor(ThemePalette.blue.argb(), 210) : cs.computeColor(palette.surfaceBackground().tone(700).argb(), 110);
        ctx.fillRoundedRect(panelX + padX, y, half, 32.0f, 8.0f, overCrack ? cs.computeColor(ThemePalette.deepGreen.argb(), 255) : crackBg);
        ctx.fillRoundedRect(panelX + padX + half + 8.0f, y, half, 32.0f, 8.0f, overMs ? cs.computeColor(ThemePalette.blue.argb(), 255) : msBg);

        int crackText = crack || overCrack ? 0xFFFFFFFF : cs.computeColor(palette.text().tone(400).argb());
        int msText = !crack || overMs ? 0xFFFFFFFF : cs.computeColor(palette.text().tone(400).argb());
        ctx.text(Fonts.INTER_BOLD.get(), "Crack", 14, panelX + padX + (half - ctx.textWidthPhysical(Fonts.INTER_BOLD.get(), "Crack", 14)) / 2.0f, y + (32.0f - Fonts.INTER_BOLD.get().getHeight(14)) / 2.0f, crackText);
        ctx.text(Fonts.INTER_BOLD.get(), "Microsoft", 14, panelX + padX + half + 8.0f + (half - ctx.textWidthPhysical(Fonts.INTER_BOLD.get(), "Microsoft", 14)) / 2.0f, y + (32.0f - Fonts.INTER_BOLD.get().getHeight(14)) / 2.0f, msText);
    }

    private void drawCtaButton(DrawCtx ctx, ColorStack cs, float x, float y, float w, float h, String label, int color, boolean hover) {
        int bg = cs.computeColor(color, hover ? 220 : 150);
        ctx.fillRoundedRect(x, y, w, h, 9.0f, bg);
        ctx.text(Fonts.INTER_BOLD.get(), label, 14, x + (w - ctx.textWidthPhysical(Fonts.INTER_BOLD.get(), label, 14)) / 2.0f, y + (h - Fonts.INTER_BOLD.get().getHeight(14)) / 2.0f, 0xFFFFFFFF);
    }

    private String currentType() {
        if (this.alts == null) {
            return TYPE_CRACK;
        }
        if (TYPE_MICROSOFT.equals(this.alts.lastType())) {
            return TYPE_MICROSOFT;
        }
        return TYPE_CRACK;
    }

    private float panelRectX() {
        float panelW = this.width * 0.34f;
        return this.width - panelW - this.width * 0.03f;
    }

    private float panelRectY() {
        return this.height * 0.03f;
    }

    private float panelRectW() {
        return this.width * 0.34f;
    }

    private float panelRectH() {
        return this.height - panelRectY() * 2.0f;
    }

    private float panelFieldW() {
        return panelRectW() - 36.0f;
    }

    private float panelHalf() {
        return (panelFieldW() - 8.0f) / 2.0f;
    }

    private float panelTypeY() {
        return panelRectY() + 16.0f + 36.0f + 28.0f;
    }

    private float panelListTop() {
        return panelTypeY() + 38.0f + 40.0f + 22.0f;
    }

    private float panelListBottom() {
        return panelRectY() + panelRectH() - 58.0f;
    }

    private float panelAdderY() {
        return panelListBottom() + 6.0f;
    }

    private float listMaxScroll() {
        float rowH = 36.0f;
        float rowGap = 6.0f;
        return Math.max(0.0f, (this.rows.size() * (rowH + rowGap)) - (panelListBottom() - panelListTop()));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Expensive.LOGGER.info("[bg] click x={} y={} w={} h={} overL={} overR={}", mouseX, mouseY, this.width, this.height, overArrow(mouseX, mouseY, true), overArrow(mouseX, mouseY, false));
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (overArrow(mouseX, mouseY, true)) {
            switchBackground(-1);
            return true;
        }
        if (overArrow(mouseX, mouseY, false)) {
            switchBackground(1);
            return true;
        }

        if (this.unlockAnimation.animatedValue() > 0.95f) {
            float s = iconSize();
            float y = iconDockY();
            for (int i = 0; i < iconTooltips.length; i++) {
                float x = iconX(i);
                if (mouseX >= x && mouseX <= x + s && mouseY >= y - 6.0f && mouseY <= y + s + 6.0f) {
                    handleIcon(i);
                    return true;
                }
            }

            if (this.altPanelOpen && handleAltPanelClick(mouseX, mouseY)) {
                return true;
            }

            lock();
            return true;
        }

        this.dragging = true;
        this.dragged = false;
        this.dragStartY = mouseY;
        this.pull = 0.0f;
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.dragging) {
            if (Math.abs(mouseY - this.dragStartY) > 6.0d) {
                this.dragged = true;
            }
            this.pull = MathUtil.clamp((float) ((this.dragStartY - mouseY) / 280.0f), 0.0f, 1.0f);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.dragging) {
            this.dragging = false;
            if (!this.dragged || this.pull >= 0.25f) {
                unlock();
            } else {
                this.unlockAnimation.set(this.pull);
                this.pull = 0.0f;
                this.unlockAnimation.destination(0.0f);
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void unlock() {
        this.unlocked = true;
        this.dragging = false;
        if (this.pull > 0.0f) {
            this.unlockAnimation.set(this.pull);
        }
        this.pull = 0.0f;
        this.unlockAnimation.destination(1.0f);
    }

    private void lock() {
        this.unlocked = false;
        this.pull = 0.0f;
        this.unlockAnimation.destination(0.0f);
    }

    private void handleIcon(int index) {
        if (this.client == null) {
            return;
        }
        switch (index) {
            case 0:
                this.client.setScreen(new SelectWorldScreen(this));
                break;
            case 1:
                this.client.setScreen(new MultiplayerScreen(this));
                break;
            case 2:
                this.client.setScreen(new OptionsScreen(this, this.client.options));
                break;
            case 3:
                this.altPanelOpen = !this.altPanelOpen;
                this.inputFocused = false;
                break;
            case 4:
                this.client.scheduleStop();
                break;
            default:
                break;
        }
    }

    private boolean handleAltPanelClick(double mouseX, double mouseY) {
        float panelX = panelRectX();
        float panelY = panelRectY();
        float fieldW = panelFieldW();
        float half = panelHalf();

        float closeX = panelX + panelRectW() - 34.0f;
        float closeY = panelY + 8.0f;
        if (mouseX >= closeX && mouseX <= closeX + 26.0f && mouseY >= closeY && mouseY <= closeY + 26.0f) {
            this.altPanelOpen = false;
            return true;
        }

        float typeY = panelTypeY();
        boolean overCrack = mouseX >= panelX + 18.0f && mouseX <= panelX + 18.0f + half && mouseY >= typeY && mouseY <= typeY + 32.0f;
        boolean overMs = mouseX >= panelX + 18.0f + half + 8.0f && mouseX <= panelX + panelRectW() - 18.0f && mouseY >= typeY && mouseY <= typeY + 32.0f;
        if (overCrack) {
            this.alts.lastType(TYPE_CRACK);
            return true;
        }
        if (overMs) {
            this.alts.lastType(TYPE_MICROSOFT);
            return true;
        }

        float fieldY = typeY + 38.0f;
        boolean overField = mouseX >= panelX + 18.0f && mouseX <= panelX + 18.0f + fieldW && mouseY >= fieldY && mouseY <= fieldY + 32.0f;
        this.inputFocused = overField;

        float addY = panelAdderY();
        boolean overEnter = mouseX >= panelX + 18.0f && mouseX <= panelX + 18.0f + half && mouseY >= addY && mouseY <= addY + 34.0f;
        boolean overAdd = mouseX >= panelX + 18.0f + half + 8.0f && mouseX <= panelX + panelRectW() - 18.0f && mouseY >= addY && mouseY <= addY + 34.0f;
        if (overAdd) {
            addFromInput();
            return true;
        }
        if (overEnter) {
            doLogin();
            return true;
        }

        if (!overField) {
            float rowH = 36.0f;
            float rowGap = 6.0f;
            float listTop = panelListTop();
            float listBottom = panelListBottom();
            for (int i = 0; i < this.rows.size(); i++) {
                AltAccount acc = this.rows.get(i);
                float ry = listTop + i * (rowH + rowGap) - this.listScroll;
                if (ry + rowH < listTop || ry > listBottom) {
                    continue;
                }
                if (mouseX >= panelX + 18.0f && mouseX <= panelX + panelRectW() - 18.0f && mouseY >= ry && mouseY <= ry + rowH) {
                    this.alts.loginAccount(acc);
                    return true;
                }
            }
        }
        return false;
    }

    private void addFromInput() {
        String name = normalizeName(this.inputText);
        if (name.isEmpty()) {
            return;
        }
        this.alts.addAlt(name, currentType());
        this.inputText = "";
        refreshRows();
    }

    private void doLogin() {
        String name = normalizeName(this.inputText);
        if (!name.isEmpty()) {
            this.alts.addAlt(name, currentType());
            refreshRows();
        }
        String target = !name.isEmpty() ? name : (this.hoveredRow >= 0 && this.hoveredRow < this.rows.size() ? this.rows.get(this.hoveredRow).name : "");
        if (!target.isEmpty()) {
            this.alts.login(target, currentType());
        }
    }

    private String normalizeName(String s) {
        if (s == null) {
            return "";
        }
        String n = s.trim().replaceAll("[^A-Za-z0-9_]", "");
        return n.length() > 16 ? n.substring(0, 16) : n;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.altPanelOpen) {
            this.listScroll -= (float) verticalAmount * 20.0f;
            this.listScroll = Math.max(0.0f, Math.min(this.listScroll, listMaxScroll()));
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.inputFocused && (keyCode == 263 || keyCode == 65)) {
            switchBackground(-1);
            return true;
        }
        if (!this.inputFocused && (keyCode == 262 || keyCode == 68)) {
            switchBackground(1);
            return true;
        }
        if (this.inputFocused) {
            if (keyCode == 259) {
                if (!this.inputText.isEmpty()) {
                    this.inputText = this.inputText.substring(0, this.inputText.length() - 1);
                }
                return true;
            }
            if (keyCode == 257) {
                addFromInput();
                return true;
            }
            if (keyCode == 256) {
                this.inputFocused = false;
                this.altPanelOpen = false;
                return true;
            }
        } else if (keyCode == 256 && this.altPanelOpen) {
            this.altPanelOpen = false;
            return true;
        } else if (keyCode == 256 && !this.unlocked) {
            unlock();
            return true;
        } else if (keyCode == 256 && this.unlocked) {
            lock();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.inputFocused && (Character.isLetterOrDigit(chr) || chr == '_') && this.inputText.length() < 16) {
            this.inputText += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void tick() {
    }
}