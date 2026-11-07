package aethereal.util;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ScriptRegistry {
    public final Map<String, ActionScheduler> scripts = new ConcurrentHashMap();

    public Optional<ActionScheduler> getScript(String str) {
        return isBlank(str) ? Optional.empty() : Optional.of(this.scripts.computeIfAbsent(str, str2 -> {
            return new ActionScheduler();
        }));
    }

    public ActionScheduler addScript(String str, ActionScheduler class265Var) {
        if (isBlank(str) || class265Var == null) {
            throw new IllegalArgumentException("Script name or instance cannot be null or empty");
        }
        return this.scripts.put(str, class265Var);
    }

    public boolean containsScript(String str) {
        return !isBlank(str) && this.scripts.containsKey(str);
    }

    public boolean finished(String str) {
        return !isBlank(str) && getScript(str).isPresent() && getScript(str).get().isFinished();
    }

    public void removeScript(String str) {
        if (isBlank(str)) {
            return;
        }
        this.scripts.remove(str);
    }

    public void cleanupScript(String str) {
        if (isBlank(str)) {
            return;
        }
        this.scripts.computeIfPresent(str, (str2, class265Var) -> {
            class265Var.cleanup();
            return class265Var;
        });
    }

    public void cleanupAll() {
        this.scripts.forEach((str, class265Var) -> {
            class265Var.cleanup();
        });
    }

    public void clearAll() {
        this.scripts.clear();
    }

    public void updateScript(String str) {
        updateScript(str, () -> {
            return true;
        });
    }

    public void updateScript(String str, Supplier<Boolean> supplier) {
        if (!supplier.get().booleanValue() || isBlank(str)) {
            return;
        }
        this.scripts.computeIfPresent(str, (str2, class265Var) -> {
            class265Var.update();
            return class265Var;
        });
    }

    public void updateAll() {
        this.scripts.values().forEach((v0) -> {
            v0.update();
        });
    }

    public Set<String> getAllScriptNames() {
        return Collections.unmodifiableSet(this.scripts.keySet());
    }

    public Map<String, ActionScheduler> getAllScripts() {
        return Collections.unmodifiableMap(this.scripts);
    }

    public boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}
