package operations.simple;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import operations.AbstractOperation;
import operations.OperationBodies.Binary;
import static operations.OperationMembers.require;

/** A x B -> B using the corrected ILeftProjectionOperation contract. */
public final class SecondResultOperation<A,B> extends AbstractOperation implements ILeftProjectionOperation<A,B> {
    private static final long serialVersionUID=1L;
    private final Algebra<A> first;
    private final Algebra<B> second;
    private final Binary<A,B,B> body;
    public SecondResultOperation(String name,Algebra<A> first,Algebra<B> second,Binary<A,B,B> body) {
        super(name,first,second,second); this.first=first; this.second=second; this.body=body;
    }
    public IAlgebraItem<B> performOperation(A a,B b) {
        return second.buildAlgebraItem(require(second,body.apply(require(first,a),require(second,b))));
    }
}

