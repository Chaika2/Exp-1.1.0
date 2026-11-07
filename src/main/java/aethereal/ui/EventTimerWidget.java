package aethereal.ui;

import aethereal.Expensive;
import aethereal.Lang;
import aethereal.math.Easings;
import aethereal.model.DragRenderContext;
import aethereal.model.MouseButtonInput2;
import aethereal.model.MouseMoveInput;
import aethereal.module.EventTimerModule;
import aethereal.render.AnimatedFloat;
import aethereal.render.ColorStack;
import aethereal.render.Fonts;
import aethereal.render.MsdfFont;
import aethereal.render.ThemePalette;
import aethereal.type.Mc;
import aethereal.util.DrawEngine;
import aethereal.util.WeightedEngine;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;

public class EventTimerWidget extends Draggable {
    static final float padding = 8.0f;
    static final float cornerRadius = 7.0f;
    static final float titleTextSize = 12.0f;
    static final float rowTextSize = 11.0f;
    static final float titleHeight = 17.0f;
    static final float rowHeight = 13.5f;
    static final float gpsButtonWidth = 26.0f;
    static final float gpsButtonHeight = 10.0f;
    static final float gpsButtonGap = 5.0f;
    static final ZoneId MOSCOW = ZoneId.of("Europe/Moscow");
    static final ScheduleEntry AIR_DROP = new ScheduleEntry("Air Drop", timesEveryHours(0, 3));
    static final ScheduleEntry SCROOGE = new ScheduleEntry("Scrooge", new int[]{1140});
    static final ScheduleEntry SECRET_TRADER = new ScheduleEntry("Secret Trader", timesEveryHours(1, 3));
    static final ScheduleEntry MASCOT = new ScheduleEntry("Mascot", new int[]{1200});
    static final ScheduleEntry COMPETITION = new ScheduleEntry("Competition", timesEveryHours(2, 3));
    static final List<ScheduleEntry> FT_SCHEDULE = List.of(AIR_DROP, SCROOGE, SECRET_TRADER, MASCOT, COMPETITION);
    public float width;
    public float height;
    public final AnimatedFloat visibilityAnimation;
    private final List<GpsButton> gpsButtons = new ArrayList<>();

    public EventTimerWidget(BooleanSupplier booleanSupplier) {
        super("EventTimer", booleanSupplier);
        this.visibilityAnimation = new AnimatedFloat(250, Easings.LINEAR);
        this.x = 10.0f;
        this.y = 120.0f;
    }

    @Override
    public void layout(DragRenderContext dragRenderContext) {
    }

