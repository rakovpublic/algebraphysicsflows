package operations.simple;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import operations.OperationBodies.*;
import static operations.OperationMembers.require;
import java.util.*;

public final class CustomResultOperation<A,B> extends operations.AbstractOperation implements ICustomResultOperation<A> {
    private static final long serialVersionUID=1L;
    private final Algebra<A> first;
    private final Algebra<B> result;
    private final Binary<A,A,B> body;
    public CustomResultOperation(String name,Algebra<A> first,Algebra<B> result,Binary<A,A,B> body) { super(name,first,first,result); this.first=first; this.result=result; this.body=body; }
    @SuppressWarnings("unchecked")
    public <V> IAlgebraItem<V> performOperation(A a,A b) {
        return (IAlgebraItem<V>)result.buildAlgebraItem(require(result,body.apply(require(first,a),require(first,b))));
    }
}
