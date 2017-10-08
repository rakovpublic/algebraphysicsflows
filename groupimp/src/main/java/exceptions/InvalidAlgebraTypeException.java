package exceptions;

/**
 * Created by Rakovskyi Dmytro on 27.09.2017.
 */
public class InvalidAlgebraTypeException extends NullPointerException {
    public InvalidAlgebraTypeException() {
        super();
    }

    public InvalidAlgebraTypeException(String s) {
        super(s);
    }
}
