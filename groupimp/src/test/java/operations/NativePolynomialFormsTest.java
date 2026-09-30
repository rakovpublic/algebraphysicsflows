package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.calculus.*;
import mathematics.core.MathFailure;
import mathematics.foundations.Pair;
import mathematics.linear.*;
import mathematics.numbers.Rational;
import org.junit.Test;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import static org.junit.Assert.*;

public class NativePolynomialFormsTest {
    private static MultivariatePolynomial p(long coefficient,int... powers) { return MultivariatePolynomial.monomial(Rational.of(coefficient),powers); }
    private static MultivariatePolynomial x(int dimension,int axis) { return MultivariatePolynomial.variable(dimension,axis); }
    private static MultivariatePolynomial c(int dimension,long value) { return MultivariatePolynomial.constant(dimension,Rational.of(value)); }
    private static PolynomialDifferentialForm term(int mask,MultivariatePolynomial coefficient) {
        return new PolynomialDifferentialForm(coefficient.variableCount(),Collections.singletonMap(mask,coefficient));
    }
    private static PolynomialDifferentialForm basis(int dimension,int mask) { return term(mask,c(dimension,1)); }
    private static RationalVector v(long... entries) {
        Rational[] values=new Rational[entries.length]; for(int i=0;i<entries.length;i++) values[i]=Rational.of(entries[i]); return new RationalVector(values);
    }
    private static RationalMatrix m(long[]... rows) {
        Rational[][] values=new Rational[rows.length][];
        for(int i=0;i<rows.length;i++) { values[i]=new Rational[rows[i].length]; for(int j=0;j<rows[i].length;j++) values[i][j]=Rational.of(rows[i][j]); }
        return new RationalMatrix(values);
    }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static void undefined(Runnable action) { failure(MathFailure.Kind.OPERATION_UNDEFINED,action); }
    private static PolynomialDifferentialForm mixed(int dimension) {
        PolynomialDifferentialForm form=PolynomialDifferentialForm.zero(dimension);
        for(int mask=0;mask<(1<<dimension);mask++) {
            int[] powers=new int[dimension]; for(int axis=0;axis<dimension;axis++) powers[axis]=(mask+axis)%3;
            form=form.add(term(mask,p((mask%5)-2,powers)));
        }
        return form;
    }
    private static RationalVector point(int dimension) { long[] values=new long[dimension]; for(int i=0;i<dimension;i++) values[i]=i-1; return v(values); }
    private static PolynomialMap field(int dimension) {
        MultivariatePolynomial[] components=new MultivariatePolynomial[dimension];
        for(int i=0;i<dimension;i++) components[i]=x(dimension,i).multiply(x(dimension,(i+1)%dimension)).add(c(dimension,i+1));
        return new PolynomialMap(components);
    }

    @Test public void formsKeepCanonicalImmutableCoefficientsAndCoordinateDimension() {
        Map<Integer,MultivariatePolynomial> input=new TreeMap<>(); input.put(1,x(3,0).add(c(3,2))); input.put(2,c(3,0));
        PolynomialDifferentialForm form=new PolynomialDifferentialForm(3,input); input.clear();
        assertEquals(term(1,x(3,0).add(c(3,2))),form); assertEquals(2,form.coefficientTermCount()); assertEquals(1,form.coefficients().size());
        assertEquals(form.hashCode(),term(1,x(3,0).add(c(3,2))).hashCode());
        assertThrows(UnsupportedOperationException.class,() -> form.coefficients().clear());
        assertThrows(UnsupportedOperationException.class,() -> form.terms().clear());
        assertEquals(Arrays.asList(1),form.degrees()); assertEquals(3,form.basis().size());
        assertEquals(PolynomialDifferentialForm.coordinate(3,1),form.basis().get(1));
        assertTrue(form.add(form.negate()).isZero()); assertTrue(PolynomialDifferentialForm.zero(3).degrees().isEmpty());
        assertNotEquals(PolynomialDifferentialForm.zero(2),PolynomialDifferentialForm.zero(3));
    }

