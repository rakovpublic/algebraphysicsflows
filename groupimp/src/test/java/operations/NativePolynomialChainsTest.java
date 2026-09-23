package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.calculus.*;
import mathematics.core.MathFailure;
import mathematics.linear.*;
import mathematics.numbers.Rational;
import org.junit.Test;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import static org.junit.Assert.*;

public class NativePolynomialChainsTest {
    private static MultivariatePolynomial p(long coefficient,int... powers) { return MultivariatePolynomial.monomial(Rational.of(coefficient),powers); }
    private static MultivariatePolynomial x(int n,int i) { return MultivariatePolynomial.variable(n,i); }
    private static MultivariatePolynomial c(int n,long value) { return MultivariatePolynomial.constant(n,Rational.of(value)); }
    private static PolynomialDifferentialForm form(int mask,MultivariatePolynomial coefficient) { return new PolynomialDifferentialForm(coefficient.variableCount(),Collections.singletonMap(mask,coefficient)); }
    private static RationalVector v(long... values) {
        Rational[] entries=new Rational[values.length]; for(int i=0;i<values.length;i++) entries[i]=Rational.of(values[i]); return new RationalVector(entries);
    }
    private static PolynomialCell cube(int dimension) { return PolynomialCell.parameterized(PolynomialMap.identity(dimension)); }
    private static PolynomialChain zero(int ambient,int degree) { return PolynomialChain.zero(ambient,BigInteger.valueOf(degree)); }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static void undefined(Runnable action) { failure(MathFailure.Kind.OPERATION_UNDEFINED,action); }

    @Test public void unitCubeIntegralsMatchIndependentTensorSimpsonQuadratureForAllTernaryQuadratics() {
        int[][] powers={{0,0},{1,0},{0,1},{2,0},{1,1},{0,2}}; Rational[] nodes={Rational.ZERO,Rational.of(1,2),Rational.ONE}; long[] weights={1,4,1};
        for(int code=0;code<729;code++) {
            int digits=code; Map<List<Integer>,Rational> coefficients=new HashMap<>();
            for(int[] power : powers) { coefficients.put(Arrays.asList(power[0],power[1]),Rational.of(digits%3-1)); digits/=3; }
            MultivariatePolynomial polynomial=new MultivariatePolynomial(2,coefficients); Rational quadrature=Rational.ZERO;
            for(int i=0;i<3;i++) for(int j=0;j<3;j++) quadrature=quadrature.add(polynomial.evaluate(new RationalVector(nodes[i],nodes[j])).multiply(Rational.of(weights[i]*weights[j],36)));
            assertEquals(quadrature,polynomial.integrateUnitCube()); assertEquals(quadrature,form(3,polynomial).integrateUnitCube());
        }
        assertEquals(Rational.of(1,120),p(1,2,3,9).integrateUnitCube());
        assertEquals(Rational.of(1,10001),p(1,10000).integrateUnitCube());
        assertEquals(Rational.ZERO,c(4,0).integrateUnitCube());
    }

    @Test public void orientedSegmentsCurvesAndFoldedParametrizationsIntegrateExactly() {
        PolynomialCell segment=PolynomialCell.segment(v(2),v(-1));
        assertEquals(Rational.of(-3),segment.integrate(form(1,p(1,2))));
        PolynomialCell curve=PolynomialCell.parameterized(new PolynomialMap(x(1,0),p(1,2),p(1,3)));
        assertEquals(Rational.of(11,15),curve.integrate(form(1,x(3,1)).add(form(2,x(3,2)))));
        PolynomialCell folded=PolynomialCell.parameterized(new PolynomialMap(x(1,0).subtract(p(1,2)).scale(Rational.of(4))));
        assertEquals(Rational.ZERO,folded.integrate(form(1,x(1,0))));
        assertFalse(PolynomialChain.of(folded).isZero()); assertTrue(folded.boundary().isZero());
        PolynomialCell constant=PolynomialCell.segment(v(3),v(3));
        assertEquals(1,constant.dimension()); assertNotEquals(PolynomialCell.point(v(3)),constant);
        assertEquals(Rational.ZERO,constant.integrate(form(1,c(1,7))));
        assertEquals(Arrays.asList(v(3),v(3)),constant.vertices()); assertEquals(Arrays.asList(PolynomialCell.point(v(3)),PolynomialCell.point(v(3))),constant.faces());
    }

