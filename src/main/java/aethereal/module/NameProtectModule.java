package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.ui.setting.BooleanSetting;
import aethereal.event.ChatSendEvent;
import aethereal.util.FriendManager;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.TextFieldSetting;

import java.util.Iterator;
import java.util.Objects;

@Aliases(aliases = {"Protect Name", "Name Protect", "Protect Friends", "Friend Protect", "Hide name", "Hide Friends", "Obfuscation"})
public class NameProtectModule extends Module {
    public final TextFieldSetting changedName;
    public final BooleanSetting hideFriends;
    public final TextFieldSetting friendName;

    public NameProtectModule() {
        super(ModuleTab.MISC, "Name Protect");
        this.changedName = new TextFieldSetting(Lang.NAMEPROTECT_CHANGEDNAME).setMax(16).setPlaceholder(Lang.NAMEPROTECT_CHANGEDNAME_PLACEHOLDER).setText("dedinside");
        this.hideFriends = new BooleanSetting(Lang.NAMEPROTECT_HIDEFRIENDS, Lang.NAMEPROTECT_HIDEFRIENDS_DESC);
        TextFieldSetting text = new TextFieldSetting(Lang.NAMEPROTECT_FRIENDNAME, Lang.NAMEPROTECT_FRIENDNAME_DESC).setMax(16).setPlaceholder(Lang.NAMEPROTECT_FRIENDNAME_PLACEHOLDER).setText("балванчик");
        BooleanSetting class665Var = this.hideFriends;
        Objects.requireNonNull(class665Var);
        this.friendName = (TextFieldSetting) text.setVisible(class665Var::isValue);
        addSettings(this.changedName, this.hideFriends, this.friendName);
        register(ChatSendEvent.class, class093Var -> {
            Mc class815Var = Mc.INSTANCE;
            if (isState() && class815Var.isWorldLoaded()) {
                String originalText = class093Var.getOriginalText();
                String strReplace = originalText.replace(class815Var.getSession().getUsername(), this.changedName.getText());
                if (this.hideFriends.isValue()) {
                    Iterator<String> it = FriendManager.getFriends().iterator();
                    while (it.hasNext()) {
                        strReplace = strReplace.replace(it.next(), this.friendName.getText());
                    }
                }
                if (strReplace.equals(originalText)) {
                    return;
                }
                class093Var.setChangedText(strReplace);
                class093Var.cancel();
            }
        });
    }

    public String replace(String str) {
        if (!isState()) {
            return str;
        }
        String strReplace = str.replace(Mc.INSTANCE.getSession().getUsername(), this.changedName.getText());
        if (this.hideFriends.isValue()) {
            Iterator<String> it = FriendManager.getFriends().iterator();
            while (it.hasNext()) {
                strReplace = strReplace.replace(it.next(), this.friendName.getText());
            }
        }
        return !strReplace.equals(str) ? strReplace : str;
    }
}
