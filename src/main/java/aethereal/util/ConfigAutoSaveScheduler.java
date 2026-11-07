package aethereal.util;
import aethereal.net.CloudConfigService;
import aethereal.Expensive;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class ConfigAutoSaveScheduler {
    public static final long autoSaveDelayMs = 1200;
    public static final Object lock = new Object();
    public static ScheduledFuture<?> pendingSave;

    public ConfigAutoSaveScheduler() {
    }

    public static void scheduleAutoSave() {
        CloudConfigService class357VarCloudConfigService = Expensive.INSTANCE.cloudConfigService();
        class357VarCloudConfigService.activeConfig().ifPresent(class304Var -> {
            if (Expensive.INSTANCE.configManager().menuStateConfig().isAutoSaveDisabled(class304Var.id())) {
                return;
            }
            synchronized (lock) {
                if (pendingSave != null && !pendingSave.isDone()) {
                    pendingSave.cancel(false);
                }
                pendingSave = Expensive.INSTANCE.executor().schedule(() -> {
                    class357VarCloudConfigService.saveConfig(class304Var.id(), Expensive.INSTANCE.moduleRepository(), Expensive.INSTANCE.widgetStack()).thenRun(() -> {
                        Expensive.LOGGER.info("Config auto-saved: {}", class304Var.id());
                    }).exceptionally(th -> {
                        Expensive.LOGGER.error("Failed to auto-save config", th);
                        return null;
                    });
                }, autoSaveDelayMs, TimeUnit.MILLISECONDS);
            }
        });
    }
}
