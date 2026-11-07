package aethereal.util;
import aethereal.type.BindMode;
import aethereal.ui.setting.BooleanSetting;
import aethereal.type.ButtonAction;
import aethereal.event.ClientListener;
import aethereal.ui.setting.ExpandableSetting;
import aethereal.Expensive;
import aethereal.type.KeyAction;
import aethereal.event.KeyInputEvent;
import aethereal.type.KeyPressState;
import aethereal.ui.setting.KeybindSetting;
import aethereal.type.Mc;
import aethereal.module.Module;
import aethereal.module.ModuleSettingConsumer;
import aethereal.event.MouseButtonEvent;
import aethereal.ui.setting.Setting;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class KeybindHandler implements ClientListener {
    public static final int codeBufferLength = 6;
    public static final String secretCode = "mxrbid";
    public final StringBuilder typedBuffer = new StringBuilder();
    public final Set<Integer> pressedKeys = new HashSet();

    public KeybindHandler() {
        Expensive.INSTANCE.eventDispatcher().register(KeyInputEvent.class, this::onKeyInput);
        Expensive.INSTANCE.eventDispatcher().register(MouseButtonEvent.class, this::onMouseButton);
    }

    public void onKeyInput(KeyInputEvent class049Var) {
        if (Mc.INSTANCE.getWorld() == null) {
            return;
        }
        if (class049Var.action() == KeyPressState.PRESS) {
            handleTypedChar((char) class049Var.key());
            this.pressedKeys.add(Integer.valueOf(class049Var.key()));
        }
        forEachModuleSetting((class605Var, class661Var) -> {
            switch (typeSwitch00252(class661Var)) {
                case Module.KEY_UNBOUND:
                    processModuleBind(class049Var, class605Var);
                    break;
                case 0:
                    processBooleanSettingBind(class049Var, (BooleanSetting) class661Var);
                    break;
                case 1:
                    ExpandableSetting class670Var = (ExpandableSetting) class661Var;
                    processExpandableSettingBind(class049Var, class670Var);
                    if (class670Var.isValue()) {
                        class670Var.getSubSettings().forEach(class661Var2 -> {
                            if (class661Var2 instanceof KeybindSetting) {
                                processKeybindSettingBind(class049Var, (KeybindSetting) class661Var2);
                            }
                        });
                    }
                    break;
                case 2:
                    processKeybindSettingBind(class049Var, (KeybindSetting) class661Var);
                    break;
            }
        });
        if (class049Var.action() == KeyPressState.RELEASE) {
            this.pressedKeys.remove(Integer.valueOf(class049Var.key()));
        }
    }

    public void onMouseButton(MouseButtonEvent class107Var) {
        if (Mc.INSTANCE.getWorld() == null) {
            return;
        }
        if (class107Var.action() == ButtonAction.PRESS) {
            this.pressedKeys.add(Integer.valueOf(class107Var.button()));
        }
        forEachModuleSetting((class605Var, class661Var) -> {
            switch (typeSwitch00252(class661Var)) {
                case Module.KEY_UNBOUND:
                    processModuleBind(class107Var, class605Var);
                    break;
                case 0:
                    processBooleanSettingBind(class107Var, (BooleanSetting) class661Var);
                    break;
                case 1:
                    ExpandableSetting class670Var = (ExpandableSetting) class661Var;
                    processExpandableSettingBind(class107Var, class670Var);
                    if (class670Var.isValue()) {
                        class670Var.getSubSettings().forEach(class661Var2 -> {
                            if (class661Var2 instanceof KeybindSetting) {
                                processKeybindSettingBind(class107Var, (KeybindSetting) class661Var2);
                            }
                        });
                    }
                    break;
                case 2:
                    processKeybindSettingBind(class107Var, (KeybindSetting) class661Var);
                    break;
            }
        });
        if (class107Var.action() == ButtonAction.RELEASE) {
            this.pressedKeys.remove(Integer.valueOf(class107Var.button()));
        }
    }

    public void handleTypedChar(char c) {
        this.typedBuffer.append(c);
        if (this.typedBuffer.length() > codeBufferLength) {
            this.typedBuffer.delete(0, this.typedBuffer.length() - codeBufferLength);
        }
        if (secretCode.equalsIgnoreCase(this.typedBuffer.toString())) {
            WavSoundPlayer.INSTANCE.playSound(secretCode, 80.0f, false);
        }
    }

    public void forEachModuleSetting(ModuleSettingConsumer class143Var) {
        for (Module class605Var : Expensive.INSTANCE.moduleRepository().getModules()) {
            class143Var.process(class605Var, null);
            if (class605Var.isState()) {
                Iterator<Setting> it = class605Var.getSettings().iterator();
                while (it.hasNext()) {
                    class143Var.process(class605Var, it.next());
                }
            }
        }
    }

    public void processModuleBind(Object obj, Module class605Var) {
        if (class605Var.getKeyBind().isEmpty()) {
            return;
        }
        if (obj instanceof KeyInputEvent) {
            KeyInputEvent class049Var = (KeyInputEvent) obj;
            if (matchesKeyBind(class049Var, class605Var.getKeyBind())) {
                BindMode type = class605Var.getType();
                boolean z = class049Var.action() == KeyPressState.PRESS;
                Objects.requireNonNull(class605Var);
                applyBindAction(type, z, class605Var::switchState);
                return;
            }
        }
        if (obj instanceof MouseButtonEvent) {
            MouseButtonEvent class107Var = (MouseButtonEvent) obj;
            if (matchesMouseBind(class107Var, class605Var.getKeyBind())) {
                BindMode type2 = class605Var.getType();
                boolean z2 = class107Var.action() == ButtonAction.PRESS;
                Objects.requireNonNull(class605Var);
                applyBindAction(type2, z2, class605Var::switchState);
            }
        }
    }

    public void processBooleanSettingBind(Object obj, BooleanSetting class665Var) {
        if (class665Var.getKeyBind().isEmpty()) {
            return;
        }
        if (obj instanceof KeyInputEvent) {
            KeyInputEvent class049Var = (KeyInputEvent) obj;
            if (matchesKeyBind(class049Var, class665Var.getKeyBind())) {
                BindMode type = class665Var.getType();
                boolean z = class049Var.action() == KeyPressState.PRESS;
                Objects.requireNonNull(class665Var);
                applyBindAction(type, z, class665Var::switchValue);
                return;
            }
        }
        if (obj instanceof MouseButtonEvent) {
            MouseButtonEvent class107Var = (MouseButtonEvent) obj;
            if (matchesMouseBind(class107Var, class665Var.getKeyBind())) {
                BindMode type2 = class665Var.getType();
                boolean z2 = class107Var.action() == ButtonAction.PRESS;
                Objects.requireNonNull(class665Var);
                applyBindAction(type2, z2, class665Var::switchValue);
            }
        }
    }

    public void processExpandableSettingBind(Object obj, ExpandableSetting class670Var) {
        if (class670Var.getKeybinds().isEmpty()) {
            return;
        }
        Runnable runnable = () -> {
            class670Var.setValue(!class670Var.isValue());
        };
        if (obj instanceof KeyInputEvent) {
            KeyInputEvent class049Var = (KeyInputEvent) obj;
            if (matchesKeyBind(class049Var, class670Var.getKeybinds())) {
                applyBindAction(class670Var.getType(), class049Var.action() == KeyPressState.PRESS, runnable);
                return;
            }
        }
        if (obj instanceof MouseButtonEvent) {
            MouseButtonEvent class107Var = (MouseButtonEvent) obj;
            if (matchesMouseBind(class107Var, class670Var.getKeybinds())) {
                applyBindAction(class670Var.getType(), class107Var.action() == ButtonAction.PRESS, runnable);
            }
        }
    }

    public void processKeybindSettingBind(Object obj, KeybindSetting class663Var) {
        if (class663Var.getKeyBind().isEmpty()) {
            return;
        }
        if (obj instanceof KeyInputEvent) {
            KeyInputEvent class049Var = (KeyInputEvent) obj;
            if (matchesKeyBind(class049Var, class663Var.getKeyBind())) {
                KeyAction class664Var = class049Var.action() == KeyPressState.PRESS ? KeyAction.PRESS : KeyAction.RELEASE;
                applyBindAction(class663Var.getType(), class049Var.action() == KeyPressState.PRESS, () -> {
                    class663Var.getConsumer().accept(class664Var);
                });
                return;
            }
        }
        if (obj instanceof MouseButtonEvent) {
            MouseButtonEvent class107Var = (MouseButtonEvent) obj;
            if (matchesMouseBind(class107Var, class663Var.getKeyBind())) {
                KeyAction class664Var2 = class107Var.action() == ButtonAction.PRESS ? KeyAction.PRESS : KeyAction.RELEASE;
                applyBindAction(class663Var.getType(), class107Var.action() == ButtonAction.PRESS, () -> {
                    class663Var.getConsumer().accept(class664Var2);
                });
            }
        }
    }

    public void applyBindAction(BindMode class660Var, boolean z, Runnable runnable) {
        if (class660Var == BindMode.TOGGLE && z) {
            runnable.run();
        } else if (class660Var == BindMode.HOLD) {
            runnable.run();
        }
    }

    public boolean matchesKeyBind(KeyInputEvent class049Var, List<Integer> list) {
        if ((class049Var.action() == KeyPressState.PRESS || class049Var.action() == KeyPressState.RELEASE) && class049Var.key() == ((Integer) list.getLast()).intValue()) {
            if (isCombinationPressed(class049Var.action() == KeyPressState.RELEASE, list)) {
                return true;
            }
        }
        return false;
    }

    public boolean matchesMouseBind(MouseButtonEvent class107Var, List<Integer> list) {
        if ((class107Var.action() == ButtonAction.PRESS || class107Var.action() == ButtonAction.RELEASE) && class107Var.button() == ((Integer) list.getLast()).intValue()) {
            if (isCombinationPressed(class107Var.action() == ButtonAction.RELEASE, list)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCombinationPressed(boolean z, List<Integer> list) {
        HashSet hashSet = new HashSet(this.pressedKeys);
        if (z) {
            hashSet.add((Integer) list.getLast());
        }
        Set<Integer> setMethod027 = collectAllBoundKeys();
        HashSet hashSet2 = new HashSet();
        Iterator it = hashSet.iterator();
        while (it.hasNext()) {
            int iIntValue = ((Integer) it.next()).intValue();
            if (setMethod027.contains(Integer.valueOf(iIntValue))) {
                hashSet2.add(Integer.valueOf(iIntValue));
            }
        }
        return hashSet2.containsAll(list) && !isOverriddenByLongerBind(hashSet2, list, ((Integer) list.getLast()).intValue());
    }

    public boolean isOverriddenByLongerBind(Set<Integer> set, List<Integer> list, int i) {
        HashSet hashSet = new HashSet(list);
        return collectAllBinds().stream().filter(list2 -> {
            return !new HashSet(list2).equals(hashSet);
        }).filter(list3 -> {
            return !list3.isEmpty() && ((Integer) list3.getLast()).intValue() == i;
        }).anyMatch(list4 -> {
            return set.equals(new HashSet(list4)) && list4.size() > list.size();
        });
    }

    public Set<List<Integer>> collectAllBinds() {
        HashSet hashSet = new HashSet();
        forEachModuleSetting((class605Var, class661Var) -> {
            switch (typeSwitch00252(class661Var)) {
                case Module.KEY_UNBOUND:
                    addBindList(hashSet, class605Var.getKeyBind());
                    break;
                case 0:
                    addBindList(hashSet, ((BooleanSetting) class661Var).getKeyBind());
                    break;
                case 1:
                    ExpandableSetting class670Var = (ExpandableSetting) class661Var;
                    addBindList(hashSet, class670Var.getKeybinds());
                    class670Var.getSubSettings().forEach(class661Var2 -> {
                        if (class661Var2 instanceof KeybindSetting) {
                            addBindList(hashSet, ((KeybindSetting) class661Var2).getKeyBind());
                        }
                    });
                    break;
                case 2:
                    addBindList(hashSet, ((KeybindSetting) class661Var).getKeyBind());
                    break;
            }
        });
        return hashSet;
    }

    public Set<Integer> collectAllBoundKeys() {
        HashSet hashSet = new HashSet();
        forEachModuleSetting((class605Var, class661Var) -> {
            switch (typeSwitch00252(class661Var)) {
                case Module.KEY_UNBOUND:
                    addBindKeys(hashSet, class605Var.getKeyBind());
                    break;
                case 0:
                    addBindKeys(hashSet, ((BooleanSetting) class661Var).getKeyBind());
                    break;
                case 1:
                    ExpandableSetting class670Var = (ExpandableSetting) class661Var;
                    addBindKeys(hashSet, class670Var.getKeybinds());
                    class670Var.getSubSettings().forEach(class661Var2 -> {
                        if (class661Var2 instanceof KeybindSetting) {
                            addBindKeys(hashSet, ((KeybindSetting) class661Var2).getKeyBind());
                        }
                    });
                    break;
                case 2:
                    addBindKeys(hashSet, ((KeybindSetting) class661Var).getKeyBind());
                    break;
            }
        });
        return hashSet;
    }

    public void addBindList(Set<List<Integer>> set, List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        set.add(list);
    }

    public void addBindKeys(Set<Integer> set, List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        set.addAll(list);
    }

    static int typeSwitch00252(Object o) {
        if (o == null) {
            return -1;
        }
        if (o instanceof BooleanSetting) {
            return 0;
        }
        if (o instanceof ExpandableSetting) {
            return 1;
        }
        if (o instanceof KeybindSetting) {
            return 2;
        }
        return 3;
    }
}
