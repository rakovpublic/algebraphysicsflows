package example;

import operations.simple.IOperation;

/**
 * Created by Rakovskyi Dmytro on 03.04.2017.
 */
public class IntSumOperation implements IOperation<Integer> {
    @Override
    public String getDescription() {
        return "Sum integer operation";
    }

    @Override
    public String getAlgebraName() {
        return "integer";
    }

    @Override
    public Class getResultBaseClass() {
        return Integer.class;
    }

    @Override
    public Integer performOperation(Integer first, Integer second) {
        return first + second;
    }
}
