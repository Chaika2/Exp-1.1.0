package aethereal.ui;

import aethereal.Expensive;
import aethereal.module.SwapRingModule;
import aethereal.render.ColorStack;
import aethereal.render.Fonts;
import aethereal.render.MsdfFont;
import aethereal.render.ThemePalette;
import aethereal.util.DrawEngine;
import aethereal.util.MathUtil;
import aethereal.util.MovementInputHelper;
import aethereal.type.Mc;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

/**
 * Экран колеса свапа (Swap Wheel): удерживай клавишу модуля, веди мышь на сектор,
 * отпускай - свап уходит в оффхенд (см. SwapRingModule.confirmSector).
 * ЛКМ свапает, ПКМ и Esc отменяют. Предметы секторов задаются в меню пресета.
 * Колесо можно двигать по экрану: зажми центр (ЛКМ) или среднюю кнопку мыши и веди -
 * отпустишь, и позиция запомнится (переживёт перезапуск и смену сервера).
 */
public class SwapRingScreen extends Screen {
    /** Радиусы кольца в scaled-пикселях GUI. */
    static final float INNER_RADIUS = 40.0f;
    static final float OUTER_RADIUS = 84.0f;
    /** Пустая зона в центре: курсор сюда - свап отменяется. */
    static final float CENTER_RADIUS = 32.0f;
    /** Зазор между секторами в градусах. */
    static final float SECTOR_GAP = 4.0f;

    /** Диапазон пользовательского размера колеса (плашка "Размер" в чате). */
    public static final float RING_SIZE_MIN = 0.5f;
    public static final float RING_SIZE_MAX = 2.0f;
    /** Габариты плашки настройки размера, scaled GUI px. */
    public static final float SIZE_PANEL_W = 170.0f;
    public static final float SIZE_PANEL_H = 46.0f;
    /** Шаг слайдера размера. */
    public static final float RING_SIZE_STEP = 0.05f;

    private final SwapRingModule module;
    private int hovered = -1;
    /** Текущий центр колеса на экране (scaled GUI px). */
    private float centerX;
    private float centerY;
    /** Перетаскивание колеса: зажали центр (ЛКМ) или средняя кнопка мыши - ведём мышь, колесо едет. */
    private boolean dragging;
    private boolean dragMoved;
    private float dragOffsetX;
    private float dragOffsetY;

    public SwapRingScreen(SwapRingModule module) {
        super(Text.literal("Swap Wheel"));
        this.module = module;
    }

    @Override
    public void init() {
        // клавиши, зажатые до открытия колеса, не должны продолжать двигать игрока
        if (this.client != null) {
            for (KeyBinding keyBinding : MovementInputHelper.getMovementKeys(true, true)) {
                keyBinding.setPressed(false);
            }
        }
        applyInitialPosition();
    }

    /** Ставит колесо на сохранённую позицию, а если её нет - в центр экрана. */
    private void applyInitialPosition() {
        if (this.module.hasCustomPosition()) {
            this.centerX = this.module.ringX();
            this.centerY = this.module.ringY();
        } else {
            this.centerX = this.width / 2.0f;
            this.centerY = this.height / 2.0f;
        }
        clampCenter();
    }

