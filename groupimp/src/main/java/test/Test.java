package test;

import algebra.ISuperAlgebraInitializer;
import algebra.imp.Algebra;
import algebra.imp.SuperAlgebra;
import distribalgebra.IAlgebraFlow;
import distribalgebra.imp.AlgebraFlow;
import operations.simple.IOperation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 02.04.2017.
 */
public class Test {
    public static void main(String [] args){
        List<Integer> in= new ArrayList<>();
        in.add(5);
        in.add(10);
        ISuperAlgebraInitializer superAlgebraInitializer = new ISuperAlgebraInitializer() {
            @Override
            public SuperAlgebra initialize() {
                SuperAlgebra superAlgebra= new SuperAlgebra();
                Algebra<Integer> integerAlgebra= new Algebra<>("integer",Integer.class);
                Algebra<Boolean> booleanAlgebra= new Algebra<>("boolean",Boolean.class);
                integerAlgebra.addOperation("sum", new IntSumOperation());
                integerAlgebra.addOperation("minus", new IOperation<Integer>() {
                    @Override
                    public Integer performOperation(Integer first, Integer second) {
                        return first-second;
                    }

                    @Override
                    public String getDescription() {
                        return "minus integer operation";
                    }

                    @Override
                    public String getAlgebraName() {
                        return "integer";
                    }

                    @Override
                    public Class getResultBaseClass() {
                        return Integer.class;
                    }
                });
                integerAlgebra.addCustomOperation("higher",new HigherIntOperation(booleanAlgebra));
                booleanAlgebra.addOperation("and",new BooleanAndOperation());
                superAlgebra.addAlgebra(integerAlgebra);
                superAlgebra.addAlgebra(booleanAlgebra);
                return superAlgebra;
            }
        };
        ListInput listInput= new ListInput(in);
        AlgebraFlow<Integer> algebraFlow= new AlgebraFlow<>(listInput,superAlgebraInitializer,"integer");
        List<String> result= algebraFlow.performOperation("sum",5).performCustomOperation("higher",11).performOperation("and",true).collect();
        for(String r:result){
            System.out.println(r);
        }

    }
}
