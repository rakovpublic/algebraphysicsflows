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

public class NativeMultivariateCalculusTest {
    private static MultivariatePolynomial term(long coefficient,int... powers) { return MultivariatePolynomial.monomial(Rational.of(coefficient),powers); }
    private static MultivariatePolynomial x(int dimension,int axis) { return MultivariatePolynomial.variable(dimension,axis); }
    private static MultivariatePolynomial c(int dimension,long value) { return MultivariatePolynomial.constant(dimension,Rational.of(value)); }
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
    private static MultivariatePolynomial example() { return term(1,2,1,0).add(term(3,0,1,2)).add(c(3,5)); }

    @Test public void sparseMembersAreCanonicalImmutableAndRetainTheirInputSpace() {
        List<Integer> powers=new ArrayList<>(Arrays.asList(2,1)); Map<List<Integer>,Rational> input=new HashMap<>();
        input.put(powers,Rational.of(3)); input.put(Arrays.asList(0,0),Rational.ZERO);
        MultivariatePolynomial p=new MultivariatePolynomial(2,input); powers.set(0,0); input.clear();
        assertEquals(term(3,2,1),p); assertEquals(3,p.degree()); assertEquals(1,p.coefficients().size());
        assertEquals(p.hashCode(),term(3,2,1).hashCode()); assertEquals("Poly(Q^2){[2, 1]=3}",p.toString());
        assertThrows(UnsupportedOperationException.class,() -> p.coefficients().clear());
        assertThrows(UnsupportedOperationException.class,() -> p.coefficients().firstKey().set(0,7));
        assertEquals(-1,c(2,0).degree()); assertEquals(0,c(2,9).degree()); assertNotEquals(c(2,0),c(3,0));
        assertEquals(c(2,0),p.add(p.negate())); assertTrue(p.subtract(p).terms().isEmpty());
    }

    @Test public void explicitPartialGradientHessianAndLaplacianValuesAreExact() {
        MultivariatePolynomial p=example(); RationalVector point=v(2,3,-1);
        assertEquals(Rational.of(26),p.evaluate(point));
        assertEquals(term(2,1,1,0),p.partial(0)); assertEquals(term(1,2,0,0).add(term(3,0,0,2)),p.partial(1));
        assertEquals(term(6,0,1,1),p.partial(2)); assertEquals(v(12,7,-18),p.gradientAt(point));
        assertEquals(m(new long[]{6,4,0},new long[]{4,0,-6},new long[]{0,-6,18}),p.hessianAt(point));
        assertEquals(term(8,0,1,0),p.laplacian());
        assertEquals(Rational.of(24),p.hessianAt(point).trace());
        assertEquals(Rational.of(-5),p.directional(v(2,1,2)).evaluate(point));
        assertEquals(c(3,0),p.directional(v(0,0,0)));
    }

    @Test public void allTernaryQuadraticsMatchIndependentCenteredDifferenceIdentities() {
        int[][] powers={{0,0},{1,0},{0,1},{2,0},{1,1},{0,2}};
        RationalVector point=new RationalVector(Rational.of(1,3),Rational.of(-2,5));
        RationalVector first=v(2,-1),second=v(1,3);
        for(int code=0;code<729;code++) {
            int digits=code; Map<List<Integer>,Rational> terms=new HashMap<>();
            for(int[] power : powers) { terms.put(Arrays.asList(power[0],power[1]),Rational.of(digits%3-1)); digits/=3; }
            MultivariatePolynomial p=new MultivariatePolynomial(2,terms);
            Rational forward=p.evaluate(point.add(first)),backward=p.evaluate(point.add(first.scale(Rational.of(-1))));
            // These centered differences are exact for every quadratic, without calling a derivative oracle.
            Rational firstDifference=forward.subtract(backward).divide(Rational.of(2));
            Rational secondDifference=forward.add(backward).subtract(p.evaluate(point).multiply(Rational.of(2)));
            assertEquals(firstDifference,p.directional(first).evaluate(point));
            assertEquals(firstDifference,p.gradientAt(point).dot(first));
            assertEquals(secondDifference,first.dot(p.hessianAt(point).multiply(first)));
            Rational mixed=p.evaluate(point.add(first).add(second)).subtract(p.evaluate(point.add(first).add(second.scale(Rational.of(-1)))))
                    .subtract(p.evaluate(point.add(first.scale(Rational.of(-1))).add(second)))
                    .add(p.evaluate(point.add(first.scale(Rational.of(-1))).add(second.scale(Rational.of(-1))))).divide(Rational.of(4));
            assertEquals(mixed,first.dot(p.hessianAt(point).multiply(second)));
            assertEquals(p.laplacian().evaluate(point),p.hessianAt(point).trace());
        }
    }

