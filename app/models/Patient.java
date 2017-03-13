package models;

import javax.persistence.*;
import com.avaje.ebean.Model;
import models.users.Consultant;
import play.data.format.Formats;

import java.util.Date;
import java.util.List;

/**
 * Created by wdd on 03/03/17.
 */
@Entity
@SequenceGenerator(name = "mrn_gen", allocationSize=1, initialValue=1)
public class Patient extends Model{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mrn_gen")
    private String mrn;
    private String fName;
    private String lName;
    private String ppsNumber;
    @Formats.DateTime(pattern="yyyy/dd/MM")
    private Date dob;
    private String address;
    private String email;
    private String homePhone;
    private String mobilePhone;
    private String nokFName;
    private String nokLName;
    private String nokAddress;
    private String nokNumber;
    private Boolean medicalCard;
    private String prevIllnesses;

    @ManyToOne()    //signifies relationship with Consultant table
    @JoinColumn(name = "idNum")    //name of column which links tables
    Consultant c;

    public static Patient create(String fname, String lname, String ppsNumber, Date dob, String address, String email, String homePhone, String mobilePhone, String nokFName, String nokLName, String nokAddress, String nokNumber, boolean medicalCard, String prevIllness){
        Patient patient = new Patient();
        patient.setfName(fname);
        patient.setlName(lname);
        patient.setPpsNumber(ppsNumber);
        patient.setDob(dob);
        patient.setAddress(address);
        patient.setEmail(email);
        patient.setHomePhone(homePhone);
        patient.setMobilePhone(mobilePhone);
        patient.setNokFName(nokFName);
        patient.setNokLName(nokLName);
        patient.setNokAddress(nokAddress);
        patient.setNokNumber(nokNumber);
        patient.setMedicalCard(medicalCard);
        patient.setPrevIllnesses(prevIllness);
        patient.save();
        return patient;
    }


    public static Finder<String, Patient> find = new Finder<String, Patient>(Patient.class);

    public static List<Patient> findAll(){
        return Patient.find.all();
    }

    public String getMrn() {
        return mrn;
    }

    /*public void genMrn() {
        double number = Double.parseDouble(this.mrn);
        number++;
        this.mrn = Double.toString(number);
    }*/

    public String getfName() {
        return fName;
    }

    public void setfName(String fname) {
        this.fName = fname;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lname) {
        this.lName = lname;
    }

    public String getPpsNumber() {
        return ppsNumber;
    }

    public void setPpsNumber(String ppsNumber) {
        this.ppsNumber = ppsNumber;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getNokFName() {
        return nokFName;
    }

    public void setNokFName(String nokFName) {
        this.nokFName = nokFName;
    }

    public String getNokLName() {
        return nokLName;
    }

    public void setNokLName(String nokLName) {
        this.nokLName = nokLName;
    }

    public String getNokAddress() {
        return nokAddress;
    }

    public void setNokAddress(String nokAddress) {
        this.nokAddress = nokAddress;
    }

    public String getNokNumber() {
        return nokNumber;
    }

    public void setNokNumber(String nokNumber) {
        this.nokNumber = nokNumber;
    }

    public Boolean getMedicalCard() {
        return medicalCard;
    }

    public void setMedicalCard(Boolean medicalCard) {
        this.medicalCard = medicalCard;
    }

    public String getPrevIllnesses() {
        return prevIllnesses;
    }

    public void setPrevIllnesses(String prevIllnesses) {
        this.prevIllnesses = prevIllnesses;
    }
}
