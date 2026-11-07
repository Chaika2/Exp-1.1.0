package aethereal.type;

public enum StaffStatus {
    SPEC("Наблюдает", 0xFFFFC107),
    PLAYING("Играет", 0xFF4CAF50),
    VANISH("Ванишнут", 0xFF9C27B0);

    private final String status;
    private final int color;

    StaffStatus(String str, int i) {
        this.status = str;
        this.color = i;
    }

    public String getStatus() {
        return this.status;
    }

    public int getColor() {
        return this.color;
    }
}
