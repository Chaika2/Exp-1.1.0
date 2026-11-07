package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.event.AspectRatioEvent;
import aethereal.type.AspectRatioPreset;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.NumberSetting;

@Aliases(aliases = {"Aspect Ratio", "Screen Ratio", "Custom Aspect", "Resolution Modifier", "Ratio Adjuster", "Display Aspect", "Resolution Adjustment"})
public class AspectRatioModule extends Module {
    public final ModeSetting<AspectRatioPreset> resolutionPreset;
    public final NumberSetting customResolution;

    public AspectRatioModule() {
        super(ModuleTab.RENDER, "Aspect Ratio");
        this.resolutionPreset = new ModeSetting(Lang.ASPECTRATIO_RESOLUTION).values(AspectRatioPreset.class);
        this.customResolution = (NumberSetting) new NumberSetting(Lang.ASPECTRATIO_RESOLUTION_VALUE).range(0.7f, 1.5f).currentValue(0.7f).step(0.05f).setVisible(() -> {
            return Boolean.valueOf(this.resolutionPreset.isSelected(AspectRatioPreset.CUSTOM));
        });
        addSettings(this.resolutionPreset, this.customResolution);
        register(AspectRatioEvent.class, class109Var -> {
            if (Mc.INSTANCE.isWorldLoaded() && isState()) {
                if (this.resolutionPreset.isSelected(AspectRatioPreset.CUSTOM)) {
                    class109Var.setAspectRatio(this.customResolution.currentValue());
                    return;
                }
                String[] strArrSplit = ((AspectRatioPreset) this.resolutionPreset.currentValue()).getDisplayName().effective().split(":");
                class109Var.setAspectRatio(Integer.parseInt(strArrSplit[0]) / Integer.parseInt(strArrSplit[1]));
            }
        });
    }
}
