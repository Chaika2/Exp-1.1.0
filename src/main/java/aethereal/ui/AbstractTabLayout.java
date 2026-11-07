package aethereal.ui;

public abstract class AbstractTabLayout implements TabLayout {
    public MenuTabElement tab;

    @Override
    public void initialize(MenuTabElement class732Var) {
        this.tab = class732Var;
    }

    public float originX() {
        return this.tab.framesOriginX();
    }

    public float originY() {
        return this.tab.framesOriginY();
    }

    public float scrollY() {
        return this.tab.scrollingAreaComponent().scrollY();
    }

    public float visibleTop() {
        return this.tab.scrollingAreaComponent().visibleTop();
    }

    public float visibleBottom() {
        return this.tab.scrollingAreaComponent().visibleBottom();
    }

    @Override
    public float height() {
        return (MenuWindow.MENU_HEIGHT - MenuWindow.COLLAPSED_HEADER_HEIGHT) - 2.0f;
    }

    public boolean isFrameVisible(AbstractFrame class757Var, float f, float f2) {
        float fY = class757Var.y() - f2;
        return (fY + class757Var.height()) + class757Var.contentHeight() >= visibleTop() - f && fY <= visibleBottom() + f;
    }

    public static int indexOfMin(float[] fArr) {
        int i = 0;
        float f = fArr[0];
        for (int i2 = 1; i2 < fArr.length; i2++) {
            if (fArr[i2] < f) {
                f = fArr[i2];
                i = i2;
            }
        }
        return i;
    }

    public MenuTabElement getTab() {
        return this.tab;
    }
}