    @Test public void wedgeSignsAgreeWithAnIndependentInversionCountForCoordinateBases() {
        for(int dimension=1;dimension<=5;dimension++) for(int first=0;first<(1<<dimension);first++) for(int second=0;second<(1<<dimension);second++) {
            PolynomialDifferentialForm a=term(first,x(dimension,0).add(c(dimension,2))),b=term(second,x(dimension,dimension-1).add(c(dimension,3)));
            PolynomialDifferentialForm actual=a.wedge(b);
            if((first&second)!=0) assertTrue(actual.isZero());
            else {
                int inversions=0; for(int i=0;i<dimension;i++) for(int j=0;j<i;j++) if(((first>>i)&1)!=0 && ((second>>j)&1)!=0) inversions++;
                RationalVector point=point(dimension); Rational coefficient=Rational.of(2).add(point.get(0)).multiply(Rational.of(3).add(point.get(dimension-1)));
                if(inversions%2!=0) coefficient=coefficient.negate();
                assertEquals(new RationalExterior(dimension,Collections.singletonMap(first|second,coefficient)),actual.evaluate(point));
                int degreeSign=(Integer.bitCount(first)*Integer.bitCount(second))%2==0?1:-1;
                assertEquals(actual,b.wedge(a).scale(Rational.of(degreeSign)));
            }
        }
        PolynomialDifferentialForm a=mixed(3),b=term(3,x(3,2)).add(term(0,x(3,0))),c=term(4,x(3,1)).add(basis(3,0));
        assertEquals(a.wedge(b).wedge(c),a.wedge(b.wedge(c)));
    }

    @Test public void exteriorDerivativeHasCorrectSignsSquaresToZeroAndObeysGradedProductRule() {
        assertEquals(term(3,c(2,-1)),term(1,x(2,1)).exteriorDerivative());
        assertEquals(term(3,c(2,1)),term(2,x(2,0)).exteriorDerivative());
        assertEquals(term(7,x(3,0)),term(6,p(1,2,0,0)).exteriorDerivative().scale(Rational.of(1,2)));
        for(int dimension=1;dimension<=4;dimension++) {
            PolynomialDifferentialForm a=mixed(dimension),b=a.hodgeStar().add(PolynomialDifferentialForm.one(dimension));
            assertTrue(a.exteriorDerivative().exteriorDerivative().isZero());
            assertEquals(a.exteriorDerivative().wedge(b).add(a.gradeInvolution().wedge(b.exteriorDerivative())),a.wedge(b).exteriorDerivative());
            assertTrue(PolynomialDifferentialForm.volume(dimension).multiplyPolynomial(x(dimension,0)).exteriorDerivative().isZero());
        }
    }

    @Test public void hodgeAndVectorFieldConversionsRecoverGradientCurlAndDivergence() {
        MultivariatePolynomial scalar=p(1,2,1,0).add(p(3,0,1,2));
        assertEquals(PolynomialMap.gradient(scalar),PolynomialDifferentialForm.scalar(scalar).exteriorDerivative().toVectorField());
        PolynomialMap vector=new PolynomialMap(p(1,1,1,0),p(1,0,1,1),p(1,1,0,1));
        PolynomialDifferentialForm lowered=PolynomialDifferentialForm.fromVectorField(vector);
        assertEquals(vector,lowered.toVectorField()); assertEquals(vector.curl(),lowered.exteriorDerivative().hodgeStar().toVectorField());
        assertEquals(vector.divergence(),lowered.hodgeStar().exteriorDerivative().hodgeStar().toPolynomial());
        for(int dimension=1;dimension<=5;dimension++) for(int degree=0;degree<=dimension;degree++) {
            PolynomialDifferentialForm a=mixed(dimension).grade(degree); int sign=(degree*(dimension-degree))%2==0?1:-1;
            assertEquals(a.scale(Rational.of(sign)),a.hodgeStar().hodgeStar());
        }
        assertEquals(PolynomialDifferentialForm.volume(3),PolynomialDifferentialForm.one(3).hodgeStar());
    }

