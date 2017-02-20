package models.users;

import java.util.*;
import javax.persistence.*;

import com.avaje.ebean.Model;
import play.data.format.*;
import play.data.validation.*;
import org.mindrot.jbcrypt.BCrypt;

@Entity
public class User extends Model{

    @Id
    private String email;

    @Constraints.Required
    private String role;

    @Constraints.Required
    private String name;

    private String passwordHash;

    //http://rny.io/playframework/bcrypt/2013/10/22/better-password-hashing-in-play-2.html
    public static User create(String email, String role, String name, String password){
        User user = new User();
        user.setEmail(email);
        user.setRole(role);
        user.setName(name);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
