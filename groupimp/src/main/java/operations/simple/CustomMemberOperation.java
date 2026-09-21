package operations.simple;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import operations.AbstractOperation;
import operations.OperationBodies.Binary;
import static operations.OperationMembers.require;

/** A x B -> A, implemented through the existing custom-member interface. */
public final class CustomMemberOperation<A,B> extends AbstractOperation implements ICustomMemberOperation<A> {
    private static final long serialVersionUID=1L;
    private final Algebra<A> first;
    private final Algebra<B> second;
    private final Binary<A,B,A> body;
    public CustomMemberOperation(String name,Algebra<A> first,Algebra<B> second,Binary<A,B,A> body) {
        super(name,first,second,first); this.first=first; this.second=second; this.body=body;
    }
    public <V> IAlgebraItem<A> performOperation(A a,V b) {
        return first.buildAlgebraItem(require(first,body.apply(require(first,a),require(second,b))));
    }
}

