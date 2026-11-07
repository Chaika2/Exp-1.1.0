package aethereal.module;

import aethereal.Expensive;
import aethereal.Lang;
import aethereal.annotation.Aliases;
import aethereal.event.PlayerTickEvent;
import aethereal.event.Render2DEvent;
import aethereal.event.MouseButtonEvent2;
import aethereal.model.SlotSearchResult2;
import aethereal.model.Translation;
import aethereal.type.BindMode;
import aethereal.type.ButtonAction;
import aethereal.type.CombatPauseManager;
import aethereal.type.InventoryScope;
import aethereal.type.KeyAction;
import aethereal.type.Mc;
import aethereal.type.NotificationType;
import aethereal.ui.ModuleTab;
import aethereal.ui.SwapRingPresetScreen;
import aethereal.ui.SwapRingScreen;
import aethereal.ui.setting.ButtonSetting;
import aethereal.ui.setting.KeybindSetting;
import aethereal.ui.setting.NumberSetting;
import aethereal.ui.setting.RingSlotsSetting;
import aethereal.util.ConfigAutoSaveScheduler;
import aethereal.util.GrimDelayHandler;
import aethereal.util.MathUtil;
import aethereal.util.SwapUtil;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import org.lwjgl.glfw.GLFW;

/**
 * Колесо свапа: удерживай клавишу - на экране появляется кольцо секторов с вещами
 * из инвентаря, веди мышь на нужный сектор и отпускай клавишу - предмет уходит в
 * оффхенд (с паузой автосвапа, как обычный свап). Набор секторов настраивается в
 * меню пресета (кнопка "Настроить"): перетащи вещь из инвентаря на сектор ЛКМ, и
 * он сохранится в конфиге модуля.
 */
@Aliases(aliases = {"Swap Ring", "Кольцо свапа", "Колесо свапов", "AutoSwap Ring", "Свап колесо"})
public class SwapRingModule extends Module {
    public final Mc mc = Mc.INSTANCE;
    public final KeybindSetting ringKey;
    public final NumberSetting sectorCount;
    public final ButtonSetting presetButton;
    public final RingSlotsSetting ringSlots;
    /** Масштаб колеса: 0.5..2.0. Меняется слайдером в плашке (ПКМ по колесу в чате). */
    public final NumberSetting ringSize;
    /** Позиция центра колеса на экране (scaled GUI px). -1 значит "по центру экрана". */
    public final NumberSetting ringX;
    public final NumberSetting ringY;

    /** Свап, ждущий своего тика: экран закрывается сразу, клик уходит из PlayerTickEvent.PRE. */
    private SlotSearchResult2 pendingSwap;

    /** Текущий центр колеса для HUD-режима (когда открыт чат), scaled GUI px. */
    private float hudCenterX;
    private float hudCenterY;
    /** Перетаскивание колеса в чате - там же, где двигаются остальные HUD-элементы. */
    private boolean chatDragging;
    private float chatDragOffsetX;
    private float chatDragOffsetY;
    /** Плашка настройки размера колеса: открыта ли сейчас и тянется ли слайдер. */
    private boolean sizePanelOpen;
    private boolean sizeSliderDragging;

