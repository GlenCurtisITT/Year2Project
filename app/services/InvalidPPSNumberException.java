package services;

/**
 * Created by conno on 20/04/2017.
 */
public class InvalidPPSNumberException extends Exception {
    public InvalidPPSNumberException(String error){
        super(error);
    }
}
