package aethereal.event;

import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

public class InteractBlockEvent extends CancellableEvent {
    public final Hand hand;

    public final BlockHitResult result;

    public final ActionResult actionResult;

    public InteractBlockEvent(Hand hand, BlockHitResult blockHitResult, ActionResult actionResult) {
        this.hand = hand;
        this.result = blockHitResult;
        this.actionResult = actionResult;
    }

    public Hand getHand() {
        return this.hand;
    }

    public BlockHitResult getResult() {
        return this.result;
    }

    public ActionResult getActionResult() {
        return this.actionResult;
    }
}
