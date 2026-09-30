package operations.simple;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import operations.OperationBodies.*;
import static operations.OperationMembers.require;
import java.util.*;

public final class ClosedOperation<T> extends operations.AbstractOperation implements IOperation<T> {
    private static final long serialVersionUID=1L;
    private final Algebra<T> algebra;
    private final Binary<T,T,T> body;
    public ClosedOperation(String name,Algebra<T> algebra,Binary<T,T,T> body) { super(name,algebra,algebra,algebra); this.algebra=algebra; this.body=body; }
    public T performOperation(T a,T b) { return require(algebra,body.apply(require(algebra,a),require(algebra,b))); }
}