    @Test public void insertionPairsWithPolynomialVectorFieldsAndLieDerivativeUsesCartansFormula() {
        PolynomialMap euler=PolynomialMap.identity(3); PolynomialDifferentialForm form=term(3,p(1,2,1,0));
        assertEquals(term(2,p(1,3,1,0)).subtract(term(1,p(1,2,2,0))),form.interior(euler));
        // Euler's field counts polynomial degree plus differential degree: (2+1)+(1+1)=5.
        assertEquals(form.scale(Rational.of(5)),form.lieDerivative(euler));
        for(int dimension=1;dimension<=4;dimension++) {
            PolynomialDifferentialForm a=mixed(dimension),b=a.hodgeStar(); PolynomialMap vector=field(dimension);
            assertTrue(a.interior(vector).interior(vector).isZero());
            assertEquals(a.interior(vector).wedge(b).add(a.gradeInvolution().wedge(b.interior(vector))),a.wedge(b).interior(vector));
            assertEquals(a.lieDerivative(vector).exteriorDerivative(),a.exteriorDerivative().lieDerivative(vector));
            assertEquals(a.lieDerivative(vector).wedge(b).add(a.wedge(b.lieDerivative(vector))),a.wedge(b).lieDerivative(vector));
            assertEquals(a.evaluate(point(dimension)).interior(vector.evaluate(point(dimension))),a.interior(vector).evaluate(point(dimension)));
        }
        PolynomialDifferentialForm scalar=PolynomialDifferentialForm.scalar(p(1,2,1));
        assertEquals(p(3,2,1),scalar.lieDerivative(PolynomialMap.identity(2)).toPolynomial());
    }

    @Test public void pullbackSubstitutesCoefficientsAndUsesTheTransposedJacobian() {
        PolynomialMap map=new PolynomialMap(p(1,2,0),p(1,0,2),p(1,1,1));
        PolynomialDifferentialForm volumeXY=term(3,x(3,2));
        assertEquals(term(3,p(4,2,2)),volumeXY.pullback(map));
        PolynomialDifferentialForm form=mixed(3); RationalVector point=v(2,3);
        assertEquals(form.evaluate(map.evaluate(point)).map(map.jacobianAt(point).transpose()),form.pullback(map).evaluate(point));
        assertEquals(form.exteriorDerivative().pullback(map),form.pullback(map).exteriorDerivative());
        assertEquals(form.wedge(form.hodgeStar()).pullback(map),form.pullback(map).wedge(form.hodgeStar().pullback(map)));
        assertTrue(PolynomialDifferentialForm.volume(3).pullback(map).isZero());
        assertEquals(2,PolynomialDifferentialForm.zero(3).pullback(map).dimension());
    }

    @Test public void linearPullbacksOfAllTernaryRectangularMapsRecoverIndependentMinors() {
        for(int code=0;code<729;code++) {
            int digits=code; long[][] entries=new long[3][2];
            for(int i=0;i<3;i++) for(int j=0;j<2;j++) { entries[i][j]=digits%3-1; digits/=3; }
            PolynomialMap map=PolynomialMap.fromMatrix(m(entries));
            for(int i=0;i<3;i++) for(int j=i+1;j<3;j++) {
                long minor=entries[i][0]*entries[j][1]-entries[i][1]*entries[j][0];
                assertEquals(term(3,c(2,minor)),basis(3,(1<<i)|(1<<j)).pullback(map));
            }
        }
    }

    @Test public void pullbackCompositionIsContravariantIncludingNonlinearRectangularMaps() {
        PolynomialMap f=new PolynomialMap(p(1,2,0),p(1,1,1),p(1,0,2));
        PolynomialMap g=new PolynomialMap(x(1,0).add(c(1,1)),p(1,2));
        PolynomialDifferentialForm form=mixed(3);
        assertEquals(form.pullback(f.compose(g)),form.pullback(f).pullback(g));
        assertEquals(form,form.pullback(PolynomialMap.identity(3)));
        PolynomialMap constant=new PolynomialMap(c(2,3),c(2,4),c(2,5));
        assertTrue(form.grade(1).pullback(constant).isZero());
        assertEquals(form.scalarPart().evaluate(v(3,4,5)),form.grade(0).pullback(constant).toPolynomial().toRational());
    }