    @Test public void mixedPartialsProductRuleAndExactTaylorRestrictionAgree() {
        MultivariatePolynomial p=example(),q=term(2,1,0,2).add(term(-1,0,2,0));
        for(int i=0;i<3;i++) {
            assertEquals(p.partial(i).multiply(q).add(p.multiply(q.partial(i))),p.multiply(q).partial(i));
            for(int j=0;j<3;j++) assertEquals(p.partial(i).partial(j),p.partial(j).partial(i));
        }
        RationalVector origin=v(1,2,3),direction=v(2,-1,4); Rational t=Rational.of(2,7);
        MultivariatePolynomial derivative=p; Rational taylor=Rational.ZERO,power=Rational.ONE,factorial=Rational.ONE;
        for(int order=0;order<=p.degree();order++) {
            if(order>0) { derivative=derivative.directional(direction); power=power.multiply(t); factorial=factorial.multiply(Rational.of(order)); }
            taylor=taylor.add(derivative.evaluate(origin).multiply(power).divide(factorial));
        }
        assertEquals(p.evaluate(origin.add(direction.scale(t))),taylor);
    }

    @Test public void primitivesChooseZeroVariableIndependentPolynomialAndCommuteAcrossVariables() {
        MultivariatePolynomial p=example();
        for(int axis=0;axis<3;axis++) {
            MultivariatePolynomial primitive=p.primitive(axis);
            assertEquals(p,primitive.partial(axis));
            long[] values={2,3,4}; values[axis]=0; assertEquals(Rational.ZERO,primitive.evaluate(v(values)));
            for(int other=0;other<3;other++) if(other!=axis) assertEquals(p.partial(other).primitive(axis),primitive.partial(other));
        }
        assertEquals(term(1,2,2).scale(Rational.of(1,6)),term(1,1,2).primitive(0).primitive(1).partial(1).scale(Rational.of(1,3)));
        assertEquals(c(3,0),c(3,0).primitive(1));
    }

    @Test public void powersConversionsAndLargeRationalCoefficientsStayExact() {
        MultivariatePolynomial p=term(1,1,0).add(term(1,0,1));
        assertEquals(term(1,2,0).add(term(2,1,1)).add(term(1,0,2)),p.pow(BigInteger.valueOf(2)));
        assertEquals(c(2,1),c(2,0).pow(BigInteger.ZERO)); assertEquals(c(2,0),c(2,0).pow(BigInteger.ONE));
        Polynomial univariate=new Polynomial(Rational.of(7,11),Rational.of(-2),Rational.of(3,5));
        assertEquals(univariate,MultivariatePolynomial.fromUnivariate(univariate).toUnivariate());
        assertEquals(univariate.derivative(),MultivariatePolynomial.fromUnivariate(univariate).partial(0).toUnivariate());
        assertEquals(Polynomial.ZERO,MultivariatePolynomial.fromUnivariate(Polynomial.ZERO).toUnivariate());
        Rational huge=Rational.of(BigInteger.TEN.pow(90));
        MultivariatePolynomial large=MultivariatePolynomial.monomial(huge,3,2).scale(Rational.of(1,7));
        assertEquals(huge.multiply(Rational.of(48,7)),large.evaluate(v(2,3)).divide(Rational.of(3,2)));
        assertEquals(Rational.of(9),c(2,9).toRational()); assertEquals(Rational.ZERO,c(2,0).toRational());
    }

    @Test public void mapCompositionUsesInnerFirstAndChecksRectangularChainRule() {
        PolynomialMap outer=new PolynomialMap(term(1,2,0).add(x(2,1)),term(1,1,1),term(1,0,2));
        PolynomialMap inner=new PolynomialMap(x(3,0).add(x(3,2)),term(1,0,1,1));
        RationalVector point=v(2,-1,3); PolynomialMap composed=outer.compose(inner);
        assertEquals(3,composed.inputDimension()); assertEquals(3,composed.outputDimension());
        assertEquals(outer.evaluate(inner.evaluate(point)),composed.evaluate(point));
        assertEquals(outer.jacobianAt(inner.evaluate(point)).multiply(inner.jacobianAt(point)),composed.jacobianAt(point));
        assertEquals(m(new long[]{10,3,9},new long[]{-3,15,-8},new long[]{0,-18,6}),composed.jacobianAt(point));
        assertEquals(composed,PolynomialMap.identity(3).compose(composed)); assertEquals(composed,composed.compose(PolynomialMap.identity(3)));
        assertEquals(outer.component(1).evaluate(inner.evaluate(point)),outer.component(1).substitute(inner).evaluate(point));
    }

