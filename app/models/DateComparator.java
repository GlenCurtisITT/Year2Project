package models;
import java.util.*;

public class DateComparator implements Comparator<Appointment>{

    public int compare(Appointment o1, Appointment o2){
        return o1.getAppDate().compareTo(o2.getAppDate());
    }
}
