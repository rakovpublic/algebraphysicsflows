package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.Pair;
import mathematics.linear.*;
import mathematics.structures.*;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeAbelianHomomorphismTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static IntegerVector v(long... values) {
        BigInteger[] result=new BigInteger[values.length]; for(int i=0;i<values.length;i++) result[i]=z(values[i]); return new IntegerVector(result);
    }
    private static IntegerMatrix m(long[]... rows) {
        BigInteger[][] result=new BigInteger[rows.length][];
        for(int r=0;r<rows.length;r++) { result[r]=new BigInteger[rows[r].length]; for(int c=0;c<rows[r].length;c++) result[r][c]=z(rows[r][c]); }
        return new IntegerMatrix(result);
    }
    private static PresentedAbelianGroup diagonal(long... orders) {
        BigInteger[][] entries=new BigInteger[orders.length][orders.length];
        for(BigInteger[] row : entries) Arrays.fill(row,BigInteger.ZERO); for(int i=0;i<orders.length;i++) entries[i][i]=z(orders[i]);
        return new PresentedAbelianGroup(new IntegerMatrix(entries));
    }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static AbelianGroupHomomorphism map(PresentedAbelianGroup s,PresentedAbelianGroup t,long[]... rows) {
        return AbelianGroupHomomorphism.fromSmith(s,t,m(rows));
    }
    private static void factorization(AbelianGroupHomomorphism f) {
        AbelianGroupHomomorphism inclusion=f.kernelInclusion(),imageInclusion=f.imageInclusion(),projection=f.imageProjection(),quotient=f.cokernelProjection();
        assertEquals(f.kernel(),inclusion.source()); assertEquals(f.source(),inclusion.target());
        assertTrue(f.compose(inclusion).isZero()); assertTrue(inclusion.isInjective());
        assertEquals(f.image(),imageInclusion.source()); assertEquals(imageInclusion.source(),projection.target());
        assertTrue(imageInclusion.isInjective()); assertTrue(projection.isSurjective()); assertEquals(f,imageInclusion.compose(projection));
        assertEquals(f.cokernel(),quotient.target()); assertTrue(quotient.isSurjective()); assertTrue(quotient.compose(f).isZero());
    }
    @Test public void allSmallCyclicMapsMatchIndependentModularArithmetic() {
        for(int n=1;n<=8;n++) for(int d=1;d<=8;d++) for(int a=0;a<d;a++) if((n*a)%d==0) {
            PresentedAbelianGroup s=diagonal(n),t=diagonal(d); AbelianGroupHomomorphism f=map(s,t,new long[]{a});
            Set<AbelianGroupElement> expectedKernel=new HashSet<>(),expectedImage=new HashSet<>();
            for(int x=0;x<n;x++) {
                AbelianGroupElement value=t.fromSmith(v((a*x)%d)); assertEquals(value,f.apply(s.fromSmith(v(x))));
                expectedImage.add(value); if(a*x%d==0) expectedKernel.add(s.fromSmith(v(x)));
                for(int y=0;y<n;y++) assertEquals(f.apply(s.fromSmith(v(x))).add(f.apply(s.fromSmith(v(y)))),f.apply(s.fromSmith(v(x+y))));
            }
            Set<AbelianGroupElement> actualKernel=new HashSet<>(),actualImage=new HashSet<>();
            AbelianGroupHomomorphism inclusion=f.kernelInclusion(),imageInclusion=f.imageInclusion();
            for(AbelianGroupElement e : inclusion.source().elements()) actualKernel.add(inclusion.apply(e));
            for(AbelianGroupElement e : imageInclusion.source().elements()) actualImage.add(imageInclusion.apply(e));
            assertEquals(expectedKernel,actualKernel); assertEquals(expectedImage,actualImage);
            assertEquals(z(expectedKernel.size()),f.kernel().order()); assertEquals(z(expectedImage.size()),f.image().order());
            assertEquals(z(d/expectedImage.size()),f.cokernel().order());
            boolean injective=expectedKernel.size()==1,surjective=expectedImage.size()==d;
            assertEquals(injective,f.isInjective()); assertEquals(surjective,f.isSurjective()); assertEquals(injective&&surjective,f.isIsomorphism());
            for(int y=0;y<d;y++) {
                AbelianGroupElement target=t.fromSmith(v(y)); assertEquals(expectedImage.contains(target),f.hasPreimage(target));
                if(expectedImage.contains(target)) assertEquals(target,f.apply(f.preimage(target)));
                else failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.preimage(target));
            }
            if(injective&&surjective) {
                AbelianGroupHomomorphism inverse=f.inverse(); assertEquals(AbelianGroupHomomorphism.identity(s),inverse.compose(f));
                assertEquals(AbelianGroupHomomorphism.identity(t),f.compose(inverse));
            } else failure(MathFailure.Kind.OPERATION_UNDEFINED,f::inverse);
            factorization(f);
        }
    }
    @Test public void productMapsMatchEnumerationWithoutAssumingDiagonalSmithCoordinates() {
        PresentedAbelianGroup s=diagonal(2,4),t=diagonal(2,6);
        for(int a=0;a<2;a++) for(int b=0;b<2;b++) for(int c=0;c<6;c+=3) for(int d=0;d<6;d+=3) {
            AbelianGroupHomomorphism f=map(s,t,new long[]{a,b},new long[]{c,d});
            Set<AbelianGroupElement> expectedImage=new HashSet<>(),expectedKernel=new HashSet<>();
            for(int x=0;x<2;x++) for(int y=0;y<4;y++) {
                AbelianGroupElement value=t.fromSmith(v((a*x+b*y)%2,(c*x+d*y)%6)); expectedImage.add(value);
                assertEquals(value,f.apply(s.fromSmith(v(x,y)))); if(value.isZero()) expectedKernel.add(s.fromSmith(v(x,y)));
            }
            assertEquals(z(expectedKernel.size()),f.kernel().order()); assertEquals(z(expectedImage.size()),f.image().order());
            for(AbelianGroupElement value : t.elements()) assertEquals(expectedImage.contains(value),f.hasPreimage(value));
            factorization(f);
        }
    }
    @Test public void freeAndMixedMapsRetainLatticeEmbeddingsAndQuotientTorsion() {
        PresentedAbelianGroup free=diagonal(0),finite=diagonal(6);
        AbelianGroupHomomorphism twice=map(free,free,new long[]{2});
        assertEquals(AbelianGroupType.ZERO,twice.kernel().type()); assertEquals(AbelianGroupType.free(z(1)),twice.image().type());
        assertEquals(AbelianGroupType.cyclic(z(2)),twice.cokernel().type()); assertTrue(twice.isInjective()); assertFalse(twice.isSurjective());
        assertFalse(twice.hasPreimage(free.fromSmith(v(3)))); assertEquals(free.fromSmith(v(-3)),twice.preimage(free.fromSmith(v(-6))));
        AbelianGroupHomomorphism mod=map(free,finite,new long[]{2});
        assertEquals(AbelianGroupType.free(z(1)),mod.kernel().type()); assertEquals(AbelianGroupType.cyclic(z(3)),mod.image().type());
        assertEquals(AbelianGroupType.cyclic(z(2)),mod.cokernel().type());
        assertEquals(z(3),mod.kernelInclusion().generatorImages().get(0).smithCoordinates().get(0).abs());
        AbelianGroupHomomorphism sum=map(diagonal(0,0),free,new long[]{2,3});
        assertEquals(AbelianGroupType.free(z(1)),sum.kernel().type()); assertTrue(sum.isSurjective()); assertFalse(sum.isInjective());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,sum::inverse);
        PresentedAbelianGroup mixed=diagonal(6,0);
        AbelianGroupHomomorphism mixedMap=map(mixed,mixed,new long[]{2,3},new long[]{0,2});
        assertEquals(AbelianGroupType.cyclic(z(2)),mixedMap.kernel().type());
        assertEquals(new AbelianGroupType(z(1),Collections.singletonList(z(3))),mixedMap.image().type());
        assertEquals(AbelianGroupType.cyclic(z(4)),mixedMap.cokernel().type());
        for(AbelianGroupHomomorphism f : Arrays.asList(twice,mod,sum,mixedMap)) factorization(f);
    }
    @Test public void originalGeneratorMatricesRoundTripThroughNontrivialSmithChanges() {
        PresentedAbelianGroup s=diagonal(2,3),t=diagonal(6,0);
        IntegerMatrix original=m(new long[]{3,2},new long[]{0,0});
        AbelianGroupHomomorphism f=AbelianGroupHomomorphism.fromMatrix(s,t,original);
        for(int a=-3;a<=3;a++) for(int b=-3;b<=3;b++) {
            assertEquals(t.project(original.multiply(v(a,b))),f.apply(s.project(v(a,b))));
            assertEquals(f.apply(s.project(v(a,b))),t.project(f.matrixLift().multiply(v(a,b))));
        }
        assertEquals(f,AbelianGroupHomomorphism.fromMatrix(s,t,f.matrixLift()));
        assertEquals(f,AbelianGroupHomomorphism.fromMatrix(s,t,m(new long[]{9,-4},new long[]{0,0})));
        assertEquals(Arrays.asList(t.project(v(3,0)),t.project(v(2,0))),f.generatorImages());
        factorization(f);
    }
    @Test public void isomorphismsAcrossDifferentPresentationsHaveActualInverses() {
        PresentedAbelianGroup s=diagonal(2,3),t=diagonal(6);
        AbelianGroupHomomorphism f=map(s,t,new long[]{0,5});
        assertTrue(f.isIsomorphism()); assertEquals(AbelianGroupHomomorphism.identity(s),f.inverse().compose(f));
        assertEquals(AbelianGroupHomomorphism.identity(t),f.compose(f.inverse()));
        PresentedAbelianGroup mixed=diagonal(6,0);
        AbelianGroupHomomorphism g=map(mixed,mixed,new long[]{5,3},new long[]{0,-1});
        assertEquals(AbelianGroupHomomorphism.identity(mixed),g.compose(g.inverse())); factorization(g);
    }
    @Test public void mapsNormalizeRelationsAndEnforceActualBoundaryPresentations() {
        PresentedAbelianGroup s=diagonal(6),t=diagonal(4),other=diagonal(2,3);
        AbelianGroupHomomorphism f=map(s,t,new long[]{2}),same=map(s,t,new long[]{-2});
        assertEquals(f,same); assertEquals(f.hashCode(),same.hashCode()); assertTrue(f.add(same).isZero()); assertTrue(f.scale(z(2)).isZero());
        assertEquals(f,f.compose(AbelianGroupHomomorphism.identity(s)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> map(s,t,new long[]{1}));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> map(s,diagonal(0),new long[]{1}));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> map(s,t,new long[]{0,0}));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.apply(other.zero()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.preimage(s.zero()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.hasPreimage(s.zero()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.compose(AbelianGroupHomomorphism.identity(other)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.add(AbelianGroupHomomorphism.zero(other,t)));
        assertThrows(UnsupportedOperationException.class,() -> f.generatorImages().clear());
    }
    @Test public void zeroDimensionsAndKilledGeneratorsPreserveTrivialMapsAndInverses() {
        List<PresentedAbelianGroup> trivial=Arrays.asList(diagonal(),diagonal(1),diagonal(1,1),new PresentedAbelianGroup(IntegerMatrix.zero(0,3)));
        for(PresentedAbelianGroup s : trivial) for(PresentedAbelianGroup t : trivial) {
            AbelianGroupHomomorphism f=AbelianGroupHomomorphism.zero(s,t); assertTrue(f.isIsomorphism()); assertTrue(f.isZero());
            assertEquals(s.zero(),f.preimage(t.zero())); assertTrue(f.hasPreimage(t.zero()));
            assertEquals(AbelianGroupHomomorphism.identity(s),f.inverse().compose(f)); factorization(f);
        }
        PresentedAbelianGroup empty=diagonal(),free=diagonal(0,0);
        for(AbelianGroupHomomorphism f : Arrays.asList(AbelianGroupHomomorphism.zero(empty,free),AbelianGroupHomomorphism.zero(free,empty))) factorization(f);
        assertEquals(0,AbelianGroupHomomorphism.zero(empty,free).generatorImages().size());
        assertEquals(AbelianGroupType.free(z(2)),AbelianGroupHomomorphism.zero(free,empty).kernel().type());
    }
    @Test public void resourceFailuresRemainDistinctFromMathematicalNonexistence() {
        PresentedAbelianGroup free=new PresentedAbelianGroup(IntegerMatrix.zero(256,0)),torsion=diagonal(2);
        AbelianGroupHomomorphism f=AbelianGroupHomomorphism.zero(free,torsion);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,f::kernel);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,f::cokernel);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> f.hasPreimage(torsion.zero()));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,f::isInjective);
        PresentedAbelianGroup large=new PresentedAbelianGroup(IntegerMatrix.zero(180,0));
        AbelianGroupHomomorphism identity=AbelianGroupHomomorphism.identity(large);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> identity.compose(identity));
        IntegerSmithNormalForm.Computation work=new IntegerSmithNormalForm.Computation(); work.use(4999999);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> work.multiply(IntegerMatrix.identity(2),IntegerMatrix.identity(2)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> work.use(Long.MAX_VALUE));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> work.use(Long.MAX_VALUE));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> work.use(1));
        assertThrows(IllegalArgumentException.class,() -> new IntegerSmithNormalForm.Computation().use(-1));
    }
    @Test public void multipleRightHandSidesUseOneSmithSystemIncludingEmptyShapes() {
        IntegerSmithNormalForm.Computation work=new IntegerSmithNormalForm.Computation();
        IntegerMatrix a=m(new long[]{2,3},new long[]{0,4}),b=m(new long[]{1,7},new long[]{4,-4});
        assertEquals(b,a.multiply(work.solve(a,b)));
        assertEquals(IntegerMatrix.zero(3,2),new IntegerSmithNormalForm.Computation().solve(IntegerMatrix.zero(0,3),IntegerMatrix.zero(0,2)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new IntegerSmithNormalForm.Computation().solve(a,IntegerMatrix.identity(2)));
    }
    @Test public void nativeOperationsReturnTheRegisteredCarrierWrappers() {
        ConcreteMathematics math=new ConcreteMathematics(); PresentedAbelianGroup s=diagonal(6),t=diagonal(4);
        IAlgebraItem<AbelianGroupHomomorphism> item=math.integerMatrices.algebra().buildAlgebraItem(m(new long[]{2}))
                .performUnsafeOperation("AbelianGroupHomomorphism.from-matrix",new Pair<>(s,t));
        assertSame(math.abelianHomomorphisms.algebra(),item.getAlgebra());
        IAlgebraItem<AbelianGroupElement> value=item.performLeftProjectionOperation("apply",s.fromSmith(v(1)));
        assertSame(math.abelianGroupElements.algebra(),value.getAlgebra()); assertEquals(t.fromSmith(v(2)),value.getResult());
        IAlgebraItem<AbelianGroupElement> lift=item.performLeftProjectionOperation("preimage",value.getResult());
        assertSame(math.abelianGroupElements.algebra(),lift.getAlgebra()); assertEquals(s,lift.getResult().group());
        for(IAlgebraItem<AbelianGroupElement> image : item.<AbelianGroupElement>performAlgebraFlatTransfer("generator-images")) assertSame(math.abelianGroupElements.algebra(),image.getAlgebra());
        assertSame(math.abelianHomomorphisms.algebra(),item.performOneOperandOperation("image-inclusion").getAlgebra());
        assertSame(math.presentedAbelianGroups.algebra(),item.performAlgebraTransfer("kernel").getAlgebra());
        assertFalse(((algebra.imp.Algebra)math.abelianHomomorphisms.boundaries).validate(new Pair<>(s,"wrong")));
    }
    @Test public void homomorphismFlowsComposeApplyAndSurviveSerialization() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); PresentedAbelianGroup s=diagonal(6); AbelianGroupHomomorphism f=AbelianGroupHomomorphism.scaling(s,z(2));
        IAlgebraFlow<BigInteger> flow=math.flow(math.abelianHomomorphisms,Collections.singletonList(f))
                .<PresentedAbelianGroup>performAlgebraTransfer("kernel")
                .<AbelianGroupElement>performFlatAlgebraTransfer("AbelianGroupElement.elements")
                .<BigInteger>performAlgebraTransfer("order");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Arrays.asList("1","2"),restored.collect()); assertEquals(restored.collect(),restored.collect());
        assertEquals(Collections.singletonList("[4]"),math.flow(math.abelianHomomorphisms,Collections.singletonList(f))
                .performOperation("compose",f).<AbelianGroupElement>performLeftProjectionOperation("apply",s.fromSmith(v(1)))
                .<IntegerVector>performAlgebraTransfer("smith-coordinates").collect());
    }
}
