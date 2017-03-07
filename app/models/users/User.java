package models.users;

import java.util.*;
import javax.persistence.*;
import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;
import org.mindrot.jbcrypt.BCrypt;

@Entity
public class User extends Model{
    private String fname;
    private String lname;
    private String phoneNumber;
    private String address;
    private String ppsNumber;
    @Formats.DateTime(pattern="yyyy/dd/MM")
    private Date dateOfBirth;
    private Date startDate;
    @Id
    private String email;
    private String role;
    private String passwordHash;

    //http://rny.io/playframework/bcrypt/2013/10/22/better-password-hashing-in-play-2.html
    public static User create(String email, String role, String fname,String lname , String address, String phoneNumber, String ppsNumber, Date dateOfBirth, String password){
        User user = new User();
        user.setEmail(email);
        user.setRole(role);
        user.setFname(fname);
        user.setLname(lname);
        user.setPhoneNumber(phoneNumber);
        user.setAddress(address);
        user.setPpsNumber(ppsNumber);
        user.setDateOfBirth(dateOfBirth);
        user.setStartDate();
        user.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.save();
        return user;
    }

    public static User authenticate(String email, String password){
        User user = User.find.where().eq("email", email).findUnique();
        if(user != null && BCrypt.checkpw(password, user.getPasswordHash())){
            return user;
        }else{
            return null;
        }
    }

    public static User getUserById(String id){
        if(id == null)
            return null;
        else
            return find.byId(id);
    }


    public static Finder<String, User> find = new Finder<String, User>(User.class);

    public static List<User> findAll(){
        return User.find.all();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPpsNumber() {
        return ppsNumber;
    }

    public void setPpsNumber(String ppsNumber) {
        this.ppsNumber = ppsNumber;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate() {
        startDate = new Date();
    }
}
