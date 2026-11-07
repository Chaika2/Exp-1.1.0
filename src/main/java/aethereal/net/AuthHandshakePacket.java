package aethereal.net;

import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class AuthHandshakePacket extends SocketPacket {
    public String hwid;
    public PublicKey publicKey;

    public AuthHandshakePacket(String str) {
        this.hwid = str;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            this.publicKey = keyPairGenerator.genKeyPair().getPublic();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public AuthHandshakePacket() {
    }

    @Override
    public void read(ByteBuffer byteBuffer) {
        byte[] bArr = new byte[byteBuffer.getInt()];
        byteBuffer.get(bArr);
        this.hwid = new String(bArr);
        byte[] bArr2 = new byte[byteBuffer.getInt()];
        byteBuffer.get(bArr2);
        try {
            this.publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bArr2));
        } catch (java.security.NoSuchAlgorithmException | java.security.spec.InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ByteBuffer write() {
        byte[] encoded = this.publicKey.getEncoded();
        int length = this.hwid.length();
        byte[] bytes = this.hwid.getBytes();
        ByteBuffer byteBufferAllocate = ByteBuffer.allocate(5 + length + 4 + encoded.length);
        byteBufferAllocate.put(packetID());
        byteBufferAllocate.putInt(length);
        byteBufferAllocate.put(bytes);
        byteBufferAllocate.putInt(encoded.length);
        byteBufferAllocate.put(encoded);
        return byteBufferAllocate;
    }

    @Override
    public byte packetID() {
        return (byte) 1;
    }
}
