package aethereal.util;
import aethereal.command.ClientCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommandResolver {
    public final CommandRegistry registry;

    public CommandResolver(CommandRegistry class187Var) {
        this.registry = class187Var;
    }

    public Optional<ClientCommand> resolve(String str) {
        if (str.isEmpty()) {
            return Optional.empty();
        }
        return this.registry.getCommand(str.split("\\s+")[0]);
    }

    public List<String> getSuggestions(String str) {
        if (str.isEmpty()) {
            return this.registry.getCommandNames();
        }
        String[] strArrSplit = str.split("\\s+", -1);
        if (strArrSplit.length == 1) {
            String lowerCase = strArrSplit[0].toLowerCase();
            return (List) this.registry.getCommandNames().stream().filter(str2 -> {
                return str2.startsWith(lowerCase);
            }).sorted().collect(Collectors.toList());
        }
        Optional<ClientCommand> command = this.registry.getCommand(strArrSplit[0]);
        if (!command.isPresent()) {
            return Collections.emptyList();
        }
        String[] strArr = (String[]) Arrays.copyOfRange(strArrSplit, 1, strArrSplit.length);
        return command.get().getSuggestions(strArr, strArr.length - 1);
    }
}
