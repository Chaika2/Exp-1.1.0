package aethereal.event;

import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public class BlockInteractEvent extends CancellableEvent {
    public BlockHitResult hitResult;
    public Hand hand;

    public BlockHitResult getHitResult() {
        return this.hitResult;
    }

    public Hand getHand() {
        return this.hand;
    }

    public BlockInteractEvent(BlockHitResult blockHitResult, Hand hand) {
        this.hitResult = blockHitResult;
        this.hand = hand;
    }
}
