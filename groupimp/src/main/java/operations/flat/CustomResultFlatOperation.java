package operations.flat;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import operations.AbstractOperation;
import operations.OperationBodies.Binary;
import java.util.*;
import static operations.OperationMembers.require;

/** A x A -> List(B), using the existing custom-result flat interface. */
public final class CustomResultFlatOperation<A,B> extends AbstractOperation implements ICustomResultFlatOperation<A> {
    private static final long serialVersionUID=1L;
    private final Algebra<A> first;
    private final Algebra<B> result;
    private final Binary<A,A,List<B>> body;
    public CustomResultFlatOperation(String name,Algebra<A> first,Algebra<B> result,Binary<A,A,List<B>> body) {
        super(name,first,first,result); this.first=first; this.result=result; this.body=body;
    }
    @SuppressWarnings("unchecked")
    public <V> List<IAlgebraItem<V>> performOperation(A a,A b) {
        List<IAlgebraItem<V>> values=new ArrayList<>();
        for(B value : body.apply(require(first,a),require(first,b))) values.add((IAlgebraItem<V>)result.buildAlgebraItem(require(result,value)));
        return values;
    }
}

