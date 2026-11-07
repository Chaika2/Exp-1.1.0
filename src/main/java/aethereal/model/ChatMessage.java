package aethereal.model;
import aethereal.render.AvatarCache;
import aethereal.ui.ChatPanel;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.render.GlTexture;
import aethereal.render.ToggleAnimator;

import java.util.concurrent.atomic.AtomicBoolean;

public class ChatMessage {
    public final String id;
    public final String senderUid;
    public final String senderUsername;
    public final String senderAvatarUrl;
    public final String senderRole;
    public final String content;
    public final String createdAt;
    public final ToggleAnimator appear = new ToggleAnimator(220, Easings.EASE_OUT_CUBIC);
    public final AtomicBoolean avatarLoading = new AtomicBoolean();
    public volatile GlTexture avatarTexture;

    public void triggerAppear() {
        this.appear.state(true);
    }

    public void loadAvatar() {
        if (this.senderAvatarUrl == null || this.senderAvatarUrl.isBlank() || !this.avatarLoading.compareAndSet(false, true)) {
            return;
        }
        AvatarCache.load(this.senderAvatarUrl, ChatPanel.defaultAvatarTexture, Expensive.INSTANCE.executor()).whenComplete((class073Var, th) -> {
            if (class073Var != null) {
                this.avatarTexture = class073Var;
            }
            this.avatarLoading.set(false);
        });
    }

    public String id() {
        return this.id;
    }

    public String senderUid() {
        return this.senderUid;
    }

    public String senderUsername() {
        return this.senderUsername;
    }

    public String senderAvatarUrl() {
        return this.senderAvatarUrl;
    }

    public String senderRole() {
        return this.senderRole;
    }

    public String content() {
        return this.content;
    }

    public String createdAt() {
        return this.createdAt;
    }

    public ToggleAnimator appear() {
        return this.appear;
    }

    public AtomicBoolean avatarLoading() {
        return this.avatarLoading;
    }

    public GlTexture avatarTexture() {
        return this.avatarTexture;
    }

    public ChatMessage(String str, String str2, String str3, String str4, String str5, String str6, String str7) {
        this.id = str;
        this.senderUid = str2;
        this.senderUsername = str3;
        this.senderAvatarUrl = str4;
        this.senderRole = str5;
        this.content = str6;
        this.createdAt = str7;
    }

    public ChatMessage avatarTexture(GlTexture class073Var) {
        this.avatarTexture = class073Var;
        return this;
    }
}
