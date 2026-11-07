package aethereal.render;
import aethereal.model.CursorMoveInput;
import aethereal.event.InputEvent;
import aethereal.model.InputEventContext;
import aethereal.model.PixelPoint;
import aethereal.util.WindowController;

public class WindowControllerAdapter {
    public final WindowController controller;

    public PixelPoint mousePosition = new PixelPoint(0, 0);

    public void preBlitFramebufferToBackbuffer(long j) throws MatchException {
        this.controller.draw(j);
        handleInput(new CursorMoveInput(this.controller.window().determineMousePosition()));
    }

    public void handleResize(int i, int i2) {
        this.controller.handleResize(i, i2);
    }

    public boolean handleInput(InputEvent class691Var) throws MatchException {
        if (class691Var instanceof CursorMoveInput) {
            try {
                this.mousePosition = ((CursorMoveInput) class691Var).mousePosition();
            } catch (Throwable th) {
                throw new MatchException(th.toString(), th);
            }
        }
        return this.controller.handleInput(new InputEventContext(class691Var, this.mousePosition, this.controller.dpiScaleFactor()));
    }

    public boolean interceptKeyboard() {
        return this.controller.interceptKeyboardIfScreenPresent() || this.controller.interceptKeyboard();
    }

    public boolean interceptMouse() {
        return this.controller.interceptCursorIfScreenNotPresent();
    }

    public WindowControllerAdapter(WindowController class686Var) {
        this.controller = class686Var;
    }
}
