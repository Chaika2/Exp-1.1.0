package aethereal;
import aethereal.util.ActivityLogger;
import aethereal.annotation.Aliases;
import aethereal.util.AshfieldChatHandler;
import aethereal.model.AuthStub;
import aethereal.render.AvatarCache;
import aethereal.render.BlurEffect;
import aethereal.render.ThemeManager;
import aethereal.command.BindCommand;
import aethereal.command.BoxCommand;
import aethereal.resource.ClasspathResource;
import aethereal.model.ClientInfo;
import aethereal.net.CloudConfigService;
import aethereal.util.CommandManager;
import aethereal.config.ConfigFile;
import aethereal.config.ConfigManager;
import aethereal.type.ConfigOrigin;
import aethereal.config.ConfigSerializer;
import aethereal.ui.ConfigTabLayout;
import aethereal.util.DrawEngine;
import aethereal.util.EventDispatcher;
import aethereal.command.FriendCommand;
import aethereal.render.GlTexture;
import aethereal.command.GpsCommand;
import aethereal.command.HClipCommand;
import aethereal.config.HandlerRepository;
import aethereal.command.HelpCommand;
import aethereal.net.InventoryService;
import aethereal.config.ListenerRepository;
import aethereal.command.MacroCommand;
import aethereal.config.MacroRepository;
import aethereal.ui.MenuWindow;
import aethereal.ui.ModuleCard;
import aethereal.util.ModuleProvider;
import aethereal.config.ModuleRepository;
import aethereal.ui.ModuleTab;
import aethereal.net.NetworkClient;
import aethereal.config.NotificationRepository;
import aethereal.util.PinnedServersController;
import aethereal.command.PrefixCommand;
import aethereal.command.ReconnectCommand;
import aethereal.util.ServerUtil;
import aethereal.command.StaffCommand;
import aethereal.util.TabsController;
import aethereal.render.Theme;
import aethereal.ui.ThemeCard;
import aethereal.ui.ThemeCard2;
import aethereal.model.ThemeData;
import aethereal.type.ThemeMode;
import aethereal.ui.ThemeTabLayout;
import aethereal.net.UserSession;
import aethereal.command.VClipCommand;
import aethereal.command.VClipGrimCommand;
import aethereal.command.WaypointCommand;
import aethereal.config.WaysRepository;
import aethereal.util.WebSocketInitializer;
import aethereal.ui.WidgetStack;
import aethereal.util.WindowController;
import aethereal.render.WindowControllerAdapter;