    @Test public void explicitSurfacePullbacksRetainOrientationWithoutAbsoluteJacobians() {
        PolynomialCell surface=PolynomialCell.parameterized(new PolynomialMap(x(2,0),x(2,1),p(1,1,1)));
        assertEquals(Rational.of(-1,4),surface.integrate(form(6,x(3,0))));
        PolynomialMap swap=new PolynomialMap(x(2,1),x(2,0));
        assertEquals(Rational.of(1,4),PolynomialCell.parameterized(surface.parameterization().compose(swap)).integrate(form(6,x(3,0))));
        PolynomialCell collapsed=PolynomialCell.parameterized(new PolynomialMap(x(2,0),x(2,0)));
        assertEquals(Rational.ZERO,collapsed.integrate(form(3,c(2,1))));
    }

    @Test public void faceOrderAndBoundarySignsAreExplicitAndPointDegreesDoNotDisappear() {
        PolynomialCell square=cube(2); List<PolynomialCell> faces=square.faces();
        assertEquals(Arrays.asList(v(0,0),v(1,0),v(0,1),v(1,1)),square.vertices());
        assertEquals(Arrays.asList(v(0,0),v(0,1)),faces.get(0).vertices());
        assertEquals(Arrays.asList(v(1,0),v(1,1)),faces.get(1).vertices());
        assertEquals(Arrays.asList(v(0,0),v(1,0)),faces.get(2).vertices());
        assertEquals(Arrays.asList(v(0,1),v(1,1)),faces.get(3).vertices());
        PolynomialChain boundary=square.boundary(); assertEquals(BigInteger.ONE,boundary.degree());
        assertEquals(Rational.of(-1),boundary.coefficient(faces.get(0))); assertEquals(Rational.ONE,boundary.coefficient(faces.get(1)));
        assertEquals(Rational.ONE,boundary.coefficient(faces.get(2))); assertEquals(Rational.of(-1),boundary.coefficient(faces.get(3)));
        assertEquals(zero(2,0),boundary.boundary());
        PolynomialCell point=PolynomialCell.point(v(2,3)); assertEquals(v(2,3),point.evaluate(v()));
        assertEquals(Rational.of(8),point.integrate(form(0,p(1,1,1).add(c(2,2)))));
        assertEquals(zero(2,-1),point.boundary()); assertEquals(zero(2,-2),point.boundary().boundary());
        BigInteger negative=BigInteger.TEN.pow(80).negate();
        assertEquals(negative.subtract(BigInteger.ONE),PolynomialChain.zero(2,negative).boundary().degree());
    }

    @Test public void allTernaryRectangularLinearCellsHaveIndependentAreaIntegralsAndZeroDoubleBoundary() {
        for(int code=0;code<729;code++) {
            int digits=code; long[][] entries=new long[3][2]; MultivariatePolynomial[] coordinates=new MultivariatePolynomial[3];
            for(int i=0;i<3;i++) {
                entries[i][0]=digits%3-1; digits/=3; entries[i][1]=digits%3-1; digits/=3;
                coordinates[i]=x(2,0).scale(Rational.of(entries[i][0])).add(x(2,1).scale(Rational.of(entries[i][1])));
            }
            PolynomialCell cell=PolynomialCell.parameterized(new PolynomialMap(coordinates)); assertEquals(zero(3,0),cell.boundary().boundary());
            for(int i=0;i<3;i++) for(int j=i+1;j<3;j++)
                assertEquals(Rational.of(entries[i][0]*entries[j][1]-entries[i][1]*entries[j][0]),cell.integrate(form((1<<i)|(1<<j),c(3,1))));
        }
    }

