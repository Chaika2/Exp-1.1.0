package aethereal.resource;

import java.util.function.Function;

public class ResourceRouter {
    public final String basePath;
    public final Function<String, ResourceSource> resolver;

    public ResourceSource route(String str) {
        return this.resolver.apply((this.basePath.endsWith("/") ? this.basePath.substring(0, this.basePath.length() - 1) : this.basePath) + "/" + (str.startsWith("/") ? str.substring(1) : str));
    }

    public ResourceRouter(String str, Function<String, ResourceSource> function) {
        this.basePath = str;
        this.resolver = function;
    }
}
