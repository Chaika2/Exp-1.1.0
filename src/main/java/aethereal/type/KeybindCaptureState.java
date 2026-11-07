package aethereal.type;
import aethereal.math.Easings;
import aethereal.util.GrimDelayHandler;
import aethereal.model.KeyInput;
import aethereal.util.KeyboardUtil;
import aethereal.model.MouseButtonInput;
import aethereal.render.ToggleAnimator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeybindCaptureState {
    static final int maxKeys = 2;
    public final Set<Integer> pressedKeys = new LinkedHashSet();
    public final Set<Integer> mutableKeys = new LinkedHashSet();
    public final Set<Integer> immutableKeys = new LinkedHashSet();
    public boolean captureKey = false;
    public final ToggleAnimator conflictAnimation = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);
    public Consumer<List<Integer>> changeCallback;

    public void toggleCapture() {
        this.captureKey = !this.captureKey;
    }

    public boolean handleKeyInput(KeyInput class696Var) {
        if (!this.captureKey) {
            return false;
        }
        if (class696Var.keyAction().press()) {
            return handleKeyPress(class696Var.keyCode());
        }
        if (class696Var.keyAction().release()) {
            return handleKeyRelease();
        }
        return false;
    }

    public boolean handleMouseInput(MouseButtonInput class693Var) {
        if (!this.captureKey || class693Var.button() == 0 || class693Var.button() == 1) {
            return false;
        }
        if (class693Var.action().press()) {
            return handleMousePress(class693Var.button());
        }
        if (class693Var.action().release()) {
            return handleMouseRelease();
        }
        return false;
    }

    boolean handleKeyPress(int i) {
        if (i == 256 || i == 261) {
            clearBinding();
            this.captureKey = false;
            return true;
        }
        if (i != -1) {
            this.pressedKeys.add(Integer.valueOf(i));
        }
        if (this.pressedKeys.size() < 2) {
            return true;
        }
        commitKeys();
        this.captureKey = false;
        return true;
    }

    boolean handleKeyRelease() {
        if (!this.pressedKeys.isEmpty()) {
            commitKeys();
        }
        this.captureKey = false;
        return true;
    }

    boolean handleMousePress(int i) {
        if (i >= 0 && i < 8) {
            this.pressedKeys.add(Integer.valueOf(i));
        }
        if (this.pressedKeys.size() < 2) {
            return true;
        }
        commitKeys();
        this.captureKey = false;
        return true;
    }

    boolean handleMouseRelease() {
        if (!this.pressedKeys.isEmpty()) {
            commitKeys();
        }
        this.captureKey = false;
        return true;
    }

    void clearBinding() {
        this.pressedKeys.clear();
        this.mutableKeys.clear();
        if (this.changeCallback != null) {
            this.changeCallback.accept(List.of());
        }
        GrimDelayHandler.refreshPressedKeys();
    }

    void commitKeys() {
        List<Integer> listCopyOf = List.copyOf(this.pressedKeys);
        this.mutableKeys.clear();
        this.mutableKeys.addAll(limitKeys(listCopyOf));
        if (this.changeCallback != null) {
            this.changeCallback.accept(List.copyOf(this.mutableKeys));
        }
        GrimDelayHandler.refreshPressedKeys();
        this.pressedKeys.clear();
    }

    public KeybindCaptureState addMutableKeys(List<Integer> list) {
        this.mutableKeys.clear();
        Stream<Integer> streamFilter = list.stream().filter(num -> {
            return num.intValue() != -1;
        });
        Set<Integer> set = this.mutableKeys;
        Objects.requireNonNull(set);
        streamFilter.forEach((v1) -> {
            set.add(v1);
        });
        return this;
    }

    public void syncKeys(List<Integer> list) {
        if (this.mutableKeys.equals(new LinkedHashSet(list))) {
            return;
        }
        addMutableKeys(list);
    }

    public KeybindCaptureState addImmutableKeys(List<Integer> list) {
        this.immutableKeys.clear();
        Stream<Integer> streamFilter = list.stream().filter(num -> {
            return num.intValue() != -1;
        });
        Set<Integer> set = this.immutableKeys;
        Objects.requireNonNull(set);
        streamFilter.forEach((v1) -> {
            set.add(v1);
        });
        return this;
    }

    public String formatBindingText() {
        ArrayList arrayList = new ArrayList();
        if (hasKeys(this.mutableKeys)) {
            arrayList.add(this.captureKey ? "..." : formatKeys(this.mutableKeys));
        }
        if (hasKeys(this.immutableKeys)) {
            arrayList.add(formatKeys(this.immutableKeys));
        }
        if (arrayList.isEmpty()) {
            return this.captureKey ? "..." : "None";
        }
        return String.join(" or ", arrayList);
    }

    String formatKeys(Set<Integer> set) {
        return (String) set.stream().map((v0) -> {
            return KeyboardUtil.keyToString(v0);
        }).collect(Collectors.joining(" + "));
    }

    public boolean hasAnyKeys() {
        return hasKeys(this.mutableKeys) || hasKeys(this.immutableKeys);
    }

    boolean hasKeys(Set<Integer> set) {
        return (set == null || set.isEmpty()) ? false : true;
    }

    List<Integer> limitKeys(List<Integer> list) {
        return list.stream().limit(2L).toList();
    }

    public Set<Integer> pressedKeys() {
        return this.pressedKeys;
    }

    public Set<Integer> mutableKeys() {
        return this.mutableKeys;
    }

    public Set<Integer> immutableKeys() {
        return this.immutableKeys;
    }

    public boolean captureKey() {
        return this.captureKey;
    }

    public ToggleAnimator conflictAnimation() {
        return this.conflictAnimation;
    }

    public KeybindCaptureState onChange(Consumer<List<Integer>> consumer) {
        this.changeCallback = consumer;
        return this;
    }
}
