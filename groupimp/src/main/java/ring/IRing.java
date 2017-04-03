package ring;

import rules.IValidationRule;

import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 25.03.2017.
 */
public interface IRing<T >{
    public IRing<T> multiply(IRing<T> ringMember);
    public List<IValidationRule<T>> getMemberRules();
    public T getMemberValue();
    public IRing<T> sum(IRing<T> ringMember);

}