    @Test public void nonlinearCompositionIsAssociativeAndItsScalarChainRuleIsExact() {
        PolynomialMap f=new PolynomialMap(term(1,2,0).add(x(2,1)),term(1,1,1));
        PolynomialMap g=new PolynomialMap(x(2,0).add(c(2,2)),term(1,0,2));
        PolynomialMap h=new PolynomialMap(x(2,1),x(2,0)); RationalVector point=v(2,-3);
        assertEquals(f.compose(g).compose(h),f.compose(g.compose(h)));
        assertNotEquals(f.compose(g),g.compose(f));
        MultivariatePolynomial scalar=term(2,2,1).add(term(1,0,3));
        assertEquals(g.jacobianAt(point).transpose().multiply(scalar.gradientAt(g.evaluate(point))),scalar.substitute(g).gradientAt(point));
    }

    @Test public void vectorCalculusUsesRightHandedCurlAndPreservesIdentities() {
        MultivariatePolynomial zero=c(3,0);
        PolynomialMap rotation=new PolynomialMap(x(3,1).negate(),x(3,0),zero);
        assertEquals(new PolynomialMap(zero,zero,c(3,2)),rotation.curl()); assertEquals(zero,rotation.divergence());
        PolynomialMap field=new PolynomialMap(term(1,1,1,0),term(1,0,1,1),term(1,1,0,1));
        assertEquals(new PolynomialMap(x(3,1).negate(),x(3,2).negate(),x(3,0).negate()),field.curl());
        assertEquals(x(3,0).add(x(3,1)).add(x(3,2)),field.divergence());
        assertEquals(zero,field.curl().divergence());
        MultivariatePolynomial p=example(); PolynomialMap gradient=PolynomialMap.gradient(p);
        assertEquals(new PolynomialMap(zero,zero,zero),gradient.curl()); assertEquals(p.laplacian(),gradient.divergence());
        assertEquals(p.hessianAt(v(2,3,4)),gradient.jacobianAt(v(2,3,4)));
    }

    @Test public void matrixMapsAndAffinePartsConnectToExactLinearAlgebra() {
        RationalMatrix matrix=m(new long[]{1,2,3},new long[]{0,-1,4}); PolynomialMap linear=PolynomialMap.fromMatrix(matrix);
        assertEquals(matrix.multiply(v(2,3,4)),linear.evaluate(v(2,3,4))); assertEquals(matrix,linear.linearPart());
        assertEquals(matrix,linear.jacobianAt(v(-9,8,7))); assertEquals(v(0,0),linear.constantPart());
        PolynomialMap nonlinear=new PolynomialMap(linear.component(0).add(c(3,7)).add(term(1,1,1,0)),linear.component(1).add(c(3,-3)));
        assertEquals(matrix,nonlinear.linearPart()); assertEquals(v(7,-3),nonlinear.constantPart());
        PolynomialMap scalar=new PolynomialMap(example()); assertEquals(example(),scalar.toPolynomial());
        MultivariatePolynomial[] input={x(2,0),x(2,1)}; PolynomialMap identity=new PolynomialMap(input); input[0]=c(2,5);
        assertEquals(PolynomialMap.identity(2),identity); assertEquals(identity.hashCode(),PolynomialMap.identity(2).hashCode());
        assertThrows(UnsupportedOperationException.class,() -> identity.components().clear());
    }

    @Test public void invalidMembersDifferFromUndefinedMathematicalOperations() {
        failure(MathFailure.Kind.INVALID_MEMBER,() -> c(0,0));
        failure(MathFailure.Kind.INVALID_MEMBER,() -> term(1,-1,0));
        failure(MathFailure.Kind.INVALID_MEMBER,() -> new MultivariatePolynomial(2,Collections.singletonMap(Collections.singletonList(1),Rational.ONE)));
        failure(MathFailure.Kind.INVALID_MEMBER,PolynomialMap::new);
        failure(MathFailure.Kind.INVALID_MEMBER,() -> new PolynomialMap(c(2,0),c(3,0)));
        undefined(() -> c(2,0).add(c(3,0))); undefined(() -> c(2,0).multiply(c(3,0)));
        undefined(() -> example().evaluate(v(1,2))); undefined(() -> example().gradientAt(v(1))); undefined(() -> example().hessianAt(v(1)));
        undefined(() -> example().partial(-1)); undefined(() -> example().primitive(3));
        undefined(() -> example().axis(BigInteger.TEN.pow(80))); undefined(() -> example().directional(v(1,2)));
        undefined(() -> c(2,0).toUnivariate()); undefined(() -> x(2,0).toRational()); undefined(() -> x(2,0).pow(BigInteger.valueOf(-1)));
        undefined(() -> PolynomialMap.identity(2).compose(PolynomialMap.identity(3)));
        undefined(() -> PolynomialMap.identity(2).add(new PolynomialMap(x(2,0))));
        undefined(() -> new PolynomialMap(x(2,0)).divergence()); undefined(() -> PolynomialMap.identity(2).curl());
        undefined(() -> PolynomialMap.identity(2).toPolynomial()); undefined(() -> PolynomialMap.identity(2).component(BigInteger.TEN.pow(80)));
        undefined(() -> c(2,0).substitute(PolynomialMap.identity(3)));
    }

