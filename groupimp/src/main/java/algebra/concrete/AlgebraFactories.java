package algebra.concrete;

import algebra.imp.Algebra;
import operations.OperationBodies.Predicate;
import rules.TypedMembershipRule;

/** Construction convenience; all membership belongs to the original Algebra. */
final class AlgebraFactories {
    private AlgebraFactories() { }
    static <T> Algebra<T> carrier(String name,Class<T> type,String description,Predicate<T> predicate) {
        Algebra<T> algebra=new Algebra<>(name,type,description);
        algebra.addValidationRule(new TypedMembershipRule<>(type,predicate,description));
        return algebra;
    }
}

