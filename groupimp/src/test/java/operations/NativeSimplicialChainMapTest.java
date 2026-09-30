package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import algebraflow.imp.AlgebraFlow;
import algebraflow.imp.ListAlgebraInput;
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

public class NativeSimplicialChainMapTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int v : facet) labels.add(v); faces.add(new FiniteSet<>(labels)); } return new FiniteSimplicialComplex(faces);
    }
    private static FiniteSimplicialComplex points(int n) { int[][] f=new int[n][1]; for(int i=0;i<n;i++) f[i][0]=i; return complex(f); }
    private static RelativeSimplicialComplex abs(FiniteSimplicialComplex c) { return RelativeSimplicialComplex.absolute(c); }
    private static IntegerMatrix matrix(long[]... entries) {
        BigInteger[][] values=new BigInteger[entries.length][]; for(int r=0;r<entries.length;r++) { values[r]=new BigInteger[entries[r].length]; for(int c=0;c<entries[r].length;c++) values[r][c]=z(entries[r][c]); } return new IntegerMatrix(values);
    }
    private static void failure(MathFailure.Kind kind,Runnable body) { assertEquals(kind,assertThrows(MathFailure.class,body::run).kind()); }
    private static RelativeSimplicialComplex circle() { return abs(complex(new int[]{0,1},new int[]{0,2},new int[]{1,2})); }
    // Fix vertices, add (degree-1) times the oriented cycle to the first edge.
    private static SimplicialChainMap degreeMap(int degree) {
        return new SimplicialChainMap(circle(),circle(),Arrays.asList(IntegerMatrix.identity(3),matrix(new long[]{degree,0,0},new long[]{1-degree,1,0},new long[]{degree-1,0,1})));
    }
    @Test public void allTernaryIntervalDegreeMatricesAreCheckedAgainstIndependentEndpointEquations() {
        RelativeSimplicialComplex edge=abs(complex(new int[]{0,1})); int accepted=0;
        for(int a=-1;a<=1;a++) for(int b=-1;b<=1;b++) for(int c=-1;c<=1;c++) for(int d=-1;d<=1;d++) for(int e=-1;e<=1;e++) {
            List<IntegerMatrix> values=Arrays.asList(matrix(new long[]{a,b},new long[]{c,d}),matrix(new long[]{e}));
            boolean valid=b-a==-e && d-c==e;
            if(valid) { accepted++; SimplicialChainMap map=new SimplicialChainMap(edge,edge,values); assertEquals(values,map.chainMatrices()); }
            else failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialChainMap(edge,edge,values));
        }
        assertEquals(17,accepted);
    }
    @Test public void allRelativeIntervalMatricesUseQuotientIncidencesAndBothEndpointShapes() {
        FiniteSimplicialComplex edge=complex(new int[]{0,1}); int[] masks={0,1,2,3,7};
        for(int a : masks) for(int b : masks) {
            RelativeSimplicialComplex source=new RelativeSimplicialComplex(edge,intervalSubcomplex(a)),target=new RelativeSimplicialComplex(edge,intervalSubcomplex(b));
            int s0=2-Integer.bitCount(a&3),t0=2-Integer.bitCount(b&3),s1=(a&4)==0?1:0,t1=(b&4)==0?1:0;
            int choices=(int)Math.pow(3,s0*t0+s1*t1);
            for(int code=0;code<choices;code++) {
                int rest=code; BigInteger[][] f0=new BigInteger[t0][s0],f1=new BigInteger[t1][s1];
                for(int r=0;r<t0;r++) for(int c=0;c<s0;c++) { f0[r][c]=z(rest%3-1); rest/=3; }
                for(int r=0;r<t1;r++) for(int c=0;c<s1;c++) { f1[r][c]=z(rest%3-1); rest/=3; }
                boolean valid=true; int row=0;
                for(int v=0;v<2;v++) if((b&(1<<v))==0) {
                    if(s1>0) {
                        long lhs=t1==0?0:(v==0?-1:1)*f1[0][0].longValue(),rhs=0; int col=0;
                        for(int w=0;w<2;w++) if((a&(1<<w))==0) rhs+=(w==0?-1:1)*f0[row][col++].longValue();
                        valid&=lhs==rhs;
                    }
                    row++;
                }
                List<IntegerMatrix> values=Arrays.asList(new IntegerMatrix(t0,s0,f0),new IntegerMatrix(t1,s1,f1));
                if(valid) assertEquals(values,new SimplicialChainMap(source,target,values).chainMatrices());
                else failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialChainMap(source,target,values));
            }
        }
    }
    private static FiniteSimplicialComplex intervalSubcomplex(int mask) {
        if(mask==7) return complex(new int[]{0,1}); if(mask==3) return points(2); if(mask==1) return points(1); if(mask==2) return complex(new int[]{1}); return complex();
    }
    @Test public void all625TwoByTwoPointMapsHaveIntegralInversesExactlyWhenTheDeterminantIsAUnit() {
        RelativeSimplicialComplex pair=abs(points(2));
        for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) for(int c=-2;c<=2;c++) for(int d=-2;d<=2;d++) {
            int determinant=a*d-b*c; SimplicialChainMap f=new SimplicialChainMap(pair,pair,Collections.singletonList(matrix(new long[]{a,b},new long[]{c,d})));
            assertEquals(Math.abs(determinant)==1,f.isIsomorphism());
            if(Math.abs(determinant)==1) {
                assertEquals(matrix(new long[]{d/determinant,-b/determinant},new long[]{-c/determinant,a/determinant}),f.inverse().chainMatrix(z(0)));
                assertTrue(f.compose(f.inverse()).isIdentity()); assertTrue(f.inverse().compose(f).isIdentity());
            } else failure(MathFailure.Kind.OPERATION_UNDEFINED,f::inverse);
        }
    }
    @Test public void circleMapsOfArbitraryDegreeAreAdditiveFunctorialAndNeedNotBeVertexMaps() {
        RelativeSimplicialChain cycle=new RelativeSimplicialChain(circle(),z(1),new IntegerVector(z(1),z(-1),z(1)));
        RelativeSimplicialCochain cocycle=new RelativeSimplicialCochain(circle(),z(1),new IntegerVector(z(1),z(0),z(0)));
        for(int n=-4;n<=4;n++) {
            SimplicialChainMap f=degreeMap(n); assertEquals(cycle.scale(z(n)),f.onChain(cycle));
            assertEquals(cycle.classOf().scale(z(n)),f.homologyMap(z(1)).apply(cycle.classOf()));
            assertTrue(f.onCochain(cocycle).cohomologous(cocycle.scale(z(n))));
            assertEquals(AbelianGroupHomomorphism.scaling(f.cohomologyMap(z(1)).source(),z(n)),f.cohomologyMap(z(1)));
            assertEquals(Math.abs(n)==1,f.isIsomorphism());
            for(int m=-4;m<=4;m++) {
                SimplicialChainMap g=degreeMap(m); assertEquals(degreeMap(n*m),f.compose(g));
                assertEquals(f.homologyMap(z(1)).compose(g.homologyMap(z(1))),f.compose(g).homologyMap(z(1)));
                assertEquals(g.cohomologyMap(z(1)).compose(f.cohomologyMap(z(1))),f.compose(g).cohomologyMap(z(1)));
                assertEquals(f.homologyMap(z(1)).add(g.homologyMap(z(1))),f.add(g).homologyMap(z(1)));
                assertEquals(f.cohomologyMap(z(1)).add(g.cohomologyMap(z(1))),f.add(g).cohomologyMap(z(1)));
            }
        }
        assertEquals(matrix(new long[]{3,0,0},new long[]{-2,1,0},new long[]{2,0,1}),degreeMap(3).chainMatrix(z(1)));
    }
    @Test public void scalarMapsNeedNotPreserveCupProductsOrTheAugmentation() {
        RelativeSimplicialComplex point=abs(points(1)); SimplicialChainMap twice=SimplicialChainMap.identity(point).scale(z(2));
        RelativeSimplicialCochain unit=new RelativeSimplicialCochain(point,z(0),new IntegerVector(z(1)));
        assertEquals(new IntegerVector(z(2)),twice.onCochain(unit.cup(unit)).coordinates());
        assertEquals(new IntegerVector(z(4)),twice.onCochain(unit).cup(twice.onCochain(unit)).coordinates());
        assertFalse(twice.isIsomorphism()); assertTrue(twice.subtract(twice).isZero());
        BigInteger huge=BigInteger.ONE.shiftLeft(1024); assertEquals(huge,SimplicialChainMap.identity(point).scale(huge).chainMatrix(z(0)).get(0,0));
    }
    @Test public void noncommutingMapsReverseCohomologyCompositionAndRespectBilinearity() {
        RelativeSimplicialComplex pair=abs(points(2));
        SimplicialChainMap f=new SimplicialChainMap(pair,pair,Collections.singletonList(matrix(new long[]{1,2},new long[]{0,1}))),
                g=new SimplicialChainMap(pair,pair,Collections.singletonList(matrix(new long[]{1,0},new long[]{3,1}))),id=SimplicialChainMap.identity(pair);
        assertEquals(matrix(new long[]{7,2},new long[]{3,1}),f.compose(g).chainMatrix(z(0))); assertFalse(f.compose(g).equals(g.compose(f)));
        assertEquals(g.cohomologyMap(z(0)).compose(f.cohomologyMap(z(0))),f.compose(g).cohomologyMap(z(0)));
        assertFalse(f.cohomologyMap(z(0)).compose(g.cohomologyMap(z(0))).equals(f.compose(g).cohomologyMap(z(0))));
        assertEquals(f.add(g).compose(id.add(g)),f.compose(id).add(f.compose(g)).add(g.compose(id)).add(g.compose(g)));
        assertEquals(g.inverse().compose(f.inverse()),f.compose(g).inverse());
        RelativeSimplicialCochain a=new RelativeSimplicialCochain(pair,z(0),new IntegerVector(z(4),z(-2)));
        assertEquals(g.onCochain(f.onCochain(a)),f.compose(g).onCochain(a));
    }
    @Test public void suppliedListsAreDefensiveAndInvalidShapesAndMissingTopDifferentialsAreRejected() {
        RelativeSimplicialComplex pair=circle(); List<IntegerMatrix> input=new ArrayList<>(Arrays.asList(IntegerMatrix.identity(3),IntegerMatrix.identity(3)));
        SimplicialChainMap.Data data=new SimplicialChainMap.Data(pair,pair,input); input.clear(); SimplicialChainMap f=new SimplicialChainMap(data);
        assertTrue(f.isIdentity()); assertEquals(data,f.data()); assertEquals(data.hashCode(),f.data().hashCode()); assertEquals(f.hashCode(),new SimplicialChainMap(f.data()).hashCode());
        assertThrows(UnsupportedOperationException.class,data.matrices()::clear); assertThrows(UnsupportedOperationException.class,f.chainMatrices()::clear); assertThrows(UnsupportedOperationException.class,f.cochainMatrices()::clear);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialChainMap(pair,pair,Collections.singletonList(IntegerMatrix.identity(3))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialChainMap(pair,pair,Arrays.asList(IntegerMatrix.identity(3),IntegerMatrix.identity(2))));
        RelativeSimplicialComplex disk=abs(complex(new int[]{0,1,2}));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialChainMap(disk,pair,Arrays.asList(IntegerMatrix.identity(3),IntegerMatrix.identity(3),IntegerMatrix.zero(0,1))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialChainMap(pair,pair,Arrays.asList(IntegerMatrix.identity(3),IntegerMatrix.identity(3),IntegerMatrix.zero(0,0))));
    }
    @Test public void chainAndCochainActionsCommuteWithDifferentialsFor729IndependentCoordinates() {
        SimplicialChainMap f=degreeMap(-3);
        for(int code=0;code<729;code++) {
            int rest=code; BigInteger[] x=new BigInteger[3],a=new BigInteger[3];
            for(int i=0;i<3;i++) { x[i]=z(rest%3-1); rest/=3; a[i]=z(rest%3-1); rest/=3; }
            RelativeSimplicialChain chain=new RelativeSimplicialChain(circle(),z(1),new IntegerVector(x));
            RelativeSimplicialCochain cochain=new RelativeSimplicialCochain(circle(),z(0),new IntegerVector(a));
            assertEquals(f.onChain(chain.boundary()),f.onChain(chain).boundary()); assertEquals(f.onCochain(cochain.coboundary()),f.onCochain(cochain).coboundary());
            assertEquals(new IntegerVector(x[0].multiply(z(-3)),x[1].add(x[0].multiply(z(4))),x[2].subtract(x[0].multiply(z(4)))),f.onChain(chain).coordinates());
            RelativeSimplicialCochain edgeCochain=new RelativeSimplicialCochain(circle(),z(1),new IntegerVector(a));
            assertEquals(edgeCochain.evaluate(f.onChain(chain).coordinates()),f.onCochain(edgeCochain).evaluate(chain.coordinates()));
        }
    }
    @Test public void strictFullPairChecksRejectEquallySizedButDifferentContexts() {
        RelativeSimplicialComplex a=abs(points(1)),b=abs(complex(new int[]{9})); SimplicialChainMap f=SimplicialChainMap.identity(a),g=SimplicialChainMap.identity(b);
        assertFalse(f.equals(g)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.add(g)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.subtract(g)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.compose(g));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.onChain(RelativeSimplicialChain.zero(b,z(0)))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.onCochain(RelativeSimplicialCochain.zero(b,z(0))));
        FiniteSimplicialComplex edge=complex(new int[]{0,1}); RelativeSimplicialComplex left=new RelativeSimplicialComplex(edge,points(1)),right=new RelativeSimplicialComplex(edge,complex(new int[]{1}));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialChainMap.identity(left).compose(SimplicialChainMap.identity(right)));
        SimplicialChainMap projection=SimplicialChainMap.fromSimplicial(RelativeSimplicialMap.inclusion(abs(edge),left));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> projection.onAbsoluteChain(SimplicialChain.zero(edge,z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> projection.onAbsoluteCochain(SimplicialCochain.zero(edge,z(0))));
        SimplicialChainMap relabelled=new SimplicialChainMap(a,b,Collections.singletonList(IntegerMatrix.identity(1))); assertTrue(relabelled.isIsomorphism()); assertFalse(relabelled.isIdentity());
    }
    @Test public void emptyGroupsAndHugeDegreesRetainRectangularShapesAndZeroChainConventions() {
        RelativeSimplicialComplex empty=abs(complex()),point=abs(points(1)); SimplicialChainMap id=SimplicialChainMap.identity(empty);
        assertTrue(id.isZero()); assertTrue(id.isIdentity()); assertTrue(id.isIsomorphism()); assertEquals(id,id.inverse()); assertTrue(id.homologyMaps().isEmpty());
        SimplicialChainMap toEmpty=SimplicialChainMap.zero(point,empty),fromEmpty=SimplicialChainMap.zero(empty,point);
        assertEquals(IntegerMatrix.zero(0,1),toEmpty.chainMatrix(z(0))); assertEquals(IntegerMatrix.zero(1,0),fromEmpty.chainMatrix(z(0))); assertTrue(toEmpty.compose(fromEmpty).isIdentity()); assertFalse(fromEmpty.compose(toEmpty).isIdentity());
        BigInteger huge=BigInteger.ONE.shiftLeft(80); assertEquals(IntegerMatrix.zero(0,0),toEmpty.chainMatrix(huge)); assertTrue(toEmpty.homologyMap(huge).source().type().isTrivial());
        assertEquals(RelativeSimplicialChain.zero(empty,z(-8)),toEmpty.onChain(RelativeSimplicialChain.zero(point,z(-8))));
        assertEquals(SimplicialChain.zero(point.ambient(),z(-8)),fromEmpty.onAbsoluteChain(SimplicialChain.zero(empty.ambient(),z(-8))));
        assertEquals(SimplicialCochain.zero(empty.ambient(),huge),fromEmpty.onAbsoluteCochain(SimplicialCochain.zero(point.ambient(),huge)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> id.chainMatrix(z(-1))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> id.homologyMap(z(-1))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> id.cohomologyMap(z(-1)));
    }
    @Test public void simplicialConversionPreservesAll729TriangleCompositionsAndContravariantActions() {
        FiniteSimplicialComplex triangle=complex(new int[]{0,1,2}); List<RelativeSimplicialMap> maps=new ArrayList<>();
        for(int code=0;code<27;code++) { int rest=code; Map<BigInteger,BigInteger> vertices=new TreeMap<>(); for(int v=0;v<3;v++) { vertices.put(z(v),z(rest%3)); rest/=3; } maps.add(RelativeSimplicialMap.absolute(new FiniteSimplicialMap(triangle,triangle,vertices))); }
        for(RelativeSimplicialMap f : maps) {
            SimplicialChainMap chain=SimplicialChainMap.fromSimplicial(f); assertEquals(chain,SimplicialChainMap.fromAbsoluteSimplicial(f.ambientMap()));
            assertEquals(f.chainMatrices(),chain.chainMatrices()); assertEquals(f.homologyMaps(),chain.homologyMaps());
            for(RelativeSimplicialMap g : maps) assertEquals(SimplicialChainMap.fromSimplicial(f.compose(g)),chain.compose(SimplicialChainMap.fromSimplicial(g)));
        }
    }
    @Test public void collapseAndSubdivisionConversionsRetainStrictSplittingsButNotStrictInverses() {
        RelativeSimplicialComplex triangle=abs(complex(new int[]{0,1,2})); SimplicialCollapse step=new SimplicialCollapse(triangle,FiniteSet.of(z(0),z(1)));
        SimplicialChainMap r=SimplicialChainMap.fromCollapse(step),i=SimplicialChainMap.fromSimplicial(step.inclusion());
        assertTrue(r.compose(i).isIdentity()); assertFalse(i.compose(r).isIdentity()); assertFalse(r.isIsomorphism()); failure(MathFailure.Kind.OPERATION_UNDEFINED,r::inverse);
        assertEquals(step.chainMatrices(),r.chainMatrices()); assertEquals(SimplicialChainMap.fromCollapseSequence(SimplicialCollapseSequence.fromCollapse(step)),r);
        SimplicialCollapseSequence sequence=SimplicialCollapseSequence.reduce(triangle); SimplicialChainMap composite=SimplicialChainMap.fromCollapseSequence(sequence);
        assertEquals(sequence.chainMatrices(),composite.chainMatrices()); for(int k=0;k<3;k++) assertEquals(sequence.homologyMap(z(k)),composite.homologyMap(z(k)));
        SimplicialSubdivision subdivision=new SimplicialSubdivision(triangle); SimplicialChainMap s=SimplicialChainMap.fromSubdivision(subdivision),last=SimplicialChainMap.fromSimplicial(subdivision.lastVertexMap());
        assertEquals(subdivision.chainMatrices(),s.chainMatrices()); assertTrue(last.compose(s).isIdentity()); assertFalse(s.compose(last).isIdentity());
        for(int k=0;k<3;k++) { assertEquals(subdivision.inverseHomologyMap(z(k)),s.homologyMap(z(k))); assertEquals(subdivision.inverseCohomologyMap(z(k)),s.cohomologyMap(z(k))); }
    }
    @Test public void relativeDiskMapsAndProjectivePlaneTorsionRetainIntegralInformation() {
        FiniteSimplicialComplex disk=complex(new int[]{0,1,2}); RelativeSimplicialComplex sphere=new RelativeSimplicialComplex(disk,disk.skeleton(1));
        SimplicialChainMap times=SimplicialChainMap.identity(sphere).scale(z(7)); assertEquals(matrix(new long[]{7}),times.chainMatrix(z(2))); assertEquals(AbelianGroupHomomorphism.scaling(times.homologyMap(z(2)).source(),z(7)),times.homologyMap(z(2)));
        FiniteSimplicialComplex rp2=complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        SimplicialChainMap id=SimplicialChainMap.identity(abs(rp2));
        assertEquals(Collections.singletonList(z(2)),id.homologyMap(z(1)).source().type().invariantFactors()); assertEquals(Collections.singletonList(z(2)),id.cohomologyMap(z(2)).source().type().invariantFactors());
        for(int n=-3;n<=3;n++) {
            SimplicialChainMap map=id.scale(z(n));
            assertEquals(AbelianGroupHomomorphism.scaling(map.homologyMap(z(1)).source(),z(n)),map.homologyMap(z(1)));
            assertEquals(AbelianGroupHomomorphism.scaling(map.cohomologyMap(z(2)).source(),z(n)),map.cohomologyMap(z(2)));
            assertEquals((n&1)==0,map.homologyMap(z(1)).isZero()); assertEquals((n&1)==0,map.cohomologyMap(z(2)).isZero());
        }
        RelativeSimplicialComplex acyclic=new RelativeSimplicialComplex(complex(new int[]{0,1}),points(1)); SimplicialChainMap zero=SimplicialChainMap.zero(acyclic,acyclic);
        assertFalse(zero.isIsomorphism()); for(AbelianGroupHomomorphism map : zero.homologyMaps()) assertTrue(map.isIsomorphism());
    }
    @Test public void quotientFilteringPrecedesTheBasisLimitEvenFor4096AmbientVertices() {
        FiniteSimplicialComplex big=points(4096); RelativeSimplicialComplex diagonal=RelativeSimplicialComplex.diagonal(big);
        assertTrue(SimplicialChainMap.identity(diagonal).isIdentity()); assertEquals(IntegerMatrix.zero(0,0),SimplicialChainMap.identity(diagonal).chainMatrix(z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialChainMap.identity(abs(points(257))));
        RelativeSimplicialComplex filtered=new RelativeSimplicialComplex(big,points(4095)); assertEquals(IntegerMatrix.identity(1),SimplicialChainMap.identity(filtered).chainMatrix(z(0)));
    }
    private static RelativeSimplicialComplex separatedRelativeCells(int count) {
        List<FiniteSet<Integer>> ambient=new ArrayList<>(),subcomplex=new ArrayList<>();
        for(int k=0;k<count;k++) {
            int v=5*k; ambient.add(FiniteSet.of(v,v+1)); ambient.add(FiniteSet.of(v+2,v+3,v+4));
            subcomplex.add(FiniteSet.of(v)); subcomplex.add(FiniteSet.of(v+1)); subcomplex.add(FiniteSet.of(v+2,v+3)); subcomplex.add(FiniteSet.of(v+2,v+4)); subcomplex.add(FiniteSet.of(v+3,v+4));
        }
        return new RelativeSimplicialComplex(new FiniteSimplicialComplex(ambient),new FiniteSimplicialComplex(subcomplex));
    }
    @Test public void compoundOperationsShareBudgetsAcrossAllDegreesAndValidation() {
        SimplicialChainMap f=SimplicialChainMap.identity(separatedRelativeCells(110)); assertTrue(f.isIsomorphism());
        assertEquals(IntegerMatrix.identity(110),f.chainMatrix(z(1)).multiply(f.chainMatrix(z(1)))); assertEquals(IntegerMatrix.identity(110),f.chainMatrix(z(2)).multiply(f.chainMatrix(z(2))));
        for(Runnable operation : Arrays.<Runnable>asList(() -> f.compose(f),f::inverse)) {
            MathFailure exhausted=assertThrows(MathFailure.class,operation::run); assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,exhausted.kind()); assertTrue(exhausted.getMessage().contains("5000000"));
        }
    }
    @Test public void wholeHomologyAndCohomologyListsFailAtomicallyEvenWhenEachDegreeFits() {
        SimplicialChainMap f=SimplicialChainMap.identity(separatedRelativeCells(70));
        for(int k=0;k<3;k++) { assertTrue(f.homologyMap(z(k)).isIsomorphism()); assertTrue(f.cohomologyMap(z(k)).isIsomorphism()); }
        for(Runnable operation : Arrays.<Runnable>asList(f::homologyMaps,f::cohomologyMaps)) {
            MathFailure exhausted=assertThrows(MathFailure.class,operation::run); assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,exhausted.kind()); assertTrue(exhausted.getMessage().contains("5000000"));
        }
    }
    @Test public void nativeConstructionSecondOperandWrappersAndFlatFlowsSurviveSerialization() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialChainMap f=degreeMap(3); RelativeSimplicialChain cycle=new RelativeSimplicialChain(circle(),z(1),new IntegerVector(z(1),z(-1),z(1)));
        IAlgebraItem<RelativeSimplicialChain> item=math.chainMaps.algebra().buildAlgebraItem(f).performLeftProjectionOperation("on-chain",cycle).perform();
        assertSame(math.relativeChains.algebra(),item.getAlgebra()); assertEquals(cycle.scale(z(3)),item.getResult());
        IAlgebraFlow<SimplicialChainMap> constructed=new AlgebraFlow<>(new ListAlgebraInput<>(math.chainMaps.inputs,Collections.singletonList(f.data())),math,"ChainMap.data").<SimplicialChainMap>performAlgebraTransfer("ChainMap.from-data");
        IAlgebraFlow<RelativeSimplicialChain> actions=math.flow(math.chainMaps,Collections.singletonList(f)).performLeftProjectionOperation("on-chain",cycle);
        IAlgebraFlow<IntegerMatrix> flat=math.flow(math.chainMaps,Collections.singletonList(f)).<IntegerMatrix>performFlatAlgebraTransfer("chain-matrices");
        IAlgebraFlow<Boolean> induced=math.flow(math.chainMaps,Collections.singletonList(degreeMap(-1))).<AbelianGroupHomomorphism>performFlatAlgebraTransfer("cohomology-maps").<Boolean>performAlgebraTransfer("is-isomorphism");
        for(IAlgebraFlow<?> original : Arrays.asList(constructed,actions,flat,induced)) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(original); }
            IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
            assertEquals(original.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        }
        assertEquals(Collections.singletonList(f.toString()),constructed.collect()); assertEquals(Arrays.asList("true","true"),induced.collect());
        SimplicialChainMap.Data invalid=new SimplicialChainMap.Data(circle(),circle(),Arrays.asList(IntegerMatrix.identity(3),IntegerMatrix.zero(3,3)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> math.chainMaps.inputs.buildAlgebraItem(invalid).performAlgebraTransfer("ChainMap.from-data").perform());
    }
}
