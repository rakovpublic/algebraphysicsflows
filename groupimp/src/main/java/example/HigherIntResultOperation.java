package example;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import operations.simple.ICustomResultOperation;

/**
 * Created by Rakovskyi Dmytro on 03.04.2017.
 */
public class HigherIntResultOperation implements ICustomResultOperation<Integer> {
    private Algebra<Boolean> algebra;
    public HigherIntResultOperation(Algebra<Boolean> algebra){
        this.algebra=algebra;
    }
    @Override
    public String getDescription() {
        return "Integer higher operation";
    }

    @Override
    public String getAlgebraName() {
        return algebra.getAlgebraName();
    }

    @Override
    public  Class getResultBaseClass() {
        return Boolean.class;
    }

    @Override
    public  IAlgebraItem<Boolean> performOperation(Integer first, Integer second) {
        return algebra.buildAlgebraItem(first>second);
    }
}
