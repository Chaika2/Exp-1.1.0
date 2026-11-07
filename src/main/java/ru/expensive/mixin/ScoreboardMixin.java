package ru.expensive.mixin;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Scoreboard.class})
public class ScoreboardMixin {
    @Inject(method = {"removeScoreHolderFromTeam"}, at = {@At("HEAD")}, cancellable = true)
    private void onRemoveScoreHolderFromTeam(String str, Team team, CallbackInfo callbackInfo) {
        if (((Scoreboard) (Object) this).getScoreHolderTeam(str) != team) {
            callbackInfo.cancel();
        }
    }
}