    @Test public void nonlinearStokesIdentitiesHoldInDimensionsOneThroughFour() {
        for(int dimension=1;dimension<=4;dimension++) {
            MultivariatePolynomial[] coordinates=new MultivariatePolynomial[dimension];
            for(int i=0;i<dimension;i++) coordinates[i]=x(dimension,i);
            coordinates[dimension-1]=coordinates[dimension-1].add(x(dimension,0).multiply(x(dimension,dimension==1?0:1)));
            PolynomialCell cell=PolynomialCell.parameterized(new PolynomialMap(coordinates));
            for(int omitted=0;omitted<dimension;omitted++) for(int exponent=0;exponent<=3;exponent++) {
                int mask=((1<<dimension)-1)^(1<<omitted); int[] powers=new int[dimension]; Arrays.fill(powers,1); powers[omitted]=exponent;
                PolynomialDifferentialForm omega=form(mask,p(exponent+1,powers));
                assertEquals(cell.integrate(omega.exteriorDerivative()),cell.boundary().integrate(omega));
            }
            assertEquals(zero(dimension,dimension-2),cell.boundary().boundary());
        }
        PolynomialCell rectangle=PolynomialCell.parameterized(new PolynomialMap(x(2,0).scale(Rational.of(2)),x(2,1).scale(Rational.of(3))));
        PolynomialDifferentialForm omega=form(1,p(1,2,1)).add(form(2,p(1,1,2)));
        assertEquals(Rational.of(10),rectangle.integrate(omega.exteriorDerivative())); assertEquals(Rational.of(10),rectangle.boundary().integrate(omega));
    }

    @Test public void chainProductsObeyTheGradedBoundaryRuleIncludingPoints() {
        for(int first=0;first<=3;first++) for(int second=0;second<=3;second++) {
            PolynomialCell a=first==0?PolynomialCell.point(v(2)):cube(first),b=second==0?PolynomialCell.point(v(3)):cube(second);
            PolynomialChain ac=PolynomialChain.of(a).scale(Rational.of(2,3)),bc=PolynomialChain.of(b).scale(Rational.of(-3,7));
            PolynomialChain product=ac.product(bc); Rational sign=Rational.of(first%2==0?1:-1);
            assertEquals(ac.boundary().product(bc).add(ac.product(bc.boundary()).scale(sign)),product.boundary());
            assertEquals(zero(a.ambientDimension()+b.ambientDimension(),first+second-2),product.boundary().boundary());
            if(first>0 && second>0) assertEquals(Rational.of(-2,7),product.integrate(PolynomialDifferentialForm.volume(first+second)));
        }
        assertEquals(v(2,3),PolynomialCell.point(v(2)).product(PolynomialCell.point(v(3))).point());
    }

    @Test public void pushforwardCommutesWithBoundaryAndIntegrationPairsNaturallyWithPullback() {
        PolynomialCell surface=PolynomialCell.parameterized(new PolynomialMap(x(2,0),x(2,1),p(1,1,1)));
        PolynomialMap outer=new PolynomialMap(p(1,2,0,0).add(x(3,1)),x(3,2).add(p(1,1,1,0)));
        PolynomialDifferentialForm omega=form(3,x(2,0).add(c(2,1))); PolynomialChain chain=PolynomialChain.of(surface).scale(Rational.of(3,5));
        assertEquals(chain.pushforward(outer).boundary(),chain.boundary().pushforward(outer));
        assertEquals(chain.pushforward(outer).integrate(omega),chain.integrate(omega.pullback(outer)));
        PolynomialMap next=new PolynomialMap(x(2,1),x(2,0).add(c(2,2)));
        assertEquals(chain.pushforward(outer).pushforward(next),chain.pushforward(next.compose(outer)));
        assertEquals(PolynomialCell.point(v(5,6)),PolynomialCell.point(v(2,3)).pushforward(new PolynomialMap(x(2,0).add(x(2,1)),p(1,1,1))));
    }

