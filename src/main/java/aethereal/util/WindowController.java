package aethereal.util;
import aethereal.ui.AbstractWindow;
import aethereal.render.AnimatedFloat;
import aethereal.render.AnimationStack2;
import aethereal.render.BloomEffect;
import aethereal.render.BlurEffect;
import aethereal.math.DeltaTimeTracker;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.event.InputEvent;
import aethereal.model.InputEventContext;
import aethereal.net.InputInterceptor;
import aethereal.type.InputType;
import aethereal.model.LayoutScaleContext;
import aethereal.type.Mc;
import aethereal.ui.MenuWindow;
import aethereal.model.PixelPoint;
import aethereal.render.ScaledRenderTarget;
import aethereal.render.ScreenResolution;
import aethereal.render.Theme;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

public class WindowController {
    public int windowVersion;
    public int backgroundAlphaValue;
    public int savedCursorMode;

    public ScreenResolution resolution;
    public static final long frameIntervalNanos = 16666667;
    public boolean dpiInitialized;
    public float dpiScaleFactor = 1.0f;
    public boolean autoDpiScale = true;
    public final List<AbstractWindow> windows = new ArrayList();
    public final InputInterceptor window = new InputInterceptor();
    public final DeltaTimeTracker deltaTimeTracker = new DeltaTimeTracker();
    public final AnimationStack2 animationStack = new AnimationStack2();
    public final BlurEffect headerBlur = new BlurEffect();
    public final BlurEffect searchBlur = new BlurEffect();

    public final BloomEffect bloom = new BloomEffect();

    public final ScaledRenderTarget bloomBuffer = new ScaledRenderTarget(2);
    public int warmupFrames = 2;

    public PixelPoint mousePosition = new PixelPoint(0, 0);
    public long lastFrameTime = 0;
    public final AnimatedFloat dpiScaleAnimation = new AnimatedFloat(180, Easings.EASE_IN_OUT_CUBIC);

    public void init() {
        this.headerBlur.init();
        this.searchBlur.init();
        this.bloom.init();
    }

    public void draw(long j) {
        MenuWindow class776VarMenuWindow;
        if (this.warmupFrames > 0) {
            this.warmupFrames--;
            this.deltaTimeTracker.clear();
        }
        if (this.resolution == null) {
            this.resolution = ScreenResolution.resolution();
            if (this.autoDpiScale) {
                updateAutoDpiScale();
            }
        }
        long jNanoTime = System.nanoTime();
        boolean z = jNanoTime - this.lastFrameTime >= frameIntervalNanos;
        if (z) {
            this.lastFrameTime = jNanoTime;
        }
        WeightedEngine class141Var = new WeightedEngine(this.deltaTimeTracker.elapsedUnit(), this.animationStack);
        this.animationStack.begin();
        if (!this.dpiInitialized) {
            this.dpiScaleAnimation.set(this.dpiScaleFactor);
            this.dpiScaleAnimation.destination(this.dpiScaleFactor);
            this.dpiInitialized = true;
        }
        this.dpiScaleAnimation.animate(class141Var);
        float f = this.dpiScaleFactor;
        this.dpiScaleFactor = this.dpiScaleAnimation.animatedValue();
        if (Math.abs(f - this.dpiScaleFactor) > 1.0E-4f && (class776VarMenuWindow = Expensive.INSTANCE.menuWindow()) != null) {
            class776VarMenuWindow.requestCentering();
        }
        Iterator<AbstractWindow> it = this.windows.iterator();
        while (it.hasNext()) {
            it.next().animation(class141Var);
        }
        this.animationStack.end();
        this.window.begin(j);
        MatrixStack matrixStack = new MatrixStack();
        DrawEngine class154VarDrawEngine = Expensive.INSTANCE.drawEngine();
        Theme class760VarTheme = Expensive.INSTANCE.theme();
        RenderCommandQueue class676Var = new RenderCommandQueue();
        OverlayCommandQueue class677Var = new OverlayCommandQueue();
        LayoutScaleContext class698Var = new LayoutScaleContext(this.resolution, this.dpiScaleFactor);
        DrawCtx class699Var = new DrawCtx(this.window, this.resolution, matrixStack, this.window.determineMousePosition(), class154VarDrawEngine, class154VarDrawEngine.colorStack(), this.mousePosition, class760VarTheme, class698Var);
        boolean z2 = false;
        for (AbstractWindow class684Var : this.windows) {
            if (class684Var.visible()) {
                class684Var.layout(class698Var);
                class684Var.collectBlurElements(class677Var);
                class684Var.collectBloomElements(class676Var);
                z2 = true;
            }
        }
        boolean z3 = !class677Var.isEmpty();
        boolean z4 = !class676Var.isEmpty();
        boolean z5 = z3 && z2;
        if (z3 && z2) {
            class154VarDrawEngine.begin();
            class677Var.renderRecorded(class699Var);
            class154VarDrawEngine.end();
        }
        if (z4 && z2) {
            FramebufferUtil.clearTransparent(this.bloomBuffer.getFramebuffer());
            this.bloomBuffer.add(() -> {
                class154VarDrawEngine.begin();
                class676Var.renderRecorded(class699Var);
                class154VarDrawEngine.end();
            });
            this.bloomBuffer.renderToFramebuffer();
        }
        if (z5 && z) {
            this.headerBlur.apply(32);
            this.searchBlur.apply(16, 2.0f);
            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
        }
        if (z4 && z2) {
            this.bloom.apply(this.bloomBuffer.getFramebuffer(), 16);
        }
        class154VarDrawEngine.begin();
        if (this.backgroundAlphaValue != 0) {
            class154VarDrawEngine.rectangle(matrixStack.peek().getPositionMatrix(), 0.0f, 0.0f, this.resolution.screenWidth(), this.resolution.screenHeight(), class154VarDrawEngine.colorStack().darkAlpha(this.backgroundAlphaValue));
        }
        for (AbstractWindow class684Var2 : this.windows) {
            if (class684Var2.visible()) {
                class684Var2.render(class699Var);
            }
        }
        class154VarDrawEngine.end();
        this.window.tickRefreshKeysIfNeeded();
    }