    @Override
    public void render(DragRenderContext dragRenderContext) {
        float animatedValue = this.visibilityAnimation.animatedValue();
        if (animatedValue <= 0.01f) {
            this.width = 0.0f;
            this.height = 0.0f;
            this.gpsButtons.clear();
            return;
        }
        List<Row> rows = buildRowObjects();
        if (rows.isEmpty()) {
            this.width = 0.0f;
            this.height = 0.0f;
            this.gpsButtons.clear();
            return;
        }
        MsdfFont titleFont = Fonts.INTER_EXTRA_BOLD.get();
        MsdfFont rowFont = Fonts.INTER_BOLD.get();
        MatrixStack matrixStack = dragRenderContext.matrixStack();
        DrawEngine drawEngine = dragRenderContext.drawEngine();
        ColorStack colorStack = drawEngine.colorStack();
        String title = Lang.EVENT_TIMER_WIDGET_TITLE.effective();
        float titleWidth = titleFont.getWidth(title, titleTextSize);
        float contentWidth = 0.0f;
        for (Row row : rows) {
            float rowWidth = rowFont.getWidth(row.name(), rowTextSize) + 6.0f + rowFont.getWidth(row.timeText(), rowTextSize);
            if (row.hasGps()) {
                rowWidth += gpsButtonGap + gpsButtonWidth;
            }
            contentWidth = Math.max(contentWidth, rowWidth);
        }
        this.width = Math.max(75.0f, Math.max(titleWidth + 16.0f, contentWidth + (padding * 2.0f)));
        this.height = padding + titleHeight + (rows.size() * rowHeight) + 4.0f;
        ThemePalette palette = Expensive.INSTANCE.theme().palette();
        colorStack.push();
        colorStack.alpha(animatedValue);
        drawEngine.roundedRectangle(matrixStack.peek().getPositionMatrix(), this.x, this.y, this.width, this.height, cornerRadius, 2.5f, colorStack.computeColor(palette.surfaceOutline().tone(700).argb()), colorStack.computeColor(palette.surfaceBackground().tone(801).argb()), colorStack.computeColor(palette.surfaceBackground().tone(801).argb()), colorStack.computeColor(palette.surfaceBackground().tone(900).argb()), colorStack.computeColor(palette.surfaceBackground().tone(900).argb()));
        float rowY = this.y + padding;
        drawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), titleFont, title, this.x + padding, rowY, titleTextSize, 0.0f, colorStack.computeColor(palette.text().tone(300).argb()));
        rowY += titleHeight;
        this.gpsButtons.clear();
        for (Row row : rows) {
            int textColor = colorStack.computeColor(palette.text().tone(400).argb());
            int timeColor = getTimeColor(colorStack, palette, row.phase());
            float timeWidth = rowFont.getWidth(row.timeText(), rowTextSize);
            float nameStart = this.x + padding;
            float nameWidth = rowFont.getWidth(row.name(), rowTextSize);
            if (nameWidth > (this.width - padding * 2.0f - (row.hasGps() ? gpsButtonGap + gpsButtonWidth : 0.0f) - timeWidth - 4.0f)) {
                nameWidth = (this.width - padding * 2.0f - (row.hasGps() ? gpsButtonGap + gpsButtonWidth : 0.0f) - timeWidth - 4.0f);
            }
            drawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), rowFont, row.name(), nameStart, rowY, rowTextSize, 0.0f, textColor);
            float timeX = (this.x + this.width) - padding - timeWidth;
            if (row.hasGps()) {
                timeX -= (gpsButtonGap + gpsButtonWidth);
            }
            drawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), rowFont, row.timeText(), timeX, rowY, rowTextSize, 0.0f, timeColor);
            if (row.hasGps()) {
                float btnX = (this.x + this.width) - padding - gpsButtonWidth;
                float btnY = rowY - 1.0f;
                boolean hovered = isGpsHovered(btnX, btnY);
                int btnFill = colorStack.computeColor(palette.accent().argb(), hovered ? 0.9f : 0.7f);
                int btnOutline = colorStack.computeColor(palette.surfaceOutline().tone(600).argb(), 0.8f);
                drawEngine.roundedRectangle(matrixStack.peek().getPositionMatrix(), btnX, btnY, gpsButtonWidth, gpsButtonHeight, 3.0f, 0.5f, btnOutline, btnFill, btnFill, btnFill, btnFill);
                float gpsTextWidth = rowFont.getWidth("GPS", rowTextSize - 1.0f);
                drawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), rowFont, "GPS", btnX + (gpsButtonWidth - gpsTextWidth) / 2.0f, btnY + 1.5f, rowTextSize - 1.0f, 0.0f, colorStack.computeColor(palette.text().tone(900).argb()));
                this.gpsButtons.add(new GpsButton(btnX, btnY, gpsButtonWidth, gpsButtonHeight, row.gpsX(), row.gpsZ()));
            }
            rowY += rowHeight;
        }
        colorStack.pop();
    }

    public List<Row> buildRowObjects() {
        ArrayList<Row> rows = new ArrayList<Row>();
        EventTimerModule module = (EventTimerModule) Expensive.INSTANCE.moduleRepository().get(EventTimerModule.class);
        EventTimerModule.Mode mode = module == null ? EventTimerModule.Mode.FT : module.currentMode();
        if (mode == EventTimerModule.Mode.ML) {
            if (module == null || !module.isState()) {
                return rows;
            }
            long now = System.currentTimeMillis();
            for (EventTimerModule.MlEvent event : module.mlActiveEvents.stream().limit(4L).toList()) {
                rows.add(new Row(event.name(), formatTime(event.targetAt() - now), event.phase(), event.x(), event.z(), event.hasCoordinates()));
            }
            if (module.mlClearAt > now) {
                rows.add(new Row(Lang.EVENT_TIMER_CLEAR.effective(), formatTime(module.mlClearAt - now), EventTimerModule.Phase.ACTIVE, 0, 0, false));
            }
            if (rows.isEmpty() && module.mlNextEventAt > now) {
                rows.add(new Row(Lang.EVENT_TIMER_NEXT_EVENT.effective(), formatTime(module.mlNextEventAt - now), EventTimerModule.Phase.SOON, 0, 0, false));
            }
            if (rows.isEmpty()) {
                rows.add(new Row(Lang.EVENT_TIMER_NO_DATA.effective(), formatTime(0L), EventTimerModule.Phase.SOON, 0, 0, false));
            }
        } else {
            ZonedDateTime now = ZonedDateTime.now(MOSCOW);
            int currentSecond = (now.getHour() * 3600) + (now.getMinute() * 60) + now.getSecond();
            for (ScheduleEntry entry : FT_SCHEDULE) {
                rows.add(new Row(entry.name(), formatSeconds(countdown(entry, currentSecond)), EventTimerModule.Phase.SOON, 0, 0, false));
            }
        }
        return rows;
    }

    @Deprecated
    public List<String[]> buildRows() {
        ArrayList<String[]> rows = new ArrayList<String[]>();
        for (Row row : buildRowObjects()) {
            rows.add(new String[]{row.name(), row.timeText()});
        }
        return rows;
    }

    public int countdown(ScheduleEntry entry, int currentSecond) {
        int remaining = Integer.MAX_VALUE;
        for (int minute : entry.minutesOfDay()) {
            int delta = (minute * 60) - currentSecond;
            if (delta < 0) {
                delta += 86400;
            }
            remaining = Math.min(remaining, delta);
        }
        return remaining;
    }

    public static String formatTime(long millis) {
        return formatSeconds(Math.max(0L, millis / 1000L));
    }

    public static String formatSeconds(long seconds) {
        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long secs = seconds % 60L;
        if (hours > 0L) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, secs);
        }
        return String.format(Locale.US, "%02d:%02d", minutes, secs);
    }

    static int[] timesEveryHours(int startHour, int intervalHours) {
        int count = 24 / intervalHours;
        int[] times = new int[count];
        for (int index = 0; index < count; index++) {
            times[index] = ((startHour + (index * intervalHours)) % 24) * 60;
        }
        return times;
    }

    private int getTimeColor(ColorStack colorStack, ThemePalette palette, EventTimerModule.Phase phase) {
        if (phase == EventTimerModule.Phase.ACTIVE) {
            return colorStack.computeColor(palette.deepGreen.argb());
        }
        return colorStack.computeColor(palette.text().tone(400).argb());
    }

    private boolean isGpsHovered(float x, float y) {
        MouseMoveInput input = null;
        if (Expensive.INSTANCE == null) {
            return false;
        }
        return false;
    }

    @Override
    public boolean click(MouseButtonInput2 mouseButtonInput2, boolean z) {
        if (!mouseButtonInput2.isLeftButtonPressed() || !mouseButtonInput2.press()) {
            return false;
        }
        for (GpsButton button : new ArrayList<>(this.gpsButtons)) {
            if (mouseButtonInput2.isWithinBounds(button.x(), button.y(), button.w(), button.h())) {
                try {
                    Expensive.INSTANCE.commandDispatcher().dispatch("gps " + button.xCoord() + " " + button.zCoord());
                } catch (Exception ignored) {
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean cursor(MouseMoveInput mouseMoveInput, boolean z) {
        return false;
    }

    @Override
    public void animate(WeightedEngine weightedEngine) {
        EventTimerModule module = (EventTimerModule) Expensive.INSTANCE.moduleRepository().get(EventTimerModule.class);
        boolean chatOpen = Mc.INSTANCE.getCurrentScreen() instanceof ChatScreen;
        boolean hasContent = module == null || module.currentMode() == EventTimerModule.Mode.FT || (module.isState() && module.usesMlMode());
        this.visibilityAnimation.destination(chatOpen || hasContent ? 1.0f : 0.0f);
        this.visibilityAnimation.animate(weightedEngine);
    }

    @Override
    public void update() {
    }

    @Override
    public float width() {
        return this.width;
    }

    @Override
    public float height() {
        return this.height;
    }

    public record ScheduleEntry(String name, int[] minutesOfDay) {
    }

    public record Row(String name, String timeText, EventTimerModule.Phase phase, int gpsX, int gpsZ, boolean hasGps) {
    }

    public record GpsButton(float x, float y, float w, float h, int xCoord, int zCoord) {
    }
}