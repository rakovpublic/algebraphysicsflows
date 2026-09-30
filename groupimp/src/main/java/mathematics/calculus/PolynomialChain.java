package mathematics.calculus;

import mathematics.core.MathFailure;
import mathematics.numbers.Rational;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Finite rational chains on polynomially parametrized cubes; all negative degrees contain only zero. */
public final class PolynomialChain implements Serializable {
    private static final long serialVersionUID=1L;
    public static final int MAX_CELLS=10000,MAX_CELL_VISITS=100000;
    private final int ambientDimension;
    private final BigInteger degree;
    private final SortedMap<PolynomialCell,Rational> coefficients;
    public PolynomialChain(int ambientDimension,BigInteger degree,Map<PolynomialCell,Rational> coefficients) {
        PolynomialCell.checkAmbient(ambientDimension); Objects.requireNonNull(degree);
        if(degree.compareTo(BigInteger.valueOf(PolynomialCell.MAX_DIMENSION))>0) throw limit("Chain degree exceeds the cell dimension limit");
        SortedMap<PolynomialCell,Rational> copy=new TreeMap<>();
        for(Map.Entry<PolynomialCell,Rational> term : coefficients.entrySet()) {
            PolynomialCell cell=Objects.requireNonNull(term.getKey()); Rational coefficient=Objects.requireNonNull(term.getValue());
            if(cell.ambientDimension()!=ambientDimension || !degree.equals(BigInteger.valueOf(cell.dimension())))
                throw MathFailure.invalid("Chain cells must share the declared ambient dimension and degree");
            if(coefficient.signum()!=0) copy.put(cell,coefficient);
            checkCells(copy.size());
        }
        this.ambientDimension=ambientDimension; this.degree=degree; this.coefficients=Collections.unmodifiableSortedMap(copy);
    }
    public PolynomialChain(int ambientDimension,int degree,Map<PolynomialCell,Rational> coefficients) { this(ambientDimension,BigInteger.valueOf(degree),coefficients); }
    private static MathFailure limit(String message) { return new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,message); }
    private static void checkCells(int count) { if(count>MAX_CELLS) throw limit("Polynomial chain exceeds the sparse cell limit"); }
    private static void checkVisits(long count) { if(count>MAX_CELL_VISITS) throw limit("Polynomial chain exceeds the cell-work budget"); }
    static void addCoefficient(Map<PolynomialCell,Rational> target,PolynomialCell cell,Rational coefficient) {
        Rational value=target.getOrDefault(cell,Rational.ZERO).add(coefficient);
        if(value.signum()==0) target.remove(cell); else target.put(cell,value);
        checkCells(target.size());
    }
    public static PolynomialChain of(PolynomialCell cell) { return new PolynomialChain(cell.ambientDimension(),cell.dimension(),Collections.singletonMap(cell,Rational.ONE)); }
    public static PolynomialChain zero(int ambientDimension,BigInteger degree) { return new PolynomialChain(ambientDimension,degree,Collections.emptyMap()); }
    public int ambientDimension() { return ambientDimension; }
    public BigInteger degree() { return degree; }
    public SortedMap<PolynomialCell,Rational> coefficients() { return coefficients; }
    public boolean isZero() { return coefficients.isEmpty(); }
    private void sameSpace(PolynomialChain other) {
        if(ambientDimension!=other.ambientDimension || !degree.equals(other.degree)) throw MathFailure.undefined("Chain ambient dimensions and degrees must agree");
    }
    public PolynomialChain add(PolynomialChain other) {
        sameSpace(other); Map<PolynomialCell,Rational> result=new TreeMap<>(coefficients);
        for(Map.Entry<PolynomialCell,Rational> term : other.coefficients.entrySet()) addCoefficient(result,term.getKey(),term.getValue());
        return new PolynomialChain(ambientDimension,degree,result);
    }
    public PolynomialChain scale(Rational factor) {
        Map<PolynomialCell,Rational> result=new TreeMap<>();
        for(Map.Entry<PolynomialCell,Rational> term : coefficients.entrySet()) addCoefficient(result,term.getKey(),term.getValue().multiply(factor));
        return new PolynomialChain(ambientDimension,degree,result);
    }
    public PolynomialChain negate() { return scale(Rational.of(-1)); }
    public PolynomialChain subtract(PolynomialChain other) { return add(other.negate()); }
    public Rational coefficient(PolynomialCell cell) {
        if(cell.ambientDimension()!=ambientDimension || !degree.equals(BigInteger.valueOf(cell.dimension()))) throw MathFailure.undefined("Coefficient lookup requires a cell in the chain space");
        return coefficients.getOrDefault(cell,Rational.ZERO);
    }
    public PolynomialChain boundary() {
        BigInteger targetDegree=degree.subtract(BigInteger.ONE);
        if(isZero() || degree.signum()<=0) return zero(ambientDimension,targetDegree);
        int dimension=degree.intValueExact(); checkVisits(2L*dimension*coefficients.size());
        MultivariatePolynomial.Work work=new MultivariatePolynomial.Work();
        Map<PolynomialCell,Rational> result=new TreeMap<>();
        for(Map.Entry<PolynomialCell,Rational> term : coefficients.entrySet()) for(int axis=0;axis<dimension;axis++) {
            Rational sign=axis%2==0?term.getValue():term.getValue().negate();
            addCoefficient(result,term.getKey().face(axis,true,work),sign);
            addCoefficient(result,term.getKey().face(axis,false,work),sign.negate());
        }
        return new PolynomialChain(ambientDimension,targetDegree,result);
    }
    public PolynomialChain product(PolynomialChain other) {
        int ambient=ambientDimension+other.ambientDimension; PolynomialCell.checkAmbient(ambient);
        BigInteger targetDegree=degree.add(other.degree); checkVisits((long)coefficients.size()*other.coefficients.size());
        MultivariatePolynomial.Work work=new MultivariatePolynomial.Work();
        Map<PolynomialCell,Rational> result=new TreeMap<>();
        for(Map.Entry<PolynomialCell,Rational> a : coefficients.entrySet()) for(Map.Entry<PolynomialCell,Rational> b : other.coefficients.entrySet())
            addCoefficient(result,a.getKey().product(b.getKey(),work),a.getValue().multiply(b.getValue()));
        return new PolynomialChain(ambient,targetDegree,result);
    }
    public PolynomialChain pushforward(PolynomialMap outer) {
        if(outer.inputDimension()!=ambientDimension) throw MathFailure.undefined("Pushforward map inputs must match the chain ambient dimension");
        PolynomialCell.checkAmbient(outer.outputDimension()); MultivariatePolynomial.Work work=new MultivariatePolynomial.Work();
        Map<PolynomialCell,Rational> result=new TreeMap<>();
        for(Map.Entry<PolynomialCell,Rational> term : coefficients.entrySet()) addCoefficient(result,term.getKey().pushforward(outer,work),term.getValue());
        return new PolynomialChain(outer.outputDimension(),degree,result);
    }
    public Rational integrate(PolynomialDifferentialForm form) {
        if(form.dimension()!=ambientDimension) throw MathFailure.undefined("The form and chain ambient dimensions must agree");
        form.requireDegree(degree); MultivariatePolynomial.Work work=new MultivariatePolynomial.Work(); Rational result=Rational.ZERO;
        for(Map.Entry<PolynomialCell,Rational> term : coefficients.entrySet()) result=result.add(term.getValue().multiply(term.getKey().integrate(form,work)));
        return result;
    }
    @Override public boolean equals(Object other) {
        return other instanceof PolynomialChain && ambientDimension==((PolynomialChain)other).ambientDimension
                && degree.equals(((PolynomialChain)other).degree) && coefficients.equals(((PolynomialChain)other).coefficients);
    }
    @Override public int hashCode() { return Objects.hash(ambientDimension,degree,coefficients); }
    @Override public String toString() { return "Chain(Q^"+ambientDimension+", degree="+degree+")"+coefficients; }
}
