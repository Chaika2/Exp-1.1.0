package aethereal.util;
import aethereal.net.ReallyWorldProcess;
import aethereal.net.ReconnectTask;

import java.util.List;

public class ReallyWorldServerHandler implements ReconnectHandler {
    @Override
    public List<String> getServerIds() {
        return List.of("reallyworld");
    }

    @Override
    public ReconnectTask createProcess() {
        return new ReallyWorldProcess();
    }
}
