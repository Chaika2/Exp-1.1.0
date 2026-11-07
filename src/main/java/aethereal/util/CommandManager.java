package aethereal.util;
import aethereal.command.ClientCommand;
import aethereal.model.CommandContext;
import aethereal.Expensive;
import aethereal.Lang;
import aethereal.event.PacketSendEvent;
import aethereal.model.TranslatedException;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CommandManager {
    public String prefix = ".";
    public final CommandRegistry repository = new CommandRegistry();

    public final CommandResolver resolver = new CommandResolver(this.repository);

    public CommandManager() {
        Expensive.INSTANCE.eventDispatcher().register(PacketSendEvent.class, class037Var -> {
            if (class037Var.getPacket() instanceof ChatMessageC2SPacket packet) {
                String strChatMessage = packet.chatMessage();
                if (strChatMessage.startsWith(this.prefix)) {
                    class037Var.cancel();
                    dispatch(strChatMessage.substring(this.prefix.length()));
                }
            }
        });
    }

    public void dispatch(String str) {
        if (str.trim().isEmpty()) {
            ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_USAGE_GENERIC.effective()).formatted(Formatting.RED));
            return;
        }
        try {
            Optional<ClientCommand> optionalResolve = this.resolver.resolve(str);
            if (optionalResolve.isEmpty()) {
                ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_UNKNOWN_COMMAND.effective().replace("{command}", str.split("\\s+")[0])).formatted(Formatting.RED));
                return;
            }
            ClientCommand class349Var = optionalResolve.get();
            String[] strArrSplit = str.split("\\s+", -1);
            class349Var.execute(new CommandContext(strArrSplit.length > 1 ? (String[]) Arrays.copyOfRange(strArrSplit, 1, strArrSplit.length) : new String[0]));
        } catch (TranslatedException e) {
            ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_ERROR_PREFIX.effective().replace("{error}", e.getMessage())).formatted(Formatting.RED));
        } catch (Exception e2) {
            ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_UNEXPECTED_ERROR.effective()).formatted(Formatting.RED));
            e2.printStackTrace();
        }
    }

    public void registerCommand(ClientCommand class349Var) {
        this.repository.register(class349Var);
    }

    public void unregisterCommand(String str) {
        this.repository.unregister(str);
    }

    public Collection<ClientCommand> getAllCommands() {
        return this.repository.getAllCommands();
    }

    public List<String> getSuggestions(String str) {
        return this.resolver.getSuggestions(str);
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String str) {
        this.prefix = str;
    }

    public CommandRegistry getRepository() {
        return this.repository;
    }
}
