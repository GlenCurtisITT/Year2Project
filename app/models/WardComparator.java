package models;

import java.util.Comparator;

public class WardComparator implements Comparator<Ward> {

    public int compare(Ward o1, Ward o2){
        return o1.getName().compareTo(o2.getName());
    }

}
