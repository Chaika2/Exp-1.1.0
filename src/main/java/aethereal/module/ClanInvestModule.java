package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.Expensive;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.type.NotificationType;
import aethereal.ui.setting.NumberSetting;
import aethereal.event.PacketReceiveEvent;
import aethereal.event.PlayerTickEvent;
import aethereal.util.ServerUtil;
import aethereal.type.SettingUnit;
import aethereal.ui.setting.TextFieldSetting;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;

@Aliases(aliases = {"Clan Invest", "Funtime", "Auto Invest"})
public class ClanInvestModule extends Module {
    public final TextFieldSetting thresholdSetting;
    public final NumberSetting percentageSetting;
    public boolean investedThisCycle;
    public boolean awaitingConfirmation;
    public int pendingAmount;
    public long confirmationDeadline;
    static final long confirmationTimeoutMs = 4000;

    public ClanInvestModule() {
        super(ModuleTab.MISC, "Clan Invest");
        this.thresholdSetting = new TextFieldSetting(Lang.CLAN_INVEST_CURRENCY_THRESHOLD, Lang.CLAN_INVEST_CURRENCY_THRESHOLD_DESC).setOnlyDigits(true).setPlaceholder(Lang.CLAN_INVEST_CURRENCY_THRESHOLD_PLACEHOLDER).setText(String.valueOf(1000000));
        this.percentageSetting = new NumberSetting(Lang.CLAN_INVEST_INVEST_PERCENTAGE).currentValue(30.0f).range(1.0f, 100.0f).step(1.0f).unit(SettingUnit.PERCENTS);
        this.investedThisCycle = false;
        this.awaitingConfirmation = false;
        this.pendingAmount = 0;
        this.confirmationDeadline = 0L;
        addSettings(this.thresholdSetting, this.percentageSetting);
        register(PacketReceiveEvent.class, class051Var -> {
            Mc class815Var = Mc.INSTANCE;
            if (isState() && class815Var.isWorldLoaded()) {
                if ((class051Var.getPacket()) instanceof GameMessageS2CPacket packet ) {
                    String lowerCase = packet.content().getString().toLowerCase(Locale.ROOT);
                    boolean zContains = lowerCase.contains("/clan create - создать клан ($10000)");
                    boolean zContains2 = lowerCase.contains("вы не можете пополнить баланс клана");
                    if (zContains) {
                        Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal("Вы не состоите в клане. Инвестиции невозможны."), 6L, TimeUnit.SECONDS);
                        setState(false);
                        return;
                    }
                    if (zContains2) {
                        Expensive.INSTANCE.notificationRepository().post(NotificationType.INFO, (Text) Text.literal("Включите модуль, когда вы сможете пополнить баланс клана."), 6L, TimeUnit.SECONDS);
                        setState(false);
                        return;
                    }
                    boolean zContains3 = lowerCase.contains("пополнил баланс казны");
                    if (this.awaitingConfirmation && zContains3) {
                        Expensive.INSTANCE.notificationRepository().post(NotificationType.SUCCESS, (Text) Text.literal("Инвестировано %s$ в клан.".formatted(Integer.valueOf(this.pendingAmount))), 6L, TimeUnit.SECONDS);
                        this.awaitingConfirmation = false;
                        this.pendingAmount = 0;
                        this.confirmationDeadline = 0L;
                        this.investedThisCycle = true;
                    }
                }
            }
        });
        register(PlayerTickEvent.class, class130Var -> {
            Mc class815Var = Mc.INSTANCE;
            if (isState() && class815Var.isWorldLoaded() && class130Var.isPre()) {
                if (!ServerUtil.isConnectedToServer("funtime")) {
                    Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal("Данный модуль работает только на Funtime!"), 3L, TimeUnit.SECONDS);
                    switchState();
                    return;
                }
                if (this.awaitingConfirmation && System.currentTimeMillis() > this.confirmationDeadline) {
                    this.awaitingConfirmation = false;
                    this.pendingAmount = 0;
                    this.confirmationDeadline = 0L;
                    Expensive.INSTANCE.notificationRepository().post(NotificationType.INFO, (Text) Text.literal("Не получено подтверждение инвестиции (таймаут)."), 4L, TimeUnit.SECONDS);
                }
                Optional<Integer> currentBalance = getCurrentBalance();
                if (currentBalance.isEmpty()) {
                    return;
                }
                int iIntValue = currentBalance.get().intValue();
                try {
                    int i = Integer.parseInt(this.thresholdSetting.getText());
                    if (i <= 0) {
                        Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal("Порог должен быть больше нуля."), 3L, TimeUnit.SECONDS);
                        switchState();
                        return;
                    }
                    if (iIntValue < i) {
                        this.investedThisCycle = false;
                        return;
                    }
                    if (this.investedThisCycle || this.awaitingConfirmation) {
                        return;
                    }
                    int iMax = (int) Math.max(1L, Math.min(2147483647L, (((long) iIntValue) * ((long) ((int) this.percentageSetting.currentValue()))) / 100));
                    class815Var.getPlayer().networkHandler.sendChatCommand("clan invest " + iMax);
                    this.awaitingConfirmation = true;
                    this.pendingAmount = iMax;
                    this.confirmationDeadline = System.currentTimeMillis() + confirmationTimeoutMs;
                } catch (NumberFormatException e) {
                    Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal("Неверный формат порога валюты!"), 3L, TimeUnit.SECONDS);
                    switchState();
                }
            }
        });
    }

    @Override
    public void applyState() {
        this.awaitingConfirmation = false;
        this.pendingAmount = 0;
        this.confirmationDeadline = 0L;
        this.investedThisCycle = false;
        super.applyState();
    }

    @Override
    public void deactivate() {
        this.investedThisCycle = false;
        this.awaitingConfirmation = false;
        this.pendingAmount = 0;
        this.confirmationDeadline = 0L;
        super.deactivate();
    }

    public Optional<Integer> getCurrentBalance() {
        Scoreboard scoreboard;
        ScoreboardObjective objectiveForSlot;
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        if (player != null && (objectiveForSlot = (scoreboard = player.getScoreboard()).getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR)) != null) {
            return scoreboard.getScoreboardEntries(objectiveForSlot).stream().filter(scoreboardEntry -> {
                return !scoreboardEntry.hidden();
            }).map(scoreboardEntry2 -> {
                return (Integer) Team.decorateName(scoreboard.getScoreHolderTeam(scoreboardEntry2.owner()), scoreboardEntry2.name()).getSiblings().stream().map((v0) -> {
                    return v0.getString();
                }).filter(str -> {
                    return str.toLowerCase(Locale.ROOT).replace('o', (char) 1086).replace('e', (char) 1077).contains("монет") || str.toLowerCase(Locale.ROOT).replace('a', (char) 1072).replace('c', (char) 1089).contains("баланс");
                }).map(this::parseDigits).flatMap((v0) -> {
                    return v0.stream();
                }).findFirst().orElse(null);
            }).filter((v0) -> {
                return Objects.nonNull(v0);
            }).findFirst();
        }
        return Optional.empty();
    }

    public Optional<Integer> parseDigits(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(c);
            }
        }
        if (sb.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.valueOf(Integer.parseInt(sb.toString())));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
