package field;

import ring.IRing;
import rules.IValidationRule;

import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 29.03.2017.
 */
public interface IField<T> {
    public IField<T> multiply(IField<T> ringMember);
    public List<IValidationRule<T>> getMemberRules();
    public T getMemberValue();
    public IField<T> sum(IField<T> ringMember);
}
