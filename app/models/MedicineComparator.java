package models;

import java.util.Comparator;

public class MedicineComparator implements Comparator<Medicine> {

    public int compare(Medicine o1, Medicine o2){
        return o1.getName().compareTo(o2.getName());
    }

}
