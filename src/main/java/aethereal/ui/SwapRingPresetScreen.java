package aethereal.ui;

import aethereal.Expensive;
import aethereal.module.SwapRingModule;
import aethereal.render.ColorStack;
import aethereal.render.Fonts;
import aethereal.render.MsdfFont;
import aethereal.render.ThemePalette;
import aethereal.util.ConfigAutoSaveScheduler;
import aethereal.util.DrawEngine;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

/**
 * Редактор пресета колеса свапа: слева инвентарь, справа само колесо с секторами.
 * Возьми вещь из инвентаря (зажав ЛКМ или ПКМ) и перетащи её на сектор - пресет
 * сохраняется в настройках модуля. ПКМ по сектору, когда в руке ничего нет - очистить,
 * Esc - готово. Сектор без вещи в бегу просто не отображается.
 */
public class SwapRingPresetScreen extends Screen {
    /** Ячейка инвентаря: 16px иконка + отступы. */
    private static final float CELL = 20.0f;
    private static final float CELL_GAP = 2.0f;
    private static final float SLOT_RADIUS = 4.0f;

    private final SwapRingModule module;
    /** Слот инвентаря (0..35), вещь из которого сейчас тащат мышью (-1 - ничего не тащат). */
    private int dragSlot = -1;
    /** Предмет, который тащится мышью от нажатия по слоту до отпускания над сектором. */
    private ItemStack dragItem = ItemStack.EMPTY;
    /** Кнопка, которой взяли вещь (ЛКМ или ПКМ) - отпускание той же кнопки ставит вещь на сектор. */
    private int dragButton = -1;

