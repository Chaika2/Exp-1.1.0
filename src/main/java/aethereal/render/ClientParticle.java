package aethereal.render;

public class ClientParticle {
    public double prevX;
    public double prevY;
    public double prevZ;
    public double x;
    public double y;
    public double z;
    public double vx;
    public double vy;
    public double vz;
    public float size;
    public int lifetime;

    public ClientParticle(double d, double d2, double d3, double d4, double d5, double d6, int i) {
        this.x = d;
        this.prevX = d;
        this.y = d2;
        this.prevY = d2;
        this.z = d3;
        this.prevZ = d3;
        this.vx = d4;
        this.vy = d5;
        this.vz = d6;
        this.lifetime = i;
    }

    public float fadeAlpha(float f) {
        float f2 = f / this.lifetime;
        if (f2 < 0.2f) {
            return f2 / 0.2f;
        }
        if (f2 > 0.8f) {
            return (1.0f - f2) / 0.2f;
        }
        return 1.0f;
    }
}
