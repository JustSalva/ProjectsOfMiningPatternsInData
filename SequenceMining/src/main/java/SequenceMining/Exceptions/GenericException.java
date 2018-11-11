package SequenceMining.Exceptions;

public abstract class GenericException extends Exception {

    public GenericException () {
        super();
    }

    public GenericException ( String message ) {
        super( message );
    }
}
