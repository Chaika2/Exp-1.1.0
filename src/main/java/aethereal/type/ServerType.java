package aethereal.type;
import aethereal.util.ChatUtil;
import aethereal.model.DisplayNamed;
import aethereal.model.DuelContext;
import aethereal.event.HandledScreenRenderEvent;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.internal.OffhandItemSwitchMap;
import aethereal.util.ScoreboardHelper;
import aethereal.util.ServerUtil;
import aethereal.math.Stopwatch;
import aethereal.util.StringUtil;
import aethereal.model.Translation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;

public enum ServerType implements DisplayNamed {
    FUNTIME(Translation.clearText("FunTime"), new DuelStrategy() {
        public final Set<String> dueledPlayers = new HashSet();
        public final Stopwatch duelDelayTimer = new Stopwatch();
        public final Stopwatch screenClickTimer = new Stopwatch();

        @Override
        public void doDuelLogic(DuelContext class445Var) {
            Mc class815Var = Mc.INSTANCE;
            Screen currentScreen = class815Var.getCurrentScreen();
            if (!ServerUtil.isConnectedToServer("funtime")) {
                ChatUtil.addChatMessage("Вы должны быть подключены к " + String.valueOf(Formatting.RED) + "FunTime!");
                resetDuelState(class445Var);
                return;
            }
            if (ScoreboardHelper.INSTANCE.headerContains("хаб")) {
                ChatUtil.addChatMessage("Вы должны быть подключены к " + String.valueOf(Formatting.RED) + "анархии!");
                resetDuelState(class445Var);
                return;
            }
            if (ScoreboardHelper.INSTANCE.headerContains("гриферский")) {
                ChatUtil.addChatMessage("Данный модуль работает только на " + String.valueOf(Formatting.RED) + "анархии!");
                resetDuelState(class445Var);
                return;
            }
            if (currentScreen instanceof GenericContainerScreen) {
                return;
            }
            if (this.duelDelayTimer.hasElapsed((long) class445Var.nextDuelDelay().currentValue(), TimeUnit.MILLISECONDS)) {
                ClientWorld world = class815Var.getWorld();
                PlayerEntity player = class815Var.getPlayer();
                AutoDuelArmorType class440VarArmorType = class445Var.armorType();
                AutoDuelOffhandItem class439VarOffhandItem = class445Var.offhandItem();
                for (PlayerEntity playerEntity : world.getPlayers()) {
                    if (playerEntity != player) {
                        String strStripTextFormat = StringHelper.stripTextFormat(playerEntity.getName().getString());
                        if (StringUtil.hasIllegalCharacter(strStripTextFormat)) {
                            return;
                        }
                        if (!this.dueledPlayers.contains(strStripTextFormat) && matchesArmor(playerEntity, class440VarArmorType) && matchesOffhand(playerEntity, class439VarOffhandItem)) {
                            ((ClientPlayerEntity) player).networkHandler.sendChatCommand("duel " + strStripTextFormat);
                            this.dueledPlayers.add(strStripTextFormat);
                            this.duelDelayTimer.reset();
                            return;
                        }
                    }
                }
            }
        }

        public void resetDuelState(DuelContext class445Var) {
            class445Var.callback().deactivateModule();
            this.dueledPlayers.clear();
            this.screenClickTimer.reset();
            this.duelDelayTimer.reset();
        }

        public boolean matchesArmor(PlayerEntity playerEntity, AutoDuelArmorType class440Var) {
            if (class440Var == AutoDuelArmorType.ANY) {
                return playerEntity.getInventory().armor.stream().noneMatch((v0) -> {
                    return v0.isEmpty();
                });
            }
            if (class440Var == AutoDuelArmorType.NONE) {
                return playerEntity.getInventory().armor.stream().allMatch((v0) -> {
                    return v0.isEmpty();
                });
            }
            List<Item> listItems = class440Var.items();
            return playerEntity.getInventory().armor.stream().allMatch(itemStack -> {
                return listItems.contains(itemStack.getItem());
            });
        }

        public boolean matchesOffhand(PlayerEntity playerEntity, AutoDuelOffhandItem class439Var) throws MatchException {
            ItemStack offHandStack = playerEntity.getOffHandStack();
            String string = offHandStack.getName().getString();
            switch (OffhandItemSwitchMap.ordinalToCase[class439Var.ordinal()]) {
                case 1:
                    return offHandStack.getItem() == Items.TOTEM_OF_UNDYING;
                case 2:
                    return offHandStack.getItem() == Items.PLAYER_HEAD && string.toLowerCase().contains("сфера");
                case 3:
                    return true;
                default:
                    throw new MatchException((String) null, (Throwable) null);
            }
        }

        @Override
        public void onScreen(HandledScreenRenderEvent class015Var, DuelContext class445Var) {
            Mc class815Var = Mc.INSTANCE;
            ClientPlayerInteractionManager interactionManager = class815Var.getInteractionManager();
            ClientPlayerEntity player = class815Var.getPlayer();
            if ((class815Var.getCurrentScreen()) instanceof GenericContainerScreen currentScreen ) {
                GenericContainerScreenHandler screenHandler = currentScreen.getScreenHandler();
                if (StringHelper.stripTextFormat(currentScreen.getTitle().getString()).contains("Дуэль") && this.screenClickTimer.hasElapsed(100L, TimeUnit.MILLISECONDS)) {
                    interactionManager.clickSlot(screenHandler.syncId, 40, 0, SlotActionType.QUICK_MOVE, player);
                    this.screenClickTimer.reset();
                }
            }
        }

        @Override
        public void onChat(String str, DuelContext class445Var) {
            if (str.contains("присоединился к дуэли") || str.contains("дуэль начнется")) {
                resetDuelState(class445Var);
            }
        }

        @Override
        public void deactivate() {
            this.dueledPlayers.clear();
            this.screenClickTimer.reset();
            this.duelDelayTimer.reset();
        }
    }),
    REALLYWORLD(Translation.clearText("ReallyWorld"), new DuelStrategy() {
        public final Stopwatch nextDuelTimer = new Stopwatch();
        public final Stopwatch screenActionTimer = new Stopwatch();
        public int playerIndex = 0;

        @Override
        public void doDuelLogic(DuelContext class445Var) {
            Mc class815Var = Mc.INSTANCE;
            ClientPlayerEntity player = class815Var.getPlayer();
            if (!ServerUtil.isConnectedToServer("reallyworld") && !ServerUtil.isConnectedToServer("rwcopy")) {
                ChatUtil.addChatMessage("Вы должны быть подключены к " + String.valueOf(Formatting.RED) + "ReallyWorld!");
                resetDuelState(class445Var);
                return;
            }
            if (ScoreboardHelper.INSTANCE.headerContains("Lobby")) {
                ChatUtil.addChatMessage("Вы должны быть подключены к " + String.valueOf(Formatting.RED) + "анархии/грифу!");
                resetDuelState(class445Var);
                return;
            }
            Screen currentScreen = class815Var.getCurrentScreen();
            ArrayList arrayList = new ArrayList(player.networkHandler.getPlayerList());
            if (arrayList.isEmpty()) {
                this.playerIndex = 0;
                return;
            }
            if (this.playerIndex >= arrayList.size()) {
                this.playerIndex = 0;
                return;
            }
            String strStripTextFormat = StringHelper.stripTextFormat(((PlayerListEntry) arrayList.get(this.playerIndex)).getProfile().getName());
            if (StringUtil.hasIllegalCharacter(strStripTextFormat)) {
                this.playerIndex++;
                return;
            }
            if (!(currentScreen instanceof GenericContainerScreen) && this.nextDuelTimer.hasElapsed((long) class445Var.nextDuelDelay().currentValue(), TimeUnit.MILLISECONDS)) {
                if (strStripTextFormat.equals(Mc.INSTANCE.getSession().getUsername())) {
                    this.playerIndex++;
                    return;
                }
                player.networkHandler.sendChatCommand("duel " + strStripTextFormat);
                this.playerIndex++;
                this.nextDuelTimer.reset();
            }
        }

        @Override
        public void onScreen(HandledScreenRenderEvent class015Var, DuelContext class445Var) {
            Mc class815Var = Mc.INSTANCE;
            ClientPlayerInteractionManager interactionManager = class815Var.getInteractionManager();
            ClientPlayerEntity player = class815Var.getPlayer();
            if ((class815Var.getCurrentScreen()) instanceof GenericContainerScreen currentScreen ) {
                GenericContainerScreenHandler screenHandler = currentScreen.getScreenHandler();
                String strStripTextFormat = StringHelper.stripTextFormat(currentScreen.getTitle().getString());
                if (!strStripTextFormat.contains("Выбор набора (1/1)")) {
                    if (strStripTextFormat.contains("Настройка поединка") && this.screenActionTimer.hasElapsed(200L, TimeUnit.MILLISECONDS)) {
                        interactionManager.clickSlot(screenHandler.syncId, 0, 0, SlotActionType.QUICK_MOVE, player);
                        this.screenActionTimer.reset();
                        return;
                    }
                    return;
                }
                MultiSelectSetting<DuelKitType> class671VarKits = class445Var.kits();
                for (int i = 0; i < screenHandler.getInventory().size(); i++) {
                    ArrayList arrayList = new ArrayList();
                    int i2 = 0;
                    for (DuelKitType class442Var : (DuelKitType[]) class671VarKits.options()) {
                        if (class671VarKits.isSelected(class442Var)) {
                            arrayList.add(Integer.valueOf(i2));
                        }
                        i2++;
                    }
                    Collections.shuffle(arrayList);
                    int iIntValue = ((Integer) arrayList.getFirst()).intValue();
                    if (this.screenActionTimer.hasElapsed(200L, TimeUnit.MILLISECONDS)) {
                        interactionManager.clickSlot(screenHandler.syncId, iIntValue, 0, SlotActionType.PICKUP, player);
                        this.screenActionTimer.reset();
                    }
                }
            }
        }

        @Override
        public void onChat(String str, DuelContext class445Var) {
            if ((str.contains("начало") && str.contains("через") && str.contains("секунд!")) || str.equals("дуэли » во время поединка запрещено использовать команды")) {
                resetDuelState(class445Var);
            } else if (str.contains("для участия в этом поединке у вас недостаточно денег")) {
                resetDuelState(class445Var);
            } else if (str.contains("команды недоступны в pvp режиме")) {
                resetDuelState(class445Var);
            }
        }

        public void resetDuelState(DuelContext class445Var) {
            this.playerIndex = 0;
            class445Var.callback().deactivateModule();
        }

        @Override
        public void deactivate() {
            this.playerIndex = 0;
            this.screenActionTimer.reset();
            this.nextDuelTimer.reset();
        }
    });

    public final Translation displayName;
    public final DuelStrategy strategy;

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }

    public DuelStrategy strategy() {
        return this.strategy;
    }

    ServerType(Translation class254Var, DuelStrategy class444Var) {
        this.displayName = class254Var;
        this.strategy = class444Var;
    }
}
