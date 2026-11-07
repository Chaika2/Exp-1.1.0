package aethereal.model;
import aethereal.ui.AbstractFrame;
import aethereal.ui.MenuTabElement;
import aethereal.util.SearchNavigator;


public final class FrameSearchTarget implements SearchNavTarget {
    public final AbstractFrame frameContainer;
    public final MenuTabElement tab;

    public FrameSearchTarget(AbstractFrame class757Var, MenuTabElement class732Var) {
        this.frameContainer = class757Var;
        this.tab = class732Var;
    }

    @Override
    public void navigate(SearchNavigator class843Var) {
        class843Var.focusFrame(this.frameContainer, this.tab);
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "frameContainer=" + this.frameContainer + ", " + "tab=" + this.tab + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.frameContainer, this.tab);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FrameSearchTarget)) return false;
        FrameSearchTarget o = (FrameSearchTarget) obj;
        return java.util.Objects.equals(this.frameContainer, o.frameContainer) && java.util.Objects.equals(this.tab, o.tab);
    }
public AbstractFrame frameContainer() {
        return this.frameContainer;
    }

    public MenuTabElement tab() {
        return this.tab;
    }
}
