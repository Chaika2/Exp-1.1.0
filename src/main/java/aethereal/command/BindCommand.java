package aethereal.command;
import aethereal.util.ArgumentTypeFactory;
import aethereal.type.BindMode;
import aethereal.util.ChatUtil;
import aethereal.model.CommandContext;
import aethereal.type.EnumArgumentType;
import aethereal.Expensive;
import aethereal.type.KeyArgumentType;
import aethereal.util.KeyboardUtil;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.module.Module;
import aethereal.util.ModuleArgumentParser;
import aethereal.model.TranslatedException;
import aethereal.model.Translation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BindCommand implements ClientCommand {
    public final ModuleArgumentParser moduleParser = new ModuleArgumentParser();

    public final KeyArgumentType keyParser = new KeyArgumentType();

    public final EnumArgumentType<BindMode> bindModeParser = ArgumentTypeFactory.enumType(BindMode.class);

    @Override
    public String getName() {
        return "bind";
    }

    @Override
    public Translation getDescription() {
        return Lang.COMMAND_BIND_DESC;
    }

    @Override
    public String getUsage() {
        return ".bind <add | remove | list | clear> [arguments]";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public void execute(CommandContext class392Var) throws TranslatedException {
        String[] strArrArgs = class392Var.args();
        if (!Mc.INSTANCE.isWorldLoaded()) {
            System.err.println("ser vi che dalbaeb");
            return;
        }
        if (strArrArgs.length == 0) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_SUBCOMMAND_REQUIRED.effective().replace("{usage}", getUsage())));
        }
        String lowerCase = strArrArgs[0].toLowerCase();
        String[] strArr = (String[]) Arrays.copyOfRange(strArrArgs, 1, strArrArgs.length);
        switch (lowerCase) {
            case "add":
                addBind(class392Var, strArr);
                return;
            case "remove":
                removeBind(class392Var, strArr);
                return;
            case "list":
                listBinds();
                return;
            case "clear":
                clearBinds();
                return;
            default:
                throw new TranslatedException(Translation.clearText(Lang.COMMAND_UNKNOWN_SUBCOMMAND.effective().replace("{sub}", lowerCase)));
        }
    }

    public void removeBind(CommandContext class392Var, String[] strArr) throws TranslatedException {
        if (strArr.length < 1) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_NO_MODULE.effective()));
        }
        Module class605Var = (Module) class392Var.getArgument(1, this.moduleParser);
        class605Var.setBind(List.of(), BindMode.TOGGLE);
        ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_BIND_REMOVED.effective().replace("{module}", String.valueOf(Formatting.RED) + class605Var.getName() + String.valueOf(Formatting.GRAY))).formatted(Formatting.GRAY));
    }

    public void listBinds() {
        List<Module> list = Expensive.INSTANCE.moduleRepository().getModules().stream().filter((v0) -> {
            return v0.hasKeyBind();
        }).toList();
        if (list.isEmpty()) {
            ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_BINDS_NOT_FOUND.effective()).formatted(Formatting.GRAY));
        } else {
            ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_BINDS_LIST.effective()).formatted(Formatting.GRAY));
            list.forEach(class605Var -> {
                ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_BIND_INFO.effective().replace("{name}", String.valueOf(Formatting.RED) + class605Var.getName().replaceAll("\\s", "") + String.valueOf(Formatting.GRAY)).replace("{key}", String.valueOf(Formatting.RED) + KeyboardUtil.keysToString(class605Var.getKeyBind()).toUpperCase() + String.valueOf(Formatting.GRAY)).replace("{type}", String.valueOf(Formatting.RED) + class605Var.getType().name() + String.valueOf(Formatting.GRAY))).formatted(Formatting.GRAY));
            });
        }
    }

    public void clearBinds() {
        Expensive.INSTANCE.moduleRepository().getModules().forEach(class605Var -> {
            class605Var.setBind(List.of(), BindMode.TOGGLE);
        });
        ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_BINDS_CLEARED.effective()).formatted(Formatting.GRAY));
    }

    public void addBind(CommandContext class392Var, String[] strArr) throws TranslatedException {
        if (strArr.length < 2) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_NOT_ENOUGH_ARGS.effective()));
        }
        Module class605Var = (Module) class392Var.getArgument(1, this.moduleParser);
        int iIntValue = ((Integer) class392Var.getArgument(2, this.keyParser)).intValue();
        BindMode class660Var = BindMode.TOGGLE;
        if (strArr.length >= 3) {
            class660Var = (BindMode) class392Var.getArgument(3, this.bindModeParser);
        }
        class605Var.setBind(List.of(Integer.valueOf(iIntValue)), class660Var);
        ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_BIND_ADDED.effective().replace("{module}", String.valueOf(Formatting.RED) + class605Var.getName().replaceAll("\\s", "") + String.valueOf(Formatting.GRAY)).replace("{key}", String.valueOf(Formatting.RED) + KeyboardUtil.keyToString(iIntValue) + String.valueOf(Formatting.GRAY)).replace("{type}", String.valueOf(Formatting.RED) + class660Var.name() + String.valueOf(Formatting.GRAY))).formatted(Formatting.GRAY));
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
                    return this.moduleParser.getSuggestions(str2);
                }
                if (lowerCase.equals("remove")) {
                    String lowerCase2 = str2.toLowerCase();
                    return (List) Expensive.INSTANCE.moduleRepository().getModules().stream().filter((v0) -> {
                        return v0.hasKeyBind();
                    }).map(class605Var -> {
                        return class605Var.getName().replaceAll("\\s", "");
                    }).filter(str3 -> {
                        return str3.replaceAll("\\s", "").toLowerCase().startsWith(lowerCase2);
                    }).collect(Collectors.toList());
                }
                break;
            case 2:
                if (lowerCase.equals("add")) {
                    return this.keyParser.getSuggestions(str2);
                }
                break;
            case 3:
                if (lowerCase.equals("add")) {
                    return this.bindModeParser.getSuggestions(str2);
                }
                break;
        }
        return List.of();
    }
}
