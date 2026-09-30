package operations.flat;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import operations.AbstractOperation;
import operations.OperationBodies.Unary;
import java.util.*;
import static operations.OperationMembers.require;

public final class OneOperandFlatOperation<T> extends AbstractOperation implements IOneOperandFlatOperation<T> {
    private static final long serialVersionUID=1L;
    private final Algebra<T> algebra;
    private final Unary<T,List<T>> body;
    public OneOperandFlatOperation(String name,Algebra<T> algebra,Unary<T,List<T>> body) {
        super(name,algebra,null,algebra); this.algebra=algebra; this.body=body;
    }
    public List<IAlgebraItem<T>> performOperation(T input) {
        List<IAlgebraItem<T>> result=new ArrayList<>();
        for(T value : body.apply(require(algebra,input))) result.add(algebra.buildAlgebraItem(require(algebra,value)));
        return result;
    }
}

