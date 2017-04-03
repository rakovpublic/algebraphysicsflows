package rules;

/**
 * Created by Rakovskyi Dmytro on 24.03.2017.
 */
public interface IValidationRule<T> {
    public boolean validate(T member);
    public String getDescription();
}
