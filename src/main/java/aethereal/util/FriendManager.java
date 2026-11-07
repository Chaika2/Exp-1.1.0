package aethereal.util;
import aethereal.model.ConfigErrorNotice;
import aethereal.Expensive;
import aethereal.model.FriendEntry;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FriendManager {
    public static final Set<FriendEntry> friends = new HashSet();
    public static final Logger logger = LoggerFactory.getLogger(FriendManager.class);

    public static boolean isFriend(String str) {
        return friends.stream().anyMatch(class722Var -> {
            return class722Var.name().equals(str);
        });
    }

    public static void add(String str) {
        friends.add(new FriendEntry(str));
        try {
            Expensive.INSTANCE.configManager().saveFriends();
        } catch (IOException e) {
            logger.error("Failed to persist friends after add(name={})", str, e);
            ChatUtil.addChatMessage(ConfigErrorNotice.withDiscord("сохранении списка друзей"));
        }
    }

    public static void remove(String str) {
        if (friends.stream().anyMatch(class722Var -> {
            return class722Var.name().equals(str);
        })) {
            friends.remove(new FriendEntry(str));
            try {
                Expensive.INSTANCE.configManager().saveFriends();
            } catch (IOException e) {
                logger.error("Failed to persist friends after remove(name={})", str, e);
                ChatUtil.addChatMessage(ConfigErrorNotice.withDiscord("удалении друга"));
            }
        }
    }

    public static void clear() {
        friends.clear();
    }

    public static Set<String> getFriends() {
        HashSet hashSet = new HashSet();
        Iterator<FriendEntry> it = friends.iterator();
        while (it.hasNext()) {
            hashSet.add(it.next().name());
        }
        return hashSet;
    }
}
