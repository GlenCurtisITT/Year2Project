package models;

import java.util.Comparator;

public class EquipmentComparator implements Comparator<Equipment> {
    public int compare(Equipment o1, Equipment o2){
        return o1.getEquipId().compareTo(o2.getEquipId());
    }
}
