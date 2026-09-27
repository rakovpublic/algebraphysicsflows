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

public class NativeSimplicialSubdivisionTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int v : facet) labels.add(v); faces.add(new FiniteSet<>(labels)); } return new FiniteSimplicialComplex(faces);
    }
    private static FiniteSimplicialComplex simplex(int count) { int[] v=new int[count]; for(int i=0;i<count;i++) v[i]=i; return complex(v); }
    private static FiniteSimplicialComplex points(int count) { int[][] f=new int[count][1]; for(int i=0;i<count;i++) f[i][0]=i; return complex(f); }
    private static RelativeSimplicialComplex absolute(FiniteSimplicialComplex c) { return RelativeSimplicialComplex.absolute(c); }
    private static RelativeSimplicialMap map(RelativeSimplicialComplex source,RelativeSimplicialComplex target,int... images) {
        Map<BigInteger,BigInteger> vertices=new TreeMap<>(); int i=0; for(BigInteger v : FiniteSimplicialMap.vertexSet(source.ambient()).members()) vertices.put(v,z(images[i++])); return new RelativeSimplicialMap(source,target,new FiniteSimplicialMap(source.ambient(),target.ambient(),vertices));
    }
    private static void failure(MathFailure.Kind kind,Runnable body) { assertEquals(kind,assertThrows(MathFailure.class,body::run).kind()); }
    private static List<FiniteSet<Integer>> allFaces(FiniteSimplicialComplex c) { List<FiniteSet<Integer>> result=new ArrayList<>(); for(int k=0;k<=c.dimension();k++) result.addAll(c.simplices(k)); return result; }
    private static boolean closed(int mask,int labels) {
        for(int face=1;face<(1<<labels);face++) if((mask&(1<<(face-1)))!=0) for(int sub=(face-1)&face;sub>0;sub=(sub-1)&face) if((mask&(1<<(sub-1)))==0) return false; return true;
    }
    private static List<Integer> faceVertices(int face,int labels) { List<Integer> v=new ArrayList<>(); for(int i=0;i<labels;i++) if((face&(1<<i))!=0) v.add(i); return v; }
    private static List<Integer> orderedFaces(int mask,int labels) {
        List<Integer> faces=new ArrayList<>(); for(int face=1;face<(1<<labels);face++) if((mask&(1<<(face-1)))!=0) faces.add(face);
        faces.sort((a,b) -> { int size=Integer.compare(Integer.bitCount(a),Integer.bitCount(b)); if(size!=0) return size; List<Integer> x=faceVertices(a,labels),y=faceVertices(b,labels); for(int i=0;i<x.size();i++) if(!x.get(i).equals(y.get(i))) return Integer.compare(x.get(i),y.get(i)); return 0; }); return faces;
    }
    private static FiniteSimplicialComplex fromMask(int mask,int labels) { List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int f : orderedFaces(mask,labels)) faces.add(new FiniteSet<>(faceVertices(f,labels))); return new FiniteSimplicialComplex(faces); }
    // Independent subset oracle: a set of original faces is a simplex exactly when every pair is comparable.
    private static FiniteSimplicialComplex oracle(int ambient,int subcomplex,int labels) {
        List<Integer> faces=orderedFaces(ambient,labels); List<FiniteSet<Integer>> simplices=new ArrayList<>();
        for(int subset=1;subset<(1<<faces.size());subset++) {
            List<Integer> vertices=new ArrayList<>(); boolean valid=true;
            for(int i=0;i<faces.size();i++) if((subset&(1<<i))!=0) {
                if((subcomplex&(1<<(faces.get(i)-1)))==0) { valid=false; break; }
                for(int j : vertices) if((faces.get(i)&faces.get(j))!=faces.get(i) && (faces.get(i)&faces.get(j))!=faces.get(j)) valid=false;
                vertices.add(i);
            }
            if(valid) simplices.add(new FiniteSet<>(vertices));
        }
        return new FiniteSimplicialComplex(simplices);
    }
    private static void inverseMaps(SimplicialSubdivision s,int degree) {
        for(List<AbelianGroupHomomorphism> maps : Arrays.asList(s.homologyMaps(z(degree)),s.cohomologyMaps(z(degree)))) {
            AbelianGroupHomomorphism f=maps.get(0),g=maps.get(1); assertEquals(AbelianGroupHomomorphism.identity(f.source()),g.compose(f)); assertEquals(AbelianGroupHomomorphism.identity(f.target()),f.compose(g));
        }
        assertEquals(s.homologyMaps(z(degree)).get(1),s.inverseHomologyMap(z(degree))); assertEquals(s.cohomologyMaps(z(degree)).get(1),s.inverseCohomologyMap(z(degree)));
    }
    @Test public void all167FourLabelComplexesMatchIndependentComparableSubsetOracle() {
        int checked=0;
        for(int mask=0;mask<(1<<15);mask++) if(closed(mask,4)) {
            checked++; SimplicialSubdivision s=SimplicialSubdivision.absolute(fromMask(mask,4));
            assertEquals(oracle(mask,mask,4),s.subdivided().ambient()); assertEquals(s.original().ambient().dimension(),s.subdivided().ambient().dimension()); assertEquals(s.original().eulerCharacteristic(),s.subdivided().eulerCharacteristic());
            List<Integer> faces=orderedFaces(mask,4); assertEquals(z(faces.size()),s.vertexCount());
            for(int i=0;i<faces.size();i++) { List<BigInteger> expected=new ArrayList<>(); for(int v : faceVertices(faces.get(i),4)) expected.add(z(v)); assertEquals(new FiniteSet<>(expected),s.vertexFace(z(i))); assertEquals(z(i),s.faceVertex(new FiniteSet<>(expected))); }
        }
        assertEquals(167,checked);
    }
    @Test public void all148ThreeLabelPairsKeepSharedAmbientLabelsAndIntegralIsomorphisms() {
        int checked=0;
        for(int x=0;x<128;x++) if(closed(x,3)) for(int a=0;a<128;a++) if(closed(a,3) && (x&a)==a) {
            checked++; SimplicialSubdivision s=new SimplicialSubdivision(new RelativeSimplicialComplex(fromMask(x,3),fromMask(a,3)));
            assertEquals(oracle(x,a,3),s.subdivided().subcomplex()); assertEquals(s.original().eulerCharacteristic(),s.subdivided().eulerCharacteristic()); for(int degree=0;degree<=2;degree++) inverseMaps(s,degree);
        }
        assertEquals(148,checked);
        SimplicialSubdivision edgeInTriangle=new SimplicialSubdivision(new RelativeSimplicialComplex(simplex(3),complex(new int[]{1,2})));
        assertEquals(new FiniteSet<>(Arrays.asList(z(1),z(2),z(5))),FiniteSimplicialMap.vertexSet(edgeInTriangle.subdivided().subcomplex()));
        assertNotEquals(SimplicialSubdivision.absolute(complex(new int[]{1,2})).subdivided().ambient(),edgeInTriangle.subdivided().subcomplex());
    }
    private static long stirling(int n,int k) { if(n==0) return k==0?1:0; return k==0?0:k*stirling(n-1,k)+stirling(n-1,k-1); }
    private static long factorial(int n) { long result=1; for(int i=2;i<=n;i++) result*=i; return result; }
    private static long binomial(int n,int k) { return factorial(n)/factorial(k)/factorial(n-k); }
    @Test public void simplexFaceCountsAgreeWithOrderedPartitionFormulaThroughDimensionFour() {
        for(int vertices=1;vertices<=5;vertices++) {
            SimplicialSubdivision s=SimplicialSubdivision.absolute(simplex(vertices));
            for(int degree=0;degree<vertices;degree++) { long expected=0; for(int m=degree+1;m<=vertices;m++) expected+=binomial(vertices,m)*factorial(degree+1)*stirling(m,degree+1); assertEquals(expected,s.subdivided().ambient().simplices(degree).size()); }
        }
        SimplicialSubdivision tetra=SimplicialSubdivision.absolute(simplex(4)); for(int degree=0;degree<=3;degree++) inverseMaps(tetra,degree);
    }
    @Test public void all256TetrahedronMapsInduceFaceImagesAndCommuteWithBoundaries() {
        RelativeSimplicialComplex pair=absolute(simplex(4)); SimplicialSubdivision s=new SimplicialSubdivision(pair);
        for(int code=0;code<256;code++) {
            int[] images={code%4,(code/4)%4,(code/16)%4,(code/64)%4}; RelativeSimplicialMap f=map(pair,pair,images),sd=s.map(f);
            for(int i=0;i<15;i++) { Set<BigInteger> image=new TreeSet<>(); for(BigInteger v : s.vertexFace(z(i)).members()) image.add(z(images[v.intValueExact()])); assertEquals(s.faceVertex(new FiniteSet<>(image)),sd.ambientMap().mapVertex(z(i))); }
            for(int k=1;k<=3;k++) assertEquals(s.subdivided().boundaryMatrix(z(k)).multiply(sd.chainMatrix(z(k))),sd.chainMatrix(z(k-1)).multiply(s.subdivided().boundaryMatrix(z(k))));
        }
    }
    @Test public void all729CircleMapCompositionsAreFunctorialIncludingCollapses() {
        RelativeSimplicialComplex circle=absolute(complex(new int[]{0,1},new int[]{0,2},new int[]{1,2})); SimplicialSubdivision s=new SimplicialSubdivision(circle); List<RelativeSimplicialMap> maps=new ArrayList<>(),subdivided=new ArrayList<>();
        for(int code=0;code<27;code++) { RelativeSimplicialMap f=map(circle,circle,code%3,(code/3)%3,(code/9)%3); maps.add(f); subdivided.add(s.map(f)); }
        assertEquals(RelativeSimplicialMap.identity(s.subdivided()),s.map(RelativeSimplicialMap.identity(circle)));
        for(int i=0;i<27;i++) for(int j=0;j<27;j++) assertEquals(subdivided.get(i).compose(subdivided.get(j)),s.map(maps.get(i).compose(maps.get(j))));
    }
    @Test public void lastVertexNaturalityIsAContiguityWitnessForEveryCircleMap() {
        RelativeSimplicialComplex circle=absolute(complex(new int[]{0,1},new int[]{0,2},new int[]{1,2})); SimplicialSubdivision s=new SimplicialSubdivision(circle); RelativeSimplicialMap last=s.lastVertexMap();
        for(int code=0;code<27;code++) {
            RelativeSimplicialMap f=map(circle,circle,code%3,(code/3)%3,(code/9)%3),sd=s.map(f); SimplicialHomotopy h=s.naturalityHomotopy(f);
            assertEquals(last.compose(sd),h.from()); assertEquals(f.compose(last),h.to());
            assertEquals(h.to().chainMatrix(z(0)).add(h.from().chainMatrix(z(0)).scale(z(-1))),circle.boundaryMatrix(z(1)).multiply(h.chainMatrix(z(0))));
            assertEquals(last.homologyMap(z(1)).compose(sd.homologyMap(z(1))),f.homologyMap(z(1)).compose(last.homologyMap(z(1))));
            assertEquals(RelativeSimplicialCochain.cohomologyMap(last,z(1)).compose(RelativeSimplicialCochain.cohomologyMap(f,z(1))),RelativeSimplicialCochain.cohomologyMap(sd,z(1)).compose(RelativeSimplicialCochain.cohomologyMap(last,z(1))));
        }
        SimplicialHomotopy reflection=s.naturalityHomotopy(map(circle,circle,2,1,0)); assertNotEquals(reflection.from(),reflection.to());
    }
    @Test public void relativeDiskBoundariesAndProjectivePlaneTorsionSurviveSubdivision() {
        for(int dimension=1;dimension<=3;dimension++) {
            FiniteSimplicialComplex disk=simplex(dimension+1); SimplicialSubdivision s=new SimplicialSubdivision(new RelativeSimplicialComplex(disk,disk.skeleton(dimension-1)));
            assertEquals(z(1),s.homologyMap(z(dimension)).source().type().freeRank()); inverseMaps(s,dimension);
        }
        FiniteSimplicialComplex rp2=complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        SimplicialSubdivision s=SimplicialSubdivision.absolute(rp2); assertEquals(Collections.singletonList(z(2)),s.homologyMap(z(1)).source().type().invariantFactors());
        assertEquals(Collections.singletonList(z(2)),s.cohomologyMap(z(2)).target().type().invariantFactors()); inverseMaps(s,1); inverseMaps(s,2);
    }
    @Test public void naturalityPreservesNonemptySubcomplexesAndGivesTypedFillings() {
        RelativeSimplicialComplex source=new RelativeSimplicialComplex(simplex(3),complex(new int[]{0,1})),target=new RelativeSimplicialComplex(simplex(3),complex(new int[]{1,2}));
        SimplicialSubdivision s=new SimplicialSubdivision(source); RelativeSimplicialMap f=map(source,target,2,1,0),sd=s.map(f); SimplicialSubdivision t=new SimplicialSubdivision(target);
        assertEquals(t.subdivided(),sd.target()); SimplicialHomotopy h=s.naturalityHomotopy(f);
        for(RelativeSimplicialChain c : RelativeSimplicialChain.basisChains(s.subdivided(),z(0))) assertEquals(c.pushforward(h.to()).subtract(c.pushforward(h.from())),h.onChain(c).boundary());
        assertEquals(f.compose(s.lastVertexMap()),h.to()); assertEquals(t.lastVertexMap().compose(sd),h.from());
    }
    @Test public void invalidLookupsAndFullSourceMismatchesAreUndefined() {
        SimplicialSubdivision s=SimplicialSubdivision.absolute(complex(new int[]{0,1},new int[]{1,2}));
        for(BigInteger v : Arrays.asList(z(-1),s.vertexCount(),BigInteger.TEN.pow(100))) failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> s.vertexFace(v));
        for(FiniteSet<BigInteger> f : Arrays.asList(FiniteSet.<BigInteger>of(),FiniteSet.of(z(0),z(2)),FiniteSet.of(BigInteger.TEN.pow(100)))) failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> s.faceVertex(f));
        RelativeSimplicialMap wrong=RelativeSimplicialMap.identity(RelativeSimplicialComplex.diagonal(s.original().ambient()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> s.map(wrong)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> s.naturalityHomotopy(wrong));
    }
    @Test public void emptyHugeDegreesExtremeLabelsAndImmutableOutputsAreRetained() {
        SimplicialSubdivision empty=SimplicialSubdivision.absolute(complex()); assertEquals(z(0),empty.vertexCount()); assertEquals(absolute(complex()),empty.subdivided()); inverseMaps(empty,0);
        assertTrue(empty.homologyMap(BigInteger.TEN.pow(100)).isIsomorphism()); assertTrue(empty.cohomologyMap(BigInteger.TEN.pow(100)).isIsomorphism());
        for(Runnable run : Arrays.<Runnable>asList(() -> empty.homologyMap(z(-1)),() -> empty.inverseHomologyMap(z(-1)),() -> empty.homologyMaps(z(-1)),() -> empty.cohomologyMap(z(-1)),() -> empty.inverseCohomologyMap(z(-1)),() -> empty.cohomologyMaps(z(-1)))) failure(MathFailure.Kind.OPERATION_UNDEFINED,run);
        SimplicialSubdivision extremes=SimplicialSubdivision.absolute(complex(new int[]{Integer.MIN_VALUE,Integer.MAX_VALUE})); assertEquals(FiniteSet.of(z(Integer.MIN_VALUE),z(Integer.MAX_VALUE)),extremes.vertexFace(z(2))); assertEquals(z(Integer.MAX_VALUE),extremes.lastVertexMap().ambientMap().mapVertex(z(2)));
        assertThrows(UnsupportedOperationException.class,() -> extremes.vertexFaces().clear()); assertThrows(UnsupportedOperationException.class,() -> extremes.homologyMaps(z(0)).clear()); assertThrows(UnsupportedOperationException.class,() -> extremes.cohomologyMaps(z(0)).clear());
    }
    @Test public void repeatedSubdivisionChangesStrongTypeWhileRetainingIntegralMaps() {
        FiniteSimplicialComplex circle=complex(new int[]{0,1},new int[]{0,2},new int[]{1,2}); SimplicialSubdivision first=SimplicialSubdivision.absolute(circle),second=new SimplicialSubdivision(first.subdivided());
        assertEquals(6,first.subdivided().ambient().simplices(0).size()); assertEquals(12,second.subdivided().ambient().simplices(0).size());
        assertFalse(SimplicialStrongCollapse.stronglyEquivalent(circle,first.subdivided().ambient())); inverseMaps(first,1); inverseMaps(second,1);
        SimplicialSubdivision reordered=SimplicialSubdivision.absolute(complex(new int[]{2,1},new int[]{2,0},new int[]{1,0})); assertEquals(first,reordered); assertEquals(first.hashCode(),reordered.hashCode()); assertEquals(first.subdivided(),reordered.subdivided());
    }
    @Test public void outputSimplexLimitsAndFilteredBasisLimitsAreSeparate() {
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialSubdivision.absolute(simplex(6)));
        SimplicialSubdivision maximum=SimplicialSubdivision.absolute(points(4096)); assertEquals(z(4096),maximum.vertexCount());
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> maximum.homologyMap(z(0)));
        SimplicialSubdivision filtered=new SimplicialSubdivision(new RelativeSimplicialComplex(points(300),points(299))); inverseMaps(filtered,0);
    }
    @Test public void inverseConstructionSharesTheMapComputationBudget() {
        SimplicialSubdivision s=SimplicialSubdivision.absolute(points(80)); AbelianGroupHomomorphism h=s.homologyMap(z(0)),c=s.cohomologyMap(z(0)); assertNotNull(h.inverse()); assertNotNull(c.inverse());
        for(Runnable run : Arrays.<Runnable>asList(() -> s.inverseHomologyMap(z(0)),() -> s.homologyMaps(z(0)),() -> s.inverseCohomologyMap(z(0)),() -> s.cohomologyMaps(z(0)))) failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,run);
    }
    @Test public void nativeSecondResultAndFlatHomologyFlowsSerializeWithActualWrappers() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); RelativeSimplicialComplex pair=absolute(complex(new int[]{0,1},new int[]{0,2},new int[]{1,2}));
        IAlgebraItem<SimplicialSubdivision> item=math.relativeComplexes.algebra().buildAlgebraItem(pair).performAlgebraTransfer("SimplicialSubdivision.from-pair"); assertSame(math.subdivisions.algebra(),item.getAlgebra());
        assertSame(math.relativeMaps.algebra(),item.performLeftProjectionOperation("map",RelativeSimplicialMap.identity(pair)).getAlgebra()); assertSame(math.simplicialHomotopies.algebra(),item.performUnsafeOperation("naturality-homotopy",RelativeSimplicialMap.identity(pair)).getAlgebra());
        IAlgebraFlow<Boolean> flow=math.flow(math.complexes,Collections.singletonList(pair.ambient())).<SimplicialSubdivision>performAlgebraTransfer("SimplicialSubdivision.from-complex")
                .<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe("homology-maps",z(1)).<Boolean>performAlgebraTransfer("is-isomorphism");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Arrays.asList("true","true"),restored.collect()); assertEquals(restored.collect(),restored.collect());
    }
}