    /** Не даёт утащить колесо за край экрана. */
    private void clampCenter() {
        float margin = margin(this.module.ringSize());
        this.centerX = MathUtil.clamp(this.centerX, margin, Math.max(margin, this.width - margin));
        this.centerY = MathUtil.clamp(this.centerY, margin, Math.max(margin, this.height - margin));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client == null || this.client.player == null || !this.module.state) {
            this.close();
            return;
        }
        updateHover(mouseX, mouseY);
        drawWheel(context, this.module, this.centerX, this.centerY, this.hovered, this.dragging, true);
    }

    /** Отступ от края экрана (scaled GUI px), чтобы колесо заданного размера не уезжало за границу. */
    public static float margin(float ringSize) {
        return outerRadius(ringSize) + 4.0f;
    }

    /** Внешний радиус колеса с учётом пользовательского размера (scaled GUI px) - по нему считается попадание клика. */
    public static float outerRadius(float ringSize) {
        return OUTER_RADIUS * ringSize;
    }

    /** Пустая зона в центре колеса с учётом пользовательского размера. */
    public static float centerRadius(float ringSize) {
        return CENTER_RADIUS * ringSize;
    }

    /**
     * Коэффициент перевода scaled-пикселей GUI в пиксели фреймбуфера.
     * <p>
     * DrawEngine рисует в пикселях фреймбуфера: его ортопроекция и ScreenResolution
     * заданы через {@code Window.getFramebufferWidth/Height}, а матрица, которую мы
     * ему отдаём, единичная. Координаты же {@code Screen} (this.width/height) и мыши
     * приходят в scaled-пикселях GUI. При GUI scale 2 или 3 без этого множителя всё,
     * что нарисовано DrawEngine'ом, получалось в 2-3 раза меньше и со смещением
     * относительно настоящего курсора. Множитель применяется к матрице рисования,
     * поэтому логика попадания кликов остаётся в scaled-пикселях GUI и не меняется.
     */
    public static float guiScale() {
        Window window = Mc.INSTANCE.getWindow();
        int scaledWidth = window.getScaledWidth();
        if (scaledWidth <= 0) {
            return 1.0f;
        }
        float scale = (float) window.getFramebufferWidth() / (float) scaledWidth;
        return scale > 0.0f ? scale : 1.0f;
    }

    /**
     * Рисует колесо свапа. Вызывается и экраном (интерактивный режим, с подсказками),
     * и модулем прямо в HUD - тогда колесо видно постоянно, в том числе поверх чата, как Target HUD.
     *
     * @param hovered   индекс сектора под курсором либо -1
     * @param dragging  колесо сейчас тащат мышью
     * @param showHints рисовать ли подписи-подсказки
     */
    public static void drawWheel(DrawContext context, SwapRingModule module, float centerX, float centerY, int hovered, boolean dragging, boolean showHints) {
        // DrawEngine работает в пикселях фреймбуфера, а колесо задано в scaled-пикселях GUI,
        // поэтому масштабируем матрицу рисования - позиции, радиусы, шрифты и иконки
        // попадают в нужные пиксели, а расчёт попадания курсора остаётся в GUI-пикселях.
        float scale = guiScale();
        // Пользовательский размер колеса: масштабируем всё относительно центра кольца,
        // чтобы радиусы, шрифты и иконки менялись пропорционально.
        float ringSize = module.ringSize();
        context.getMatrices().push();
        context.getMatrices().scale(scale, scale, 1.0f);
        context.getMatrices().translate(centerX, centerY, 0.0f);
        context.getMatrices().scale(ringSize, ringSize, 1.0f);
        context.getMatrices().translate(-centerX, -centerY, 0.0f);
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        DrawEngine drawEngine = Expensive.INSTANCE.drawEngine();
        ColorStack colors = drawEngine.colorStack();
        ThemePalette palette = Expensive.INSTANCE.theme().palette();
        MsdfFont font = Fonts.INTER_SEMIBOLD.get();

        int count = module.sectors();
        float sweep = 360.0f / count;
        float midRadius = (INNER_RADIUS + OUTER_RADIUS) / 2.0f;

        drawEngine.begin();
        try {
            // мягкая подложка, чтобы кольцо читалось поверх любого фона
            drawEngine.circle(matrix, centerX, centerY, OUTER_RADIUS + 16.0f, colors.computeColor(palette.surfaceBackground().tone(400).argb(), dragging ? 0.8f : 0.6f));
            drawEngine.hollowCircle(matrix, centerX, centerY, CENTER_RADIUS, 1.5f, colors.computeColor(palette.surfaceOutline().tone(600).argb(), 0.8f));

            for (int i = 0; i < count; i++) {
                boolean selected = i == hovered;
                ItemStack stack = module.sectorStack(i);
                int sectorColor = selected
                        ? colors.computeColor(palette.accent().argb(), 0.9f)
                        : colors.computeColor(palette.surfaceBackground().tone(600).argb(), stack.isEmpty() ? 0.45f : 0.75f);
                // фон сектора: 0 градусов шейдера = 3 часа, рост - против часовой
                drawEngine.arc(matrix, centerX, centerY, OUTER_RADIUS,
                        shaderAngle((i + 1) * sweep + SECTOR_GAP / 2.0f),
                        shaderAngle(i * sweep - SECTOR_GAP / 2.0f),
                        OUTER_RADIUS - INNER_RADIUS, sectorColor);
                if (!stack.isEmpty()) {
                    double midAngle = Math.toRadians((i + 0.5f) * sweep);
                    float iconX = centerX + (float) Math.sin(midAngle) * midRadius - 8.0f;
                    float iconY = centerY - (float) Math.cos(midAngle) * midRadius - 8.0f;
                    drawEngine.itemStack(matrix, stack, iconX, iconY, 0.5f, selected ? 1.0f : 0.8f);
                }
                // номер сектора - под ним прячется хоткей цифры 1-8
                double numberAngle = Math.toRadians((i + 0.5f) * sweep);
                float numberX = centerX + (float) Math.sin(numberAngle) * (OUTER_RADIUS - 9.0f);
                float numberY = centerY - (float) Math.cos(numberAngle) * (OUTER_RADIUS - 9.0f);
                drawEngine.msdfFontHorizontalC(matrix, font, String.valueOf(i + 1), numberX, numberY - font.getHeight(10.0f) / 2.0f, 10.0f, 0.0f,
                        colors.computeColor(palette.text().tone(selected ? 100 : 400).argb(), selected ? 1.0f : 0.7f));
            }

            if (!showHints) {
                return;
            }

            // подпись в центре: предмет под курсором либо подсказка
            String label;
            int labelColor;
            if (dragging) {
                label = "Двигай колесо · отпусти, чтобы закрепить";
                labelColor = colors.computeColor(palette.text().tone(100).argb());
            } else if (hovered >= 0) {
                ItemStack hoveredStack = module.sectorStack(hovered);
                if (hoveredStack.isEmpty()) {
                    label = "Пусто - назначь вещь в пресете";
                    labelColor = colors.computeColor(palette.text().tone(500).argb());
                } else {
                    int amount = module.sectorAmount(hovered);
                    label = Formatting.GRAY + hoveredStack.getName().getString() + (amount > 1 ? " x" + amount : "");
                    labelColor = colors.computeColor(palette.text().tone(200).argb());
                }
            } else {
                label = "Веди на сектор и отпусти клавишу";
                labelColor = colors.computeColor(palette.text().tone(400).argb(), 0.8f);
            }
            drawEngine.msdfFontHorizontalC(matrix, font, label, centerX, centerY - font.getHeight(13.0f) / 2.0f, 13.0f, 0.05f, labelColor);
            drawEngine.msdfFontHorizontalC(matrix, font, "ЛКМ - свап · Зажми центр - двигать колесо · Esc - отмена", centerX, centerY + OUTER_RADIUS + 20.0f, 12.0f, 0.0f, colors.computeColor(palette.text().tone(500).argb(), 0.7f));
        } finally {
            drawEngine.end();
            context.getMatrices().pop();
        }
    }

    /** Перевод экранного угла (по часовой от верха) в градусы шейдера: 0 = 3 часа, рост против часовой. */
    static float shaderAngle(float clockWiseFromTop) {
        float angle = 90.0f - clockWiseFromTop;
        angle %= 360.0f;
        return angle < 0.0f ? angle + 360.0f : angle;
    }

    /** Левый край дорожки слайдера размера внутри плашки (scaled GUI px). */
    public static float sizeTrackX(float panelX) {
        return panelX + 10.0f;
    }

    /** Длина дорожки слайдера размера (scaled GUI px). */
    public static float sizeTrackWidth() {
        return SIZE_PANEL_W - 20.0f;
    }

    /** Y дорожки слайдера размера внутри плашки. */
    public static float sizeTrackY(float panelY) {
        return panelY + SIZE_PANEL_H - 15.0f;
    }

    /** Текущий размер по позиции курсора вдоль дорожки слайдера (scaled GUI px). */
    public static float sizeFromMouse(double mouseX, float panelX) {
        float t = (float) ((mouseX - sizeTrackX(panelX)) / sizeTrackWidth());
        t = MathUtil.clamp(t, 0.0f, 1.0f);
        return RING_SIZE_MIN + t * (RING_SIZE_MAX - RING_SIZE_MIN);
    }

    /**
     * Рисует плашку настройки размера колеса: подпись, текущее значение и горизонтальный слайдер.
     * Показывается в чате по правому клику по колесу.
     */
    public static void drawSizePanel(DrawContext context, float panelX, float panelY, float size) {
        float scale = guiScale();
        context.getMatrices().push();
        context.getMatrices().scale(scale, scale, 1.0f);
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        DrawEngine drawEngine = Expensive.INSTANCE.drawEngine();
        ColorStack colors = drawEngine.colorStack();
        ThemePalette palette = Expensive.INSTANCE.theme().palette();
        MsdfFont font = Fonts.INTER_SEMIBOLD.get();

        float trackX = sizeTrackX(panelX);
        float trackY = sizeTrackY(panelY);
        float trackW = sizeTrackWidth();
        float t = (size - RING_SIZE_MIN) / (RING_SIZE_MAX - RING_SIZE_MIN);
        String valueText = String.format(java.util.Locale.ROOT, "%.2fx", size);

        drawEngine.begin();
        try {
            drawEngine.roundedRectangle(matrix, panelX, panelY, SIZE_PANEL_W, SIZE_PANEL_H, 7.0f, 1.0f,
                    colors.computeColor(palette.surfaceOutline().tone(600).argb(), 0.9f),
                    colors.computeColor(palette.surfaceBackground().tone(400).argb(), 0.94f));
            drawEngine.msdfFont(matrix, font, "Размер колеса", panelX + 10.0f, panelY + 10.0f, 12.0f, 0.0f,
                    colors.computeColor(palette.text().tone(200).argb()));
            drawEngine.msdfFont(matrix, font, valueText, panelX + SIZE_PANEL_W - 10.0f - font.getWidth(valueText, 12.0f), panelY + 10.0f, 12.0f, 0.0f,
                    colors.computeColor(palette.accent().argb()));
            // дорожка слайдера и её заполненная часть до текущего значения
            drawEngine.roundedRectangle(matrix, trackX, trackY, trackW, 4.0f, 2.0f,
                    colors.computeColor(palette.surfaceBackground().tone(600).argb(), 0.9f));
            drawEngine.roundedRectangle(matrix, trackX, trackY, trackW * t, 4.0f, 2.0f,
                    colors.computeColor(palette.accent().argb(), 0.95f));
            // ручка слайдера
            drawEngine.circle(matrix, trackX + trackW * t, trackY + 2.0f, 5.0f,
                    colors.computeColor(palette.accent().argb()));
        } finally {
            drawEngine.end();
            context.getMatrices().pop();
        }
    }

    /** Индекс сектора под курсором (для колеса с центром centerX/centerY), -1 - курсор в пустой центральной зоне. */
    static int sectorAt(double mouseX, double mouseY, float centerX, float centerY, float centerRadius, int count) {
        float dx = (float) (mouseX - centerX);
        float dy = (float) (mouseY - centerY);
        if (Math.sqrt(dx * dx + dy * dy) < centerRadius) {
            return -1;
        }
        float angle = (float) Math.toDegrees(Math.atan2(dx, -dy));
        if (angle < 0.0f) {
            angle += 360.0f;
        }
        return Math.min(count - 1, (int) (angle / (360.0f / count)));
    }

    private void updateHover(double mouseX, double mouseY) {
        this.hovered = sectorAt(mouseX, mouseY, this.centerX, this.centerY, centerRadius(this.module.ringSize()), this.module.sectors());
    }

    /** Начинает перетаскивание колеса от текущей позиции курсора. */
    private void startDrag(double mouseX, double mouseY) {
        this.dragging = true;
        this.dragMoved = false;
        this.dragOffsetX = (float) mouseX - this.centerX;
        this.dragOffsetY = (float) mouseY - this.centerY;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        updateHover(mouseX, mouseY);
        // Нажатие кнопки колеса (если бинд на мышь) - это то же нажатие, которым колесо открылось;
        // его не трактуем как клик по сектору. Свап подтверждается на отпускании.
        if (isRingMouseButton(button)) {
            return true;
        }
        // средняя кнопка мыши двигает колесо из любой точки
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            startDrag(mouseX, mouseY);
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (this.hovered >= 0) {
                confirmAndClose();
                return true;
            }
            // центр кольца - "ручка": зажал и повёл - колесо едет, короткий клик - отмена
            startDrag(mouseX, mouseY);
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            this.close();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** Кнопка мыши, которой открывают колесо (если бинд - кнопка мыши, а не клавиша). */
    private boolean isRingMouseButton(int button) {
        int key = this.module.ringKey.getKey();
        return key >= 0 && key <= GLFW.GLFW_MOUSE_BUTTON_LAST && button == key;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.dragging) {
            this.dragMoved = true;
            this.centerX = (float) mouseX - this.dragOffsetX;
            this.centerY = (float) mouseY - this.dragOffsetY;
            clampCenter();
            updateHover(mouseX, mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.dragging && (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE)) {
            this.dragging = false;
            if (this.dragMoved) {
                // колесо утащили - запоминаем позицию, она сохранится в конфиге
                this.module.setRingPosition(this.centerX, this.centerY);
                return true;
            }
            // короткий клик по центру без движения - как раньше, отменяем колесо
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                this.close();
            }
            return true;
        }
        // колесо могло быть привязано на кнопку мыши - её отпускание подтверждает свап
        if (button == this.module.ringKey.getKey()) {
            confirmAndClose();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }
        if (keyCode == this.module.ringKey.getKey()) {
            // Это либо то самое нажатие, которым открылось колесо, либо автоповтор удерживаемой
            // клавиши. Ни то, ни другое не должно подтверждать свап - иначе колесо закроется сразу.
            // Свап идёт по отпусканию клавиши (keyReleased).
            return true;
        }
        // цифры 1-8 выбирают сектор напрямую
        if (keyCode >= GLFW.GLFW_KEY_1 && keyCode <= GLFW.GLFW_KEY_8) {
            int index = keyCode - GLFW.GLFW_KEY_1;
            if (index < this.module.sectors()) {
                this.module.confirmSector(index);
            }
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        // отпустили удерживаемую клавишу колеса - свап на наведённый сектор
        if (keyCode == this.module.ringKey.getKey()) {
            confirmAndClose();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private void confirmAndClose() {
        if (this.hovered >= 0) {
            this.module.confirmSector(this.hovered);
        }
        this.close();
    }
}
