package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.model.Translation;

public enum CameraMode implements DisplayNamed {
    ZOOM(Translation.clearText("Zoom")),
    PERSPECTIVE(Translation.clearText("Perspective"));

    final Translation displayName;

    CameraMode(Translation class254Var) {
        this.displayName = class254Var;
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }
}
