package aethereal.model;

public final class OffsetInputContext extends InputEventContext {
    public final InputEventContext delegate;
    public final float offsetX;
    public final float offsetY;

    public OffsetInputContext(InputEventContext class688Var, float f, float f2) {
        super(class688Var.inputEvent(), class688Var.currentMousePosition(), class688Var.scaleFactor());
        this.delegate = class688Var;
        this.offsetX = f;
        this.offsetY = f2;
    }

    @Override
    public PixelPoint currentMousePosition() {
        return this.delegate.currentMousePosition();
    }

    @Override
    public PixelPoint logicalMousePosition() {
        PixelPoint class708VarLogicalMousePosition = this.delegate.logicalMousePosition();
        return new PixelPoint(class708VarLogicalMousePosition.x() + this.offsetX, class708VarLogicalMousePosition.y() + this.offsetY);
    }

    @Override
    public boolean inArea(float f, float f2, float f3, float f4) {
        PixelPoint class708VarLogicalMousePosition = logicalMousePosition();
        return ((float) class708VarLogicalMousePosition.x()) >= f && ((float) class708VarLogicalMousePosition.y()) >= f2 && ((float) class708VarLogicalMousePosition.x()) <= f + f3 && ((float) class708VarLogicalMousePosition.y()) <= f2 + f4;
    }
}
