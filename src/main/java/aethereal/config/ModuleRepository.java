package aethereal.config;
import aethereal.module.Module;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ModuleRepository {
    public final Map<Class<? extends Module>, Module> moduleMap;

    public ModuleRepository(List<Supplier<Module>> list) {
        this.moduleMap = (Map) list.stream().map((v0) -> {
            return v0.get();
        }).collect(Collectors.toMap((v0) -> {
            return v0.getClass();
        }, Function.identity(), (class605Var, class605Var2) -> {
            return class605Var;
        }, HashMap::new));
    }

    public Collection<Module> getModules() {
        return Collections.unmodifiableCollection(this.moduleMap.values());
    }

    public <T extends Module> T get(Class<T> cls) {
        Module class605Var = this.moduleMap.get(cls);
        if (class605Var == null) {
            throw new NoSuchElementException("Module not found: " + cls.getName());
        }
        return cls.cast(class605Var);
    }

    public <T extends Module> Optional<T> find(Class<T> cls) {
        Optional optionalOfNullable = Optional.ofNullable(this.moduleMap.get(cls));
        Objects.requireNonNull(cls);
        return optionalOfNullable.map((v1) -> {
            return cls.cast(v1);
        });
    }

    public Map<Class<? extends Module>, Module> getModuleMap() {
        return this.moduleMap;
    }
}
