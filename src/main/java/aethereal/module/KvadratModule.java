package aethereal.module;
import aethereal.render.ColorStack;
import aethereal.util.DrawEngine;
import aethereal.Expensive;
import aethereal.ui.ModuleTab;
import aethereal.event.Render2DEvent;
import aethereal.render.ScaledRenderTarget;

import org.joml.Matrix4f;

public class KvadratModule extends Module {
    ScaledRenderTarget renderTarget;

    public KvadratModule() {
        super(ModuleTab.RENDER, "Kvadrat");
        this.renderTarget = new ScaledRenderTarget(2);
        register(Render2DEvent.class, class311Var -> {
            if (isState() && class311Var.isPost()) {
                Matrix4f positionMatrix = class311Var.matrixStack().peek().getPositionMatrix();
                DrawEngine class154VarDrawEngine = Expensive.INSTANCE.drawEngine();
                ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
                this.renderTarget.add(() -> {
                    class154VarDrawEngine.begin();
                    class154VarDrawEngine.roundedRectangle(positionMatrix, 150.0f, 150.0f, 100.0f, 100.0f, 15.0f, class115VarColorStack.black());
                    class154VarDrawEngine.end();
                });
                this.renderTarget.renderToFramebuffer();
            }
        });
    }
}
