package aethereal.util;
import aethereal.type.Mc;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;

public class ScoreboardHelper {
    public static final ScoreboardHelper INSTANCE = new ScoreboardHelper();
    public Text header;

    public String getHeaderAsString() {
        return this.header == null ? "" : this.header.getString();
    }

    public boolean headerContains(String str) {
        return getHeaderAsString().toLowerCase().contains(str);
    }

    public float getHealthBelowName(LivingEntity livingEntity) {
        PlayerEntity clientPlayerEntity;
        ScoreAccess orCreateScore;
        if ((livingEntity instanceof PlayerEntity) && (clientPlayerEntity = (PlayerEntity) livingEntity) != Mc.INSTANCE.getPlayer()) {
            ClientWorld world = Mc.INSTANCE.getWorld();
            ScoreboardObjective objectiveForSlot = world.getScoreboard().getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);
            if (objectiveForSlot != null && (orCreateScore = world.getScoreboard().getOrCreateScore(ScoreHolder.fromName(livingEntity.getNameForScoreboard()), objectiveForSlot)) != null) {
                return orCreateScore.getScore() == 0 ? clientPlayerEntity.getHealth() : orCreateScore.getScore();
            }
        }
        return livingEntity.getHealth();
    }

    public float getEntityHealth(LivingEntity livingEntity) {
        return livingEntity.getHealth();
    }

    public float getRealHealth(LivingEntity livingEntity) {
        float scoreboardHealth = getHealthBelowName(livingEntity);
        float entityHealth = livingEntity.getHealth();
        return Math.max(scoreboardHealth, entityHealth);
    }

    public void setHeader(Text text) {
        this.header = text;
    }

    public Text getHeader() {
        return this.header;
    }
}