import discord.DiscordManager;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Expensive {
    public static final Logger LOGGER = LoggerFactory.getLogger(Expensive.class);
    public static Expensive INSTANCE;
    public final WidgetStack widgetStack = new WidgetStack();

    public final ScheduledExecutorService executor = Executors.newScheduledThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors() / 2), runnable -> {
        Thread thread = new Thread(runnable, "expensive-worker");
        thread.setDaemon(true);
        return thread;
    });

    public final WaysRepository waysRepository = new WaysRepository();

    public final CloudConfigService cloudConfigService = new CloudConfigService();

    public final WindowController windowController = new WindowController();

    public final BlurEffect hudBlur = new BlurEffect();

    public final WindowControllerAdapter windowControllerAdapter = new WindowControllerAdapter(this.windowController);

    public final TabsController tabsController = new TabsController();

    public final PinnedServersController pinnedServersController = new PinnedServersController();

    public final ActivityLogger activityLogger = new ActivityLogger();

    public final boolean devMode = detectDevMode();

    public ConfigManager configManager;

    public CommandManager commandDispatcher;

    public final MacroRepository macroRepository;

    public final DiscordManager discordManager;

    public final InventoryService inventoryService;

    public final HandlerRepository handlerRepository;

    public final EventDispatcher eventDispatcher;

    public final NotificationRepository notificationRepository;

    public final ModuleProvider moduleProvider;

    public final ModuleRepository moduleRepository;

    public final ListenerRepository listenerRepository;

    public ConfigFile defaultConfigFile;

    public final Lang languages;

    public DrawEngine drawEngine;

    public UserSession userSession;

    public final ClientInfo clientInfo;

    public MenuWindow menuWindow;

    public Theme theme;

    public AshfieldChatHandler ashfieldChatHandler;

    public static final String CLOUD_SECRET = "FYkDPz_LRLsoEm30Yy6k";

    public final WebSocketInitializer webSocketInitializer;

    public String token;

    public Expensive(UserSession class385Var) throws NoSuchAlgorithmException, InvalidKeyException {
        this.webSocketInitializer = new WebSocketInitializer(devMode() ? "dev" : AuthStub.hwid(), false);
        if (INSTANCE != null) {
            throw new IllegalStateException(getClass().getName() + " already initialized!");
        }
        INSTANCE = this;
        this.userSession = class385Var;
        this.activityLogger.start();
        this.languages = new Lang();
        this.languages.primaryLanguage();
        this.pinnedServersController.init();
        this.eventDispatcher = new EventDispatcher();
        this.macroRepository = new MacroRepository();
        this.notificationRepository = new NotificationRepository();
        this.listenerRepository = new ListenerRepository();
        this.moduleProvider = new ModuleProvider();
        this.moduleRepository = new ModuleRepository(this.moduleProvider.getAll());
        captureDefaultConfig();
        AvatarCache.load(class385Var.avatarUrl(), new GlTexture(new ClasspathResource("assets/expensive/textures/avatar.png")), this.executor).thenAccept(class073Var -> {
            this.userSession = class385Var.withTexture(class073Var);
        });
        this.handlerRepository = new HandlerRepository();
        this.inventoryService = new InventoryService();
        this.clientInfo = new ClientInfo("4.1.3", "Stable", "09.03.2026");
        this.discordManager = new DiscordManager();
        this.discordManager.init();
        String strJoin = String.join("|", class385Var.uid(), class385Var.hwid(), class385Var.username());
        String strMethod013 = computeHmac(strJoin, CLOUD_SECRET);
        LOGGER.info("[CLOUD]{} {}", strJoin, strMethod013);
        initConfigs();
        try {
            initCloud(strMethod013, class385Var.hwid());
        } catch (Exception e) {
            LOGGER.error("Failed to init cloud", e);
        }
        registerCommands();
        setupMenu();
        this.configManager.loadLocalConfig();
        this.executor.scheduleAtFixedRate(this.configManager::saveLocalConfig, 30L, 30L, TimeUnit.SECONDS);
        registerShutdownHook();
    }

    public void captureDefaultConfig() {
        try {
            ConfigSerializer class351Var = new ConfigSerializer();
            this.defaultConfigFile = class351Var.deserialize(class351Var.serialize(this.moduleRepository, this.widgetStack));
        } catch (IOException e) {
            LOGGER.error("Failed to capture default config", e);
        }
    }

    public void initCloud(String str, String str2) {
        String strAvatarUrl = this.userSession.avatarUrl();
        try {
            this.ashfieldChatHandler = new AshfieldChatHandler(URI.create("ws://127.0.0.1:1/ws"));
            this.ashfieldChatHandler.createUser(this.userSession.uid(), this.userSession.username(), strAvatarUrl, this.userSession.role());
            LOGGER.info("AshfieldChat initialized");
            this.cloudConfigService.initialize(String.valueOf(this.userSession.uid()), this.userSession.username(), str, str2, strAvatarUrl).thenCompose(r3 -> {
                return this.cloudConfigService.listConfigs();
            }).thenAccept(list -> {
                LOGGER.info("Available configs: {}", Integer.valueOf(list.size()));
                this.configManager.loadLastConfigID();
                this.cloudConfigService.activeConfig().ifPresent(class304Var -> {
                    this.executor.scheduleAtFixedRate(() -> {
                        this.cloudConfigService.saveConfig(class304Var.id(), this.moduleRepository, this.widgetStack).exceptionally(th -> {
                            LOGGER.error("Auto-save failed", th);
                            return null;
                        });
                    }, 2L, 2L, TimeUnit.MINUTES);
                });
            }).exceptionally(th -> {
                LOGGER.error("Cloud init failed", th);
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String urlEncode(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    public static String computeHmac(String str, String str2) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(str2.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] bArrDoFinal = mac.doFinal(str.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : bArrDoFinal) {
            sb.append(String.format("%02x", Byte.valueOf(b)));
        }
        return sb.toString();
    }

    public void registerCommands() {
        this.commandDispatcher = new CommandManager();
        this.commandDispatcher.registerCommand(new BindCommand());
        this.commandDispatcher.registerCommand(new MacroCommand());
        this.commandDispatcher.registerCommand(new ReconnectCommand());
        this.commandDispatcher.registerCommand(new HClipCommand());
        this.commandDispatcher.registerCommand(new GpsCommand());
        this.commandDispatcher.registerCommand(new WaypointCommand());
        this.commandDispatcher.registerCommand(new VClipCommand());
        this.commandDispatcher.registerCommand(new VClipGrimCommand());
        this.commandDispatcher.registerCommand(new FriendCommand());
        this.commandDispatcher.registerCommand(new StaffCommand());
        this.commandDispatcher.registerCommand(new HelpCommand(this.commandDispatcher));
        this.commandDispatcher.registerCommand(new PrefixCommand());
        this.commandDispatcher.registerCommand(new BoxCommand());
    }

    public void setupMenu() {
        this.menuWindow = new MenuWindow();
        this.windowController.newWindow(this.menuWindow);
        ThemeManager.INSTANCE.setTheme(ThemeManager.INSTANCE.active());
        Map.of(ModuleTab.COMBAT, this.tabsController.combat(), ModuleTab.MOVEMENT, this.tabsController.movement(), ModuleTab.PLAYER, this.tabsController.player(), ModuleTab.RENDER, this.tabsController.render(), ModuleTab.MISC, this.tabsController.misc()).forEach((class847Var, class732Var) -> {
            this.moduleRepository.getModules().stream().filter(class605Var -> {
                return class605Var.getModuleTab() == class847Var;
            }).forEach(class605Var2 -> {
                class732Var.newFrame(new ModuleCard(class605Var2, class605Var2.getClass().isAnnotationPresent(Aliases.class) ? ((Aliases) class605Var2.getClass().getAnnotation(Aliases.class)).aliases() : new String[0]));
            });
        });
        ConfigTabLayout class797Var = new ConfigTabLayout();
        this.tabsController.config().setRenderStrategy(class797Var);
        class797Var.updateConfigList();
        this.tabsController.theme().setRenderStrategy(new ThemeTabLayout());
        ArrayList arrayList = new ArrayList();
        for (Theme theme : ThemeManager.INSTANCE.themes()) {
            arrayList.add(new ThemeCard2(new ThemeCard(theme, this.userSession.texture())));
        }
        this.tabsController.theme().updateFramesSafe(arrayList);
        this.tabsController.theme().setActive(true);
    }

    public void initConfigs() {
        try {
            this.configManager = new ConfigManager(Path.of("expensive/config", new String[0]), this.waysRepository, this.macroRepository);
            this.configManager.loadMacros();
            this.configManager.loadFriends();
            this.configManager.loadWays();
            this.configManager.loadMenuState();
            this.configManager.loadStaff();
            this.configManager.loadAlts();
            this.configManager.altManager().lastType(this.configManager.menuStateConfig().loadLastType());
            ServerUtil.applySessionNickname(this.configManager.loadSessionNickname());
        } catch (Exception e) {
            Expensive.LOGGER.error("Failed to init configs", e);
        }
    }

    public void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            this.executor.shutdownNow();
            try {
                this.executor.awaitTermination(5L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            this.discordManager.stopRPC();
            try {
                this.configManager.saveMacros();
                this.configManager.saveFriends();
                this.configManager.saveWays();
                this.configManager.saveMenuState();
                this.configManager.saveLastConfigID();
                NetworkClient.shutdown();
                this.configManager.saveSessionNickname();
                this.configManager.saveStaff();
                this.configManager.saveAlts();
                this.configManager.saveLocalConfig();
            } catch (Exception e2) {
                LOGGER.error("Failed to save on shutdown", e2);
            }
        }));
    }

    public boolean devMode() {
        return this.devMode;
    }

    public static boolean detectDevMode() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments().stream().anyMatch(str -> {
            return str.contains("-Ddebugger.agent.enable.coroutines=true");
        });
    }

    public WidgetStack widgetStack() {
        return this.widgetStack;
    }

    public ScheduledExecutorService executor() {
        return this.executor;
    }

    public WaysRepository waysRepository() {
        return this.waysRepository;
    }

    public CloudConfigService cloudConfigService() {
        return this.cloudConfigService;
    }

    public WindowController windowController() {
        return this.windowController;
    }

    public BlurEffect hudBlur() {
        return this.hudBlur;
    }

    public WindowControllerAdapter windowControllerAdapter() {
        return this.windowControllerAdapter;
    }

    public TabsController tabsController() {
        return this.tabsController;
    }

    public PinnedServersController pinnedServersController() {
        return this.pinnedServersController;
    }

    public ActivityLogger activityLogger() {
        return this.activityLogger;
    }

    public ConfigManager configManager() {
        return this.configManager;
    }

    public CommandManager commandDispatcher() {
        return this.commandDispatcher;
    }

    public MacroRepository macroRepository() {
        return this.macroRepository;
    }

    public DiscordManager discordManager() {
        return this.discordManager;
    }

    public InventoryService inventoryService() {
        return this.inventoryService;
    }

    public HandlerRepository handlerRepository() {
        return this.handlerRepository;
    }

    public EventDispatcher eventDispatcher() {
        return this.eventDispatcher;
    }

    public NotificationRepository notificationRepository() {
        return this.notificationRepository;
    }

    public ModuleProvider moduleProvider() {
        return this.moduleProvider;
    }

    public ModuleRepository moduleRepository() {
        return this.moduleRepository;
    }

    public ListenerRepository listenerRepository() {
        return this.listenerRepository;
    }

    public ConfigFile defaultConfigFile() {
        return this.defaultConfigFile;
    }

    public Lang languages() {
        return this.languages;
    }

    public DrawEngine drawEngine() {
        return this.drawEngine;
    }

    public UserSession userSession() {
        return this.userSession;
    }

    public ClientInfo clientInfo() {
        return this.clientInfo;
    }

    public MenuWindow menuWindow() {
        return this.menuWindow;
    }

    public Theme theme() {
        return this.theme;
    }

    public AshfieldChatHandler ashfieldChatHandler() {
        return this.ashfieldChatHandler;
    }

    public WebSocketInitializer webSocketInitializer() {
        return this.webSocketInitializer;
    }

    public String token() {
        return this.token;
    }

    public Expensive drawEngine(DrawEngine class154Var) {
        this.drawEngine = class154Var;
        return this;
    }

    public Expensive token(String str) {
        this.token = str;
        return this;
    }
}
