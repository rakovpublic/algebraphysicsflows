package mathematics.calculus;

import mathematics.core.MathFailure;
import mathematics.linear.RationalExterior;
import mathematics.linear.RationalVector;
import mathematics.numbers.Rational;
import java.io.Serializable;
import java.util.*;

/** Polynomial differential forms in standard ordered rational coordinates; mixed degrees are allowed. */
public final class PolynomialDifferentialForm implements Serializable {
    private static final long serialVersionUID=1L;
    public static final int MAX_DIMENSION=RationalExterior.MAX_DIMENSION,MAX_TERMS=MultivariatePolynomial.MAX_TERMS;
    private final int dimension;
    private final SortedMap<Integer,MultivariatePolynomial> coefficients;
    private final int coefficientTerms;

    /** Bit i denotes dx_i. Coefficients have exactly the declared number of variables. */
    public PolynomialDifferentialForm(int dimension,Map<Integer,MultivariatePolynomial> coefficients) {
        checkDimension(dimension); this.dimension=dimension;
        SortedMap<Integer,MultivariatePolynomial> copy=new TreeMap<>(); int terms=0;
        for(Map.Entry<Integer,MultivariatePolynomial> term : coefficients.entrySet()) {
            int mask=Objects.requireNonNull(term.getKey()); MultivariatePolynomial value=Objects.requireNonNull(term.getValue());
            if(mask<0 || mask>=(1<<dimension)) throw MathFailure.invalid("Differential basis mask is outside its coordinate dimension");
            if(value.variableCount()!=dimension) throw MathFailure.invalid("Form coefficients must have the declared coordinate dimension");
            if(!value.isZero()) { copy.put(mask,value); terms+=value.coefficients().size(); checkTerms(terms); }
        }
        this.coefficients=Collections.unmodifiableSortedMap(copy); coefficientTerms=terms;
    }
    private static void checkDimension(int dimension) {
        if(dimension<1) throw MathFailure.invalid("Polynomial forms require a positive coordinate dimension");
        if(dimension>MAX_DIMENSION) throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Polynomial form dimension exceeds the coordinate representation limit");
    }
    private static void checkTerms(int count) {
        if(count>MAX_TERMS) throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Polynomial form exceeds the total coefficient-term limit");
    }
    private static final class Accumulator {
        final SortedMap<Integer,MultivariatePolynomial> values=new TreeMap<>(); int terms;
        void add(int mask,MultivariatePolynomial value) {
            if(value.isZero()) return;
            MultivariatePolynomial previous=values.get(mask);
            if(previous!=null) { terms-=previous.coefficients().size(); value=previous.add(value); }
            if(value.isZero()) values.remove(mask); else { values.put(mask,value); terms+=value.coefficients().size(); }
            checkTerms(terms);
        }
    }
    public int dimension() { return dimension; }
    public SortedMap<Integer,MultivariatePolynomial> coefficients() { return coefficients; }
    public int coefficientTermCount() { return coefficientTerms; }
    public boolean isZero() { return coefficients.isEmpty(); }
    public static PolynomialDifferentialForm scalar(MultivariatePolynomial value) {
        return new PolynomialDifferentialForm(value.variableCount(),Collections.singletonMap(0,value));
    }
    public static PolynomialDifferentialForm zero(int dimension) { return scalar(MultivariatePolynomial.constant(dimension,Rational.ZERO)); }
    public static PolynomialDifferentialForm one(int dimension) { return scalar(MultivariatePolynomial.constant(dimension,Rational.ONE)); }
    public static PolynomialDifferentialForm volume(int dimension) {
        checkDimension(dimension);
        return new PolynomialDifferentialForm(dimension,Collections.singletonMap((1<<dimension)-1,MultivariatePolynomial.constant(dimension,Rational.ONE)));
    }
    public static PolynomialDifferentialForm coordinate(int dimension,int axis) {
        checkDimension(dimension);
        if(axis<0 || axis>=dimension) throw MathFailure.undefined("Differential coordinate index is outside the input space");
        return new PolynomialDifferentialForm(dimension,Collections.singletonMap(1<<axis,MultivariatePolynomial.constant(dimension,Rational.ONE)));
    }
    private void sameDimension(PolynomialDifferentialForm other) {
        if(dimension!=other.dimension) throw MathFailure.undefined("Polynomial forms must have the same coordinate dimension");
    }
    private void checkVectorField(PolynomialMap field) {
        if(field.inputDimension()!=dimension || field.outputDimension()!=dimension)
            throw MathFailure.undefined("The polynomial vector field must map the form's coordinate space to itself");
    }
    public PolynomialDifferentialForm add(PolynomialDifferentialForm other) {
        sameDimension(other); Accumulator result=new Accumulator();
        for(Map.Entry<Integer,MultivariatePolynomial> term : coefficients.entrySet()) result.add(term.getKey(),term.getValue());
        for(Map.Entry<Integer,MultivariatePolynomial> term : other.coefficients.entrySet()) result.add(term.getKey(),term.getValue());
        return new PolynomialDifferentialForm(dimension,result.values);
    }
    public PolynomialDifferentialForm scale(Rational factor) {
        Map<Integer,MultivariatePolynomial> result=new TreeMap<>();
        for(Map.Entry<Integer,MultivariatePolynomial> term : coefficients.entrySet()) result.put(term.getKey(),term.getValue().scale(factor));
        return new PolynomialDifferentialForm(dimension,result);
    }
    public PolynomialDifferentialForm negate() { return scale(Rational.of(-1)); }
    public PolynomialDifferentialForm subtract(PolynomialDifferentialForm other) { return add(other.negate()); }
    private static int sign(int first,int second) {
        int parity=0;
        while(first!=0) { int bit=Integer.lowestOneBit(first); parity^=Integer.bitCount(second&(bit-1))&1; first^=bit; }
        return parity==0?1:-1;
    }
    public PolynomialDifferentialForm wedge(PolynomialDifferentialForm other) { return wedge(other,new MultivariatePolynomial.Work()); }
    private PolynomialDifferentialForm wedge(PolynomialDifferentialForm other,MultivariatePolynomial.Work work) {
        sameDimension(other); work.consume((long)coefficients.size()*other.coefficients.size());
        Accumulator result=new Accumulator();
        for(Map.Entry<Integer,MultivariatePolynomial> first : coefficients.entrySet())
            for(Map.Entry<Integer,MultivariatePolynomial> second : other.coefficients.entrySet()) {
                int a=first.getKey(),b=second.getKey(); if((a&b)!=0) continue;
                MultivariatePolynomial value=first.getValue().multiply(second.getValue(),work);
                result.add(a|b,sign(a,b)==1?value:value.negate());
            }
        return new PolynomialDifferentialForm(dimension,result.values);
    }
    public PolynomialDifferentialForm multiplyPolynomial(MultivariatePolynomial coefficient) {
        if(coefficient.variableCount()!=dimension) throw MathFailure.undefined("The scalar polynomial must have the form's coordinate dimension");
        return wedge(scalar(coefficient));
    }
    /** d(f dx_I) = sum_j (partial_j f) dx_j wedge dx_I. */
    public PolynomialDifferentialForm exteriorDerivative() {
        Accumulator result=new Accumulator();
        for(Map.Entry<Integer,MultivariatePolynomial> term : coefficients.entrySet()) for(int axis=0;axis<dimension;axis++) {
            int bit=1<<axis,mask=term.getKey(); if((bit&mask)!=0) continue;
            MultivariatePolynomial value=term.getValue().partial(axis);
            result.add(mask|bit,sign(bit,mask)==1?value:value.negate());
        }
        return new PolynomialDifferentialForm(dimension,result.values);
    }
    public PolynomialDifferentialForm grade(int degree) {
        if(degree<0) throw MathFailure.undefined("Form degree must be nonnegative");
        Map<Integer,MultivariatePolynomial> result=new TreeMap<>();
        for(Map.Entry<Integer,MultivariatePolynomial> term : coefficients.entrySet()) if(Integer.bitCount(term.getKey())==degree) result.put(term.getKey(),term.getValue());
        return new PolynomialDifferentialForm(dimension,result);
    }
    public List<Integer> degrees() {
        SortedSet<Integer> result=new TreeSet<>(); for(int mask : coefficients.keySet()) result.add(Integer.bitCount(mask));
        return Collections.unmodifiableList(new ArrayList<>(result));
    }
    public List<PolynomialDifferentialForm> terms() {
        List<PolynomialDifferentialForm> result=new ArrayList<>();
        for(Map.Entry<Integer,MultivariatePolynomial> term : coefficients.entrySet())
            result.add(new PolynomialDifferentialForm(dimension,Collections.singletonMap(term.getKey(),term.getValue())));
        return Collections.unmodifiableList(result);
    }
    public List<PolynomialDifferentialForm> basis() {
        List<PolynomialDifferentialForm> result=new ArrayList<>(); for(int axis=0;axis<dimension;axis++) result.add(coordinate(dimension,axis));
        return Collections.unmodifiableList(result);
    }
    public PolynomialDifferentialForm gradeInvolution() {
        Map<Integer,MultivariatePolynomial> result=new TreeMap<>();
        for(Map.Entry<Integer,MultivariatePolynomial> term : coefficients.entrySet())
            result.put(term.getKey(),Integer.bitCount(term.getKey())%2==0?term.getValue():term.getValue().negate());
        return new PolynomialDifferentialForm(dimension,result);
    }
    /** Euclidean Hodge star in the standard oriented orthonormal coordinate coframe. */
    public PolynomialDifferentialForm hodgeStar() {
        Map<Integer,MultivariatePolynomial> result=new TreeMap<>(); int full=(1<<dimension)-1;
        for(Map.Entry<Integer,MultivariatePolynomial> term : coefficients.entrySet()) {
            int complement=full^term.getKey();
            result.put(complement,sign(term.getKey(),complement)==1?term.getValue():term.getValue().negate());
        }
        return new PolynomialDifferentialForm(dimension,result);
    }
    /** Left insertion of a polynomial vector field, using the natural covector-vector pairing. */
    public PolynomialDifferentialForm interior(PolynomialMap field) { return interior(field,new MultivariatePolynomial.Work()); }
    private PolynomialDifferentialForm interior(PolynomialMap field,MultivariatePolynomial.Work work) {
        checkVectorField(field); Accumulator result=new Accumulator();
        for(Map.Entry<Integer,MultivariatePolynomial> term : coefficients.entrySet()) {
            int mask=term.getKey(),remaining=mask,position=0;
            while(remaining!=0) {
                work.consume(1); int bit=Integer.lowestOneBit(remaining);
                MultivariatePolynomial value=term.getValue().multiply(field.component(Integer.numberOfTrailingZeros(bit)),work);
                result.add(mask^bit,position%2==0?value:value.negate()); remaining^=bit; position++;
            }
        }
        return new PolynomialDifferentialForm(dimension,result.values);
    }
    /** Cartan's formula L_X = i_X d + d i_X; the two insertions share one work budget. */
    public PolynomialDifferentialForm lieDerivative(PolynomialMap field) {
        checkVectorField(field); MultivariatePolynomial.Work work=new MultivariatePolynomial.Work();
        return exteriorDerivative().interior(field,work).add(interior(field,work).exteriorDerivative());
    }
    /** For F:Q^m -> Q^n, pull back a form on Q^n by f -> f(F) and dx_i -> dF_i. */
    public PolynomialDifferentialForm pullback(PolynomialMap map) {
        return pullback(map,new MultivariatePolynomial.Work());
    }
    PolynomialDifferentialForm pullback(PolynomialMap map,MultivariatePolynomial.Work work) {
        if(map.outputDimension()!=dimension) throw MathFailure.undefined("Pullback map output dimension must equal the form coordinate dimension");
        int input=map.inputDimension(); checkDimension(input);
        PolynomialDifferentialForm[] differentials=new PolynomialDifferentialForm[dimension]; Accumulator result=new Accumulator();
        for(Map.Entry<Integer,MultivariatePolynomial> term : coefficients.entrySet()) {
            PolynomialDifferentialForm image=scalar(term.getValue().substitute(map,work));
            int remaining=term.getKey();
            while(remaining!=0 && !image.isZero()) {
                int bit=Integer.lowestOneBit(remaining),axis=Integer.numberOfTrailingZeros(bit);
                if(differentials[axis]==null) differentials[axis]=scalar(map.component(axis)).exteriorDerivative();
                image=image.wedge(differentials[axis],work); remaining^=bit;
            }
            for(Map.Entry<Integer,MultivariatePolynomial> part : image.coefficients.entrySet()) result.add(part.getKey(),part.getValue());
        }
        return new PolynomialDifferentialForm(input,result.values);
    }
    void requireDegree(java.math.BigInteger degree) {
        for(int mask : coefficients.keySet()) if(!degree.equals(java.math.BigInteger.valueOf(Integer.bitCount(mask))))
            throw MathFailure.undefined("Integration requires a homogeneous form of the chain or cell degree");
    }
    /** Integrate a top-degree form over the standard positively oriented unit cube. */
    public Rational integrateUnitCube() {
        requireDegree(java.math.BigInteger.valueOf(dimension));
        MultivariatePolynomial top=coefficients.get((1<<dimension)-1);
        return top==null?Rational.ZERO:top.integrateUnitCube();
    }
    /** Evaluate coefficients and identify the standard coordinate coframe with the existing exterior basis. */
    public RationalExterior evaluate(RationalVector point) {
        if(point.dimension()!=dimension) throw MathFailure.undefined("Form evaluation point must have the coordinate dimension");
        Map<Integer,Rational> result=new TreeMap<>();
        for(Map.Entry<Integer,MultivariatePolynomial> term : coefficients.entrySet()) result.put(term.getKey(),term.getValue().evaluate(point));
        return new RationalExterior(dimension,result);
    }
    public static PolynomialDifferentialForm fromExterior(RationalExterior value) {
        if(value.dimension()==0) throw MathFailure.undefined("Polynomial forms require a positive coordinate dimension");
        Map<Integer,MultivariatePolynomial> result=new TreeMap<>();
        for(Map.Entry<Integer,Rational> term : value.coefficients().entrySet()) result.put(term.getKey(),MultivariatePolynomial.constant(value.dimension(),term.getValue()));
        return new PolynomialDifferentialForm(value.dimension(),result);
    }
    public RationalExterior toExterior() {
        Map<Integer,Rational> result=new TreeMap<>();
        for(Map.Entry<Integer,MultivariatePolynomial> term : coefficients.entrySet()) result.put(term.getKey(),term.getValue().toRational());
        return new RationalExterior(dimension,result);
    }
    public MultivariatePolynomial scalarPart() { return coefficients.getOrDefault(0,MultivariatePolynomial.constant(dimension,Rational.ZERO)); }
    public MultivariatePolynomial toPolynomial() {
        if(!isZero() && coefficients.lastKey()!=0) throw MathFailure.undefined("The differential form contains nonzero positive-degree components");
        return scalarPart();
    }
    /** Lower a vector field with the standard Euclidean metric to a polynomial one-form. */
    public static PolynomialDifferentialForm fromVectorField(PolynomialMap field) {
        if(field.inputDimension()!=field.outputDimension()) throw MathFailure.undefined("One-form conversion requires a square polynomial vector field");
        checkDimension(field.inputDimension()); Map<Integer,MultivariatePolynomial> result=new TreeMap<>();
        for(int i=0;i<field.inputDimension();i++) result.put(1<<i,field.component(i));
        return new PolynomialDifferentialForm(field.inputDimension(),result);
    }
    public PolynomialMap toVectorField() {
        for(int mask : coefficients.keySet()) if(Integer.bitCount(mask)!=1) throw MathFailure.undefined("Vector field conversion requires only degree-one components");
        MultivariatePolynomial[] result=new MultivariatePolynomial[dimension]; MultivariatePolynomial zero=MultivariatePolynomial.constant(dimension,Rational.ZERO);
        for(int i=0;i<dimension;i++) result[i]=coefficients.getOrDefault(1<<i,zero);
        return new PolynomialMap(result);
    }
    @Override public boolean equals(Object other) {
        return other instanceof PolynomialDifferentialForm && dimension==((PolynomialDifferentialForm)other).dimension
                && coefficients.equals(((PolynomialDifferentialForm)other).coefficients);
    }
    @Override public int hashCode() { return Objects.hash(dimension,coefficients); }
    @Override public String toString() { return "Form(Q^"+dimension+")"+coefficients; }
}
