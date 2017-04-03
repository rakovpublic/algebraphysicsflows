package groups;

import rules.IValidationRule;

import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 24.03.2017.
 */
public interface IGroup <T > {
    public IGroup<T> multiply(IGroup<T> groupMember);
    public List<IValidationRule<T>> getMemberRules();
    public T getMemberValue();
}
