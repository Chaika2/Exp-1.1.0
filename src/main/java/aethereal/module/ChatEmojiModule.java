package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.event.PacketSendEvent;
import aethereal.model.Translation;
import aethereal.type.ChatColorOption;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.ModeSetting;

import java.util.Random;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

@Aliases(aliases = {"Chat Emoji", "Kaomoji", "Emoji Chat", "Смайлики", "Смайлы", "Global Chat Emoji"})
public class ChatEmojiModule extends Module {
    private static final String[] BRIGHT_COLORS = {"&b", "&3", "&e", "&6", "&c", "&4", "&d", "&5", "&a", "&2", "&f"};
    private static final String PREFIX = "!";
    private static final String[] EMOJIS = {"UwU", "OwO", "0w0", "QwQ", "TwT", "T_T", "X_X", "o_o", "O_O", "u_u", "-_-", "-.-", "=_=", ">_<", "<_>", ">w<", "-w-", "x.x", "o.o", "u.u", ";_;", "Q_Q", "T.T", "._.", "(o_o)", "(O_O)", "(T_T)", "(X_X)", "(-_-)", "(>_<)", "(._.)", "(=_=)", "(n_n)", "(u_u)", "(Q_Q)", "(UwU)", "(OwO)", "(0w0)", ":3", ":D", ";D", ":P", ";P", "XD", "xD", "8D", "B)", "8)", "<3", "(>w<)"};

    public final BooleanSetting colorText;
    public final ModeSetting<ChatColorOption> colorSetting;
    public final BooleanSetting rgbText;
    public final BooleanSetting boldText;
    public final BooleanSetting underlineText;
    public final BooleanSetting strikethroughText;
    public final BooleanSetting appendEmoji;
    public final BooleanSetting antiMatBypass;
    public final BooleanSetting localChat;
    public final BooleanSetting globalChat;
    private final Random random = new Random();
    private boolean processing;

    public ChatEmojiModule() {
        super(ModuleTab.MISC, "Chat Emoji");
        this.colorText = new BooleanSetting(Translation.clearText("Цвет текста"), Translation.clearText("Красит сообщение в выбранный цвет.")).setValue(true);
        this.colorSetting = new ModeSetting(Translation.clearText("Цвет"), Translation.clearText("Выбери цвет для текста в чате.")).values(ChatColorOption.class).select(ChatColorOption.DARK_GREEN);
        this.rgbText = new BooleanSetting(Translation.clearText("RGB текст"), Translation.clearText("Каждая буква красится случайным цветом, не совпадающим с соседними буквами.")).setValue(false);
        this.boldText = new BooleanSetting(Translation.clearText("Жирный шрифт"), Translation.clearText("Делает текст жирным (&l).")).setValue(false);
        this.underlineText = new BooleanSetting(Translation.clearText("Подчёркнутый"), Translation.clearText("Подчёркивает текст (&n).")).setValue(false);
        this.strikethroughText = new BooleanSetting(Translation.clearText("Перечёркнутый"), Translation.clearText("Зачёркивает текст (&m).")).setValue(false);
        this.appendEmoji = new BooleanSetting(Translation.clearText("Смайлик в конце"), Translation.clearText("Добавляет случайный каомодзи в конец сообщения.")).setValue(true);
        this.antiMatBypass = new BooleanSetting(Translation.clearText("Обход анти-мата"), Translation.clearText("Вставляет случайный цветовой код между буквами, чтобы цензура не находила слова как цельные.")).setValue(false);
        this.localChat = new BooleanSetting(Translation.clearText("Локальный чат"), Translation.clearText("Применять цвет и смайлики к обычному чату (без ! в начале).")).setValue(true);
        this.globalChat = new BooleanSetting(Translation.clearText("Глобальный чат"), Translation.clearText("Применять цвет и смайлики к глобальному чату (с ! в начале).")).setValue(true);
        addSettings(this.colorText, this.colorSetting, this.rgbText, this.boldText, this.underlineText, this.strikethroughText, this.appendEmoji, this.antiMatBypass, this.localChat, this.globalChat);
        register(PacketSendEvent.class, this::onPacketSend);
    }

    public void onPacketSend(PacketSendEvent class037Var) {
        if (!isState() || !Mc.INSTANCE.isWorldLoaded() || this.processing || class037Var.isCancelled()) {
            return;
        }
        if (!(class037Var.getPacket() instanceof ChatMessageC2SPacket packet)) {
            return;
        }
        String strMessage = packet.chatMessage();
        if (strMessage == null || strMessage.isEmpty()) {
            return;
        }
        if (strMessage.startsWith("/") || strMessage.startsWith(".")) {
            return;
        }
        boolean global = strMessage.startsWith(PREFIX);
        boolean local = !global;
        if (global && !this.globalChat.isValue()) {
            return;
        }
        if (local && !this.localChat.isValue()) {
            return;
        }
        boolean bypassText = this.antiMatBypass.isValue();
        String strBody = global ? strMessage.substring(1) : strMessage;
        if (strBody.isEmpty()) {
            return;
        }
        if (bypassText || (this.colorText.isValue() && !strBody.startsWith("&"))) {
            if (bypassText || this.rgbText.isValue()) {
                strBody = applyRgb(strBody);
            } else {
                StringBuilder strPrefix = new StringBuilder(this.colorSetting.currentValue().code);
                if (this.boldText.isValue()) {
                    strPrefix.append("&l");
                }
                if (this.underlineText.isValue()) {
                    strPrefix.append("&n");
                }
                if (this.strikethroughText.isValue()) {
                    strPrefix.append("&m");
                }
                strBody = strPrefix + strBody.replaceAll("\\s+", " " + strPrefix);
            }
        }
        StringBuilder strBuilder = new StringBuilder();
        if (global) {
            strBuilder.append(PREFIX);
        }
        strBuilder.append(strBody);
        if (this.appendEmoji.isValue()) {
            strBuilder.append(' ').append(EMOJIS[this.random.nextInt(EMOJIS.length)]);
        }
        String strModified = strBuilder.toString();
        if (strModified.equals(strMessage)) {
            return;
        }
        ChatMessageC2SPacket class037Var2 = new ChatMessageC2SPacket(strModified, packet.timestamp(), packet.salt(), packet.signature(), packet.acknowledgment());
        class037Var.cancel();
        this.processing = true;
        try {
            ClientConnection class434VarConnection = Mc.INSTANCE.getNetworkHandler().getConnection();
            class434VarConnection.send(class037Var2);
        } finally {
            this.processing = false;
        }
    }

    private String applyRgb(String str) {
        StringBuilder strBuilder = new StringBuilder(str.length() + str.length() * 4);
        String strFormatCodes = "";
        if (this.boldText.isValue()) {
            strFormatCodes = strFormatCodes + "&l";
        }
        if (this.underlineText.isValue()) {
            strFormatCodes = strFormatCodes + "&n";
        }
        if (this.strikethroughText.isValue()) {
            strFormatCodes = strFormatCodes + "&m";
        }
        String strLastColor = null;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == ' ') {
                strBuilder.append(c);
                continue;
            }
            String strColor = randomColorExcluding(strLastColor);
            strBuilder.append(strColor).append(strFormatCodes).append(c);
            strLastColor = strColor;
        }
        return strBuilder.toString();
    }

    private String randomColorExcluding(String strExcluded) {
        String strColor;
        do {
            strColor = BRIGHT_COLORS[this.random.nextInt(BRIGHT_COLORS.length)];
        } while (strColor.equals(strExcluded));
        return strColor;
    }
}