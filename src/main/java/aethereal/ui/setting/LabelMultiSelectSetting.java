package aethereal.ui.setting;
import aethereal.model.Translation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class LabelMultiSelectSetting extends Setting {
    public List<Translation> list;
    public List<Translation> selected;

    public LabelMultiSelectSetting(Translation class254Var, Translation class254Var2) {
        super(class254Var, class254Var2);
        this.selected = new ArrayList();
    }

    public LabelMultiSelectSetting(Translation class254Var) {
        this(class254Var, null);
    }

    public LabelMultiSelectSetting value(Translation... class254VarArr) {
        this.list = Arrays.asList(class254VarArr);
        return this;
    }

    public LabelMultiSelectSetting visible(Supplier<Boolean> supplier) {
        setVisible(supplier);
        return this;
    }

    public boolean isSelected(Translation class254Var) {
        return this.selected.stream().anyMatch(class254Var2 -> {
            return class254Var2.original().equals(class254Var.original());
        });
    }

    @Override
    public Map<String, Object> toSerializedData() {
        ArrayList arrayList = new ArrayList();
        Iterator<Translation> it = this.selected.iterator();
        while (it.hasNext()) {
            arrayList.add(Integer.valueOf(this.list.indexOf(it.next())));
        }
        return Map.of("indexes", arrayList);
    }

    @Override
    public void loadSerializedData(Map<String, Object> map) {
        int iIntValue;
        Object obj = map.get("indexes");
        if (obj instanceof List) {
            this.selected.clear();
            for (Object obj2 : (List) obj) {
                if ((obj2 instanceof Number) && (iIntValue = ((Number) obj2).intValue()) >= 0 && iIntValue < this.list.size()) {
                    this.selected.add(this.list.get(iIntValue));
                }
            }
        }
    }

    public List<Translation> getList() {
        return this.list;
    }

    public List<Translation> getSelected() {
        return this.selected;
    }

    public void setList(List<Translation> list) {
        this.list = list;
    }

    public void setSelected(List<Translation> list) {
        this.selected = list;
    }
}
