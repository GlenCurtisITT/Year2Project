package models.users;
import com.avaje.ebean.Model;
import org.mindrot.jbcrypt.BCrypt;
import models.*;
import java.util.*;
import javax.persistence.*;
import java.util.Date;

/**
 * Created by conno on 11/03/2017.
 */
@Entity
@DiscriminatorValue("Consultant")
@PrimaryKeyJoinColumn(referencedColumnName = "role")
public class Consultant extends User{
    private String specialization;

    @OneToMany(mappedBy = "acc")
    ArrayList<Patient> patients = new ArrayList<>();


    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }


}
