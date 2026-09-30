package operations.flat;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import operations.OperationBodies.*;
import static operations.OperationMembers.require;
import java.util.*;

public final class ClosedFlatOperation<T> extends operations.AbstractOperation implements IFlatOperation<T> {
    private static final long serialVersionUID=1L;
    private final Algebra<T> algebra;
    private final Binary<T,T,List<T>> body;
    public ClosedFlatOperation(String name,Algebra<T> algebra,Binary<T,T,List<T>> body) { super(name,algebra,algebra,algebra); this.algebra=algebra; this.body=body; }
    public List<IAlgebraItem<T>> performOperation(T a,T b) {
        List<IAlgebraItem<T>> result=new ArrayList<>();
        for(T value : body.apply(require(algebra,a),require(algebra,b))) result.add(algebra.buildAlgebraItem(require(algebra,value)));
        return result;
    }
}
