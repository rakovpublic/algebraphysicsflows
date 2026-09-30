package operations.simple;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import operations.OperationBodies.*;
import static operations.OperationMembers.require;
import java.util.*;

public final class MixedOperation<A,B,C> extends operations.AbstractOperation implements IUnsafeOperation<A> {
    private static final long serialVersionUID=1L;
    private final Algebra<A> first;
    private final Algebra<B> other;
    private final Algebra<C> result;
    private final Binary<A,B,C> body;
    public MixedOperation(String name,Algebra<A> first,Algebra<B> other,Algebra<C> result,Binary<A,B,C> body) {
        super(name,first,other,result); this.first=first; this.other=other; this.result=result; this.body=body;
    }
    @SuppressWarnings("unchecked")
    public <K,V> IAlgebraItem<K> performOperation(A a,V b) {
        return (IAlgebraItem<K>)result.buildAlgebraItem(require(result,body.apply(require(first,a),require(other,b))));
    }
}
