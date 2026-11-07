package aethereal.util;

import aethereal.Expensive;
import aethereal.config.AltManagerStore;
import aethereal.model.AltAccount;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;

public class AltManager {
    public static final String TYPE_CRACK = "Crack";
    public static final String TYPE_MICROSOFT = "Microsoft";

    private final List<AltAccount> accounts = new ArrayList();
    private final AltManagerStore store;
    private String lastType = TYPE_CRACK;
    private String lastNick = "";

    public AltManager(Path path) {
        this.store = new AltManagerStore(path);
        load();
    }

    public void load() {
        try {
            List<AltAccount> list = this.store.load();
            this.accounts.clear();
            this.accounts.addAll(list);
            message("Загружено аккаунтов: " + this.accounts.size());
        } catch (IOException e) {
            Expensive.LOGGER.error("Failed to load alts", e);
        }
    }

    public void save() {
        try {
            this.store.save(this.accounts);
        } catch (IOException e) {
            Expensive.LOGGER.error("Failed to save alts", e);
        }
    }

    public List<AltAccount> accounts() {
        return this.accounts;
    }

    public String lastType() {
        return this.lastType;
    }

    public void lastType(String type) {
        if (type == null || type.isBlank()) {
            return;
        }
        this.lastType = type;
        saveSettings();
    }

    public String lastNick() {
        return this.lastNick;
    }

    public void rememberNick(String nick) {
        if (nick == null || nick.isBlank()) {
            return;
        }
        this.lastNick = nick;
        saveSettings();
    }

    public boolean contains(String name) {
        if (name == null) {
            return false;
        }
        for (AltAccount acc : this.accounts) {
            if (name.equalsIgnoreCase(acc.name)) {
                return true;
            }
        }
        return false;
    }

    public void addAlt(String name, String type) {
        if (name == null || name.isBlank() || contains(name)) {
            return;
        }
        this.accounts.add(0, new AltAccount(name, type));
        save();
    }

    public void removeAlt(String name) {
        if (name == null) {
            return;
        }
        this.accounts.removeIf(acc -> name.equalsIgnoreCase(acc.name));
        save();
    }

    public AltAccount find(String name) {
        if (name == null) {
            return null;
        }
        for (AltAccount acc : this.accounts) {
            if (name.equalsIgnoreCase(acc.name)) {
                return acc;
            }
        }
        return null;
    }

    public boolean login(String name, String type) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || name == null || name.isBlank()) {
            return false;
        }
        if (TYPE_CRACK.equalsIgnoreCase(type)) {
            crack(name);
        } else {
            microsoftStub(name);
        }
        rememberNick(name);
        if (Expensive.INSTANCE != null) {
            try {
                Expensive.INSTANCE.configManager().menuStateConfig().setLastType(type);
                Expensive.INSTANCE.configManager().saveMenuState();
            } catch (Exception e) {
                aethereal.Expensive.LOGGER.error("Failed to save menu state", e);
            }
        }
        return true;
    }

    public boolean loginAccount(AltAccount account) {
        if (account == null) {
            return false;
        }
        account.lastUsed = System.currentTimeMillis();
        save();
        return login(account.name, account.type);
    }

    public void crack(String name) {
        ServerUtil.applySessionNickname(name);
        message("Вошли офлайн как: " + name + " (Crack)");
    }

    private void microsoftStub(String name) {
        message("Вход через Microsoft пока недоступен (нужен Azure Client ID). Установлен ник: " + name);
    }

    private void saveSettings() {
        if (Expensive.INSTANCE != null && Expensive.INSTANCE.configManager() != null) {
            Expensive.INSTANCE.configManager().menuStateConfig().setLastType(this.lastType);
            try {
                Expensive.INSTANCE.configManager().saveMenuState();
            } catch (Exception e) {
            }
        }
    }

    private void message(String str) {
        Expensive.LOGGER.info("[Alts] {}", str);
    }
}
