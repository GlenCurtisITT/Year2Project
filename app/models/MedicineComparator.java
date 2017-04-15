package models;

import java.util.Comparator;

/**
 * Created by Glen on 15/04/2017.
 */
public class MedicineComparator implements Comparator<Medicine> {

    public int compare(Medicine o1, Medicine o2){
        return o1.getName().compareTo(o2.getName());
    }

}
