package aethereal.util;
import aethereal.net.HolyWorldProcess;
import aethereal.net.ReconnectTask;

import java.util.List;

public class HolyWorldServerHandler implements ReconnectHandler {
    @Override
    public List<String> getServerIds() {
        return List.of("holyworld");
    }

    @Override
    public ReconnectTask createProcess() {
        return new HolyWorldProcess();
    }
}
