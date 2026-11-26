Core terms/concepts
Algebra - set rules which defines which elements could be the elements of this algebra (see IValidationRule interface) and set of methods which defines possible operations in this algebra.

Operations- incupsulate logic of operations (see package operations)

Left operations follow `A op B = B`, where `A` and `B` may have different types.
`ILeftProjectionOperation<T, V>` returns an `IAlgebraItem<V>` in `V`'s algebra.
`ILeftProjectionFlatOperation<T, V>` returns a list of those items. Register them
on the first operand's algebra, and pass the second operand's algebra to the
constructor. `LeftProjectionOperation` returns the second value unchanged.
`SecondResultOperation` may transform it. `A op B = A` is `ICustomMemberOperation`,
not a left operation. Details and the flow contract are in
[Left operations](docs/LEFT_OPERATIONS.md).

```java
import algebra.imp.Algebra;
import operations.simple.LeftProjectionOperation;
import operations.flat.LeftProjectionFlatOperation;

Algebra<Integer> integers = new Algebra<>("integer", Integer.class, "Integers");
Algebra<String> text = new Algebra<>("text", String.class, "Text");
integers.addLeftProjectionOperation("left", new LeftProjectionOperation<>(text, String.class));
integers.addLeftProjectionFlatOperation("left", new LeftProjectionFlatOperation<>(text, String.class));

// 5 op "kept" = "kept", in the text algebra
String value = integers.buildAlgebraItem(5)
        .performLeftProjectionOperation("left", "kept").getResult();
String flatValue = integers.buildAlgebraItem(5)
        .performLeftProjectionFlatOperation("left", "kept").get(0).getResult();
```

After both algebras are in `MathTool`,
`flow.performLeftProjectionOperation("left", "kept")` and
`flow.performLeftProjectionFlatOperation("left", "kept")` switch the flow to the
text algebra. If that algebra is missing, scheduling fails and the existing plan
stays unchanged.

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
