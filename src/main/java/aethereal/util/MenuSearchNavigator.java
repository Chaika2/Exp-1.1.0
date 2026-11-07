package aethereal.util;
import aethereal.ui.AbstractFrame;
import aethereal.Expensive;
import aethereal.ui.MenuTabElement;
import aethereal.ui.ModuleCard;
import aethereal.ui.setting.Setting;

public final class MenuSearchNavigator implements SearchNavigator {
    @Override
    public void focusFrame(AbstractFrame class757Var, MenuTabElement class732Var) {
        TabsController class733VarTabsController = Expensive.INSTANCE.tabsController();
        if (class733VarTabsController.current() != class732Var) {
            class733VarTabsController.open(class732Var);
            class732Var.markFramesDirty();
        }
        ensureCategoryVisible(class733VarTabsController, class732Var, class757Var);
        class732Var.focusFrame(class757Var);
    }

    @Override
    public void focusSetting(MenuTabElement class732Var, AbstractFrame class757Var, Setting class661Var) {
        TabsController class733VarTabsController = Expensive.INSTANCE.tabsController();
        if (class733VarTabsController.current() != class732Var) {
            class733VarTabsController.open(class732Var);
            class732Var.markFramesDirty();
        }
        ensureCategoryVisible(class733VarTabsController, class732Var, class757Var);
        class732Var.focusSetting(class757Var, class661Var);
    }

    public void ensureCategoryVisible(TabsController class733Var, MenuTabElement class732Var, AbstractFrame class757Var) {
        if (class757Var instanceof ModuleCard) {
            class733Var.ensureCategoryVisible(class732Var, ((ModuleCard) class757Var).module().getCategory());
        }
    }
}
