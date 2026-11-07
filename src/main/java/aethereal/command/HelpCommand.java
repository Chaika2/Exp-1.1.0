package aethereal.command;
import aethereal.util.ChatUtil;
import aethereal.util.ColorUtil;
import aethereal.model.CommandContext;
import aethereal.util.CommandManager;
import aethereal.Lang;
import aethereal.util.StencilBufferUtil;
import aethereal.model.Translation;

import java.util.Comparator;
import java.util.List;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HelpCommand implements ClientCommand {
    public final CommandManager commandManager;

    public HelpCommand(CommandManager class043Var) {
        this.commandManager = class043Var;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public Translation getDescription() {
        return Lang.COMMAND_HELP_DESC;
    }

    @Override
    public String getUsage() {
        return ".help";
    }

    @Override
    public List<String> getAliases() {
        return List.of("?");
    }

    @Override
    public void execute(CommandContext class392Var) {
        ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_HELP_AVAILABLE.effective()).formatted(Formatting.GRAY));
        this.commandManager.getAllCommands().stream().sorted(Comparator.comparing((v0) -> {
            return v0.getName();
        })).forEach(class349Var -> {
            if (class349Var == this) {
                return;
            }
            String str = "." + class349Var.getName();
            ChatUtil.addChatMessage((Text) Text.literal(str).setStyle(Style.EMPTY.withColor(ColorUtil.argb(StencilBufferUtil.STENCIL_MASK, 100, 100, 100)).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, str)).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(Lang.COMMAND_HELP_HOVER.effective().replace("{usage}", class349Var.getDescription().effective() + "\n" + class349Var.getUsage())).formatted(Formatting.GRAY)))));
        });
    }

    @Override
    public List<String> getSuggestions(String[] strArr, int i) {
        return List.of();
    }
}
