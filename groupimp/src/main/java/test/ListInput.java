package test;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import algebraflow.InputFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 03.04.2017.
 */
public class ListInput implements InputFormat<Integer>{
    private List<Integer> input;

    public ListInput(List<Integer> input) {
        this.input = input;
    }

    @Override
    public Class getInputType() {
        return Integer.class;
    }

    @Override
    public List<IAlgebraItem<Integer>> read(Algebra<Integer> algebra) {
        List<IAlgebraItem<Integer>> res= new ArrayList<>();
        for(Integer i:input){
            res.add(algebra.buildAlgebraItem(i));
        }
        return res;
    }
}
