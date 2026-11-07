package aethereal.net;

public class LoginPacket implements OutgoingPacket {
    public final String username;
    public final String password;
    public final byte[] token;

    public LoginPacket(String str, String str2, byte[] bArr) {
        this.username = str;
        this.password = str2;
        this.token = bArr;
    }

    @Override
    public int id() {
        return 1;
    }

    @Override
    public void encode(PacketBuffer class621Var) {
        class621Var.writeString(this.username);
        class621Var.writeString(this.password != null ? this.password : "");
        class621Var.writeByteArray(this.token);
    }
}
