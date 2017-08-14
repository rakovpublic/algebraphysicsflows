package exceptions;

/**
 * Created by Rakovskyi Dmytro on 14.08.2017.
 */
public class AlgebraNotExistsException extends NullPointerException {
    public AlgebraNotExistsException() {
        super();
    }

    public AlgebraNotExistsException(String s) {
        super(s);
    }
}
