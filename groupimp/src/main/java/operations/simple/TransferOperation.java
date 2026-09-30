package operations.simple;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import operations.OperationBodies.*;
import static operations.OperationMembers.require;
import java.util.*;

public final class TransferOperation<A,B> extends operations.AbstractOperation implements ITransferOperation<A> {
    private static final long serialVersionUID=1L;
    private final Algebra<A> first;
    private final Algebra<B> result;
    private final Unary<A,B> body;
    public TransferOperation(String name,Algebra<A> first,Algebra<B> result,Unary<A,B> body) { super(name,first,null,result); this.first=first; this.result=result; this.body=body; }
    @SuppressWarnings("unchecked")
    public <V> IAlgebraItem<V> performOperation(A input) {
        return (IAlgebraItem<V>)result.buildAlgebraItem(require(result,body.apply(require(first,input))));
    }
}
