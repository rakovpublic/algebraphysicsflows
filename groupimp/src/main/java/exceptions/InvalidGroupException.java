package exceptions;

/**
 * Created by Rakovskyi Dmytro on 29.03.2017.
 */
public class InvalidGroupException extends NullPointerException {
    public InvalidGroupException() {
        super();
    }

    public InvalidGroupException(String s) {
        super(s);
    }
}
