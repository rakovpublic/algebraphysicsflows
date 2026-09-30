package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.linear.*;
import mathematics.structures.AbelianGroupHomomorphism;
import mathematics.topology.*;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeSimplicialSubdivisionChainsTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> result=new ArrayList<>(); for(int[] f : facets) { List<Integer> vertices=new ArrayList<>(); for(int v : f) vertices.add(v); result.add(new FiniteSet<>(vertices)); } return new FiniteSimplicialComplex(result);
    }
    private static FiniteSimplicialComplex simplex(int count) { int[] labels=new int[count]; for(int i=0;i<count;i++) labels[i]=i; return complex(labels); }
    private static FiniteSimplicialComplex points(int count) { int[][] facets=new int[count][1]; for(int i=0;i<count;i++) facets[i][0]=i; return complex(facets); }
    private static FiniteSimplicialComplex circle() { return complex(new int[]{0,1},new int[]{0,2},new int[]{1,2}); }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static boolean closed(int mask,int labels) {
        for(int f=1;f<(1<<labels);f++) if((mask&(1<<(f-1)))!=0) for(int sub=(f-1)&f;sub>0;sub=(sub-1)&f) if((mask&(1<<(sub-1)))==0) return false; return true;
    }
    private static FiniteSimplicialComplex fromMask(int mask,int labels) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int f=1;f<(1<<labels);f++) if((mask&(1<<(f-1)))!=0) { List<Integer> v=new ArrayList<>(); for(int i=0;i<labels;i++) if((f&(1<<i))!=0) v.add(i); faces.add(new FiniteSet<>(v)); } return new FiniteSimplicialComplex(faces);
    }
    // Independent signed-permutation formula, rather than the implementation's recursive cones.
    private static void permutations(SimplicialSubdivision s,List<BigInteger> original,List<BigInteger> selected,boolean[] used,List<FiniteSet<BigInteger>> rows,BigInteger[][] expected,int column) {
        if(selected.size()==original.size()) {
            int sign=1; for(int i=0;i<selected.size();i++) for(int j=i+1;j<selected.size();j++) if(selected.get(i).compareTo(selected.get(j))>0) sign=-sign;
            List<BigInteger> chain=new ArrayList<>(); for(int n=1;n<=selected.size();n++) chain.add(s.faceVertex(new FiniteSet<>(selected.subList(0,n))));
            int row=rows.indexOf(new FiniteSet<>(chain)); if(row>=0) expected[row][column]=expected[row][column].add(z(sign)); return;
        }
        for(int i=0;i<used.length;i++) if(!used[i]) { used[i]=true; selected.add(original.get(i)); permutations(s,original,selected,used,rows,expected,column); selected.remove(selected.size()-1); used[i]=false; }
    }
    private static IntegerMatrix oracle(SimplicialSubdivision s,int degree) {
        List<FiniteSet<BigInteger>> rows=s.subdivided().simplexBasis(z(degree)),columns=s.original().simplexBasis(z(degree)); BigInteger[][] values=new BigInteger[rows.size()][columns.size()]; for(BigInteger[] row : values) Arrays.fill(row,BigInteger.ZERO);
        for(int c=0;c<columns.size();c++) { List<BigInteger> v=new ArrayList<>(columns.get(c).members()); Collections.sort(v); permutations(s,v,new ArrayList<>(),new boolean[v.size()],rows,values,c); }
        return new IntegerMatrix(rows.size(),columns.size(),values);
    }
    private static IntegerMatrix difference(IntegerMatrix a,IntegerMatrix b) { return a.add(b.scale(z(-1))); }
    private static void identities(SimplicialSubdivision s,int degree) {
        BigInteger n=z(degree); IntegerMatrix sub=s.chainMatrix(n),last=s.lastVertexMap().chainMatrix(n),p=s.chainHomotopyMatrix(n);
        assertEquals(IntegerMatrix.identity(sub.columns()),last.multiply(sub));
        IntegerMatrix previous=degree==0?IntegerMatrix.zero(0,0):s.chainMatrix(z(degree-1));
        assertEquals(s.subdivided().boundaryMatrix(n).multiply(sub),previous.multiply(s.original().boundaryMatrix(n)));
        IntegerMatrix pPrevious=degree==0?s.cochainHomotopyMatrix(z(0)).transpose():s.chainHomotopyMatrix(z(degree-1));
        assertEquals(difference(IntegerMatrix.identity(sub.rows()),sub.multiply(last)),s.subdivided().boundaryMatrix(n.add(BigInteger.ONE)).multiply(p).add(pPrevious.multiply(s.subdivided().boundaryMatrix(n))));
        assertEquals(sub.transpose(),s.cochainMatrix(n)); assertEquals(p.transpose(),s.cochainHomotopyMatrix(n.add(BigInteger.ONE)));
    }
    @Test public void all167FourLabelComplexesMatchPermutationOracleAndBothChainIdentities() {
        int count=0; for(int mask=0;mask<(1<<15);mask++) if(closed(mask,4)) {
            count++; SimplicialSubdivision s=SimplicialSubdivision.absolute(fromMask(mask,4));
            for(int k=0;k<=Math.max(0,s.original().ambient().dimension());k++) { assertEquals(oracle(s,k),s.chainMatrix(z(k))); identities(s,k); }
        } assertEquals(167,count);
    }
    private static IntegerMatrix restrict(IntegerMatrix matrix,List<FiniteSet<BigInteger>> allRows,List<FiniteSet<BigInteger>> allColumns,List<FiniteSet<BigInteger>> rows,List<FiniteSet<BigInteger>> columns) {
        BigInteger[][] result=new BigInteger[rows.size()][columns.size()]; for(int r=0;r<rows.size();r++) for(int c=0;c<columns.size();c++) result[r][c]=matrix.get(allRows.indexOf(rows.get(r)),allColumns.indexOf(columns.get(c))); return new IntegerMatrix(rows.size(),columns.size(),result);
    }
    @Test public void all148ThreeLabelPairsAgreeWithAbsoluteConstructionThenQuotientProjection() {
        int count=0; for(int x=0;x<128;x++) if(closed(x,3)) for(int a=0;a<128;a++) if(closed(a,3) && (x&a)==a) {
            count++; SimplicialSubdivision s=new SimplicialSubdivision(new RelativeSimplicialComplex(fromMask(x,3),fromMask(a,3))),abs=SimplicialSubdivision.absolute(s.original().ambient());
            for(int k=0;k<=2;k++) {
                BigInteger n=z(k); identities(s,k); assertEquals(oracle(s,k),s.chainMatrix(n));
                assertEquals(restrict(abs.chainHomotopyMatrix(n),abs.subdivided().simplexBasis(n.add(BigInteger.ONE)),abs.subdivided().simplexBasis(n),s.subdivided().simplexBasis(n.add(BigInteger.ONE)),s.subdivided().simplexBasis(n)),s.chainHomotopyMatrix(n));
            }
        } assertEquals(148,count);
    }
    @Test public void topSimplexCoefficientsHaveFactorialSupportAndTheEdgeHomotopyHasKnownSign() {
        int factorial=1; for(int vertices=1;vertices<=5;vertices++) {
            factorial*=vertices; SimplicialSubdivision s=SimplicialSubdivision.absolute(simplex(vertices)); IntegerMatrix matrix=s.chainMatrix(z(vertices-1));
            assertEquals(factorial,matrix.rows()); assertEquals(1,matrix.columns()); assertEquals(oracle(s,vertices-1),matrix);
            for(int r=0;r<matrix.rows();r++) assertEquals(BigInteger.ONE,matrix.get(r,0).abs());
        }
        SimplicialSubdivision edge=SimplicialSubdivision.absolute(simplex(2));
        assertEquals(new IntegerMatrix(new BigInteger[][]{{z(1)},{z(-1)}}),edge.chainMatrix(z(1)));
        assertEquals(new IntegerMatrix(new BigInteger[][]{{z(0),z(0),z(0)},{z(0),z(0),z(1)}}),edge.chainHomotopyMatrix(z(0)));
    }
    @Test public void all256TetrahedronVertexMapsGiveStrictSubdivisionNaturalityIncludingCollapsedFaces() {
        FiniteSimplicialComplex tetra=simplex(4); RelativeSimplicialComplex source=new RelativeSimplicialComplex(tetra,complex(new int[]{0})); SimplicialSubdivision s=new SimplicialSubdivision(source);
        for(int code=0;code<256;code++) {
            int[] image={code%4,(code/4)%4,(code/16)%4,(code/64)%4}; Map<BigInteger,BigInteger> labels=new TreeMap<>(); for(int i=0;i<4;i++) labels.put(z(i),z(image[i]));
            RelativeSimplicialComplex target=new RelativeSimplicialComplex(tetra,complex(new int[]{image[0]})); RelativeSimplicialMap f=new RelativeSimplicialMap(source,target,new FiniteSimplicialMap(tetra,tetra,labels)),sd=s.map(f); SimplicialSubdivision t=new SimplicialSubdivision(target);
            for(int k=0;k<=3;k++) assertEquals(sd.chainMatrix(z(k)).multiply(s.chainMatrix(z(k))),t.chainMatrix(z(k)).multiply(f.chainMatrix(z(k))));
        }
    }
    @Test public void all729TernaryCircleChainsAndCochainsSatisfyTypedHomotopyAndPairingIdentities() {
        SimplicialSubdivision s=SimplicialSubdivision.absolute(circle()); RelativeSimplicialMap last=s.lastVertexMap();
        for(int code=0;code<729;code++) {
            BigInteger[] v=new BigInteger[6]; int value=code; for(int i=0;i<6;i++) { v[i]=z(value%3-1); value/=3; }
            RelativeSimplicialChain c=new RelativeSimplicialChain(s.subdivided(),z(1),new IntegerVector(v));
            assertEquals(c.subtract(s.onChain(c.pushforward(last))),s.homotopyOnChain(c).boundary().add(s.homotopyOnChain(c.boundary())));
            RelativeSimplicialCochain q=new RelativeSimplicialCochain(s.subdivided(),z(1),new IntegerVector(v));
            assertEquals(q.subtract(s.onCochain(q).pullback(last)),s.homotopyOnCochain(q).coboundary().add(s.homotopyOnCochain(q.coboundary())));
            RelativeSimplicialChain original=new RelativeSimplicialChain(s.original(),z(1),new IntegerVector(z(2),z(-3),z(5)));
            assertEquals(s.onChain(original).evaluate(q),original.evaluate(s.onCochain(q)));
            assertEquals(q.coboundary().coordinates().dimension(),0);
        }
    }
    @Test public void higherDimensionalCyclesAndCocyclesHaveTypedFillingsRetainingRelativePairs() {
        for(boolean relative : Arrays.asList(false,true)) {
            FiniteSimplicialComplex tetra=simplex(4); SimplicialSubdivision s=new SimplicialSubdivision(new RelativeSimplicialComplex(tetra,relative?complex(new int[]{0,1},new int[]{1,2}):complex()));
            RelativeSimplicialMap last=s.lastVertexMap(); boolean nonzero=false;
            for(RelativeSimplicialChain face : RelativeSimplicialChain.basisChains(s.subdivided(),z(2))) {
                RelativeSimplicialChain cycle=face.boundary(),filling=s.homotopyOnChain(cycle); nonzero|=!filling.isZero();
                assertEquals(cycle.subtract(s.onChain(cycle.pushforward(last))),filling.boundary()); assertEquals(s.subdivided(),filling.pair());
            }
            assertTrue(nonzero);
            for(RelativeSimplicialCochain vertex : RelativeSimplicialCochain.basisCochains(s.subdivided(),z(0))) {
                RelativeSimplicialCochain q=vertex.coboundary(); assertEquals(q.subtract(s.onCochain(q).pullback(last)),s.homotopyOnCochain(q).coboundary());
            }
        }
    }
    private static void inducedInverse(SimplicialSubdivision s,int degree) {
        BigInteger n=z(degree); AbelianGroupHomomorphism forward=s.original().homology(n).inducedMap(s.subdivided().homology(n),s.chainMatrix(n));
        assertEquals(s.inverseHomologyMap(n),forward);
        AbelianGroupHomomorphism pullback=RelativeSimplicialCochain.cohomology(s.subdivided(),n).inducedMap(RelativeSimplicialCochain.cohomology(s.original(),n),s.cochainMatrix(n));
        assertEquals(s.inverseCohomologyMap(n),pullback);
    }
    @Test public void inducedSubdivisionMapsEqualTheExistingIntegralInversesIncludingTorsion() {
        for(int dimension=1;dimension<=3;dimension++) { FiniteSimplicialComplex disk=simplex(dimension+1); inducedInverse(new SimplicialSubdivision(new RelativeSimplicialComplex(disk,disk.skeleton(dimension-1))),dimension); }
        FiniteSimplicialComplex rp2=complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        SimplicialSubdivision s=SimplicialSubdivision.absolute(rp2); for(int k=0;k<=2;k++) inducedInverse(s,k);
        assertEquals(Collections.singletonList(z(2)),s.onAbsoluteChain(new SimplicialChain(rp2,z(1),s.original().homology(z(1)).generators().get(0))).classOf().group().type().invariantFactors());
    }
    @Test public void repeatedSubdivisionRetainsCyclesAndTheChosenHomotopyDependsOnVertexOrder() {
        SimplicialSubdivision s=SimplicialSubdivision.absolute(circle()),t=new SimplicialSubdivision(s.subdivided());
        SimplicialChain c=new SimplicialChain(circle(),z(1),new IntegerVector(z(1),z(-1),z(1))),twice=t.onAbsoluteChain(s.onAbsoluteChain(c));
        assertEquals(12,twice.coordinates().dimension()); assertTrue(twice.isCycle());
        assertEquals(c,twice.pushforward(t.lastVertexMap().ambientMap()).pushforward(s.lastVertexMap().ambientMap()));
        SimplicialSubdivision edge=SimplicialSubdivision.absolute(simplex(2)); Map<BigInteger,BigInteger> swap=new TreeMap<>(); swap.put(z(0),z(1)); swap.put(z(1),z(0));
        RelativeSimplicialMap reflection=RelativeSimplicialMap.absolute(new FiniteSimplicialMap(simplex(2),simplex(2),swap)),sd=edge.map(reflection);
        assertNotEquals(sd.chainMatrix(z(1)).multiply(edge.chainHomotopyMatrix(z(0))),edge.chainHomotopyMatrix(z(0)).multiply(sd.chainMatrix(z(0))));
    }
    @Test public void wrongFullPairsNegativeMatrixDegreesAndAbsoluteRelativeMixingAreUndefined() {
        SimplicialSubdivision s=SimplicialSubdivision.absolute(simplex(2)); RelativeSimplicialComplex wrong=RelativeSimplicialComplex.diagonal(simplex(2)),wrongSd=RelativeSimplicialComplex.diagonal(s.subdivided().ambient());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> s.onChain(RelativeSimplicialChain.zero(wrong,z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> s.onCochain(RelativeSimplicialCochain.zero(wrongSd,z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> s.homotopyOnChain(RelativeSimplicialChain.zero(wrongSd,z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> s.homotopyOnCochain(RelativeSimplicialCochain.zero(wrongSd,z(1))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> s.onChain(RelativeSimplicialChain.zero(s.subdivided(),z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> s.onCochain(RelativeSimplicialCochain.zero(s.original(),z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> s.homotopyOnCochain(RelativeSimplicialCochain.zero(s.subdivided(),z(0))));
        for(Runnable op : Arrays.<Runnable>asList(() -> s.chainMatrix(z(-1)),() -> s.cochainMatrix(z(-1)),() -> s.chainHomotopyMatrix(z(-1)),() -> s.cochainHomotopyMatrix(z(-1)))) failure(MathFailure.Kind.OPERATION_UNDEFINED,op);
        SimplicialSubdivision relative=new SimplicialSubdivision(wrong);
        for(Runnable op : Arrays.<Runnable>asList(() -> relative.onAbsoluteChain(SimplicialChain.zero(simplex(2),z(0))),() -> relative.onAbsoluteCochain(SimplicialCochain.zero(relative.subdivided().ambient(),z(1))),() -> relative.homotopyOnAbsoluteChain(SimplicialChain.zero(relative.subdivided().ambient(),z(0))),() -> relative.homotopyOnAbsoluteCochain(SimplicialCochain.zero(relative.subdivided().ambient(),z(1))))) failure(MathFailure.Kind.OPERATION_UNDEFINED,op);
    }
    @Test public void emptyHugeAndNegativeTypedDegreesHaveCorrectShapesAndFlatListsAreImmutable() {
        SimplicialSubdivision empty=SimplicialSubdivision.absolute(complex()),edge=SimplicialSubdivision.absolute(simplex(2)); BigInteger huge=BigInteger.TEN.pow(100);
        for(SimplicialSubdivision s : Arrays.asList(empty,edge)) {
            assertEquals(IntegerMatrix.zero(0,0),s.chainMatrix(huge)); assertEquals(IntegerMatrix.zero(0,0),s.chainHomotopyMatrix(huge)); assertEquals(IntegerMatrix.zero(0,0),s.cochainHomotopyMatrix(huge));
            assertEquals(RelativeSimplicialChain.zero(s.subdivided(),z(-2)),s.onChain(RelativeSimplicialChain.zero(s.original(),z(-2))));
            assertEquals(RelativeSimplicialChain.zero(s.subdivided(),z(0)),s.homotopyOnChain(RelativeSimplicialChain.zero(s.subdivided(),z(-1))));
            assertEquals(IntegerMatrix.zero(0,s.subdivided().simplexCount(z(0))),s.cochainHomotopyMatrix(z(0)));
            for(List<IntegerMatrix> list : Arrays.asList(s.chainMatrices(),s.cochainMatrices(),s.chainHomotopyMatrices(),s.cochainHomotopyMatrices())) assertThrows(UnsupportedOperationException.class,list::clear);
        }
        assertTrue(empty.chainMatrices().isEmpty()); assertEquals(Collections.singletonList(IntegerMatrix.zero(0,0)),empty.cochainHomotopyMatrices());
        assertEquals(Arrays.asList(edge.chainMatrix(z(0)),edge.chainMatrix(z(1))),edge.chainMatrices());
        assertEquals(Arrays.asList(edge.cochainHomotopyMatrix(z(0)),edge.cochainHomotopyMatrix(z(1)),edge.cochainHomotopyMatrix(z(2))),edge.cochainHomotopyMatrices());
    }
    @Test public void quotientBasesAreFilteredBeforeLimitsAndHighDimensionalTopMatricesRemainAvailable() {
        SimplicialSubdivision filtered=new SimplicialSubdivision(new RelativeSimplicialComplex(points(300),points(299))); assertEquals(IntegerMatrix.identity(1),filtered.chainMatrix(z(0))); assertEquals(IntegerMatrix.zero(0,1),filtered.chainHomotopyMatrix(z(0)));
        SimplicialSubdivision diagonal=new SimplicialSubdivision(RelativeSimplicialComplex.diagonal(simplex(5))); for(int k=0;k<=4;k++) identities(diagonal,k);
        SimplicialSubdivision large=SimplicialSubdivision.absolute(simplex(5)); assertEquals(120,large.chainMatrix(z(4)).rows());
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> large.chainMatrix(z(2))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,large::chainMatrices);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> large.chainHomotopyMatrix(z(1))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,large::cochainHomotopyMatrices);
        SimplicialSubdivision many=SimplicialSubdivision.absolute(points(4096)); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> many.chainMatrix(z(0)));
    }
    @Test public void nativeActionsReturnAllFourActualSecondOperandAlgebras() {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialSubdivision s=SimplicialSubdivision.absolute(simplex(2)); IAlgebraItem<SimplicialSubdivision> item=math.subdivisions.algebra().buildAlgebraItem(s);
        assertSame(math.relativeChains.algebra(),item.performLeftProjectionOperation("on-chain",RelativeSimplicialChain.zero(s.original(),z(0))).getAlgebra());
        assertSame(math.relativeCochains.algebra(),item.performLeftProjectionOperation("on-cochain",RelativeSimplicialCochain.zero(s.subdivided(),z(0))).getAlgebra());
        assertSame(math.simplicialChains.algebra(),item.performLeftProjectionOperation("on-absolute-chain",SimplicialChain.zero(s.original().ambient(),z(0))).getAlgebra());
        assertSame(math.cochains.algebra(),item.performLeftProjectionOperation("on-absolute-cochain",SimplicialCochain.zero(s.subdivided().ambient(),z(0))).getAlgebra());
        assertSame(math.relativeChains.algebra(),item.performLeftProjectionOperation("homotopy-on-chain",RelativeSimplicialChain.zero(s.subdivided(),z(0))).getAlgebra());
        assertSame(math.relativeCochains.algebra(),item.performLeftProjectionOperation("homotopy-on-cochain",RelativeSimplicialCochain.zero(s.subdivided(),z(1))).getAlgebra());
        assertSame(math.simplicialChains.algebra(),item.performLeftProjectionOperation("homotopy-on-absolute-chain",SimplicialChain.zero(s.subdivided().ambient(),z(0))).getAlgebra());
        assertSame(math.cochains.algebra(),item.performLeftProjectionOperation("homotopy-on-absolute-cochain",SimplicialCochain.zero(s.subdivided().ambient(),z(1))).getAlgebra());
    }
    @Test public void scalarAndFlatNativeFlowsSerializeAndCollectRepeatedly() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialChain cycle=new SimplicialChain(circle(),z(1),new IntegerVector(z(1),z(-1),z(1)));
        IAlgebraFlow<Boolean> scalar=math.flow(math.complexes,Collections.singletonList(circle())).<SimplicialSubdivision>performAlgebraTransfer("SimplicialSubdivision.from-complex")
                .performLeftProjectionOperation("on-absolute-chain",cycle).<Boolean>performAlgebraTransfer("is-cycle");
        IAlgebraFlow<BigInteger> flat=math.flow(math.complexes,Collections.singletonList(simplex(2))).<SimplicialSubdivision>performAlgebraTransfer("SimplicialSubdivision.from-complex")
                .<IntegerMatrix>performFlatAlgebraTransfer("chain-matrices").<BigInteger>performAlgebraTransfer("row-count");
        for(IAlgebraFlow<?> flow : Arrays.asList(scalar,flat)) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
            IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
            assertEquals(flow.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        }
        assertEquals(Collections.singletonList("true"),scalar.collect()); assertEquals(Arrays.asList("3","2"),flat.collect());
    }
}