    public boolean handleInput(InputEventContext class688Var) {
        InputEvent class691VarInputEvent = class688Var.inputEvent();
        this.mousePosition = class688Var.logicalMousePosition();
        boolean z = false;
        boolean z2 = class691VarInputEvent.type() == InputType.BUTTON;
        boolean z3 = class691VarInputEvent.type() == InputType.CURSOR;
        int size = this.windows.size();
        int i = this.windowVersion;
        if (!this.windows.isEmpty()) {
            for (int size2 = this.windows.size() - 1; size2 >= 0; size2--) {
                AbstractWindow class684Var = this.windows.get(size2);
                if (class684Var.handleInput(class688Var, false)) {
                    z = true;
                    if (!z2 || this.windows.size() != size || this.windowVersion != i || size2 == this.windows.size() - 1) {
                        break;
                    }
                    this.windows.remove(size2);
                    this.windows.add(class684Var);
                    this.windowVersion++;
                    break;
                }
            }
        }
        return z;
    }

    public boolean interceptKeyboardIfScreenPresent() {
        return Mc.INSTANCE.getCurrentScreen() != null && this.window.interceptKeyboardIfScreenPresent();
    }

    public boolean interceptKeyboard() {
        return window().interceptKeyboard();
    }

    public boolean interceptCursorIfScreenNotPresent() {
        return Mc.INSTANCE.getCurrentScreen() == null && this.window.interceptCursorfScreenNotPresent();
    }

    public void handleResize(int i, int i2) {
        this.resolution = new ScreenResolution(i, i2);
        if (this.autoDpiScale) {
            updateAutoDpiScale();
        }
    }

    public void newWindow(AbstractWindow class684Var) {
        if (this.windows.contains(class684Var)) {
            return;
        }
        this.windows.add(class684Var);
        this.windowVersion++;
    }

    public void setManualDpiScaleFactor(float f) {
        this.autoDpiScale = false;
        float fMethod004 = sanitizeDpiScale(f);
        if (!this.dpiInitialized) {
            this.dpiScaleFactor = fMethod004;
            this.dpiScaleAnimation.set(fMethod004);
            this.dpiInitialized = true;
        }
        this.dpiScaleAnimation.destination(fMethod004);
        MenuWindow class776VarMenuWindow = Expensive.INSTANCE.menuWindow();
        if (class776VarMenuWindow != null) {
            class776VarMenuWindow.requestCentering();
        }
    }

