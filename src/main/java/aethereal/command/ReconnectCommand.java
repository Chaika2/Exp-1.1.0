package aethereal.command;
import aethereal.util.ChatUtil;
import aethereal.model.CommandContext;
import aethereal.Expensive;
import aethereal.util.FunTimeServerHandler;
import aethereal.event.HandledScreenRenderEvent;
import aethereal.util.HolyWorldServerHandler;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.event.PlayerTickEvent;
import aethereal.util.ReallyWorldServerHandler;
import aethereal.net.ReconnectException;
import aethereal.util.ReconnectHandler;
import aethereal.net.ReconnectTask;
import aethereal.util.ServerUtil;
import aethereal.model.TranslatedException;
import aethereal.model.Translation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ReconnectCommand implements ClientCommand {
    public ReconnectTask activeTask;
    public final List<ReconnectHandler> handlers = new ArrayList();

    public final Mc mc = Mc.INSTANCE;

    public ReconnectCommand() {
        this.handlers.add(new FunTimeServerHandler());
        this.handlers.add(new HolyWorldServerHandler());
        this.handlers.add(new ReallyWorldServerHandler());
        Expensive.INSTANCE.eventDispatcher().register(PlayerTickEvent.class, class130Var -> {
            if (this.activeTask != null) {
                try {
                    this.activeTask.tick();
                } catch (ReconnectException e) {
                    ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_ERROR_PREFIX.effective().replace("{error}", e.getMessage())).formatted(Formatting.RED));
                }
                if (this.activeTask.isComplete()) {
                    this.activeTask = null;
                }
            }
        });
        Expensive.INSTANCE.eventDispatcher().register(HandledScreenRenderEvent.class, class015Var -> {
            if (this.activeTask != null) {
                try {
                    this.activeTask.handledScreenTick();
                } catch (ReconnectException e) {
                    ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_ERROR_PREFIX.effective().replace("{error}", e.getMessage())).formatted(Formatting.RED));
                }
            }
        });
    }

    @Override
    public void execute(CommandContext class392Var) throws TranslatedException {
        if (!this.mc.isWorldLoaded()) {
            throw new TranslatedException(Lang.COMMAND_WORLD_NOT_LOADED);
        }
        if (this.mc.isSingleplayer()) {
            throw new TranslatedException(Lang.COMMAND_SINGLEPLAYER_ONLY);
        }
        for (ReconnectHandler class036Var : this.handlers) {
            Iterator<String> it = class036Var.getServerIds().iterator();
            while (it.hasNext()) {
                if (ServerUtil.isConnectedToServer(it.next())) {
                    this.activeTask = class036Var.createProcess();
                    return;
                }
            }
        }
        throw new TranslatedException(Lang.COMMAND_UNSUPPORTED_SERVER);
    }

    @Override
    public String getName() {
        return "rct";
    }

    @Override
    public Translation getDescription() {
        return Lang.COMMAND_RCT_DESC;
    }

    @Override
    public String getUsage() {
        return ".rct";
    }

    @Override
    public List<String> getAliases() {
        return List.of("reconnect");
    }

    @Override
    public List<String> getSuggestions(String[] strArr, int i) {
        return List.of();
    }
}
