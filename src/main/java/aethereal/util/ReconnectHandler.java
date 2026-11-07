package aethereal.util;
import aethereal.net.ReconnectTask;

import java.util.List;

public interface ReconnectHandler {
    List<String> getServerIds();

    ReconnectTask createProcess();
}
