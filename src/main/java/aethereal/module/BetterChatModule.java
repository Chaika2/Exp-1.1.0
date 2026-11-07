package aethereal.module;
import aethereal.accessor.ChatLineIdAccessor;
import aethereal.annotation.Aliases;
import aethereal.type.BetterChatOption;
import aethereal.event.ChatLimitEvent;
import aethereal.type.ChatLimitType;
import aethereal.type.ChatMessageType;
import aethereal.event.ChatReceiveEvent;
import aethereal.util.ChatUtil;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.MultiSelectSetting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.Formatting;

@Aliases(aliases = {"Better Chat", "Chat History", "Anti Spam", "Infinite Chat"})
public class BetterChatModule extends Module {
    public static final String externalSuffix = "-external";
    public final MultiSelectSetting<BetterChatOption> chatImprovements;
    private final Map<String, Integer> messageCounters;
    private final List<String> commonSeparators = List.of("-----", "======");
    private final int maximumOccurrences = 100;
    private final boolean onlyCompactConsecutiveMessages = false;
    private final boolean ignoreCommonSeparators = true;
    private String previousMessage;

    public BetterChatModule() {
        super(ModuleTab.MISC, "Better Chat");
        this.chatImprovements = new MultiSelectSetting(Lang.BETTERCHAT_CHAT_IMPROVEMENTS).values(BetterChatOption.class);
        this.messageCounters = new HashMap();
        addSettings(this.chatImprovements);
        register(ChatReceiveEvent.class, this::onChatReceive);
        register(ChatLimitEvent.class, this::onChatLimit);
    }

    public void onChatReceive(ChatReceiveEvent class066Var) {
        if (!isState() || !Mc.INSTANCE.isWorldLoaded() || class066Var.type() == ChatMessageType.DISGUISED_CHAT_MESSAGE || !this.chatImprovements.isSelected(BetterChatOption.ANTI_SPAM)) {
            return;
        }
        String raw = class066Var.textData().getString();
        String key = stripOccurrenceSuffix(TextVisitFactory.removeFormattingCodes(class066Var.textData()));
        if (shouldIgnoreByContent(raw)) {
            return;
        }
        int occurrences = this.messageCounters.getOrDefault(key, 0);
        if (this.shouldIgnore(raw, key)) {
            if (occurrences == 0) {
                this.messageCounters.put(key, 1);
            }
            return;
        }
        int i = Math.min(occurrences + 1, this.maximumOccurrences);
        this.messageCounters.put(key, i);
        ChatHud chatHud = Mc.INSTANCE.getInGameHud().getChatHud();
        String ourId = key + externalSuffix + System.nanoTime();
        if (i > 1) {
            chatHud.messages.removeIf(chatHudLine -> {
                String id = ((ChatLineIdAccessor) ChatLineIdAccessor.class.cast(chatHudLine)).expensive_ru$getId();
                if (ourId.startsWith(key + externalSuffix)) {
                    String existingId = id;
                    if (existingId != null && existingId.contains(key + externalSuffix)) {
                        return true;
                    }
                }
                return id == null && stripOccurrenceSuffix(chatHudLine.content().getString()).equals(key);
            });
            chatHud.visibleMessages.clear();
            for (int j = chatHud.messages.size() - 1; j >= 0; j--) {
                chatHud.addVisibleMessage(chatHud.messages.get(j));
            }
            class066Var.cancel();
            MutableText mutableTextLiteral = class066Var.applyChatDecoration().apply(class066Var.textData()).copy();
            mutableTextLiteral.append(Text.literal(this.occurrenceSuffix(i)).formatted(Formatting.GRAY));
            ChatUtil.addMessage(chatHud, mutableTextLiteral, key + externalSuffix);
            return;
        }
        class066Var.cancel();
        ChatUtil.addMessage(chatHud, class066Var.applyChatDecoration().apply(class066Var.textData()), key + externalSuffix);
    }

    public void onChatLimit(ChatLimitEvent class056Var) {
        if (class056Var.getType().isHistory()) {
            this.messageCounters.clear();
        }
        if (isState() && Mc.INSTANCE.isWorldLoaded()) {
            ChatLimitType type = class056Var.getType();
            if ((type.isHistory() && this.chatImprovements.isSelected(BetterChatOption.ANTI_CLEAR)) || (type.isLimit() && this.chatImprovements.isSelected(BetterChatOption.INFINITY))) {
                class056Var.cancel();
            }
        }
    }

    private boolean shouldIgnoreByContent(String string) {
        if (string == null) {
            return true;
        }
        String lower = string.toLowerCase();
        if (lower.contains("ивент") || lower.contains("статус") || lower.contains("до следующего ивента") || lower.contains("координаты")) {
            return true;
        }
        if (lower.contains("телепортироваться") || lower.contains("/warp") || lower.contains("пробудился") || lower.contains("появился")) {
            return true;
        }
        if (lower.contains("tpa") || lower.contains("tpaccept") || lower.contains("tpacancel") || lower.contains("запрос на телепорт") || lower.contains("принять телепорт")) {
            return true;
        }
        return false;
    }

    private boolean shouldIgnore(String string, String string2) {
        if (string.isBlank()) {
            return true;
        }
        if (this.onlyCompactConsecutiveMessages && !string2.equals(this.previousMessage)) {
            return true;
        }
        if (this.ignoreCommonSeparators) {
            return this.commonSeparators.stream().filter(string3 -> !string3.isBlank()).anyMatch(string2::contains);
        }
        return false;
    }

    private String occurrenceSuffix(int i) {
        if (i >= this.maximumOccurrences) {
            return " (" + this.maximumOccurrences + "+)";
        }
        return " (" + i + ")";
    }

    private String stripOccurrenceSuffix(String string) {
        return string.replaceAll("\\s*\\(\\d+\\+?\\)$", "");
    }
}