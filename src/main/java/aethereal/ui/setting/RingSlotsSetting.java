package aethereal.ui.setting;

import aethereal.model.Translation;

import java.util.ArrayList;
import java.util.List;

/**
 * Предметы колеса свапа (Swap Wheel): по идентификатору на каждый сектор.
 * Сектор без предмета - пустая строка. Значение хранится строкой вида
 * "id1;id2;id3..." - наследует TextFieldSetting, поэтому пишется и читается
 * из конфига штатным механизмом сериализации.
 * Редактируется прямо в игре: открой колесо и крути колесо мыши над сектором,
 * предмет подбирается из текущего инвентаря.
 */
public class RingSlotsSetting extends TextFieldSetting {

    public RingSlotsSetting(Translation class254Var, Translation class254Var2) {
        super(class254Var, class254Var2);
        setText("");
    }

    /**
     * Идентификатор предмета сектора: "namespace:item"
     * (+ "|d" если предмет с CUSTOM_DATA - Сфера и Шар отличаются только им).
     */
    public String slotId(int index) {
        List<String> list = parse();
        return index >= 0 && index < list.size() ? list.get(index) : "";
    }

    public void setSlotId(int index, String id) {
        ArrayList<String> list = new ArrayList<>(parse());
        while (list.size() <= index) {
            list.add("");
        }
        list.set(index, id == null ? "" : id);
        setText(String.join(";", list));
    }

    /** Очищает сектор (например, если в инвентаре больше нет такой вещи). */
    public void clearSlot(int index) {
        setSlotId(index, "");
    }

    private List<String> parse() {
        String text = getText();
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        List<String> list = new ArrayList<>();
        for (String part : text.split(";")) {
            list.add(part.trim());
        }
        return list;
    }
}
