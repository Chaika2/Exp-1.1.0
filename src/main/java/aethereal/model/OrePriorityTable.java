package aethereal.model;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class OrePriorityTable {
    public static final Map<Block, Integer> orePriorities;

    public int getPriority(BlockState blockState) {
        return orePriorities.getOrDefault(blockState.getBlock(), 100).intValue();
    }

    public boolean isOre(BlockState blockState) {
        return orePriorities.containsKey(blockState.getBlock());
    }

    static {
        HashMap map = new HashMap();
        map.put(Blocks.DIAMOND_ORE, 0);
        map.put(Blocks.EMERALD_ORE, 1);
        map.put(Blocks.GOLD_ORE, 2);
        map.put(Blocks.IRON_ORE, 3);
        map.put(Blocks.REDSTONE_ORE, 4);
        map.put(Blocks.LAPIS_ORE, 5);
        map.put(Blocks.NETHER_QUARTZ_ORE, 6);
        map.put(Blocks.NETHER_GOLD_ORE, 7);
        map.put(Blocks.COAL_ORE, 8);
        orePriorities = Map.copyOf(map);
    }
}
