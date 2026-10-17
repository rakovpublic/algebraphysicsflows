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

public class NativeSimplicialChainEquivalenceTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int v : facet) labels.add(v); faces.add(new FiniteSet<>(labels)); } return new FiniteSimplicialComplex(faces);
    }
    private static RelativeSimplicialComplex abs(FiniteSimplicialComplex c) { return RelativeSimplicialComplex.absolute(c); }
    private static RelativeSimplicialComplex points(int n) { int[][] f=new int[n][1]; for(int i=0;i<n;i++) f[i][0]=i; return abs(complex(f)); }
    private static RelativeSimplicialComplex edge() { return abs(complex(new int[]{0,1})); }
    private static RelativeSimplicialComplex circle() { return abs(complex(new int[]{0,1},new int[]{0,2},new int[]{1,2})); }
    private static IntegerMatrix matrix(long[]... rows) {
        BigInteger[][] result=new BigInteger[rows.length][]; for(int r=0;r<rows.length;r++) { result[r]=new BigInteger[rows[r].length]; for(int c=0;c<rows[r].length;c++) result[r][c]=z(rows[r][c]); } return new IntegerMatrix(result);
    }
    private static MathFailure failure(MathFailure.Kind kind,Runnable body) { MathFailure e=assertThrows(MathFailure.class,body::run); assertEquals(kind,e.kind()); return e; }
    private static void boundaries(SimplicialChainEquivalence e) {
        assertEquals(e.backward().compose(e.forward()),e.sourceHomotopy().from());
        assertEquals(e.forward().compose(e.backward()),e.targetHomotopy().from());
        for(SimplicialChainHomotopy h : e.homotopies()) {
            assertEquals(SimplicialChainMap.identity(h.source()),h.to());
            for(int d=0;d<=h.source().ambient().dimension();d++) {
                IntegerMatrix actual=h.target().boundaryMatrix(z(d+1)).multiply(h.chainMatrix(z(d)))
                        .add(h.cochainMatrix(z(d)).transpose().multiply(h.source().boundaryMatrix(z(d))));
                assertEquals(h.to().chainMatrix(z(d)).add(h.from().chainMatrix(z(d)).scale(z(-1))),actual);
            }
        }
    }
    private static SimplicialChainMap intervalMap(int a,int b) {
        return new SimplicialChainMap(edge(),edge(),Arrays.asList(matrix(new long[]{a,b},new long[]{1-a,1-b}),matrix(new long[]{a-b})));
    }
    private static SimplicialChainMap circleMap(int h0,int h1) {
        return new SimplicialChainMap(circle(),circle(),Arrays.asList(IntegerMatrix.identity(3).scale(z(h0)),matrix(new long[]{h1,0,0},new long[]{h0-h1,h0,0},new long[]{h1-h0,0,h0})));
    }
    private static IntegerMatrix loopMatrix(long n) { return matrix(new long[]{n,n,n},new long[]{-n,-n,-n},new long[]{n,n,n}); }
    private static SimplicialChainHomotopy loop(long n) {
        SimplicialChainMap id=SimplicialChainMap.identity(circle()); return new SimplicialChainHomotopy(id,id,Arrays.asList(loopMatrix(n),IntegerMatrix.zero(0,3)));
    }
    private static SimplicialChainEquivalence circleEquivalence(int h0,int h1,int h,int k) {
        SimplicialChainMap f=circleMap(h0,h1); return new SimplicialChainEquivalence(f,f.inverse(),loop(h),loop(k));
    }
    @Test public void solverBundlesTheSameMapsAndBothOrientedWitnesses() {
        for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) {
            SimplicialChainMap f=intervalMap(a,b); SimplicialChainEquivalence e=SimplicialChainEquivalence.fromMap(f);
            assertEquals(f,e.forward()); assertEquals(SimplicialChainInverseSolver.inverse(f),e.backward());
            assertEquals(SimplicialChainInverseSolver.inverseHomotopies(f),e.homotopies()); boundaries(e);
        }
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialChainEquivalence.fromMap(SimplicialChainMap.identity(points(1)).scale(z(2))));
    }
    @Test public void suppliedNonzeroLoopsRemainDistinctAndImmutable() {
        SimplicialChainEquivalence a=circleEquivalence(1,1,1,2),b=circleEquivalence(1,1,2,1);
        assertNotEquals(a,b); assertNotEquals(a.data(),b.data()); assertEquals(a.forwardClass(),b.forwardClass());
        assertEquals(a,new SimplicialChainEquivalence(a.data())); assertEquals(a.hashCode(),new SimplicialChainEquivalence(a.data()).hashCode());
        assertEquals(a,a.inverse().inverse()); assertEquals(a.sourceHomotopy(),a.inverse().targetHomotopy());
        assertThrows(UnsupportedOperationException.class,a.maps()::clear); assertThrows(UnsupportedOperationException.class,a.homotopies()::clear);
        assertThrows(UnsupportedOperationException.class,() -> a.homologyMaps(z(1)).clear()); assertThrows(UnsupportedOperationException.class,() -> a.cohomologyMaps(z(1)).clear());
    }
    @Test public void all1296CircleCompositionsMatchIndependentSignedLoopCoefficients() {
        for(int a : new int[]{-1,1}) for(int b : new int[]{-1,1}) for(int c : new int[]{-1,1}) for(int d : new int[]{-1,1})
            for(int h=-1;h<=1;h++) for(int k=-1;k<=1;k++) for(int j=-1;j<=1;j++) for(int l=-1;l<=1;l++) {
                SimplicialChainEquivalence before=circleEquivalence(a,b,h,k),after=circleEquivalence(c,d,j,l),e=after.compose(before);
                assertEquals(circleMap(a*c,b*d),e.forward());
                assertEquals(loopMatrix(a*b*j+h),e.sourceHomotopy().chainMatrix(z(0)));
                assertEquals(loopMatrix(c*d*k+l),e.targetHomotopy().chainMatrix(z(0)));
                assertEquals(before.inverse().compose(after.inverse()),e.inverse());
            }
    }
    @Test public void retainedCompositionIsAssociativeAndUnitalForNontrivialWitnesses() {
        SimplicialChainEquivalence a=circleEquivalence(-1,1,2,-3),b=circleEquivalence(1,-1,-1,4),c=circleEquivalence(-1,-1,5,2),id=SimplicialChainEquivalence.identity(circle());
        assertEquals(c.compose(b).compose(a),c.compose(b.compose(a))); assertEquals(a,id.compose(a)); assertEquals(a,a.compose(id));
        assertNotEquals(id,a.inverse().compose(a)); assertTrue(a.backwardClass().compose(a.forwardClass()).isIdentity());
    }
    @Test public void noncommutingPointMapsKeepRightOperandFirstAndDualVariance() {
        SimplicialChainEquivalence a=SimplicialChainEquivalence.fromMap(new SimplicialChainMap(points(2),points(2),Collections.singletonList(matrix(new long[]{1,2},new long[]{0,1})))),
                b=SimplicialChainEquivalence.fromMap(new SimplicialChainMap(points(2),points(2),Collections.singletonList(matrix(new long[]{1,0},new long[]{3,1}))));
        SimplicialChainEquivalence e=a.compose(b); assertEquals(matrix(new long[]{7,2},new long[]{3,1}),e.forward().chainMatrix(z(0))); assertNotEquals(e,b.compose(a));
        assertEquals(b.inverse().compose(a.inverse()),e.inverse());
        assertEquals(a.homologyMap(z(0)).compose(b.homologyMap(z(0))),e.homologyMap(z(0)));
        assertEquals(b.cohomologyMap(z(0)).compose(a.cohomologyMap(z(0))),e.cohomologyMap(z(0)));
    }
    @Test public void changingDimensionWitnessesSatisfyBothBoundaryIdentitiesAfterComposition() {
        RelativeSimplicialComplex triangle=abs(complex(new int[]{0,1,2}));
        SimplicialChainEquivalence a=SimplicialChainEquivalence.fromMap(SimplicialChainMap.fromSimplicial(RelativeSimplicialMap.inclusion(points(1),edge()))),
                b=SimplicialChainEquivalence.fromMap(SimplicialChainMap.fromSimplicial(RelativeSimplicialMap.inclusion(edge(),triangle))),e=b.compose(a);
        assertEquals(points(1),e.source()); assertEquals(triangle,e.target()); boundaries(a); boundaries(b); boundaries(e); boundaries(e.inverse());
        assertEquals(IntegerMatrix.zero(0,1),e.backward().chainMatrix(z(2))); assertEquals(1,e.sourceHomotopy().chainMatrices().size()); assertEquals(3,e.targetHomotopy().chainMatrices().size());
        for(int d=0;d<=3;d++) { assertEquals(e.homologyMap(z(d)).inverse(),e.inverseHomologyMap(z(d))); assertEquals(e.cohomologyMap(z(d)).inverse(),e.inverseCohomologyMap(z(d))); }
    }
    @Test public void constructorChecksExactCompositeAndIdentityMapsNotOnlyHomotopyClasses() {
        SimplicialChainEquivalence e=SimplicialChainEquivalence.fromMap(intervalMap(1,1)); SimplicialChainMap id=SimplicialChainMap.identity(edge());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialChainEquivalence(e.forward(),e.backward(),SimplicialChainHomotopy.stationary(id),e.targetHomotopy()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialChainEquivalence(e.forward(),e.backward(),e.sourceHomotopy().reverse(),e.targetHomotopy()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialChainEquivalence(e.forward(),e.backward(),SimplicialChainHomotopy.stationary(e.sourceHomotopy().from()),e.targetHomotopy()));
    }
    @Test public void fullLabelsSubcomplexesAndBothOppositeEndpointsAreRequired() {
        SimplicialChainEquivalence a=SimplicialChainEquivalence.identity(points(1)),other=SimplicialChainEquivalence.identity(abs(complex(new int[]{7})));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> a.compose(other));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialChainEquivalence(a.forward(),other.backward(),a.sourceHomotopy(),a.targetHomotopy()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialChainEquivalence(a.forward(),a.backward(),other.sourceHomotopy(),a.targetHomotopy()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialChainEquivalence(a.forward(),a.backward(),a.sourceHomotopy(),other.targetHomotopy()));
        SimplicialChainEquivalence b=SimplicialChainEquivalence.identity(new RelativeSimplicialComplex(edge().ambient(),complex(new int[]{0}))),
                c=SimplicialChainEquivalence.identity(new RelativeSimplicialComplex(edge().ambient(),complex(new int[]{1})));
        assertNotEquals(b,c); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> b.compose(c));
    }
    private static RelativeSimplicialComplex torsionPair() {
        int[][] f={{0,1,2},{0,1,3},{0,2,4},{0,3,5},{0,4,5},{1,2,5},{1,3,4},{1,4,5},{2,3,4},{2,3,5}};
        return new RelativeSimplicialComplex(complex(f),complex(Arrays.copyOf(f,5)));
    }
    @Test public void torsionAndUnboundedCoefficientsRetainBothInverseIntegralMaps() {
        RelativeSimplicialComplex p=torsionPair(); assertEquals(AbelianGroupType.cyclic(z(2)),p.homologyType(z(1)));
        SimplicialChainEquivalence e=SimplicialChainEquivalence.fromMap(SimplicialChainMap.identity(p).scale(BigInteger.ONE.shiftLeft(1024).add(BigInteger.ONE)));
        assertFalse(e.forward().isIsomorphism()); boundaries(e);
        assertEquals(e.homologyMap(z(1)).inverse(),e.inverseHomologyMap(z(1))); assertEquals(e.cohomologyMap(z(2)).inverse(),e.inverseCohomologyMap(z(2)));
        assertEquals(Arrays.asList(e.homologyMap(z(1)),e.inverseHomologyMap(z(1))),e.homologyMaps(z(1)));
        assertEquals(Arrays.asList(e.cohomologyMap(z(2)),e.inverseCohomologyMap(z(2))),e.cohomologyMaps(z(2)));
        assertTrue(e.forwardClass().compose(e.backwardClass()).isIdentity());
    }
    @Test public void rawChainsNeedNotBeFixedByInverseComposition() {
        SimplicialChainEquivalence e=SimplicialChainEquivalence.fromMap(intervalMap(1,1)); RelativeSimplicialChain v=new RelativeSimplicialChain(edge(),z(0),new IntegerVector(z(1),z(0)));
        assertNotEquals(v,e.inverseOnChain(e.onChain(v))); assertEquals(v.classOf(),e.inverseOnChain(e.onChain(v)).classOf());
        RelativeSimplicialCochain c=new RelativeSimplicialCochain(edge(),z(0),new IntegerVector(z(2),z(3)));
        assertEquals(e.forward().onCochain(c),e.onCochain(c)); assertEquals(e.backward().onCochain(c),e.inverseOnCochain(c));
        assertEquals(RelativeSimplicialChain.zero(edge(),z(-1)),e.onChain(RelativeSimplicialChain.zero(edge(),z(-1))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> e.onChain(RelativeSimplicialChain.zero(points(1),z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> e.inverseOnChain(RelativeSimplicialChain.zero(points(1),z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> e.onCochain(RelativeSimplicialCochain.zero(points(1),z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> e.inverseOnCochain(RelativeSimplicialCochain.zero(points(1),z(0))));
    }
    @Test public void emptyAndDiagonalPairsRetainBothWitnessesAndMapListEntries() {
        for(RelativeSimplicialComplex p : Arrays.asList(points(0),RelativeSimplicialComplex.diagonal(edge().ambient()))) {
            SimplicialChainEquivalence e=SimplicialChainEquivalence.identity(p); assertEquals(e,SimplicialChainEquivalence.fromMap(e.forward())); boundaries(e);
            assertEquals(2,e.maps().size()); assertEquals(2,e.homotopies().size()); assertEquals(2,e.homologyMaps(z(0)).size()); assertEquals(2,e.cohomologyMaps(z(5)).size());
            assertEquals(IntegerMatrix.zero(0,0),e.forward().chainMatrix(z(2))); assertEquals(e,e.compose(e));
        }
    }
    @Test public void suppliedDataAndCompositionWorkBeyondInverseSearchEquationBounds() {
        SimplicialChainEquivalence e=SimplicialChainEquivalence.identity(points(12));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialChainEquivalence.fromMap(e.forward())).getMessage().contains("256 total equations"));
        assertEquals(e,new SimplicialChainEquivalence(e.data())); assertEquals(e,e.inverse()); assertEquals(e,e.compose(e));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialChainEquivalence.identity(points(257)));
    }
    @Test public void integralDegreesRejectNegativeAndRetainZeroMapsAboveDimension() {
        SimplicialChainEquivalence e=SimplicialChainEquivalence.identity(points(1));
        for(Runnable action : Arrays.<Runnable>asList(() -> e.homologyMap(z(-1)),() -> e.inverseHomologyMap(z(-1)),() -> e.cohomologyMap(z(-1)),() -> e.inverseCohomologyMap(z(-1)),() -> e.homologyMaps(z(-1)),() -> e.cohomologyMaps(z(-1)))) failure(MathFailure.Kind.OPERATION_UNDEFINED,action);
        for(AbelianGroupHomomorphism m : e.homologyMaps(BigInteger.ONE.shiftLeft(64))) assertTrue(m.isZero());
    }
    @Test public void wholeWitnessCompositionSharesTheBudgetAcrossSuccessfulIndividualStages() {
        SimplicialChainEquivalence e=SimplicialChainEquivalence.identity(points(80));
        assertEquals(e.forward(),e.forward().compose(e.forward()));
        assertEquals(e.sourceHomotopy(),e.sourceHomotopy().precompose(e.forward()).postcompose(e.backward()).then(e.sourceHomotopy()));
        assertEquals(e,new SimplicialChainEquivalence(e.data()));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> e.compose(e)).getMessage().contains("5000000"));
    }
    @Test public void wholeIntegralMapListsShareTheBudgetAcrossTwoIndividuallySuccessfulMaps() {
        SimplicialChainEquivalence e=SimplicialChainEquivalence.identity(points(70));
        assertEquals(e.homologyMap(z(0)),e.inverseHomologyMap(z(0))); assertEquals(e.cohomologyMap(z(0)),e.inverseCohomologyMap(z(0)));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> e.homologyMaps(z(0))).getMessage().contains("5000000"));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> e.cohomologyMaps(z(0))).getMessage().contains("5000000"));
    }
    @Test public void nativeScalarAndFlatOperationsReturnActualAlgebraWrappers() {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialChainEquivalence e=SimplicialChainEquivalence.fromMap(intervalMap(1,1));
        IAlgebraItem<SimplicialChainEquivalence> item=math.chainMaps.algebra().buildAlgebraItem(e.forward()).performAlgebraTransfer("ChainEquivalence.from-map");
        assertSame(math.chainEquivalences.algebra(),item.getAlgebra()); assertEquals(e,item.perform().getResult());
        assertSame(math.chainEquivalences.algebra(),math.chainEquivalences.inputs.buildAlgebraItem(e.data()).performAlgebraTransfer("ChainEquivalence.from-data").getAlgebra());
        assertSame(math.chainEquivalences.algebra(),item.performOneOperandOperation("inverse").getAlgebra());
        assertSame(math.chainMaps.algebra(),item.performAlgebraTransfer("forward").getAlgebra());
        assertSame(math.chainMapClasses.algebra(),item.performAlgebraTransfer("backward-class").getAlgebra());
        for(String name : Arrays.asList("on-chain","inverse-on-chain")) assertSame(math.relativeChains.algebra(),item.performLeftProjectionOperation(name,RelativeSimplicialChain.zero(edge(),z(0))).getAlgebra());
        for(String name : Arrays.asList("on-cochain","inverse-on-cochain")) assertSame(math.relativeCochains.algebra(),item.performLeftProjectionOperation(name,RelativeSimplicialCochain.zero(edge(),z(0))).getAlgebra());
        for(IAlgebraItem<?> h : item.performAlgebraFlatTransfer("homotopies")) assertSame(math.chainHomotopies.algebra(),h.getAlgebra());
        for(IAlgebraItem<?> m : item.performAlgebraFlatTransfer("maps")) assertSame(math.chainMaps.algebra(),m.getAlgebra());
        for(IAlgebraItem<?> m : item.performUnsafeFlatOperation("homology-maps",z(0))) assertSame(math.abelianHomomorphisms.algebra(),m.getAlgebra());
    }
    @Test public void constructedComposedTypedAndFlatNativeFlowsSerializeAndCollectRepeatedly() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialChainEquivalence e=SimplicialChainEquivalence.fromMap(intervalMap(1,1));
        IAlgebraFlow<SimplicialChainEquivalence> built=math.flow(math.chainMaps,Collections.singletonList(e.forward())).performAlgebraTransfer("ChainEquivalence.from-map");
        IAlgebraFlow<SimplicialChainEquivalence> composed=math.flow(math.chainEquivalences,Collections.singletonList(e)).performOneOperandOperation("inverse").performOperation("compose",e);
        IAlgebraFlow<SimplicialChainHomotopy> homotopies=composed.performFlatAlgebraTransfer("homotopies");
        IAlgebraFlow<AbelianGroupHomomorphism> homology=math.flow(math.chainEquivalences,Collections.singletonList(e)).performFlatAlgebraUnsafe("homology-maps",z(0));
        IAlgebraFlow<RelativeSimplicialChain> chains=math.flow(math.chainEquivalences,Collections.singletonList(e)).performLeftProjectionOperation("on-chain",RelativeSimplicialChain.zero(edge(),z(0)));
        for(IAlgebraFlow<?> flow : Arrays.asList(built,composed,homotopies,homology,chains)) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
            IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
            assertEquals(flow.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        }
        assertEquals(2,homotopies.collect().size()); assertEquals(2,homology.collect().size());
    }
}