    @Test public void orientedRectangleBoundaryPullbacksAgreeWithAnExactStokesIntegral() {
        PolynomialDifferentialForm form=term(1,p(1,2,1)).add(term(2,p(1,1,2)));
        assertEquals(term(3,p(1,0,2).subtract(p(1,2,0))),form.exteriorDerivative());
        MultivariatePolynomial t=x(1,0),reverse=c(1,1).subtract(t);
        PolynomialMap[] boundary={new PolynomialMap(t.scale(Rational.of(2)),c(1,0)),
                new PolynomialMap(c(1,2),t.scale(Rational.of(3))),
                new PolynomialMap(reverse.scale(Rational.of(2)),c(1,3)),
                new PolynomialMap(c(1,0),reverse.scale(Rational.of(3)))};
        ConcreteMathematics math=new ConcreteMathematics(); Rational sum=Rational.ZERO;
        for(PolynomialMap edge : boundary) {
            IAlgebraItem<PolynomialDifferentialForm> pulled=math.polynomialForms.algebra().buildAlgebraItem(form).performCustomMemberOperation("pullback",edge);
            IAlgebraItem<PolynomialMap> field=pulled.performAlgebraTransfer("to-vector-field");
            IAlgebraItem<MultivariatePolynomial> coefficient=field.performUnsafeOperation("component",BigInteger.ZERO);
            IAlgebraItem<Polynomial> polynomial=coefficient.performAlgebraTransfer("to-univariate");
            IAlgebraItem<Rational> integral=polynomial.performUnsafeOperation("integrate",new Pair<>(Rational.ZERO,Rational.ONE));
            sum=sum.add(integral.getResult());
        }
        // Integral over [0,2] x [0,3] of (y^2-x^2): 2*(3^3/3)-3*(2^3/3)=10.
        assertEquals(Rational.of(10),sum);
    }

    @Test public void constantFormsAndPointwiseOperationsConnectToTheExistingExteriorCarrier() {
        PolynomialDifferentialForm a=mixed(3),b=a.grade(2).add(PolynomialDifferentialForm.one(3)); RationalVector point=v(2,-1,3);
        assertEquals(a.evaluate(point).wedge(b.evaluate(point)),a.wedge(b).evaluate(point));
        assertEquals(a.evaluate(point).hodgeStar(),a.hodgeStar().evaluate(point));
        RationalExterior value=a.evaluate(point); PolynomialDifferentialForm constant=PolynomialDifferentialForm.fromExterior(value);
        assertEquals(value,constant.toExterior()); assertEquals(value,constant.evaluate(v(9,8,7))); assertTrue(constant.exteriorDerivative().isZero());
        assertEquals(c(3,0),PolynomialDifferentialForm.zero(3).toPolynomial());
        assertEquals(new PolynomialMap(c(3,0),c(3,0),c(3,0)),PolynomialDifferentialForm.zero(3).toVectorField());
    }

    @Test public void invalidShapesUndefinedConversionsAndRepresentationLimitsStayDistinct() {
        failure(MathFailure.Kind.INVALID_MEMBER,() -> PolynomialDifferentialForm.zero(0));
        failure(MathFailure.Kind.INVALID_MEMBER,() -> term(4,c(2,1)));
        failure(MathFailure.Kind.INVALID_MEMBER,() -> new PolynomialDifferentialForm(2,Collections.singletonMap(1,c(3,1))));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> PolynomialDifferentialForm.one(21));
        undefined(() -> basis(2,1).wedge(basis(3,1))); undefined(() -> basis(2,1).add(basis(3,1)));
        undefined(() -> basis(2,1).multiplyPolynomial(c(32,1))); undefined(() -> basis(2,1).grade(-1));
        undefined(() -> basis(2,1).pullback(PolynomialMap.identity(3))); undefined(() -> basis(2,1).interior(PolynomialMap.identity(3)));
        undefined(() -> basis(2,1).lieDerivative(PolynomialMap.identity(3)));
        undefined(() -> basis(2,1).toPolynomial()); undefined(() -> basis(2,3).toVectorField());
        undefined(() -> term(1,x(2,0)).toExterior()); undefined(() -> PolynomialDifferentialForm.zero(2).evaluate(v(1)));
        undefined(() -> PolynomialDifferentialForm.fromVectorField(new PolynomialMap(x(2,0))));
        undefined(() -> PolynomialDifferentialForm.fromExterior(RationalExterior.zero(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> basis(2,1).pullback(new PolynomialMap(x(21,0),x(21,1))));
        Map<List<Integer>,Rational> terms=new HashMap<>(); for(int i=0;i<1001;i++) terms.put(Collections.singletonList(i),Rational.ONE);
        PolynomialDifferentialForm dense=term(0,new MultivariatePolynomial(1,terms));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> dense.wedge(dense));
    }

