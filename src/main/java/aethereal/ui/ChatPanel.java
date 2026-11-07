package aethereal.ui;
import aethereal.util.AshfieldChatHandler;
import aethereal.model.ChatMessage;
import aethereal.event.ChatSocketListener;
import aethereal.resource.ClasspathResource;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.event.InputEvent;
import aethereal.model.InputEventContext;
import aethereal.model.KeyInput;
import aethereal.Lang;
import aethereal.model.LayoutScaleContext;
import aethereal.model.MouseButtonInput;
import aethereal.render.MsdfFont;
import aethereal.util.StringUtil;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.net.TooltipService;
import aethereal.model.Translation;
import aethereal.net.UserSession;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.joml.Vector4f;
import org.json.JSONArray;
import org.json.JSONObject;

public class ChatPanel extends WidgetContainer implements ChatSocketListener {
    static final GlTexture oldMessagesIcon = loadTexture("/icons/menu/new/arrow_rotation.png");
    public static final GlTexture defaultAvatarTexture = loadTexture("assets/expensive/textures/avatar.png");
    static final GlTexture messageIcon = loadTexture("assets/expensive/icons/menu/new/message_fill.png");
    public final ToggleAnimator openAnimator = ToggleAnimator.times(2);
    public final GlTexture backgroundTexture = loadTexture("/textures/chat_background.png");
    public final AshfieldChatHandler chatHandler = Expensive.INSTANCE.ashfieldChatHandler();
    public final List<ChatMessage> messages = new ArrayList();
    public final Object messagesLock = new Object();

    public final ScrollArea scrollArea = new ScrollArea();
    public final WidgetBounds inputBounds = new WidgetBounds(0.0f, 0.0f, 228.0f, 42.0f);
    public final WidgetBounds tooltipAnchorBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
    public final WidgetBounds oldMessagesButtonBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
    public final TextInputField inputField = new TextInputField(Fonts.INTER_SEMIBOLD.get(), 12);
    public final ClickableBehavior sendButton = new ClickableBehavior();

    public final IconLabelBadge headerBadge = new IconLabelBadge(loadTexture("/icons/menu/new/chat.png"), Lang.CHAT_PLACEHOLDER);

    public final LoadingButtonWidget reconnectButton;
    public boolean atBottom;
    public boolean opened;
    public volatile boolean connected;
    public boolean scrollBottomSmooth;
    public boolean scrollBottomImmediate;
    public boolean reconnecting;
    public boolean oldMessagesTooltipShown;
    public boolean errorTooltipShown;
    public long errorTooltipTime;

    static GlTexture loadTexture(String str) {
        return new GlTexture(new ClasspathResource(str));
    }

    public ChatPanel() {
        this.chatHandler.setListener(this);
        this.sendButton.clickCallback(this::sendCurrentMessage);
        this.reconnectButton = new LoadingButtonWidget(loadTexture("/icons/menu/new/click.png"), 12, Lang.CHAT_RECONNECT, 200, 24, this::reconnect, 8.0f, true);
        this.reconnectButton.loadingVisuals(loadTexture("/icons/menu/new/progress.png"), Lang.ACTION_LOADING);
    }

