package mathematics.calculus;

import mathematics.core.MathFailure;
import mathematics.linear.RationalMatrix;
import mathematics.linear.RationalVector;
import mathematics.numbers.Rational;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** An ordered finite tuple of rational polynomials: an exact map Q^n -> Q^m. */
public final class PolynomialMap implements Serializable {
    private static final long serialVersionUID=1L;
    private final List<MultivariatePolynomial> components;
    public PolynomialMap(MultivariatePolynomial... components) {
        MultivariatePolynomial.checkVariables(components.length);
        List<MultivariatePolynomial> values=new ArrayList<>();
        for(MultivariatePolynomial component : components) {
            Objects.requireNonNull(component);
            if(component.variableCount()!=components[0].variableCount())
                throw MathFailure.invalid("All polynomial map components must share an input dimension");
            values.add(component);
        }
        this.components=Collections.unmodifiableList(values);
    }
    public int inputDimension() { return components.get(0).variableCount(); }
    public int outputDimension() { return components.size(); }
    public List<MultivariatePolynomial> components() { return components; }
    public MultivariatePolynomial component(int index) {
        if(index<0 || index>=outputDimension()) throw MathFailure.undefined("Polynomial map component index is outside its output space");
        return components.get(index);
    }
    public MultivariatePolynomial component(BigInteger index) {
        if(index.signum()<0 || index.compareTo(BigInteger.valueOf(outputDimension()))>=0)
            throw MathFailure.undefined("Polynomial map component index is outside its output space");
        return component(index.intValueExact());
    }
    public PolynomialMap add(PolynomialMap other) {
        if(inputDimension()!=other.inputDimension() || outputDimension()!=other.outputDimension())
            throw MathFailure.undefined("Polynomial map input and output dimensions must both agree");
        MultivariatePolynomial[] result=new MultivariatePolynomial[outputDimension()];
        for(int i=0;i<result.length;i++) result[i]=component(i).add(other.component(i));
        return new PolynomialMap(result);
    }
    public PolynomialMap scale(Rational factor) {
        MultivariatePolynomial[] result=new MultivariatePolynomial[outputDimension()];
        for(int i=0;i<result.length;i++) result[i]=component(i).scale(factor);
        return new PolynomialMap(result);
    }
    public PolynomialMap negate() { return scale(Rational.of(-1)); }
    public PolynomialMap subtract(PolynomialMap other) { return add(other.negate()); }
    /** Composition outer.compose(inner) acts with inner first. */
    public PolynomialMap compose(PolynomialMap inner) {
        return compose(inner,new MultivariatePolynomial.Work());
    }
    PolynomialMap compose(PolynomialMap inner,MultivariatePolynomial.Work work) {
        if(inputDimension()!=inner.outputDimension()) throw MathFailure.undefined("Inner outputs must match outer inputs");
        MultivariatePolynomial[] result=new MultivariatePolynomial[outputDimension()];
        for(int i=0;i<result.length;i++) result[i]=component(i).substitute(inner,work);
        return new PolynomialMap(result);
    }
    public RationalVector evaluate(RationalVector point) {
        Rational[] result=new Rational[outputDimension()];
        for(int i=0;i<result.length;i++) result[i]=component(i).evaluate(point);
        return new RationalVector(result);
    }
    public PolynomialMap partial(int axis) {
        MultivariatePolynomial[] result=new MultivariatePolynomial[outputDimension()];
        for(int i=0;i<result.length;i++) result[i]=component(i).partial(axis);
        return new PolynomialMap(result);
    }
    public int axis(BigInteger index) { return component(0).axis(index); }
    /** Rows index output components; columns index input variables. */
    public RationalMatrix jacobianAt(RationalVector point) {
        Rational[][] entries=new Rational[outputDimension()][inputDimension()];
        for(int i=0;i<outputDimension();i++) {
            RationalVector row=component(i).gradientAt(point);
            for(int j=0;j<inputDimension();j++) entries[i][j]=row.get(j);
        }
        return new RationalMatrix(entries);
    }
    public MultivariatePolynomial divergence() {
        if(inputDimension()!=outputDimension()) throw MathFailure.undefined("Divergence requires a vector field Q^n -> Q^n");
        MultivariatePolynomial result=MultivariatePolynomial.constant(inputDimension(),Rational.ZERO);
        for(int i=0;i<inputDimension();i++) result=result.add(component(i).partial(i));
        return result;
    }
    public PolynomialMap curl() {
        if(inputDimension()!=3 || outputDimension()!=3) throw MathFailure.undefined("Curl requires a vector field Q^3 -> Q^3");
        return new PolynomialMap(component(2).partial(1).subtract(component(1).partial(2)),
                component(0).partial(2).subtract(component(2).partial(0)),
                component(1).partial(0).subtract(component(0).partial(1)));
    }
    public static PolynomialMap gradient(MultivariatePolynomial polynomial) {
        return new PolynomialMap(polynomial.partials().toArray(new MultivariatePolynomial[0]));
    }
    public MultivariatePolynomial toPolynomial() {
        if(outputDimension()!=1) throw MathFailure.undefined("Scalar conversion requires exactly one output component");
        return component(0);
    }
    public static PolynomialMap identity(int dimension) {
        MultivariatePolynomial.checkVariables(dimension); MultivariatePolynomial[] result=new MultivariatePolynomial[dimension];
        for(int i=0;i<dimension;i++) result[i]=MultivariatePolynomial.variable(dimension,i);
        return new PolynomialMap(result);
    }
    public static PolynomialMap fromMatrix(RationalMatrix matrix) {
        MultivariatePolynomial.checkVariables(matrix.rows()); MultivariatePolynomial.checkVariables(matrix.columns());
        MultivariatePolynomial[] result=new MultivariatePolynomial[matrix.rows()];
        for(int i=0;i<matrix.rows();i++) {
            result[i]=MultivariatePolynomial.constant(matrix.columns(),Rational.ZERO);
            for(int j=0;j<matrix.columns();j++) if(matrix.get(i,j).signum()!=0)
                result[i]=result[i].add(MultivariatePolynomial.variable(matrix.columns(),j).scale(matrix.get(i,j)));
        }
        return new PolynomialMap(result);
    }
    private RationalVector origin() {
        Rational[] coordinates=new Rational[inputDimension()]; Arrays.fill(coordinates,Rational.ZERO);
        return new RationalVector(coordinates);
    }
    public RationalVector constantPart() { return evaluate(origin()); }
    public RationalMatrix linearPart() { return jacobianAt(origin()); }
    @Override public boolean equals(Object other) { return other instanceof PolynomialMap && components.equals(((PolynomialMap)other).components); }
    @Override public int hashCode() { return components.hashCode(); }
    @Override public String toString() { return "PolynomialMap"+components; }
}
