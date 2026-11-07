package aethereal.util;

import net.minecraft.text.Text;

@FunctionalInterface
public interface ChatDecorator {
    Text apply(Text text);
}