    public SwapRingModule() {
        super(ModuleTab.COMBAT, "Swap Wheel");
        this.ringKey = new KeybindSetting(Translation.clearText("Клавиша колеса"), Translation.clearText("Удерживай - колесо открыто, отпусти на секторе - свап.")).consumer(BindMode.HOLD, action -> {
            if (action == KeyAction.PRESS) {
                openRing();
            }
        });
        this.sectorCount = new NumberSetting(Translation.clearText("Секторов"), Translation.clearText("Количество слотов в колесе свапа")).currentValue(6.0f).range(3.0f, 8.0f).step(1.0f);
        this.presetButton = new ButtonSetting(Translation.clearText("Пресет колеса"), Translation.clearText("Менюшка, где ты расставляешь вещи по секторам колеса: слева инвентарь, справа колесо. Перетащи вещь из инвентаря ЛКМ на сектор - он сохранится. В бегу сектор без вещи просто не отображается.")).setButtonName(Translation.clearText("Настроить"));
        this.presetButton.setRunnable(this::openPresetEditor);
        this.ringSlots = new RingSlotsSetting(Translation.clearText("Предметы колеса"), Translation.clearText("Секторы через точку с запятой. Настраивается в меню пресета: перетащи вещь из инвентаря на нужный сектор."));
        this.ringSlots.setPlaceholder(Translation.clearText("Предмет не выбран"));
        // Позицию колеса можно менять прямо на экране (зажми центр колеса и веди мышь),
        // поэтому она живёт в скрытых настройках и сохраняется вместе с конфигом модуля.
        // Размер колеса тоже хранится здесь, но меняется слайдером в плашке (ПКМ по колесу в чате).
        this.ringSize = new NumberSetting(Translation.clearText("Колесо: размер"), Translation.clearText("Масштаб колеса свапа. Меняется слайдером в плашке, которая открывается правым кликом по колесу при открытом чате.")).currentValue(1.0f).range(SwapRingScreen.RING_SIZE_MIN, SwapRingScreen.RING_SIZE_MAX).step(SwapRingScreen.RING_SIZE_STEP).visible(() -> false);
        this.ringX = new NumberSetting(Translation.clearText("Колесо: X"), Translation.clearText("Позиция центра колеса свапа по горизонтали. Меняется перетаскиванием колеса на экране.")).currentValue(-1.0f).range(-1.0f, 8000.0f).step(1.0f).visible(() -> false);
        this.ringY = new NumberSetting(Translation.clearText("Колесо: Y"), Translation.clearText("Позиция центра колеса свапа по вертикали. Меняется перетаскиванием колеса на экране.")).currentValue(-1.0f).range(-1.0f, 8000.0f).step(1.0f).visible(() -> false);
        addSettings(this.ringKey, this.sectorCount, this.presetButton, this.ringSlots, this.ringSize, this.ringX, this.ringY);
        // Клик по оффхенду всегда делается из PlayerTickEvent.PRE, как в Auto Swap:
        // иначе сервер отбрасывает SWAP-клик, отправленный не в своём тике.
        register(PlayerTickEvent.class, event -> {
            if (event.isPre()) {
                tickPendingSwap();
            }
        });
        // Колесо показывается только в режиме настройки - когда открыт чат (как остальные HUD-элементы),
        // плюс отдельно по удержанию клавиши (интерактивный экран). При закрытом чате его не видно.
        register(Render2DEvent.class, event -> {
            if (!event.isPost() || !isState() || !this.mc.isWorldLoaded()) {
                return;
            }
            if (!(this.mc.getCurrentScreen() instanceof ChatScreen)) {
                // чат закрыли (в т.ч. прямо во время перетаскивания) - колесо прячем, позицию фиксируем
                if (this.chatDragging) {
                    this.chatDragging = false;
                    setRingPosition(this.hudCenterX, this.hudCenterY);
                }
                this.sizePanelOpen = false;
                this.sizeSliderDragging = false;
                return;
            }
            DrawContext context = event.drawContext();
            int scaledWidth = context.getScaledWindowWidth();
            int scaledHeight = context.getScaledWindowHeight();
            updateHudCenter(scaledWidth, scaledHeight);
            SwapRingScreen.drawWheel(context, this, this.hudCenterX, this.hudCenterY, -1, this.chatDragging, false);
            if (this.sizePanelOpen) {
                // тянем слайдер - обновляем размер по текущему положению курсора каждый кадр
                if (this.sizeSliderDragging) {
                    setRingSize(SwapRingScreen.sizeFromMouse(scaledMouseX(), sizePanelX()));
                }
                SwapRingScreen.drawSizePanel(context, sizePanelX(), sizePanelY(), ringSize());
            }
        });
        // Двигаем колесо мышью прямо в чате: зажал ЛКМ на колесе - ведёшь, отпустил - позиция
        // запомнилась. ПКМ по колесу открывает плашку настройки размера со слайдером.
        register(MouseButtonEvent2.class, event -> {
            int button = event.button();
            boolean dragButton = button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
            if (!isState() || !this.mc.isWorldLoaded() || !dragButton) {
                return;
            }
            if (!(this.mc.getCurrentScreen() instanceof ChatScreen)) {
                return;
            }
            updateHudCenter(this.mc.getWindow().getScaledWidth(), this.mc.getWindow().getScaledHeight());
            double mouseX = scaledMouseX();
            double mouseY = scaledMouseY();

            if (this.sizePanelOpen) {
                // плашка открыта: ЛКМ по слайдеру тянет размер, ПКМ или клик мимо - закрывают
                if (event.action() == ButtonAction.PRESS) {
                    if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                        this.sizePanelOpen = false;
                        this.sizeSliderDragging = false;
                    } else if (mouseX >= sizePanelX() && mouseX <= sizePanelX() + SwapRingScreen.SIZE_PANEL_W
                            && mouseY >= sizePanelY() && mouseY <= sizePanelY() + SwapRingScreen.SIZE_PANEL_H) {
                        this.sizeSliderDragging = true;
                        setRingSize(SwapRingScreen.sizeFromMouse(mouseX, sizePanelX()));
                    } else {
                        this.sizePanelOpen = false;
                        this.sizeSliderDragging = false;
                    }
                } else if (this.sizeSliderDragging) {
                    this.sizeSliderDragging = false;
                    ConfigAutoSaveScheduler.scheduleAutoSave();
                }
                return;
            }

            double dx = mouseX - this.hudCenterX;
            double dy = mouseY - this.hudCenterY;
            boolean overRing = dx * dx + dy * dy <= SwapRingScreen.outerRadius(ringSize()) * SwapRingScreen.outerRadius(ringSize());
            if (event.action() == ButtonAction.PRESS) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                    // правый клик по колесу - плашка настройки размера
                    if (overRing) {
                        this.sizePanelOpen = true;
                        Expensive.LOGGER.info("[DBG] SwapRing size panel open center=({},{})", this.hudCenterX, this.hudCenterY);
                    }
                    return;
                }
                if (overRing) {
                    this.chatDragging = true;
                    this.chatDragOffsetX = (float) mouseX - this.hudCenterX;
                    this.chatDragOffsetY = (float) mouseY - this.hudCenterY;
                    Expensive.LOGGER.info("[DBG] SwapRing chat drag start btn={} center=({},{})", button, this.hudCenterX, this.hudCenterY);
                }
            } else if (event.action() == ButtonAction.RELEASE && this.chatDragging) {
                this.chatDragging = false;
                setRingPosition(this.hudCenterX, this.hudCenterY);
                Expensive.LOGGER.info("[DBG] SwapRing chat drag end pos=({},{})", this.hudCenterX, this.hudCenterY);
            }
        });
    }

    /** X левого края плашки размера: справа от колеса, с ограничением по краю экрана. */
    private float sizePanelX() {
        int scaledWidth = this.mc.getWindow().getScaledWidth();
        float x = this.hudCenterX + SwapRingScreen.outerRadius(ringSize()) + 12.0f;
        return MathUtil.clamp(x, 4.0f, Math.max(4.0f, scaledWidth - SwapRingScreen.SIZE_PANEL_W - 4.0f));
    }

    /** Y верхнего края плашки размера: по центру колеса по вертикали. */
    private float sizePanelY() {
        int scaledHeight = this.mc.getWindow().getScaledHeight();
        float y = this.hudCenterY - SwapRingScreen.SIZE_PANEL_H / 2.0f;
        return MathUtil.clamp(y, 4.0f, Math.max(4.0f, scaledHeight - SwapRingScreen.SIZE_PANEL_H - 4.0f));
    }

    /** Центр колеса для HUD-режима: при перетаскивании - от курсора, иначе - сохранённая позиция (или центр экрана). */
    private void updateHudCenter(int scaledWidth, int scaledHeight) {
        if (this.chatDragging) {
            this.hudCenterX = (float) scaledMouseX() - this.chatDragOffsetX;
            this.hudCenterY = (float) scaledMouseY() - this.chatDragOffsetY;
        } else if (hasCustomPosition()) {
            this.hudCenterX = ringX();
            this.hudCenterY = ringY();
        } else {
            this.hudCenterX = scaledWidth / 2.0f;
            this.hudCenterY = scaledHeight / 2.0f;
        }
        float margin = SwapRingScreen.margin(ringSize());
        this.hudCenterX = MathUtil.clamp(this.hudCenterX, margin, Math.max(margin, scaledWidth - margin));
        this.hudCenterY = MathUtil.clamp(this.hudCenterY, margin, Math.max(margin, scaledHeight - margin));
    }

    /** X курсора в scaled GUI px - та же система, что у Screen.width/height и позиции колеса. */
    private double scaledMouseX() {
        return this.mc.getMouse().getX() * this.mc.getWindow().getScaledWidth() / (double) this.mc.getWindow().getWidth();
    }

    /** Y курсора в scaled GUI px. */
    private double scaledMouseY() {
        return this.mc.getMouse().getY() * this.mc.getWindow().getScaledHeight() / (double) this.mc.getWindow().getHeight();
    }

    /** Сколько секторов в колесе (3..8). */
    public int sectors() {
        return Math.max(3, Math.min(8, (int) this.sectorCount.currentValue()));
    }

    /** Есть ли у колеса сохранённая позиция (иначе оно рисуется по центру экрана). */
    public boolean hasCustomPosition() {
        return this.ringX.currentValue() >= 0.0f && this.ringY.currentValue() >= 0.0f;
    }

    /** Сохранённая позиция центра колеса по горизонтали (scaled GUI px). */
    public float ringX() {
        return this.ringX.currentValue();
    }

    /** Сохранённая позиция центра колеса по вертикали (scaled GUI px). */
    public float ringY() {
        return this.ringY.currentValue();
    }

    /** Запоминает новую позицию центра колеса (вызывается экраном при перетаскивании). */
    public void setRingPosition(float f, float f2) {
        this.ringX.setCurrentValue(f);
        this.ringY.setCurrentValue(f2);
        ConfigAutoSaveScheduler.scheduleAutoSave();
    }

    /** Масштаб колеса (0.5..2.0), хранится в конфиге модуля. */
    public float ringSize() {
        return MathUtil.clamp(this.ringSize.currentValue(), SwapRingScreen.RING_SIZE_MIN, SwapRingScreen.RING_SIZE_MAX);
    }

    /** Запоминает новый масштаб колеса (вызывается слайдером плашки размера). */
    public void setRingSize(float f) {
        this.ringSize.setCurrentValue(MathUtil.clamp(f, SwapRingScreen.RING_SIZE_MIN, SwapRingScreen.RING_SIZE_MAX));
    }

    /** Плашка размера открыта сейчас (в чате). */
    public boolean isSizePanelOpen() {
        return this.sizePanelOpen;
    }

    /** Открывает колесо свапа (вызывается по нажатию клавиши модуля). */
    public void openRing() {
        if (!isState() || !this.mc.isWorldLoaded()) {
            return;
        }
        if (this.mc.getCurrentScreen() != null) {
            return;
        }
        if (CombatPauseManager.INSTANCE.shouldPauseAutoSwap()) {
            return;
        }
        this.mc.setCurrentScreen(new SwapRingScreen(this));
    }

    /** Открывает редактор пресета колеса (менюшка "Настроить"). */
    public void openPresetEditor() {
        if (!isState() || !this.mc.isWorldLoaded()) {
            return;
        }
        if (this.mc.getCurrentScreen() != null) {
            return;
        }
        this.mc.setCurrentScreen(new SwapRingPresetScreen(this));
    }

    /** Подтверждение сектора экраном колеса: ищем вещь в инвентаре и ставим свап в очередь. */
    public void confirmSector(int index) {
        String id = this.ringSlots.slotId(index);
        if (id == null || id.isEmpty()) {
            return;
        }
        ClientPlayerEntity player = this.mc.getPlayer();
        if (player == null || CombatPauseManager.INSTANCE.shouldPauseAutoSwap()) {
            return;
        }
        Optional<SlotSearchResult2> found = Expensive.INSTANCE.inventoryService().searcher().findItem(filterFor(id), InventoryScope.HOTBAR, InventoryScope.INVENTORY);
        if (found.isEmpty() || !found.get().found()) {
            Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal("В Вашем инвентаре не найден предмет %s.".formatted(String.valueOf(Formatting.RED) + displayNameFor(id))), 3L, TimeUnit.SECONDS);
            return;
        }
        this.pendingSwap = found.get();
    }

    /** Свап уходит кликом только в своём тике - здесь очередь и исполняется. */
    private void tickPendingSwap() {
        SlotSearchResult2 pending = this.pendingSwap;
        if (pending == null) {
            return;
        }
        if (!isState() || !this.mc.isWorldLoaded() || CombatPauseManager.INSTANCE.shouldPauseAutoSwap()) {
            this.pendingSwap = null;
            return;
        }
        if (!GrimDelayHandler.script.isFinished()) {
            return;
        }
        ClientPlayerEntity player = this.mc.getPlayer();
        if (player == null) {
            this.pendingSwap = null;
            return;
        }
        int slot = pending.slotReference().increasedSlot();
        if (!AutoTotemModule.requestSprintPausedClick(player, slot, 2, new float[]{0.95f, 0.85f})) {
            return;
        }
        this.pendingSwap = null;
        SwapUtil.swapToOffhand(slot);
        String name = StringHelper.stripTextFormat(pending.stack().getName().getString()).replace("[★]", "").replace("fff", "").replace("ggg", "").replace("xxx", "").trim();
        Expensive.INSTANCE.notificationRepository().post(Text.empty().append(Lang.ELYTRAHELPER_SWAPPED_TO.effective().replace("{item}", String.valueOf(Formatting.RED) + name + String.valueOf(Formatting.RESET))), pending.stack(), 3L, TimeUnit.SECONDS);
    }

    /** Текущий стак предмета сектора в инвентаре (для отрисовки колеса), EMPTY если вещи нет. */
    public ItemStack sectorStack(int index) {
        String id = this.ringSlots.slotId(index);
        if (id == null || id.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Optional<SlotSearchResult2> found = Expensive.INSTANCE.inventoryService().searcher().findItem(filterFor(id), InventoryScope.HOTBAR, InventoryScope.INVENTORY);
        return found.isPresent() && found.get().found() ? found.get().stack() : ItemStack.EMPTY;
    }

    /** Суммарное количество предмета сектора в инвентаре (для подписи xN под иконкой). */
    public int sectorAmount(int index) {
        String id = this.ringSlots.slotId(index);
        if (id == null || id.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (SlotSearchResult2 result : Expensive.INSTANCE.inventoryService().searcher().findAllItems(filterFor(id), InventoryScope.HOTBAR, InventoryScope.INVENTORY)) {
            total += result.stack().getCount();
        }
        return total;
    }

    /** Идентификатор вещи для колеса: "namespace:item" + маркер "|d" для предметов с CUSTOM_DATA. */
    public static String itemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        return Registries.ITEM.getId(stack.getItem()) + (stack.contains(DataComponentTypes.CUSTOM_DATA) ? "|d" : "");
    }

    /** Фильтр поиска в инвентаре для идентификатора колеса. */
    public static Predicate<ItemStack> filterFor(String id) {
        if (id == null || id.isEmpty()) {
            return stack -> false;
        }
        boolean customData = id.endsWith("|d");
        String base = customData ? id.substring(0, id.length() - 2) : id;
        Identifier identifier;
        try {
            identifier = Identifier.of(base);
        } catch (Exception ignored) {
            // ручная правка текстового поля в настройках могла дать мусор
            return stack -> false;
        }
        return stack -> !stack.isEmpty()
                && stack.contains(DataComponentTypes.CUSTOM_DATA) == customData
                && Registries.ITEM.getId(stack.getItem()).equals(identifier);
    }

    /** Отображаемое имя предмета по идентификатору колеса. */
    public static String displayNameFor(String id) {
        if (id == null || id.isEmpty()) {
            return "";
        }
        String base = id.endsWith("|d") ? id.substring(0, id.length() - 2) : id;
        try {
            return Registries.ITEM.get(Identifier.of(base)).getName().getString();
        } catch (Exception ignored) {
            return base;
        }
    }
}
