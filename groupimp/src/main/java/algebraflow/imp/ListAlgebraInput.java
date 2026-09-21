package algebraflow.imp;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import algebraflow.InputFormat;
import exceptions.NotMemberException;
import java.util.*;

/** Finite list input validated by the actual Algebra registered in MathTool. */
public final class ListAlgebraInput<T> implements InputFormat<T> {
    private final Algebra<T> algebra;
    private final List<T> values;
    public ListAlgebraInput(Algebra<T> algebra,List<T> values) {
        this.algebra=Objects.requireNonNull(algebra);
        this.values=Collections.unmodifiableList(new ArrayList<>(values));
        for(T value : this.values) {
            if(!algebra.getParamClass().isInstance(value) || !algebra.validate(value))
                throw new NotMemberException("Invalid input for "+algebra.getAlgebraName());
        }
    }
    @SuppressWarnings("unchecked")
    public Class<T> getInputType() { return algebra.getParamClass(); }
    public Algebra<T> getAlgebra() { return algebra; }
    public List<IAlgebraItem<T>> read(Algebra<T> target) {
        if(target!=algebra) throw new IllegalArgumentException("Input and MathTool must use the same Algebra");
        List<IAlgebraItem<T>> result=new ArrayList<>();
        for(T value : values) {
            IAlgebraItem<T> item=algebra.buildAlgebraItem(value);
            if(item==null) throw new NotMemberException("Input no longer satisfies "+algebra.getAlgebraName());
            result.add(item);
        }
        return result;
    }
}

