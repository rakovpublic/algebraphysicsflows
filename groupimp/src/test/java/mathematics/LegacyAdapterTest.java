package mathematics;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import algebra.imp.MathTool;
import algebraflow.InputFormat;
import algebraflow.imp.AlgebraFlow;
import mathematics.adapters.LegacyAdapters;
import mathematics.catalog.StandardMathematics;
import mathematics.core.*;
import mathematics.foundations.*;
import mathematics.numbers.*;
import org.junit.Test;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import static org.junit.Assert.*;

public class LegacyAdapterTest {
    private AlgebraFlow<BigInteger> flow(MathTool tool) {
        InputFormat<BigInteger> input=new InputFormat<BigInteger>() {
            public Class<BigInteger> getInputType() { return BigInteger.class; }
            public Algebra<BigInteger> getAlgebra() { return NumberDomains.NATURALS.algebra(); }
            public List<IAlgebraItem<BigInteger>> read(Algebra<BigInteger> algebra) {
                return Arrays.asList(algebra.buildAlgebraItem(BigInteger.ONE),algebra.buildAlgebraItem(BigInteger.valueOf(2)));
            }
        };
        return new AlgebraFlow<>(input,() -> tool,"N");
    }
    @Test public void multipleCrossDomainStepsUseSharedExecutionState() {
        StandardMathematics math=new StandardMathematics();
        MathTool tool=LegacyAdapters.installCatalog(math.catalog);
        algebraflow.IAlgebraFlow<Rational> result=flow(tool)
                .<BigInteger>performAlgebraTransfer("N-to-Z")
                .<Rational>performAlgebraTransfer("Z-to-Q")
                .<Rational,Rational>performAlgebraUnsafe("rational-add",Rational.of(3));
        assertEquals(Arrays.asList("4","5"),result.collect());
        assertEquals(Arrays.asList("4","5"),result.collect());
        assertEquals("Q",result.getCurrentAlgebraName());
    }
    @Test public void flatAdaptersPreserveDuplicatesThroughCrossDomainChains() {
        StandardMathematics math=new StandardMathematics();
        FlatOperation<Rational,Unit,Rational> duplicate=new FlatOperation<>(Metadata.of("duplicateQ","Two ordered copies"),
                NumberDomains.RATIONALS,FoundationDomains.UNIT,NumberDomains.RATIONALS,
                MathStatus.Computation.EXACT,false,(value,unit) -> Arrays.asList(value,value));
        math.catalog.addOperation(duplicate);
        MathTool tool=LegacyAdapters.installCatalog(math.catalog);
        assertEquals(Arrays.asList("1","1","2","2"),flow(tool)
                .<BigInteger>performAlgebraTransfer("N-to-Z")
                .<Rational>performAlgebraTransfer("Z-to-Q")
                .<Rational,Unit>performFlatAlgebraUnsafe("duplicateQ",Unit.INSTANCE).collect());
    }
    @Test public void adapterFailuresKeepMathematicalMeaning() {
        StandardMathematics math=new StandardMathematics();
        LegacyAdapters.install(math.divide);
        MathFailure failure=assertThrows(MathFailure.class,() -> NumberDomains.RATIONALS.item(Rational.ONE).performUnsafeOperation("rational-divide",Rational.ZERO));
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,failure.kind());
    }
    @Test public void checkedOperationSerializationPreservesItsDomainGraph() throws Exception {
        StandardMathematics math=new StandardMathematics();
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream output=new ObjectOutputStream(bytes)) { output.writeObject(math.integerToRational); }
        UnaryOperation<BigInteger,Rational> restored;
        try(ObjectInputStream input=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            @SuppressWarnings("unchecked") UnaryOperation<BigInteger,Rational> operation=(UnaryOperation<BigInteger,Rational>)input.readObject();
            restored=operation;
        }
        assertEquals(Rational.of(12),restored.apply(BigInteger.valueOf(12)));
        assertEquals("Z",restored.source().metadata().id);
    }
}
