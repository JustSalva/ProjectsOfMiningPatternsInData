package Exceptions;

public class NotExistentPositionException extends GenericException{

    public NotExistentPositionException () {
    }

    public NotExistentPositionException ( String message ) {
        super( message );
    }
}
