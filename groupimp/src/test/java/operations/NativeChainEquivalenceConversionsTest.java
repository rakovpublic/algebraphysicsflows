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

public class NativeChainEquivalenceConversionsTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int v : facet) labels.add(v); faces.add(new FiniteSet<>(labels)); } return new FiniteSimplicialComplex(faces);
    }
    private static RelativeSimplicialComplex abs(FiniteSimplicialComplex c) { return RelativeSimplicialComplex.absolute(c); }
    private static FiniteSimplicialComplex simplex(int n) { int[] f=new int[n]; for(int i=0;i<n;i++) f[i]=i; return n==0?complex():complex(f); }
    private static RelativeSimplicialComplex points(int n) { int[][] f=new int[n][1]; for(int i=0;i<n;i++) f[i][0]=i; return abs(complex(f)); }
    private static FiniteSimplicialComplex line(int edges) { int[][] f=new int[edges][2]; for(int k=0;k<edges;k++) f[k]=new int[]{k,k+1}; return complex(f); }
    private static RelativeSimplicialMap map(RelativeSimplicialComplex source,RelativeSimplicialComplex target,int... images) {
        Map<BigInteger,BigInteger> vertices=new TreeMap<>(); for(int i=0;i<images.length;i++) vertices.put(z(i),z(images[i]));
        return new RelativeSimplicialMap(source,target,new FiniteSimplicialMap(source.ambient(),target.ambient(),vertices));
    }
    private static SimplicialHomotopyEquivalence contraction(int edges,boolean relative) {
        FiniteSimplicialComplex a=relative?simplex(1):complex(); RelativeSimplicialComplex source=new RelativeSimplicialComplex(line(edges),a),target=new RelativeSimplicialComplex(simplex(1),a);
        RelativeSimplicialMap f=map(source,target,new int[edges+1]),g=RelativeSimplicialMap.inclusion(target,source); List<RelativeSimplicialMap> stages=new ArrayList<>();
        for(int cutoff=edges;cutoff>=0;cutoff--) { int[] images=new int[edges+1]; for(int v=0;v<=edges;v++) images[v]=Math.min(v,cutoff); stages.add(map(source,source,images)); }
        return new SimplicialHomotopyEquivalence(f,g,new SimplicialHomotopyPath(stages),SimplicialHomotopyPath.stationary(RelativeSimplicialMap.identity(target)));
    }
    private static MathFailure failure(MathFailure.Kind kind,Runnable action) { MathFailure e=assertThrows(MathFailure.class,action::run); assertEquals(kind,e.kind()); return e; }
    private static void identities(SimplicialChainEquivalence e) {
        assertEquals(e.backward().compose(e.forward()),e.sourceHomotopy().from()); assertEquals(e.forward().compose(e.backward()),e.targetHomotopy().from());
        for(SimplicialChainHomotopy h : e.homotopies()) {
            assertEquals(SimplicialChainMap.identity(h.source()),h.to());
            for(int d=0;d<=h.source().ambient().dimension();d++) {
                IntegerMatrix actual=h.target().boundaryMatrix(z(d+1)).multiply(h.chainMatrix(z(d)))
                        .add(h.cochainMatrix(z(d)).transpose().multiply(h.source().boundaryMatrix(z(d))));
                assertEquals(h.to().chainMatrix(z(d)).add(h.from().chainMatrix(z(d)).scale(z(-1))),actual);
            }
        }
    }
    private static void inverseMaps(SimplicialChainEquivalence e,int degree) {
        for(List<AbelianGroupHomomorphism> maps : Arrays.asList(e.homologyMaps(z(degree)),e.cohomologyMaps(z(degree)))) {
            assertEquals(maps.get(0).inverse(),maps.get(1)); assertEquals(AbelianGroupHomomorphism.identity(maps.get(0).source()),maps.get(1).compose(maps.get(0)));
        }
    }
    @Test public void all625PointMatricesUseStrictIntegralInversesAndStationaryWitnesses() {
        for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) for(int c=-2;c<=2;c++) for(int d=-2;d<=2;d++) {
            int det=a*d-b*c; IntegerMatrix m=new IntegerMatrix(new BigInteger[][]{{z(a),z(b)},{z(c),z(d)}});
            SimplicialChainMap f=new SimplicialChainMap(points(2),points(2),Collections.singletonList(m));
            if(Math.abs(det)!=1) { failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialChainEquivalence.fromIsomorphism(f)); continue; }
            SimplicialChainEquivalence e=SimplicialChainEquivalence.fromIsomorphism(f);
            assertEquals(new IntegerMatrix(new BigInteger[][]{{z(d/det),z(-b/det)},{z(-c/det),z(a/det)}}),e.backward().chainMatrix(z(0)));
            assertEquals(SimplicialChainHomotopy.stationary(SimplicialChainMap.identity(points(2))),e.sourceHomotopy()); identities(e);
        }
        SimplicialChainMap singular=SimplicialChainMap.fromSimplicial(contraction(1,false).forward());
        assertNotNull(SimplicialChainEquivalence.fromMap(singular)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialChainEquivalence.fromIsomorphism(singular));
    }
    @Test public void intervalPathsConvertToPositiveIndependentEdgeFillingsWithRelativeProjection() {
        for(int edges=1;edges<=8;edges++) for(boolean relative : new boolean[]{false,true}) {
            SimplicialHomotopyEquivalence geometric=contraction(edges,relative); SimplicialChainEquivalence e=SimplicialChainEquivalence.fromHomotopyEquivalence(geometric);
            IntegerMatrix h=e.sourceHomotopy().chainMatrix(z(0));
            for(int r=0;r<edges;r++) for(int c=0;c<h.columns();c++) assertEquals(z(r<c+(relative?1:0)?1:0),h.get(r,c));
            assertEquals(SimplicialChainMap.fromSimplicial(geometric.forward()),e.forward()); assertEquals(SimplicialChainMap.fromSimplicial(geometric.backward()),e.backward());
            assertEquals(geometric.sourceHomotopy().chainMatrix(z(0)).scale(z(-1)),h); identities(e); inverseMaps(e,0);
            assertEquals(e.inverse(),SimplicialChainEquivalence.fromHomotopyEquivalence(geometric.inverse()));
        }
    }
    private static SimplicialHomotopyEquivalence loopEquivalence() {
        RelativeSimplicialComplex triangle=abs(simplex(3)); RelativeSimplicialMap id=RelativeSimplicialMap.identity(triangle),a=map(triangle,triangle,0,0,0),b=map(triangle,triangle,1,1,1),c=map(triangle,triangle,2,2,2);
        SimplicialHomotopyPath loop=new SimplicialHomotopyPath(Arrays.asList(id,a,b,c,a,id));
        return new SimplicialHomotopyEquivalence(id,id,loop,loop.then(loop));
    }
    @Test public void conversionNegatesActualPrismsAndPreservesNonzeroClosedChoices() {
        SimplicialHomotopyEquivalence geometric=loopEquivalence(); SimplicialChainEquivalence e=SimplicialChainEquivalence.fromHomotopyEquivalence(geometric);
        assertNotEquals(SimplicialChainEquivalence.identity(e.source()),e);
        for(int d=0;d<=2;d++) {
            assertEquals(geometric.sourceHomotopy().chainMatrix(z(d)).scale(z(-1)),e.sourceHomotopy().chainMatrix(z(d)));
            assertEquals(e.sourceHomotopy().chainMatrix(z(d)).scale(z(2)),e.targetHomotopy().chainMatrix(z(d)));
        }
        for(int c=0;c<3;c++) { assertEquals(z(-1),e.sourceHomotopy().chainMatrix(z(0)).get(0,c)); assertEquals(z(1),e.sourceHomotopy().chainMatrix(z(0)).get(1,c)); assertEquals(z(-1),e.sourceHomotopy().chainMatrix(z(0)).get(2,c)); }
        identities(e);
    }
    @Test public void everySimplexFacetThroughDimensionFiveHasItsOrientedElementaryWitness() {
        for(int vertices=2;vertices<=6;vertices++) for(int omitted=0;omitted<vertices;omitted++) {
            List<BigInteger> labels=new ArrayList<>(); for(int v=0;v<vertices;v++) if(v!=omitted) labels.add(z(v));
            SimplicialCollapse collapse=new SimplicialCollapse(abs(simplex(vertices)),new FiniteSet<>(labels)); SimplicialChainEquivalence e=SimplicialChainEquivalence.fromCollapse(collapse);
            IntegerMatrix h=e.sourceHomotopy().chainMatrix(z(vertices-2)),boundary=collapse.source().boundaryMatrix(z(vertices-1)); int nonzero=0;
            for(int c=0;c<h.columns();c++) if(h.get(0,c).signum()!=0) { nonzero++; assertEquals(z((omitted&1)==0?1:-1),h.get(0,c)); assertEquals(boundary.get(c,0),h.get(0,c)); }
            assertEquals(1,nonzero); assertEquals(SimplicialChainHomotopy.fromCollapse(collapse),e.sourceHomotopy());
            assertEquals(SimplicialChainHomotopy.stationary(SimplicialChainMap.identity(collapse.target())),e.targetHomotopy()); identities(e);
        }
    }
    @Test public void collapseSequenceConversionEqualsChronologicalCompositionOfItsSteps() {
        for(int vertices=2;vertices<=4;vertices++) {
            SimplicialCollapseSequence sequence=SimplicialCollapseSequence.reduceAbsolute(simplex(vertices));
            SimplicialChainEquivalence composed=SimplicialChainEquivalence.identity(sequence.source());
            for(SimplicialCollapse step : sequence.steps()) composed=SimplicialChainEquivalence.fromCollapse(step).compose(composed);
            SimplicialChainEquivalence e=SimplicialChainEquivalence.fromCollapseSequence(sequence); assertEquals(composed,e); identities(e); inverseMaps(e,0);
            assertEquals(SimplicialChainHomotopy.fromCollapseSequence(sequence),e.sourceHomotopy());
        }
        RelativeSimplicialComplex circle=abs(complex(new int[]{0,1},new int[]{0,2},new int[]{1,2}));
        assertEquals(SimplicialChainEquivalence.identity(circle),SimplicialChainEquivalence.fromCollapseSequence(SimplicialCollapseSequence.identity(circle)));
    }
    private static List<FiniteSimplicialComplex> allThreeLabelComplexes() {
        Set<FiniteSimplicialComplex> values=new LinkedHashSet<>();
        for(int mask=0;mask<128;mask++) { List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int face=1;face<8;face++) if((mask&(1<<(face-1)))!=0) { List<Integer> vertices=new ArrayList<>(); for(int v=0;v<3;v++) if((face&(1<<v))!=0) vertices.add(v); faces.add(new FiniteSet<>(vertices)); } values.add(new FiniteSimplicialComplex(faces)); }
        return new ArrayList<>(values);
    }
    @Test public void all148ThreeLabelPairsRetainBothSubdivisionIdentitiesAndZeroSourceWitness() {
        int count=0; List<FiniteSimplicialComplex> complexes=allThreeLabelComplexes();
        for(FiniteSimplicialComplex x : complexes) for(FiniteSimplicialComplex a : complexes) if(a.subcomplexOf(x)) {
            SimplicialSubdivision sd=new SimplicialSubdivision(new RelativeSimplicialComplex(x,a)); SimplicialChainEquivalence e=SimplicialChainEquivalence.fromSubdivision(sd); count++;
            assertEquals(sd.original(),e.source()); assertEquals(sd.subdivided(),e.target()); assertTrue(e.backward().compose(e.forward()).isIdentity());
            assertEquals(SimplicialChainHomotopy.stationary(SimplicialChainMap.identity(sd.original())),e.sourceHomotopy());
            assertEquals(sd.chainMatrices(),e.forward().chainMatrices()); assertEquals(SimplicialChainHomotopy.fromSubdivision(sd),e.targetHomotopy()); identities(e);
        }
        assertEquals(148,count);
    }
    @Test public void strictRelabellingsPreserveExtremeLabelsAndUnboundedCoefficients() {
        RelativeSimplicialComplex target=abs(complex(new int[]{Integer.MIN_VALUE},new int[]{Integer.MAX_VALUE})); BigInteger huge=BigInteger.ONE.shiftLeft(1024);
        SimplicialChainMap f=new SimplicialChainMap(points(2),target,Collections.singletonList(new IntegerMatrix(new BigInteger[][]{{z(1),huge},{z(0),z(1)}})));
        SimplicialChainEquivalence e=SimplicialChainEquivalence.fromIsomorphism(f); assertEquals(target,e.target()); assertEquals(huge.negate(),e.backward().chainMatrix(z(0)).get(0,1)); identities(e); inverseMaps(e,0);
    }
    @Test public void collapsedProjectivePlaneLeafRetainsIntegralTorsionAndInverseMaps() {
        FiniteSimplicialComplex plane=complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        SimplicialCollapse collapse=new SimplicialCollapse(abs(plane.union(complex(new int[]{5,6}))),FiniteSet.of(z(6)));
        SimplicialChainEquivalence e=SimplicialChainEquivalence.fromCollapse(collapse); assertEquals(abs(plane),e.target()); assertEquals(AbelianGroupType.cyclic(z(2)),e.source().homologyType(z(1))); identities(e);
        inverseMaps(e,1); inverseMaps(e,2);
    }
    @Test public void bothTypedChainAndCochainActionsSatisfyFullDifferentialIdentities() {
        SimplicialChainEquivalence e=SimplicialChainEquivalence.fromHomotopyEquivalence(loopEquivalence());
        for(int a=-1;a<=1;a++) for(int b=-1;b<=1;b++) for(int c=-1;c<=1;c++) for(int degree=0;degree<=1;degree++) {
            RelativeSimplicialChain chain=new RelativeSimplicialChain(e.source(),z(degree),new IntegerVector(z(a),z(b),z(c)));
            assertEquals(chain.subtract(e.inverseOnChain(e.onChain(chain))),e.sourceHomotopyOnChain(chain).boundary().add(e.sourceHomotopyOnChain(chain.boundary())));
            assertEquals(chain.subtract(e.onChain(e.inverseOnChain(chain))),e.targetHomotopyOnChain(chain).boundary().add(e.targetHomotopyOnChain(chain.boundary())));
        }
        SimplicialChainEquivalence contraction=SimplicialChainEquivalence.fromHomotopyEquivalence(contraction(3,false));
        for(int a=-1;a<=1;a++) for(int b=-1;b<=1;b++) for(int c=-1;c<=1;c++) {
            RelativeSimplicialCochain cochain=new RelativeSimplicialCochain(contraction.source(),z(1),new IntegerVector(z(a),z(b),z(c)));
            assertEquals(cochain.subtract(contraction.onCochain(contraction.inverseOnCochain(cochain))),contraction.sourceHomotopyOnCochain(cochain).coboundary().add(contraction.sourceHomotopyOnCochain(cochain.coboundary())));
            SimplicialChainEquivalence inverse=contraction.inverse(); assertEquals(contraction.sourceHomotopyOnCochain(cochain),inverse.targetHomotopyOnCochain(cochain));
        }
    }
    @Test public void typedWitnessActionsRequireExactContextsAndPositiveCochainDegrees() {
        SimplicialChainEquivalence e=SimplicialChainEquivalence.fromHomotopyEquivalence(contraction(1,false));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> e.sourceHomotopyOnChain(RelativeSimplicialChain.zero(e.target(),z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> e.targetHomotopyOnChain(RelativeSimplicialChain.zero(e.source(),z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> e.sourceHomotopyOnCochain(RelativeSimplicialCochain.zero(e.target(),z(1))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> e.targetHomotopyOnCochain(RelativeSimplicialCochain.zero(e.source(),z(1))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> e.sourceHomotopyOnCochain(RelativeSimplicialCochain.zero(e.source(),z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> e.targetHomotopyOnCochain(RelativeSimplicialCochain.zero(e.target(),z(0))));
        assertEquals(RelativeSimplicialChain.zero(e.source(),z(0)),e.sourceHomotopyOnChain(RelativeSimplicialChain.zero(e.source(),z(-1))));
    }
    @Test public void constructiveConversionsSucceedBeyondSimultaneousInverseSearchBounds() {
        SimplicialChainEquivalence strict=SimplicialChainEquivalence.fromIsomorphism(SimplicialChainMap.identity(points(12)));
        SimplicialChainEquivalence geometric=SimplicialChainEquivalence.fromHomotopyEquivalence(contraction(12,false));
        for(SimplicialChainEquivalence e : Arrays.asList(strict,geometric)) {
            assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialChainEquivalence.fromMap(e.forward())).getMessage().contains("256")); identities(e);
        }
    }
    @Test public void emptyAndFilteredDiagonalContextsRetainTheirAmbientDegreeRanges() {
        for(RelativeSimplicialComplex pair : Arrays.asList(points(0),RelativeSimplicialComplex.diagonal(simplex(10)))) {
            SimplicialChainEquivalence e=SimplicialChainEquivalence.fromHomotopyEquivalence(SimplicialHomotopyEquivalence.identity(pair));
            assertEquals(SimplicialChainEquivalence.identity(pair),e); assertEquals(pair.ambient().dimension()+1,e.sourceHomotopy().chainMatrices().size()); identities(e);
            assertEquals(e,SimplicialChainEquivalence.fromIsomorphism(e.forward()));
        }
    }
    @Test public void completeConversionsShareBudgetsWithTheirIndividuallySuccessfulComponents() {
        SimplicialChainMap strict=SimplicialChainMap.identity(points(100)); assertNotNull(strict.inverse());
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialChainEquivalence.fromIsomorphism(strict)).getMessage().contains("5000000"));
        SimplicialHomotopyEquivalence geometric=SimplicialHomotopyEquivalence.identity(points(110));
        assertNotNull(SimplicialChainMap.fromSimplicial(geometric.forward())); assertNotNull(SimplicialChainHomotopy.fromPath(geometric.sourceHomotopy()));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialChainEquivalence.fromHomotopyEquivalence(geometric)).getMessage().contains("5000000"));
        FiniteSimplicialComplex x=points(110).ambient().union(complex(new int[]{108,109})); SimplicialCollapse collapse=new SimplicialCollapse(abs(x),FiniteSet.of(z(109)));
        assertNotNull(SimplicialChainMap.fromCollapse(collapse)); assertNotNull(SimplicialChainHomotopy.fromCollapse(collapse));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialChainEquivalence.fromCollapse(collapse)).getMessage().contains("5000000"));
        SimplicialCollapseSequence sequence=SimplicialCollapseSequence.fromCollapse(collapse);
        assertNotNull(SimplicialChainMap.fromCollapseSequence(sequence)); assertNotNull(SimplicialChainHomotopy.fromCollapseSequence(sequence));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialChainEquivalence.fromCollapseSequence(sequence)).getMessage().contains("5000000"));
    }
    @Test public void subdivisionConversionSharesConstructionAndBothWitnessBudgets() {
        int[][] edges=new int[30][2]; for(int i=0;i<30;i++) edges[i]=new int[]{2*i,2*i+1}; SimplicialSubdivision subdivision=new SimplicialSubdivision(abs(complex(edges)));
        assertNotNull(SimplicialChainMap.fromSubdivision(subdivision)); assertNotNull(SimplicialChainHomotopy.fromSubdivision(subdivision));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialChainEquivalence.fromSubdivision(subdivision)).getMessage().contains("5000000"));
    }
    @Test public void nativeConversionsAndAllFourWitnessActionsRetainActualWrappersAndSerialize() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialHomotopyEquivalence g=contraction(2,false); SimplicialChainEquivalence e=SimplicialChainEquivalence.fromHomotopyEquivalence(g);
        IAlgebraItem<SimplicialChainEquivalence> item=math.homotopyEquivalences.algebra().buildAlgebraItem(g).performAlgebraTransfer("ChainEquivalence.from-homotopy-equivalence");
        assertSame(math.chainEquivalences.algebra(),item.getAlgebra()); assertEquals(e,item.perform().getResult());
        List<IAlgebraFlow<?>> flows=new ArrayList<>();
        flows.add(math.flow(math.homotopyEquivalences,Collections.singletonList(g)).<SimplicialChainEquivalence>performAlgebraTransfer("ChainEquivalence.from-homotopy-equivalence").performFlatAlgebraTransfer("homotopies"));
        for(String prefix : Arrays.asList("source","target")) {
            RelativeSimplicialComplex pair=prefix.equals("source")?e.source():e.target(); RelativeSimplicialChain c=RelativeSimplicialChain.zero(pair,z(0)); RelativeSimplicialCochain q=RelativeSimplicialCochain.zero(pair,z(1));
            assertSame(math.relativeChains.algebra(),item.performLeftProjectionOperation(prefix+"-homotopy-on-chain",c).getAlgebra());
            assertSame(math.relativeCochains.algebra(),item.performLeftProjectionOperation(prefix+"-homotopy-on-cochain",q).getAlgebra());
            flows.add(math.flow(math.chainEquivalences,Collections.singletonList(e)).performLeftProjectionOperation(prefix+"-homotopy-on-chain",c));
            flows.add(math.flow(math.chainEquivalences,Collections.singletonList(e)).performLeftProjectionOperation(prefix+"-homotopy-on-cochain",q));
        }
        SimplicialCollapse step=new SimplicialCollapse(abs(simplex(2)),FiniteSet.of(z(1)));
        flows.add(math.flow(math.collapses,Collections.singletonList(step)).<SimplicialChainEquivalence>performAlgebraTransfer("ChainEquivalence.from-collapse").performFlatAlgebraTransfer("maps"));
        flows.add(math.flow(math.collapseSequences,Collections.singletonList(SimplicialCollapseSequence.fromCollapse(step))).performAlgebraTransfer("ChainEquivalence.from-collapse-sequence"));
        flows.add(math.flow(math.subdivisions,Collections.singletonList(new SimplicialSubdivision(abs(simplex(2))))).performAlgebraTransfer("ChainEquivalence.from-subdivision"));
        flows.add(math.flow(math.chainMaps,Collections.singletonList(SimplicialChainMap.identity(points(2)))).performAlgebraTransfer("ChainEquivalence.from-isomorphism"));
        for(IAlgebraFlow<?> original : flows) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(original); }
            IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
            assertEquals(original.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        }
    }
}
