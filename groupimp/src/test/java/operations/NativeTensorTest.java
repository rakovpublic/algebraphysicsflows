package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.Pair;
import mathematics.linear.*;
import mathematics.numbers.Rational;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeTensorTest {
    private static RationalTensor t(int[] shape,long... entries) {
        Rational[] values=new Rational[entries.length]; for(int i=0;i<values.length;i++) values[i]=Rational.of(entries[i]);
        return new RationalTensor(shape,values);
    }
    private static RationalVector v(long... entries) { return t(new int[]{entries.length},entries).toVector(); }
    private static Pair<BigInteger,BigInteger> axes(long first,long second) { return new Pair<>(BigInteger.valueOf(first),BigInteger.valueOf(second)); }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static void undefined(Runnable action) { failure(MathFailure.Kind.OPERATION_UNDEFINED,action); }

    @Test public void rowMajorCoordinatesAndImmutableShapesDistinguishScalarsFromSingletonAxes() {
        int[] shape={2,3}; Rational[] values={Rational.ONE,Rational.of(2),Rational.of(3),Rational.of(4),Rational.of(5),Rational.of(6)};
        RationalTensor tensor=new RationalTensor(shape,values); shape[0]=7; values[0]=Rational.ZERO;
        assertEquals(Arrays.asList(2,3),tensor.shape()); assertEquals(2,tensor.order()); assertEquals(6,tensor.size());
        assertEquals(Rational.ONE,tensor.get(0,0)); assertEquals(Rational.of(6),tensor.get(1,2));
        assertThrows(UnsupportedOperationException.class,() -> tensor.shape().clear());
        assertThrows(UnsupportedOperationException.class,() -> tensor.entries().set(0,Rational.ZERO));
        RationalTensor scalar=RationalTensor.scalar(Rational.of(3)); assertEquals(0,scalar.order()); assertEquals(1,scalar.size());
        assertEquals(Rational.of(3),scalar.get()); assertEquals(Rational.of(3),scalar.scalar());
        assertNotEquals(scalar,t(new int[]{1},3)); assertNotEquals(t(new int[]{2,1},1,2),t(new int[]{1,2},1,2));
        undefined(() -> tensor.get(0)); undefined(() -> tensor.get(0,3)); undefined(() -> tensor.get(-1,0));
        failure(MathFailure.Kind.INVALID_MEMBER,() -> t(new int[]{2,3},1,2));
        failure(MathFailure.Kind.INVALID_MEMBER,() -> t(new int[]{0,-1}));
    }

    @Test public void tensorProductsUseOrderedAxesAndObeyBilinearityAssociativityAndScalarUnits() {
        RationalTensor a=t(new int[]{2},1,2),b=t(new int[]{3},3,4,5),c=t(new int[]{2},-1,7);
        assertEquals(t(new int[]{2,3},3,4,5,6,8,10),a.tensorProduct(b));
        assertEquals(a.tensorProduct(b).tensorProduct(c),a.tensorProduct(b.tensorProduct(c)));
        assertEquals(a,a.tensorProduct(RationalTensor.scalar(Rational.ONE)));
        assertEquals(a,RationalTensor.scalar(Rational.ONE).tensorProduct(a));
        assertEquals(a.scale(Rational.of(3)),a.tensorProduct(RationalTensor.scalar(Rational.of(3))));
        assertEquals(a.tensorProduct(b).add(c.tensorProduct(b)),a.add(c).tensorProduct(b));
        assertEquals(a.tensorProduct(b).swapAxes(0,1),b.tensorProduct(a));
        assertEquals(t(new int[]{2},-1,14),a.hadamard(c)); assertEquals(Rational.of(13),a.dot(c));
        undefined(() -> a.add(b)); undefined(() -> a.hadamard(b)); undefined(() -> a.dot(b));
    }

    private static long[] ternaryMatrix(int code) {
        long[] entries=new long[4]; for(int i=0;i<4;i++) { entries[i]=code%3-1; code/=3; } return entries;
    }
    @Test public void contractionOfAllTernaryTwoByTwoMatrixProductsAgreesWithIndependentCoordinateSums() {
        for(int first=0;first<81;first++) for(int second=0;second<81;second++) {
            long[] a=ternaryMatrix(first),b=ternaryMatrix(second);
            RationalTensor product=t(new int[]{2,2},a).tensorProduct(t(new int[]{2,2},b));
            RationalTensor contracted=product.contract(1,2);
            assertEquals(Arrays.asList(2,2),contracted.shape()); assertEquals(contracted,product.contract(2,1));
            for(int i=0;i<2;i++) for(int j=0;j<2;j++) {
                long expected=0; for(int k=0;k<2;k++) expected+=a[2*i+k]*b[2*k+j];
                assertEquals(Rational.of(expected),contracted.get(i,j));
            }
            assertEquals(Rational.of(a[0]+a[3]),t(new int[]{2,2},a).contract(0,1).scalar());
        }
    }

    @Test public void nonadjacentAxisContractionAndSwapsPreserveRemainingAxisOrder() {
        long[] entries=new long[36]; for(int i=0;i<entries.length;i++) entries[i]=i+1;
        RationalTensor tensor=t(new int[]{2,3,2,3},entries),first=tensor.contract(0,2),second=tensor.contract(3,1);
        assertEquals(Arrays.asList(3,3),first.shape()); assertEquals(Arrays.asList(2,2),second.shape());
        for(int j=0;j<3;j++) for(int l=0;l<3;l++) {
            long expected=0; for(int i=0;i<2;i++) expected+=entries[((i*3+j)*2+i)*3+l];
            assertEquals(Rational.of(expected),first.get(j,l));
        }
        for(int i=0;i<2;i++) for(int k=0;k<2;k++) {
            long expected=0; for(int j=0;j<3;j++) expected+=entries[((i*3+j)*2+k)*3+j];
            assertEquals(Rational.of(expected),second.get(i,k));
        }
        RationalTensor swapped=tensor.swapAxes(0,3); assertEquals(Arrays.asList(3,3,2,2),swapped.shape());
        for(int i=0;i<2;i++) for(int j=0;j<3;j++) for(int k=0;k<2;k++) for(int l=0;l<3;l++)
            assertEquals(tensor.get(i,j,k,l),swapped.get(l,j,k,i));
        assertEquals(tensor,swapped.swapAxes(0,3)); assertEquals(tensor,tensor.swapAxes(2,2));
        undefined(() -> tensor.contract(0,1)); undefined(() -> tensor.contract(0,0)); undefined(() -> tensor.contract(-1,2));
        undefined(() -> tensor.swapAxes(0,4)); undefined(() -> RationalTensor.scalar(Rational.ONE).contract(0,1));
    }

    @Test public void zeroAxesRetainShapeAndEmptyContractionsProduceActualZeroTensors() {
        RationalTensor empty=t(new int[]{0,2,0}); assertEquals(0,empty.size());
        assertEquals(t(new int[]{2},0,0),empty.contract(0,2));
        assertEquals(RationalTensor.scalar(Rational.ZERO),t(new int[]{0,0}).contract(0,1));
        assertEquals(t(new int[]{0,2,0,3}),empty.tensorProduct(t(new int[]{3},1,2,3)));
        assertEquals(t(new int[]{0,0,2}),empty.swapAxes(1,2)); assertEquals(Rational.ZERO,empty.dot(empty));
        assertEquals(v(),RationalTensor.fromVector(v()).toVector());
        assertNotEquals(t(new int[]{0}),t(new int[]{0,1})); undefined(() -> t(new int[]{0,2}).toMatrix());
        undefined(() -> empty.get(0,0,0)); undefined(empty::scalar); undefined(empty::toVector);
        RationalTensor hugeEmpty=t(new int[]{Integer.MAX_VALUE,Integer.MAX_VALUE,0}); assertEquals(0,hugeEmpty.size());
        assertEquals(Arrays.asList(0,Integer.MAX_VALUE,Integer.MAX_VALUE),hugeEmpty.swapAxes(0,2).shape());
    }

    @Test public void resourceCapsAreImplementationFailuresDistinctFromMathematicalUndefinedness() {
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> t(new int[]{1001,1000}));
        int[] manyAxes=new int[RationalTensor.MAX_ORDER+1]; Arrays.fill(manyAxes,1);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> t(manyAxes,1));
        int[] thirtyTwoAxes=new int[RationalTensor.MAX_ORDER]; Arrays.fill(thirtyTwoAxes,1);
        RationalTensor maximalOrder=t(thirtyTwoAxes,1);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> maximalOrder.tensorProduct(t(new int[]{1},1)));
        // Empty input is representable, but removing its two zero axes would materialize too many zeros.
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> t(new int[]{0,0,1001,1000}).contract(0,1));
        Rational[] thousand=new Rational[1001]; Arrays.fill(thousand,Rational.ONE); RationalTensor vector=new RationalTensor(new int[]{1001},thousand);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> vector.tensorProduct(vector));
    }

    @Test public void nativeConversionsKeepActualAlgebrasAndValidateAxisPairs() {
        ConcreteMathematics math=new ConcreteMathematics(),other=new ConcreteMathematics();
        IAlgebraItem<RationalTensor> vector=math.finiteVectors.algebra().buildAlgebraItem(v(1,2,3)).performAlgebraTransfer("Tensor(Q).from-vector");
        assertSame(math.tensors.algebra(),vector.getAlgebra());
        assertSame(math.finiteVectors.algebra(),vector.performAlgebraTransfer("to-vector").getAlgebra());
        IAlgebraItem<RationalTensor> scalar=math.rationals.algebra().buildAlgebraItem(Rational.of(7)).performAlgebraTransfer("Tensor(Q).from-scalar");
        assertSame(math.rationals.algebra(),scalar.performAlgebraTransfer("to-scalar").getAlgebra());
        RationalTensor values=t(new int[]{2,2},1,2,3,4);
        IAlgebraItem<RationalTensor> tensor=math.tensors.algebra().buildAlgebraItem(values);
        IAlgebraItem<RationalTensor> trace=tensor.performCustomMemberOperation("contract",axes(0,1));
        assertSame(math.tensors.algebra(),trace.getAlgebra()); assertEquals(Rational.of(5),trace.performAlgebraTransfer("to-scalar").getResult());
        IAlgebraItem<RationalMatrix> matrix=tensor.performAlgebraTransfer("to-matrix"); assertSame(math.rectangularMatrices.algebra(),matrix.getAlgebra());
        assertEquals(values,matrix.performAlgebraTransfer("Tensor(Q).from-matrix").getResult());
        assertTrue(math.tensors.axisPairs.validate(axes(0,1))); assertFalse(math.tensors.axisPairs.validate(axes(-1,1)));
        undefined(() -> tensor.performCustomMemberOperation("contract",new Pair<>(BigInteger.ZERO,BigInteger.TEN.pow(80))));
        undefined(() -> tensor.performOperation("add",RationalTensor.scalar(Rational.ONE)).perform());
        undefined(() -> scalar.performAlgebraTransfer("to-vector"));
        assertThrows(IllegalArgumentException.class,() -> math.flow(other.tensors,Collections.singletonList(values)));
    }

    @Test public void serializedTensorFlowsRecoverMatrixMultiplicationAndFlatScalarResults() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics();
        RationalMatrix a=t(new int[]{2,2},1,2,3,4).toMatrix(); RationalTensor b=t(new int[]{2,3},1,2,3,4,5,6);
        IAlgebraFlow<Rational> flow=math.flow(math.rectangularMatrices,Collections.singletonList(a))
                .<RationalTensor>performAlgebraTransfer("Tensor(Q).from-matrix").performOperation("tensor-product",b)
                .performCustomMemberOperation("contract",axes(1,2)).<RationalMatrix>performAlgebraTransfer("to-matrix")
                .performLeftProjectionOperation("apply",v(1,1,1)).<Rational>performFlatAlgebraTransfer("entries").performOneOperandOperation("negate");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored;
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Arrays.asList("-36","-78"),restored.collect()); assertEquals(Arrays.asList("-36","-78"),restored.collect());
        assertEquals(Collections.singletonList("32"),math.flow(math.tensors,Collections.singletonList(t(new int[]{3},1,2,3)))
                .performOperation("tensor-product",t(new int[]{3},4,5,6)).performCustomMemberOperation("contract",axes(0,1))
                .<Rational>performAlgebraTransfer("to-scalar").collect());
        assertEquals(Arrays.asList("2","3"),math.flow(math.tensors,Collections.singletonList(b)).<BigInteger>performFlatAlgebraTransfer("shape").collect());
        assertEquals(Collections.emptyList(),math.flow(math.tensors,Collections.singletonList(t(new int[]{0,2})))
                .<Rational>performFlatAlgebraTransfer("entries").collect());
    }
}
