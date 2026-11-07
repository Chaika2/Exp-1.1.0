package aethereal.util;
import aethereal.type.EnumArgumentType;
import aethereal.type.IntegerArgumentType;

public class ArgumentTypeFactory {
    public static IntegerArgumentType integer(int i, int i2) {
        return new IntegerArgumentType(Integer.valueOf(i), Integer.valueOf(i2));
    }

    public static FloatArgumentParser floatType(float f, float f2) {
        return new FloatArgumentParser(Float.valueOf(f), Float.valueOf(f2));
    }

    public static <E extends Enum<E>> EnumArgumentType<E> enumType(Class<E> cls) {
        return new EnumArgumentType<>(cls);
    }
}
