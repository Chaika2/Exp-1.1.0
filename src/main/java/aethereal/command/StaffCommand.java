package aethereal.command;
import aethereal.util.ChatUtil;
import aethereal.model.CommandContext;
import aethereal.Expensive;
import aethereal.util.FriendManager;
import aethereal.util.StaffDetector;
import aethereal.util.StaffPacketDebug;
import aethereal.util.ServerProbe;
import aethereal.util.StaffMessageProbe;
import aethereal.model.TranslatedException;
import aethereal.model.Translation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class StaffCommand implements ClientCommand {
    @Override
    public String getName() {
        return "staff";
    }

    @Override
    public Translation getDescription() {
        return Translation.clearText("Управление собственным staff-листом");
    }

    @Override
    public String getUsage() {
        return ".staff <add | remove | list | clear | packets | probe> [name]";
    }

    @Override
    public List<String> getAliases() {
        return List.of("stafflist");
    }

    @Override
    public void execute(CommandContext class392Var) throws TranslatedException {
        String[] strArrArgs = class392Var.args();
        if (strArrArgs.length == 0) {
            throw new TranslatedException(Translation.clearText("Укажи подкоманду. Использование: " + getUsage()));
        }
        String lowerCase = strArrArgs[0].toLowerCase();
        String[] strArr = (String[]) Arrays.copyOfRange(strArrArgs, 1, strArrArgs.length);
        switch (lowerCase) {
            case "add":
                addStaff(strArr);
                return;
            case "remove":
                removeStaff(strArr);
                return;
            case "list":
                listStaff();
                return;
            case "clear":
                clearStaff();
                return;
            case "packets":
                togglePackets(strArr);
                return;
            case "probe":
                probe();
                return;
            case "check":
                check(strArr);
                return;
            default:
                throw new TranslatedException(Translation.clearText("Неизвестная подкоманда: " + lowerCase));
        }
    }

    public void addStaff(String[] strArr) throws TranslatedException {
        if (strArr.length < 1) {
            throw new TranslatedException(Translation.clearText("Укажи ник для добавления"));
        }
        String str = strArr[0];
        if (!StaffDetector.addCustomStaffName(str)) {
            throw new TranslatedException(Translation.clearText("Этот ник уже есть в staff-листе: " + str));
        }
        ChatUtil.addChatMessage((Text) Text.literal("Добавлен в staff-лист: " + str).formatted(Formatting.GRAY));
        saveStaff();
    }

    public void removeStaff(String[] strArr) throws TranslatedException {
        if (strArr.length < 1) {
            throw new TranslatedException(Translation.clearText("Укажи ник для удаления"));
        }
        String str = strArr[0];
        if (!StaffDetector.removeCustomStaffName(str)) {
            throw new TranslatedException(Translation.clearText("Ник не найден в staff-листе: " + str));
        }
        ChatUtil.addChatMessage((Text) Text.literal("Удален из staff-листа: " + str).formatted(Formatting.GRAY));
        saveStaff();
    }

    public void listStaff() {
        Set<String> customStaffNames = StaffDetector.getCustomStaffNames();
        if (customStaffNames.isEmpty()) {
            ChatUtil.addChatMessage((Text) Text.literal("Пользовательский staff-лист пуст").formatted(Formatting.GRAY));
        } else {
            ChatUtil.addChatMessage((Text) Text.literal("Пользовательский staff-лист: " + ((String) customStaffNames.stream().sorted().collect(Collectors.joining(", ")))).formatted(Formatting.GRAY));
        }
    }

    public void clearStaff() {
        StaffDetector.clearCustomStaffNames();
        ChatUtil.addChatMessage((Text) Text.literal("Пользовательский staff-лист очищен").formatted(Formatting.GRAY));
        saveStaff();
    }

    public void togglePackets(String[] strArr) {
        if (strArr.length > 0) {
            String mode = strArr[0].toLowerCase();
            if (mode.equals("on") || mode.equals("true") || mode.equals("1")) {
                StaffPacketDebug.setEnabled(true);
            } else if (mode.equals("off") || mode.equals("false") || mode.equals("0")) {
                StaffPacketDebug.setEnabled(false);
            } else {
                throw new TranslatedException(Translation.clearText("Использование: .staff packets [on|off]"));
            }
        } else {
            StaffPacketDebug.toggle();
        }
        if (StaffPacketDebug.isEnabled()) {
            ChatUtil.addChatMessage((Text) Text.literal("Лог пакетов ВКЛЮЧЕН -> " + StaffPacketDebug.getLogPath().toAbsolutePath()).formatted(Formatting.GREEN));
            ChatUtil.addChatMessage((Text) Text.literal("Заходи на сервер, жди заход админа, потом .staff packets off").formatted(Formatting.GRAY));
        } else {
            ChatUtil.addChatMessage((Text) Text.literal("Лог пакетов ВЫКЛЮЧЕН. Файл: " + StaffPacketDebug.getLogPath().toAbsolutePath()).formatted(Formatting.GRAY));
        }
    }

    public void probe() {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        ServerInfo serverInfo = minecraftClient.getCurrentServerEntry();
        if (serverInfo == null || serverInfo.address == null || serverInfo.address.isBlank()) {
            ChatUtil.addChatMessage((Text) Text.literal("Не удалось определить адрес сервера").formatted(Formatting.RED));
            return;
        }
        String address = serverInfo.address;
        ChatUtil.addChatMessage((Text) Text.literal("Опрос сервера " + address + " ...").formatted(Formatting.GRAY));
        Thread thread = new Thread(() -> {
            ServerProbe.Result result = ServerProbe.query(address);
            minecraftClient.execute(() -> reportProbe(result));
        }, "staff-probe");
        thread.setDaemon(true);
        thread.start();
    }

    public void reportProbe(ServerProbe.Result result) {
        if (!result.ok) {
            ChatUtil.addChatMessage((Text) Text.literal("Опрос не удался: " + result.error).formatted(Formatting.RED));
            return;
        }
        Set<String> tab = currentTabNames();
        Set<String> custom = StaffDetector.getCustomStaffNames();
        int hidden = result.online - tab.size();
        ChatUtil.addChatMessage((Text) Text.literal("Online=" + result.online + "/" + result.max
                + " в табе=" + tab.size()
                + " скрытых(возможно ваниш)=" + hidden).formatted(hidden > 0 ? Formatting.GOLD : Formatting.GREEN));
        ChatUtil.addChatMessage((Text) Text.literal("sample=" + result.samples.size()).formatted(Formatting.GRAY));
        for (ServerProbe.Sample sample : result.samples) {
            boolean staff = isKnownStaff(sample.name, custom);
            boolean inTab = tab.contains(sample.name.toLowerCase(Locale.ROOT));
            String tag = (staff ? " [STAFF]" : "") + (inTab ? " [в табе]" : " [НЕ в табе]");
            ChatUtil.addChatMessage((Text) Text.literal("  " + sample.name + tag).formatted(staff ? Formatting.GOLD : Formatting.GRAY));
        }
    }

    public void check(String[] strArr) {
        if (strArr.length < 1) {
            throw new TranslatedException(Translation.clearText("Укажи ник: .staff check <ник>"));
        }
        String nick = strArr[0];
        Set<String> tab = currentTabNames();
        if (tab.contains(nick.toLowerCase(Locale.ROOT))) {
            ChatUtil.addChatMessage((Text) Text.literal("[" + nick + "]: ОНЛАЙН и виден в табе (не ваниш)").formatted(Formatting.GREEN));
            return;
        }
        if (StaffDetector.spectatorNames.contains(nick.toLowerCase(Locale.ROOT))) {
            ChatUtil.addChatMessage((Text) Text.literal("[" + nick + "]: В ВАНИШЕ (видели переход в спек в этой сессии)").formatted(Formatting.GOLD));
        } else {
            ChatUtil.addChatMessage((Text) Text.literal("[" + nick + "]: в табе нет, переход в спек в этой сессии не замечен").formatted(Formatting.GRAY));
        }
        StaffMessageProbe.request(nick);
    }

    private static boolean isKnownStaff(String str, Set<String> set) {
        for (String str2 : set) {
            if (str2.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return StaffDetector.isKnownStaffName(str);
    }

    private static Set<String> currentTabNames() {
        Set<String> hashSet = new HashSet<>();
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        if (minecraftClient.getNetworkHandler() != null) {
            for (PlayerListEntry playerListEntry : minecraftClient.getNetworkHandler().getListedPlayerListEntries()) {
                if (playerListEntry.getProfile() != null && playerListEntry.getProfile().getName() != null) {
                    hashSet.add(playerListEntry.getProfile().getName().toLowerCase(Locale.ROOT));
                }
            }
        }
        return hashSet;
    }

    public void saveStaff() {
        try {
            Expensive.INSTANCE.configManager().saveStaff();
        } catch (Exception e) {
            Expensive.LOGGER.error("Failed to save staff list", e);
        }
    }

    @Override
    public List<String> getSuggestions(String[] strArr, int i) {
        if (i == 0) {
            return Stream.of(new String[]{"add", "remove", "list", "clear", "packets", "probe", "check"}).filter(str -> {
                return str.startsWith(strArr[0].toLowerCase());
            }).toList();
        }
        if (i == 1) {
            String lowerCase = strArr[0].toLowerCase();
            String str2 = i < strArr.length ? strArr[i] : "";
            if (lowerCase.equals("packets")) {
                return Stream.of(new String[]{"on", "off"}).filter(str -> {
                    return str.startsWith(str2.toLowerCase());
                }).toList();
            }
            if (lowerCase.equals("check")) {
                String lowerCase4 = str2.toLowerCase();
                MinecraftClient minecraftClient2 = MinecraftClient.getInstance();
                if (minecraftClient2.getNetworkHandler() == null) {
                    return List.of();
                }
                return minecraftClient2.getNetworkHandler().getPlayerList().stream().map(playerListEntry2 -> {
                    return playerListEntry2.getProfile().getName();
                }).filter(str6 -> {
                    return str6.toLowerCase().startsWith(lowerCase4);
                }).sorted().toList();
            }
            if (lowerCase.equals("remove")) {
                String lowerCase2 = str2.toLowerCase();
                return StaffDetector.getCustomStaffNames().stream().filter(str3 -> {
                    return str3.toLowerCase().startsWith(lowerCase2);
                }).sorted().toList();
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
