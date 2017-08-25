package example;

import operations.simple.IOperation;

/**
 * Created by Rakovskyi Dmytro on 03.04.2017.
 */
public class BooleanAndOperation implements IOperation<Boolean> {

    @Override
    public String getDescription() {
        return "Boolean and operation";
    }

    @Override
    public String getAlgebraName() {
        return "boolean";
    }

    @Override
    public  Class getResultBaseClass() {
        return Boolean.class;
    }

    @Override
    public Boolean performOperation(Boolean first, Boolean second) {
        return first&&second;
    }
}
