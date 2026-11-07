package aethereal.ui;
import aethereal.model.LayoutScaleContext;
import aethereal.event.WindowVisibilityListener;

public abstract class AbstractWindow extends WidgetContainer {
    public WindowVisibilityListener windowVisibilityListener;

    public void visibilityListener(WindowVisibilityListener class685Var) {
        this.windowVisibilityListener = class685Var;
    }

    public void onVisibilityChanged(boolean z) {
        if (this.windowVisibilityListener != null) {
            this.windowVisibilityListener.onWindowVisibilityChanged(this, z);
        }
    }

    public void open() {
        visible(true);
        onVisibilityChanged(true);
    }

    public void close() {
        visible(false);
        onVisibilityChanged(false);
        handleClose();
    }

    public void toggle() {
        if (this.visible) {
            close();
        } else {
            open();
        }
    }

    public AbstractWindow(float f, float f2) {
        setSize(f, f2);
    }

    public void center(LayoutScaleContext class698Var) {
        setPosition(class698Var.alignToScale((class698Var.logicalWidth() - width()) / 2.0f), class698Var.alignToScale((class698Var.logicalHeight() - height()) / 2.0f));
    }
}
