package aethereal.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class ServerProbe {
    private static final int DEFAULT_PORT = 25565;
    private static final int TIMEOUT_MS = 5000;

    private ServerProbe() {
    }

    public static final class Sample {
        public final String name;
        public final String uuid;

        public Sample(String str, String str2) {
            this.name = str;
            this.uuid = str2;
        }
    }

    public static final class Result {
        public final boolean ok;
        public final String error;
        public final String host;
        public final int port;
        public final int online;
        public final int max;
        public final List<Sample> samples;

        Result(boolean z, String str, String str2, int i, int j, int k, List<Sample> list) {
            this.ok = z;
            this.error = str;
            this.host = str2;
            this.port = i;
            this.online = j;
            this.max = k;
            this.samples = list;
        }
    }

    public static Result query(String str) {
        String str2 = str;
        int i = DEFAULT_PORT;
        if (str != null && str.startsWith("[") && str.contains("]")) {
            int iIndexOf = str.indexOf(93);
            str2 = str.substring(1, iIndexOf);
            String strSubstring = str.substring(iIndexOf + 1);
            if (strSubstring.startsWith(":")) {
                i = parsePort(strSubstring.substring(1), i);
            }
        } else if (str != null && str.contains(":")) {
            int iLastIndexOf = str.lastIndexOf(58);
            str2 = str.substring(0, iLastIndexOf);
            i = parsePort(str.substring(iLastIndexOf + 1), i);
        }
        if (str2 == null || str2.isEmpty()) {
            return new Result(false, "empty address", str, i, 0, 0, List.of());
        }
        try {
            return ping(str2, i);
        } catch (Exception exception) {
            return new Result(false, exception.toString(), str2, i, 0, 0, List.of());
        }
    }

    private static int parsePort(String str, int i) {
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException numberFormatException) {
            return i;
        }
    }

    private static Result ping(String str, int i) throws IOException {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(str, i), TIMEOUT_MS);
            socket.setSoTimeout(TIMEOUT_MS);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream2 = new DataOutputStream(byteArrayOutputStream);
            writeVarInt(dataOutputStream2, 0);
            writeVarInt(dataOutputStream2, 0);
            writeString(dataOutputStream2, str);
            dataOutputStream2.writeShort(i);
            writeVarInt(dataOutputStream2, 1);
            writePacket(dataOutputStream, byteArrayOutputStream.toByteArray());
            writePacket(dataOutputStream, new byte[]{0});
            dataOutputStream.flush();
            readVarInt(dataInputStream);
            readVarInt(dataInputStream);
            JsonObject asJsonObject = JsonParser.parseString(readString(dataInputStream)).getAsJsonObject();
            int asInt = 0;
            int asInt2 = 0;
            List<Sample> arrayList = new ArrayList<>();
            if (asJsonObject.has("players")) {
                JsonObject asJsonObject2 = asJsonObject.getAsJsonObject("players");
                if (asJsonObject2.has("online")) {
                    asInt = asJsonObject2.get("online").getAsInt();
                }
                if (asJsonObject2.has("max")) {
                    asInt2 = asJsonObject2.get("max").getAsInt();
                }
                if (asJsonObject2.has("sample")) {
                    JsonArray asJsonArray = asJsonObject2.getAsJsonArray("sample");
                    for (JsonElement jsonElement : asJsonArray) {
                        JsonObject asJsonObject3 = jsonElement.getAsJsonObject();
                        String asString = asJsonObject3.has("name") ? asJsonObject3.get("name").getAsString() : "?";
                        String asString2 = asJsonObject3.has("id") ? asJsonObject3.get("id").getAsString() : "?";
                        arrayList.add(new Sample(asString, asString2));
                    }
                }
            }
            return new Result(true, null, str, i, asInt, asInt2, arrayList);
        } finally {
            try {
                socket.close();
            } catch (IOException ioException) {
            }
        }
    }

    private static void writePacket(OutputStream outputStream, byte[] bArr) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        writeVarInt(dataOutputStream, bArr.length);
        dataOutputStream.write(bArr);
    }

    private static void writeString(DataOutputStream dataOutputStream, String str) throws IOException {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        writeVarInt(dataOutputStream, bytes.length);
        dataOutputStream.write(bytes);
    }

    private static String readString(DataInputStream dataInputStream) throws IOException {
        int varInt = readVarInt(dataInputStream);
        byte[] bArr = new byte[varInt];
        dataInputStream.readFully(bArr);
        return new String(bArr, StandardCharsets.UTF_8);
    }

    private static void writeVarInt(DataOutputStream dataOutputStream, int i) throws IOException {
        while ((i & (-128)) != 0) {
            dataOutputStream.writeByte((i & 127) | 128);
            i >>>= 7;
        }
        dataOutputStream.writeByte(i & 127);
    }

    private static int readVarInt(DataInputStream dataInputStream) throws IOException {
        int i = 0;
        int i2 = 0;
        while (true) {
            int i3 = dataInputStream.readByte();
            i |= (i3 & 127) << i2;
            if ((i3 & 128) == 0) {
                return i;
            }
            i2 += 7;
            if (i2 > 35) {
                throw new EOFException("VarInt too big");
            }
        }
    }
}
