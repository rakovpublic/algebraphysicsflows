package operations.flat;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import operations.OperationBodies.*;
import static operations.OperationMembers.require;
import java.util.*;

public final class MixedFlatOperation<A,B,C> extends operations.AbstractOperation implements IUnsafeFlatOperation<A> {
    private static final long serialVersionUID=1L;
    private final Algebra<A> first;
    private final Algebra<B> other;
    private final Algebra<C> result;
    private final Binary<A,B,List<C>> body;
    public MixedFlatOperation(String name,Algebra<A> first,Algebra<B> other,Algebra<C> result,Binary<A,B,List<C>> body) {
        super(name,first,other,result); this.first=first; this.other=other; this.result=result; this.body=body;
    }
    @SuppressWarnings("unchecked")
    public <K,V> List<IAlgebraItem<K>> performOperation(A a,V b) {
        List<IAlgebraItem<K>> values=new ArrayList<>();
        for(C value : body.apply(require(first,a),require(other,b))) values.add((IAlgebraItem<K>)result.buildAlgebraItem(require(result,value)));
        return values;
    }
}