    public SwapRingPresetScreen(SwapRingModule module) {
        super(Text.literal("Swap Wheel Preset"));
        this.module = module;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // --- геометрия инвентарной сетки: 3 ряда основной части + зазор + хотбар ---

    private float gridX() {
        return 18.0f;
    }

    private float gridY() {
        return (this.height - gridHeight()) / 2.0f;
    }

    private float gridHeight() {
        return CELL * 5.0f;
    }

    private float slotX(int inventoryIndex) {
        int column = inventoryIndex >= 9 ? (inventoryIndex - 9) % 9 : inventoryIndex;
        return gridX() + column * CELL;
    }

    private float slotY(int inventoryIndex) {
        int row = inventoryIndex >= 9 ? (inventoryIndex - 9) / 9 : 3;
        return gridY() + row * CELL + (row == 3 ? CELL : 0.0f);
    }

    private int hoveredSlot(double mouseX, double mouseY) {
        for (int i = 0; i < 36; i++) {
            float x = slotX(i);
            float y = slotY(i);
            if (mouseX >= x && mouseX < x + CELL - CELL_GAP && mouseY >= y && mouseY < y + CELL - CELL_GAP) {
                return i;
            }
        }
        return -1;
    }

    // --- геометрия колеса (такая же, как в бегу: SwapRingScreen) ---

    private float wheelCenterX() {
        return Math.max(this.width - (SwapRingScreen.OUTER_RADIUS + 30.0f), gridX() + 9.0f * CELL + SwapRingScreen.OUTER_RADIUS + 50.0f);
    }

    private float wheelCenterY() {
        return this.height / 2.0f;
    }

    private int hoveredSector(double mouseX, double mouseY) {
        float centerX = wheelCenterX();
        float centerY = wheelCenterY();
        float dx = (float) (mouseX - centerX);
        float dy = (float) (mouseY - centerY);
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        // сектор под курсором только в пределах кольца, иначе отпускание мимо колеса ничего не ставит
        if (distance < SwapRingScreen.INNER_RADIUS || distance > SwapRingScreen.OUTER_RADIUS + 12.0f) {
            return -1;
        }
        return SwapRingScreen.sectorAt(mouseX, mouseY, centerX, centerY, SwapRingScreen.INNER_RADIUS, this.module.sectors());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client == null || this.client.player == null) {
            this.close();
            return;
        }
        PlayerInventory inventory = this.client.player.getInventory();
        // DrawEngine рисует в пикселях фреймбуфера, а вся геометрия задана в scaled-пикселях
        // GUI - масштабируем матрицу, чтобы колесо и инвентарь совпадали с курсором.
        float scale = SwapRingScreen.guiScale();
        context.getMatrices().push();
        context.getMatrices().scale(scale, scale, 1.0f);
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        DrawEngine drawEngine = Expensive.INSTANCE.drawEngine();
        ColorStack colors = drawEngine.colorStack();
        ThemePalette palette = Expensive.INSTANCE.theme().palette();
        MsdfFont font = Fonts.INTER_SEMIBOLD.get();

        int count = this.module.sectors();
        float sweep = 360.0f / count;
        float wheelX = wheelCenterX();
        float wheelY = wheelCenterY();
        float midRadius = (SwapRingScreen.INNER_RADIUS + SwapRingScreen.OUTER_RADIUS) / 2.0f;
        int sector = hoveredSector(mouseX, mouseY);
        int slot = hoveredSlot(mouseX, mouseY);

        drawEngine.begin();
        try {
            // панель под инвентарём
            drawEngine.roundedRectangle(matrix, gridX() - 9.0f, gridY() - 9.0f, 9.0f * CELL + 18.0f - CELL_GAP, gridHeight() + 18.0f - CELL_GAP, 8.0f,
                    colors.computeColor(palette.surfaceBackground().tone(400).argb(), 0.65f));
            drawEngine.msdfFont(matrix, font, "Инвентарь", gridX() - 1.0f, gridY() - 15.0f, 12.0f, 0.0f, colors.computeColor(palette.text().tone(300).argb()));

            // слоты инвентаря
            for (int i = 0; i < 36; i++) {
                ItemStack stack = inventory.getStack(i);
                float x = slotX(i);
                float y = slotY(i);
                int slotColor = i == this.dragSlot
                        ? colors.computeColor(palette.accent().argb(), 0.85f)
                        : colors.computeColor(palette.surfaceBackground().tone(600).argb(), i == slot ? 0.9f : 0.5f);
                drawEngine.roundedRectangle(matrix, x, y, CELL - CELL_GAP, CELL - CELL_GAP, SLOT_RADIUS, slotColor);
                if (!stack.isEmpty()) {
                    drawEngine.itemStack(matrix, stack, x + 1.0f, y + 1.0f, 0.5f, i == this.dragSlot ? 0.35f : 0.9f);
                }
            }

            // колесо: подложка, секторы, иконки назначенных вещей, номера
            drawEngine.circle(matrix, wheelX, wheelY, SwapRingScreen.OUTER_RADIUS + 16.0f, colors.computeColor(palette.surfaceBackground().tone(400).argb(), 0.6f));
            drawEngine.hollowCircle(matrix, wheelX, wheelY, SwapRingScreen.INNER_RADIUS - 8.0f, 1.5f, colors.computeColor(palette.surfaceOutline().tone(600).argb(), 0.8f));
            for (int i = 0; i < count; i++) {
                boolean highlighted = i == sector;
                ItemStack assigned = this.module.sectorStack(i);
                int sectorColor = highlighted
                        ? colors.computeColor(palette.accent().argb(), 0.9f)
                        : colors.computeColor(palette.surfaceBackground().tone(600).argb(), assigned.isEmpty() ? 0.45f : 0.75f);
                drawEngine.arc(matrix, wheelX, wheelY, SwapRingScreen.OUTER_RADIUS,
                        SwapRingScreen.shaderAngle((i + 1) * sweep + SwapRingScreen.SECTOR_GAP / 2.0f),
                        SwapRingScreen.shaderAngle(i * sweep - SwapRingScreen.SECTOR_GAP / 2.0f),
                        SwapRingScreen.OUTER_RADIUS - SwapRingScreen.INNER_RADIUS, sectorColor);
                if (highlighted && !this.dragItem.isEmpty()) {
                    // яркая окантовка сектора-цели, пока вещь "в руке"
                    drawEngine.arc(matrix, wheelX, wheelY, SwapRingScreen.OUTER_RADIUS - 1.0f,
                            SwapRingScreen.shaderAngle((i + 1) * sweep + SwapRingScreen.SECTOR_GAP / 2.0f),
                            SwapRingScreen.shaderAngle(i * sweep - SwapRingScreen.SECTOR_GAP / 2.0f),
                            3.0f, colors.computeColor(palette.accent().argb(), 1.0f));
                }
                double midAngle = Math.toRadians((i + 0.5f) * sweep);
                float iconX = wheelX + (float) Math.sin(midAngle) * midRadius - 8.0f;
                float iconY = wheelY - (float) Math.cos(midAngle) * midRadius - 8.0f;
                if (!assigned.isEmpty()) {
                    drawEngine.itemStack(matrix, assigned, iconX, iconY, 0.5f, highlighted ? 1.0f : 0.85f);
                }
                float numberX = wheelX + (float) Math.sin(midAngle) * (SwapRingScreen.OUTER_RADIUS - 9.0f);
                float numberY = wheelY - (float) Math.cos(midAngle) * (SwapRingScreen.OUTER_RADIUS - 9.0f);
                drawEngine.msdfFontHorizontalC(matrix, font, String.valueOf(i + 1), numberX, numberY - font.getHeight(10.0f) / 2.0f, 10.0f, 0.0f,
                        colors.computeColor(palette.text().tone(highlighted ? 100 : 400).argb(), highlighted ? 1.0f : 0.7f));
            }

            // что под курсором: имя вещи на секторе либо имя слота инвентаря
            String label;
            int labelColor = colors.computeColor(palette.text().tone(200).argb());
            boolean dragging = !this.dragItem.isEmpty();
            if (sector >= 0) {
                ItemStack assigned = this.module.sectorStack(sector);
                if (dragging) {
                    label = Formatting.GRAY + "Отпусти на сектор " + (sector + 1) + (assigned.isEmpty() ? "" : Formatting.GRAY + " (заменит " + Formatting.WHITE + assigned.getName().getString() + Formatting.GRAY + ")");
                } else {
                    label = assigned.isEmpty()
                            ? Formatting.GRAY + "Сектор " + (sector + 1) + " - пусто"
                            : Formatting.GRAY + "Сектор " + (sector + 1) + ": " + Formatting.WHITE + assigned.getName().getString();
                }
            } else if (dragging) {
                label = Formatting.GRAY + "Ведёшь: " + Formatting.WHITE + this.dragItem.getName().getString() + Formatting.GRAY + " - отпусти над сектором";
            } else if (slot >= 0 && !inventory.getStack(slot).isEmpty()) {
                label = Formatting.GRAY + inventory.getStack(slot).getName().getString();
            } else {
                label = Formatting.GRAY + "Зажми ЛКМ или ПКМ на вещи и перетащи её на сектор колеса";
                labelColor = colors.computeColor(palette.text().tone(400).argb(), 0.8f);
            }
            drawEngine.msdfFontHorizontalC(matrix, font, label, this.width / 2.0f, this.height - 42.0f, 13.0f, 0.05f, labelColor);
            drawEngine.msdfFontHorizontalC(matrix, font, "Зажми ЛКМ/ПКМ на вещи и перетащи на сектор · ПКМ по сектору (без вещи) - очистить · Esc - готово",
                    this.width / 2.0f, this.height - 24.0f, 12.0f, 0.0f, colors.computeColor(palette.text().tone(500).argb(), 0.7f));

            // призрак вещи в "руке" при перетаскивании
            if (!this.dragItem.isEmpty()) {
                drawEngine.itemStack(matrix, this.dragItem, (float) mouseX - 8.0f, (float) mouseY - 8.0f, 0.5f, 0.9f);
            }
        } finally {
            drawEngine.end();
            context.getMatrices().pop();
        }
    }