    @Test public void formalChainEqualityIsCanonicalWithoutIdentifyingGeometricImages() {
        PolynomialCell a=PolynomialCell.segment(v(0),v(1)),b=PolynomialCell.parameterized(new PolynomialMap(p(1,2)));
        assertNotEquals(a,b); assertNotEquals(PolynomialChain.of(a),PolynomialChain.of(b));
        assertEquals(a.integrate(form(1,c(1,1))),b.integrate(form(1,c(1,1))));
        Map<PolynomialCell,Rational> input=new HashMap<>(); input.put(a,Rational.of(2,3)); input.put(b,Rational.of(1,7));
        PolynomialChain chain=new PolynomialChain(1,1,input); input.clear();
        assertEquals(Rational.of(17,21),chain.integrate(form(1,c(1,1))));
        assertEquals(2,chain.coefficients().size()); assertThrows(UnsupportedOperationException.class,() -> chain.coefficients().clear());
        assertEquals(zero(1,1),chain.subtract(chain)); assertNotEquals(zero(1,1),zero(1,0)); assertNotEquals(zero(1,1),zero(2,1));
        PolynomialChain reordered=PolynomialChain.of(b).scale(Rational.of(1,7)).add(PolynomialChain.of(a).scale(Rational.of(2,3)));
        assertEquals(chain,reordered); assertEquals(chain.hashCode(),reordered.hashCode()); assertEquals(chain.toString(),reordered.toString());
        assertEquals(0,a.compareTo(PolynomialCell.segment(v(0),v(1))));
        assertEquals(0,PolynomialCell.point(v(2,3)).compareTo(PolynomialCell.point(v(2,3))));
    }

    @Test public void wrongDegreesDimensionsAndParameterDomainsAreRejectedEvenForEmptyChains() {
        undefined(() -> cube(2).evaluate(v(0))); undefined(() -> cube(1).evaluate(v(-1))); undefined(() -> cube(1).evaluate(v(2)));
        undefined(() -> PolynomialCell.point(v(1)).face(0,false)); undefined(() -> cube(2).axis(BigInteger.TEN.pow(80)));
        undefined(() -> cube(2).point()); undefined(() -> PolynomialCell.point(v(1)).parameterization());
        undefined(() -> PolynomialCell.point(v())); undefined(() -> PolynomialCell.segment(v(1),v(1,2)));
        undefined(() -> cube(2).integrate(form(1,c(2,1)))); undefined(() -> cube(2).integrate(form(3,c(3,1))));
        undefined(() -> cube(2).integrate(form(3,c(2,1)).add(form(0,c(2,1)))));
        undefined(() -> form(1,c(2,1)).integrateUnitCube());
        undefined(() -> zero(2,2).integrate(form(1,c(2,1)))); undefined(() -> zero(2,2).integrate(PolynomialDifferentialForm.zero(3)));
        undefined(() -> zero(2,1).add(zero(2,0))); undefined(() -> zero(2,1).coefficient(PolynomialCell.point(v(1,2))));
        undefined(() -> zero(2,1).pushforward(PolynomialMap.identity(3))); undefined(() -> cube(2).pushforward(PolynomialMap.identity(3)));
        failure(MathFailure.Kind.INVALID_MEMBER,() -> new PolynomialChain(2,1,Collections.singletonMap(cube(1),Rational.ONE)));
        failure(MathFailure.Kind.INVALID_MEMBER,() -> new PolynomialChain(1,-1,Collections.singletonMap(PolynomialCell.point(v(1)),Rational.ONE)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> cube(11));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> cube(6).product(cube(5)));
        assertEquals(Rational.ZERO,zero(2,-1).integrate(PolynomialDifferentialForm.zero(2)));
    }

