package exceptions;

/**
 * Created by Rakovskyi Dmytro on 24.03.2017.
 */
public class NotMemberException extends NullPointerException {
    public NotMemberException() {
        super();
    }

    public NotMemberException(String s) {
        super(s);
    }
}
