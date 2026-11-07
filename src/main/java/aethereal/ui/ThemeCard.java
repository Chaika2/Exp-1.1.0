package aethereal.ui;
import aethereal.type.ConfigOrigin;
import aethereal.render.GlTexture;
import aethereal.model.StaffProfile;
import aethereal.render.Theme;
import aethereal.type.ThemeMode;
import aethereal.render.ThemePalette;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ThemeCard {
    public String name;
    public ThemeMode type;

    public ConfigOrigin kind;

    public ThemePalette palette;
    public final String cloudId;
    public final String date;
    public final Theme theme;

    public StaffProfile author;

    public ThemeCard(Theme class760Var, GlTexture class073Var) {
        this.theme = class760Var;
        this.name = class760Var.name();
        this.type = class760Var.type();
        this.kind = class760Var.kind();
        this.palette = class760Var.palette();
        this.date = formatDate(Timestamp.from(class760Var.createdAt()).getTime());
        this.cloudId = class760Var.id();
        this.author = new StaffProfile(class760Var.owner(), "", class073Var, null);
    }

    public String formatDate(long j) {
        return new SimpleDateFormat("dd.MM.yyyy").format(new Date(j));
    }

    public String name() {
        return this.name;
    }

    public ThemeMode type() {
        return this.type;
    }

    public ConfigOrigin kind() {
        return this.kind;
    }

    public ThemePalette palette() {
        return this.palette;
    }

    public String cloudId() {
        return this.cloudId;
    }

    public String date() {
        return this.date;
    }

    public StaffProfile author() {
        return this.author;
    }

    public ThemeCard(String str, String str2) {
        this.theme = null;
        this.cloudId = str;
        this.date = str2;
    }

    public ThemeCard name(String str) {
        this.name = str;
        return this;
    }

    public Theme theme() {
        return this.theme;
    }
}