    @Test public void representationAndExpansionLimitsAreImplementationFailures() {
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> c(33,0));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> term(1,10001));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> term(1,10000).multiply(x(1,0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> term(1,10000).primitive(0));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> x(1,0).pow(BigInteger.TEN.pow(80)));
        assertEquals(term(10000,9999),term(1,10000).partial(0));
        Map<List<Integer>,Rational> terms=new HashMap<>(); for(int i=0;i<=1000;i++) terms.put(Collections.singletonList(i),Rational.ONE);
        MultivariatePolynomial dense=new MultivariatePolynomial(1,terms);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> dense.multiply(dense));
        assertEquals(c(1,0),dense.multiply(c(1,0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> PolynomialMap.fromMatrix(RationalMatrix.identity(33)));
    }

    @Test public void wholeMapCompositionSharesItsExpansionBudget() {
        Map<List<Integer>,Rational> terms=new HashMap<>(); for(int i=0;i<710;i++) terms.put(Collections.singletonList(i),Rational.ONE);
        PolynomialMap inner=new PolynomialMap(new MultivariatePolynomial(1,terms));
        MultivariatePolynomial square=term(1,2); PolynomialMap single=new PolynomialMap(square);
        assertEquals(Rational.of(710L*710L),single.compose(inner).evaluate(v(1)).get(0));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> new PolynomialMap(square,square).compose(inner));
    }

    @Test public void nativeResultsKeepActualRegisteredScalarVectorMatrixAndPolynomialWrappers() {
        ConcreteMathematics math=new ConcreteMathematics(); IAlgebraItem<MultivariatePolynomial> item=math.multivariatePolynomials.algebra().buildAlgebraItem(example());
        IAlgebraItem<RationalVector> gradient=item.performLeftProjectionOperation("gradient-at",v(2,3,-1));
        assertSame(math.finiteVectors.algebra(),gradient.getAlgebra()); assertEquals(v(12,7,-18),gradient.getResult());
        IAlgebraItem<Rational> value=item.performUnsafeOperation("evaluate",v(2,3,-1));
        assertSame(math.rationals.algebra(),value.getAlgebra()); assertEquals(Rational.of(26),value.getResult());
        IAlgebraItem<RationalMatrix> hessian=item.performUnsafeOperation("hessian-at",v(2,3,-1));
        assertSame(math.rectangularMatrices.algebra(),hessian.getAlgebra());
        for(IAlgebraItem<MultivariatePolynomial> derivative : item.performOneOperandFlatOperation("partials")) assertSame(math.multivariatePolynomials.algebra(),derivative.getAlgebra());
        IAlgebraItem<PolynomialMap> map=item.performAlgebraTransfer("PolynomialMap(Q).gradient"); assertSame(math.polynomialMaps.algebra(),map.getAlgebra());
        IAlgebraItem<RationalVector> evaluated=map.performLeftProjectionOperation("evaluate",v(2,3,-1)); assertSame(math.finiteVectors.algebra(),evaluated.getAlgebra());
        assertEquals(gradient.getResult(),evaluated.getResult());
        IAlgebraItem<MultivariatePolynomial> substituted=item.performCustomMemberOperation("PolynomialMap(Q).substitute",PolynomialMap.identity(3));
        assertSame(math.multivariatePolynomials.algebra(),substituted.getAlgebra()); assertEquals(example(),substituted.getResult());
        undefined(() -> item.performOperation("add",c(2,0)).perform());
        undefined(() -> item.performCustomMemberOperation("partial",BigInteger.valueOf(3)));
    }

    @Test public void serializedGradientJacobianFlowsCanBeCollectedRepeatedly() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics();
        IAlgebraFlow<Rational> flow=math.flow(math.multivariatePolynomials,Collections.singletonList(example()))
                .<PolynomialMap>performAlgebraTransfer("PolynomialMap(Q).gradient").<RationalMatrix,RationalVector>performAlgebraUnsafe("jacobian-at",v(2,3,-1))
                .<Rational>performAlgebraTransfer("trace");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("24"),restored.collect()); assertEquals(Collections.singletonList("24"),restored.collect());
        assertEquals(Arrays.asList("12","7","-18"),math.flow(math.multivariatePolynomials,Collections.singletonList(example()))
                .performOneOperandFlatOperation("partials").<Rational,RationalVector>performAlgebraUnsafe("evaluate",v(2,3,-1)).collect());
    }
}
