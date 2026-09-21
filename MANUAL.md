Core terms/concepts
Algebra - set rules which defines which elements could be the elements of this algebra (see IValidationRule interface) and set of methods which defines possible operations in this algebra.

Operations- incupsulate logic of operations (see package operations)

The reusable left projection operation follows `A op B = A`, where `A` and `B`
may have different types. `ILeftProjectionOperation<T, V>` returns the original
first value of type `T`; `ILeftProjectionFlatOperation<T, V>` returns a one-element
list containing that value as an `IAlgebraItem<T>`. These interfaces have their
own implementations and registration methods. Supply the second operand's class
for operation lookup and register either version under a name of your choice:

```java
import algebra.imp.Algebra;
import operations.simple.LeftProjectionOperation;
import operations.flat.LeftProjectionFlatOperation;

Algebra<Integer> integers = new Algebra<>("integer", Integer.class, "Integers");
integers.addLeftProjectionOperation("left", new LeftProjectionOperation<>(integers, String.class));
integers.addLeftProjectionFlatOperation("left", new LeftProjectionFlatOperation<>(integers, String.class));

// 5 op "ignored" = 5
Integer value = integers.buildAlgebraItem(5)
        .performLeftProjectionOperation("left", "ignored").getResult();
// The flat result contains one item with value 5.
Integer flatValue = integers.buildAlgebraItem(5)
        .performLeftProjectionFlatOperation("left", "ignored").get(0).getResult();
```

After registering the algebra with `MathTool`, use
`flow.performLeftProjectionOperation("left", "ignored")` or
`flow.performLeftProjectionFlatOperation("left", "ignored")` to retain each input
value. The first operand remains in its original algebra; the second operand does
not need to belong to that algebra.

MathTool - incupsulate all algebras which will be used in modelling(see mathtool class)

AlgebraFlow -  provide operation invoke api (see IAlgebraFlow interafce and AlgebraFlow class)

You can find the small example how to use in package examples

This code describes 2 algebras: 

-int(natural numbers) with 2 operations sum betwen 2 int values and comparison operation > which return the element of boolean algebra 

-boolean with 1 logical operation and 

The algebraflow class constructor takes as params object which implement InputFormat interface, objetc which implement IMathToolInitializer interface and name of the initial algebra for the items from input. 
    
     List<Integer> in= new ArrayList<>();
        in.add(5);
        in.add(10);
        IMathToolInitializer superAlgebraInitializer = new IMathToolInitializer() {
            @Override
            public MathTool initialize() {
                MathTool mathTool = new MathTool("example");
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
