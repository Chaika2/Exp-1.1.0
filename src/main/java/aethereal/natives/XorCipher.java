package aethereal.natives;

public final class XorCipher {
    public static final byte[] key = "s8Fk2pL9xQm4vN7wR1jT6yB3cHdA0eZ".getBytes();

    public XorCipher() {
    }

    public static byte[] encrypt(byte[] bArr) {
        return applyCipher(bArr);
    }

    public static byte[] decrypt(byte[] bArr) {
        return applyCipher(bArr);
    }

    public static byte[] applyCipher(byte[] bArr) {
        byte[] bArr2 = new byte[bArr.length];
        for (int i = 0; i < bArr.length; i++) {
            bArr2[i] = (byte) (bArr[i] ^ key[i % key.length]);
        }
        return bArr2;
    }
}