    /** Ставит вещь из слота инвентаря на сектор и запоминает пресет. */
    private void assign(int sectorIndex, int inventoryIndex) {
        if (this.client == null || this.client.player == null) {
            return;
        }
        ItemStack stack = this.client.player.getInventory().getStack(inventoryIndex);
        if (stack.isEmpty()) {
            return;
        }
        this.module.ringSlots.setSlotId(sectorIndex, SwapRingModule.itemId(stack));
        ConfigAutoSaveScheduler.scheduleAutoSave();
        Expensive.LOGGER.info("[DBG] SwapRing preset assign sector={} item={} button={}", sectorIndex, stack.getName().getString(), this.dragButton);
    }

    /** Очищает сектор. */
    private void clear(int sectorIndex) {
        this.module.ringSlots.clearSlot(sectorIndex);
        ConfigAutoSaveScheduler.scheduleAutoSave();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.client == null || this.client.player == null) {
            return true;
        }
        // вещь берётся любой из кнопок: зажал ЛКМ или ПКМ на слоте - поехала за курсором
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            int slot = hoveredSlot(mouseX, mouseY);
            if (slot >= 0) {
                ItemStack stack = this.client.player.getInventory().getStack(slot);
                if (!stack.isEmpty()) {
                    this.dragSlot = slot;
                    this.dragItem = stack;
                    this.dragButton = button;
                }
                return true;
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && this.dragItem.isEmpty()) {
                // ПКМ по сектору, когда в руке ничего нет - очистить сектор
                int sector = hoveredSector(mouseX, mouseY);
                if (sector >= 0) {
                    clear(sector);
                }
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        // вещь в руке следует за курсором (призрак рисуется в render), событие дальше не отдаём
        if (button == this.dragButton && !this.dragItem.isEmpty()) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // отпустили тащимую вещь той же кнопкой над сектором - поставили её туда и сохранили пресет
        if (button == this.dragButton && !this.dragItem.isEmpty()) {
            int sector = hoveredSector(mouseX, mouseY);
            if (sector >= 0 && this.dragSlot >= 0) {
                assign(sector, this.dragSlot);
            }
            this.dragSlot = -1;
            this.dragItem = ItemStack.EMPTY;
            this.dragButton = -1;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            // пресет сохраняется по мере правок, тут лишь закрываем редактор
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
