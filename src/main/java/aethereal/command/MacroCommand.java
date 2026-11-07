package aethereal.command;
import aethereal.util.ChatUtil;
import aethereal.model.CommandContext;
import aethereal.Expensive;
import aethereal.type.KeyArgumentType;
import aethereal.util.KeyboardUtil;
import aethereal.Lang;
import aethereal.model.MacroEntry;
import aethereal.config.MacroRepository;
import aethereal.type.Mc;
import aethereal.model.TranslatedException;
import aethereal.model.Translation;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class MacroCommand implements ClientCommand {
    public final KeyArgumentType keyArgument = new KeyArgumentType();

    @Override
    public String getName() {
        return "macro";
    }

    @Override
    public Translation getDescription() {
        return Lang.COMMAND_MACRO_DESC;
    }

    @Override
    public String getUsage() {
        return ".macro <add | remove | list | clear> [arguments]";
    }

    @Override
    public List<String> getAliases() {
        return null;
    }

    @Override
    public void execute(CommandContext class392Var) {
        String[] strArrArgs = class392Var.args();
        if (!Mc.INSTANCE.isWorldLoaded()) {
            System.err.println("ser vi che dalbaeb");
            return;
        }
        if (strArrArgs.length == 0) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_SUBCOMMAND_REQUIRED.effective().replace("{usage}", getUsage())));
        }
        MacroRepository class725VarMacroRepository = Expensive.INSTANCE.macroRepository();
        String lowerCase = strArrArgs[0].toLowerCase();
        String[] strArr = (String[]) Arrays.copyOfRange(strArrArgs, 1, strArrArgs.length);
        switch (lowerCase) {
            case "add":
                handleAdd(class725VarMacroRepository, strArr);
                return;
            case "remove":
                handleRemove(class725VarMacroRepository, strArr);
                return;
            case "list":
                handleList();
                return;
            case "clear":
                handleClear();
                return;
            default:
                throw new TranslatedException(Translation.clearText(Lang.COMMAND_UNKNOWN_SUBCOMMAND.effective().replace("{sub}", lowerCase)));
        }
    }

    public void handleAdd(MacroRepository class725Var, String[] strArr) throws TranslatedException {
        if (strArr.length < 2) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_NOT_ENOUGH_ARGS.effective()));
        }
        int iIntValue = this.keyArgument.parse(strArr[0]).intValue();
        String str = strArr[1];
        String strCollectMessage = collectMessage(2, strArr);
        if (strCollectMessage.isEmpty()) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_MACRO_EMPTY_MESSAGE.effective()));
        }
        if (class725Var.hasMacro(str)) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_MACRO_EXISTS.effective()));
        }
        class725Var.addMacro(str, strCollectMessage, iIntValue);
        ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_MACRO_ADDED.effective().replace("{name}", str).replace("{key}", KeyboardUtil.keyToString(iIntValue)).replace("{command}", strCollectMessage)).formatted(Formatting.GRAY));
    }

    public void handleRemove(MacroRepository class725Var, String[] strArr) {
        if (strArr.length < 1) {
            throw new TranslatedException(Lang.COMMAND_MACRO_NO_NAME);
        }
        MacroEntry class724VarOrElseThrow = Expensive.INSTANCE.macroRepository().getMacroList().stream().filter(class724Var -> {
            return class724Var.name().replaceAll("\\s", "").equalsIgnoreCase(strArr[0]);
        }).findFirst().orElseThrow(() -> {
            return new TranslatedException(Translation.clearText(Lang.COMMAND_MACRO_NOT_FOUND.effective().replace("{name}", strArr[0])));
        });
        ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_MACRO_REMOVED.effective().replace("{name}", class724VarOrElseThrow.name())).formatted(Formatting.GRAY));
        class725Var.deleteMacro(class724VarOrElseThrow.name());
    }

    public void handleList() {
        List<MacroEntry> macroList = Expensive.INSTANCE.macroRepository().getMacroList();
        if (macroList.isEmpty()) {
            ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_MACROS_NOT_FOUND.effective()).formatted(Formatting.GRAY));
            return;
        }
        ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_MACROS_LIST.effective()).formatted(Formatting.GRAY));
        for (MacroEntry class724Var : macroList) {
            String strReplaceAll = class724Var.name().replaceAll("\\s", "");
            ChatUtil.addChatMessage((Text) Text.literal("").formatted(Formatting.GRAY).append(Text.literal(strReplaceAll + String.valueOf(Formatting.GRAY) + Lang.COMMAND_MACRO_REMOVE_HINT.effective()).setStyle(Text.literal(strReplaceAll + String.valueOf(Formatting.GRAY) + " (Нажмите, чтобы удалить)").getStyle().withColor(Formatting.WHITE).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("").append(Text.literal(Lang.COMMAND_MACRO_HOVER_NAME.effective()).formatted(Formatting.GRAY)).append(Text.literal(strReplaceAll + "\n").formatted(Formatting.WHITE)).append(Text.literal(Lang.COMMAND_MACRO_HOVER_KEY.effective()).formatted(Formatting.GRAY)).append(Text.literal(KeyboardUtil.keyToString(class724Var.key()).toUpperCase() + "\n").formatted(Formatting.WHITE)).append(Text.literal(Lang.COMMAND_MACRO_HOVER_COMMAND.effective()).formatted(Formatting.GRAY)).append(Text.literal(class724Var.content()).formatted(Formatting.WHITE)))).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ".macro remove " + strReplaceAll)))));
        }
    }

    public void handleClear() {
        Expensive.INSTANCE.macroRepository().getMacroList().clear();
        ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_BINDS_CLEARED.effective()).formatted(Formatting.GRAY));
    }

    @Override
    public List<String> getSuggestions(String[] strArr, int i) {
        if (i == 0) {
            return Stream.of(new String[]{"add", "remove", "list", "clear"}).filter(str -> {
                return str.startsWith(strArr[0].toLowerCase());
            }).toList();
        }
        String lowerCase = strArr[0].toLowerCase();
        String str2 = i < strArr.length ? strArr[i] : "";
        switch (i) {
            case 1:
                if (lowerCase.equals("add")) {
                    return this.keyArgument.getSuggestions(str2);
                }
                if (lowerCase.equals("remove")) {
                    String lowerCase2 = str2.toLowerCase();
                    return (List) Expensive.INSTANCE.macroRepository().getMacroList().stream().map(class724Var -> {
                        return class724Var.name().replaceAll("\\s", "");
                    }).filter(str3 -> {
                        return str3.replaceAll("\\s", "").toLowerCase().startsWith(lowerCase2);
                    }).collect(Collectors.toList());
                }
                break;
            case 2:
                if (lowerCase.equals("add")) {
                    return List.of("<name>");
                }
                break;
            case 3:
                if (lowerCase.equals("add")) {
                    return List.of("<command>");
                }
                break;
        }
        return List.of();
    }

    public String collectMessage(int i, String[] strArr) {
        return ((String) IntStream.range(i, strArr.length).mapToObj(i2 -> {
            return asString(i2, strArr).orElse("");
        }).collect(Collectors.joining(" "))).trim();
    }

    public Optional<String> asString(int i, String[] strArr) {
        return Optional.ofNullable((String) parseArgument(i, (v0) -> {
            return String.valueOf(v0);
        }, strArr));
    }

    public <T> T parseArgument(int i, Function<String, T> function, String[] strArr) {
        if (i >= strArr.length) {
            return null;
        }
        try {
            return function.apply(strArr[i]);
        } catch (Exception e) {
            return null;
        }
    }
}
