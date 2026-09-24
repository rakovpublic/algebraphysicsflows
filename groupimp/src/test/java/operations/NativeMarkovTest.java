package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.*;
import mathematics.linear.*;
import mathematics.numbers.Rational;
import mathematics.probability.*;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeMarkovTest {
    private final ConcreteMathematics math=new ConcreteMathematics();
    private static BigInteger z(long value) { return BigInteger.valueOf(value); }
    private static FiniteSet<BigInteger> set(long... values) {
        List<BigInteger> result=new ArrayList<>(); for(long value : values) result.add(z(value)); return new FiniteSet<>(result);
    }
    private static RationalVector vector(long denominator,long... values) {
        Rational[] result=new Rational[values.length]; for(int i=0;i<values.length;i++) result[i]=Rational.of(values[i],denominator); return new RationalVector(result);
    }
    private static RationalMatrix matrix(long denominator,long[]... rows) {
        Rational[][] values=new Rational[rows.length][];
        for(int r=0;r<rows.length;r++) { values[r]=new Rational[rows[r].length]; for(int c=0;c<rows[r].length;c++) values[r][c]=Rational.of(rows[r][c],denominator); }
        return new RationalMatrix(values);
    }
    private FiniteDistribution<BigInteger> distribution(FiniteSet<BigInteger> states,Rational... values) {
        Map<BigInteger,Rational> masses=new LinkedHashMap<>(); int i=0;
        for(BigInteger state : states.members()) masses.put(state,values[i++]); return new FiniteDistribution<>(math.integers.algebra(),masses);
    }
    private FiniteDistribution<BigInteger> point(long state) { return distribution(set(state),Rational.ONE); }
    private FiniteMarkovKernel chain(RationalMatrix matrix) {
        List<BigInteger> states=new ArrayList<>(); for(int i=0;i<matrix.rows();i++) states.add(z(i));
        return math.markovKernels.fromMatrix(matrix,new FiniteSet<>(states),new FiniteSet<>(states));
    }
    private FiniteMarkovKernel two() { return chain(matrix(4,new long[]{2,2},new long[]{1,3})); }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static void undefined(Runnable action) { failure(MathFailure.Kind.OPERATION_UNDEFINED,action); }
    private static void invalid(Runnable action) { failure(MathFailure.Kind.INVALID_MEMBER,action); }
    private static void limit(Runnable action) { failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,action); }

    @Test public void labelsRowsAndBoundariesAreCanonicalAndValidated() {
        FiniteMarkovKernel kernel=math.markovKernels.fromMatrix(matrix(4,new long[]{1,3},new long[]{2,2}),set(7,-3),set(99,20));
        assertEquals(Arrays.asList(z(-3),z(7)),new ArrayList<>(kernel.domain().members()));
        assertEquals(Arrays.asList(z(20),z(99)),new ArrayList<>(kernel.codomain().members()));
        assertEquals(Rational.of(3,4),kernel.probability(z(-3),z(99)));
        assertEquals(matrix(4,new long[]{1,3},new long[]{2,2}),kernel.toMatrix()); assertFalse(kernel.isChain());
        Map<BigInteger,FiniteDistribution<BigInteger>> rows=new HashMap<>(kernel.rowMap());
        FiniteMarkovKernel copy=math.markovKernels.member(set(7,-3),set(99,20),rows); rows.clear();
        assertEquals(kernel,copy); assertEquals(kernel.hashCode(),copy.hashCode());
        assertThrows(UnsupportedOperationException.class,() -> copy.rowMap().clear());
        assertThrows(UnsupportedOperationException.class,() -> copy.rows().clear());
        invalid(() -> math.markovKernels.member(set(0),set(1),Collections.emptyMap()));
        invalid(() -> math.markovKernels.member(set(0),set(1),Collections.singletonMap(z(0),point(2))));
        FiniteDistribution<BigInteger> foreign=new FiniteDistribution<>(math.naturals.algebra(),Collections.singletonMap(z(1),Rational.ONE));
        invalid(() -> math.markovKernels.member(set(0),set(1),Collections.singletonMap(z(0),foreign)));
        undefined(() -> math.markovKernels.fromMatrix(matrix(2,new long[]{1,0}),set(0),set(1,2)));
        undefined(() -> math.markovKernels.fromMatrix(matrix(1,new long[]{-1,2}),set(0),set(1,2)));
        undefined(() -> math.markovKernels.fromMatrix(matrix(1,new long[]{1}),set(0,1),set(2)));
        undefined(() -> kernel.row(z(20))); undefined(() -> kernel.probability(z(-3),z(7)));
    }

    @Test public void compositionUsesTheRightKernelFirstAndRespectsExactMiddleBoundaries() {
        FiniteMarkovKernel first=math.markovKernels.fromMatrix(matrix(2,new long[]{1,1},new long[]{0,2},new long[]{2,0}),set(1,2,3),set(10,20));
        FiniteMarkovKernel second=math.markovKernels.fromMatrix(matrix(4,new long[]{1,3,0},new long[]{0,2,2}),set(20,10),set(100,200,300));
        FiniteMarkovKernel third=math.markovKernels.fromMatrix(matrix(1,new long[]{1},new long[]{1},new long[]{1}),set(100,200,300),set(-1));
        FiniteMarkovKernel composed=second.compose(first);
        assertEquals(matrix(8,new long[]{1,5,2},new long[]{0,4,4},new long[]{2,6,0}),composed.toMatrix());
        assertEquals(first.toMatrix().multiply(second.toMatrix()),composed.toMatrix());
        assertEquals(third.compose(second).compose(first),third.compose(second.compose(first)));
        assertEquals(first,first.compose(FiniteMarkovKernel.identity(math.integers.algebra(),first.domain())));
        assertEquals(first,FiniteMarkovKernel.identity(math.integers.algebra(),first.codomain()).compose(first));
        assertEquals(composed.apply(point(1)),second.apply(first.apply(point(1))));
        undefined(() -> first.compose(second));
        FiniteMarkovKernel padded=math.markovKernels.member(first.domain(),set(10,20,30),first.rowMap());
        undefined(() -> second.compose(padded)); assertNotEquals(first,padded);
    }

    @Test public void everyQuarterProbabilityTwoStateChainMatchesClosedFormStationarityAndHittingTimes() {
        for(int a=0;a<=4;a++) for(int b=0;b<=4;b++) {
            // P = [[1-a,a],[b,1-b]], with a,b in quarters.
            FiniteMarkovKernel kernel=chain(matrix(4,new long[]{4-a,a},new long[]{b,4-b}));
            assertEquals(a>0 && b>0,kernel.isIrreducible());
            assertEquals(a==0 && b==0?2:1,kernel.stationaryExtremes().size());
            if(a+b==0) undefined(kernel::stationary);
            else {
                FiniteDistribution<BigInteger> expected=distribution(set(0,1),Rational.of(b,a+b),Rational.of(a,a+b));
                assertEquals(expected,kernel.stationary()); assertTrue(kernel.isStationary(expected)); assertTrue(kernel.isReversible(expected));
                if(a>0 && b>0) assertEquals(kernel,kernel.reverse(expected)); else undefined(() -> kernel.reverse(expected));
            }
            assertEquals(vector(1,a==0?0:1,1),kernel.hittingProbabilities(set(1)));
            if(a==0) undefined(() -> kernel.meanHittingTimes(set(1)));
            else assertEquals(new RationalVector(Rational.of(4,a),Rational.ZERO),kernel.meanHittingTimes(set(1)));
            FiniteDistribution<BigInteger> initial=distribution(set(0,1),Rational.of(1,3),Rational.of(2,3));
            Rational first=Rational.of(4-a+2*b,12);
            assertEquals(distribution(set(0,1),first,Rational.ONE.subtract(first)),kernel.apply(initial));
        }
    }

    private static void enumeratePaths(RationalMatrix matrix,int current,int remaining,Rational weight,Rational[] totals) {
        if(remaining==0) { totals[current]=totals[current].add(weight); return; }
        for(int next=0;next<matrix.rows();next++) enumeratePaths(matrix,next,remaining-1,weight.multiply(matrix.get(current,next)),totals);
    }
    @Test public void powersAndOrbitsAgreeWithIndependentWeightedPathEnumeration() {
        // Every three-state row chooses one of three Dirac laws or a uniform law: 64 chains.
        for(int code=0;code<64;code++) {
            long[][] rows=new long[3][3]; int digits=code;
            for(int i=0;i<3;i++) { int choice=digits%4; digits/=4; if(choice==3) Arrays.fill(rows[i],1); else rows[i][choice]=3; }
            FiniteMarkovKernel kernel=chain(matrix(3,rows)); RationalMatrix transition=kernel.toMatrix();
            List<FiniteDistribution<BigInteger>> orbit=kernel.orbit(point(0),z(4)); assertEquals(5,orbit.size());
            for(int count=0;count<=4;count++) {
                FiniteMarkovKernel power=kernel.power(z(count));
                for(int source=0;source<3;source++) {
                    Rational[] totals={Rational.ZERO,Rational.ZERO,Rational.ZERO};
                    enumeratePaths(transition,source,count,Rational.ONE,totals);
                    FiniteDistribution<BigInteger> expected=distribution(set(0,1,2),totals);
                    assertEquals(expected,power.row(z(source))); if(source==0) assertEquals(expected,orbit.get(count));
                }
            }
        }
    }

    @Test public void recurrentClassesProduceAllExtremeStationaryDistributionsIncludingPeriodicOnes() {
        FiniteMarkovKernel kernel=chain(matrix(2,new long[]{0,1,0,1,0},new long[]{0,0,2,0,0},new long[]{0,2,0,0,0},
                new long[]{0,0,0,2,0},new long[]{2,0,0,0,0}));
        assertEquals(Arrays.asList(set(0),set(1,2),set(3),set(4)),kernel.communicatingClasses());
        assertEquals(Arrays.asList(set(1,2),set(3)),kernel.recurrentClasses()); assertEquals(set(0,4),kernel.transientStates());
        assertEquals(set(3),kernel.absorbingStates()); assertFalse(kernel.isIrreducible());
        FiniteDistribution<BigInteger> periodic=distribution(set(1,2),Rational.of(1,2),Rational.of(1,2));
        assertEquals(Arrays.asList(periodic,point(3)),kernel.stationaryExtremes()); undefined(kernel::stationary);
        FiniteDistribution<BigInteger> mixture=distribution(set(1,2,3),Rational.of(1,6),Rational.of(1,6),Rational.of(2,3));
        assertTrue(kernel.isStationary(mixture)); assertTrue(kernel.isReversible(mixture));
        assertNotEquals(kernel.apply(point(1)),kernel.power(z(2)).apply(point(1)));
        assertThrows(UnsupportedOperationException.class,() -> kernel.stationaryExtremes().clear());
    }

    @Test public void deterministicThreeStateGraphsHaveExactlyTheStationaryCycleLaws() {
        for(int code=0;code<27;code++) {
            int digits=code; int[] images=new int[3]; Map<BigInteger,BigInteger> mapping=new HashMap<>();
            for(int i=0;i<3;i++) { images[i]=digits%3; digits/=3; mapping.put(z(i),z(images[i])); }
            FiniteFunction<BigInteger,BigInteger> function=math.integerFunctions.member(set(0,1,2),set(0,1,2),mapping);
            FiniteMarkovKernel kernel=FiniteMarkovKernel.fromFunction(function); assertEquals(function,kernel.toFunction());
            Set<FiniteSet<BigInteger>> cycles=new HashSet<>();
            for(int start=0;start<3;start++) {
                List<Integer> path=new ArrayList<>(); int next=start;
                while(!path.contains(next)) { path.add(next); next=images[next]; }
                List<BigInteger> cycle=new ArrayList<>(); for(int i=path.indexOf(next);i<path.size();i++) cycle.add(z(path.get(i)));
                cycles.add(new FiniteSet<>(cycle));
            }
            assertEquals(cycles,new HashSet<>(kernel.recurrentClasses()));
            assertEquals(cycles.size(),kernel.stationaryExtremes().size());
            for(FiniteDistribution<BigInteger> stationary : kernel.stationaryExtremes()) {
                assertTrue(cycles.contains(new FiniteSet<>(stationary.masses().keySet()))); assertTrue(kernel.isStationary(stationary));
                for(Rational mass : stationary.masses().values()) assertEquals(Rational.of(1,stationary.masses().size()),mass);
            }
        }
    }

    @Test public void allThreeStateSupportGraphsAgreeWithSearchAndDirectedSpanningTrees() {
        // Each row has one of seven nonempty supports, so these are all 343 admissible graphs.
        for(int code=0;code<343;code++) {
            int digits=code; Rational[][] entries=new Rational[3][3];
            for(int i=0;i<3;i++) {
                int mask=1+digits%7; digits/=7;
                for(int j=0;j<3;j++) entries[i][j]=(mask&(1<<j))==0?Rational.ZERO:Rational.of(1,Integer.bitCount(mask));
            }
            FiniteMarkovKernel kernel=chain(new RationalMatrix(entries));
            List<Set<Integer>> reachable=new ArrayList<>();
            for(int start=0;start<3;start++) {
                Set<Integer> reached=new HashSet<>(); Deque<Integer> queue=new ArrayDeque<>(); reached.add(start); queue.add(start);
                while(!queue.isEmpty()) {
                    int state=queue.remove();
                    for(int next=0;next<3;next++) if(entries[state][next].signum()>0 && reached.add(next)) queue.add(next);
                }
                reachable.add(reached);
            }
            Set<FiniteSet<BigInteger>> components=new HashSet<>(),closed=new HashSet<>();
            for(int i=0;i<3;i++) {
                List<BigInteger> component=new ArrayList<>();
                for(int j=0;j<3;j++) if(reachable.get(i).contains(j) && reachable.get(j).contains(i)) component.add(z(j));
                FiniteSet<BigInteger> states=new FiniteSet<>(component); components.add(states);
                if(component.size()==reachable.get(i).size()) closed.add(states);
            }
            assertEquals(components,new HashSet<>(kernel.communicatingClasses()));
            assertEquals(closed,new HashSet<>(kernel.recurrentClasses()));
            assertEquals(closed.size(),kernel.stationaryExtremes().size());
            for(FiniteDistribution<BigInteger> stationary : kernel.stationaryExtremes()) {
                assertTrue(kernel.isStationary(stationary)); assertTrue(closed.contains(new FiniteSet<>(stationary.masses().keySet())));
            }
            if(components.size()==1) {
                // Three directed spanning trees toward each root; independent of linear-system solving.
                Rational[] weights=new Rational[3];
                for(int root=0;root<3;root++) {
                    int a=(root+1)%3,b=(root+2)%3;
                    weights[root]=entries[a][root].multiply(entries[b][root])
                            .add(entries[a][root].multiply(entries[b][a])).add(entries[a][b].multiply(entries[b][root]));
                }
                Rational total=weights[0].add(weights[1]).add(weights[2]);
                for(int i=0;i<3;i++) weights[i]=weights[i].divide(total);
                assertEquals(distribution(set(0,1,2),weights),kernel.stationary());
            }
        }
    }

    @Test public void fractionalHittingProbabilitiesSeparateNonarrivalFromInfiniteTime() {
        FiniteMarkovKernel kernel=chain(matrix(4,new long[]{2,1,1},new long[]{0,4,0},new long[]{0,0,4}));
        assertEquals(vector(2,1,2,0),kernel.hittingProbabilities(set(1)));
        undefined(() -> kernel.meanHittingTimes(set(1)));
        assertEquals(vector(1,1,1,1),kernel.hittingProbabilities(set(1,2)));
        assertEquals(vector(1,2,0,0),kernel.meanHittingTimes(set(1,2)));
        assertEquals(vector(1,0,0,0),kernel.hittingProbabilities(set()));
        undefined(() -> kernel.meanHittingTimes(set()));
        assertEquals(vector(1,1,1,1),kernel.hittingProbabilities(set(0,1,2)));
        assertEquals(vector(1,0,0,0),kernel.meanHittingTimes(set(0,1,2)));
        FiniteMarkovKernel madeAbsorbing=two().absorbingOn(set(1));
        assertEquals(matrix(2,new long[]{1,1},new long[]{0,2}),madeAbsorbing.toMatrix());
        assertEquals(point(1),madeAbsorbing.stationary()); assertEquals(set(1),madeAbsorbing.absorbingStates());
        assertEquals(vector(1,2,0),madeAbsorbing.meanHittingTimes(set(1)));
    }

    @Test public void gamblerRuinMatchesIndependentLinearAndQuadraticFormulas() {
        for(int last=2;last<=10;last++) {
            long[][] rows=new long[last+1][last+1]; rows[0][0]=2; rows[last][last]=2;
            for(int i=1;i<last;i++) { rows[i][i-1]=1; rows[i][i+1]=1; }
            FiniteMarkovKernel kernel=chain(matrix(2,rows));
            RationalVector chances=kernel.hittingProbabilities(set(last)),times=kernel.meanHittingTimes(set(0,last));
            for(int i=0;i<=last;i++) { assertEquals(Rational.of(i,last),chances.get(i)); assertEquals(Rational.of((long)i*(last-i)),times.get(i)); }
        }
    }

    @Test public void timeReversalTransposesStationaryFluxAndDoesNotAssumeReversibility() {
        FiniteMarkovKernel cycle=chain(matrix(1,new long[]{0,1,0},new long[]{0,0,1},new long[]{1,0,0}));
        FiniteDistribution<BigInteger> stationary=cycle.stationary();
        assertFalse(cycle.isReversible(stationary)); FiniteMarkovKernel reverse=cycle.reverse(stationary);
        assertEquals(cycle.toMatrix().transpose(),reverse.toMatrix()); assertEquals(cycle,reverse.reverse(stationary));
        assertTrue(reverse.isStationary(stationary));
        for(BigInteger i : cycle.domain().members()) for(BigInteger j : cycle.domain().members())
            assertEquals(stationary.masses().get(i).multiply(cycle.probability(i,j)),stationary.masses().get(j).multiply(reverse.probability(j,i)));
        assertFalse(two().isStationary(point(0))); assertFalse(two().isReversible(point(0))); undefined(() -> two().reverse(point(0)));
    }

    @Test public void rectangularAndEmptyKernelsKeepTheirDeclaredDomains() {
        FiniteMarkovKernel rectangle=math.markovKernels.fromMatrix(matrix(2,new long[]{1,1}),set(0),set(1,2));
        assertEquals(distribution(set(1,2),Rational.of(1,2),Rational.of(1,2)),rectangle.apply(point(0)));
        undefined(rectangle::stationary); undefined(rectangle::communicatingClasses); undefined(() -> rectangle.power(z(0)));
        undefined(() -> rectangle.orbit(point(0),z(0))); undefined(() -> rectangle.hittingProbabilities(set(1)));
        undefined(() -> rectangle.meanHittingTimes(set(1))); undefined(() -> rectangle.absorbingOn(set(1)));
        undefined(() -> rectangle.isStationary(point(0))); undefined(() -> rectangle.isReversible(point(0)));
        FiniteMarkovKernel empty=FiniteMarkovKernel.identity(math.integers.algebra(),set());
        assertTrue(empty.isChain()); assertTrue(empty.isDeterministic()); assertFalse(empty.isIrreducible());
        assertTrue(empty.rows().isEmpty()); assertTrue(empty.stationaryExtremes().isEmpty()); assertTrue(empty.recurrentClasses().isEmpty());
        assertEquals(vector(1),empty.hittingProbabilities(set())); assertEquals(vector(1),empty.meanHittingTimes(set()));
        assertEquals(empty,empty.power(z(10000))); assertEquals(empty,empty.compose(empty));
        assertEquals(FiniteFunction.identity(math.integers.algebra(),set()),empty.toFunction());
        undefined(empty::stationary); undefined(empty::toMatrix); undefined(() -> empty.apply(point(0)));
        FiniteMarkovKernel emptyToNonempty=math.markovKernels.member(set(),set(0,1),Collections.emptyMap());
        assertFalse(emptyToNonempty.isChain()); assertNotEquals(empty,emptyToNonempty); undefined(emptyToNonempty::toMatrix);
    }

    @Test public void carrierIdentityAndPartialDomainsAreCheckedBeforeExecution() {
        FiniteMarkovKernel kernel=two(); ConcreteMathematics other=new ConcreteMathematics();
        FiniteDistribution<BigInteger> foreign=new FiniteDistribution<>(other.integers.algebra(),Collections.singletonMap(z(0),Rational.ONE));
        undefined(() -> kernel.apply(foreign)); undefined(() -> kernel.isStationary(foreign)); undefined(() -> kernel.apply(point(99)));
        FiniteMarkovKernel otherKernel=other.markovKernels.fromMatrix(kernel.toMatrix(),set(0,1),set(0,1));
        assertNotEquals(kernel,otherKernel); undefined(() -> kernel.compose(otherKernel));
        assertNull(math.markovKernels.algebra().buildAlgebraItem(otherKernel));
        undefined(() -> kernel.absorbingOn(set(99))); undefined(() -> kernel.hittingProbabilities(set(99)));
        undefined(kernel::toFunction); undefined(() -> kernel.power(z(-1))); undefined(() -> kernel.orbit(point(0),z(-1)));
        undefined(() -> FiniteMarkovKernel.fromFunction(new FiniteFunction<>(math.integers.algebra(),math.naturals.algebra(),set(0),set(1),Collections.singletonMap(z(0),z(1)))));
    }

    @Test public void finiteResourceBudgetsAreSeparateFromMathematicalNonexistence() {
        List<BigInteger> labels=new ArrayList<>(); for(int i=0;i<65;i++) labels.add(z(i));
        limit(() -> FiniteMarkovKernel.identity(math.integers.algebra(),new FiniteSet<>(labels)));
        limit(() -> two().power(BigInteger.TEN.pow(100))); limit(() -> two().orbit(point(0),z(10001)));
        labels.remove(64); FiniteMarkovKernel identity=FiniteMarkovKernel.identity(math.integers.algebra(),new FiniteSet<>(labels));
        // Even sparse kernels share the conservative dense work budget across one power/orbit request.
        limit(() -> identity.power(z(8191))); limit(() -> identity.orbit(point(0),z(2000)));
        assertEquals(identity,identity.power(z(1))); assertEquals(64,identity.stationaryExtremes().size());
        assertEquals(10001,two().orbit(two().stationary(),z(10000)).size());
        Rational tiny=new Rational(BigInteger.ONE,BigInteger.TEN.pow(80));
        FiniteMarkovKernel slow=chain(new RationalMatrix(new Rational[][]{{Rational.ONE.subtract(tiny),tiny},{Rational.ZERO,Rational.ONE}}));
        assertEquals(new RationalVector(Rational.ONE.divide(tiny),Rational.ZERO),slow.meanHittingTimes(set(1)));
    }

    @Test public void nativeWrappersConnectKernelsDistributionsMatricesFunctionsAndVectors() {
        FiniteMarkovKernel kernel=two(); IAlgebraItem<FiniteMarkovKernel> item=math.markovKernels.algebra().buildAlgebraItem(kernel);
        assertSame(math.markovKernels.algebra(),math.mathTool.getAlgebra("FiniteMarkov(Z)"));
        IAlgebraItem<FiniteDistribution<BigInteger>> applied=item.performLeftProjectionOperation("apply",point(0));
        assertSame(math.integerProbabilities.algebra(),applied.getAlgebra()); assertEquals(kernel.row(z(0)),applied.getResult());
        IAlgebraItem<FiniteDistribution<BigInteger>> row=item.performUnsafeOperation("row",z(1));
        assertSame(math.integerProbabilities.algebra(),row.getAlgebra()); assertEquals(kernel.row(z(1)),row.getResult());
        IAlgebraItem<RationalMatrix> matrix=item.performAlgebraTransfer("to-matrix"); assertSame(math.rectangularMatrices.algebra(),matrix.getAlgebra());
        IAlgebraItem<FiniteMarkovKernel> restored=matrix.performUnsafeOperation("FiniteMarkov(Z).from-matrix",new Pair<>(set(0,1),set(0,1)));
        assertSame(math.markovKernels.algebra(),restored.getAlgebra()); assertEquals(kernel,restored.getResult());
        IAlgebraItem<RationalVector> hitting=item.performUnsafeOperation("mean-hitting-times",set(1));
        assertSame(math.finiteVectors.algebra(),hitting.getAlgebra()); assertEquals(vector(1,2,0),hitting.getResult());
        for(IAlgebraItem<FiniteDistribution<BigInteger>> stationary : item.<FiniteDistribution<BigInteger>>performAlgebraFlatTransfer("stationary-extremes"))
            assertSame(math.integerProbabilities.algebra(),stationary.getAlgebra());
        undefined(() -> item.performLeftProjectionOperation("apply",point(2)));
        IAlgebraItem<FiniteMarkovKernel> identity=math.integerSets.algebra().buildAlgebraItem(set(0,1)).performAlgebraTransfer("FiniteMarkov(Z).identity-on");
        IAlgebraItem<FiniteFunction<BigInteger,BigInteger>> function=identity.performAlgebraTransfer("to-function");
        assertSame(math.integerFunctions.algebra(),function.getAlgebra());
        assertEquals(identity.getResult(),function.<FiniteMarkovKernel>performAlgebraTransfer("FiniteMarkov(Z).from-function").getResult());
    }

    private static IAlgebraFlow<?> serialized(IAlgebraFlow<?> flow) throws Exception {
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { return (IAlgebraFlow<?>)in.readObject(); }
    }
    @Test public void serializedScalarAndFlatFlowsPreserveExactTransitionAndStationaryLaws() throws Exception {
        FiniteMarkovKernel kernel=two();
        IAlgebraFlow<Rational> flow=math.flow(math.markovKernels,Collections.singletonList(kernel))
                .<FiniteDistribution<BigInteger>,Pair<FiniteDistribution<BigInteger>,BigInteger>>performFlatAlgebraUnsafe("orbit",new Pair<>(point(0),z(2)))
                .<Rational>performAlgebraTransfer("expectation");
        IAlgebraFlow<?> restored=serialized(flow); assertEquals(Arrays.asList("0","1/2","5/8"),restored.collect()); assertEquals(restored.collect(),restored.collect());
        IAlgebraFlow<Rational> stationary=math.flow(math.markovKernels,Collections.singletonList(kernel))
                .<FiniteDistribution<BigInteger>>performAlgebraTransfer("stationary").<Rational>performAlgebraTransfer("expectation");
        assertEquals(Collections.singletonList("2/3"),serialized(stationary).collect());
        IAlgebraFlow<Rational> hitting=math.flow(math.markovKernels,Collections.singletonList(kernel))
                .<RationalVector,FiniteSet<BigInteger>>performAlgebraUnsafe("mean-hitting-times",set(1))
                .<Rational>performFlatAlgebraTransfer("entries");
        assertEquals(Arrays.asList("2","0"),serialized(hitting).collect());
        IAlgebraFlow<FiniteDistribution<BigInteger>> applied=math.flow(math.markovKernels,Collections.singletonList(kernel))
                .performCustomMemberOperation("power",z(2)).performLeftProjectionOperation("apply",point(0));
        assertEquals(Collections.singletonList("Distribution{0=3/8, 1=5/8}"),serialized(applied).collect());
    }
}
