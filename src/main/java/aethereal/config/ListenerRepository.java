package aethereal.config;
import aethereal.event.ClientListener;
import aethereal.util.CombatDetector;
import aethereal.util.CoreTickHandler;
import aethereal.ui.EnderChestScreenRedirect;
import aethereal.util.FragmentShaderBuilder;
import aethereal.event.GrimInputListener;
import aethereal.util.KeybindHandler;
import aethereal.util.MatrixCaptureHandler;
import aethereal.event.PlayerListListener;
import aethereal.util.SlotSyncHandler;
import aethereal.util.StaffPacketDebug;
import aethereal.util.StaffMessageProbe;

import java.util.ArrayList;
import java.util.List;

public class ListenerRepository {
    public final List<ClientListener> listeners = new ArrayList();

    public ListenerRepository() {
        setup();
    }

    public void setup() {
        registerListeners(new SlotSyncHandler(), new KeybindHandler(), new CombatDetector(), new FragmentShaderBuilder(), new EnderChestScreenRedirect(), new GrimInputListener(), new PlayerListListener(), new MatrixCaptureHandler(), new CoreTickHandler(), new StaffPacketDebug(), new StaffMessageProbe());
    }

    public void registerListeners(ClientListener... class291VarArr) {
        this.listeners.addAll(List.of(class291VarArr));
    }

    public List<ClientListener> getListeners() {
        return this.listeners;
    }
}
