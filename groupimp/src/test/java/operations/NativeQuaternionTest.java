package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.linear.*;
import mathematics.numbers.*;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeQuaternionTest {
    private static RationalQuaternion q(long w,long x,long y,long z) { return new RationalQuaternion(Rational.of(w),Rational.of(x),Rational.of(y),Rational.of(z)); }
    private static RationalVector v(long x,long y,long z) { return new RationalVector(Rational.of(x),Rational.of(y),Rational.of(z)); }
    private static RationalMatrix m(long[]... rows) {
        Rational[][] entries=new Rational[rows.length][];
        for(int r=0;r<rows.length;r++) { entries[r]=new Rational[rows[r].length]; for(int c=0;c<rows[r].length;c++) entries[r][c]=Rational.of(rows[r][c]); }
        return new RationalMatrix(entries);
    }
    private static void undefined(Runnable action) { assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,action::run).kind()); }
    private static RationalQuaternion ternary(int code) {
        long[] entries=new long[4]; for(int i=0;i<4;i++) { entries[i]=code%3-1; code/=3; }
        return q(entries[0],entries[1],entries[2],entries[3]);
    }
    private static RationalVector cross(RationalVector a,RationalVector b) {
        return new RationalVector(a.get(1).multiply(b.get(2)).subtract(a.get(2).multiply(b.get(1))),
                a.get(2).multiply(b.get(0)).subtract(a.get(0).multiply(b.get(2))),a.get(0).multiply(b.get(1)).subtract(a.get(1).multiply(b.get(0))));
    }

    @Test public void hamiltonBasisAndBothDivisionOrdersRespectNoncommutativity() {
        RationalQuaternion one=RationalQuaternion.ONE,i=RationalQuaternion.I,j=RationalQuaternion.J,k=RationalQuaternion.K;
        assertEquals(one.negate(),i.multiply(i)); assertEquals(one.negate(),j.multiply(j)); assertEquals(one.negate(),k.multiply(k));
        assertEquals(k,i.multiply(j)); assertEquals(k.negate(),j.multiply(i)); assertEquals(i,j.multiply(k)); assertEquals(j,k.multiply(i));
        assertEquals(one.negate(),i.multiply(j).multiply(k));
        assertEquals(k.negate(),i.divideRight(j)); assertEquals(k,i.divideLeft(j));
        for(RationalQuaternion a : Arrays.asList(one,i,j,k)) for(RationalQuaternion b : Arrays.asList(one,i,j,k)) for(RationalQuaternion c : Arrays.asList(one,i,j,k))
            assertEquals(a.multiply(b).multiply(c),a.multiply(b.multiply(c)));
        RationalQuaternion a=q(1,2,3,4),b=q(2,-1,1,3);
        assertEquals(a,a.divideRight(b).multiply(b)); assertEquals(a,b.multiply(a.divideLeft(b)));
        assertEquals(RationalQuaternion.ONE,a.multiply(a.inverse())); assertEquals(RationalQuaternion.ONE,a.inverse().multiply(a));
    }

    @Test public void allTernaryProductsAgreeWithAnIndependentBasisMultiplicationTable() {
        int[][] target={{0,1,2,3},{1,0,3,2},{2,3,0,1},{3,2,1,0}};
        int[][] sign={{1,1,1,1},{1,-1,1,-1},{1,-1,-1,1},{1,1,-1,-1}};
        for(int first=0;first<81;first++) for(int second=0;second<81;second++) {
            RationalQuaternion a=ternary(first),b=ternary(second); Rational[] expected={Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO};
            for(int i=0;i<4;i++) for(int j=0;j<4;j++) expected[target[i][j]]=expected[target[i][j]].add(a.components().get(i).multiply(b.components().get(j)).multiply(Rational.of(sign[i][j])));
            RationalQuaternion product=a.multiply(b);
            assertEquals(new RationalQuaternion(expected[0],expected[1],expected[2],expected[3]),product);
            assertEquals(a.normSquared().multiply(b.normSquared()),product.normSquared());
            assertEquals(b.conjugate().multiply(a.conjugate()),product.conjugate());
            if(b.normSquared().signum()!=0) { assertEquals(a,a.divideRight(b).multiply(b)); assertEquals(a,b.multiply(a.divideLeft(b))); }
        }
    }

    @Test public void rationalAndComplexEmbeddingsPreserveTheirChosenSubfields() {
        RationalComplex a=new RationalComplex(Rational.of(1,2),Rational.of(-3,7)),b=new RationalComplex(Rational.of(2,3),Rational.of(4,5));
        RationalQuaternion aq=RationalQuaternion.fromComplex(a),bq=RationalQuaternion.fromComplex(b);
        assertEquals(a,aq.toComplex()); assertEquals(a.multiply(b),aq.multiply(bq).toComplex()); assertEquals(a.divide(b),aq.divideRight(bq).toComplex());
        assertEquals(Rational.of(7,11),RationalQuaternion.scalar(Rational.of(7,11)).toRational());
        RationalVector vector=v(1,2,3); assertEquals(vector,RationalQuaternion.fromVector(vector).toVector());
        assertEquals(vector,q(9,1,2,3).imaginaryPart()); assertEquals(Arrays.asList(Rational.ZERO,Rational.ONE,Rational.ZERO,Rational.ZERO),RationalQuaternion.I.components());
        assertThrows(UnsupportedOperationException.class,() -> aq.components().clear());
    }

    @Test public void quarterTurnsHalfTurnsAndCanonicalProjectiveRecoveryAreExact() {
        RationalQuaternion aroundZ=q(1,0,0,1); RationalMatrix rz=m(new long[]{0,-1,0},new long[]{1,0,0},new long[]{0,0,1});
        assertEquals(v(-2,1,3),aroundZ.rotate(v(1,2,3))); assertEquals(rz,aroundZ.toRotationMatrix());
        assertEquals(v(0,0,1),q(1,1,0,0).rotate(v(0,1,0)));
        RationalQuaternion half=q(0,1,2,3); RationalMatrix rh=m(new long[]{-6,2,3},new long[]{2,-3,6},new long[]{3,6,2}).scale(Rational.of(1,7));
        assertEquals(rh,half.toRotationMatrix()); assertEquals(half,RationalQuaternion.fromRotationMatrix(rh));
        assertEquals(RationalQuaternion.J,RationalQuaternion.fromRotationMatrix(RationalQuaternion.J.toRotationMatrix()));
        assertEquals(RationalQuaternion.K,RationalQuaternion.fromRotationMatrix(RationalQuaternion.K.toRotationMatrix()));
        assertEquals(q(1,0,0,1),RationalQuaternion.fromRotationMatrix(q(-7,0,0,-7).toRotationMatrix()));
        assertEquals(RationalQuaternion.ONE,RationalQuaternion.fromRotationMatrix(RationalMatrix.identity(3)));
        assertFalse(aroundZ.equals(aroundZ.scale(Rational.of(2)))); assertTrue(aroundZ.sameRotation(aroundZ.scale(Rational.of(-2))));
        assertFalse(aroundZ.sameRotation(RationalQuaternion.I));
    }

    @Test public void allSmallNonzeroQuaternionsProduceExactOrientationPreservingRotations() {
        RationalVector u=v(1,2,3),v=v(-2,4,1);
        for(int code=0;code<625;code++) {
            int digits=code; long[] entries=new long[4]; for(int i=0;i<4;i++) { entries[i]=digits%5-2; digits/=5; }
            RationalQuaternion q=q(entries[0],entries[1],entries[2],entries[3]); if(q.normSquared().signum()==0) continue;
            RationalMatrix rotation=q.toRotationMatrix(); RationalVector rotated=q.rotate(u),other=q.rotate(v);
            assertEquals(RationalMatrix.identity(3),rotation.transpose().multiply(rotation)); assertEquals(Rational.ONE,rotation.determinant());
            assertEquals(rotation.multiply(u),rotated); assertEquals(u.dot(u),rotated.dot(rotated)); assertEquals(u.dot(v),rotated.dot(other));
            assertEquals(q.rotate(cross(u,v)),cross(rotated,other)); assertEquals(rotation.transpose(),q.inverse().toRotationMatrix());
            RationalQuaternion recovered=RationalQuaternion.fromRotationMatrix(rotation);
            assertTrue(q.sameRotation(recovered)); assertEquals(rotation,recovered.toRotationMatrix());
            for(Rational component : recovered.components()) if(component.signum()!=0) { assertEquals(Rational.ONE,component); break; }
            assertEquals(rotation,q.scale(Rational.of(-7,3)).toRotationMatrix());
        }
    }

    @Test public void rotationCompositionFollowsQuaternionMultiplicationOrder() {
        RationalVector input=v(2,-1,4);
        for(int code=0;code<81;code++) {
            RationalQuaternion a=ternary(code),b=ternary((31*code+19)%81); if(a.normSquared().signum()==0 || b.normSquared().signum()==0) continue;
            assertEquals(a.rotate(b.rotate(input)),a.multiply(b).rotate(input));
            assertEquals(a.toRotationMatrix().multiply(b.toRotationMatrix()),a.multiply(b).toRotationMatrix());
            assertEquals(a.multiply(b).multiply(q(2,1,-3,4)),a.multiply(b.multiply(q(2,1,-3,4))));
        }
    }

    @Test public void invalidConversionsAndNonrotationsAreRejectedWithoutApproximation() {
        undefined(RationalQuaternion.ZERO::inverse); undefined(() -> RationalQuaternion.ONE.divideRight(RationalQuaternion.ZERO));
        undefined(() -> RationalQuaternion.ONE.divideLeft(RationalQuaternion.ZERO)); undefined(RationalQuaternion.ZERO::toRotationMatrix);
        undefined(() -> RationalQuaternion.ZERO.rotate(v(1,2,3))); undefined(() -> RationalQuaternion.ZERO.sameRotation(RationalQuaternion.ONE));
        undefined(() -> RationalQuaternion.ONE.sameRotation(RationalQuaternion.ZERO));
        undefined(RationalQuaternion.J::toComplex); undefined(RationalQuaternion.I::toRational); undefined(RationalQuaternion.ONE::toVector);
        undefined(() -> RationalQuaternion.fromVector(new RationalVector())); undefined(() -> RationalQuaternion.I.rotate(new RationalVector(Rational.ONE)));
        undefined(() -> RationalQuaternion.fromRotationMatrix(RationalMatrix.identity(2)));
        undefined(() -> RationalQuaternion.fromRotationMatrix(m(new long[]{-1,0,0},new long[]{0,1,0},new long[]{0,0,1})));
        undefined(() -> RationalQuaternion.fromRotationMatrix(m(new long[]{1,1,0},new long[]{0,1,0},new long[]{0,0,1})));
        Rational epsilon=Rational.ONE.divide(Rational.of(BigInteger.TEN.pow(80)));
        RationalMatrix nearlyIdentity=new RationalMatrix(new Rational[][]{{Rational.ONE.add(epsilon),Rational.ZERO,Rational.ZERO},{Rational.ZERO,Rational.ONE,Rational.ZERO},{Rational.ZERO,Rational.ZERO,Rational.ONE}});
        undefined(() -> RationalQuaternion.fromRotationMatrix(nearlyIdentity));
    }

    @Test public void actualWrappersAndSerializedFlowsConnectQuaternionsVectorsAndMatrices() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); RationalQuaternion aroundZ=q(1,0,0,1);
        IAlgebraItem<RationalQuaternion> item=math.quaternions.algebra().buildAlgebraItem(aroundZ);
        IAlgebraItem<RationalVector> result=item.performLeftProjectionOperation("rotate",v(1,2,3));
        assertSame(math.finiteVectors.algebra(),result.getAlgebra()); assertEquals(v(-2,1,3),result.getResult());
        IAlgebraItem<RationalMatrix> rotation=item.performAlgebraTransfer("to-rotation-matrix"); assertSame(math.rectangularMatrices.algebra(),rotation.getAlgebra());
        IAlgebraItem<RationalQuaternion> restoredQuaternion=rotation.performAlgebraTransfer("H(Q).from-rotation-matrix");
        assertSame(math.quaternions.algebra(),restoredQuaternion.getAlgebra()); assertEquals(aroundZ,restoredQuaternion.getResult());
        assertSame(math.quaternions.algebra(),math.rationals.algebra().buildAlgebraItem(Rational.of(2)).performAlgebraTransfer("H(Q).embed-rational").getAlgebra());
        assertSame(math.complexRationals.algebra(),math.quaternions.algebra().buildAlgebraItem(RationalQuaternion.I).performAlgebraTransfer("to-complex").getAlgebra());
        undefined(() -> item.performOperation("divide-left",RationalQuaternion.ZERO).perform());
        IAlgebraFlow<Rational> flow=math.flow(math.quaternions,Collections.singletonList(aroundZ)).<RationalMatrix>performAlgebraTransfer("to-rotation-matrix")
                .<RationalQuaternion>performAlgebraTransfer("H(Q).from-rotation-matrix").performLeftProjectionOperation("rotate",v(1,2,3))
                .<Rational>performFlatAlgebraTransfer("entries");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Arrays.asList("-2","1","3"),restored.collect()); assertEquals(Arrays.asList("-2","1","3"),restored.collect());
    }

    @Test public void largeRationalRotationsAndInversesRetainExactCoefficients() {
        Rational huge=Rational.of(BigInteger.TEN.pow(80));
        RationalQuaternion q=new RationalQuaternion(Rational.ONE.divide(huge),huge,Rational.of(1,7),Rational.ZERO);
        assertEquals(RationalQuaternion.ONE,q.multiply(q.inverse())); assertEquals(RationalQuaternion.ONE,q.inverse().multiply(q));
        RationalMatrix rotation=q.toRotationMatrix(); assertEquals(Rational.ONE,rotation.determinant());
        assertEquals(RationalMatrix.identity(3),rotation.transpose().multiply(rotation));
        assertTrue(q.sameRotation(RationalQuaternion.fromRotationMatrix(rotation)));
        assertEquals(rotation.multiply(v(1,-2,3)),q.rotate(v(1,-2,3)));
    }
}
