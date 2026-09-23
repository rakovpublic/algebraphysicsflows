package mathematics.calculus;

import mathematics.core.MathFailure;
import mathematics.linear.RationalMatrix;
import mathematics.linear.RationalVector;
import mathematics.numbers.Rational;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Sparse polynomials over Q in an explicit, ordered, positive number of variables. */
public final class MultivariatePolynomial implements Serializable {
    private static final long serialVersionUID=1L;
    public static final int MAX_VARIABLES=32,MAX_DEGREE=10000,MAX_TERMS=100000,MAX_PRODUCTS=1000000;
    private final int variables;
    private final SortedMap<List<Integer>,Rational> coefficients;
    private static final class PowerOrder implements Comparator<List<Integer>>,Serializable {
        private static final long serialVersionUID=1L;
        public int compare(List<Integer> first,List<Integer> second) {
            for(int i=0;i<first.size();i++) {
                int comparison=Integer.compare(first.get(i),second.get(i));
                if(comparison!=0) return comparison;
            }
            return 0;
        }
    }
    static final class Work {
        private int products;
        void consume(long count) {
            if(count>MAX_PRODUCTS-products) throw limit("Exact polynomial computation exceeds the work budget");
            products+=(int)count;
        }
    }
    public MultivariatePolynomial(int variables,Map<List<Integer>,Rational> coefficients) {
        checkVariables(variables); this.variables=variables;
        SortedMap<List<Integer>,Rational> copy=new TreeMap<>(new PowerOrder());
        for(Map.Entry<List<Integer>,Rational> term : coefficients.entrySet()) {
            List<Integer> powers=term.getKey();
            if(powers.size()!=variables) throw MathFailure.invalid("One exponent is required per polynomial variable");
            long degree=0;
            for(Integer power : powers) {
                if(power==null || power<0) throw MathFailure.invalid("Polynomial exponents must be nonnegative integers");
                degree+=power;
            }
            if(degree>MAX_DEGREE) throw limit("Polynomial degree exceeds the representation limit");
            Rational coefficient=Objects.requireNonNull(term.getValue());
            if(coefficient.signum()!=0) {
                copy.put(Collections.unmodifiableList(new ArrayList<>(powers)),coefficient);
                checkTerms(copy.size());
            }
        }
        this.coefficients=Collections.unmodifiableSortedMap(copy);
    }
    static void checkVariables(int count) {
        if(count<1) throw MathFailure.invalid("Polynomial spaces require a positive number of variables");
        if(count>MAX_VARIABLES) throw limit("Polynomial variable count exceeds the representation limit");
    }
    private static MathFailure limit(String message) { return new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,message); }
    private static void checkTerms(int count) { if(count>MAX_TERMS) throw limit("Sparse polynomial exceeds the term limit"); }
    public static MultivariatePolynomial constant(int variables,Rational value) {
        checkVariables(variables);
        return new MultivariatePolynomial(variables,Collections.singletonMap(Collections.nCopies(variables,0),value));
    }
    public static MultivariatePolynomial monomial(Rational coefficient,int... powers) {
        List<Integer> exponents=new ArrayList<>(); for(int power : powers) exponents.add(power);
        return new MultivariatePolynomial(powers.length,Collections.singletonMap(exponents,coefficient));
    }
    public static MultivariatePolynomial variable(int variables,int axis) {
        checkVariables(variables); checkAxis(axis,variables);
        int[] powers=new int[variables]; powers[axis]=1; return monomial(Rational.ONE,powers);
    }
    public int variableCount() { return variables; }
    public SortedMap<List<Integer>,Rational> coefficients() { return coefficients; }
    public boolean isZero() { return coefficients.isEmpty(); }
    /** Total degree; the zero polynomial has degree -1. */
    public int degree() {
        int degree=-1;
        for(List<Integer> powers : coefficients.keySet()) {
            int sum=0; for(int power : powers) sum+=power;
            degree=Math.max(degree,sum);
        }
        return degree;
    }
    private static void checkAxis(int axis,int count) {
        if(axis<0 || axis>=count) throw MathFailure.undefined("Polynomial variable index is outside its declared input space");
    }
    public int axis(BigInteger index) {
        if(index.signum()<0 || index.compareTo(BigInteger.valueOf(variables))>=0)
            throw MathFailure.undefined("Polynomial variable index is outside its declared input space");
        return index.intValueExact();
    }
    private void sameSpace(MultivariatePolynomial other) {
        if(variables!=other.variables) throw MathFailure.undefined("Polynomial input dimensions must agree");
    }
    private void checkPoint(RationalVector point) {
        if(point.dimension()!=variables) throw MathFailure.undefined("One rational coordinate is required per polynomial variable");
    }
    public MultivariatePolynomial add(MultivariatePolynomial other) {
        sameSpace(other); Map<List<Integer>,Rational> result=new HashMap<>(coefficients);
        for(Map.Entry<List<Integer>,Rational> term : other.coefficients.entrySet()) {
            merge(result,term.getKey(),term.getValue());
        }
        return new MultivariatePolynomial(variables,result);
    }
    private static void merge(Map<List<Integer>,Rational> result,List<Integer> powers,Rational coefficient) {
        Rational value=result.getOrDefault(powers,Rational.ZERO).add(coefficient);
        if(value.signum()==0) result.remove(powers); else result.put(powers,value);
        checkTerms(result.size());
    }
    public MultivariatePolynomial scale(Rational factor) {
        Map<List<Integer>,Rational> result=new HashMap<>();
        if(factor.signum()!=0) for(Map.Entry<List<Integer>,Rational> term : coefficients.entrySet())
            result.put(term.getKey(),term.getValue().multiply(factor));
        return new MultivariatePolynomial(variables,result);
    }
    public MultivariatePolynomial negate() { return scale(Rational.of(-1)); }
    public MultivariatePolynomial subtract(MultivariatePolynomial other) { return add(other.negate()); }
    public MultivariatePolynomial multiply(MultivariatePolynomial other) { return multiply(other,new Work()); }
    MultivariatePolynomial multiply(MultivariatePolynomial other,Work work) {
        sameSpace(other); work.consume((long)coefficients.size()*other.coefficients.size());
        Map<List<Integer>,Rational> result=new HashMap<>();
        for(Map.Entry<List<Integer>,Rational> first : coefficients.entrySet())
            for(Map.Entry<List<Integer>,Rational> second : other.coefficients.entrySet()) {
                List<Integer> powers=new ArrayList<>(); int degree=0;
                for(int i=0;i<variables;i++) {
                    int power=first.getKey().get(i)+second.getKey().get(i); powers.add(power); degree+=power;
                }
                if(degree>MAX_DEGREE) throw limit("Polynomial product exceeds the degree limit");
                merge(result,powers,first.getValue().multiply(second.getValue()));
            }
        return new MultivariatePolynomial(variables,result);
    }
    public MultivariatePolynomial pow(BigInteger exponent) {
        if(exponent.signum()<0) throw MathFailure.undefined("Polynomial power requires a nonnegative exponent");
        if(exponent.compareTo(BigInteger.valueOf(MAX_DEGREE))>0) throw limit("Polynomial exponent exceeds the expansion limit");
        return pow(exponent.intValueExact(),new Work());
    }
    private MultivariatePolynomial pow(int exponent,Work work) {
        MultivariatePolynomial result=constant(variables,Rational.ONE),base=this;
        for(int remaining=exponent;remaining>0;remaining>>=1) {
            if((remaining&1)!=0) result=result.multiply(base,work);
            if(remaining>1) base=base.multiply(base,work);
        }
        return result;
    }
    public Rational evaluate(RationalVector point) {
        checkPoint(point); Rational result=Rational.ZERO;
        for(Map.Entry<List<Integer>,Rational> term : coefficients.entrySet()) {
            Rational value=term.getValue();
            for(int i=0;i<variables;i++) value=value.multiply(point.get(i).pow(term.getKey().get(i)));
            result=result.add(value);
        }
        return result;
    }
    public MultivariatePolynomial partial(int axis) {
        checkAxis(axis,variables); Map<List<Integer>,Rational> result=new HashMap<>();
        for(Map.Entry<List<Integer>,Rational> term : coefficients.entrySet()) {
            int power=term.getKey().get(axis);
            if(power>0) {
                List<Integer> powers=new ArrayList<>(term.getKey()); powers.set(axis,power-1);
                result.put(powers,term.getValue().multiply(Rational.of(power)));
            }
        }
        return new MultivariatePolynomial(variables,result);
    }
    /** Antiderivative in one variable, with the entire variable-independent polynomial set to zero. */
    public MultivariatePolynomial primitive(int axis) {
        checkAxis(axis,variables); Map<List<Integer>,Rational> result=new HashMap<>();
        for(Map.Entry<List<Integer>,Rational> term : coefficients.entrySet()) {
            List<Integer> powers=new ArrayList<>(term.getKey()); int power=powers.get(axis)+1; powers.set(axis,power);
            result.put(powers,term.getValue().divide(Rational.of(power)));
        }
        return new MultivariatePolynomial(variables,result);
    }
    public List<MultivariatePolynomial> partials() {
        List<MultivariatePolynomial> result=new ArrayList<>(); for(int axis=0;axis<variables;axis++) result.add(partial(axis));
        return Collections.unmodifiableList(result);
    }
    public List<MultivariatePolynomial> variables() {
        List<MultivariatePolynomial> result=new ArrayList<>(); for(int axis=0;axis<variables;axis++) result.add(variable(variables,axis));
        return Collections.unmodifiableList(result);
    }
    /** Constant-direction derivative; the direction is not normalized. */
    public MultivariatePolynomial directional(RationalVector direction) {
        checkPoint(direction); MultivariatePolynomial result=constant(variables,Rational.ZERO);
        for(int axis=0;axis<variables;axis++) if(direction.get(axis).signum()!=0)
            result=result.add(partial(axis).scale(direction.get(axis)));
        return result;
    }
    public RationalVector gradientAt(RationalVector point) {
        checkPoint(point); Rational[] entries=new Rational[variables];
        for(int axis=0;axis<variables;axis++) entries[axis]=partial(axis).evaluate(point);
        return new RationalVector(entries);
    }
    public RationalMatrix hessianAt(RationalVector point) {
        checkPoint(point); Rational[][] entries=new Rational[variables][variables];
        for(int i=0;i<variables;i++) {
            MultivariatePolynomial derivative=partial(i);
            for(int j=i;j<variables;j++) entries[i][j]=entries[j][i]=derivative.partial(j).evaluate(point);
        }
        return new RationalMatrix(entries);
    }
    public MultivariatePolynomial laplacian() {
        MultivariatePolynomial result=constant(variables,Rational.ZERO);
        for(int axis=0;axis<variables;axis++) result=result.add(partial(axis).partial(axis));
        return result;
    }
    public List<MultivariatePolynomial> terms() {
        List<MultivariatePolynomial> result=new ArrayList<>();
        for(Map.Entry<List<Integer>,Rational> term : coefficients.entrySet())
            result.add(new MultivariatePolynomial(variables,Collections.singletonMap(term.getKey(),term.getValue())));
        return Collections.unmodifiableList(result);
    }
    public Rational constantPart() { return coefficients.getOrDefault(Collections.nCopies(variables,0),Rational.ZERO); }
    /** Exact integral on [0,1]^n in the declared variable order. */
    public Rational integrateUnitCube() {
        Rational result=Rational.ZERO;
        for(Map.Entry<List<Integer>,Rational> term : coefficients.entrySet()) {
            BigInteger denominator=BigInteger.ONE;
            for(int exponent : term.getKey()) denominator=denominator.multiply(BigInteger.valueOf(exponent+1));
            result=result.add(term.getValue().divide(Rational.of(denominator)));
        }
        return result;
    }
    /** Fix and remove one coordinate; zero-variable faces are represented separately as cell points. */
    MultivariatePolynomial restrictCoordinate(int axis,Rational value) {
        checkAxis(axis,variables);
        if(variables==1) throw MathFailure.undefined("Removing the last variable requires scalar evaluation");
        Map<List<Integer>,Rational> result=new HashMap<>();
        for(Map.Entry<List<Integer>,Rational> term : coefficients.entrySet()) {
            List<Integer> powers=new ArrayList<>(term.getKey()); int exponent=powers.remove(axis);
            merge(result,powers,term.getValue().multiply(value.pow(exponent)));
        }
        return new MultivariatePolynomial(variables-1,result);
    }
    public Rational toRational() {
        if(degree()>0) throw MathFailure.undefined("Polynomial is not constant");
        return constantPart();
    }
    public static MultivariatePolynomial fromUnivariate(Polynomial polynomial) {
        if(polynomial.degree()>MAX_DEGREE) throw limit("Univariate polynomial exceeds the multivariate degree limit");
        Map<List<Integer>,Rational> terms=new HashMap<>();
        for(int i=0;i<=polynomial.degree();i++) if(polynomial.coefficient(i).signum()!=0)
            terms.put(Collections.singletonList(i),polynomial.coefficient(i));
        return new MultivariatePolynomial(1,terms);
    }
    public Polynomial toUnivariate() {
        if(variables!=1) throw MathFailure.undefined("Univariate conversion requires exactly one declared variable");
        Rational[] values=new Rational[Math.max(1,degree()+1)]; Arrays.fill(values,Rational.ZERO);
        for(Map.Entry<List<Integer>,Rational> term : coefficients.entrySet()) values[term.getKey().get(0)]=term.getValue();
        return new Polynomial(values);
    }
    public MultivariatePolynomial substitute(PolynomialMap inner) { return substitute(inner,new Work()); }
    MultivariatePolynomial substitute(PolynomialMap inner,Work work) {
        if(inner.outputDimension()!=variables) throw MathFailure.undefined("Substitution needs one inner component per outer variable");
        MultivariatePolynomial result=constant(inner.inputDimension(),Rational.ZERO);
        for(Map.Entry<List<Integer>,Rational> term : coefficients.entrySet()) {
            MultivariatePolynomial value=constant(inner.inputDimension(),term.getValue());
            for(int i=0;i<variables;i++) if(term.getKey().get(i)>0)
                value=value.multiply(inner.component(i).pow(term.getKey().get(i),work),work);
            result=result.add(value);
        }
        return result;
    }
    @Override public boolean equals(Object other) {
        return other instanceof MultivariatePolynomial && variables==((MultivariatePolynomial)other).variables
                && coefficients.equals(((MultivariatePolynomial)other).coefficients);
    }
    @Override public int hashCode() { return Objects.hash(variables,coefficients); }
    @Override public String toString() { return "Poly(Q^"+variables+")"+coefficients; }
}