    @Override
    public void render(DrawCtx class699Var) {
        if (this.inputField.focused()) {
            class699Var.window().interceptKeyboard(true);
        }
        if (this.openAnimator.isZero()) {
            return;
        }
        MsdfFont class161Var = Fonts.INTER_SEMIBOLD.get();
        MsdfFont class161Var2 = Fonts.INTER_BOLD.get();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        float f = MenuWindow.COLLAPSED_HEADER_HEIGHT;
        float fHeight = ((height() - 70.0f) - f) - 10.0f;
        class699Var.texture(this.backgroundTexture, x(), y(), width(), height(), class115VarColorStack.computeColor(16777215));
        class699Var.fillRoundedRect(x(), y(), width(), f, new Vector4f(8.0f, 0.0f, 8.0f, 0.0f), class115VarColorStack.computeColor(16777215, 0.005f));
        class115VarColorStack.push();
        class115VarColorStack.alpha(Math.max(this.openAnimator.smoothAnimation() - 1.0f, 0.0f));
        class699Var.fillRect(x(), y(), 1.0f, height(), class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb()));
        class699Var.text(class161Var, Lang.CHAT.effective(), 16, x() + 16.0f, (y() + (f / 2.0f)) - (class161Var.getHeight(16.0f) / 2.0f), class115VarColorStack.computeColor(class764VarPalette.text().tone(200).argb()));
        this.headerBadge.render(class699Var);
        this.scrollArea.beginArea(class699Var, x(), y() + f + 10.0f, width(), fHeight);
        float fMethod005 = this.connected ? renderMessages(class699Var, y() + f + 10.0f, x() + 10.0f, class161Var, class161Var2, class115VarColorStack, class764VarPalette) : 0.0f;
        if (this.messages.isEmpty() || !this.connected) {
            renderEmptyState(class699Var, y() + f + 10.0f, class161Var, class115VarColorStack, class764VarPalette, fHeight);
        }
        this.scrollArea.endArea(class699Var, fMethod005);
        this.atBottom = fMethod005 <= fHeight || fMethod005 - this.scrollArea.visibleBottom() <= 80.0f;
        if (this.scrollBottomImmediate) {
            this.scrollArea.scrollToBottomImmediate();
            this.scrollBottomSmooth = false;
            this.scrollBottomImmediate = false;
        } else if (this.scrollBottomSmooth) {
            this.scrollArea.scrollToBottomSmooth();
            this.scrollBottomSmooth = false;
        }
        updateScrollTooltip(class699Var, fMethod005, fHeight);
        if (fMethod005 > fHeight && fMethod005 - this.scrollArea.visibleBottom() > 10.0f) {
            class699Var.fillVerticalGradientRect(x(), ((y() + f) + fHeight) - 49.0f, width(), 60.0f, class115VarColorStack.computeColor(987153, 0.0f), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb(), 0.5f));
        }
        int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb());
        class699Var.fillRect(x(), (y() + f) - 1.0f, width(), 1.0f, iComputeColor);
        class699Var.fillRect(x(), (y() + height()) - 70.0f, width(), 1.0f, iComputeColor);
        renderInputArea(class699Var, class161Var, class115VarColorStack, class764VarPalette);
        class115VarColorStack.pop();
        super.render(class699Var);
    }

    public void renderInputArea(DrawCtx class699Var, MsdfFont class161Var, ColorStack class115Var, ThemePalette class764Var) {
        class699Var.fillOutlinedRoundedRect(this.inputBounds.x(), this.inputBounds.y(), this.inputBounds.width(), this.inputBounds.height(), 10.0f, 2.5f, class115Var.computeColor(class764Var.surfaceOutline().tone(600).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(900).argb(), 0.01f));
        String strText = this.inputField.text();
        float fX = this.inputBounds.x() + 14.0f;
        float fY = (this.inputBounds.y() + (this.inputBounds.height() / 2.0f)) - (class161Var.getHeight(12.0f) / 2.0f);
        boolean z = strText.isEmpty() && !this.inputField.focused();
        class699Var.drawEngine().beginStencil();
        class699Var.fillRoundedRect(fX, this.inputBounds.y() + 14.0f, this.inputBounds.width() - 28.0f, this.inputBounds.height() - 28.0f, 8.0f, class115Var.computeColor(16777215));
        class699Var.drawEngine().prepareStencil(1);
        if (this.inputField.hasSelection()) {
            float fViewportOffset = fX - this.inputField.viewportOffset();
            float fTextWidthPhysical = class699Var.textWidthPhysical(class161Var, strText.substring(0, this.inputField.selMin()), 12);
            class699Var.fillRoundedRect(fViewportOffset + fTextWidthPhysical, fY - 1.0f, (class699Var.textWidthPhysical(class161Var, strText.substring(0, this.inputField.selMax()), 12) - fTextWidthPhysical) + 1.0f, class161Var.getHeight(12.0f) + 2.0f, 4.0f, class115Var.computeColor(class764Var.accent().argb(), 128));
        }
        class699Var.text(class161Var, z ? Lang.CHAT_INPUT_PLACEHOLDER.effective() : strText, 12, z ? fX : fX - this.inputField.viewportOffset(), fY, class115Var.computeColor(class764Var.text().tone(z ? 800 : 300).argb()));
        class699Var.drawEngine().endStencil();
        if (this.inputField.isCursorVisible() && !this.inputField.hasSelection()) {
            class699Var.fillRect((fX - this.inputField.viewportOffset()) + class699Var.textWidthPhysical(class161Var, strText.substring(0, Math.min(this.inputField.cursorIndex(), strText.length())), 12), fY - 1.0f, 1.0f, class161Var.getHeight(12.0f) + 2.0f, class115Var.computeColor(class764Var.text().tone(200).argb()));
        }
        class699Var.textureVerticalC(messageIcon, this.inputBounds.right() + 18.0f, this.inputBounds.y() + (this.inputBounds.height() / 2.0f), 18, 18, class115Var.interpolate(class115Var.computeColor(class764Var.text().tone(200).argb()), class115Var.computeColor(class764Var.text().tone(50).argb()), this.sendButton.hoverAnimation()));
        this.sendButton.setDimensions(this.inputBounds.right() + 16.0f, (this.inputBounds.y() + (this.inputBounds.height() / 2.0f)) - 2.0f, 22.0f, 22.0f);
    }

    public float renderMessages(DrawCtx class699Var, float f, float f2, MsdfFont class161Var, MsdfFont class161Var2, ColorStack class115Var, ThemePalette class764Var) {
        ArrayList<ChatMessage> arrayList;
        synchronized (this.messagesLock) {
            arrayList = new ArrayList(this.messages);
        }
        float f3 = 0.0f;
        float fWidth = width() - 52.0f;
        for (ChatMessage class781Var : arrayList) {
            class781Var.loadAvatar();
            class115Var.push();
            class115Var.alpha(class781Var.appear.smoothAnimation());
            String textToFitWidth = StringUtil.formatTextToFitWidth(class781Var.content, fWidth - 20.0f, class161Var, 13);
            float height = 4.0f + class161Var.getHeight(14.0f) + class161Var.getHeightWithLineBreaks(textToFitWidth, 13);
            class699Var.roundedTexture(class781Var.avatarTexture != null ? class781Var.avatarTexture : defaultAvatarTexture, f2, f, 24.0f, 24.0f, 8.0f, class115Var.white());
            class699Var.text(class161Var, class781Var.senderUsername, 14, f2 + 32.0f, f + 2.0f, class115Var.computeColor(class764Var.text().tone(200).argb()));
            class699Var.text(class161Var, textToFitWidth, 13, f2 + 32.0f, f + 4.0f + class161Var.getHeight(14.0f), class115Var.computeColor(class764Var.text().tone(500).argb()));
            class699Var.text(class161Var2, class781Var.createdAt, 10, ((f2 + 32.0f) + fWidth) - class699Var.textWidthPhysical(class161Var2, class781Var.createdAt, 10), ((f + 2.0f) + (class161Var.getHeight(14.0f) / 2.0f)) - (class161Var2.getHeight(10.0f) / 2.0f), class115Var.computeColor(class764Var.text().tone(800).argb()));
            class115Var.pop();
            f += height + 16.0f;
            f3 += height + 16.0f;
        }
        return f3;
    }

    public void renderEmptyState(DrawCtx class699Var, float f, MsdfFont class161Var, ColorStack class115Var, ThemePalette class764Var, float f2) {
        String[] strArrSplit = (this.connected ? Lang.CHAT_EMPTY_HISTORY : Lang.CHAT_NO_CONNECTION).effective().split("\n");
        float height = class161Var.getHeight(11.0f);
        float length = f + ((f2 - (((strArrSplit.length * (height + 2.0f)) - 2.0f) + (this.connected ? 0 : 36))) / 2.0f);
        for (String str : strArrSplit) {
            class699Var.text(class161Var, str, 11, (x() + (width() / 2.0f)) - (class699Var.textWidthPhysical(class161Var, str, 11) / 2.0f), length, class115Var.computeColor(class764Var.text().tone(600).argb()));
            length += height + 2.0f;
        }
        if (this.connected) {
            return;
        }
        this.reconnectButton.baseColor(class115Var.computeColor(class764Var.accent().argb()));
        this.reconnectButton.textColor(class115Var.computeColor(ThemePalette.white.argb()));
        this.reconnectButton.outlineColor(class115Var.computeColor(class764Var.accentBright().argb()));
        this.reconnectButton.render(class699Var);
    }

    public void updateScrollTooltip(DrawCtx class699Var, float f, float f2) {
        if (this.errorTooltipShown && System.currentTimeMillis() - this.errorTooltipTime > 3000) {
            hideTooltip();
            this.errorTooltipShown = false;
        }
        if (this.errorTooltipShown) {
            updateTooltipAnchor(class699Var);
            return;
        }
        TooltipService class736Var = Expensive.INSTANCE.menuWindow().tooltipService();
        if (class736Var == null) {
            return;
        }
        if (!(f > f2 && f - this.scrollArea.visibleBottom() > 600.0f)) {
            if (class736Var.isOwnedBy(this)) {
                class736Var.hide(this);
                this.oldMessagesTooltipShown = false;
                return;
            }
            return;
        }
        if (!class736Var.isOwnedBy(this)) {
            class736Var.show(this, this.tooltipAnchorBounds, Lang.CHAT_OLD_MESSAGES, oldMessagesIcon);
        }
        updateTooltipAnchor(class699Var);
        this.oldMessagesTooltipShown = true;
        WidgetBounds screenRect = TooltipService.toScreenRect(class699Var, this.tooltipAnchorBounds.x(), this.tooltipAnchorBounds.y(), this.tooltipAnchorBounds.width(), this.tooltipAnchorBounds.height());
        float fTextWidthPhysical = 30.0f + class699Var.textWidthPhysical(Fonts.INTER_BOLD.get(), Lang.CHAT_OLD_MESSAGES.effective(), 11);
        float fMax = 8.0f + Math.max(Fonts.INTER_BOLD.get().getHeight(11.0f), 12.0f);
        this.oldMessagesButtonBounds.withPosition((screenRect.x() + (screenRect.width() / 2.0f)) - (fTextWidthPhysical / 2.0f), (screenRect.y() - fMax) - 10.0f).withSize(fTextWidthPhysical, fMax);
    }

    public void updateTooltipAnchor(DrawCtx class699Var) {
        TooltipService class736Var = Expensive.INSTANCE.menuWindow().tooltipService();
        if (class736Var == null || !class736Var.isOwnedBy(this)) {
            return;
        }
        class736Var.updateAnchor(this, this.tooltipAnchorBounds, class699Var);
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        setSize(300.0f * Math.min(this.openAnimator.smoothAnimation(), 1.0f), MenuWindow.MENU_HEIGHT);
        float f = MenuWindow.COLLAPSED_HEADER_HEIGHT;
        this.inputBounds.withPosition(x() + 14.0f, ((y() + height()) - 35.0f) - (this.inputBounds.height() / 2.0f));
        this.headerBadge.layout(class698Var);
        this.headerBadge.setPosition(((x() + width()) - 16.0f) - this.headerBadge.width(), (y() + (f / 2.0f)) - (this.headerBadge.height() / 2.0f));
        this.inputField.listen(this.inputBounds.x(), this.inputBounds.y(), this.inputBounds.width(), this.inputBounds.height(), this.inputBounds.width() - 28.0f, this.inputBounds.x() + 14.0f, 1.0f);
        this.tooltipAnchorBounds.withPosition(x(), this.inputBounds.y() - 12.0f).withSize(width(), this.inputBounds.height() + 24.0f);
        String[] strArrSplit = (this.connected ? Lang.CHAT_EMPTY_HISTORY : Lang.CHAT_NO_CONNECTION).effective().split("\n");
        MsdfFont class161Var = Fonts.INTER_SEMIBOLD.get();
        float fY = y() + f + 10.0f;
        float fHeight = ((height() - 70.0f) - f) - 10.0f;
        float length = (strArrSplit.length * (class161Var.getHeight(11.0f) + 2.0f)) - 2.0f;
        this.reconnectButton.setPosition((x() + (width() / 2.0f)) - 100.0f, fY + (((fHeight - length) - (this.connected ? 0 : 36)) / 2.0f) + length + 12.0f);
        this.reconnectButton.layout(class698Var);
        super.layout(class698Var);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        if (!this.opened) {
            return false;
        }
        boolean zHandleInput = super.handleInput(class688Var, z);
        if (!z) {
            InputEvent class691VarInputEvent = class688Var.inputEvent();
            if (class691VarInputEvent instanceof KeyInput) {
                KeyInput class696Var = (KeyInput) class691VarInputEvent;
                if (this.inputField.focused() && ((class696Var.keyAction().press() || class696Var.keyAction().repeat()) && class696Var.keyCode() == 257 && !this.inputField.text().trim().isEmpty())) {
                    return sendCurrentMessage();
                }
            }
        }
        if (!z && !zHandleInput && this.oldMessagesTooltipShown) {
            InputEvent class691VarInputEvent2 = class688Var.inputEvent();
            if (class691VarInputEvent2 instanceof MouseButtonInput) {
                MouseButtonInput class693Var = (MouseButtonInput) class691VarInputEvent2;
                if (class693Var.action().press() && class693Var.button() == 0 && class688Var.inPhysicalArea(class688Var.logicalMousePosition(), this.oldMessagesButtonBounds.x(), this.oldMessagesButtonBounds.y(), this.oldMessagesButtonBounds.width(), this.oldMessagesButtonBounds.height())) {
                    this.scrollBottomImmediate = true;
                    hideTooltip();
                    return true;
                }
            }
        }
        if (!z && !zHandleInput && !this.connected) {
            zHandleInput = this.reconnectButton.handleInput(class688Var, zHandleInput);
        }
        return zHandleInput | this.sendButton.handleInput(class688Var, zHandleInput) | this.inputField.handleInput(class688Var, zHandleInput) | this.scrollArea.handleInput(class688Var, zHandleInput);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.openAnimator.animate(class141Var);
        this.openAnimator.state(this.opened);
        this.sendButton.animate(class141Var);
        this.scrollArea.animation(class141Var);
        this.reconnectButton.animation(class141Var);
        if ((!this.opened || width() < 300.0f) && (this.oldMessagesTooltipShown || this.errorTooltipShown)) {
            hideTooltip();
        }
        synchronized (this.messagesLock) {
            this.messages.forEach(class781Var -> {
                class781Var.appear.animate(class141Var);
            });
        }
        super.animation(class141Var);
    }

    public boolean sendCurrentMessage() {
        String strTrim = this.inputField.text().trim();
        if (strTrim.isEmpty() || !this.connected) {
            return false;
        }
        this.chatHandler.sendMessage(strTrim);
        this.inputField.clearText();
        this.inputField.focusAtEnd();
        this.scrollBottomImmediate = true;
        return true;
    }

    public void reconnect() {
        if (this.connected || this.reconnecting || Expensive.INSTANCE.userSession() == null) {
            return;
        }
        this.reconnecting = true;
        this.reconnectButton.loading(true);
        UserSession class385VarUserSession = Expensive.INSTANCE.userSession();
        this.chatHandler.createUser(String.valueOf(class385VarUserSession.uid()), class385VarUserSession.username(), class385VarUserSession.avatarUrl(), class385VarUserSession.role());
        this.chatHandler.reconnect();
    }

    public void invertOpenState() {
        this.opened = !this.opened;
        if (this.opened) {
            float fMethod002 = computeMessagesHeight();
            if (fMethod002 <= ((height() - 70.0f) - MenuWindow.COLLAPSED_HEADER_HEIGHT) - 10.0f || fMethod002 - this.scrollArea.visibleBottom() > 100.0f) {
                this.scrollBottomImmediate = true;
            }
        }
    }

    public float computeMessagesHeight() {
        if (!this.connected) {
            return 0.0f;
        }
        synchronized (this.messagesLock) {
            if (this.messages.isEmpty()) {
                return 0.0f;
            }
            MsdfFont class161Var = Fonts.INTER_SEMIBOLD.get();
            float height = 0.0f;
            synchronized (this.messagesLock) {
                Iterator<ChatMessage> it = this.messages.iterator();
                while (it.hasNext()) {
                    height += 4.0f + class161Var.getHeight(14.0f) + class161Var.getHeightWithLineBreaks(StringUtil.formatTextToFitWidth(it.next().content, 228.0f, class161Var, 13), 13) + 16.0f;
                }
            }
            return height;
        }
    }

    public void hideTooltip() {
        TooltipService class736Var = Expensive.INSTANCE.menuWindow().tooltipService();
        if (class736Var != null && class736Var.isOwnedBy(this)) {
            class736Var.hide(this);
        }
        this.errorTooltipShown = false;
        this.oldMessagesTooltipShown = false;
    }

    public ChatMessage parseMessage(JSONObject jSONObject) {
        return new ChatMessage(jSONObject.getString("id"), jSONObject.getString("senderUid"), jSONObject.getString("senderUsername"), jSONObject.optString("senderAvatarUrl", null), jSONObject.optString("senderRole", ""), jSONObject.getString("content"), DateTimeFormatter.ofPattern("HH:mm").format(Instant.parse(jSONObject.getString("createdAt")).atZone(ZoneId.systemDefault())));
    }

    @Override
    public void onConnected() {
        this.connected = true;
        this.reconnecting = false;
        this.reconnectButton.loading(false);
    }

    @Override
    public void onDisconnected() {
        this.connected = false;
        this.reconnecting = false;
        this.reconnectButton.loading(false);
    }

    @Override
    public void onUserCountUpdate(int i) {
    }

    @Override
    public void onMessageHistory(JSONArray jSONArray) {
        if (this.connected) {
            ArrayList arrayList = new ArrayList();
            for (int iMax = Math.max(0, jSONArray.length() - 200); iMax < jSONArray.length(); iMax++) {
                try {
                    ChatMessage class781VarMethod008 = parseMessage(jSONArray.getJSONObject(iMax));
                    class781VarMethod008.triggerAppear();
                    class781VarMethod008.loadAvatar();
                    arrayList.add(class781VarMethod008);
                } catch (Exception e) {
                }
            }
            synchronized (this.messagesLock) {
                this.messages.clear();
                this.messages.addAll(arrayList);
            }
            this.scrollBottomImmediate = true;
        }
    }

    @Override
    public void onNewMessage(JSONObject jSONObject) {
        ChatMessage class781VarMethod008 = parseMessage(jSONObject);
        class781VarMethod008.triggerAppear();
        synchronized (this.messagesLock) {
            this.messages.add(class781VarMethod008);
            while (this.messages.size() > 200) {
                this.messages.removeFirst();
            }
        }
        this.scrollBottomSmooth = this.opened && this.connected && this.atBottom;
    }

    @Override
    public void onError(String str) {
        if (!this.connected && this.reconnecting) {
            this.reconnecting = false;
            this.reconnectButton.loading(false);
        }
        if (this.opened && this.connected) {
            TooltipService class736Var = Expensive.INSTANCE.menuWindow().tooltipService();
            if (class736Var != null) {
                if (this.oldMessagesTooltipShown) {
                    class736Var.hide(this);
                    this.oldMessagesTooltipShown = false;
                }
                class736Var.show(this, this.tooltipAnchorBounds, Translation.clearText(str), messageIcon);
            }
            this.errorTooltipShown = true;
            this.errorTooltipTime = System.currentTimeMillis();
        }
    }

    public boolean isOpened() {
        return this.opened;
    }
}
