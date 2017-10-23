package rules;

/**
 * Created by Rakovskyi Dmytro on 24.03.2017.
 */
public interface IValidationRule<T> {
    /**
     * Validate value
     * if it's match the rule return true
     *
     * @param member
     * @return validation result boolean
     */
    public boolean validate(T member);

    /**
     * return description of validation rule
     *
     * @return rule description type String
     */
    public String getDescription();
}
