package models;
import java.util.*;
/**
 * Created by wdd on 03/04/17.
 */
public class DateComparator implements Comparator<Appointment>{

    public int compare(Appointment o1, Appointment o2){
        return o1.getAppDate().compareTo(o2.getAppDate());
    }
}
