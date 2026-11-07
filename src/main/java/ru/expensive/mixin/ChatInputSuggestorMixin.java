package ru.expensive.mixin;

import aethereal.Expensive;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ChatInputSuggestor.class})
public abstract class ChatInputSuggestorMixin {

    @Shadow
    @Final
    MinecraftClient field_21597;

    @Shadow
    @Final
    TextFieldWidget field_21599;

    @Shadow
    private CompletableFuture<Suggestions> field_21611;

    @Shadow
    private ChatInputSuggestor.SuggestionWindow field_21612;

    @Shadow
    private ParseResults<CommandSource> field_21610;

    @Shadow
    boolean field_21614;

    @Shadow
    private boolean field_21613;

    @Shadow
    public abstract void method_23920(boolean z);

    @Inject(method = {"refresh"}, at = {@At("HEAD")}, cancellable = true)
    private void onRefresh(CallbackInfo callbackInfo) {
        String text = this.field_21599.getText();
        if (text.startsWith(Expensive.INSTANCE.commandDispatcher().getPrefix())) {
            callbackInfo.cancel();
            refreshCustomCommands(text);
        }
    }

    @Unique
    private void refreshCustomCommands(String str) {
        if (this.field_21610 != null && !this.field_21610.getReader().getString().equals(str)) {
            this.field_21610 = null;
        }
        if (!this.field_21614) {
            this.field_21599.setSuggestion((String) null);
            this.field_21612 = null;
        }
        int cursor = this.field_21599.getCursor();
        String strSubstring = str.substring(Expensive.INSTANCE.commandDispatcher().getPrefix().length());
        List<String> suggestions = Expensive.INSTANCE.commandDispatcher().getSuggestions(strSubstring);
        if (suggestions.isEmpty() && cursor <= 0) {
            this.field_21611 = Suggestions.empty();
            return;
        }
        new StringReader(str).skip();
        this.field_21611 = buildSuggestions(str, strSubstring, suggestions, cursor);
        if (this.field_21613 && ((Boolean) (Object) this.field_21597.options.getAutoSuggestions().getValue()).booleanValue()) {
            this.field_21611.thenRun(() -> {
                if (this.field_21611.isDone()) {
                    method_23920(false);
                }
            });
        }
    }

    @Unique
    private CompletableFuture<Suggestions> buildSuggestions(String str, String str2, List<String> list, int i) {
        int iLastIndexOf;
        String[] strArrSplit = str2.split("\\s+", -1);
        if (strArrSplit.length <= 1) {
            iLastIndexOf = str2.isEmpty() ? 1 : str.lastIndexOf(strArrSplit[0]);
        } else {
            String str3 = strArrSplit[strArrSplit.length - 1];
            iLastIndexOf = str3.isEmpty() ? i : str.lastIndexOf(str3);
        }
        SuggestionsBuilder suggestionsBuilder = new SuggestionsBuilder(str, iLastIndexOf);
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            suggestionsBuilder.suggest(it.next());
        }
        return CompletableFuture.completedFuture(suggestionsBuilder.build());
    }
}
