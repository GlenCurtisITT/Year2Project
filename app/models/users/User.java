package models.users;

import java.util.*;
import javax.persistence.*;
import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;
import org.mindrot.jbcrypt.BCrypt;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role")
@DiscriminatorValue("Admin")
public class User extends Model{
    @Id
    private String idNum;
    private String fname;
    private String lname;
    private String phoneNumber;
    private String address;
    private String ppsNumber;
    @Formats.DateTime(pattern="yyyy/dd/MM")
    private Date dateOfBirth;
    private Date startDate;
    private String email;
    private String passwordHash;

    public User(String fname, String lname, String phoneNumber, String address, String ppsNumber, Date dateOfBirth, String email, String password) {
        this.idNum = idGen();
        this.fname = fname;
        this.lname = lname;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.ppsNumber = ppsNumber;
        this.dateOfBirth = dateOfBirth;
        this.startDate = new Date();
        this.email = email;
        this.passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
    }


    //http://rny.io/playframework/bcrypt/2013/10/22/better-password-hashing-in-play-2.html
    public static User create(User u){
        u.save();
        return u;
    }

    public static User authenticate(String email, String password){
        User user = User.find.where().eq("email", email).findUnique();
        if(user != null && BCrypt.checkpw(password, user.getPasswordHash())){
            if(user.checkRole().equals("Consultant")){
                Consultant c = Consultant.find.where().eq("email", email).findUnique();
                return c;
            } else {
                return user;
            }
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

    private static String idGen(){
        Random rand = new Random();
        List<User> allusers = findAll();
        int randNum = 0;
        boolean check = true;
        do{
            randNum = rand.nextInt((99999999 - 10000001) + 1) + 10000001;
            check = true;
            for(User a: allusers){
                if(randNum == Integer.parseInt(a.getIdNum())) {
                    check = false;
                }
            }
        }while(!check);
        String numberAsString = Integer.toString(randNum);
        return numberAsString;
    }

    public String checkRole(){
        if(this instanceof Consultant){
            return "Consultant";
        }else{
            return "Admin";
        }
    }

    public String getIdNum() {
        return idNum;
    }

    public void setIdNum(String idNum) {
        this.idNum = idNum;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
