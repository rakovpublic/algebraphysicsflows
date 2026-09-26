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

public class NativeSimplicialHomotopyPathTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static IntegerVector v(long... values) { BigInteger[] result=new BigInteger[values.length]; for(int i=0;i<values.length;i++) result[i]=z(values[i]); return new IntegerVector(result); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int vertex : facet) labels.add(vertex); faces.add(new FiniteSet<>(labels)); } return new FiniteSimplicialComplex(faces);
    }
    private static FiniteSimplicialComplex simplex(int count) { int[] labels=new int[count]; for(int i=0;i<count;i++) labels[i]=i; return complex(labels); }
    private static FiniteSimplicialMap map(FiniteSimplicialComplex source,FiniteSimplicialComplex target,int... images) {
        Map<BigInteger,BigInteger> values=new TreeMap<>(); int i=0; for(BigInteger vertex : FiniteSimplicialMap.vertexSet(source).members()) values.put(vertex,z(images[i++]));
        return new FiniteSimplicialMap(source,target,values);
    }
    private static RelativeSimplicialMap absolute(FiniteSimplicialMap map) { return RelativeSimplicialMap.absolute(map); }
    private static SimplicialHomotopyPath walk(FiniteSimplicialComplex target,int... vertices) {
        List<RelativeSimplicialMap> stages=new ArrayList<>(); for(int vertex : vertices) stages.add(absolute(map(complex(new int[]{-1}),target,vertex))); return new SimplicialHomotopyPath(stages);
    }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static void identities(SimplicialHomotopyPath path,int degree) {
        BigInteger k=z(degree); IntegerMatrix d=path.chainMatrix(k),before=degree==0?IntegerMatrix.zero(path.target().simplexCount(z(0)),0):path.chainMatrix(k.subtract(BigInteger.ONE));
        IntegerMatrix difference=path.to().chainMatrix(k).add(path.from().chainMatrix(k).scale(z(-1)));
        assertEquals(difference,path.target().boundaryMatrix(k.add(BigInteger.ONE)).multiply(d).add(before.multiply(path.source().boundaryMatrix(k))));
        IntegerMatrix lower=degree==0?IntegerMatrix.zero(path.source().simplexCount(k),0):path.source().boundaryMatrix(k).transpose();
        assertEquals(difference.transpose(),lower.multiply(path.cochainMatrix(k)).add(path.cochainMatrix(k.add(BigInteger.ONE)).multiply(path.target().boundaryMatrix(k.add(BigInteger.ONE)).transpose())));
    }
    @Test public void all178FiveStageWalksOnTheFourVertexIntervalMatchIndependentSignedEdgeCounts() {
        FiniteSimplicialComplex line=complex(new int[]{0,1},new int[]{1,2},new int[]{2,3}); int checked=0;
        for(int code=0;code<1024;code++) {
            int n=code; int[] vertices=new int[5]; for(int i=0;i<5;i++) { vertices[i]=n%4; n/=4; }
            boolean valid=true; long[] counts=new long[3]; for(int i=1;i<5;i++) {
                int a=vertices[i-1],b=vertices[i]; if(Math.abs(a-b)>1) valid=false;
                else if(a!=b) counts[Math.min(a,b)]+=b>a?1:-1;
            }
            if(!valid) continue; checked++; SimplicialHomotopyPath path=walk(line,vertices); identities(path,0); identities(path,1);
            assertEquals(v(counts),path.chainMatrix(z(0)).column(0));
            SimplicialCochain differential=new SimplicialCochain(line,z(0),v(0,1,4,9)).coboundary();
            assertEquals(v(vertices[4]*vertices[4]-vertices[0]*vertices[0]),path.onAbsoluteCochain(differential).coordinates());
        }
        assertEquals(178,checked);
    }
    @Test public void intermediateMapsConnectNoncontiguousEndpointsAndGiveExplicitFillings() {
        FiniteSimplicialComplex line=complex(new int[]{0,1},new int[]{1,2}); SimplicialHomotopyPath path=walk(line,0,1,2);
        assertFalse(path.from().contiguous(path.to())); assertEquals(z(2),path.stepCount());
        SimplicialChain cycle=new SimplicialChain(path.source().ambient(),z(0),v(3)),filling=path.onAbsoluteChain(cycle);
        assertEquals(v(3,3),filling.coordinates()); assertEquals(v(-3,0,3),filling.boundary().coordinates());
        assertEquals(path.from().homologyMap(z(0)),path.to().homologyMap(z(0)));
        assertEquals(RelativeSimplicialCochain.cohomologyMap(path.from(),z(0)),RelativeSimplicialCochain.cohomologyMap(path.to(),z(0)));
    }
    @Test public void closedContiguityPathsRetainNonzeroIntegralCyclesRatherThanOnlyTheirEndpoints() {
        FiniteSimplicialComplex circle=complex(new int[]{0,1},new int[]{0,2},new int[]{1,2}); SimplicialHomotopyPath loop=walk(circle,0,1,2,0);
        assertEquals(loop.from(),loop.to()); assertNotEquals(loop,SimplicialHomotopyPath.stationary(loop.from()));
        SimplicialChain cycle=loop.onAbsoluteChain(new SimplicialChain(loop.source().ambient(),z(0),v(1)));
        assertEquals(v(1,-1,1),cycle.coordinates()); assertTrue(cycle.isCycle()); assertFalse(cycle.isBoundary());
        assertEquals(IntegerMatrix.zero(3,1),loop.then(loop.reverse()).chainMatrix(z(0)));
    }
    @Test public void concatenationIsChronologicalAssociativeAndUnitalAndStageListsAreImmutable() {
        FiniteSimplicialComplex line=complex(new int[]{0,1},new int[]{1,2},new int[]{2,3}); SimplicialHomotopyPath a=walk(line,0,1),b=walk(line,1,2),c=walk(line,2,3);
        assertEquals(walk(line,0,1,2,3),a.then(b).then(c)); assertEquals(a.then(b.then(c)),a.then(b).then(c));
        assertEquals(a,SimplicialHomotopyPath.stationary(a.from()).then(a)); assertEquals(a,a.then(SimplicialHomotopyPath.stationary(a.to())));
        assertEquals(a.then(b),a.append(b.to())); assertEquals(a,SimplicialHomotopyPath.fromHomotopy(a.steps().get(0)));
        assertEquals(a,a.reverse().reverse()); assertEquals(a.hashCode(),a.reverse().reverse().hashCode());
        List<RelativeSimplicialMap> values=new ArrayList<>(a.stages()); SimplicialHomotopyPath copy=new SimplicialHomotopyPath(values); values.clear(); assertEquals(a,copy);
        assertNotEquals(a,a.append(a.to())); assertEquals(a.chainMatrix(z(0)),a.append(a.to()).chainMatrix(z(0)));
        assertThrows(UnsupportedOperationException.class,() -> a.stages().clear()); assertThrows(UnsupportedOperationException.class,() -> a.steps().clear());
        assertThrows(UnsupportedOperationException.class,() -> a.chainMatrices().clear()); assertThrows(UnsupportedOperationException.class,() -> a.cochainMatrices().clear());
    }
    @Test public void all729TrianglePathsThroughAConstantMapSatisfyBothTelescopingIdentities() {
        FiniteSimplicialComplex triangle=simplex(3); List<RelativeSimplicialMap> maps=new ArrayList<>();
        for(int a=0;a<3;a++) for(int b=0;b<3;b++) for(int c=0;c<3;c++) maps.add(absolute(map(triangle,triangle,a,b,c)));
        RelativeSimplicialMap middle=maps.get(0);
        for(RelativeSimplicialMap from : maps) for(RelativeSimplicialMap to : maps) {
            SimplicialHomotopyPath path=new SimplicialHomotopyPath(Arrays.asList(from,middle,to));
            for(int k=0;k<=2;k++) {
                identities(path,k); assertEquals(path.steps().get(0).chainMatrix(z(k)).add(path.steps().get(1).chainMatrix(z(k))),path.chainMatrix(z(k)));
            }
        }
    }
    @Test public void all324RelativeTrianglePathsRespectBothQuotientContexts() {
        FiniteSimplicialComplex triangle=simplex(3); RelativeSimplicialComplex source=new RelativeSimplicialComplex(triangle,simplex(1)),target=new RelativeSimplicialComplex(triangle,simplex(2));
        List<RelativeSimplicialMap> maps=new ArrayList<>();
        for(int a=0;a<2;a++) for(int b=0;b<3;b++) for(int c=0;c<3;c++) maps.add(new RelativeSimplicialMap(source,target,map(triangle,triangle,a,b,c)));
        for(RelativeSimplicialMap from : maps) for(RelativeSimplicialMap to : maps) {
            SimplicialHomotopyPath path=new SimplicialHomotopyPath(Arrays.asList(from,maps.get(0),to));
            for(int k=0;k<=2;k++) identities(path,k);
        }
    }
    @Test public void precompositionRecomputesPrismsWhilePostcompositionAgreesWithChainPushforward() {
        FiniteSimplicialComplex edge=simplex(2),tetrahedron=simplex(4);
        SimplicialHomotopyPath path=new SimplicialHomotopyPath(Arrays.asList(absolute(map(edge,tetrahedron,0,1)),absolute(map(edge,tetrahedron,2,3))));
        RelativeSimplicialMap swap=absolute(map(edge,edge,1,0)),permutation=absolute(map(tetrahedron,tetrahedron,3,1,2,0));
        SimplicialHomotopyPath pre=path.precompose(swap),post=path.postcompose(permutation);
        assertEquals(path,pre.precompose(swap)); assertEquals(path,post.postcompose(permutation));
        assertEquals(path.from().compose(swap),pre.from()); assertEquals(permutation.compose(path.to()),post.to());
        assertNotEquals(path.chainMatrix(z(1)).multiply(swap.chainMatrix(z(1))),pre.chainMatrix(z(1)));
        for(int k=0;k<=3;k++) { identities(pre,k); identities(post,k); assertEquals(permutation.chainMatrix(z(k+1)).multiply(path.chainMatrix(z(k))),post.chainMatrix(z(k))); }
        assertNotEquals(path.chainMatrix(z(1)).scale(z(-1)),path.reverse().chainMatrix(z(1))); identities(path.reverse(),1);
        assertEquals(path.then(path.reverse()).precompose(swap),pre.then(pre.reverse()));
    }
    @Test public void typedRelativeCycleAndCocycleActionsRetainChangedPairsAndDegrees() {
        FiniteSimplicialComplex edge=simplex(2),triangle=simplex(3); RelativeSimplicialComplex source=new RelativeSimplicialComplex(edge,simplex(1)),target=new RelativeSimplicialComplex(triangle,edge);
        RelativeSimplicialMap f=new RelativeSimplicialMap(source,target,map(edge,triangle,0,2)),g=new RelativeSimplicialMap(source,target,map(edge,triangle,1,1)),h=new RelativeSimplicialMap(source,target,map(edge,triangle,0,0));
        SimplicialHomotopyPath path=new SimplicialHomotopyPath(Arrays.asList(f,g,h)); RelativeSimplicialChain cycle=new RelativeSimplicialChain(source,z(0),v(3));
        assertEquals(cycle.pushforward(h).subtract(cycle.pushforward(f)),path.onChain(cycle).boundary()); assertEquals(target,path.onChain(cycle).pair());
        RelativeSimplicialCochain cocycle=new RelativeSimplicialCochain(target,z(1),v(5,5));
        assertEquals(cocycle.pullback(h).subtract(cocycle.pullback(f)),path.onCochain(cocycle).coboundary()); assertEquals(source,path.onCochain(cocycle).pair());
        assertEquals(path.onChain(cycle).coordinates().dot(cocycle.coordinates()),cycle.coordinates().dot(path.onCochain(cocycle).coordinates()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> path.onAbsoluteChain(SimplicialChain.zero(edge,z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> path.onAbsoluteCochain(SimplicialCochain.zero(triangle,z(1))));
    }
    @Test public void invalidStagesFullJoiningMapsAndWrongActionContextsAreRejected() {
        FiniteSimplicialComplex line=complex(new int[]{0,1},new int[]{1,2}); SimplicialHomotopyPath path=walk(line,0,1,2);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialHomotopyPath(Collections.emptyList()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> walk(line,0,2)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> path.then(path));
        RelativeSimplicialMap differentPair=RelativeSimplicialMap.diagonal(path.from().ambientMap());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> path.append(differentPair));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> path.precompose(RelativeSimplicialMap.identity(path.target())));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> path.postcompose(RelativeSimplicialMap.identity(path.source())));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> path.onChain(RelativeSimplicialChain.zero(path.target(),z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> path.onCochain(RelativeSimplicialCochain.zero(path.source(),z(1))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> path.onCochain(RelativeSimplicialCochain.zero(path.target(),z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> path.chainMatrix(z(-1))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> path.cochainMatrix(z(-1)));
    }
    @Test public void stationaryEmptyNegativeHugeAndExtremeLabelCasesRetainTheirExactShapes() {
        FiniteSimplicialComplex edge=complex(new int[]{Integer.MIN_VALUE,Integer.MAX_VALUE}); SimplicialHomotopyPath path=walk(edge,Integer.MAX_VALUE,Integer.MIN_VALUE);
        assertEquals(v(-1),path.chainMatrix(z(0)).column(0));
        SimplicialHomotopyPath stationary=SimplicialHomotopyPath.stationary(path.from()); assertEquals(z(0),stationary.stepCount()); assertTrue(stationary.steps().isEmpty());
        assertEquals(IntegerMatrix.zero(1,1),stationary.chainMatrix(z(0))); assertEquals(IntegerMatrix.zero(0,2),stationary.cochainMatrix(z(0)));
        assertEquals(SimplicialChain.zero(edge,z(0)),path.onAbsoluteChain(SimplicialChain.zero(path.source().ambient(),z(-1))));
        assertEquals(SimplicialChain.zero(edge,z(-1)),path.onAbsoluteChain(SimplicialChain.zero(path.source().ambient(),z(-2))));
        assertEquals(IntegerMatrix.zero(0,0),path.chainMatrix(BigInteger.TEN.pow(100))); assertEquals(IntegerMatrix.zero(0,0),path.cochainMatrix(BigInteger.TEN.pow(100)));
        SimplicialHomotopyPath empty=SimplicialHomotopyPath.stationary(absolute(FiniteSimplicialMap.identity(complex())));
        assertTrue(empty.chainMatrices().isEmpty()); assertEquals(Collections.singletonList(IntegerMatrix.zero(0,0)),empty.cochainMatrices()); identities(empty,0);
    }
    @Test public void stageCapsAndFilteredBasisBoundsRemainDistinctFromUndefinedOperations() {
        SimplicialHomotopyPath point=walk(simplex(1),0); RelativeSimplicialMap f=point.from(); SimplicialHomotopyPath maximum=new SimplicialHomotopyPath(Collections.nCopies(256,f)); assertEquals(z(255),maximum.stepCount());
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> maximum.append(f)); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> maximum.then(maximum));
        List<FiniteSet<Integer>> points=new ArrayList<>(); for(int i=0;i<300;i++) points.add(FiniteSet.of(i)); FiniteSimplicialComplex x=new FiniteSimplicialComplex(points);
        RelativeSimplicialComplex pair=new RelativeSimplicialComplex(x,new FiniteSimplicialComplex(points.subList(0,299))); RelativeSimplicialMap id=RelativeSimplicialMap.identity(pair);
        SimplicialHomotopyPath filtered=new SimplicialHomotopyPath(Arrays.asList(id,id)); assertEquals(IntegerMatrix.zero(0,1),filtered.chainMatrix(z(0))); identities(filtered,0);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialHomotopyPath.stationary(absolute(id.ambientMap())).chainMatrix(z(0)));
    }
    @Test public void constructionAndComposedStagesShareTheWholeValidationBudget() {
        RelativeSimplicialMap f=RelativeSimplicialMap.identity(RelativeSimplicialComplex.diagonal(simplex(10)));
        assertNotNull(new SimplicialHomotopy(f,f));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> new SimplicialHomotopyPath(Collections.nCopies(256,f)));
        SimplicialHomotopyPath path=new SimplicialHomotopyPath(Collections.nCopies(201,f)); assertEquals(z(200),path.stepCount());
        assertNotNull(f.compose(f)); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> path.precompose(f)); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> path.postcompose(f));
    }
    @Test public void entireMatrixListsAndIndividualPathMatricesShareTheBudgetAcrossAllSteps() {
        RelativeSimplicialMap f=absolute(FiniteSimplicialMap.identity(simplex(9))); SimplicialHomotopyPath path=new SimplicialHomotopyPath(Collections.nCopies(51,f));
        for(int k=0;k<=8;k++) assertNotNull(path.chainMatrix(z(k))); for(int k=0;k<=9;k++) assertNotNull(path.cochainMatrix(z(k)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,path::chainMatrices); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,path::cochainMatrices);
        SimplicialHomotopyPath longPath=new SimplicialHomotopyPath(Collections.nCopies(256,f)); assertNotNull(longPath.steps().get(0).chainMatrix(z(4)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> longPath.chainMatrix(z(4)));
    }
    @Test public void nativeOperationsPreserveOriginalWrappersFlatStagesAndSerializedExecution() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialHomotopyPath path=walk(complex(new int[]{0,1},new int[]{1,2}),0,1,2);
        IAlgebraItem<SimplicialHomotopyPath> item=math.relativeMaps.algebra().buildAlgebraItem(path.from()).performAlgebraTransfer("HomotopyPath.stationary-on");
        assertSame(math.homotopyPaths.algebra(),item.getAlgebra());
        item=item.performCustomMemberOperation("append",path.stages().get(1)).performCustomMemberOperation("append",path.to()); assertEquals(path,item.perform().getResult());
        IAlgebraItem<RelativeSimplicialChain> chain=item.performLeftProjectionOperation("on-chain",new RelativeSimplicialChain(path.source(),z(0),v(3))); assertSame(math.relativeChains.algebra(),chain.getAlgebra()); assertEquals(v(3,3),chain.perform().getResult().coordinates());
        IAlgebraItem<SimplicialCochain> cochain=item.performLeftProjectionOperation("on-absolute-cochain",new SimplicialCochain(path.target().ambient(),z(1),v(2,5))); assertSame(math.cochains.algebra(),cochain.getAlgebra()); assertEquals(v(7),cochain.perform().getResult().coordinates());
        for(IAlgebraItem<RelativeSimplicialMap> stage : item.<RelativeSimplicialMap>performAlgebraFlatTransfer("stages")) assertSame(math.relativeMaps.algebra(),stage.getAlgebra());
        for(IAlgebraItem<SimplicialHomotopy> step : item.<SimplicialHomotopy>performAlgebraFlatTransfer("steps")) assertSame(math.simplicialHomotopies.algebra(),step.getAlgebra());
        assertEquals(Arrays.asList("1","1"),math.flow(math.homotopyPaths,Collections.singletonList(path)).<SimplicialHomotopy>performFlatAlgebraTransfer("steps")
                .<SimplicialHomotopyPath>performAlgebraTransfer("HomotopyPath.from-homotopy").<BigInteger>performAlgebraTransfer("step-count").collect());
        IAlgebraFlow<IntegerVector> flow=math.flow(math.relativeMaps,Collections.singletonList(path.from())).<SimplicialHomotopyPath>performAlgebraTransfer("HomotopyPath.stationary-on")
                .performCustomMemberOperation("append",path.stages().get(1)).performCustomMemberOperation("append",path.to())
                .performLeftProjectionOperation("on-absolute-chain",new SimplicialChain(path.source().ambient(),z(0),v(3))).performOneOperandOperation("boundary").<IntegerVector>performAlgebraTransfer("coordinates");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("[-3, 0, 3]"),restored.collect()); assertEquals(restored.collect(),restored.collect());
    }
}
