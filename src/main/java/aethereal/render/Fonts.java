package aethereal.render;

import com.google.common.base.Suppliers;
import java.util.function.Supplier;

public class Fonts {
    public static final Supplier<MsdfFont> INTER_SEMIBOLD = Suppliers.memoize(() -> {
        return MsdfFont.builder().atlas("inter-semi").data("inter-semi").build();
    });
    public static final Supplier<MsdfFont> INTER_BOLD = Suppliers.memoize(() -> {
        return MsdfFont.builder().atlas("inter-bold").data("inter-bold").build();
    });
    public static final Supplier<MsdfFont> INTER_MEDIUM = Suppliers.memoize(() -> {
        return MsdfFont.builder().atlas("inter-medium").data("inter-medium").build();
    });
    public static final Supplier<MsdfFont> INTER_EXTRA_BOLD = Suppliers.memoize(() -> {
        return MsdfFont.builder().atlas("inter-extrabold").data("inter-extrabold").build();
    });
}
