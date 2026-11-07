package aethereal.config;
import aethereal.type.BindMode;
import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.ColorSetting;
import aethereal.ui.Draggable;
import aethereal.ui.setting.EnumSetting;
import aethereal.ui.setting.ExpandableSetting;
import aethereal.Expensive;
import aethereal.ui.setting.KeybindSetting;
import aethereal.ui.setting.LabelMultiSelectSetting;
import aethereal.ui.setting.LabelSelectSetting;
import aethereal.model.LayoutNode;
import aethereal.ui.setting.ModeSetting;
import aethereal.module.Module;
import aethereal.model.ModuleConfigData;
import aethereal.model.ModuleConfigEntry;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.ui.setting.NumberSetting;
import aethereal.ui.setting.OrderedEnumSetting;
import aethereal.ui.setting.Setting;
import aethereal.ui.setting.TextFieldSetting;
import aethereal.model.Translation;
import aethereal.ui.WidgetStack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class ConfigSerializer {
    public final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public byte[] serialize(ModuleRepository class795Var, WidgetStack class814Var) throws IOException {
        return this.gson.toJson(buildConfigFile(class795Var, class814Var)).getBytes(StandardCharsets.UTF_8);
    }

    public ConfigFile deserialize(byte[] bArr) throws IOException {
        return (ConfigFile) this.gson.fromJson(new String(bArr, StandardCharsets.UTF_8), ConfigFile.class);
    }

    public Optional<ConfigFile> tryDeserialize(byte[] bArr) {
        try {
            return Optional.of(deserialize(bArr));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public void applyConfig(LoadedConfig class089Var) throws IOException {
        ModuleRepository class795VarModuleRepository = Expensive.INSTANCE.moduleRepository();
        WidgetStack class814VarWidgetStack = Expensive.INSTANCE.widgetStack();
        ConfigFile class145VarBundle = class089Var.bundle();
        HashMap map = new HashMap();
        for (Module class605Var : class795VarModuleRepository.getModules()) {
            map.put(class605Var.getName(), class605Var);
        }
        if (class145VarBundle.modules() != null) {
            for (ModuleConfigData class218Var : class145VarBundle.modules()) {
                Module class605Var2 = (Module) map.get(class218Var.name());
                if (class605Var2 != null) {
                    class605Var2.setStateSilent(class218Var.enabled());
                    class605Var2.setBind(class218Var.keys(), class218Var.bindType() != null ? class218Var.bindType() : class605Var2.getType());
                    if (class218Var.settings() != null) {
                        Set<Setting> usedSettings = new HashSet<>();
                        Iterator<LayoutNode> it = class218Var.settings().iterator();
                        while (it.hasNext()) {
                            applySettingNode(class605Var2.getSettings(), it.next(), usedSettings);
                        }
                    }
                }
            }
        }
        if (class145VarBundle.widgets() != null) {
            Iterator<ModuleConfigEntry> it2 = class145VarBundle.widgets().iterator();
            while (it2.hasNext()) {
                applyWidgetEntry(class814VarWidgetStack, it2.next());
            }
        }
    }

    public void applySettingNode(List<Setting> list, LayoutNode class301Var) {
        applySettingNode(list, class301Var, new HashSet<>());
    }

    public void applySettingNode(List<Setting> list, LayoutNode class301Var, Set<Setting> usedSettings) {
        if (class301Var == null || class301Var.name() == null) {
            return;
        }
        list.stream().filter((v0) -> {
            return Objects.nonNull(v0);
        }).filter(class661Var -> {
            return (class661Var.getName() == null || class661Var.getName().original() == null) ? false : true;
        }).filter(class661Var2 -> {
            return class661Var2.getName().original().equalsIgnoreCase(class301Var.name());
        }).filter(class661Var2 -> {
            return !usedSettings.contains(class661Var2);
        }).findFirst().ifPresent(class661Var3 -> {
            usedSettings.add(class661Var3);
            applySettingData(class661Var3, class301Var);
        });
    }

    public void applyWidgetEntry(WidgetStack class814Var, ModuleConfigEntry class124Var) {
        String strTrim = class124Var.name() == null ? "" : class124Var.name().trim();
        class814Var.widgets().stream().filter((v0) -> {
            return Objects.nonNull(v0);
        }).filter(class806Var -> {
            return class806Var.getName() != null;
        }).filter(class806Var2 -> {
            return class806Var2.getName().equalsIgnoreCase(strTrim);
        }).findFirst().ifPresent(class806Var3 -> {
            if (class124Var.settings() != null) {
                Set<Setting> usedSettings = new HashSet<>();
                Iterator<LayoutNode> it = class124Var.settings().iterator();
                while (it.hasNext()) {
                    applySettingNode(class806Var3.getSettings(), it.next(), usedSettings);
                }
            }
            class806Var3.applySavedPosition(class124Var.x(), class124Var.y());
        });
    }

    public void applySettingData(Setting class661Var, LayoutNode class301Var) {
        int iIntValue;
        if (class661Var instanceof ExpandableSetting) {
            ExpandableSetting class670Var = (ExpandableSetting) class661Var;
            if (class301Var.children() != null) {
                for (LayoutNode class301Var2 : class301Var.children()) {
                    class670Var.getSubSettings().stream().filter(class661Var2 -> {
                        return class661Var2.getName().original().equalsIgnoreCase(class301Var2.name());
                    }).findFirst().ifPresent(class661Var3 -> {
                        applySettingData(class661Var3, class301Var2);
                    });
                }
            }
            Object obj = class301Var.data().get("value");
            if (obj instanceof Boolean) {
                class670Var.setValue(((Boolean) obj).booleanValue());
            }
            Object obj2 = class301Var.data().get("keys");
            if (obj2 instanceof List) {
                Stream stream = ((List) obj2).stream();
                Class<Number> cls = Number.class;
                Objects.requireNonNull(Number.class);
                Stream streamFilter = stream.filter(cls::isInstance);
                Class<Number> cls2 = Number.class;
                Objects.requireNonNull(Number.class);
                class670Var.setKey(streamFilter.map(cls2::cast).map((v0) -> {
                    return ((Number) v0).intValue();
                }).toList());
            } else {
                Object obj3 = class301Var.data().get("key");
                if (obj3 instanceof Number) {
                    class670Var.setKey(((Number) obj3).intValue());
                }
            }
            Object obj4 = class301Var.data().get("bindType");
            if (obj4 instanceof String) {
                class670Var.setType(BindMode.valueOf((String) obj4));
                return;
            }
            return;
        }
        if (class661Var instanceof KeybindSetting) {
            KeybindSetting class663Var = (KeybindSetting) class661Var;
            Object obj5 = class301Var.data().get("keys");
            if (obj5 instanceof List) {
                Stream stream2 = ((List) obj5).stream();
                Class<Number> cls3 = Number.class;
                Objects.requireNonNull(Number.class);
                Stream streamFilter2 = stream2.filter(cls3::isInstance);
                Class<Number> cls4 = Number.class;
                Objects.requireNonNull(Number.class);
                class663Var.setKey(streamFilter2.map(cls4::cast).map((v0) -> {
                    return ((Number) v0).intValue();
                }).toList());
            } else {
                Object obj6 = class301Var.data().get("key");
                if (obj6 instanceof Number) {
                    class663Var.setKey(((Number) obj6).intValue());
                }
            }
            Object obj7 = class301Var.data().get("bindType");
            if (obj7 instanceof String) {
                class663Var.setType(BindMode.valueOf((String) obj7));
                return;
            }
            return;
        }
        if (class661Var instanceof BooleanSetting) {
            BooleanSetting class665Var = (BooleanSetting) class661Var;
            Object obj8 = class301Var.data().get("value");
            if (obj8 instanceof Boolean) {
                class665Var.setValue(((Boolean) obj8).booleanValue());
            }
            Object obj9 = class301Var.data().get("key");
            if (obj9 instanceof Number) {
                class665Var.setKey(((Number) obj9).intValue());
            }
            Object obj10 = class301Var.data().get("bindType");
            if (obj10 instanceof String) {
                class665Var.setType(BindMode.valueOf((String) obj10));
                return;
            }
            return;
        }
        if (class661Var instanceof ModeSetting) {
            ModeSetting class669Var = (ModeSetting) class661Var;
            Object obj11 = class301Var.data().get("value");
            if (obj11 instanceof String) {
                try {
                    class669Var.select(Enum.valueOf(class669Var.currentValue().getDeclaringClass(), (String) obj11));
                    return;
                } catch (IllegalArgumentException e) {
                    return;
                }
            }
            return;
        }
        if (class661Var instanceof EnumSetting) {
            EnumSetting class610Var = (EnumSetting) class661Var;
            Object obj12 = class301Var.data().get("value");
            if (obj12 instanceof String) {
                try {
                    class610Var.select(Enum.valueOf(class610Var.currentValue().getDeclaringClass(), (String) obj12));
                    return;
                } catch (IllegalArgumentException e2) {
                    return;
                }
            }
            return;
        }
        if (class661Var instanceof MultiSelectSetting) {
            MultiSelectSetting class671Var = (MultiSelectSetting) class661Var;
            Object obj13 = class301Var.data().get("selected");
            if (obj13 instanceof List) {
                List list = (List) obj13;
                class671Var.selectedValues().clear();
                Class declaringClass = class671Var.options()[0].getDeclaringClass();
                for (Object obj14 : list) {
                    if (obj14 instanceof String) {
                        try {
                            class671Var.selectedValues().add(Enum.valueOf(declaringClass, (String) obj14));
                        } catch (IllegalArgumentException e3) {
                        }
                    }
                }
                return;
            }
            return;
        }
        if (class661Var instanceof OrderedEnumSetting) {
            OrderedEnumSetting class609Var = (OrderedEnumSetting) class661Var;
            Class declaringClass2 = class609Var.options()[0].getDeclaringClass();
            Object obj15 = class301Var.data().get("selected");
            if (obj15 instanceof List) {
                getSelectedEnums(class609Var).clear();
                for (Object obj16 : (List) obj15) {
                    if (obj16 instanceof String) {
                        getSelectedEnums(class609Var).add(Enum.valueOf(declaringClass2, (String) obj16));
                    }
                }
            }
            Object obj17 = class301Var.data().get("order");
            if (obj17 instanceof List) {
                class609Var.order().clear();
                for (Object obj18 : (List) obj17) {
                    if (obj18 instanceof String) {
                        class609Var.order().add(Enum.valueOf(declaringClass2, (String) obj18));
                    }
                }
                return;
            }
            return;
        }
        if (class661Var instanceof ColorSetting) {
            ColorSetting class667Var = (ColorSetting) class661Var;
            Object obj19 = class301Var.data().get("color");
            if (obj19 instanceof Number) {
                class667Var.setColor(((Number) obj19).intValue());
                return;
            }
            return;
        }
        if (class661Var instanceof NumberSetting) {
            NumberSetting class613Var = (NumberSetting) class661Var;
            Object obj20 = class301Var.data().get("value");
            if (obj20 instanceof Number) {
                class613Var.currentValue(((Number) obj20).floatValue());
                return;
            }
            return;
        }
        if (class661Var instanceof TextFieldSetting) {
            TextFieldSetting class612Var = (TextFieldSetting) class661Var;
            Object obj21 = class301Var.data().get("text");
            if (obj21 instanceof String) {
                class612Var.setText((String) obj21);
                return;
            }
            return;
        }
        if (class661Var instanceof LabelSelectSetting) {
            LabelSelectSetting class607Var = (LabelSelectSetting) class661Var;
            Object obj22 = class301Var.data().get("index");
            if (obj22 instanceof Number) {
                class607Var.selected(((Number) obj22).intValue());
                return;
            }
            return;
        }
        if (class661Var instanceof LabelMultiSelectSetting) {
            LabelMultiSelectSetting class606Var = (LabelMultiSelectSetting) class661Var;
            Object obj23 = class301Var.data().get("indexes");
            if (obj23 instanceof List) {
                class606Var.getSelected().clear();
                for (Object obj24 : (List) obj23) {
                    if ((obj24 instanceof Number) && (iIntValue = ((Number) obj24).intValue()) >= 0 && iIntValue < class606Var.getList().size()) {
                        class606Var.getSelected().add(class606Var.getList().get(iIntValue));
                    }
                }
            }
        }
    }

    public ConfigFile buildConfigFile(ModuleRepository class795Var, WidgetStack class814Var) {
        return new ConfigFile().version(1).modules(serializeModules(class795Var)).widgets(serializeWidgets(class814Var));
    }

    public List<ModuleConfigData> serializeModules(ModuleRepository class795Var) {
        ArrayList arrayList = new ArrayList();
        for (Module class605Var : class795Var.getModules()) {
            ModuleConfigData class218VarBindType = new ModuleConfigData().name(class605Var.getName()).enabled(class605Var.isState()).keys(new ArrayList(class605Var.getKeyBind())).bindType(class605Var.getType());
            for (Setting class661Var : class605Var.getSettings()) {
                if (class661Var != null && class661Var.getName() != null && class661Var.getName().original() != null) {
                    class218VarBindType.settings().add(serializeSetting(class661Var));
                }
            }
            arrayList.add(class218VarBindType);
        }
        return arrayList;
    }

    public List<ModuleConfigEntry> serializeWidgets(WidgetStack class814Var) {
        ArrayList arrayList = new ArrayList();
        for (Draggable class806Var : class814Var.widgets()) {
            ModuleConfigEntry class124VarY = new ModuleConfigEntry().name(class806Var.getName()).x(class806Var.getX()).y(class806Var.getY());
            for (Setting class661Var : class806Var.getSettings()) {
                if (class661Var != null && class661Var.getName() != null && class661Var.getName().original() != null) {
                    class124VarY.settings().add(serializeSetting(class661Var));
                }
            }
            arrayList.add(class124VarY);
        }
        return arrayList;
    }

    static int typeSwitch00351(Object o) {
        if (o == null) return -1;
        if (o instanceof ExpandableSetting) return 0;
        if (o instanceof BooleanSetting) return 1;
        if (o instanceof KeybindSetting) return 2;
        if (o instanceof ModeSetting) return 3;
        if (o instanceof EnumSetting) return 4;
        if (o instanceof MultiSelectSetting) return 5;
        if (o instanceof OrderedEnumSetting) return 6;
        if (o instanceof ColorSetting) return 7;
        if (o instanceof NumberSetting) return 8;
        if (o instanceof TextFieldSetting) return 9;
        if (o instanceof LabelSelectSetting) return 10;
        if (o instanceof LabelMultiSelectSetting) return 11;
        return 12;
    }

    public LayoutNode serializeSetting(Setting class661Var) {
        LayoutNode class301VarType = new LayoutNode().name(class661Var.getName().original()).type(class661Var.getClass().getSimpleName());
        Objects.requireNonNull(class661Var);
        switch (typeSwitch00351(class661Var)) {
            case 0:
                ExpandableSetting class670Var = (ExpandableSetting) class661Var;
                class301VarType.data().put("value", Boolean.valueOf(class670Var.isValue()));
                class301VarType.data().put("keys", class670Var.getKeybinds());
                class301VarType.data().put("bindType", class670Var.getType().name());
                Iterator<Setting> it = class670Var.getSubSettings().iterator();
                while (it.hasNext()) {
                    class301VarType.children().add(serializeSetting(it.next()));
                }
                break;
            case 1:
                BooleanSetting class665Var = (BooleanSetting) class661Var;
                class301VarType.data().put("value", Boolean.valueOf(class665Var.isValue()));
                class301VarType.data().put("key", Integer.valueOf(class665Var.getKey()));
                class301VarType.data().put("bindType", class665Var.getType().name());
                break;
            case 2:
                KeybindSetting class663Var = (KeybindSetting) class661Var;
                class301VarType.data().put("keys", class663Var.getKeyBind());
                class301VarType.data().put("bindType", class663Var.getType().name());
                break;
            case 3:
                class301VarType.data().put("value", ((ModeSetting) class661Var).currentValue().name());
                break;
            case 4:
                class301VarType.data().put("value", ((EnumSetting) class661Var).currentValue().name());
                break;
            case 5:
                ArrayList arrayList = new ArrayList();
                Iterator it2 = ((MultiSelectSetting) class661Var).selectedValues().iterator();
                while (it2.hasNext()) {
                    arrayList.add(((Enum) it2.next()).name());
                }
                class301VarType.data().put("selected", arrayList);
                break;
            case 6:
                OrderedEnumSetting<?> class609Var = (OrderedEnumSetting) class661Var;
                ArrayList arrayList2 = new ArrayList();
                Iterator<Enum<?>> it3 = getSelectedEnums(class609Var).iterator();
                while (it3.hasNext()) {
                    arrayList2.add(it3.next().name());
                }
                ArrayList arrayList3 = new ArrayList();
                Iterator it4 = class609Var.order().iterator();
                while (it4.hasNext()) {
                    arrayList3.add(((Enum) it4.next()).name());
                }
                class301VarType.data().put("selected", arrayList2);
                class301VarType.data().put("order", arrayList3);
                break;
            case 7:
                class301VarType.data().put("color", Integer.valueOf(((ColorSetting) class661Var).getColor()));
                break;
            case 8:
                class301VarType.data().put("value", Float.valueOf(((NumberSetting) class661Var).currentValue()));
                break;
            case 9:
                class301VarType.data().put("text", ((TextFieldSetting) class661Var).getText());
                break;
            case 10:
                class301VarType.data().put("index", Integer.valueOf(((LabelSelectSetting) class661Var).getSelectedIndex()));
                break;
            case 11:
                LabelMultiSelectSetting class606Var = (LabelMultiSelectSetting) class661Var;
                ArrayList arrayList4 = new ArrayList();
                Iterator<Translation> it5 = class606Var.getSelected().iterator();
                while (it5.hasNext()) {
                    arrayList4.add(Integer.valueOf(class606Var.getList().indexOf(it5.next())));
                }
                class301VarType.data().put("indexes", arrayList4);
                break;
        }
        return class301VarType;
    }

    public Set<Enum<?>> getSelectedEnums(OrderedEnumSetting class609Var) {
        return class609Var.getSelected();
    }
}
