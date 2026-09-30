package operations.flat;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import operations.AbstractOperation;
import operations.OperationBodies.Binary;
import java.util.*;
import static operations.OperationMembers.require;

/** A x B -> List(B), with each result wrapped in the second algebra. */
public final class SecondResultFlatOperation<A,B> extends AbstractOperation implements ILeftProjectionFlatOperation<A,B> {
    private static final long serialVersionUID=1L;
    private final Algebra<A> first;
    private final Algebra<B> second;
    private final Binary<A,B,List<B>> body;
    public SecondResultFlatOperation(String name,Algebra<A> first,Algebra<B> second,Binary<A,B,List<B>> body) {
        super(name,first,second,second); this.first=first; this.second=second; this.body=body;
    }
    public List<IAlgebraItem<B>> performOperation(A a,B b) {
        List<IAlgebraItem<B>> result=new ArrayList<>();
        for(B value : body.apply(require(first,a),require(second,b))) result.add(second.buildAlgebraItem(require(second,value)));
        return result;
    }
}

