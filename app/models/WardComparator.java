package models;

import java.util.Comparator;

/**
 * Created by Glen on 15/04/2017.
 */
public class WardComparator implements Comparator<Ward> {

    public int compare(Ward o1, Ward o2){
        return o1.getName().compareTo(o2.getName());
    }

}
