package aethereal.ui;
import aethereal.net.CloudConfigMetadata;
import aethereal.render.GlTexture;
import aethereal.model.StaffProfile;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CloudConfigCard {
    public String name;
    public final String cloudId;
    public final String date;
    public final long lastModified;
    public StaffProfile author;

    public CloudConfigCard(CloudConfigMetadata class163Var, GlTexture class073Var) {
        this.name = orEmpty(class163Var.name());
        this.date = formatDate(class163Var.createdTimestamp());
        this.lastModified = class163Var.updatedTimestamp();
        this.cloudId = class163Var.id();
        this.author = new StaffProfile(orEmpty(class163Var.author()), "", class073Var, class163Var.authorAvatarUrl());
    }

    public static String orEmpty(String str) {
        return str == null ? "" : str;
    }

    public String formatDate(long j) {
        return new SimpleDateFormat("dd.MM.yyyy").format(new Date(j));
    }

    public String name() {
        return this.name;
    }

    public String cloudId() {
        return this.cloudId;
    }

    public String date() {
        return this.date;
    }

    public long lastModified() {
        return this.lastModified;
    }

    public StaffProfile author() {
        return this.author;
    }

    public CloudConfigCard name(String str) {
        this.name = str;
        return this;
    }

    public CloudConfigCard author(StaffProfile class629Var) {
        this.author = class629Var;
        return this;
    }
}
