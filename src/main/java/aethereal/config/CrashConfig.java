package aethereal.config;
import aethereal.net.AuthResponsePacket;


public final class CrashConfig {
    public final boolean enableCrashes;

    public CrashConfig(boolean z) {
        this.enableCrashes = z;
    }

    public void setup(AuthResponsePacket class718Var) {
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "enableCrashes=" + this.enableCrashes + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.enableCrashes);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CrashConfig)) return false;
        CrashConfig o = (CrashConfig) obj;
        return java.util.Objects.equals(this.enableCrashes, o.enableCrashes);
    }
public boolean enableCrashes() {
        return this.enableCrashes;
    }
}
