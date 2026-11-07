package aethereal.ui.setting;
import aethereal.model.Translation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class LabelSelectSetting extends Setting {
    public List<Translation> list;
    public Runnable action;
    public Translation selected;

    public LabelSelectSetting(Translation class254Var, Translation class254Var2) {
        super(class254Var, class254Var2);
    }

    public LabelSelectSetting(Translation class254Var) {
        this(class254Var, null);
    }

    public LabelSelectSetting value(Translation... class254VarArr) {
        List<Translation> listAsList = Arrays.asList(class254VarArr);
        this.selected = (Translation) listAsList.getFirst();
        this.list = listAsList;
        return this;
    }

    public LabelSelectSetting selected(int i) {
        this.selected = this.list.get(i);
        return this;
    }

    public LabelSelectSetting visible(Supplier<Boolean> supplier) {
        setVisible(supplier);
        return this;
    }

    public boolean isSelected(Translation class254Var) {
        return this.selected.original().equalsIgnoreCase(class254Var.original());
    }

    public void setSelected(Translation class254Var) {
        this.selected = class254Var;
        if (this.action != null) {
            this.action.run();
        }
    }

    public int getSelectedIndex() {
        return this.list.indexOf(this.selected);
    }

    @Override
    public Map<String, Object> toSerializedData() {
        return Map.of("index", Integer.valueOf(getSelectedIndex()));
    }

    @Override
    public void loadSerializedData(Map<String, Object> map) {
        Object obj = map.get("index");
        if (obj instanceof Number) {
            selected(((Number) obj).intValue());
        }
    }

    public List<Translation> getList() {
        return this.list;
    }

    public Runnable getAction() {
        return this.action;
    }

    public Translation getSelected() {
        return this.selected;
    }

    public void setAction(Runnable runnable) {
        this.action = runnable;
    }
}
