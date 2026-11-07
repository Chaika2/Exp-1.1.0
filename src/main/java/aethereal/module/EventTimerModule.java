package aethereal.module;

import aethereal.Expensive;
import aethereal.Lang;
import aethereal.event.ChatReceiveEvent;
import aethereal.event.ClientTickEvent;
import aethereal.event.WorldLoadEvent;
import aethereal.math.Stopwatch;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;
import aethereal.type.Mc;
import aethereal.type.NotificationType;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.setting.NumberSetting;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventTimerModule extends Module {
    public static final String EVENT_DELAY_COMMAND = "/event delay";

    private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    private static final String BOX_CHARS = "╔╗╚╝║═╦╩╠╣╬";

    /** Удаление цветовых кодов §a, §k, §f и др. */
    private static final Pattern COLOR_CODES = Pattern.compile("§.");
    /** Обрезка рамочных символов по краям строки */
    private static final Pattern BOX_EDGES = Pattern.compile("^[\\s" + BOX_CHARS + "]+|[\\s" + BOX_CHARS + "]+$");
    /** Имя ивента в квадратных скобках [Мистический сундук] */
    private static final Pattern BRACKET_NAME = Pattern.compile("\\[([^\\]]+)]");
    /** Координаты [2399 66 1230] */
    private static final Pattern COORDS_PATTERN = Pattern.compile("\\[\\s*(-?\\d+)\\s+(-?\\d+)\\s+(-?\\d+)\\s*]");
    /** Парсинг "3 минуты", "30 сек", "1 мин", "5 с" */
    private static final Pattern DURATION_PART = Pattern.compile("(\\d+)\\s*(мин\\p{L}*|сек\\p{L}*|с(?!\\p{L}))", FLAGS);
    /** "До следующего ивента: ..." */
    private static final Pattern NEXT_EVENT_PATTERN = Pattern.compile("до\\s+следующего\\s+ивента\\s*:\\s*(.+)", FLAGS);
    /** Чистое число */
    private static final Pattern BARE_NUMBER = Pattern.compile("^\\s*(\\d+)\\s*$");

    /** Сколько держим ACTIVE-ивент, если не получили явного завершения */
    private static final long ACTIVE_LIFETIME_MS = 10 * 60_000L;
    /** Грейс после истечения SOON-таймера */
    private static final long SOON_GRACE_MS = 20_000L;
    /** Время жизни pendingEventName после последней строки с именем */
    private static final long NAME_TTL_MS = 30_000L;

    public final ModeSetting<Mode> mode;
    public final BooleanSetting autoRequest;
    public final NumberSetting requestInterval;
    public final BooleanSetting notifications;
    public final Stopwatch requestTimer = new Stopwatch();

    public long mlNextEventAt = -1L;
    public long mlClearAt = -1L;

    public final List<MlEvent> mlActiveEvents = new CopyOnWriteArrayList<>();

    public String lastNotifiedEvent;
    public String pendingEventName = null;
    private long pendingNameAt = 0L;

    public EventTimerModule() {
        super(ModuleTab.MISC, "Event Timer");
        this.mode = new ModeSetting<Mode>(Lang.EVENT_TIMER_MODE)
                .options(Mode.values())
                .currentValue(Mode.FT);
        this.autoRequest = new BooleanSetting(Lang.EVENT_TIMER_AUTO_REQUEST, Lang.EVENT_TIMER_AUTO_REQUEST_DESC)
                .setValue(true)
                .visible(this::usesMlMode);
        this.requestInterval = new NumberSetting(Lang.EVENT_TIMER_INTERVAL, Lang.EVENT_TIMER_INTERVAL_DESC)
                .currentValue(3.0f)
                .range(0.5f, 10.0f)
                .step(0.5f)
                .visible(this::requestVisible);
        this.notifications = new BooleanSetting(Lang.EVENT_TIMER_NOTIFICATIONS, Lang.EVENT_TIMER_NOTIFICATIONS_DESC)
                .visible(this::usesMlMode);

        addSettings(this.mode, this.autoRequest, this.requestInterval, this.notifications);

        register(WorldLoadEvent.class, e -> {
            if (shouldAutoRequest()) {
                sendEventDelay();
            }
        });

        register(ClientTickEvent.class, e -> {
            if (!isState() || !usesMlMode()) {
                return;
            }

            long now = System.currentTimeMillis();
            cleanup(now);

            if (shouldAutoRequest()) {
                double minutes = requestInterval.currentValue();
                long intervalMs = (long) (minutes * 60_000.0D);
                if (intervalMs < 30_000L) {
                    intervalMs = 30_000L;
                }
                if (requestTimer.getElapsedTime(TimeUnit.MILLISECONDS) >= intervalMs) {
                    sendEventDelay();
                }
            }
        });

        register(ChatReceiveEvent.class, this::onChat);
    }

    public Mode currentMode() {
        return (Mode) this.mode.currentValue();
    }

    public boolean usesMlMode() {
        return currentMode() == Mode.ML;
    }

    public boolean requestVisible() {
        return usesMlMode() && this.autoRequest.isValue();
    }

    public boolean shouldAutoRequest() {
        return isState() && usesMlMode() && this.autoRequest.isValue() && Mc.INSTANCE.isWorldLoaded();
    }

    public void sendEventDelay() {
        ClientPlayNetworkHandler networkHandler = Mc.INSTANCE.getNetworkHandler();
        if (networkHandler == null) {
            return;
        }
        networkHandler.sendChatMessage(EVENT_DELAY_COMMAND);
        requestTimer.reset();
    }

    public void onChat(ChatReceiveEvent event) {
        if (!isState() || !usesMlMode() || !Mc.INSTANCE.isWorldLoaded()) {
            return;
        }

        String text = event.textData().getString();
        if (text == null || text.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        String[] lines = text.split("\\R");
        for (String rawLine : lines) {
            processLine(rawLine, now);
        }
    }

    private void processLine(String rawLine, long now) {
        String line = COLOR_CODES.matcher(rawLine).replaceAll("").trim();
        if (line.isEmpty()) {
            return;
        }

        char first = line.charAt(0);
        boolean isBoxLine = BOX_CHARS.indexOf(first) >= 0;

        if (!isBoxLine) {
            if (handleNextEvent(line, now)) {
                return;
            }
            if (isClearMessage(line)) {
                handleClearMessage(line, now);
                return;
            }
            return;
        }

        if (first == '╔' || first == '╚') {
            this.pendingEventName = null;
            return;
        }

        String content = BOX_EDGES.matcher(line).replaceAll("").replace("|", "").trim();
        if (content.isEmpty()) {
            return;
        }

        Matcher bracket = BRACKET_NAME.matcher(content);
        if (bracket.find()) {
            if (!COORDS_PATTERN.matcher(content).find()) {
                String name = bracket.group(1).trim();
                if (!name.isEmpty() && name.length() < 80) {
                    char c = name.charAt(0);
                    if (Character.isLetter(c) || Character.isDigit(c)) {
                        this.pendingEventName = name;
                        this.pendingNameAt = now;
                        return;
                    }
                }
            }
        }

        if (this.pendingEventName == null) {
            return;
        }

        if (now - this.pendingNameAt > NAME_TTL_MS) {
            this.pendingEventName = null;
            return;
        }

        String name = this.pendingEventName;
        String lower = content.toLowerCase();

        Matcher coords = COORDS_PATTERN.matcher(content);
        if (coords.find()) {
            try {
                int x = Integer.parseInt(coords.group(1));
                int z = Integer.parseInt(coords.group(3));
                upsert(name, now, true, x, z, Phase.ACTIVE, now);
            } catch (NumberFormatException ignored) {
            }
            return;
        }

        if (lower.contains("появился") || lower.contains("заспавнился")) {
            upsert(name, now, false, 0, 0, Phase.ACTIVE, now);
            return;
        }

        if (lower.contains("появится")) {
            long duration = durationAfterVia(content);
            if (duration < 0) {
                duration = parseDurationMs(content);
            }
            if (duration >= 0) {
                upsert(name, now + duration, false, 0, 0, Phase.SOON, now);
            }
            return;
        }
    }

    private boolean handleNextEvent(String line, long now) {
        Matcher m = NEXT_EVENT_PATTERN.matcher(line);
        if (!m.find()) {
            return false;
        }

        String tail = m.group(1).trim();
        long duration = parseDurationMs(tail);
        if (duration < 0) {
            Matcher bare = BARE_NUMBER.matcher(tail);
            if (bare.find()) {
                try {
                    duration = Long.parseLong(bare.group(1)) * 1000L;
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (duration >= 0) {
            this.mlNextEventAt = now + duration;
        }

        return true;
    }

    public boolean isClearMessage(String text) {
        String lower = text.toLowerCase();
        return lower.contains("очистк") || lower.contains("очистится");
    }

    private void handleClearMessage(String text, long now) {
        String lower = text.toLowerCase();

        if (lower.contains("все очистится") || lower.contains("полностью очистится")
                || lower.contains("все будет очищено") || lower.contains("будет очищена карта")
                || lower.contains("вся карта") || lower.contains("весь мир")) {
            return;
        }

        if (lower.contains("удален") || lower.contains("удалён") || lower.contains("очищено")
                || lower.contains("завершен") || lower.contains("завершён") || lower.contains("законч")
                || lower.contains("завершится")) {
            this.mlClearAt = -1L;
            return;
        }

        long duration = durationAfterVia(text);
        if (duration < 0) {
            duration = parseDurationMs(text);
        }

        if (duration >= 0) {
            this.mlClearAt = now + duration;
        }
    }

    public boolean isEventMessage(String text) {
        if (text == null) {
            return false;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        String lower = trimmed.toLowerCase();
        if (lower.contains("ивент") || lower.contains("появится") || lower.contains("появился")) {
            return true;
        }
        char first = trimmed.charAt(0);
        return BOX_CHARS.indexOf(first) >= 0;
    }

    private static long durationAfterVia(String text) {
        String lower = text.toLowerCase();
        int idx = lower.indexOf("через");
        if (idx < 0) {
            return -1L;
        }
        String after = text.substring(idx + 5);
        long dur = parseDurationMs(after);
        if (dur < 0) {
            Matcher bare = BARE_NUMBER.matcher(after.trim());
            if (bare.find()) {
                try {
                    return Long.parseLong(bare.group(1)) * 1000L;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return dur;
    }

    static long parseDurationMs(String text) {
        if (text == null) {
            return -1L;
        }
        Matcher m = DURATION_PART.matcher(text);
        long total = 0L;
        boolean found = false;
        while (m.find()) {
            try {
                long value = Long.parseLong(m.group(1));
                String unit = m.group(2).toLowerCase();
                if (unit.startsWith("мин")) {
                    total += value * 60_000L;
                } else {
                    total += value * 1000L;
                }
                found = true;
            } catch (NumberFormatException ignored) {
            }
        }
        return found ? total : -1L;
    }

    private static String formatRemaining(long ms) {
        if (ms <= 0) {
            return "меньше минуты";
        }
        long sec = ms / 1000L;
        if (sec >= 60) {
            long m = sec / 60;
            long s = sec % 60;
            if (s == 0) {
                return m + " мин";
            } else {
                return m + " мин " + s + " сек";
            }
        }
        return sec + " сек";
    }

    private void upsert(String name, long targetAt, boolean hasCoords, int x, int z, Phase phase, long now) {
        MlEvent current = null;
        for (MlEvent event : mlActiveEvents) {
            if (event.name().equals(name)) {
                current = event;
                break;
            }
        }

        int newX = x;
        int newZ = z;
        long newTarget = targetAt;

        if (current != null && phase == Phase.ACTIVE && current.phase() == Phase.ACTIVE) {
            newTarget = current.targetAt();
            if (!hasCoords) {
                newX = current.x();
                newZ = current.z();
            }
        }

        boolean phaseChanged = current == null || current.phase() != phase;

        if (current != null) {
            mlActiveEvents.remove(current);
        }

        mlActiveEvents.add(new MlEvent(name, newTarget, newX, newZ, phase));

        if (phaseChanged) {
            boolean coordsKnown = hasCoords || (current != null && current.hasCoordinates());
            notifyEvent(name, phase, newTarget - now, newX, newZ, coordsKnown);
        }
    }

    private void notifyEvent(String name, Phase phase, long remainingMs, int x, int z, boolean coordsKnown) {
        this.lastNotifiedEvent = name;
        if (!this.notifications.isValue()) {
            return;
        }

        String message;
        if (phase == Phase.ACTIVE) {
            if (coordsKnown && (x != 0 || z != 0)) {
                message = "Ивент: " + name + " [" + x + " " + z + "]";
            } else {
                message = "Ивент: " + name + " появился";
            }
        } else {
            if (remainingMs <= 0) {
                return;
            }
            message = "Ивент: " + name + " через " + formatRemaining(remainingMs);
        }

        Expensive.INSTANCE.notificationRepository().post(NotificationType.EVENT, Text.literal(message), 5L, TimeUnit.SECONDS);
    }

    private void cleanup(long now) {
        mlActiveEvents.removeIf(event -> {
            if (event.phase() == Phase.ACTIVE) {
                return now - event.targetAt() > ACTIVE_LIFETIME_MS;
            } else {
                return now - event.targetAt() > SOON_GRACE_MS;
            }
        });

        if (this.mlNextEventAt > 0 && now > this.mlNextEventAt + 2000L) {
            this.mlNextEventAt = -1L;
        }

        if (this.mlClearAt > 0 && now > this.mlClearAt + 2000L) {
            this.mlClearAt = -1L;
        }

        if (mlActiveEvents.isEmpty()) {
            this.lastNotifiedEvent = null;
        }

        if (this.pendingEventName != null && now - this.pendingNameAt > NAME_TTL_MS) {
            this.pendingEventName = null;
        }
    }

    @Override
    public void deactivate() {
        this.mlNextEventAt = -1L;
        this.mlClearAt = -1L;
        this.mlActiveEvents.clear();
        this.lastNotifiedEvent = null;
        this.pendingEventName = null;
        this.pendingNameAt = 0L;
        super.deactivate();
    }

    public enum Mode implements DisplayNamed {
        FT(Lang.EVENT_TIMER_MODE_FT),
        ML(Lang.EVENT_TIMER_MODE_ML);

        private final Translation displayName;

        Mode(Translation translation) {
            this.displayName = translation;
        }

        @Override
        public Translation getDisplayName() {
            return this.displayName;
        }
    }

    public enum Phase {
        SOON,
        ACTIVE
    }

    public record MlEvent(String name, long targetAt, int x, int z, Phase phase) {
        public boolean hasCoordinates() {
            return x != 0 || z != 0;
        }
    }
}