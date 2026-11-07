package aethereal.util;
import aethereal.ui.AbstractFrame;
import aethereal.ui.MenuTabElement;
import aethereal.ui.setting.Setting;

public interface SearchNavigator {
    void focusFrame(AbstractFrame class757Var, MenuTabElement class732Var);

    void focusSetting(MenuTabElement class732Var, AbstractFrame class757Var, Setting class661Var);
}
