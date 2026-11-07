package aethereal.util;
import aethereal.type.CountLoopStrategy;
import aethereal.model.DelayStep;
import aethereal.type.LoopStrategy;
import aethereal.type.PerformAction;
import aethereal.math.Stopwatch;
import aethereal.model.TickStep;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

public class ActionScheduler {
    public int currentStepIndex;
    public int currentTickStepIndex;
    public int currentTick;
    public boolean interrupt;
    public final Stopwatch time = new Stopwatch(false);
    public final List<DelayStep> scriptSteps = Lists.newCopyOnWriteArrayList();
    public final List<TickStep> scriptTickSteps = Lists.newCopyOnWriteArrayList();
    public LoopStrategy loopStrategy = new CountLoopStrategy(1);

    public ActionScheduler() {
        cleanup();
    }

    public ActionScheduler addStep(int i, PerformAction class062Var) {
        return addStep(i, class062Var, () -> {
            return true;
        }, 0);
    }

    public ActionScheduler addStep(int i, PerformAction class062Var, BooleanSupplier booleanSupplier) {
        return addStep(i, class062Var, booleanSupplier, 0);
    }

    public ActionScheduler addStep(int i, PerformAction class062Var, int i2) {
        return addStep(i, class062Var, () -> {
            return true;
        }, i2);
    }

    public ActionScheduler addStep(int i, PerformAction class062Var, BooleanSupplier booleanSupplier, int i2) {
        this.scriptSteps.add(new DelayStep(i, class062Var, booleanSupplier, i2));
        Collections.sort(this.scriptSteps);
        return this;
    }

    public ActionScheduler addTickStep(int i, PerformAction class062Var) {
        return addTickStep(i, class062Var, () -> {
            return true;
        }, 0);
    }

    public ActionScheduler addTickStep(int i, PerformAction class062Var, BooleanSupplier booleanSupplier) {
        return addTickStep(i, class062Var, booleanSupplier, 0);
    }

    public ActionScheduler addTickStep(int i, PerformAction class062Var, int i2) {
        return addTickStep(i, class062Var, () -> {
            return true;
        }, i2);
    }

    public ActionScheduler addTickStep(int i, PerformAction class062Var, BooleanSupplier booleanSupplier, int i2) {
        this.scriptTickSteps.add(new TickStep(i, class062Var, booleanSupplier, i2));
        Collections.sort(this.scriptTickSteps);
        return this;
    }

    public void resetTime() {
        this.time.reset();
    }

    public void resetStepIndex() {
        this.currentStepIndex = 0;
        this.currentTickStepIndex = 0;
        this.currentTick = 0;
    }

    public ActionScheduler cleanupIfFinished() {
        if (isFinished()) {
            cleanup();
        }
        return this;
    }

    public ActionScheduler cleanup() {
        this.scriptSteps.clear();
        this.scriptTickSteps.clear();
        resetTime();
        resetStepIndex();
        return this;
    }

    public ActionScheduler update() {
        if ((this.scriptSteps.isEmpty() && this.scriptTickSteps.isEmpty()) || this.interrupt) {
            return this;
        }
        this.scriptSteps.forEach(step -> {
            if (this.currentStepIndex < this.scriptSteps.size()) {
                DelayStep class266Var = this.scriptSteps.get(this.currentStepIndex);
                if (class266Var.condition().getAsBoolean() && this.time.hasElapsed(class266Var.delay())) {
                    class266Var.action().perform();
                    this.currentStepIndex++;
                    resetTime();
                    if (this.loopStrategy.shouldLoop(this.currentStepIndex, this.scriptSteps.size())) {
                        resetStepIndex();
                        this.loopStrategy.onLoop();
                    }
                }
            }
        });
        if (this.currentTickStepIndex < this.scriptTickSteps.size()) {
            TickStep class268Var = this.scriptTickSteps.get(this.currentTickStepIndex);
            if (!class268Var.condition().getAsBoolean()) {
                this.currentTick++;
                return this;
            }
            if (class268Var.ticks() <= this.currentTick) {
                class268Var.action().perform();
                this.currentTickStepIndex++;
                if (this.loopStrategy.shouldLoop(this.currentTickStepIndex, this.scriptTickSteps.size())) {
                    resetStepIndex();
                    this.loopStrategy.onLoop();
                }
            }
        }
        this.currentTick++;
        this.currentStepIndex = Math.min(this.currentStepIndex, this.scriptSteps.size());
        this.currentTickStepIndex = Math.min(this.currentTickStepIndex, this.scriptTickSteps.size());
        return this;
    }

    public ActionScheduler setLoopStrategy(LoopStrategy class269Var) {
        this.loopStrategy = class269Var;
        return this;
    }

    public boolean isFinished() {
        return this.currentStepIndex >= this.scriptSteps.size() && this.currentTickStepIndex >= this.scriptTickSteps.size() && !this.interrupt && this.loopStrategy.isFinished();
    }

    public Stopwatch getTime() {
        return this.time;
    }

    public List<DelayStep> getScriptSteps() {
        return this.scriptSteps;
    }

    public List<TickStep> getScriptTickSteps() {
        return this.scriptTickSteps;
    }

    public int getCurrentStepIndex() {
        return this.currentStepIndex;
    }

    public int getCurrentTickStepIndex() {
        return this.currentTickStepIndex;
    }

    public int getCurrentTick() {
        return this.currentTick;
    }

    public boolean isInterrupt() {
        return this.interrupt;
    }

    public LoopStrategy getLoopStrategy() {
        return this.loopStrategy;
    }

    public void setCurrentStepIndex(int i) {
        this.currentStepIndex = i;
    }

    public void setCurrentTickStepIndex(int i) {
        this.currentTickStepIndex = i;
    }

    public void setCurrentTick(int i) {
        this.currentTick = i;
    }

    public void setInterrupt(boolean z) {
        this.interrupt = z;
    }
}
