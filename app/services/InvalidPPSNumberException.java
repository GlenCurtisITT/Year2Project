package services;

public class InvalidPPSNumberException extends Exception {
    public InvalidPPSNumberException(String error){
        super(error);
    }
}
