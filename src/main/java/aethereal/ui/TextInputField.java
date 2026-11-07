package aethereal.ui;
import aethereal.model.CharInput;
import aethereal.model.CursorMoveInput;
import aethereal.math.Easings;
import aethereal.event.InputEvent;
import aethereal.model.InputEventContext;
import aethereal.model.KeyInput;
import aethereal.type.KeyInputAction;
import aethereal.type.Mc;
import aethereal.type.MouseButtonAction;
import aethereal.model.MouseButtonInput;
import aethereal.render.MsdfFont;
import aethereal.model.ScrollInput;
import aethereal.math.Stopwatch;
import aethereal.render.ToggleAnimator;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.util.function.Consumer;
import net.minecraft.client.gui.screen.Screen;

public class TextInputField {
    public final MsdfFont font;
    public final int fontSize;
    public boolean focused;
    public boolean cursorVisible;
    public boolean dragging;
    public float viewportOffset;
    public float viewportWidth;
    public float textOriginX;
    public boolean pendingScroll;
    public long lastClickTime;
    public int lastClickIndex;
    public Consumer<String> changeCallback;
    public StringBuilder textBuffer = new StringBuilder();
    public final Stopwatch blinkTimer = new Stopwatch();
    public final WidgetBounds bounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
    public int selectionAnchor = -1;
    public int selectionEnd = -1;
    public int cursorIndex = 0;
    public boolean numbersOnly = false;
    public int maxLength = -1;
    public float scaleFactor = 1.0f;
    public final ToggleAnimator hoverAnimation = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);

    public TextInputField(MsdfFont class161Var, int i) {
        this.font = class161Var;
        this.fontSize = i;
    }

    public void listen(float f, float f2, float f3, float f4, float f5, float f6, float f7) {
        this.viewportWidth = f5;
        this.bounds.withPosition(f, f2).withSize(f3, f4);
        this.textOriginX = f6;
        this.scaleFactor = f7 <= 0.0f ? 1.0f : f7;
        if (this.pendingScroll && f5 > 0.0f) {
            ensureCursorVisible(this.cursorIndex);
            this.pendingScroll = false;
        }
        if (this.focused && this.blinkTimer.hasElapsed(500L)) {
            this.cursorVisible = !this.cursorVisible;
            this.blinkTimer.reset();
        }
    }

    public boolean handleInput(InputEventContext class688Var, boolean z) throws MatchException {
        InputEvent class691VarInputEvent = class688Var.inputEvent();
        boolean zInArea = class688Var.inArea(this.bounds.x(), this.bounds.y(), this.bounds.width(), this.bounds.height());
        if (class691VarInputEvent instanceof ScrollInput) {
            return !z && this.focused && zInArea;
        }
        if (class691VarInputEvent instanceof CursorMoveInput) {
            try {
                ((CursorMoveInput) class691VarInputEvent).mousePosition();
                this.hoverAnimation.state(zInArea && !z);
                if (z) {
                    this.dragging = false;
                    return false;
                }
                if (!this.dragging) {
                    return zInArea;
                }
                if (!this.focused) {
                    return false;
                }
                int iCalculateCursorPosition = calculateCursorPosition(class688Var.logicalMousePosition().x());
                if (iCalculateCursorPosition == this.cursorIndex) {
                    return true;
                }
                this.cursorIndex = iCalculateCursorPosition;
                this.selectionEnd = this.cursorIndex;
                this.blinkTimer.reset();
                ensureCursorVisible(this.cursorIndex);
                return true;
            } catch (Throwable th) {
                throw new MatchException(th.toString(), th);
            }
        }
        if (class691VarInputEvent instanceof MouseButtonInput) {
            MouseButtonInput class693Var = (MouseButtonInput) class691VarInputEvent;
            int iButton = class693Var.button();
            MouseButtonAction class706VarAction = class693Var.action();
            if (iButton != 0) {
                return false;
            }
            if (class706VarAction.press()) {
                if (!zInArea) {
                    this.focused = false;
                    this.dragging = false;
                    clearSelection();
                } else if (!z) {
                    this.focused = true;
                    int iCalculateCursorPosition2 = calculateCursorPosition(class688Var.logicalMousePosition().x());
                    long jCurrentTimeMillis = System.currentTimeMillis();
                    if (jCurrentTimeMillis - this.lastClickTime >= 300 || iCalculateCursorPosition2 != this.lastClickIndex) {
                        this.dragging = true;
                        this.cursorIndex = iCalculateCursorPosition2;
                        this.selectionAnchor = this.cursorIndex;
                        this.selectionEnd = this.cursorIndex;
                    } else {
                        selectWord(iCalculateCursorPosition2);
                        this.dragging = false;
                    }
                    this.lastClickTime = jCurrentTimeMillis;
                    this.lastClickIndex = iCalculateCursorPosition2;
                    ensureCursorVisible(this.cursorIndex);
                    this.blinkTimer.reset();
                    return true;
                }
            } else if (class706VarAction.release()) {
                this.dragging = false;
            }
        }
        if (class691VarInputEvent instanceof CharInput) {
            CharInput class694Var = (CharInput) class691VarInputEvent;
            if (!this.focused) {
                return false;
            }
            insertCodePoint(class694Var.codePoint());
            this.blinkTimer.reset();
            return true;
        }
        if (!(class691VarInputEvent instanceof KeyInput)) {
            return false;
        }
        KeyInput class696Var = (KeyInput) class691VarInputEvent;
        int iKeyCode = class696Var.keyCode();
        KeyInputAction class704VarKeyAction = class696Var.keyAction();
        if (!this.focused) {
            return false;
        }
        if (!class704VarKeyAction.repeat() && !class704VarKeyAction.press()) {
            return false;
        }
        boolean zHasShiftDown = Screen.hasShiftDown();
        if (Screen.hasControlDown()) {
            switch (iKeyCode) {
                case 65:
                    selectAll();
                    this.blinkTimer.reset();
                    return true;
                case 67:
                    copy();
                    return true;
                case 86:
                    paste();
                    return true;
                case 88:
                    cut();
                    return true;
            }
        }
        switch (iKeyCode) {
            case 256:
            case 257:
                if (!this.focused) {
                    return false;
                }
                this.focused = false;
                clearSelection();
                return true;
            case 258:
            case 260:
            case 264:
            case 265:
            case 266:
            case 267:
            default:
                return false;
            case 259:
                backspace();
                this.blinkTimer.reset();
                return true;
            case 261:
                deleteForward();
                this.blinkTimer.reset();
                return true;
            case 262:
                moveCursor(1, zHasShiftDown);
                this.blinkTimer.reset();
                return true;
            case 263:
                moveCursor(-1, zHasShiftDown);
                this.blinkTimer.reset();
                return true;
            case 268:
                moveToStart(zHasShiftDown);
                this.blinkTimer.reset();
                return true;
            case 269:
                moveToEnd(zHasShiftDown);
                this.blinkTimer.reset();
                return true;
        }
    }

    public void animate(WeightedEngine class141Var) {
        this.hoverAnimation.animate(class141Var);
    }

    public void insertCodePoint(int i) {
        if (!this.focused || Character.isISOControl(i)) {
            return;
        }
        String str = new String(Character.toChars(i));
        if (!this.numbersOnly || str.matches("\\d")) {
            if (this.maxLength > 0) {
                if ((this.textBuffer.length() - (hasSelection() ? selMax() - selMin() : 0)) + str.length() > this.maxLength) {
                    return;
                }
            }
            if (hasSelection()) {
                deleteSelection();
            }
            this.textBuffer.insert(this.cursorIndex, str);
            this.cursorIndex += str.length();
            ensureCursorVisible(this.cursorIndex);
            notifyChange();
            this.cursorVisible = true;
        }
    }

    public void append(String str) {
        this.textBuffer.append(str);
    }

    public void backspace() {
        if (!this.focused || this.textBuffer.isEmpty()) {
            return;
        }
        if (hasSelection()) {
            deleteSelection();
        } else if (this.cursorIndex > 0) {
            this.textBuffer.deleteCharAt(this.cursorIndex - 1);
            this.cursorIndex--;
        }
        ensureCursorVisible(this.cursorIndex > 0 ? this.cursorIndex - 1 : this.cursorIndex);
        notifyChange();
        this.cursorVisible = true;
    }

    public void deleteForward() {
        if (!this.focused || this.textBuffer.isEmpty()) {
            return;
        }
        if (hasSelection()) {
            deleteSelection();
        } else if (this.cursorIndex < this.textBuffer.length()) {
            this.textBuffer.deleteCharAt(this.cursorIndex);
        }
        ensureCursorVisible(this.cursorIndex);
        notifyChange();
        this.cursorVisible = true;
    }

    public void focusAtEnd() {
        this.focused = true;
        this.cursorIndex = this.textBuffer.length();
        clearSelection();
        this.cursorVisible = true;
        this.blinkTimer.reset();
        this.pendingScroll = true;
    }

    public void moveCursor(int i, boolean z) {
        if (this.focused) {
            if (z && this.selectionAnchor == -1) {
                this.selectionAnchor = this.cursorIndex;
                this.selectionEnd = this.cursorIndex;
            }
            if (!z && hasSelection()) {
                this.cursorIndex = i > 0 ? selMax() : selMin();
                clearSelection();
                return;
            }
            this.cursorIndex = Math.max(0, Math.min(this.textBuffer.length(), this.cursorIndex + i));
            if (z) {
                this.selectionEnd = this.cursorIndex;
            } else {
                clearSelection();
            }
            ensureCursorVisible(this.cursorIndex);
            this.cursorVisible = true;
        }
    }

    public void moveToEnd(boolean z) {
        if (this.focused) {
            if (z && this.selectionAnchor == -1) {
                this.selectionAnchor = this.cursorIndex;
                this.selectionEnd = this.cursorIndex;
            }
            this.cursorIndex = this.textBuffer.length();
            if (z) {
                this.selectionEnd = this.cursorIndex;
            } else {
                clearSelection();
            }
            ensureCursorVisible(this.cursorIndex);
            this.cursorVisible = true;
        }
    }

    public void moveToStart(boolean z) {
        if (this.focused) {
            if (z && this.selectionAnchor == -1) {
                this.selectionAnchor = this.cursorIndex;
                this.selectionEnd = this.cursorIndex;
            }
            this.cursorIndex = 0;
            if (z) {
                this.selectionEnd = this.cursorIndex;
            } else {
                clearSelection();
            }
            ensureCursorVisible(this.cursorIndex);
            this.cursorVisible = true;
        }
    }

    public void clearSelection() {
        this.selectionAnchor = -1;
        this.selectionEnd = -1;
    }

    public void selectWord(int i) {
        if (this.textBuffer.isEmpty()) {
            return;
        }
        int i2 = i;
        int i3 = i;
        while (i2 > 0 && this.textBuffer.charAt(i2 - 1) != ' ') {
            i2--;
        }
        while (i3 < this.textBuffer.length() && this.textBuffer.charAt(i3) != ' ') {
            i3++;
        }
        this.selectionAnchor = i2;
        this.selectionEnd = i3;
        this.cursorIndex = i3;
    }

    public void selectAll() {
        this.selectionAnchor = 0;
        this.selectionEnd = this.textBuffer.length();
    }

    public void cut() {
        if (hasSelection()) {
            Mc.INSTANCE.getMinecraft().keyboard.setClipboard(this.textBuffer.substring(selMin(), selMax()));
            deleteSelection();
            notifyChange();
            this.blinkTimer.reset();
            ensureCursorVisible(this.cursorIndex);
        }
    }

    public void paste() {
        String clipboard = Mc.INSTANCE.getMinecraft().keyboard.getClipboard();
        if (clipboard == null || clipboard.isEmpty()) {
            return;
        }
        String strSubstring = clipboard;
        if (this.numbersOnly) {
            strSubstring = clipboard.replaceAll("[^0-9]", "");
        }
        if (strSubstring.isEmpty()) {
            return;
        }
        if (this.maxLength > 0) {
            int length = this.maxLength - (this.textBuffer.length() - (hasSelection() ? selMax() - selMin() : 0));
            if (length <= 0) {
                return;
            }
            if (strSubstring.length() > length) {
                strSubstring = strSubstring.substring(0, length);
            }
        }
        if (hasSelection()) {
            deleteSelection();
        }
        this.textBuffer.insert(this.cursorIndex, strSubstring);
        this.cursorIndex += strSubstring.length();
        notifyChange();
        this.blinkTimer.reset();
        ensureCursorVisible(this.cursorIndex);
    }

    public void clearText() {
        this.textBuffer = new StringBuilder();
        this.cursorIndex = 0;
        clearSelection();
        notifyChange();
    }

    public void copy() {
        if (hasSelection()) {
            Mc.INSTANCE.getMinecraft().keyboard.setClipboard(this.textBuffer.substring(selMin(), selMax()));
        }
    }

    public void notifyChange() {
        this.cursorIndex = Math.min(this.cursorIndex, this.textBuffer.length());
        if (this.changeCallback != null) {
            this.changeCallback.accept(this.textBuffer.toString());
        }
    }

    public void deleteSelection() {
        this.textBuffer.delete(selMin(), selMax());
        this.cursorIndex = selMin();
        this.selectionAnchor = -1;
        this.selectionEnd = -1;
    }

    public String text() {
        return this.textBuffer.toString();
    }

    public boolean isCursorVisible() {
        return this.focused && this.cursorVisible;
    }

    public int selMin() {
        return Math.min(this.selectionAnchor, this.selectionEnd);
    }

    public int selMax() {
        return Math.max(this.selectionAnchor, this.selectionEnd);
    }

    public boolean hasSelection() {
        return (this.selectionAnchor == -1 || this.selectionEnd == -1 || this.selectionAnchor == this.selectionEnd) ? false : true;
    }

    public int calculateCursorPosition(int i) {
        float f = (i - this.textOriginX) + this.viewportOffset;
        int i2 = 0;
        while (i2 < this.textBuffer.length() && measureWidth(this.textBuffer.substring(0, i2 + 1)) <= f) {
            i2++;
        }
        return i2;
    }

    public void ensureCursorVisible(int i) {
        float fMethod005 = measureWidth(this.textBuffer.substring(0, i));
        float f = this.viewportWidth;
        if (f <= 0.0f) {
            this.viewportOffset = 0.0f;
            return;
        }
        if (fMethod005 < this.viewportOffset) {
            this.viewportOffset = fMethod005;
        } else if (fMethod005 > this.viewportOffset + f) {
            this.viewportOffset = fMethod005 - f;
        }
        this.viewportOffset = Math.min(this.viewportOffset, Math.max(0.0f, measureWidth(this.textBuffer.toString()) - f));
        this.viewportOffset = Math.max(0.0f, this.viewportOffset);
    }

    public float measureWidth(String str) {
        if (this.scaleFactor <= 0.0f || this.scaleFactor == 1.0f) {
            return this.font.getWidth(str, this.fontSize);
        }
        return this.font.getWidth(str, Math.max(1, Math.round(this.fontSize * this.scaleFactor))) / this.scaleFactor;
    }

    public boolean focused() {
        return this.focused;
    }

    public TextInputField focused(boolean z) {
        this.focused = z;
        return this;
    }

    public int cursorIndex() {
        return this.cursorIndex;
    }

    public boolean numbersOnly() {
        return this.numbersOnly;
    }

    public TextInputField numbersOnly(boolean z) {
        this.numbersOnly = z;
        return this;
    }

    public int maxLength() {
        return this.maxLength;
    }

    public TextInputField maxLength(int i) {
        this.maxLength = i;
        return this;
    }

    public float viewportOffset() {
        return this.viewportOffset;
    }

    public float viewportWidth() {
        return this.viewportWidth;
    }

    public TextInputField changeText(Consumer<String> consumer) {
        this.changeCallback = consumer;
        return this;
    }

    public ToggleAnimator hoverAnimation() {
        return this.hoverAnimation;
    }
}
