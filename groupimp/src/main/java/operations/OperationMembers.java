package operations;

import algebra.imp.Algebra;
import exceptions.NotMemberException;

public final class OperationMembers {
    private OperationMembers() { }
    public static <T> T require(Algebra<T> algebra,Object value) {
        if(!algebra.getParamClass().isInstance(value)) throw new NotMemberException("Expected a member of "+algebra.getAlgebraName());
        @SuppressWarnings("unchecked") T typed=(T)value;
        if(!algebra.validate(typed)) throw new NotMemberException("Invalid member of "+algebra.getAlgebraName());
        return typed;
    }
}

