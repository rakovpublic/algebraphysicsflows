package exceptions;

/**
 * Created by Rakovskyi Dmytro on 23.03.2017.
 */
public class UnsupportedOperationException extends NullPointerException{
    public UnsupportedOperationException() {
    }

    public UnsupportedOperationException(String s) {
        super(s);
    }
}
