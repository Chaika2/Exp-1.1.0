package aethereal.module;
import aethereal.util.ActionScheduler;
import aethereal.annotation.Aliases;
import aethereal.type.AutoAuthMode;
import aethereal.ui.setting.ButtonSetting;
import aethereal.event.ChatReceiveEvent;
import aethereal.util.ChatUtil;
import aethereal.model.CredentialKey;
import aethereal.Expensive;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.ModuleTab;
import aethereal.type.NotificationType;
import aethereal.event.PlayerTickEvent;
import aethereal.util.StringUtil;
import aethereal.ui.setting.TextFieldSetting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

@Aliases(aliases = {"Auto Auth", "Auto Login", "Auto Register", "Server Auth", "Login Helper"})
public class AutoAuthModule extends Module {
    public static final Pattern registerPattern = Pattern.compile("(?i)(^|\\s)/(register|reg)(\\s|$)");
    public static final Pattern loginPattern = Pattern.compile("(?i)(^|\\s)/(login|l)(\\s|$)");
    public final ModeSetting<AutoAuthMode> modeSetting;
    public final TextFieldSetting customValueSetting;
    public final Path defaultConfigPath;
    public boolean credentialsLoaded;
    public final Map<CredentialKey, String> credentials;

    public final ActionScheduler actionScheduler;
    public boolean processing;

    public AutoAuthModule() {
        super(ModuleTab.MISC, "Auto Auth");
        this.modeSetting = new ModeSetting(Lang.AUTOAUTH_MODE).values(AutoAuthMode.class);
        this.customValueSetting = new TextFieldSetting(Lang.AUTOAUTH_CUSTOM_VALUE).setPlaceholder(Lang.AUTOAUTH_CUSTOM_VALUE_PLACEHOLDER).setText("1337").setPassword(true).visible(() -> {
            return Boolean.valueOf(this.modeSetting.isSelected(AutoAuthMode.CUSTOM));
        });
        this.defaultConfigPath = Path.of("expensive/config/auto-auth.json", new String[0]);
        this.credentials = new HashMap();
        this.actionScheduler = new ActionScheduler();
        this.processing = false;
        addSettings(this.modeSetting, this.customValueSetting, new ButtonSetting(Lang.AUTOAUTH_OPEN_DIRECTORY_BUTTON, Lang.AUTOAUTH_OPEN_DIRECTORY_BUTTON_DESC).setButtonName(Lang.AUTOAUTH_OPEN_DIRECTORY_BUTTON_NAME).setRunnable(() -> {
            try {
                Path pathMethod007 = getConfigPath();
                if (Files.exists(pathMethod007, new LinkOption[0])) {
                    new ProcessBuilder("explorer", pathMethod007.toAbsolutePath().toString()).start();
                    Expensive.INSTANCE.notificationRepository().post(NotificationType.INFO, (Text) Text.literal(Lang.AUTOAUTH_OPEN_DIRECTORY_SUCCESS.effective()), 3L, TimeUnit.SECONDS);
                } else {
                    Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal(Lang.AUTOAUTH_OPEN_DIRECTORY_FAIL.effective()), 3L, TimeUnit.SECONDS);
                }
            } catch (IOException e) {
                Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal(Lang.AUTOAUTH_OPEN_DIRECTORY_ERROR.effective()), 3L, TimeUnit.SECONDS);
                Expensive.LOGGER.error("Failed to open directory with credentials", e);
            }
        }));
        register(ChatReceiveEvent.class, class066Var -> {
            String strMessage;
            if (!isState() || this.processing || (strMessage = class066Var.message()) == null) {
                return;
            }
            loadCredentials();
            String strRemoveSymbols = StringUtil.removeSymbols(strMessage);
            if (registerPattern.matcher(strRemoveSymbols).find()) {
                scheduleAuthAction(this::doRegister);
            } else if (loginPattern.matcher(strRemoveSymbols).find()) {
                scheduleAuthAction(this::doLogin);
            }
        });
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded()) {
                this.actionScheduler.update().cleanupIfFinished();
            }
        });
    }

    public void scheduleAuthAction(Runnable runnable) {
        this.processing = true;
        this.actionScheduler.addTickStep(10, () -> {
            runnable.run();
            this.processing = false;
        }, () -> {
            return Mc.INSTANCE.getMinecraft().getNetworkHandler() != null;
        });
    }

    public void doRegister() throws MatchException {
        String strMethod006 = generatePassword();
        if (strMethod006 == null) {
            return;
        }
        this.credentials.put(currentCredentialKey(), strMethod006);
        sendAuthCommand("register " + strMethod006 + " " + strMethod006, "Успешная регистрация! §7(наведите для просмотра пароля)", strMethod006);
        saveCredentials();
    }

    public void doLogin() {
        CredentialKey class166VarMethod005 = currentCredentialKey();
        if (this.credentials.containsKey(class166VarMethod005)) {
            String str = this.credentials.get(class166VarMethod005);
            sendAuthCommand("login " + str, "Успешная авторизация! §7(наведите для просмотра пароля)", str);
        } else if (this.modeSetting.isSelected(AutoAuthMode.CUSTOM)) {
            String text = this.customValueSetting.getText();
            sendAuthCommand("login " + text, "Успешная авторизация! §7(наведите для просмотра пароля)", text);
        }
    }

    public String generatePassword() throws MatchException {
        switch ((AutoAuthMode) this.modeSetting.currentValue()) {
            case RANDOM:
                return generateRandomString(12);
            case CUSTOM:
                return getCustomPassword();
            default:
                throw new MatchException((String) null, (Throwable) null);
        }
    }

    public String getCustomPassword() {
        String text = this.customValueSetting.getText();
        if (text.isEmpty()) {
            return null;
        }
        return text;
    }

    public String generateRandomString(int i) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i2 = 0; i2 < i; i2++) {
            sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".charAt(random.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".length())));
        }
        return sb.toString();
    }

    public void sendAuthCommand(String str, String str2, String str3) {
        Mc.INSTANCE.getNetworkHandler().sendChatCommand(str);
        ChatUtil.addChatMessage((Text) Text.literal(str2 + ": ").append(Text.literal("******").styled(style -> {
            return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("§7" + Lang.AUTOAUTH_CUSTOM_VALUE.effective() + ": §f" + str3)));
        })));
    }

    public void saveCredentials() {
        try {
            Expensive.INSTANCE.configManager().saveAutoAuth(this.credentials);
        } catch (IOException e) {
            Expensive.LOGGER.error("Failed to save credentials", e);
        }
    }

    public CredentialKey currentCredentialKey() {
        ServerInfo currentServer = Mc.INSTANCE.getCurrentServer();
        return new CredentialKey(currentServer != null ? currentServer.address : "local", Mc.INSTANCE.getSession().getUsername());
    }

    public void loadCredentials() {
        if (this.credentialsLoaded || Expensive.INSTANCE.configManager() == null) {
            return;
        }
        try {
            this.credentials.clear();
            this.credentials.putAll(Expensive.INSTANCE.configManager().loadAutoAuth());
            this.credentialsLoaded = true;
        } catch (IOException e) {
            Expensive.LOGGER.error("Failed to load credentials", e);
        }
    }

    public Path getConfigPath() {
        return Expensive.INSTANCE.configManager() != null ? Expensive.INSTANCE.configManager().autoAuthConfig().path() : this.defaultConfigPath;
    }
}
