package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.event.ClientTickEvent;
import aethereal.ui.CoordsWidget;
import aethereal.Expensive;
import aethereal.ui.EventTimerWidget;
import aethereal.ui.HotkeysWidget;
import aethereal.type.HudWidgetType;
import aethereal.ui.ItemBindWidget;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.MouseButtonEvent2;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.ui.NotificationWidget;
import aethereal.ui.PotionListWidget;
import aethereal.event.Render2DEvent;
import aethereal.ui.StaffListWidget;
import aethereal.event.RenderOverlayEvent;
import aethereal.event.StatusEffectOverlayEvent;
import aethereal.ui.TargetHudWidget;
import aethereal.ui.TrapTimerWidget;
import aethereal.ui.WatermarkWidget;
import aethereal.ui.WidgetStack;
import aethereal.event.WindowResizeEvent;
import aethereal.type.RenderOverlayType;
import aethereal.ui.ScoreboardWidget;
import aethereal.render.BlurEffect;
import aethereal.ui.HotbarWidget;
import aethereal.ui.ArmorDurabilityWidget;

@Aliases(aliases = {"HUD", "Widgets", "Overlay", "Target Hud", "Water Mark", "Potion List", "Staff List", "Coords", "Armor Status", "Armor Hud", "Hot keys", "Key binds"})
public class WidgetsModule extends Module {
    public final MultiSelectSetting<HudWidgetType> elements;
    public final NotificationWidget notificationWidget;

    public WidgetsModule() {
        super(ModuleTab.RENDER, "Widgets");
        this.elements = new MultiSelectSetting(Lang.WIDGETS_ELEMENTS, Lang.WIDGETS_ELEMENTS_DESC).values(HudWidgetType.class);
        addSettings(this.elements);
        WidgetStack class814VarWidgetStack = Expensive.INSTANCE.widgetStack();
        class814VarWidgetStack.newWidget(new WatermarkWidget(() -> {
            return isState() && this.elements.isSelected(HudWidgetType.WATERMARK);
        }));
        class814VarWidgetStack.newWidget(new PotionListWidget(() -> {
            return isState() && this.elements.isSelected(HudWidgetType.POTION_LIST);
        }));
        class814VarWidgetStack.newWidget(new HotkeysWidget(() -> {
            return isState() && this.elements.isSelected(HudWidgetType.HOTKEYS);
        }));
        class814VarWidgetStack.newWidget(new TargetHudWidget(() -> {
            return isState() && this.elements.isSelected(HudWidgetType.TARGET_HUD);
        }));
        class814VarWidgetStack.newWidget(new CoordsWidget(() -> {
            return isState() && this.elements.isSelected(HudWidgetType.COORDS);
        }));
        class814VarWidgetStack.newWidget(new ItemBindWidget(() -> {
            return isState() && this.elements.isSelected(HudWidgetType.ITEM_BIND);
        }));
        class814VarWidgetStack.newWidget(new StaffListWidget(() -> {
            return isState() && this.elements.isSelected(HudWidgetType.STAFF_LIST);
        }));
        NotificationWidget class644Var = new NotificationWidget(() -> {
            return isState() && this.elements.isSelected(HudWidgetType.NOTIFICATION);
        });
        this.notificationWidget = class644Var;
        class814VarWidgetStack.newWidget(class644Var);
        class814VarWidgetStack.newWidget(new TrapTimerWidget(() -> {
            return isState() && this.elements.isSelected(HudWidgetType.TRAP_TIMER);
        }));
        class814VarWidgetStack.newWidget(new EventTimerWidget(() -> {
            return isState() && this.elements.isSelected(HudWidgetType.EVENT_TIMER);
        }));
        class814VarWidgetStack.newWidget(new ScoreboardWidget(() -> {
            return isState() && this.elements.isSelected(HudWidgetType.SCOREBOARD);
        }));
        class814VarWidgetStack.newWidget(new HotbarWidget(() -> {
            return isState() && this.elements.isSelected(HudWidgetType.HOTBAR);
        }));
        class814VarWidgetStack.newWidget(new ArmorDurabilityWidget(() -> {
            return isState() && this.elements.isSelected(HudWidgetType.ARMOR_DURABILITY);
        }));
        register(Render2DEvent.class, class311Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded() && class311Var.isPost() && class814VarWidgetStack.widgets().stream().anyMatch((v0) -> {
                return v0.isVisible();
            })) {
                if (ThemesModule.hudBlurActive()) {
                    BlurEffect blur = Expensive.INSTANCE.hudBlur();
                    if (blur != null) {
                        blur.apply(ThemesModule.hudBlurPasses());
                    }
                }
                class814VarWidgetStack.draw();
            }
        });
        register(ClientTickEvent.class, class181Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded()) {
                class814VarWidgetStack.update();
            }
        });
        register(WindowResizeEvent.class, class061Var -> {
            class814VarWidgetStack.handleResize(class061Var.width(), class061Var.height());
        });
        register(MouseButtonEvent2.class, class300Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded()) {
                class814VarWidgetStack.click(class300Var);
            }
        });
        register(StatusEffectOverlayEvent.class, class315Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded() && this.elements.isSelected(HudWidgetType.POTION_LIST)) {
                class315Var.cancel();
            }
        });
        register(RenderOverlayEvent.class, class252Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded() && class252Var.getType().isScoreboard() && this.elements.isSelected(HudWidgetType.SCOREBOARD)) {
                class252Var.cancel();
            }
            if (isState() && Mc.INSTANCE.isWorldLoaded() && class252Var.getType().isHotbar() && this.elements.isSelected(HudWidgetType.HOTBAR)) {
                class252Var.cancel();
            }
            if (isState() && Mc.INSTANCE.isWorldLoaded() && class252Var.getType().isHeldItemTooltip() && this.elements.isSelected(HudWidgetType.HOTBAR)) {
                class252Var.cancel();
            }
        });
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    public MultiSelectSetting<HudWidgetType> getElements() {
        return this.elements;
    }

    public NotificationWidget getNotificationWidget() {
        return this.notificationWidget;
    }
}
