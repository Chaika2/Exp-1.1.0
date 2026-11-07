package aethereal.config;
import aethereal.util.ChatUtil;
import aethereal.model.ConfigErrorNotice;
import aethereal.Expensive;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaysRepository {
    public final Set<Waypoint> waypoints = new HashSet();
    static final Logger logger = LoggerFactory.getLogger(WaysRepository.class);

    public void add(String str, Vector3i vector3i) {
        this.waypoints.add(new Waypoint(str, vector3i));
        try {
            Expensive.INSTANCE.configManager().saveWays();
        } catch (IOException e) {
            logger.error("Failed to persist ways after add(name={})", str, e);
            ChatUtil.addChatMessage(ConfigErrorNotice.withDiscord("сохранении точки"));
        }
    }

    public boolean remove(String str) {
        if (!this.waypoints.stream().anyMatch(class674Var -> {
            return class674Var.name().equals(str);
        })) {
            return false;
        }
        this.waypoints.removeIf(class674Var2 -> {
            return class674Var2.name().equalsIgnoreCase(str);
        });
        try {
            Expensive.INSTANCE.configManager().saveWays();
            return true;
        } catch (IOException e) {
            logger.error("Failed to persist ways after remove(name={})", str, e);
            ChatUtil.addChatMessage(ConfigErrorNotice.withDiscord("удалении точки"));
            return true;
        }
    }

    public boolean isEmpty() {
        return this.waypoints.isEmpty();
    }

    public boolean hasWaypoint(String str) {
        Iterator<Waypoint> it = this.waypoints.iterator();
        while (it.hasNext()) {
            if (it.next().name().equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    public void clearList() {
        if (this.waypoints.isEmpty()) {
            return;
        }
        this.waypoints.clear();
        try {
            Expensive.INSTANCE.configManager().saveWays();
        } catch (IOException e) {
            logger.error("Failed to persist ways after clear()", e);
            ChatUtil.addChatMessage(ConfigErrorNotice.withDiscord("очистке путей"));
        }
    }

    public Set<Waypoint> getWaypoints() {
        return this.waypoints;
    }
}
