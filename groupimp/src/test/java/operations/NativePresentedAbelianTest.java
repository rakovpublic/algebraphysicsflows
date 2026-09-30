package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerVector;
import mathematics.structures.AbelianGroupElement;
import mathematics.structures.AbelianGroupType;
import mathematics.structures.PresentedAbelianGroup;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativePresentedAbelianTest {
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
        BigInteger[][] result=new BigInteger[orders.length][orders.length];
        for(BigInteger[] row : result) Arrays.fill(row,BigInteger.ZERO); for(int i=0;i<orders.length;i++) result[i][i]=z(orders[i]);
        return new PresentedAbelianGroup(new IntegerMatrix(result));
    }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static List<BigInteger> divisors(long[][] a) {
        BigInteger gcd1=BigInteger.ZERO,gcd2=BigInteger.ZERO;
        for(long[] row : a) for(long value : row) gcd1=gcd1.gcd(z(value));
        for(int i=0;i<a[0].length;i++) for(int j=i+1;j<a[0].length;j++) gcd2=gcd2.gcd(z(a[0][i]*a[1][j]-a[0][j]*a[1][i]));
        List<BigInteger> result=new ArrayList<>(); if(gcd1.signum()>0) result.add(gcd1); if(gcd2.signum()>0) result.add(gcd2.divide(gcd1)); return result;
    }
    @Test public void quotientEqualityMatchesIndependentLatticeMinorsFor15625Vectors() {
        for(int code=0;code<625;code++) {
            int digits=code; long[][] entries=new long[2][2];
            for(int r=0;r<2;r++) for(int c=0;c<2;c++) { entries[r][c]=digits%5-2; digits/=5; }
            IntegerMatrix matrix=m(entries); PresentedAbelianGroup group=new PresentedAbelianGroup(matrix); List<BigInteger> original=divisors(entries);
            for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) {
                long[][] augmented={Arrays.copyOf(entries[0],3),Arrays.copyOf(entries[1],3)}; augmented[0][2]=a; augmented[1][2]=b;
                assertEquals(original.equals(divisors(augmented)),group.project(v(a,b)).isZero());
            }
            IntegerVector representative=v(3,-4),changed=representative.add(matrix.multiply(v(2,-3)));
            assertEquals(group.project(representative),group.project(changed));
            AbelianGroupElement element=group.project(representative); assertEquals(element,group.project(element.representative()));
            assertEquals(group.type(),matrix.cokernel());
            for(IntegerVector relation : matrix.columnVectors()) assertTrue(group.project(relation).isZero());
        }
    }
    @Test public void finiteEnumerationMatchesIndependentCyclicProductElementOrders() {
        for(int a=1;a<=8;a++) for(int b=1;b<=8;b++) {
            PresentedAbelianGroup group=diagonal(a,b); List<AbelianGroupElement> elements=group.elements();
            assertEquals(a*b,elements.size()); assertEquals(a*b,new HashSet<>(elements).size()); assertEquals(group.zero(),elements.get(0));
            Map<BigInteger,Integer> expected=new TreeMap<>(),actual=new TreeMap<>();
            for(int x=0;x<a;x++) for(int y=0;y<b;y++) {
                int order=1; while((order*x)%a!=0 || (order*y)%b!=0) order++;
                expected.merge(z(order),1,Integer::sum);
            }
            for(AbelianGroupElement element : elements) {
                actual.merge(element.order(),1,Integer::sum); assertTrue(element.scale(element.order()).isZero());
                assertEquals(element,group.project(element.representative()));
            }
            assertEquals(expected,actual);
        }
    }
    @Test public void projectionsAndAdditiveLawsHoldOnFiniteMixedAndFreePresentations() {
        List<PresentedAbelianGroup> groups=Arrays.asList(diagonal(2,3),diagonal(4,6),diagonal(3,0),diagonal(0,0),diagonal(1));
        for(PresentedAbelianGroup group : groups) {
            List<AbelianGroupElement> samples=new ArrayList<>(); samples.add(group.zero()); samples.addAll(group.generators());
            for(AbelianGroupElement a : samples) for(AbelianGroupElement b : samples) {
                assertEquals(a.add(b),b.add(a)); assertEquals(a,a.add(group.zero())); assertTrue(a.add(a.scale(z(-1))).isZero());
                assertEquals(a.scale(z(7)).add(b.scale(z(7))),a.add(b).scale(z(7)));
                assertEquals(a.add(b),group.project(a.representative().add(b.representative())));
                for(AbelianGroupElement c : samples) assertEquals(a.add(b).add(c),a.add(b.add(c)));
            }
        }
    }
    @Test public void originalAndSmithGeneratorsRetainTheirDistinctCoordinateConventions() {
        PresentedAbelianGroup group=diagonal(2,3);
        assertEquals(Arrays.asList(group.fromSmith(v(0,3)),group.fromSmith(v(0,2))),group.generators());
        assertEquals(Collections.singletonList(group.fromSmith(v(0,1))),group.smithGenerators());
        assertEquals(v(0,5),group.reduce(v(1,1))); assertEquals(v(0,1),group.fromSmith(v(-7,-5)).smithCoordinates());
        assertEquals(group.fromSmith(v(0,1)),group.project(v(1,2)));
        assertEquals(v(1,-1),group.fromSmith(v(0,1)).representative());
        PresentedAbelianGroup killed=diagonal(1,1); assertEquals(2,killed.generators().size()); assertTrue(killed.smithGenerators().isEmpty());
        for(AbelianGroupElement generator : killed.generators()) assertTrue(generator.isZero());
    }
    @Test public void presentationEqualityDoesNotSilentlyIdentifyIsomorphicGroups() {
        PresentedAbelianGroup first=diagonal(2,3),same=diagonal(2,3),other=diagonal(3,2);
        assertEquals(first,same); assertEquals(first.hashCode(),same.hashCode()); assertNotEquals(first,other); assertEquals(first.type(),other.type());
        assertEquals(first.project(v(1,2)),same.project(v(1,2)));
        assertNotEquals(first.zero(),other.zero());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> first.zero().add(other.zero()));
        assertEquals(first.zero(),first.zero().add(same.zero()));
        assertNotEquals(diagonal(6),first); assertEquals(diagonal(6).type(),first.type());
    }
    @Test public void freeCoordinatesAndTorsionOrdersUseNoInfinitySentinel() {
        PresentedAbelianGroup group=diagonal(6,0); AbelianGroupElement torsion=group.fromSmith(v(2,0)),infinite=group.fromSmith(v(2,-5));
        assertFalse(group.isFinite()); failure(MathFailure.Kind.OPERATION_UNDEFINED,group::order);
        assertTrue(torsion.isTorsion()); assertEquals(z(3),torsion.order()); assertFalse(infinite.isTorsion());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,infinite::order); failure(MathFailure.Kind.OPERATION_UNDEFINED,infinite::cyclicSubgroup);
        assertEquals(BigInteger.ONE,group.zero().order()); assertEquals(Collections.singletonList(group.zero()),group.zero().cyclicSubgroup());
        assertEquals(Arrays.asList(group.zero(),torsion,torsion.scale(z(2))),torsion.cyclicSubgroup());
        BigInteger huge=BigInteger.TEN.pow(100); AbelianGroupElement large=group.fromSmith(new IntegerVector(huge,huge.negate()));
        assertEquals(huge.negate(),large.smithCoordinates().get(1)); assertEquals(huge.mod(z(6)),large.smithCoordinates().get(0));
    }
    @Test public void everySmallFiniteMultiplicationFiberMatchesIndependentEnumeration() {
        for(int a=1;a<=6;a++) for(int b=1;b<=6;b++) {
            PresentedAbelianGroup group=diagonal(a,b); List<AbelianGroupElement> elements=group.elements();
            for(int n=-6;n<=6;n++) for(AbelianGroupElement target : elements) {
                Set<AbelianGroupElement> expected=new HashSet<>();
                for(AbelianGroupElement candidate : elements) if(candidate.scale(z(n)).equals(target)) expected.add(candidate);
                List<AbelianGroupElement> actual=target.multiplicationPreimages(z(n));
                assertEquals(expected,new HashSet<>(actual)); assertEquals(expected.size(),actual.size());
            }
        }
    }
    @Test public void multiplicationFibersDistinguishEmptyFiniteInfiniteAndOversizedResults() {
        PresentedAbelianGroup mixed=diagonal(6,0);
        assertEquals(Arrays.asList(mixed.fromSmith(v(1,2)),mixed.fromSmith(v(4,2))),mixed.fromSmith(v(2,4)).multiplicationPreimages(z(2)));
        assertEquals(Arrays.asList(mixed.fromSmith(v(2,-2)),mixed.fromSmith(v(5,-2))),mixed.fromSmith(v(2,4)).multiplicationPreimages(z(-2)));
        assertTrue(mixed.fromSmith(v(1,4)).multiplicationPreimages(z(2)).isEmpty());
        assertTrue(mixed.fromSmith(v(2,3)).multiplicationPreimages(z(2)).isEmpty());
        assertTrue(mixed.fromSmith(v(1,0)).multiplicationPreimages(BigInteger.ZERO).isEmpty());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> mixed.zero().multiplicationPreimages(BigInteger.ZERO));
        PresentedAbelianGroup large=diagonal(8192,0);
        assertTrue(large.fromSmith(v(0,1)).multiplicationPreimages(z(8192)).isEmpty());
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> large.zero().multiplicationPreimages(z(8192)));
        assertEquals(1,diagonal(1000000).fromSmith(v(7)).multiplicationPreimages(BigInteger.ONE).size());
    }
    @Test public void typeConstructionDirectSumsAndEmptyPresentationsRetainFreeRanks() {
        AbelianGroupType type=new AbelianGroupType(z(2),Arrays.asList(z(2),z(6)));
        PresentedAbelianGroup group=PresentedAbelianGroup.fromType(type);
        assertEquals(type,group.type()); assertEquals(4,group.generatorCount()); assertEquals(2,group.relationCount());
        assertEquals(m(new long[]{2,0},new long[]{0,6},new long[]{0,0},new long[]{0,0}),group.relations());
        PresentedAbelianGroup sum=group.directSum(diagonal(3)); assertEquals(type.directSum(AbelianGroupType.cyclic(z(3))),sum.type());
        PresentedAbelianGroup empty=new PresentedAbelianGroup(IntegerMatrix.zero(0,0));
        assertEquals(empty,PresentedAbelianGroup.fromType(AbelianGroupType.ZERO)); assertEquals(group,empty.directSum(group));
        assertEquals(Collections.singletonList(empty.zero()),empty.elements()); assertEquals(v(),empty.zero().representative());
        assertTrue(empty.generators().isEmpty()); assertTrue(empty.smithGenerators().isEmpty());
        assertEquals(Collections.singletonList(empty.zero()),empty.zero().multiplicationPreimages(z(0)));
        assertEquals(AbelianGroupType.free(z(3)),new PresentedAbelianGroup(IntegerMatrix.zero(3,0)).type());
        assertEquals(AbelianGroupType.ZERO,new PresentedAbelianGroup(IntegerMatrix.zero(0,7)).type());
    }
    @Test public void immutableRepresentationsAndExplicitResourceLimitsAreEnforced() {
        PresentedAbelianGroup group=diagonal(2,3); AbelianGroupElement element=group.project(v(1,1));
        assertThrows(UnsupportedOperationException.class,() -> group.elements().clear());
        assertThrows(UnsupportedOperationException.class,() -> element.smithCoordinates().entries().clear());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> group.project(v(1)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> group.fromSmith(v(1)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> diagonal(0).elements());
        assertEquals(4096,diagonal(4096).elements().size());
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> diagonal(4097).elements());
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> diagonal(4097).project(v(1)).cyclicSubgroup());
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> PresentedAbelianGroup.fromType(AbelianGroupType.free(BigInteger.TEN.pow(100))));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> new PresentedAbelianGroup(IntegerMatrix.identity(256)));
        PresentedAbelianGroup free=new PresentedAbelianGroup(IntegerMatrix.zero(129,0));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> free.directSum(free));
    }
    @Test public void nativeQuotientOperationsPreserveTheOriginalAlgebraWrappers() {
        ConcreteMathematics math=new ConcreteMathematics(); PresentedAbelianGroup group=diagonal(2,3);
        IAlgebraItem<PresentedAbelianGroup> item=math.integerMatrices.algebra().buildAlgebraItem(group.relations()).performAlgebraTransfer("PresentedAbelianGroup.from-matrix");
        assertSame(math.presentedAbelianGroups.algebra(),item.getAlgebra());
        IAlgebraItem<AbelianGroupElement> element=item.performUnsafeOperation("AbelianGroupElement.project",v(1,1));
        assertSame(math.abelianGroupElements.algebra(),element.getAlgebra()); assertEquals(group.project(v(1,1)),element.getResult());
        assertSame(math.integerVectors.algebra(),item.performLeftProjectionOperation("AbelianGroupElement.reduce",v(1,1)).getAlgebra());
        List<IAlgebraItem<AbelianGroupElement>> generators=item.performAlgebraFlatTransfer("AbelianGroupElement.generators");
        assertEquals(2,generators.size()); for(IAlgebraItem<AbelianGroupElement> result : generators) assertSame(math.abelianGroupElements.algebra(),result.getAlgebra());
        IAlgebraItem<AbelianGroupElement> scaled=element.performCustomMemberOperation("scale",z(2)); assertSame(math.abelianGroupElements.algebra(),scaled.getAlgebra());
        List<IAlgebraItem<AbelianGroupElement>> fiber=math.abelianGroupElements.algebra().buildAlgebraItem(group.fromSmith(v(0,2))).performCustomMemberFlatOperation("multiplication-preimages",z(2));
        assertEquals(2,fiber.size()); for(IAlgebraItem<AbelianGroupElement> result : fiber) assertSame(math.abelianGroupElements.algebra(),result.getAlgebra());
    }
    @Test public void serializedProjectionEnumerationAndFiberFlowsRemainRepeatable() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); PresentedAbelianGroup group=diagonal(2,3);
        IAlgebraFlow<BigInteger> flow=math.flow(math.integerMatrices,Collections.singletonList(group.relations()))
                .<PresentedAbelianGroup>performAlgebraTransfer("PresentedAbelianGroup.from-matrix")
                .<AbelianGroupElement>performFlatAlgebraTransfer("AbelianGroupElement.elements")
                .<BigInteger>performAlgebraTransfer("order");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Arrays.asList("1","6","3","2","3","6"),restored.collect()); assertEquals(restored.collect(),restored.collect());
        assertEquals(Arrays.asList("[0, 1]","[0, 4]"),math.flow(math.presentedAbelianGroups,Collections.singletonList(group))
                .<AbelianGroupElement,IntegerVector>performAlgebraUnsafe("AbelianGroupElement.from-smith",v(0,2))
                .performFlatCustomMemberOperation("multiplication-preimages",z(2))
                .<IntegerVector>performAlgebraTransfer("smith-coordinates").collect());
    }
}
