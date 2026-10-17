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

public class NativeSimplicialChainInverseTest {
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
    private static SimplicialChainMap pointMap(IntegerMatrix m) { return new SimplicialChainMap(points(m.columns()),points(m.rows()),Collections.singletonList(m)); }
    private static MathFailure failure(MathFailure.Kind kind,Runnable body) { MathFailure e=assertThrows(MathFailure.class,body::run); assertEquals(kind,e.kind()); return e; }
    private static void boundaryIdentities(SimplicialChainHomotopy h) {
        for(int d=0;d<=h.source().ambient().dimension();d++) {
            BigInteger degree=z(d); IntegerMatrix expected=h.to().chainMatrix(degree).add(h.from().chainMatrix(degree).scale(z(-1)));
            IntegerMatrix actual=h.target().boundaryMatrix(z(d+1)).multiply(h.chainMatrix(degree))
                    .add(h.cochainMatrix(degree).transpose().multiply(h.source().boundaryMatrix(degree)));
            assertEquals(expected,actual);
        }
    }
    private static SimplicialChainMap check(SimplicialChainMap f,boolean exists) {
        assertEquals(exists,SimplicialChainInverseSolver.isHomotopyEquivalence(f));
        if(!exists) {
            failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialChainInverseSolver.inverse(f));
            failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialChainInverseSolver.inverseHomotopies(f)); return null;
        }
        SimplicialChainMap g=SimplicialChainInverseSolver.inverse(f); assertEquals(f.target(),g.source()); assertEquals(f.source(),g.target());
        List<SimplicialChainHomotopy> h=SimplicialChainInverseSolver.inverseHomotopies(f); assertEquals(2,h.size());
        assertEquals(g.compose(f),h.get(0).from()); assertEquals(SimplicialChainMap.identity(f.source()),h.get(0).to());
        assertEquals(f.compose(g),h.get(1).from()); assertEquals(SimplicialChainMap.identity(f.target()),h.get(1).to());
        boundaryIdentities(h.get(0)); boundaryIdentities(h.get(1)); return g;
    }
    @Test public void all625PointMatricesMatchIndependentUnimodularDeterminantsAndAdjugates() {
        for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) for(int c=-2;c<=2;c++) for(int d=-2;d<=2;d++) {
            int det=a*d-b*c; SimplicialChainMap f=pointMap(matrix(new long[]{a,b},new long[]{c,d})),g=check(f,Math.abs(det)==1);
            SimplicialChainMapClass value=SimplicialChainMapClass.fromMap(f); assertEquals(Math.abs(det)==1,value.isIsomorphism());
            if(g!=null) { assertEquals(matrix(new long[]{d/det,-b/det},new long[]{-c/det,a/det}),g.chainMatrix(z(0))); assertEquals(SimplicialChainMapClass.fromMap(g),value.inverse()); }
            else failure(MathFailure.Kind.OPERATION_UNDEFINED,value::inverse);
        }
    }
    private static SimplicialChainMap intervalMap(int a,int b,int augmentation) {
        return new SimplicialChainMap(edge(),edge(),Arrays.asList(matrix(new long[]{a,b},new long[]{augmentation-a,augmentation-b}),matrix(new long[]{a-b})));
    }
    @Test public void all125IntervalMapsAreInvertibleExactlyForUnitAugmentation() {
        for(int n=-2;n<=2;n++) for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) check(intervalMap(a,b,n),Math.abs(n)==1);
        SimplicialChainMap constant=intervalMap(1,1,1); assertFalse(constant.isIsomorphism()); check(constant,true);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,constant::inverse);
        SimplicialChainMapClass c=SimplicialChainMapClass.fromMap(constant); assertTrue(c.isIsomorphism()); assertEquals(c,c.inverse()); assertTrue(c.compose(c.inverse()).isIdentity());
    }
    private static RelativeSimplicialComplex relativeInterval(int mask) {
        FiniteSimplicialComplex sub=mask==7?edge().ambient():mask==3?points(2).ambient():mask==1?complex(new int[]{0}):mask==2?complex(new int[]{1}):complex();
        return new RelativeSimplicialComplex(edge().ambient(),sub);
    }
    @Test public void all25RelativeIntervalContextsAgreeWithIndependentQuotientHomology() {
        int[] masks={0,1,2,3,7}; int checked=0;
        for(int a : masks) for(int b : masks) {
            RelativeSimplicialComplex s=relativeInterval(a),t=relativeInterval(b); int s0=s.simplexCount(z(0)),s1=s.simplexCount(z(1)),t0=t.simplexCount(z(0)),t1=t.simplexCount(z(1));
            int choices=(int)Math.pow(3,s0*t0+s1*t1);
            for(int code=0;code<choices;code++) {
                int rest=code; BigInteger[][] f0=new BigInteger[t0][s0],f1=new BigInteger[t1][s1];
                for(int r=0;r<t0;r++) for(int c=0;c<s0;c++) { f0[r][c]=z(rest%3-1); rest/=3; }
                for(int r=0;r<t1;r++) for(int c=0;c<s1;c++) { f1[r][c]=z(rest%3-1); rest/=3; }
                IntegerMatrix m0=new IntegerMatrix(t0,s0,f0),m1=new IntegerMatrix(t1,s1,f1);
                if(!t.boundaryMatrix(z(1)).multiply(m1).equals(m0.multiply(s.boundaryMatrix(z(1))))) continue;
                boolean acyclicSource=a==1 || a==2 || a==7,acyclicTarget=b==1 || b==2 || b==7;
                boolean exists=acyclicSource && acyclicTarget;
                if(a==0 && b==0) exists=f0[0][0].add(f0[1][0]).abs().equals(z(1));
                if(a==3 && b==3) exists=f1[0][0].abs().equals(z(1));
                check(new SimplicialChainMap(s,t,Arrays.asList(m0,m1)),exists); checked++;
            }
        }
        assertTrue(checked>50);
    }
    @Test public void aOneSidedInverseDoesNotSupplyTheMissingHomotopy() {
        SimplicialChainMap inclusion=pointMap(matrix(new long[]{1},new long[]{0})),projection=pointMap(matrix(new long[]{1,0}));
        assertTrue(projection.compose(inclusion).isIdentity()); assertFalse(inclusion.compose(projection).isIdentity()); check(inclusion,false); check(projection,false);
        check(SimplicialChainMap.zero(points(0),points(1)),false); check(SimplicialChainMap.zero(points(1),points(0)),false);
    }
    private static SimplicialChainMap circleMap(int h0,int h1) {
        return new SimplicialChainMap(circle(),circle(),Arrays.asList(IntegerMatrix.identity(3).scale(z(h0)),matrix(new long[]{h1,0,0},new long[]{h0-h1,h0,0},new long[]{h1-h0,0,h0})));
    }
    @Test public void bothCircleDegreesMustBeIntegralUnits() {
        for(int h0=-3;h0<=3;h0++) for(int h1=-3;h1<=3;h1++) {
            boolean exists=Math.abs(h0)==1 && Math.abs(h1)==1; SimplicialChainMap f=circleMap(h0,h1),g=check(f,exists);
            if(exists) for(int d=0;d<2;d++) {
                assertEquals(f.homologyMap(z(d)).inverse(),g.homologyMap(z(d))); assertEquals(f.cohomologyMap(z(d)).inverse(),g.cohomologyMap(z(d)));
            }
        }
    }
    private static RelativeSimplicialComplex torsionPair() {
        int[][] f={{0,1,2},{0,1,3},{0,2,4},{0,3,5},{0,4,5},{1,2,5},{1,3,4},{1,4,5},{2,3,4},{2,3,5}};
        return new RelativeSimplicialComplex(complex(f),complex(Arrays.copyOf(f,5)));
    }
    @Test public void oddScalingIsAHomotopyUnitOnTwoTorsionEvenWithoutAStrictInverse() {
        RelativeSimplicialComplex pair=torsionPair(); assertEquals(AbelianGroupType.cyclic(z(2)),pair.homologyType(z(1))); assertEquals(AbelianGroupType.ZERO,pair.homologyType(z(2)));
        for(int n=-4;n<=4;n++) {
            SimplicialChainMap f=SimplicialChainMap.identity(pair).scale(z(n)); SimplicialChainMap g=check(f,n%2!=0);
            if(g!=null) { assertEquals(f.homologyMap(z(1)).inverse(),g.homologyMap(z(1))); assertEquals(f.cohomologyMap(z(2)).inverse(),g.cohomologyMap(z(2))); }
        }
        SimplicialChainMapClass odd=SimplicialChainMapClass.fromMap(SimplicialChainMap.identity(pair).scale(z(3)));
        assertTrue(odd.isIsomorphism()); assertEquals(odd,odd.inverse()); assertTrue(odd.compose(odd.inverse()).isIdentity());
        assertFalse(SimplicialChainMap.identity(pair).scale(z(3)).isIsomorphism());
    }
    @Test public void unboundedCoefficientsRetainExactIntegralObstructions() {
        BigInteger huge=BigInteger.ONE.shiftLeft(1024); SimplicialChainMap f=pointMap(new IntegerMatrix(new BigInteger[][]{{BigInteger.ONE,huge},{BigInteger.ZERO,BigInteger.ONE}}));
        assertEquals(f.inverse(),check(f,true));
        check(SimplicialChainMap.identity(torsionPair()).scale(huge),false); check(SimplicialChainMap.identity(torsionPair()).scale(huge.add(BigInteger.ONE)),true);
    }
    @Test public void differentlyLabelledAndDifferentDimensionalEndpointsAreReversedExactly() {
        RelativeSimplicialComplex target=abs(complex(new int[]{7})); SimplicialChainMap f=new SimplicialChainMap(points(1),target,Collections.singletonList(IntegerMatrix.identity(1))); check(f,true);
        RelativeSimplicialComplex triangle=abs(complex(new int[]{0,1,2})); SimplicialChainMap inclusion=SimplicialChainMap.fromSimplicial(RelativeSimplicialMap.inclusion(edge(),triangle)),inverse=check(inclusion,true);
        List<SimplicialChainHomotopy> h=SimplicialChainInverseSolver.inverseHomotopies(inclusion); assertEquals(2,h.get(0).chainMatrices().size()); assertEquals(3,h.get(1).chainMatrices().size());
        assertEquals(IntegerMatrix.zero(0,1),inverse.chainMatrix(z(2))); assertEquals(IntegerMatrix.zero(0,1),h.get(1).chainMatrix(z(2)));
        check(inverse,true);
    }
    @Test public void classInversesAreUniqueReverseCompositionAndAreHomotopyInvariant() {
        SimplicialChainMapClass a=SimplicialChainMapClass.fromMap(pointMap(matrix(new long[]{1,2},new long[]{0,1}))),b=SimplicialChainMapClass.fromMap(pointMap(matrix(new long[]{1,0},new long[]{3,1})));
        assertEquals(a,a.inverse().inverse()); assertEquals(b.inverse().compose(a.inverse()),a.compose(b).inverse()); assertNotEquals(a.inverse().compose(b.inverse()),a.compose(b).inverse());
        assertTrue(a.inverse().compose(a).isIdentity()); assertTrue(a.compose(a.inverse()).isIdentity());
        SimplicialChainMapClass constant=SimplicialChainMapClass.fromMap(intervalMap(1,1,1)),identity=SimplicialChainMapClass.identityOn(edge()); assertEquals(constant.inverse(),identity.inverse());
    }
    @Test public void acyclicEmptyAndDiagonalContextsRetainBothWitnessesAndEmptyShapes() {
        List<RelativeSimplicialComplex> contexts=Arrays.asList(points(0),relativeInterval(1),relativeInterval(2),RelativeSimplicialComplex.diagonal(edge().ambient()));
        for(RelativeSimplicialComplex s : contexts) for(RelativeSimplicialComplex t : contexts) {
            SimplicialChainMap f=SimplicialChainMap.zero(s,t); check(f,true); SimplicialChainMapClass value=SimplicialChainMapClass.fromMap(f);
            assertTrue(value.isZero()); assertTrue(value.isIsomorphism()); assertTrue(value.inverse().isZero()); assertTrue(value.compose(value.inverse()).isIdentity());
        }
        assertThrows(UnsupportedOperationException.class,SimplicialChainInverseSolver.inverseHomotopies(SimplicialChainMap.identity(points(0)))::clear);
    }
    @Test public void aggregateBoundsCountBothIdentitiesEvenForObviousInputs() {
        check(SimplicialChainMap.identity(points(11)),true);
        SimplicialChainMap tooLarge=SimplicialChainMap.identity(points(12));
        for(Runnable body : Arrays.<Runnable>asList(() -> SimplicialChainInverseSolver.isHomotopyEquivalence(tooLarge),() -> SimplicialChainInverseSolver.inverse(tooLarge),() -> SimplicialChainInverseSolver.inverseHomotopies(tooLarge)))
            assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,body).getMessage().contains("256 total equations"));
        check(SimplicialChainMap.zero(points(16),points(0)),false);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialChainInverseSolver.isHomotopyEquivalence(SimplicialChainMap.zero(points(17),points(0))));
    }
    @Test public void inverseSolvingAndClassNormalizationShareOneBudget() {
        List<FiniteSet<Integer>> facets=new ArrayList<>(); for(int i=0;i<2;i++) for(int j=2;j<6;j++) facets.add(FiniteSet.of(i,j));
        RelativeSimplicialComplex pair=abs(new FiniteSimplicialComplex(facets)); SimplicialChainMapClass value=SimplicialChainMapClass.identityOn(pair);
        assertTrue(value.isIsomorphism()); SimplicialChainMap inverse=SimplicialChainInverseSolver.inverse(value.representative());
        assertEquals(value,SimplicialChainMapClass.fromMap(inverse));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,value::inverse).getMessage().contains("5000000"));
        List<FiniteSet<Integer>> dense=new ArrayList<>(); for(int i=0;i<6;i++) for(int j=i+1;j<6;j++) if(dense.size()<8) dense.add(FiniteSet.of(i,j));
        SimplicialChainMapClass costly=SimplicialChainMapClass.identityOn(abs(new FiniteSimplicialComplex(dense)));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,costly::isIsomorphism).getMessage().contains("5000000"));
    }
    @Test public void nativeDecisionsInverseMapsClassesAndWitnessFlowsSerialize() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialChainMap constant=intervalMap(1,1,1); SimplicialChainMapClass value=SimplicialChainMapClass.fromMap(constant);
        IAlgebraItem<SimplicialChainMap> item=math.chainMaps.algebra().buildAlgebraItem(constant); assertSame(math.chainMaps.algebra(),item.performOneOperandOperation("homotopy-inverse").getAlgebra());
        assertSame(math.booleans.algebra(),item.performAlgebraTransfer("is-homotopy-equivalence").getAlgebra());
        List<IAlgebraItem<SimplicialChainHomotopy>> witnesses=item.performAlgebraFlatTransfer("ChainHomotopy.inverse-homotopies"); assertEquals(2,witnesses.size()); for(IAlgebraItem<?> witness : witnesses) assertSame(math.chainHomotopies.algebra(),witness.getAlgebra());
        assertSame(math.chainMapClasses.algebra(),math.chainMapClasses.algebra().buildAlgebraItem(value).performOneOperandOperation("inverse").getAlgebra());
        IAlgebraFlow<Boolean> decision=math.flow(math.chainMaps,Collections.singletonList(constant)).performAlgebraTransfer("is-homotopy-equivalence");
        IAlgebraFlow<SimplicialChainMap> maps=math.flow(math.chainMaps,Collections.singletonList(constant)).performOneOperandOperation("homotopy-inverse");
        IAlgebraFlow<SimplicialChainHomotopy> homotopies=math.flow(math.chainMaps,Collections.singletonList(constant)).performFlatAlgebraTransfer("ChainHomotopy.inverse-homotopies");
        IAlgebraFlow<Boolean> classes=math.flow(math.chainMapClasses,Collections.singletonList(value)).performOneOperandOperation("inverse").performOperation("compose",value).performAlgebraTransfer("is-identity");
        for(IAlgebraFlow<?> flow : Arrays.asList(decision,maps,homotopies,classes)) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
            IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
            assertEquals(flow.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        }
        assertEquals(Collections.singletonList("true"),decision.collect()); assertEquals(Collections.singletonList("true"),classes.collect()); assertEquals(2,homotopies.collect().size());
    }
}
