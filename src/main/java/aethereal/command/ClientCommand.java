package aethereal.command;
import aethereal.model.CommandContext;
import aethereal.model.Translation;

import java.util.List;

public interface ClientCommand {
    String getName();

    Translation getDescription();

    String getUsage();

    List<String> getAliases();

    void execute(CommandContext class392Var);

    List<String> getSuggestions(String[] strArr, int i);
}
