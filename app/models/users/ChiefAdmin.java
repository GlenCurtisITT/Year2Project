package models.users;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import java.util.Date;
import java.util.List;

/**
 * Created by Glen on 14/04/2017.
 */
@Entity
@DiscriminatorValue("ChiefAdmin")
@PrimaryKeyJoinColumn(referencedColumnName = "idNum")
public class ChiefAdmin extends User{

    public ChiefAdmin(String fname, String lname, String phoneNumber, String address, String ppsNumber, Date dateOfBirth, String email, String password) {
        super(fname, lname, phoneNumber, address, ppsNumber, dateOfBirth, email, password);
    }

    public static Finder<String, ChiefAdmin> find = new Finder<String, ChiefAdmin>(ChiefAdmin.class);

    public static List<ChiefAdmin> findAllChiefAdmins(){
        return ChiefAdmin.find.all();
    }

    public static ChiefAdmin create(ChiefAdmin ca){
        ca.save();
        return ca;
    }

    public static ChiefAdmin getChiefAdminById(String id){
        if(id == null)
            return null;
        else
            return find.byId(id);
    }
}
