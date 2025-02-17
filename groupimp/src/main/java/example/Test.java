package example;

import algebra.IMathToolInitializer;
import algebra.imp.Algebra;
import algebra.imp.MathTool;
import algebraflow.imp.AlgebraFlow;
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
        IMathToolInitializer superAlgebraInitializer = new IMathToolInitializer() {
            @Override
            public MathTool initialize() {
                MathTool mathTool = new MathTool("example");
                Algebra<Integer> integerAlgebra= new Algebra<>("integer",Integer.class,"");
                Algebra<Boolean> booleanAlgebra= new Algebra<>("boolean",Boolean.class,"");
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

                    @Override
                    public Class getSecondElementClass() {
                        return Integer.class;
                    }
                });
                integerAlgebra.addCustomResultOperation("higher",new HigherIntResultOperation(booleanAlgebra));
            booleanAlgebra.addOperation("and",new BooleanAndOperation());
            mathTool.addAlgebra(integerAlgebra);
            mathTool.addAlgebra(booleanAlgebra);
            return mathTool;
        }
    };
    ListInput listInput= new ListInput(in);
    AlgebraFlow<Integer> algebraFlow= new AlgebraFlow<>(listInput,superAlgebraInitializer,"integer");
    List<String> result= algebraFlow.performOperation("sum",5).performCustomResultOperation("higher",11).performOperation("and",true).collect();
        for(String r:result){
        System.out.println(r);
        }

    }
}
