package aethereal.ui;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.model.LayoutScaleContext;
import aethereal.render.MsdfFont;
import aethereal.render.ThemePalette;
import aethereal.net.UserSession;

public class UserProfileBadge extends AbstractWidget {
    public final MsdfFont font = Fonts.INTER_SEMIBOLD.get();

    @Override
    public void render(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        UserSession class385VarUserSession = Expensive.INSTANCE.userSession();
        String strUsername = class385VarUserSession.username();
        String strExpire = class385VarUserSession.expire();
        GlTexture class073VarTexture = class385VarUserSession.texture();
        class073VarTexture.magFilter(9728);
        class073VarTexture.minFilter(9728);
        class699Var.roundedTexture(class073VarTexture, x(), y(), 24.0f, 24.0f, 7.0f, class115VarColorStack.white());
        float fY = y() + ((24.0f - ((this.font.getHeight(12.0f) + 1.0f) + this.font.getHeight(11.0f))) / 2.0f);
        class699Var.text(this.font, strUsername, 12, x() + 24.0f + 6.0f, fY, class115VarColorStack.computeColor(class764VarPalette.text().tone(200).argb()));
        class699Var.text(this.font, "Till " + strExpire, 11, x() + 24.0f + 6.0f, fY + this.font.getHeight(12.0f) + 1.0f, class115VarColorStack.computeColor(class764VarPalette.accent().argb()));
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        UserSession class385VarUserSession = Expensive.INSTANCE.userSession();
        String strUsername = class385VarUserSession.username();
        String strExpire = class385VarUserSession.expire();
        setSize(30.0f + Math.max(class698Var.textWidthPhysical(this.font, "Till " + strExpire, 10), class698Var.textWidthPhysical(this.font, strUsername, 12)) + 8.0f, 24.0f);
    }
}
