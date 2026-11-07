package aethereal.ui;
import aethereal.render.GlTexture;


public final class SearchTypeBadge {
    public final String text;
    public final GlTexture texture;

    public SearchTypeBadge(String str, GlTexture class073Var) {
        this.text = str;
        this.texture = class073Var;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "text=" + this.text + ", " + "texture=" + this.texture + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.text, this.texture);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SearchTypeBadge)) return false;
        SearchTypeBadge o = (SearchTypeBadge) obj;
        return java.util.Objects.equals(this.text, o.text) && java.util.Objects.equals(this.texture, o.texture);
    }
public String text() {
        return this.text;
    }

    public GlTexture texture() {
        return this.texture;
    }
}
