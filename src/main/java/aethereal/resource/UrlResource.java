package aethereal.resource;
import aethereal.Expensive;

import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;

public class UrlResource implements ResourceSource {
    public final String url;

    @Override
    public InputStream stream() {
        try {
            Expensive.LOGGER.debug("Trying to load URL resource: {}", this.url);
            URI uriCreate = URI.create(this.url);
            Expensive.LOGGER.debug("Parsed URI: {}", uriCreate);
            URLConnection uRLConnectionOpenConnection = uriCreate.toURL().openConnection();
            uRLConnectionOpenConnection.setUseCaches(false);
            uRLConnectionOpenConnection.setConnectTimeout(5000);
            uRLConnectionOpenConnection.setReadTimeout(5000);
            uRLConnectionOpenConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
            return uRLConnectionOpenConnection.getInputStream();
        } catch (Exception e) {
            Expensive.LOGGER.error("Failed to open URL resource: {}", this.url, e);
            throw new RuntimeException("Failed to open URL resource: " + this.url, e);
        }
    }

    public UrlResource(String str) {
        this.url = str;
    }
}
