package operations.flat;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import operations.AbstractOperation;
import operations.OperationBodies.Binary;
import java.util.*;
import static operations.OperationMembers.require;

/** A x B -> List(A) using the original custom-member flat interface. */
public final class CustomMemberFlatOperation<A,B> extends AbstractOperation implements ICustomMemberFlatOperation<A> {
    private static final long serialVersionUID=1L;
    private final Algebra<A> first;
    private final Algebra<B> second;
    private final Binary<A,B,List<A>> body;
    public CustomMemberFlatOperation(String name,Algebra<A> first,Algebra<B> second,Binary<A,B,List<A>> body) {
        super(name,first,second,first); this.first=first; this.second=second; this.body=body;
    }
    public <V> List<IAlgebraItem<A>> performOperation(A a,V b) {
        List<IAlgebraItem<A>> result=new ArrayList<>();
        for(A value : body.apply(require(first,a),require(second,b))) result.add(first.buildAlgebraItem(require(first,value)));
        return result;
    }
}

