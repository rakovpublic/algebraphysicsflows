package operations.flat;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import operations.AbstractOperation;
import operations.OperationBodies.Unary;
import java.util.*;
import static operations.OperationMembers.require;

/** Native A -> List(B) implementation of the corrected existing flat-transfer interface. */
public final class TransferFlatOperation<A,B> extends AbstractOperation implements ITransferFlatOperation<A> {
    private static final long serialVersionUID=1L;
    private final Algebra<A> first;
    private final Algebra<B> result;
    private final Unary<A,List<B>> body;
    public TransferFlatOperation(String name,Algebra<A> first,Algebra<B> result,Unary<A,List<B>> body) {
        super(name,first,null,result); this.first=first; this.result=result; this.body=body;
    }
    @SuppressWarnings("unchecked")
    public <V> List<IAlgebraItem<V>> performOperation(A input) {
        List<IAlgebraItem<V>> values=new ArrayList<>();
        for(B value : body.apply(require(first,input))) values.add((IAlgebraItem<V>)result.buildAlgebraItem(require(result,value)));
        return values;
    }
}

