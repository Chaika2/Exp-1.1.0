package aethereal.util;
import aethereal.resource.ResourceSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public final class ResourceJsonUtil {
    public static final Gson gson = new Gson();

    public static JsonObject toJson(ResourceSource class178Var) {
        return JsonParser.parseString(toString(class178Var)).getAsJsonObject();
    }

    public static <T> T fromJsonToInstance(ResourceSource class178Var, Class<T> cls) {
        return (T) gson.fromJson(toString(class178Var), cls);
    }

    public static String toString(ResourceSource class178Var) {
        return toString(class178Var, "\n");
    }

    public static String toString(ResourceSource class178Var, String str) {
        try {
            InputStream inputStreamStream = class178Var.stream();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamStream));
                try {
                    String str2 = (String) bufferedReader.lines().collect(Collectors.joining(str));
                    bufferedReader.close();
                    if (inputStreamStream != null) {
                        inputStreamStream.close();
                    }
                    return str2;
                } catch (Throwable th) {
                    try {
                        bufferedReader.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                if (inputStreamStream != null) {
                    try {
                        inputStreamStream.close();
                    } catch (Throwable th4) {
                        th3.addSuppressed(th4);
                    }
                }
                throw th3;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
