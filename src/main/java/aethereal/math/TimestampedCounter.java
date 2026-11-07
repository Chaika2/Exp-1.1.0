package aethereal.math;

public class TimestampedCounter {
    public int count;

    public long timestamp;

    public int getCount() {
        return this.count;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public TimestampedCounter(int i, long j) {
        this.count = i;
        this.timestamp = j;
    }
}
