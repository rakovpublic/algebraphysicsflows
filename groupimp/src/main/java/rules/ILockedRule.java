package rules;

/**
 * Created by Rakovskyi Dmytro on 27.03.2017.
 */
public interface ILockedRule<T> {
    boolean validate();
}
