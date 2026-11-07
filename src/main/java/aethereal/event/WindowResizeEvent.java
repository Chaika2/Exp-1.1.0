package aethereal.event;


public final class WindowResizeEvent implements Event {
    public final int width;
    public final int height;

    public WindowResizeEvent(int i, int i2) {
        this.width = i;
        this.height = i2;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "width=" + this.width + ", " + "height=" + this.height + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.width, this.height);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WindowResizeEvent)) return false;
        WindowResizeEvent o = (WindowResizeEvent) obj;
        return java.util.Objects.equals(this.width, o.width) && java.util.Objects.equals(this.height, o.height);
    }
public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }
}
