package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.linear.*;
import mathematics.structures.*;
import mathematics.topology.*;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeRelativeCapProductTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static IntegerVector v(long... values) { BigInteger[] result=new BigInteger[values.length]; for(int i=0;i<values.length;i++) result[i]=z(values[i]); return new IntegerVector(result); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int vertex : facet) labels.add(vertex); faces.add(new FiniteSet<>(labels)); } return new FiniteSimplicialComplex(faces);
    }
    private static RelativeSimplicialComplex pair(FiniteSimplicialComplex x,FiniteSimplicialComplex a) { return new RelativeSimplicialComplex(x,a); }
    private static RelativeSimplicialChain chain(RelativeSimplicialComplex pair,int degree,long... values) { return new RelativeSimplicialChain(pair,z(degree),v(values)); }
    private static RelativeSimplicialCochain cochain(RelativeSimplicialComplex pair,int degree,long... values) { return new RelativeSimplicialCochain(pair,z(degree),v(values)); }
    private static FiniteSimplicialComplex horizontal() { return complex(new int[]{0,1},new int[]{2,3}); }
    private static FiniteSimplicialComplex vertical() { return complex(new int[]{0,3},new int[]{1,2}); }
    private static FiniteSimplicialComplex square() { return complex(new int[]{0,1,2},new int[]{0,2,3}); }
    private static RelativeCapProduct squareCap() { FiniteSimplicialComplex x=square(); return new RelativeCapProduct(chain(pair(x,horizontal().union(vertical())),2,1,1),pair(x,vertical())); }
    private static RelativeSimplicialCochain squareCochain() { return cochain(pair(square(),horizontal()),1,1,1,1); }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    @Test public void oppositeSquareEdgesGiveNonzeroGeneralRelativeDualityAndExplicitCoefficients() {
        RelativeCapProduct cap=squareCap(); RelativeSimplicialCochain phi=squareCochain(); RelativeSimplicialChain result=cap.cap(phi);
        assertEquals(v(0,0,1),result.coordinates()); assertEquals(pair(square(),vertical()),result.pair()); assertEquals(z(1),result.degree());
        assertTrue(result.isCycle()); assertFalse(result.isBoundary()); assertEquals(AbelianGroupType.Z,result.homology().type());
        assertTrue(cap.capCohomologyMap(phi).isIsomorphism()); assertTrue(cap.capHomologyMap(phi).isIsomorphism());
        assertEquals(result.classOf(),cap.capClass(phi)); assertEquals(result.classOf(),cap.capHomologyMap(phi).apply(cap.chain().classOf()));
        assertEquals(result.classOf(),cap.capCohomologyMap(phi).apply(phi.classOf()));
        assertEquals(AbelianGroupType.cyclic(z(3)),cap.withChain(cap.chain().scale(z(3))).capCohomologyMap(phi).cokernel().type());
        assertEquals(AbelianGroupType.cyclic(z(2)),cap.capHomologyMap(phi.scale(z(2))).cokernel().type());
    }
    @Test public void all729CoefficientPairsObeyIndependentFormulaBothMatricesAndSignedBoundary() {
        FiniteSimplicialComplex x=complex(new int[]{0,1,2}),a=complex(new int[]{0}),b=complex(new int[]{2});
        RelativeSimplicialComplex source=pair(x,a.union(b)),target=pair(x,b),dual=pair(x,a);
        for(int i=0;i<27;i++) for(int j=0;j<27;j++) {
            long[] c=new long[3],f=new long[3]; int ii=i,jj=j; for(int k=0;k<3;k++) { c[k]=ii%3-1; ii/=3; f[k]=jj%3-1; jj/=3; }
            RelativeCapProduct cap=new RelativeCapProduct(chain(source,1,c),target); RelativeSimplicialCochain phi=cochain(dual,1,f);
            assertEquals(v(0,c[0]*f[0]),cap.cap(phi).coordinates());
            assertEquals(cap.cap(phi).coordinates(),cap.capMatrix(phi).multiply(cap.chain().coordinates()));
            assertEquals(cap.cap(phi).coordinates(),cap.capCohomologyMatrix(phi).multiply(phi.coordinates()));
            RelativeCapProduct face=cap.withChain(chain(source,2,c[0]));
            assertEquals(v(0,0,c[0]*f[0]),face.cap(phi).coordinates()); // Back edge 12 survives, although vertex 2 is in B.
            assertEquals(face.cap(phi).boundary(),face.boundary().cap(phi).subtract(face.cap(phi.coboundary())).negate());
            RelativeSimplicialCochain function=cochain(dual,0,f[0],f[1]);
            assertEquals(face.cap(function).boundary(),face.boundary().cap(function).subtract(face.cap(function.coboundary())));
        }
    }
    @Test public void all361TriangleSubcomplexPairsSatisfyTheBoundaryIdentityInEveryDegree() {
        FiniteSimplicialComplex x=complex(new int[]{0,1,2}); List<FiniteSet<Integer>> faces=new ArrayList<>();
        for(int k=0;k<=2;k++) faces.addAll(x.simplices(k)); Set<FiniteSimplicialComplex> subcomplexes=new HashSet<>();
        for(int mask=0;mask<128;mask++) { List<FiniteSet<Integer>> selected=new ArrayList<>(); for(int i=0;i<7;i++) if((mask&(1<<i))!=0) selected.add(faces.get(i)); subcomplexes.add(new FiniteSimplicialComplex(selected)); }
        assertEquals(19,subcomplexes.size());
        for(FiniteSimplicialComplex a : subcomplexes) for(FiniteSimplicialComplex b : subcomplexes) {
            RelativeSimplicialComplex source=pair(x,a.union(b)),target=pair(x,b),dual=pair(x,a);
            for(int n=0;n<=2;n++) for(int p=0;p<=3;p++) {
                for(RelativeSimplicialChain c : RelativeSimplicialChain.basisChains(source,z(n))) for(RelativeSimplicialCochain phi : RelativeSimplicialCochain.basisCochains(dual,z(p))) {
                    RelativeCapProduct cap=new RelativeCapProduct(c,target); RelativeSimplicialChain rhs=cap.boundary().cap(phi).subtract(cap.cap(phi.coboundary()));
                    assertEquals(cap.cap(phi).boundary(),rhs.scale(z((p&1)==0?1:-1)));
                }
            }
        }
    }
    @Test public void bothExistingRelativeCapsAreSpecialCasesIncludingOverlappingSubcomplexes() {
        RelativeSimplicialComplex source=pair(complex(new int[]{0,1,2}),complex(new int[]{1,2})); RelativeSimplicialChain c=chain(source,2,3);
        SimplicialCochain absolute=new SimplicialCochain(source.ambient(),z(1),v(2,5,7)); RelativeSimplicialCochain relative=cochain(source,1,2,5);
        RelativeCapProduct action=new RelativeCapProduct(c,source),lift=new RelativeCapProduct(c,RelativeSimplicialComplex.absolute(source.ambient()));
        assertEquals(c.cap(absolute),action.cap(RelativeSimplicialCochain.absolute(absolute)));
        assertEquals(RelativeSimplicialChain.absolute(c.relativeCap(relative)),lift.cap(relative));
        assertEquals(c.relativeCapMatrix(relative),lift.capMatrix(relative)); assertEquals(c.relativeCapCohomologyMatrix(z(1)),lift.capCohomologyMatrix(relative));
        assertTrue(action.cap(relative).isZero()); // A=B is allowed, and its back edge lies in B.
        assertEquals(RelativeSimplicialChain.absolute(c.relativeCap(relative)).coordinates(),v(0,0,6));
    }
    @Test public void generalCapsComposeWithRelativeCupProductsForThreeIndependentSubcomplexes() {
        FiniteSimplicialComplex x=complex(new int[]{0,1,2,3}),a=complex(new int[]{0}),b=complex(new int[]{1}),d=complex(new int[]{3});
        RelativeSimplicialChain c=chain(pair(x,a.union(b).union(d)),3,5);
        RelativeSimplicialCochain alpha=cochain(pair(x,a),1,2,3,5,7,11,13),beta=cochain(pair(x,b),1,17,19,23,29,31,37);
        RelativeSimplicialChain first=new RelativeCapProduct(c,pair(x,b.union(d))).cap(alpha);
        RelativeSimplicialChain iterated=new RelativeCapProduct(first,pair(x,d)).cap(beta);
        assertEquals(new RelativeCapProduct(c,pair(x,d)).cap(alpha.cup(beta)),iterated);
        assertEquals(v(0,0,0,0,0,290),iterated.coordinates()); // 5 * alpha(01) * beta(12), on edge 23.
    }
    @Test public void classDoesNotDependOnCocycleRepresentativeOrRelativeCycleRepresentative() {
        FiniteSimplicialComplex x=complex(new int[]{-1,0,1},new int[]{-1,0,3},new int[]{-1,1,2},new int[]{-1,2,3});
        RelativeSimplicialComplex source=pair(x,horizontal().union(vertical())),dual=pair(x,horizontal()),target=pair(x,vertical());
        RelativeCapProduct cap=new RelativeCapProduct(chain(source,2,1,-1,1,1),target);
        RelativeSimplicialCochain phi=RelativeSimplicialCochain.fromAbsolute(new SimplicialCochain(x,z(0),v(0,0,0,1,1)).coboundary(),dual);
        RelativeSimplicialCochain changed=phi.add(cochain(dual,0,7).coboundary());
        assertNotEquals(cap.cap(phi),cap.cap(changed)); assertEquals(cap.capClass(phi),cap.capClass(changed));
        assertEquals(cap.capHomologyMap(phi),cap.capHomologyMap(changed)); assertTrue(cap.capCohomologyMap(phi).isIsomorphism());
        FiniteSimplicialComplex larger=square().union(complex(new int[]{10,11,12,13})); source=pair(larger,horizontal().union(vertical()));
        RelativeSimplicialChain c=chain(source,2,1,1,0,0,0,0),changedChain=c.add(chain(source,3,7).boundary());
        phi=cochain(pair(larger,horizontal()),1,1,1,1,0,0,0,0,0,0);
        RelativeCapProduct original=new RelativeCapProduct(c,pair(larger,vertical()));
        assertNotEquals(c,changedChain); assertEquals(original.capClass(phi),original.withChain(changedChain).capClass(phi));
        assertEquals(original.capCohomologyMap(phi),original.withChain(changedChain).capCohomologyMap(phi));
    }
    @Test public void capClassesAreNaturalUnderAllEightSquareSymmetriesWithSeparatePairs() {
        FiniteSimplicialComplex x=complex(new int[]{0,1,4},new int[]{0,3,4},new int[]{1,2,4},new int[]{2,3,4});
        RelativeSimplicialComplex source=pair(x,horizontal().union(vertical())),dual=pair(x,horizontal()),target=pair(x,vertical());
        RelativeSimplicialChain c=chain(source,2,1,-1,1,1);
        RelativeSimplicialCochain phi=RelativeSimplicialCochain.fromAbsolute(new SimplicialCochain(x,z(0),v(0,0,1,1,0)).coboundary(),dual);
        for(int offset=0;offset<4;offset++) for(int sign : new int[]{-1,1}) {
            int[] image=new int[5]; Map<BigInteger,BigInteger> images=new TreeMap<>(); for(int i=0;i<4;i++) { image[i]=Math.floorMod(offset+sign*i,4); images.put(z(i),z(image[i])); } image[4]=4; images.put(z(4),z(4));
            FiniteSimplicialMap f=new FiniteSimplicialMap(x,x,images);
            RelativeSimplicialComplex mappedDual=pair(x,complex(new int[]{image[0],image[1]},new int[]{image[2],image[3]}));
            RelativeSimplicialComplex mappedTarget=pair(x,complex(new int[]{image[0],image[3]},new int[]{image[1],image[2]}));
            RelativeSimplicialMap onSource=new RelativeSimplicialMap(source,source,f),onDual=new RelativeSimplicialMap(dual,mappedDual,f),onTarget=new RelativeSimplicialMap(target,mappedTarget,f);
            RelativeSimplicialCochain psi=phi.pullback(onDual.inverse());
            assertEquals(new RelativeCapProduct(c.pushforward(onSource),mappedTarget).capClass(psi),new RelativeCapProduct(c,target).cap(psi.pullback(onDual)).pushforward(onTarget).classOf());
        }
    }
    @Test public void separateSubcomplexesPreserveTorsionInBothInducedMaps() {
        FiniteSimplicialComplex plane=complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        FiniteSimplicialComplex a=complex(new int[]{6}),b=complex(new int[]{0}),x=plane.union(a);
        RelativeSimplicialChain torsion=RelativeSimplicialChain.zero(pair(x,a.union(b)),z(1)).cycleGenerators().get(0);
        RelativeCapProduct cap=new RelativeCapProduct(torsion,pair(x,b)); RelativeSimplicialCochain phi=cochain(pair(x,a),0,1,1,1,1,1,1);
        assertEquals(AbelianGroupType.cyclic(z(2)),cap.cap(phi).homology().type()); assertFalse(cap.capClass(phi).isZero());
        assertTrue(cap.capClass(phi.scale(z(2))).isZero()); assertTrue(cap.capHomologyMap(phi).isIsomorphism());
        assertEquals(cap.capClass(phi),cap.capCohomologyMap(phi).apply(phi.classOf())); assertTrue(cap.capCohomologyMap(phi).isSurjective());
        assertEquals(AbelianGroupType.Z,cap.capCohomologyMap(phi).source().type());
    }
    @Test public void fixedArgumentMapsIgnoreOnlyTheDocumentedCoordinates() {
        RelativeCapProduct cap=squareCap(); RelativeSimplicialCochain phi=squareCochain(),noncocycle=phi.withCoordinates(v(1,0,0));
        assertFalse(noncocycle.isCocycle());
        assertEquals(cap.capCohomologyMatrix(phi),cap.capCohomologyMatrix(noncocycle)); assertEquals(cap.capCohomologyMap(phi),cap.capCohomologyMap(noncocycle));
        RelativeCapProduct noncycle=cap.withChain(cap.chain().withCoordinates(v(1,0))); assertFalse(noncycle.chain().isCycle());
        assertEquals(cap.capMatrix(phi),noncycle.capMatrix(phi)); assertEquals(cap.capHomologyMap(phi),noncycle.capHomologyMap(phi));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> cap.capClass(noncocycle)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> cap.capHomologyMap(noncocycle));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> noncycle.capClass(phi)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> noncycle.capCohomologyMap(phi));
    }
    @Test public void wrongFullPairsAndWrongUnionAreRejectedBeforeComputing() {
        RelativeCapProduct cap=squareCap(); FiniteSimplicialComplex x=square();
        RelativeSimplicialCochain wrongUnion=RelativeSimplicialCochain.zero(RelativeSimplicialComplex.absolute(x),z(1));
        for(Runnable action : Arrays.<Runnable>asList(() -> cap.cap(wrongUnion),() -> cap.capClass(wrongUnion),() -> cap.capMatrix(wrongUnion),() -> cap.capHomologyMap(wrongUnion),() -> cap.capCohomologyMatrix(wrongUnion),() -> cap.capCohomologyMap(wrongUnion),
                () -> cap.cap(RelativeSimplicialCochain.zero(pair(x.union(complex(new int[]{8})),horizontal()),z(1))),
                () -> new RelativeCapProduct(cap.chain(),RelativeSimplicialComplex.diagonal(x)),
                () -> new RelativeCapProduct(cap.chain(),RelativeSimplicialComplex.absolute(complex(new int[]{0,1}))),
                () -> cap.withChain(RelativeSimplicialChain.zero(pair(x,horizontal()),z(2))))) failure(MathFailure.Kind.OPERATION_UNDEFINED,action);
    }
    @Test public void filteredBasesAvoidAbsoluteLimitsAndCompoundMapsShareWorkBudgets() {
        List<FiniteSet<Integer>> points=new ArrayList<>(); for(int i=0;i<300;i++) points.add(FiniteSet.of(i));
        FiniteSimplicialComplex x=new FiniteSimplicialComplex(points),a=new FiniteSimplicialComplex(points.subList(0,299)),b=new FiniteSimplicialComplex(points.subList(0,298));
        RelativeCapProduct small=new RelativeCapProduct(chain(pair(x,a),0,3),pair(x,b)); RelativeSimplicialCochain phi=cochain(pair(x,a),0,2);
        assertEquals(v(0,6),small.cap(phi).coordinates()); assertEquals(small.capClass(phi),small.capHomologyMap(phi).apply(small.chain().classOf()));
        assertEquals(small.capClass(phi),small.capCohomologyMap(phi).apply(phi.classOf()));
        RelativeCapProduct oversized=new RelativeCapProduct(small.chain(),RelativeSimplicialComplex.absolute(x)); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> oversized.cap(phi));
        RelativeSimplicialComplex large=RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(points.subList(0,100)));
        RelativeCapProduct cap=new RelativeCapProduct(RelativeSimplicialChain.zero(large,z(0)),large); RelativeSimplicialCochain unit=RelativeSimplicialCochain.absolute(SimplicialCochain.unit(large.ambient()));
        assertEquals(AbelianGroupType.free(z(100)),cap.chain().homology().type()); assertEquals(AbelianGroupType.free(z(100)),unit.cohomology().type());
        assertEquals(IntegerMatrix.identity(100),cap.capMatrix(unit)); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> cap.capHomologyMap(unit)); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> cap.capCohomologyMap(unit));
    }
    @Test public void negativeAndHugeDegreesRetainZeroGroupsAndExactTargetContext() {
        RelativeCapProduct cap=squareCap(); BigInteger huge=BigInteger.TEN.pow(100); RelativeSimplicialCochain phi=squareCochain();
        for(BigInteger n : Arrays.asList(z(-1),huge,huge.negate())) {
            RelativeCapProduct zero=cap.withChain(RelativeSimplicialChain.zero(cap.chain().pair(),n));
            assertEquals(n.subtract(z(1)),zero.cap(phi).degree()); assertEquals(cap.targetPair(),zero.cap(phi).pair()); assertTrue(zero.capClass(phi).isZero());
            assertTrue(zero.capHomologyMap(phi).isZero()); assertTrue(zero.capCohomologyMap(phi).isZero());
        }
        RelativeSimplicialCochain high=RelativeSimplicialCochain.zero(phi.pair(),huge); assertTrue(cap.cap(high).isZero()); assertTrue(cap.capCohomologyMap(high).isZero());
        RelativeSimplicialComplex empty=RelativeSimplicialComplex.absolute(complex()); RelativeCapProduct e=new RelativeCapProduct(chain(empty,0),empty);
        assertEquals(IntegerMatrix.zero(0,0),e.capMatrix(cochain(empty,0))); assertEquals(cap,cap.withChain(cap.chain())); assertEquals(cap.hashCode(),cap.withChain(cap.chain()).hashCode());
        assertNotEquals(cap,new RelativeCapProduct(cap.chain(),cap.chain().pair()));
    }
    @Test public void originalWrappersAndSerializedFlowsRetainTheExplicitTarget() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); RelativeCapProduct cap=squareCap(); RelativeSimplicialCochain phi=squareCochain();
        IAlgebraItem<?> context=math.relativeChains.algebra().buildAlgebraItem(cap.chain()).performUnsafeOperation("RelativeCap.on",cap.targetPair()); assertSame(math.relativeCaps.algebra(),context.getAlgebra()); assertEquals(cap,context.perform().getResult());
        IAlgebraItem<RelativeCapProduct> replaced=math.relativeCaps.algebra().buildAlgebraItem(cap).performCustomMemberOperation("with-chain",cap.chain()); assertSame(math.relativeCaps.algebra(),replaced.getAlgebra());
        assertSame(math.relativeChains.algebra(),replaced.performUnsafeOperation("cap",phi).getAlgebra());
        assertSame(math.relativeCaps.algebra(),replaced.performOneOperandOperation("boundary").getAlgebra());
        IAlgebraFlow<IntegerVector> flow=math.flow(math.relativeChains,Collections.singletonList(cap.chain()))
                .<RelativeCapProduct,RelativeSimplicialComplex>performAlgebraUnsafe("RelativeCap.on",cap.targetPair())
                .<RelativeSimplicialChain,RelativeSimplicialCochain>performAlgebraUnsafe("cap",phi).<IntegerVector>performAlgebraTransfer("coordinates");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("[0, 0, 1]"),restored.collect()); assertEquals(restored.collect(),restored.collect());
    }
}
