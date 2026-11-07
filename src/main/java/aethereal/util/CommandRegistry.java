package aethereal.util;
import aethereal.command.ClientCommand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CommandRegistry {
    public final Map<String, ClientCommand> commands = new LinkedHashMap();
    public final Map<String, String> aliases = new LinkedHashMap();

    public void register(ClientCommand class349Var) {
        this.commands.put(class349Var.getName().toLowerCase(), class349Var);
        if (class349Var.getAliases() == null || class349Var.getAliases().isEmpty()) {
            return;
        }
        Iterator<String> it = class349Var.getAliases().iterator();
        while (it.hasNext()) {
            this.aliases.put(it.next().toLowerCase(), class349Var.getName().toLowerCase());
        }
    }

    public void unregister(String str) {
        ClientCommand class349VarRemove = this.commands.remove(str.toLowerCase());
        if (class349VarRemove != null) {
            class349VarRemove.getAliases().forEach(str2 -> {
                this.aliases.remove(str2.toLowerCase());
            });
        }
    }

    public Optional<ClientCommand> getCommand(String str) {
        return Optional.ofNullable(this.commands.get(this.aliases.getOrDefault(str.toLowerCase(), str.toLowerCase())));
    }

    public Collection<ClientCommand> getAllCommands() {
        return Collections.unmodifiableCollection(this.commands.values());
    }

    public List<String> getCommandNames() {
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(this.commands.keySet());
        arrayList.addAll(this.aliases.keySet());
        return arrayList;
    }
}
