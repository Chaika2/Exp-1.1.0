package ru.dreamix.fabricloader;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Runtime value provider for the obfuscated {@code sg.mx.*} mixin classes.
 *
 * The original Dreamix loader used this hook to inject per-class constant
 * values (Identifier paths, colors, sizes, ...) into {@code private static
 * final} fields that the mixin bytecode never initializes itself. The token
 * table that stored those values is not present in any shipped artifact, so
 * this reconstruction only guarantees that the fields are non-null / non-zero
 * before the mixin {@code <clinit>} reads them, which lets the client boot.
 */
public final class VMBridge {

    private static final sun.misc.Unsafe UNSAFE = findUnsafe();

    private VMBridge() {
    }

    public static void identifyClass(Class<?> clazz, String token) {
        if (clazz == null) {
            return;
        }
        Class<?> cursor = clazz;
        while (cursor != null && cursor != Object.class) {
            Field[] fields;
            try {
                fields = cursor.getDeclaredFields();
            } catch (Throwable ignored) {
                cursor = cursor.getSuperclass();
                continue;
            }
            for (Field field : fields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                try {
                    seed(field);
                } catch (Throwable ignored) {
                    // never let value seeding break class init
                }
            }
            cursor = cursor.getSuperclass();
        }
    }

    private static void seed(Field field) {
        Class<?> type = field.getType();
        Object base = UNSAFE.staticFieldBase(field);
        long offset = UNSAFE.staticFieldOffset(field);

        if (type == String.class) {
            if (UNSAFE.getObject(base, offset) == null) {
                UNSAFE.putObject(base, offset, "default");
            }
        } else if (type == int.class) {
            UNSAFE.putInt(base, offset, 0);
        } else if (type == long.class) {
            UNSAFE.putLong(base, offset, 0L);
        } else if (type == float.class) {
            UNSAFE.putFloat(base, offset, 0.0F);
        } else if (type == double.class) {
            UNSAFE.putDouble(base, offset, 0.0D);
        } else if (type == short.class) {
            UNSAFE.putShort(base, offset, (short) 0);
        } else if (type == byte.class) {
            UNSAFE.putByte(base, offset, (byte) 0);
        } else if (type == char.class) {
            UNSAFE.putChar(base, offset, '\0');
        } else if (type == boolean.class) {
            UNSAFE.putBoolean(base, offset, false);
        }
    }

    private static sun.misc.Unsafe findUnsafe() {
        try {
            Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (sun.misc.Unsafe) field.get(null);
        } catch (Throwable error) {
            throw new ExceptionInInitializerError(error);
        }
    }
}
