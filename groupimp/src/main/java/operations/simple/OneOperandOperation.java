package operations.simple;

import algebra.imp.Algebra;
import operations.AbstractOperation;
import operations.OperationBodies.Unary;
import static operations.OperationMembers.require;

/** Native A -> A implementation with input and closure checks against the same Algebra. */
public final class OneOperandOperation<T> extends AbstractOperation implements IOneOperandOperation<T> {
    private static final long serialVersionUID=1L;
    private final Algebra<T> algebra;
    private final Unary<T,T> body;
    public OneOperandOperation(String name,Algebra<T> algebra,Unary<T,T> body) {
        super(name,algebra,null,algebra); this.algebra=algebra; this.body=body;
    }
    public T performOperation(T input) { return require(algebra,body.apply(require(algebra,input))); }
}