    public void enableAutoDpiScale() {
        this.autoDpiScale = true;
        updateAutoDpiScale();
        MenuWindow class776VarMenuWindow = Expensive.INSTANCE.menuWindow();
        if (class776VarMenuWindow != null) {
            class776VarMenuWindow.requestCentering();
        }
    }

    public void updateAutoDpiScale() {
        ScreenResolution class710VarResolution;
        if (this.resolution != null) {
            class710VarResolution = this.resolution;
        } else if (Mc.INSTANCE.getWindow() == null) {
            return;
        } else {
            class710VarResolution = ScreenResolution.resolution();
        }
        float fMethod003 = computeDpiScale(class710VarResolution);
        if (!this.dpiInitialized) {
            this.dpiScaleFactor = fMethod003;
            this.dpiScaleAnimation.set(fMethod003);
            this.dpiInitialized = true;
        }
        this.dpiScaleAnimation.destination(fMethod003);
    }

    public float computeDpiScale(ScreenResolution class710Var) {
        int iScreenWidth = class710Var.screenWidth();
        int iScreenHeight = class710Var.screenHeight();
        if (iScreenWidth >= 5120 || iScreenHeight >= 2880) {
            return 3.0f;
        }
        if (iScreenWidth >= 3840 || iScreenHeight >= 2160) {
            return 2.0f;
        }
        if (iScreenWidth >= 2560 || iScreenHeight >= 1440) {
            return 1.5f;
        }
        if (iScreenWidth >= 1920 || iScreenHeight >= 1080) {
            return 1.0f;
        }
        return (iScreenWidth >= 1280 || iScreenHeight >= 720) ? 0.75f : 1.0f;
    }

    public float sanitizeDpiScale(float f) {
        if (f <= 0.0f) {
            return 1.0f;
        }
        return f;
    }

    public void removeWindow(AbstractWindow class684Var) {
        if (this.windows.remove(class684Var)) {
            this.windowVersion++;
        }
    }

    public void bringToFront(AbstractWindow class684Var) {
        if (!this.windows.remove(class684Var)) {
            newWindow(class684Var);
        } else {
            this.windows.add(class684Var);
            this.windowVersion++;
        }
    }

    public <T extends AbstractWindow> T getWindow(Class<T> cls) {
        for (AbstractWindow class684Var : this.windows) {
            if (cls.isInstance(class684Var)) {
                return cls.cast(class684Var);
            }
        }
        return null;
    }

    public void showCursor(InputInterceptor class631Var) {
        Mc.INSTANCE.getMinecraft().onCursorEnterChanged();
        this.savedCursorMode = class631Var.cursorInputMode();
        class631Var.showCursor();
    }

    public void revertCursor(InputInterceptor class631Var) {
        if (Mc.INSTANCE.getCurrentScreen() == null) {
            class631Var.cursorInputMode(this.savedCursorMode);
        }
        Mc.INSTANCE.getMinecraft().onCursorEnterChanged();
    }

    public void recenterMouse() {
        if (this.resolution == null || Mc.INSTANCE.getCurrentScreen() != null) {
            return;
        }
        this.window.mousePosition(new PixelPoint(this.resolution.screenWidth() / 2, this.resolution.screenHeight() / 2));
    }

    public float dpiScaleFactor() {
        return this.dpiScaleFactor;
    }

    public boolean autoDpiScale() {
        return this.autoDpiScale;
    }

    public InputInterceptor window() {
        return this.window;
    }

    public BlurEffect headerBlur() {
        return this.headerBlur;
    }

    public BlurEffect searchBlur() {
        return this.searchBlur;
    }

    public BloomEffect bloom() {
        return this.bloom;
    }

    public ScaledRenderTarget bloomBuffer() {
        return this.bloomBuffer;
    }

    public WindowController backgroundAlpha(int i) {
        this.backgroundAlphaValue = i;
        return this;
    }

    public ScreenResolution resolution() {
        return this.resolution;
    }
}
