package aethereal.util;
import aethereal.net.FunTimeProcess;
import aethereal.net.ReconnectTask;

import java.util.List;

public class FunTimeServerHandler implements ReconnectHandler {
    public static final String funtimeId = "funtime";

    @Override
    public List<String> getServerIds() {
        return List.of(funtimeId, "spookytime");
    }

    @Override
    public ReconnectTask createProcess() {
        return new FunTimeProcess();
    }
}
