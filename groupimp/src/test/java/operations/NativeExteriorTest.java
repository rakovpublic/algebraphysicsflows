package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.linear.*;
import mathematics.numbers.Rational;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeExteriorTest {
    private static RationalVector v(long... values) {
        Rational[] entries=new Rational[values.length]; for(int i=0;i<entries.length;i++) entries[i]=Rational.of(values[i]); return new RationalVector(entries);
    }
    private static RationalExterior basis(int dimension,int mask) { return new RationalExterior(dimension,Collections.singletonMap(mask,Rational.ONE)); }
    private static RationalExterior vector(long... values) { return RationalExterior.fromVector(v(values)); }
    private static RationalMatrix matrix(long[]... values) {
        Rational[][] entries=new Rational[values.length][];
        for(int i=0;i<values.length;i++) { entries[i]=new Rational[values[i].length]; for(int j=0;j<values[i].length;j++) entries[i][j]=Rational.of(values[i][j]); }
        return new RationalMatrix(entries);
    }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static void undefined(Runnable action) { failure(MathFailure.Kind.OPERATION_UNDEFINED,action); }

    @Test public void canonicalSparseElementsRetainAmbientDimensionAndAllGrades() {
        Map<Integer,Rational> terms=new HashMap<>(); terms.put(0,Rational.of(2)); terms.put(1,Rational.ONE); terms.put(3,Rational.of(-3)); terms.put(7,Rational.ZERO);
        RationalExterior value=new RationalExterior(3,terms); terms.clear();
        assertEquals(Arrays.asList(0,1,3),new ArrayList<>(value.coefficients().keySet())); assertEquals(Arrays.asList(0,1,2),value.degrees());
        assertEquals(Rational.of(2),value.scalarPart()); assertEquals(basis(3,1),value.grade(1)); assertTrue(value.grade(4).isZero());
        assertEquals(RationalExterior.scalar(3,Rational.of(2)),value.grade(0));
        assertEquals(value,value.grade(0).add(value.grade(1)).add(value.grade(2)));
        assertThrows(UnsupportedOperationException.class,() -> value.coefficients().clear());
        assertThrows(UnsupportedOperationException.class,() -> value.terms().clear());
        assertTrue(RationalExterior.zero(3).terms().isEmpty()); assertTrue(RationalExterior.zero(3).degrees().isEmpty());
        assertNotEquals(RationalExterior.zero(2),RationalExterior.zero(3));
        assertEquals(v(0,0,0),RationalExterior.zero(3).toVector()); assertEquals(Rational.ZERO,RationalExterior.zero(3).toScalar());
        assertEquals(RationalExterior.scalar(0,Rational.ONE),RationalExterior.volume(0));
        assertEquals(v(),RationalExterior.fromVector(v()).toVector());
        undefined(value::toScalar); undefined(value::toVector); undefined(() -> RationalExterior.scalar(0,Rational.ONE).toVector());
    }

    /** Independent reference: concatenate index lists, reject duplicates, and count inversions. */
    private static RationalExterior referenceWedge(int n,int first,int second) {
        List<Integer> indices=new ArrayList<>();
        for(int i=0;i<n;i++) if((first&(1<<i))!=0) indices.add(i);
        for(int i=0;i<n;i++) if((second&(1<<i))!=0) indices.add(i);
        if(new HashSet<>(indices).size()!=indices.size()) return RationalExterior.zero(n);
        int inversions=0; for(int i=0;i<indices.size();i++) for(int j=i+1;j<indices.size();j++) if(indices.get(i)>indices.get(j)) inversions++;
        return basis(n,first|second).scale(Rational.of(inversions%2==0?1:-1));
    }
    @Test public void basisProductsObeyIndependentSignsAssociativityAndGradedCommutativity() {
        for(int n=0;n<=5;n++) for(int a=0;a<(1<<n);a++) for(int b=0;b<(1<<n);b++) {
            RationalExterior x=basis(n,a),y=basis(n,b),product=x.wedge(y);
            assertEquals(referenceWedge(n,a,b),product);
            int parity=Integer.bitCount(a)*Integer.bitCount(b)%2;
            assertEquals(product,y.wedge(x).scale(Rational.of(parity==0?1:-1)));
            assertEquals(product.reverse(),y.reverse().wedge(x.reverse()));
            assertEquals(product.gradeInvolution(),x.gradeInvolution().wedge(y.gradeInvolution()));
            if(n<=4) for(int c=0;c<(1<<n);c++) assertEquals(x.wedge(y).wedge(basis(n,c)),x.wedge(y.wedge(basis(n,c))));
        }
        RationalExterior mixed=basis(4,1).add(basis(4,6)),other=basis(4,2).add(basis(4,8));
        assertEquals(mixed.wedge(other).add(mixed.wedge(mixed)),mixed.wedge(other.add(mixed)));
        assertEquals(mixed,mixed.reverse().reverse()); assertEquals(mixed,mixed.gradeInvolution().gradeInvolution());
    }

    @Test public void hodgeStarMatchesOrientationInnerProductAndDegreeDependentSquare() {
        for(int n=0;n<=7;n++) for(int mask=0;mask<(1<<n);mask++) {
            RationalExterior value=basis(n,mask),star=value.hodgeStar(); int k=Integer.bitCount(mask);
            assertEquals(RationalExterior.volume(n),value.wedge(star));
            assertEquals(value.scale(Rational.of(k*(n-k)%2==0?1:-1)),star.hodgeStar());
            assertEquals(value.dot(value),star.dot(star));
        }
        for(int a=0;a<32;a++) for(int b=0;b<32;b++) if(Integer.bitCount(a)==Integer.bitCount(b))
            assertEquals(RationalExterior.volume(5).scale(a==b?Rational.ONE:Rational.ZERO),basis(5,a).wedge(basis(5,b).hodgeStar()));
        assertEquals(basis(3,5).scale(Rational.of(-1)),basis(3,2).hodgeStar());
        RationalExterior mixed=basis(4,1).add(basis(4,3)).add(basis(4,7));
        assertEquals(mixed.grade(1).scale(Rational.of(-1)).add(mixed.grade(2)).add(mixed.grade(3).scale(Rational.of(-1))),mixed.hodgeStar().hodgeStar());
    }

    @Test public void insertionIsAnAntiderivationAndAdjointToLeftWedge() {
        RationalVector v=v(1,-2,3,4); RationalExterior lifted=RationalExterior.fromVector(v);
        for(int a=0;a<16;a++) for(int b=0;b<16;b++) {
            RationalExterior x=basis(4,a),y=basis(4,b);
            assertEquals(x.interior(v).wedge(y).add(x.wedge(y.interior(v)).scale(Rational.of(Integer.bitCount(a)%2==0?1:-1))),x.wedge(y).interior(v));
            assertEquals(lifted.wedge(x).dot(y),x.dot(y.interior(v)));
            assertTrue(x.interior(v).interior(v).isZero());
        }
        assertEquals(vector(-2,1,0),basis(3,3).interior(v(1,2,3)));
        assertEquals(Rational.of(32),vector(1,2,3).interior(v(4,5,6)).toScalar());
    }

    private static long[] ternaryVector(int code) {
        long[] result=new long[3]; for(int i=0;i<3;i++) { result[i]=code%3-1; code/=3; } return result;
    }
    @Test public void threeDimensionalWedgeAndHodgeRecoverAllTernaryCrossProducts() {
        for(int first=0;first<27;first++) for(int second=0;second<27;second++) {
            long[] a=ternaryVector(first),b=ternaryVector(second);
            RationalExterior wedge=vector(a).wedge(vector(b));
            assertEquals(v(a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]),wedge.hodgeStar().toVector());
            RationalVector av=v(a),bv=v(b);
            assertEquals(av.dot(av).multiply(bv.dot(bv)).subtract(av.dot(bv).multiply(av.dot(bv))),wedge.dot(wedge));
        }
    }

    @Test public void inducedMatrixMapsPreserveWedgesCompositionMinorsAndDeterminants() {
        for(int code=0;code<729;code++) {
            int digits=code; long[][] entries=new long[2][3];
            for(int i=0;i<2;i++) for(int j=0;j<3;j++) { entries[i][j]=digits%3-1; digits/=3; }
            RationalMatrix matrix=matrix(entries);
            for(int a=0;a<3;a++) for(int b=a+1;b<3;b++) {
                long minor=entries[0][a]*entries[1][b]-entries[0][b]*entries[1][a];
                assertEquals(basis(2,3).scale(Rational.of(minor)),basis(3,(1<<a)|(1<<b)).map(matrix));
            }
            assertTrue(RationalExterior.volume(3).map(matrix).isZero());
            assertEquals(RationalExterior.scalar(2,Rational.of(5)),RationalExterior.scalar(3,Rational.of(5)).map(matrix));
        }
        for(int code=0;code<512;code++) {
            long[][] a=new long[3][3]; for(int i=0;i<9;i++) a[i/3][i%3]=(code>>i)&1;
            long determinant=a[0][0]*a[1][1]*a[2][2]+a[0][1]*a[1][2]*a[2][0]+a[0][2]*a[1][0]*a[2][1]
                    -a[0][2]*a[1][1]*a[2][0]-a[0][1]*a[1][0]*a[2][2]-a[0][0]*a[1][2]*a[2][1];
            assertEquals(RationalExterior.volume(3).scale(Rational.of(determinant)),RationalExterior.volume(3).map(matrix(a)));
        }
        RationalMatrix first=matrix(new long[]{1,2},new long[]{3,4},new long[]{5,6}),second=matrix(new long[]{1,-1,2},new long[]{0,3,1});
        RationalExterior x=basis(2,0).add(basis(2,1)).add(basis(2,3)),y=basis(2,2).add(basis(2,3));
        assertEquals(x.map(first).map(second),x.map(second.multiply(first)));
        assertEquals(x.wedge(y).map(first),x.map(first).wedge(y.map(first)));
        assertEquals(x,x.map(RationalMatrix.identity(2)));
    }

    @Test public void nativeOperationsPreserveActualWrappersAndSerializeCrossProductFlows() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics();
        IAlgebraItem<RationalExterior> lifted=math.finiteVectors.algebra().buildAlgebraItem(v(1,2,3)).performAlgebraTransfer("Exterior(Q).from-vector");
        assertSame(math.exterior.algebra(),lifted.getAlgebra());
        IAlgebraItem<RationalExterior> mapped=math.rectangularMatrices.algebra().buildAlgebraItem(matrix(new long[]{1,2,3},new long[]{2,4,6}))
                .performLeftProjectionOperation("Exterior(Q).apply",vector(4,5,6));
        assertSame(math.exterior.algebra(),mapped.getAlgebra()); assertEquals(vector(32,64),mapped.getResult());
        IAlgebraItem<RationalVector> recovered=mapped.performAlgebraTransfer("to-vector"); assertSame(math.finiteVectors.algebra(),recovered.getAlgebra());
        for(IAlgebraItem<RationalExterior> term : lifted.performOneOperandFlatOperation("terms")) assertSame(math.exterior.algebra(),term.getAlgebra());
        IAlgebraFlow<Rational> flow=math.flow(math.finiteVectors,Collections.singletonList(v(1,2,3)))
                .<RationalExterior>performAlgebraTransfer("Exterior(Q).from-vector").performOperation("wedge",vector(4,5,6))
                .performOneOperandOperation("hodge-star").<RationalVector>performAlgebraTransfer("to-vector")
                .<Rational>performFlatAlgebraTransfer("entries").performOneOperandOperation("negate");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Arrays.asList("3","-6","3"),restored.collect()); assertEquals(Arrays.asList("3","-6","3"),restored.collect());
        assertEquals(Collections.singletonList("1"),math.flow(math.naturals,Collections.singletonList(BigInteger.valueOf(3)))
                .<RationalExterior>performAlgebraTransfer("Exterior(Q).volume-in").performOneOperandOperation("hodge-star").<Rational>performAlgebraTransfer("to-scalar").collect());
        assertEquals(RationalExterior.zero(3),lifted.performCustomMemberOperation("grade",BigInteger.TEN.pow(80)).getResult());
    }

    @Test public void invalidMembersUndefinedOperationsAndResourceLimitsRemainDistinct() {
        failure(MathFailure.Kind.INVALID_MEMBER,() -> RationalExterior.zero(-1));
        failure(MathFailure.Kind.INVALID_MEMBER,() -> basis(2,4)); failure(MathFailure.Kind.INVALID_MEMBER,() -> basis(2,-1));
        undefined(() -> vector(1,2).wedge(vector(1,2,3))); undefined(() -> vector(1,2).add(vector(1)));
        undefined(() -> vector(1,2).dot(vector(1))); undefined(() -> vector(1,2).interior(v(1)));
        undefined(() -> basis(3,3).map(RationalMatrix.identity(2))); undefined(() -> basis(3,3).grade(-1));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> RationalExterior.zero(21));
        ConcreteMathematics math=new ConcreteMathematics();
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> math.naturals.algebra().buildAlgebraItem(BigInteger.TEN.pow(80)).performAlgebraTransfer("Exterior(Q).one-in"));
        Map<Integer,Rational> many=new TreeMap<>(); for(int i=0;i<1001;i++) many.put(i,Rational.ONE);
        RationalExterior dense=new RationalExterior(10,many);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> dense.wedge(dense));
        for(int i=1001;i<=RationalExterior.MAX_TERMS;i++) many.put(i,Rational.ONE);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> new RationalExterior(17,many));
        assertEquals(basis(20,(1<<20)-1),RationalExterior.volume(20));
        undefined(() -> math.exterior.algebra().buildAlgebraItem(vector(1,2)).performOperation("wedge",vector(1)).perform());
    }

    @Test public void largeRationalCoefficientsAndGramDeterminantsStayExact() {
        Rational huge=Rational.of(BigInteger.TEN.pow(80)),next=huge.add(Rational.ONE);
        RationalVector u=new RationalVector(huge,next,Rational.of(1,3)),v=new RationalVector(next,huge,Rational.of(-2,7));
        RationalExterior wedge=RationalExterior.fromVector(u).wedge(RationalExterior.fromVector(v));
        assertEquals(huge.multiply(huge).subtract(next.multiply(next)),wedge.coefficients().get(3));
        assertEquals(u.dot(u).multiply(v.dot(v)).subtract(u.dot(v).multiply(u.dot(v))),wedge.dot(wedge));
        assertEquals(wedge,wedge.hodgeStar().hodgeStar());
        assertEquals(Rational.ZERO,RationalExterior.fromVector(u).wedge(RationalExterior.fromVector(u)).toScalar());
    }
}
