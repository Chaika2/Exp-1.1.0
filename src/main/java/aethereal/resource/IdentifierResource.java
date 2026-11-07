package aethereal.resource;

import java.io.IOException;
import java.io.InputStream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public class IdentifierResource implements ResourceSource {
    public final Identifier identifier;

    public IdentifierResource(Identifier identifier) {
        this.identifier = identifier;
    }

    @Override
    public InputStream stream() {
        try {
            return ((Resource) MinecraftClient.getInstance().getResourceManager().getResource(this.identifier).orElseThrow(() -> {
                return new IllegalStateException("Can't find resource for " + String.valueOf(this.identifier));
            })).getInputStream();
        } catch (IOException e) {
            throw new IllegalStateException("Error reading resource for " + String.valueOf(this.identifier), e);
        }
    }
}
