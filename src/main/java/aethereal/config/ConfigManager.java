package aethereal.config;
import aethereal.model.CredentialKey;
import aethereal.model.AltAccount;
import aethereal.Expensive;
import aethereal.util.AltManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

public class ConfigManager {
    public final Path configDir;
    public final MacroStorage macrosConfig;

    public final FriendStore friendsConfig;

    public final LastConfigStore lastConfigIDConfig;

    public final WaypointStorage waysConfig;

    public final CredentialStore autoAuthConfig;

    public final SessionNicknameStore sessionNicknameConfig;

    public final StaffNamesStore staffConfig;

    public final LocalPreferencesFile menuStateConfig;

    public final AltManagerStore altManagerConfig;

    public final AltManager altManager;

    public ScheduledExecutorService executor;

    public ConfigManager(Path path, WaysRepository class675Var, MacroRepository class725Var) throws IOException {
        this.configDir = path;
        Files.createDirectories(path, new FileAttribute[0]);
        this.macrosConfig = new MacroStorage(path.resolve("macros.json"), class725Var);
        this.friendsConfig = new FriendStore(path.resolve("friends.json"));
        this.waysConfig = new WaypointStorage(path.resolve("ways.json"), class675Var);
        this.lastConfigIDConfig = new LastConfigStore(path.resolve("lastConfigID.json"));
        this.menuStateConfig = new LocalPreferencesFile(path.resolve("menu-state.json"));
        this.autoAuthConfig = new CredentialStore(path.resolve("auto-auth.json"));
        this.sessionNicknameConfig = new SessionNicknameStore(path.resolve("session-nickname.json"));
        this.staffConfig = new StaffNamesStore(path.resolve("staff.json"));
        this.altManagerConfig = new AltManagerStore(path.resolve("alts.json"));
        this.altManager = new AltManager(path.resolve("alts.json"));
    }

    public void saveMacros() throws IOException {
        this.macrosConfig.save();
    }

    public void loadMacros() throws IOException {
        this.macrosConfig.load();
    }

    public void saveLastConfigID() {
        this.lastConfigIDConfig.save();
    }

    public void loadLastConfigID() {
        this.lastConfigIDConfig.load();
    }

    public void saveWays() throws IOException {
        this.waysConfig.save();
    }

    public void loadWays() throws IOException {
        this.waysConfig.load();
    }

    public void saveFriends() throws IOException {
        this.friendsConfig.save();
    }

    public void loadFriends() throws IOException {
        this.friendsConfig.load();
    }

    public void saveMenuState() throws IOException {
        this.menuStateConfig.save();
    }

    public void loadMenuState() {
        this.menuStateConfig.load();
    }

    public void saveLocalConfig() {
        try {
            byte[] data = new ConfigSerializer().serialize(Expensive.INSTANCE.moduleRepository(), Expensive.INSTANCE.widgetStack());
            Files.write(this.configDir.resolve("local-config.json"), data);
        } catch (Exception e) {
            Expensive.LOGGER.error("Failed to save local config", e);
        }
    }

    public void loadLocalConfig() {
        try {
            Path path = this.configDir.resolve("local-config.json");
            if (!Files.exists(path)) {
                return;
            }
            ConfigSerializer serializer = new ConfigSerializer();
            ConfigFile bundle = serializer.deserialize(Files.readAllBytes(path));
            serializer.applyConfig(new LoadedConfig(null, bundle));
            Expensive.LOGGER.info("Loaded local config ({} modules)", bundle.modules() == null ? 0 : bundle.modules().size());
        } catch (Exception e) {
            Expensive.LOGGER.error("Failed to load local config", e);
        }
    }

    public void saveAutoAuth(Map<CredentialKey, String> map) throws IOException {
        this.autoAuthConfig.saveCredentials(map);
    }

    public void saveSessionNickname() {
        this.sessionNicknameConfig.saveCurrentSessionNickname();
    }

    public String loadSessionNickname() {
        return this.sessionNicknameConfig.loadSessionNickname();
    }

    public Map<CredentialKey, String> loadAutoAuth() throws IOException {
        return this.autoAuthConfig.loadCredentials();
    }

    public void saveStaff() throws IOException {
        this.staffConfig.save();
    }

    public void loadStaff() throws IOException {
        this.staffConfig.load();
    }

    public Path configDir() {
        return this.configDir;
    }

    public MacroStorage macrosConfig() {
        return this.macrosConfig;
    }

    public FriendStore friendsConfig() {
        return this.friendsConfig;
    }

    public LastConfigStore lastConfigIDConfig() {
        return this.lastConfigIDConfig;
    }

    public WaypointStorage waysConfig() {
        return this.waysConfig;
    }

    public CredentialStore autoAuthConfig() {
        return this.autoAuthConfig;
    }

    public SessionNicknameStore sessionNicknameConfig() {
        return this.sessionNicknameConfig;
    }

    public StaffNamesStore staffConfig() {
        return this.staffConfig;
    }

    public AltManagerStore altManagerConfig() {
        return this.altManagerConfig;
    }

    public AltManager altManager() {
        return this.altManager;
    }

    public void saveAlts() throws IOException {
        this.altManagerConfig.save(this.altManager.accounts());
    }

    public void loadAlts() {
        this.altManager.load();
    }

    public ScheduledExecutorService executor() {
        return this.executor;
    }

    public LocalPreferencesFile menuStateConfig() {
        return this.menuStateConfig;
    }
}
