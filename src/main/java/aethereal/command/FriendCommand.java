package aethereal.command;
import aethereal.util.ChatUtil;
import aethereal.model.CommandContext;
import aethereal.model.ConfigErrorNotice;
import aethereal.Expensive;
import aethereal.util.FriendManager;
import aethereal.Lang;
import aethereal.model.TranslatedException;
import aethereal.model.Translation;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FriendCommand implements ClientCommand {
    public static final Logger logger = LoggerFactory.getLogger(FriendCommand.class);

    @Override
    public String getName() {
        return "friend";
    }

    @Override
    public Translation getDescription() {
        return Lang.COMMAND_FRIEND_DESC;
    }

    @Override
    public String getUsage() {
        return ".friend <add | remove | list | clear> [name]";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    @Override
    public void execute(CommandContext class392Var) throws TranslatedException {
        String[] strArrArgs = class392Var.args();
        if (strArrArgs.length == 0) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_SUBCOMMAND_REQUIRED.effective().replace("{usage}", getUsage())));
        }
        String lowerCase = strArrArgs[0].toLowerCase();
        String[] strArr = (String[]) Arrays.copyOfRange(strArrArgs, 1, strArrArgs.length);
        switch (lowerCase) {
            case "add":
                addFriend(strArr);
                return;
            case "remove":
                removeFriend(strArr);
                return;
            case "list":
                listFriends();
                return;
            case "clear":
                clearFriends();
                return;
            default:
                throw new TranslatedException(Translation.clearText(Lang.COMMAND_UNKNOWN_SUBCOMMAND.effective().replace("{sub}", lowerCase)));
        }
    }

    public void addFriend(String[] strArr) throws TranslatedException {
        if (strArr.length < 1) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_NOT_ENOUGH_ARGS.effective()));
        }
        String str = strArr[0];
        if (FriendManager.isFriend(str)) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_FRIEND_ALREADY.effective().replace("{name}", str)));
        }
        FriendManager.add(str);
        ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_FRIEND_ADDED.effective().replace("{name}", str)).formatted(Formatting.GRAY));
    }

    public void removeFriend(String[] strArr) throws TranslatedException {
        if (strArr.length < 1) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_NOT_ENOUGH_ARGS.effective()));
        }
        String str = strArr[0];
        if (!FriendManager.isFriend(str)) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_FRIEND_NOT_FOUND.effective().replace("{name}", str)));
        }
        FriendManager.remove(str);
        ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_FRIEND_REMOVED.effective().replace("{name}", str)).formatted(Formatting.GRAY));
    }

    public void listFriends() {
        Set<String> friends = FriendManager.getFriends();
        if (friends.isEmpty()) {
            ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_FRIENDS_NOT_FOUND.effective()).formatted(Formatting.GRAY));
        } else {
            ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_FRIENDS_LIST.effective().replace("{list}", (String) friends.stream().sorted().collect(Collectors.joining(", ")))).formatted(Formatting.GRAY));
        }
    }

    public void clearFriends() {
        FriendManager.clear();
        try {
            Expensive.INSTANCE.configManager().saveFriends();
            ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_FRIENDS_CLEARED.effective()).formatted(Formatting.GRAY));
        } catch (IOException e) {
            logger.error("Failed to persist friends after clear", e);
            ChatUtil.addChatMessage(ConfigErrorNotice.withDiscord("сохранении списка друзей"));
        }
    }

    @Override
    public List<String> getSuggestions(String[] strArr, int i) {
        if (i == 0) {
            return Stream.of(new String[]{"add", "remove", "list", "clear"}).filter(str -> {
                return str.startsWith(strArr[0].toLowerCase());
            }).toList();
        }
        if (i == 1) {
            String lowerCase = strArr[0].toLowerCase();
            String str2 = i < strArr.length ? strArr[i] : "";
            if (lowerCase.equals("remove")) {
                String lowerCase2 = str2.toLowerCase();
                return (List) FriendManager.getFriends().stream().filter(str3 -> {
                    return str3.toLowerCase().startsWith(lowerCase2);
                }).sorted().collect(Collectors.toList());
            }
            if (lowerCase.equals("add")) {
                String lowerCase3 = str2.toLowerCase();
                MinecraftClient minecraftClient = MinecraftClient.getInstance();
                if (minecraftClient.player == null || minecraftClient.getNetworkHandler() == null) {
                    return List.of();
                }
                Set<String> friends = FriendManager.getFriends();
                return minecraftClient.getNetworkHandler().getPlayerList().stream().map(playerListEntry -> {
                    return playerListEntry.getProfile().getName();
                }).filter(str4 -> {
                    return str4.toLowerCase().startsWith(lowerCase3);
                }).filter(str5 -> {
                    return !friends.contains(str5);
                }).sorted().toList();
            }
        }
        return List.of();
    }
}
