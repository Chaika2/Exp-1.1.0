package aethereal.util;
import aethereal.Expensive;
import aethereal.Lang;
import aethereal.module.Module;
import aethereal.model.TranslatedException;
import aethereal.model.Translation;

import java.util.List;
import java.util.stream.Collectors;

public class ModuleArgumentParser implements ArgumentParser<Module> {
    @Override
    public Module parse(String str) {
        return Expensive.INSTANCE.moduleRepository().getModules().stream().filter(class605Var -> {
            return class605Var.getName().replaceAll("\\s", "").equalsIgnoreCase(str);
        }).findFirst().orElseThrow(() -> {
            return new TranslatedException(Translation.clearText(Lang.TYPE_MODULE_NOT_FOUND.effective().replace("{input}", str)));
        });
    }

    @Override
    public List<String> getSuggestions(String str) {
        String lowerCase = str.toLowerCase();
        return (List) Expensive.INSTANCE.moduleRepository().getModules().stream().map(class605Var -> {
            return class605Var.getName().replaceAll("\\s", "");
        }).filter(str2 -> {
            return str2.replaceAll("\\s", "").toLowerCase().startsWith(lowerCase);
        }).collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return "module";
    }
}
