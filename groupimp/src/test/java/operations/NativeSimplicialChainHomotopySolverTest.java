package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.linear.*;
import mathematics.topology.*;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeSimplicialChainHomotopySolverTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int v : facet) labels.add(v); faces.add(new FiniteSet<>(labels)); } return new FiniteSimplicialComplex(faces);
    }
    private static FiniteSimplicialComplex points(int n) { int[][] facets=new int[n][1]; for(int i=0;i<n;i++) facets[i][0]=i; return complex(facets); }
    private static RelativeSimplicialComplex abs(FiniteSimplicialComplex c) { return RelativeSimplicialComplex.absolute(c); }
    private static RelativeSimplicialComplex point() { return abs(points(1)); }
    private static RelativeSimplicialComplex edge() { return abs(complex(new int[]{0,1})); }
    private static RelativeSimplicialComplex circle() { return abs(complex(new int[]{0,1},new int[]{0,2},new int[]{1,2})); }
    private static IntegerMatrix matrix(long[]... entries) {
        BigInteger[][] values=new BigInteger[entries.length][]; for(int r=0;r<entries.length;r++) { values[r]=new BigInteger[entries[r].length]; for(int c=0;c<entries[r].length;c++) values[r][c]=z(entries[r][c]); } return new IntegerMatrix(values);
    }
    private static MathFailure failure(MathFailure.Kind kind,Runnable body) { MathFailure error=assertThrows(MathFailure.class,body::run); assertEquals(kind,error.kind()); return error; }
    private static List<Runnable> operations(SimplicialChainMap f,SimplicialChainMap g) {
        return Arrays.asList(() -> SimplicialChainHomotopySolver.areHomotopic(f,g),() -> SimplicialChainHomotopySolver.between(f,g),() -> SimplicialChainHomotopySolver.solutionGenerators(f,g));
    }
    private static void identities(SimplicialChainHomotopy h) {
        for(int k=0;k<=Math.max(h.source().ambient().dimension(),h.target().ambient().dimension())+1;k++) {
            IntegerMatrix difference=h.to().chainMatrix(z(k)).add(h.from().chainMatrix(z(k)).scale(z(-1)));
            assertEquals(difference,h.target().boundaryMatrix(z(k+1)).multiply(h.chainMatrix(z(k))).add(h.cochainMatrix(z(k)).transpose().multiply(h.source().boundaryMatrix(z(k)))));
        }
    }
    private static List<SimplicialChainHomotopy> check(SimplicialChainMap f,SimplicialChainMap g,boolean exists) {
        assertEquals(exists,SimplicialChainHomotopySolver.areHomotopic(f,g));
        List<SimplicialChainHomotopy> generators=SimplicialChainHomotopySolver.solutionGenerators(f,g);
        if(!exists) { assertTrue(generators.isEmpty()); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialChainHomotopySolver.between(f,g)); }
        else {
            assertFalse(generators.isEmpty()); SimplicialChainHomotopy h=SimplicialChainHomotopySolver.between(f,g); assertEquals(h,generators.get(0)); assertEquals(f,h.from()); assertEquals(g,h.to());
            for(int i=0;i<generators.size();i++) { identities(generators.get(i)); if(i>0) { assertTrue(generators.get(i).from().isZero()); assertEquals(generators.get(i).from(),generators.get(i).to()); } }
        }
        return generators;
    }
    @Test public void all289IntervalMapPairsAgreeWithAugmentationAndUniqueEndpointFormula() {
        List<SimplicialChainMap> maps=new ArrayList<>();
        for(int a=-1;a<=1;a++) for(int b=-1;b<=1;b++) for(int c=-1;c<=1;c++) for(int d=-1;d<=1;d++) for(int e=-1;e<=1;e++)
            if(b-a==-e && d-c==e) maps.add(new SimplicialChainMap(edge(),edge(),Arrays.asList(matrix(new long[]{a,b},new long[]{c,d}),matrix(new long[]{e}))));
        assertEquals(17,maps.size());
        for(SimplicialChainMap f : maps) for(SimplicialChainMap g : maps) {
            IntegerMatrix a=f.chainMatrix(z(0)),b=g.chainMatrix(z(0)); boolean exists=a.get(0,0).add(a.get(1,0)).equals(b.get(0,0).add(b.get(1,0)));
            List<SimplicialChainHomotopy> result=check(f,g,exists);
            if(exists) { assertEquals(1,result.size()); assertEquals(matrix(new long[]{b.get(1,0).subtract(a.get(1,0)).longValue(),b.get(1,1).subtract(a.get(1,1)).longValue()}),result.get(0).chainMatrix(z(0))); }
        }
    }
    private static FiniteSimplicialComplex intervalSubcomplex(int mask) {
        if(mask==7) return complex(new int[]{0,1}); if(mask==3) return points(2); if(mask==1) return points(1); if(mask==2) return complex(new int[]{1}); return complex();
    }
    @Test public void all25RelativeIntervalContextsUseQuotientGroupsAndIntegralContractions() {
        int[] masks={0,1,2,3,7};
        for(int a : masks) for(int b : masks) {
            RelativeSimplicialComplex source=new RelativeSimplicialComplex(edge().ambient(),intervalSubcomplex(a)),target=new RelativeSimplicialComplex(edge().ambient(),intervalSubcomplex(b));
            int s0=2-Integer.bitCount(a&3),t0=2-Integer.bitCount(b&3),s1=(a&4)==0?1:0,t1=(b&4)==0?1:0;
            List<SimplicialChainMap> maps=new ArrayList<>(); int choices=(int)Math.pow(3,s0*t0+s1*t1);
            for(int code=0;code<choices;code++) {
                int rest=code; BigInteger[][] f0=new BigInteger[t0][s0],f1=new BigInteger[t1][s1];
                for(int r=0;r<t0;r++) for(int c=0;c<s0;c++) { f0[r][c]=z(rest%3-1); rest/=3; }
                for(int r=0;r<t1;r++) for(int c=0;c<s1;c++) { f1[r][c]=z(rest%3-1); rest/=3; }
                boolean valid=true; int row=0;
                for(int v=0;v<2;v++) if((b&(1<<v))==0) {
                    if(s1>0) { long lhs=t1==0?0:(v==0?-1:1)*f1[0][0].longValue(),rhs=0; int col=0; for(int w=0;w<2;w++) if((a&(1<<w))==0) rhs+=(w==0?-1:1)*f0[row][col++].longValue(); valid&=lhs==rhs; } row++;
                }
                if(valid) maps.add(new SimplicialChainMap(source,target,Arrays.asList(new IntegerMatrix(t0,s0,f0),new IntegerMatrix(t1,s1,f1))));
            }
            for(SimplicialChainMap f : maps) for(SimplicialChainMap g : maps) {
                boolean exists=true;
                if(a==0 && b==0) exists=f.chainMatrix(z(0)).get(0,0).add(f.chainMatrix(z(0)).get(1,0)).equals(g.chainMatrix(z(0)).get(0,0).add(g.chainMatrix(z(0)).get(1,0)));
                if(a==3 && b==3) exists=f.chainMatrix(z(1)).equals(g.chainMatrix(z(1)));
                check(f,g,exists);
            }
        }
    }
    @Test public void all729CircleVertexMapPairsAgreeWithIndependentWindingNumbers() {
        List<SimplicialChainMap> maps=new ArrayList<>(); List<Integer> winding=new ArrayList<>();
        for(int a=0;a<3;a++) for(int b=0;b<3;b++) for(int c=0;c<3;c++) {
            Map<BigInteger,BigInteger> vertices=new TreeMap<>(); vertices.put(z(0),z(a)); vertices.put(z(1),z(b)); vertices.put(z(2),z(c));
            maps.add(SimplicialChainMap.fromAbsoluteSimplicial(new FiniteSimplicialMap(circle().ambient(),circle().ambient(),vertices)));
            winding.add(a==b || b==c || a==c?0:((a>b?1:0)+(a>c?1:0)+(b>c?1:0))%2==0?1:-1);
        }
        for(int i=0;i<27;i++) for(int j=0;j<27;j++) {
            boolean exists=winding.get(i).equals(winding.get(j)); List<SimplicialChainHomotopy> result=check(maps.get(i),maps.get(j),exists);
            if(exists) {
                assertEquals(2,result.size()); IntegerMatrix loop=result.get(1).chainMatrix(z(0)); BigInteger sign=loop.get(0,0); assertEquals(BigInteger.ONE,sign.abs());
                for(int c=0;c<3;c++) assertEquals(new IntegerVector(sign,sign.negate(),sign),loop.column(c));
            }
        }
    }
    private static SimplicialChainMap pointToCircle(int vertex) {
        return new SimplicialChainMap(point(),circle(),Arrays.asList(matrix(new long[]{vertex==0?1:0},new long[]{vertex==1?1:0},new long[]{vertex==2?1:0}),IntegerMatrix.zero(3,0)));
    }
    @Test public void affineGeneratorsParametrizeEveryBoundedIntegralPathAndRetainZeroEndpointLoops() {
        SimplicialChainMap f=pointToCircle(0),g=pointToCircle(2); List<SimplicialChainHomotopy> result=check(f,g,true); assertEquals(2,result.size());
        SimplicialChainHomotopy particular=result.get(0),loop=result.get(1); IntegerVector p=particular.chainMatrix(z(0)).column(0),v=loop.chainMatrix(z(0)).column(0);
        assertEquals(BigInteger.ONE,v.get(0).abs()); assertEquals(v.get(0).negate(),v.get(1)); assertEquals(v.get(0),v.get(2));
        for(int a=-5;a<=5;a++) for(int b=-5;b<=5;b++) for(int c=-5;c<=5;c++) if(-a-b==-1 && a-c==0 && b+c==1) {
            BigInteger coefficient=z(a).subtract(p.get(0)).divide(v.get(0)); SimplicialChainHomotopy h=particular.add(loop.scale(coefficient));
            assertEquals(f,h.from()); assertEquals(g,h.to()); assertEquals(new IntegerVector(z(a),z(b),z(c)),h.chainMatrix(z(0)).column(0)); identities(h);
        }
        List<SimplicialChainHomotopy> equal=check(f,f,true); assertEquals(SimplicialChainHomotopy.stationary(f),equal.get(0)); assertFalse(equal.get(1).chainMatrix(z(0)).equals(IntegerMatrix.zero(3,1)));
        assertEquals(result,SimplicialChainHomotopySolver.solutionGenerators(f,g)); assertThrows(UnsupportedOperationException.class,result::clear);
    }
    private static RelativeSimplicialComplex projectivePlane() { return abs(complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5})); }
    private static SimplicialChainMap torsionMap(BigInteger a,BigInteger b) {
        FiniteSimplicialComplex disk=complex(new int[]{0,1,2}); RelativeSimplicialComplex target=new RelativeSimplicialComplex(disk,disk.skeleton(1));
        BigInteger[][] top=new BigInteger[1][10]; Arrays.fill(top[0],BigInteger.ZERO); top[0][0]=a; top[0][1]=b;
        return new SimplicialChainMap(projectivePlane(),target,Arrays.asList(IntegerMatrix.zero(0,6),IntegerMatrix.zero(0,15),new IntegerMatrix(top)));
    }
    @Test public void projectivePlaneObstructionsAreIntegralParityEvenWhenEveryHomologyMapAgrees() {
        SimplicialChainMap zero=torsionMap(z(0),z(0)),odd=torsionMap(z(1),z(0));
        for(int k=0;k<3;k++) assertEquals(zero.homologyMap(z(k)),odd.homologyMap(z(k)));
        assertNotEquals(zero.cohomologyMap(z(2)),odd.cohomologyMap(z(2)));
        for(int a=-4;a<=4;a++) for(int b=-4;b<=4;b++) {
            List<SimplicialChainHomotopy> result=check(zero,torsionMap(z(a),z(b)),(a+b)%2==0);
            if(!result.isEmpty()) assertEquals(6,result.size());
        }
    }
    @Test public void integralCoefficientsAreUnboundedAndNoRationalWitnessIsReturnedForOddTorsion() {
        BigInteger huge=BigInteger.ONE.shiftLeft(1024); SimplicialChainMap zero=torsionMap(z(0),z(0));
        check(zero,torsionMap(huge,z(0)),true); check(zero,torsionMap(huge.add(BigInteger.ONE),z(0)),false);
    }
    @Test public void solverCouplesBothDegreesOfTriangleWitnessesAndAcceptsGeometricEndpoints() {
        RelativeSimplicialComplex triangle=abs(complex(new int[]{0,1,2})); SimplicialChainMap f=SimplicialChainMap.identity(triangle);
        IntegerMatrix d1=matrix(new long[]{-1,-1,0},new long[]{1,0,-1},new long[]{0,1,1}),d2=matrix(new long[]{1},new long[]{-1},new long[]{1});
        for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) {
            IntegerMatrix h0=matrix(new long[]{a,0,1},new long[]{0,b,0},new long[]{1,0,a}),h1=matrix(new long[]{b,a,1});
            SimplicialChainMap g=new SimplicialChainMap(triangle,triangle,Arrays.asList(IntegerMatrix.identity(3).add(d1.multiply(h0)),IntegerMatrix.identity(3).add(d2.multiply(h1)).add(h0.multiply(d1)),IntegerMatrix.identity(1).add(h1.multiply(d2)))); check(f,g,true);
        }
        List<SimplicialChainHomotopy> geometric=Arrays.asList(SimplicialChainHomotopy.fromCollapse(new SimplicialCollapse(triangle,FiniteSet.of(z(0),z(1)))),SimplicialChainHomotopy.fromSubdivision(new SimplicialSubdivision(edge())));
        for(SimplicialChainHomotopy h : geometric) check(h.from(),h.to(),true);
    }
    @Test public void zeroDimensionalAndEmptySystemsRetainAllDegreeShapes() {
        RelativeSimplicialComplex empty=abs(complex());
        for(RelativeSimplicialComplex source : Arrays.asList(empty,point())) for(RelativeSimplicialComplex target : Arrays.asList(empty,point())) {
            SimplicialChainMap f=SimplicialChainMap.zero(source,target); List<SimplicialChainHomotopy> result=check(f,f,true); assertEquals(Collections.singletonList(SimplicialChainHomotopy.stationary(f)),result);
            assertEquals(RelativeSimplicialChain.zero(target,z(0)),result.get(0).onChain(RelativeSimplicialChain.zero(source,z(-1))));
        }
        SimplicialChainMap id=SimplicialChainMap.identity(point()); check(id,id.scale(z(2)),false);
    }
    @Test public void allSolverOperationsRequireFullLabelledPairsBeforeShapeChecks() {
        SimplicialChainMap id=SimplicialChainMap.identity(edge()),relabelled=SimplicialChainMap.identity(abs(complex(new int[]{4,5}))),based=SimplicialChainMap.identity(new RelativeSimplicialComplex(edge().ambient(),points(1)));
        for(SimplicialChainMap other : Arrays.asList(relabelled,based)) for(Runnable operation : operations(id,other)) failure(MathFailure.Kind.OPERATION_UNDEFINED,operation);
        // Same quotient ranks and the same ambient labels still do not identify distinct subcomplexes.
        SimplicialChainMap opposite=SimplicialChainMap.identity(new RelativeSimplicialComplex(edge().ambient(),complex(new int[]{1})));
        for(Runnable operation : operations(based,opposite)) failure(MathFailure.Kind.OPERATION_UNDEFINED,operation);
    }
    private static RelativeSimplicialComplex star(int edges,boolean killLeaves) {
        int[][] facets=new int[edges][2]; for(int i=0;i<edges;i++) { facets[i][0]=0; facets[i][1]=i+1; }
        return new RelativeSimplicialComplex(complex(facets),points(killLeaves?edges+1:1));
    }
    @Test public void aggregateEquationBoundAccepts256AndFailsBeforeClaimingAbsenceAt289() {
        SimplicialChainMap small=SimplicialChainMap.identity(abs(points(16))),large=SimplicialChainMap.identity(abs(points(17)));
        check(small,small,true); check(small,SimplicialChainMap.zero(small.source(),small.target()),false);
        for(Runnable operation : operations(large,large)) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,operation).getMessage().contains("256 total equations"));
    }
    @Test public void maximalKernelReturnsAll257GeneratorsAndUnknownBoundIsIndependentOfEquations() {
        SimplicialChainMap small=SimplicialChainMap.zero(abs(points(16)),star(16,true)); List<SimplicialChainHomotopy> result=check(small,small,true); assertEquals(257,result.size());
        for(int i=0;i<256;i++) {
            IntegerMatrix h=result.get(i+1).chainMatrix(z(0)); assertEquals(16,h.rows()); assertEquals(16,h.columns());
            for(int r=0;r<16;r++) for(int c=0;c<16;c++) assertEquals(z(r*16+c==i?1:0),h.get(r,c));
        }
        SimplicialChainMap large=SimplicialChainMap.zero(abs(points(17)),star(16,true));
        for(Runnable operation : operations(large,large)) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,operation).getMessage().contains("256 total unknown coefficients"));
    }
    @Test public void denseWorkLimitIsAnImplementationFailureEvenWithAnExplicitIntegralWitness() {
        RelativeSimplicialComplex source=abs(points(16)),target=star(16,false); SimplicialChainMap f=SimplicialChainMap.zero(source,target),g=new SimplicialChainMap(source,target,Arrays.asList(IntegerMatrix.identity(16),IntegerMatrix.zero(16,0)));
        SimplicialChainHomotopy supplied=new SimplicialChainHomotopy(f,g,Arrays.asList(IntegerMatrix.identity(16),IntegerMatrix.zero(0,0))); identities(supplied);
        for(Runnable operation : operations(f,g)) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,operation).getMessage().contains("5000000"));
    }
    @Test public void wholeGeneratorListSharesTheSolveAndValidationBudgetWithoutTruncation() {
        List<FiniteSet<Integer>> facets=new ArrayList<>(); for(int i=0;i<12;i++) for(int j=i+1;j<12;j++) if(facets.size()<63) facets.add(FiniteSet.of(i,j));
        SimplicialChainMap f=SimplicialChainMap.zero(circle(),abs(new FiniteSimplicialComplex(facets)));
        assertTrue(SimplicialChainHomotopySolver.areHomotopic(f,f)); identities(SimplicialChainHomotopySolver.between(f,f));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialChainHomotopySolver.solutionGenerators(f,f)).getMessage().contains("5000000"));
    }
    @Test public void nativeScalarFlatAndSecondOperandFlowsUseActualWrappersAndSerialize() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialChainMap f=pointToCircle(0),g=pointToCircle(2); IAlgebraItem<SimplicialChainMap> item=math.chainMaps.algebra().buildAlgebraItem(f);
        IAlgebraItem<SimplicialChainHomotopy> witness=item.performCustomResultOperation("ChainHomotopy.between",g); assertSame(math.chainHomotopies.algebra(),witness.getAlgebra());
        List<IAlgebraItem<SimplicialChainHomotopy>> wrapped=item.performCustomResultFlatOperation("ChainHomotopy.solution-generators",g); assertEquals(2,wrapped.size());
        for(IAlgebraItem<SimplicialChainHomotopy> value : wrapped) { assertSame(math.chainHomotopies.algebra(),value.getAlgebra()); identities(value.perform().getResult()); }
        RelativeSimplicialChain vertex=new RelativeSimplicialChain(point(),z(0),new IntegerVector(z(1))); assertSame(math.relativeChains.algebra(),witness.performLeftProjectionOperation("on-chain",vertex).getAlgebra());
        IAlgebraFlow<Boolean> decision=math.flow(math.chainMaps,Collections.singletonList(f)).performCustomResultOperation("ChainHomotopy.are-homotopic",g);
        IAlgebraFlow<IntegerVector> action=math.flow(math.chainMaps,Collections.singletonList(f)).<SimplicialChainHomotopy>performCustomResultOperation("ChainHomotopy.between",g).performLeftProjectionOperation("on-chain",vertex).performAlgebraTransfer("coordinates");
        IAlgebraFlow<IntegerMatrix> generators=math.flow(math.chainMaps,Collections.singletonList(f)).<SimplicialChainHomotopy>performFlatCustomResultOperation("ChainHomotopy.solution-generators",g).performFlatAlgebraTransfer("chain-matrices");
        for(IAlgebraFlow<?> original : Arrays.asList(decision,action,generators)) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(original); }
            IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
            assertEquals(original.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        }
        assertEquals(Collections.singletonList("true"),decision.collect()); assertEquals(1,action.collect().size()); assertEquals(4,generators.collect().size());
        SimplicialChainMap id=SimplicialChainMap.identity(point()); assertEquals(Collections.emptyList(),math.flow(math.chainMaps,Collections.singletonList(id)).performFlatCustomResultOperation("ChainHomotopy.solution-generators",id.scale(z(2))).collect());
    }
}
