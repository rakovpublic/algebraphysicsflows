package mathematics.linear;

import mathematics.core.MathFailure;
import mathematics.numbers.Rational;
import java.io.Serializable;
import java.util.*;

/** Sparse elements of the exterior algebra of a standard oriented rational coordinate space. */
public final class RationalExterior implements Serializable {
    private static final long serialVersionUID=1L;
    public static final int MAX_DIMENSION=20,MAX_TERMS=100000,MAX_PRODUCTS=1000000;
    private final int dimension;
    private final SortedMap<Integer,Rational> coefficients;

    /** Bit i in a basis mask denotes e_i; its factors are ordered by increasing index. Mask zero is 1. */
    public RationalExterior(int dimension,Map<Integer,Rational> coefficients) {
        checkDimension(dimension); this.dimension=dimension;
        SortedMap<Integer,Rational> canonical=new TreeMap<>();
        for(Map.Entry<Integer,Rational> term : coefficients.entrySet()) {
            int mask=Objects.requireNonNull(term.getKey()); Rational value=Objects.requireNonNull(term.getValue());
            if(mask<0 || mask>=(1<<dimension)) throw MathFailure.invalid("Exterior basis mask is outside its ambient dimension");
            if(value.signum()!=0) canonical.put(mask,value);
            checkTerms(canonical.size());
        }
        this.coefficients=Collections.unmodifiableSortedMap(canonical);
    }
    private static void checkDimension(int dimension) {
        if(dimension<0) throw MathFailure.invalid("Exterior dimension must be nonnegative");
        if(dimension>MAX_DIMENSION) throw limit("Exterior dimension exceeds the coordinate representation limit");
    }
    private static MathFailure limit(String message) { return new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,message); }
    private static void checkTerms(int count) { if(count>MAX_TERMS) throw limit("Exterior sparse term limit exceeded"); }
    private static void addTerm(Map<Integer,Rational> target,int mask,Rational value) {
        Rational sum=target.getOrDefault(mask,Rational.ZERO).add(value);
        if(sum.signum()==0) target.remove(mask); else target.put(mask,sum);
        checkTerms(target.size());
    }
    private static final class Budget {
        long products;
        void spend(long count) { products+=count; if(products>MAX_PRODUCTS) throw limit("Exterior product work limit exceeded"); }
    }
    private void sameDimension(RationalExterior other) {
        if(dimension!=other.dimension) throw MathFailure.undefined("Exterior ambient dimensions must agree");
    }
    public int dimension() { return dimension; }
    public SortedMap<Integer,Rational> coefficients() { return coefficients; }
    public boolean isZero() { return coefficients.isEmpty(); }
    public static RationalExterior scalar(int dimension,Rational value) { return new RationalExterior(dimension,Collections.singletonMap(0,value)); }
    public static RationalExterior zero(int dimension) { return scalar(dimension,Rational.ZERO); }
    public static RationalExterior volume(int dimension) {
        checkDimension(dimension); return new RationalExterior(dimension,Collections.singletonMap((1<<dimension)-1,Rational.ONE));
    }
    public RationalExterior add(RationalExterior other) {
        sameDimension(other); Map<Integer,Rational> result=new TreeMap<>(coefficients);
        for(Map.Entry<Integer,Rational> term : other.coefficients.entrySet()) addTerm(result,term.getKey(),term.getValue());
        return new RationalExterior(dimension,result);
    }
    public RationalExterior scale(Rational scalar) {
        Map<Integer,Rational> result=new TreeMap<>();
        for(Map.Entry<Integer,Rational> term : coefficients.entrySet()) addTerm(result,term.getKey(),term.getValue().multiply(scalar));
        return new RationalExterior(dimension,result);
    }
    /** Sign for sorting concatenated increasing basis sequences; caller handles repeated factors separately. */
    private static int sign(int first,int second) {
        int parity=0;
        while(first!=0) { int bit=Integer.lowestOneBit(first); parity^=Integer.bitCount(second&(bit-1))&1; first^=bit; }
        return parity==0?1:-1;
    }
    public RationalExterior wedge(RationalExterior other) { return wedge(other,new Budget()); }
    private RationalExterior wedge(RationalExterior other,Budget budget) {
        sameDimension(other); budget.spend((long)coefficients.size()*other.coefficients.size());
        Map<Integer,Rational> result=new TreeMap<>();
        for(Map.Entry<Integer,Rational> first : coefficients.entrySet()) for(Map.Entry<Integer,Rational> second : other.coefficients.entrySet()) {
            int a=first.getKey(),b=second.getKey(); if((a&b)!=0) continue;
            Rational value=first.getValue().multiply(second.getValue());
            addTerm(result,a|b,sign(a,b)==1?value:value.negate());
        }
        return new RationalExterior(dimension,result);
    }
    public RationalExterior grade(int degree) {
        if(degree<0) throw MathFailure.undefined("Exterior degree must be nonnegative");
        Map<Integer,Rational> result=new TreeMap<>();
        for(Map.Entry<Integer,Rational> term : coefficients.entrySet()) if(Integer.bitCount(term.getKey())==degree) result.put(term.getKey(),term.getValue());
        return new RationalExterior(dimension,result);
    }
    public List<Integer> degrees() {
        Set<Integer> result=new TreeSet<>(); for(int mask : coefficients.keySet()) result.add(Integer.bitCount(mask));
        return Collections.unmodifiableList(new ArrayList<>(result));
    }
    public List<RationalExterior> terms() {
        List<RationalExterior> result=new ArrayList<>();
        for(Map.Entry<Integer,Rational> term : coefficients.entrySet()) result.add(new RationalExterior(dimension,Collections.singletonMap(term.getKey(),term.getValue())));
        return Collections.unmodifiableList(result);
    }
    public RationalExterior gradeInvolution() { return gradeSigns(false); }
    public RationalExterior reverse() { return gradeSigns(true); }
    private RationalExterior gradeSigns(boolean reverse) {
        Map<Integer,Rational> result=new TreeMap<>();
        for(Map.Entry<Integer,Rational> term : coefficients.entrySet()) {
            int degree=Integer.bitCount(term.getKey()),exponent=reverse?degree*(degree-1)/2:degree;
            result.put(term.getKey(),exponent%2==0?term.getValue():term.getValue().negate());
        }
        return new RationalExterior(dimension,result);
    }
    /** Standard Euclidean Hodge star: e_I wedge star(e_I) is the positive coordinate volume. */
    public RationalExterior hodgeStar() {
        Map<Integer,Rational> result=new TreeMap<>(); int full=(1<<dimension)-1;
        for(Map.Entry<Integer,Rational> term : coefficients.entrySet()) {
            int complement=full^term.getKey();
            result.put(complement,sign(term.getKey(),complement)==1?term.getValue():term.getValue().negate());
        }
        return new RationalExterior(dimension,result);
    }
    /** Left insertion i_v, identifying coordinate vectors with covectors through the standard dot product. */
    public RationalExterior interior(RationalVector vector) {
        if(vector.dimension()!=dimension) throw MathFailure.undefined("Contraction vector must match the exterior ambient dimension");
        Map<Integer,Rational> result=new TreeMap<>();
        for(Map.Entry<Integer,Rational> term : coefficients.entrySet()) {
            int mask=term.getKey(),remaining=mask,position=0;
            while(remaining!=0) {
                int bit=Integer.lowestOneBit(remaining),index=Integer.numberOfTrailingZeros(bit);
                Rational value=term.getValue().multiply(vector.get(index));
                addTerm(result,mask^bit,position%2==0?value:value.negate()); remaining^=bit; position++;
            }
        }
        return new RationalExterior(dimension,result);
    }
    public Rational dot(RationalExterior other) {
        sameDimension(other); Rational result=Rational.ZERO;
        for(Map.Entry<Integer,Rational> term : coefficients.entrySet()) result=result.add(term.getValue().multiply(other.coefficients.getOrDefault(term.getKey(),Rational.ZERO)));
        return result;
    }
    public Rational scalarPart() { return coefficients.getOrDefault(0,Rational.ZERO); }
    public Rational toScalar() {
        if(!coefficients.isEmpty() && coefficients.lastKey()!=0) throw MathFailure.undefined("The exterior element contains non-scalar grades");
        return scalarPart();
    }
    public static RationalExterior fromVector(RationalVector vector) {
        checkDimension(vector.dimension()); Map<Integer,Rational> result=new TreeMap<>();
        for(int i=0;i<vector.dimension();i++) addTerm(result,1<<i,vector.get(i));
        return new RationalExterior(vector.dimension(),result);
    }
    public RationalVector toVector() {
        for(int mask : coefficients.keySet()) if(Integer.bitCount(mask)!=1) throw MathFailure.undefined("The exterior element contains grades other than one");
        Rational[] result=new Rational[dimension];
        for(int i=0;i<dimension;i++) result[i]=coefficients.getOrDefault(1<<i,Rational.ZERO);
        return new RationalVector(result);
    }
    /** Covariant induced map on multivectors: send each basis generator to the corresponding matrix column. */
    public RationalExterior map(RationalMatrix matrix) {
        if(matrix.columns()!=dimension) throw MathFailure.undefined("Matrix columns must match the exterior source dimension");
        checkDimension(matrix.rows()); Budget budget=new Budget();
        List<RationalExterior> columns=new ArrayList<>();
        for(int c=0;c<dimension;c++) columns.add(fromVector(matrix.column(c)));
        Map<Integer,Rational> result=new TreeMap<>();
        for(Map.Entry<Integer,Rational> term : coefficients.entrySet()) {
            RationalExterior image=scalar(matrix.rows(),term.getValue()); int remaining=term.getKey();
            while(remaining!=0) {
                int bit=Integer.lowestOneBit(remaining); image=image.wedge(columns.get(Integer.numberOfTrailingZeros(bit)),budget); remaining^=bit;
            }
            for(Map.Entry<Integer,Rational> part : image.coefficients.entrySet()) addTerm(result,part.getKey(),part.getValue());
        }
        return new RationalExterior(matrix.rows(),result);
    }
    @Override public boolean equals(Object other) { return other instanceof RationalExterior && dimension==((RationalExterior)other).dimension && coefficients.equals(((RationalExterior)other).coefficients); }
    @Override public int hashCode() { return Objects.hash(dimension,coefficients); }
    @Override public String toString() { return "Exterior(Q^"+dimension+")"+coefficients; }
}
