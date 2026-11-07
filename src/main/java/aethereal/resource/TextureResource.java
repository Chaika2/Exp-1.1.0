package aethereal.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public class TextureResource implements ResourceSource {
    public final AbstractTexture texture;

    @Override
    public InputStream stream() {
        if ((this.texture) instanceof ResourceTexture resourceTexture ) {
            Identifier id = resourceTexture.getId();
            try {
                System.out.println("loaded resource texture");
                return openResourceStream(id);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load ResourceTexture: " + String.valueOf(id), e);
            }
        }
        if (!(this.texture instanceof NativeImageBackedTexture nativeImageBackedTexture2)) {
            return null;
        }
        try {
            System.out.println("loaded native image texture");
            return encodeNativeImage((NativeImage) Objects.requireNonNull(nativeImageBackedTexture2.getImage()));
        } catch (IOException e2) {
            throw new IllegalStateException("Failed to process NativeImageBackedTexture", e2);
        }
    }

    public InputStream encodeNativeImage(NativeImage nativeImage) throws IOException {
        if (!nativeImage.getFormat().isWriteable()) {
            throw new UnsupportedOperationException("Image format is not writeable: " + String.valueOf(nativeImage.getFormat()));
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (writeImage(nativeImage, Channels.newChannel(byteArrayOutputStream))) {
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        }
        throw new IOException("Failed to write NativeImage to PNG: " + String.valueOf(nativeImage));
    }

    public boolean writeImage(NativeImage nativeImage, WritableByteChannel writableByteChannel) throws IOException {
        return nativeImage.write(writableByteChannel);
    }

    public InputStream openResourceStream(Identifier identifier) throws IOException {
        return ((Resource) MinecraftClient.getInstance().getResourceManager().getResource(identifier).orElseThrow(() -> {
            return new IllegalStateException("Resource not found: " + String.valueOf(identifier));
        })).getInputStream();
    }

    public TextureResource(AbstractTexture abstractTexture) {
        this.texture = abstractTexture;
    }
}