    @Test public void chainSupportAndCellVisitCapsAreExplicitImplementationFailures() {
        Map<PolynomialCell,Rational> points=new TreeMap<>();
        for(int i=0;i<317;i++) points.put(PolynomialCell.point(v(i)),Rational.ONE);
        PolynomialChain manyPoints=new PolynomialChain(1,0,points);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> manyPoints.product(manyPoints));
        for(int i=317;i<=PolynomialChain.MAX_CELLS;i++) points.put(PolynomialCell.point(v(i)),Rational.ONE);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> new PolynomialChain(1,0,points));
        Map<PolynomialCell,Rational> cells=new TreeMap<>();
        for(int i=0;i<5001;i++) cells.put(PolynomialCell.parameterized(new PolynomialMap(c(10,i))),Rational.ONE);
        PolynomialChain manyCells=new PolynomialChain(1,10,cells);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,manyCells::boundary);
    }

    @Test public void pointIntegrationUsesOneSharedWorkBudgetAcrossTheWholeChain() {
        Map<PolynomialCell,Rational> points=new TreeMap<>();
        for(int code=0;code<2500;code++) {
            long[] coordinates=new long[20]; for(int i=0;i<20;i++) coordinates[i]=(code>>i)&1;
            points.put(PolynomialCell.point(v(coordinates)),Rational.ONE);
        }
        MultivariatePolynomial scalar=c(20,0); for(int i=0;i<20;i++) scalar=scalar.add(x(20,i));
        PolynomialDifferentialForm omega=PolynomialDifferentialForm.scalar(scalar);
        assertEquals(Rational.ZERO,points.keySet().iterator().next().integrate(omega));
        PolynomialChain chain=new PolynomialChain(20,0,points);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> chain.integrate(omega));
        Map<List<Integer>,Rational> coefficients=new HashMap<>();
        for(int exponent=0;exponent<=100;exponent++) {
            List<Integer> powers=new ArrayList<>(Collections.nCopies(10,0)); powers.set(0,exponent); coefficients.put(powers,Rational.ONE);
        }
        PolynomialCell manyVertices=PolynomialCell.parameterized(new PolynomialMap(new MultivariatePolynomial(10,coefficients)));
        assertEquals(v(101),manyVertices.evaluate(v(1,0,0,0,0,0,0,0,0,0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,manyVertices::vertices);
    }

    @Test public void actualWrappersAndSerializedBoundaryIntegrationFlowsUseMathTool() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); PolynomialCell rectangle=PolynomialCell.parameterized(new PolynomialMap(x(2,0).scale(Rational.of(2)),x(2,1).scale(Rational.of(3))));
        PolynomialDifferentialForm omega=form(1,p(1,2,1)).add(form(2,p(1,1,2)));
        IAlgebraItem<PolynomialCell> item=math.polynomialCells.algebra().buildAlgebraItem(rectangle);
        IAlgebraItem<RationalVector> evaluated=item.performLeftProjectionOperation("evaluate",new RationalVector(Rational.of(1,2),Rational.of(1,3)));
        assertSame(math.finiteVectors.algebra(),evaluated.getAlgebra()); assertEquals(v(1,1),evaluated.getResult());
        IAlgebraItem<PolynomialChain> boundary=item.performAlgebraTransfer("PolynomialChain(Q).boundary-of-cell"); assertSame(math.polynomialChains.algebra(),boundary.getAlgebra());
        IAlgebraItem<Rational> result=boundary.performUnsafeOperation("integrate",omega); assertSame(math.rationals.algebra(),result.getAlgebra()); assertEquals(Rational.of(10),result.getResult());
        for(IAlgebraItem<PolynomialCell> cell : boundary.<PolynomialCell>performAlgebraFlatTransfer("cells")) assertSame(math.polynomialCells.algebra(),cell.getAlgebra());
        for(IAlgebraItem<PolynomialCell> face : item.performOneOperandFlatOperation("faces")) assertSame(math.polynomialCells.algebra(),face.getAlgebra());
        IAlgebraFlow<Rational> flow=math.flow(math.polynomialCells,Collections.singletonList(rectangle))
                .<PolynomialChain>performAlgebraTransfer("PolynomialChain(Q).from-cell").performOneOperandOperation("boundary")
                .<Rational,PolynomialDifferentialForm>performAlgebraUnsafe("integrate",omega);
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("10"),restored.collect()); assertEquals(Collections.singletonList("10"),restored.collect());
        assertEquals(Collections.singletonList("10"),math.flow(math.polynomialCells,Collections.singletonList(rectangle))
                .<Rational,PolynomialDifferentialForm>performAlgebraUnsafe("integrate",omega.exteriorDerivative()).collect());
    }
}
