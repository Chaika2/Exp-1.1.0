package aethereal.type;
import aethereal.model.DisplayNamed;
import aethereal.Lang;
import aethereal.model.Translation;

import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;

public enum BlockEspType implements DisplayNamed {
    DIAMOND(Lang.BLOCK_ESP_DIAMOND, s -> s.isIn(BlockTags.DIAMOND_ORES)),
    GOLD(Lang.BLOCK_ESP_GOLD, s -> s.isIn(BlockTags.GOLD_ORES)),
    IRON(Lang.BLOCK_ESP_IRON, s -> s.isIn(BlockTags.IRON_ORES)),
    COAL(Lang.BLOCK_ESP_COAL, s -> s.isIn(BlockTags.COAL_ORES)),
    EMERALD(Lang.BLOCK_ESP_EMERALD, s -> s.isIn(BlockTags.EMERALD_ORES)),
    REDSTONE(Lang.BLOCK_ESP_REDSTONE, s -> s.isIn(BlockTags.REDSTONE_ORES)),
    LAPIS(Lang.BLOCK_ESP_LAPIS, s -> s.isIn(BlockTags.LAPIS_ORES)),
    COPPER(Lang.BLOCK_ESP_COPPER, s -> s.isIn(BlockTags.COPPER_ORES)),
    ANCIENT_DEBRIS(Lang.BLOCK_ESP_ANCIENT_DEBRIS, s -> s.isOf(Blocks.ANCIENT_DEBRIS)),
    SPAWNER(Lang.BLOCK_ESP_SPAWNER, s -> s.isOf(Blocks.SPAWNER) || s.isOf(Blocks.TRIAL_SPAWNER)),
    END_PORTAL_FRAME(Lang.BLOCK_ESP_END_PORTAL_FRAME, s -> s.isOf(Blocks.END_PORTAL_FRAME)),
    CHEST(Lang.BLOCK_ESP_CHEST, s -> s.isOf(Blocks.CHEST) || s.isOf(Blocks.TRAPPED_CHEST)),
    SHULKER_BOX(Lang.BLOCK_ESP_SHULKER_BOX, s -> s.isIn(BlockTags.SHULKER_BOXES));

    public final Translation displayName;
    public final Predicate<BlockState> predicate;

    public boolean matches(BlockState blockState) {
        return this.predicate.test(blockState);
    }

    @Override
    public Translation getDisplayName() {
        return this.displayName;
    }

    public Translation displayName() {
        return this.displayName;
    }

    BlockEspType(Translation class254Var, Predicate<BlockState> predicate) {
        this.displayName = class254Var;
        this.predicate = predicate;
    }
}