    @Test public void nativeWrappersAndSerializedCurlFlowsRetainTheRegisteredAlgebras() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); PolynomialMap rotation=new PolynomialMap(x(3,1).negate(),x(3,0),c(3,0));
        IAlgebraItem<PolynomialDifferentialForm> form=math.polynomialMaps.algebra().buildAlgebraItem(rotation).performAlgebraTransfer("PolynomialForm(Q).from-vector-field");
        assertSame(math.polynomialForms.algebra(),form.getAlgebra());
        IAlgebraItem<RationalExterior> value=form.performUnsafeOperation("evaluate",v(2,3,4));
        assertSame(math.exterior.algebra(),value.getAlgebra()); assertEquals(new RationalVector(Rational.of(-3),Rational.of(2),Rational.ZERO),value.getResult().toVector());
        for(IAlgebraItem<MultivariatePolynomial> coefficient : form.<MultivariatePolynomial>performAlgebraFlatTransfer("coefficients")) assertSame(math.multivariatePolynomials.algebra(),coefficient.getAlgebra());
        for(IAlgebraItem<PolynomialDifferentialForm> basis : form.performOneOperandFlatOperation("basis")) assertSame(math.polynomialForms.algebra(),basis.getAlgebra());
        assertTrue(form.performCustomMemberOperation("grade",BigInteger.TEN.pow(80)).getResult().isZero());
        IAlgebraFlow<Rational> flow=math.flow(math.polynomialMaps,Collections.singletonList(rotation))
                .<PolynomialDifferentialForm>performAlgebraTransfer("PolynomialForm(Q).from-vector-field")
                .performOneOperandOperation("exterior-derivative").performOneOperandOperation("hodge-star")
                .<PolynomialMap>performAlgebraTransfer("to-vector-field").performLeftProjectionOperation("evaluate",v(2,3,4))
                .<Rational>performFlatAlgebraTransfer("entries");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Arrays.asList("0","0","2"),restored.collect()); assertEquals(Arrays.asList("0","0","2"),restored.collect());
    }

    @Test public void coefficientLimitsAndMultiplicationBudgetsApplyToTheWholeForm() {
        Map<List<Integer>,Rational> powers=new HashMap<>();
        for(int i=0;i<1000;i++) powers.put(Arrays.asList(i,0,0,0,0,0,0),Rational.ONE);
        MultivariatePolynomial dense=new MultivariatePolynomial(7,powers); Map<Integer,MultivariatePolynomial> terms=new TreeMap<>();
        for(int mask=0;mask<100;mask++) terms.put(mask,dense);
        assertEquals(100000,new PolynomialDifferentialForm(7,terms).coefficientTermCount());
        terms.put(100,dense);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> new PolynomialDifferentialForm(7,terms));
        powers.clear(); for(int i=0;i<710;i++) powers.put(Arrays.asList(i,0),Rational.ONE);
        MultivariatePolynomial factor=new MultivariatePolynomial(2,powers);
        PolynomialDifferentialForm first=term(1,factor),second=term(2,factor);
        assertEquals(Rational.of(710L*710L),first.multiplyPolynomial(factor).evaluate(v(1,0)).coefficients().get(1));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> first.add(second).multiplyPolynomial(factor));
    }
}
