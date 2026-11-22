package mathematics.numbers;

import mathematics.calculus.Polynomial;
import mathematics.core.MathFailure;
import java.io.Serializable;
import java.math.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Exact Q(zeta_n), represented in the power basis modulo the nth cyclotomic polynomial. */
public final class RationalCyclotomic implements Serializable {
    private static final long serialVersionUID=1L;
    private static final Map<Integer,Polynomial> MODULI=new ConcurrentHashMap<>();
    private final int order;
    private final Polynomial modulus;
    private final Polynomial representative;
    public RationalCyclotomic(int order,Rational... coefficients){
        if(order<1||order>4096)throw new IllegalArgumentException("Cyclotomic order must be between 1 and 4096");
        this.order=order;this.modulus=cyclotomicPolynomial(order);
        this.representative=new Polynomial(coefficients).remainder(modulus);
    }
    private RationalCyclotomic(int order,Polynomial value){this.order=order;this.modulus=cyclotomicPolynomial(order);this.representative=value.remainder(modulus);}
    public static RationalCyclotomic zero(int n){return new RationalCyclotomic(n,Polynomial.ZERO);}
    public static RationalCyclotomic one(int n){return new RationalCyclotomic(n,Polynomial.ONE);}
    public static RationalCyclotomic zeta(int n){return new RationalCyclotomic(n,new Polynomial(Rational.ZERO,Rational.ONE));}
    public static RationalCyclotomic fromRational(int n,Rational value){return new RationalCyclotomic(n,new Polynomial(value));}
    public static RationalCyclotomic fromComplex(int n,RationalComplex value){
        if(n%4!=0)throw MathFailure.undefined("Q(zeta_n) contains the selected Q(i) embedding only when 4 divides n");
        Rational[] coefficients=new Rational[n/4+1];Arrays.fill(coefficients,Rational.ZERO);coefficients[0]=value.real;coefficients[n/4]=value.imaginary;
        return new RationalCyclotomic(n,coefficients);
    }
    public Rational toRational(){if(representative.degree()>0)throw MathFailure.undefined("Cyclotomic element is not rational");return representative.coefficient(0);}
    public RationalComplex toComplex(){
        if(order%4!=0)throw MathFailure.undefined("Selected Q(i) subfield is absent because 4 does not divide the cyclotomic order");
        Rational imaginary=representative.coefficient(order/4);Rational[] remainder=new Rational[Math.max(1,representative.degree()+1)];
        for(int i=0;i<remainder.length;i++)remainder[i]=i==order/4?Rational.ZERO:representative.coefficient(i);
        Polynomial realPart=new Polynomial(remainder);if(realPart.degree()>0)throw MathFailure.undefined("Cyclotomic element is outside the selected Q(i) subfield");
        return new RationalComplex(realPart.coefficient(0),imaginary);
    }
    public static Polynomial cyclotomicPolynomial(int n){
        if(n<1||n>4096)throw new IllegalArgumentException("Cyclotomic order must be between 1 and 4096");
        Polynomial cached=MODULI.get(n);if(cached!=null)return cached;
        Rational[] xMinusOne=new Rational[n+1];Arrays.fill(xMinusOne,Rational.ZERO);xMinusOne[0]=Rational.of(-1);xMinusOne[n]=Rational.ONE;
        Polynomial result=new Polynomial(xMinusOne);
        for(int d=1;d<n;d++)if(n%d==0)result=result.divideExact(cyclotomicPolynomial(d));
        Polynomial previous=MODULI.putIfAbsent(n,result);return previous==null?result:previous;
    }
    public int order(){return order;}
    public int degree(){return modulus.degree();}
    public Polynomial cyclotomicPolynomial(){return modulus;}
    public Polynomial representative(){return representative;}
    private void sameField(RationalCyclotomic b){if(order!=b.order)throw new IllegalArgumentException("Cyclotomic fields have different declared orders");}
    public RationalCyclotomic add(RationalCyclotomic b){sameField(b);return new RationalCyclotomic(order,representative.add(b.representative));}
    public RationalCyclotomic negate(){return new RationalCyclotomic(order,representative.negate());}
    public RationalCyclotomic subtract(RationalCyclotomic b){return add(b.negate());}
    public RationalCyclotomic multiply(RationalCyclotomic b){sameField(b);return new RationalCyclotomic(order,representative.multiply(b.representative));}
    public RationalCyclotomic scale(Rational factor){return new RationalCyclotomic(order,representative.scale(factor));}
    public boolean isZero(){return representative.degree()<0;}
    public RationalCyclotomic inverse(){
        if(isZero())throw MathFailure.undefined("Zero has no inverse in a cyclotomic field");
        Polynomial r0=modulus,r1=representative,s0=Polynomial.ZERO,s1=Polynomial.ONE;
        while(r1.degree()>=0){mathematics.foundations.Pair<Polynomial,Polynomial> qr=r0.divideAndRemainder(r1);
            Polynomial r2=qr.second,s2=s0.subtract(qr.first.multiply(s1));r0=r1;r1=r2;s0=s1;s1=s2;}
        if(r0.degree()!=0)throw new IllegalStateException("Cyclotomic modulus was not irreducible over Q");
        return new RationalCyclotomic(order,s0.scale(Rational.ONE.divide(r0.coefficient(0))));
    }
    public RationalCyclotomic divide(RationalCyclotomic b){sameField(b);return multiply(b.inverse());}
    public RationalCyclotomic pow(long exponent){
        return pow(BigInteger.valueOf(exponent));
    }
    public RationalCyclotomic pow(BigInteger exponent){
        Objects.requireNonNull(exponent);RationalCyclotomic base=exponent.signum()<0?inverse():this,result=one(order);BigInteger e=exponent.abs();
        while(e.signum()>0){if(e.testBit(0))result=result.multiply(base);e=e.shiftRight(1);if(e.signum()>0)base=base.multiply(base);}return result;
    }
    /** Galois complex conjugation zeta_n -> zeta_n^-1. */
    public RationalCyclotomic conjugate(){
        RationalCyclotomic result=zero(order);
        for(int k=0;k<=representative.degree();k++)if(representative.coefficient(k).signum()!=0)
            result=result.add(zeta(order).pow(-((long)k)).scale(representative.coefficient(k)));
        return result;
    }
    /** Decimal approximation of a+b*i with a documented requested precision. */
    public DecimalComplex evaluate(int precision){
        if(precision<1||precision>10000)throw new IllegalArgumentException("Precision must be between 1 and 10000 digits");
        MathContext mc=new MathContext(precision+12,RoundingMode.HALF_EVEN);
        BigDecimal pi=pi(mc),twoPi=pi.multiply(BigDecimal.valueOf(2),mc),n=BigDecimal.valueOf(order);
        BigDecimal real=BigDecimal.ZERO,imag=BigDecimal.ZERO;
        for(int k=0;k<=representative.degree();k++){
            Rational coefficient=representative.coefficient(k);if(coefficient.signum()==0)continue;
            BigDecimal theta=twoPi.multiply(BigDecimal.valueOf(k),mc).divide(n,mc);
            DecimalComplex term=trig(theta,mc);BigDecimal c=new BigDecimal(coefficient.numerator()).divide(new BigDecimal(coefficient.denominator()),mc);
            real=real.add(c.multiply(term.real,mc),mc);imag=imag.add(c.multiply(term.imaginary,mc),mc);
        }
        MathContext output=new MathContext(precision,RoundingMode.HALF_EVEN);return new DecimalComplex(real.round(output),imag.round(output));
    }
    /** BigDecimal sine for a measured angle in radians; unlike a cyclotomic phase, the angle need not be a rational multiple of pi. */
    public static BigDecimal sinRadians(BigDecimal angle,int precision){
        if(precision<1||precision>10000)throw new IllegalArgumentException("Precision must be between 1 and 10000 digits");
        MathContext mc=new MathContext(precision+12,RoundingMode.HALF_EVEN);BigDecimal pi=pi(mc),twoPi=pi.multiply(BigDecimal.valueOf(2),mc);
        BigDecimal reduced=angle.remainder(twoPi,mc);
        if(reduced.compareTo(pi)>0)reduced=reduced.subtract(twoPi,mc);
        if(reduced.compareTo(pi.negate())<0)reduced=reduced.add(twoPi,mc);
        return trig(reduced,mc).imaginary.round(new MathContext(precision,RoundingMode.HALF_EVEN));
    }
    private static DecimalComplex trig(BigDecimal x,MathContext mc){
        BigDecimal x2=x.multiply(x,mc),sin=x,cos=BigDecimal.ONE,st=x,ct=BigDecimal.ONE;
        for(int k=1;k<mc.getPrecision()*2;k++){
            st=st.multiply(x2,mc).negate().divide(BigDecimal.valueOf((2L*k)*(2L*k+1)),mc);
            ct=ct.multiply(x2,mc).negate().divide(BigDecimal.valueOf((2L*k-1)*(2L*k)),mc);
            sin=sin.add(st,mc);cos=cos.add(ct,mc);
            if(st.abs().compareTo(BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision()))<0&&ct.abs().compareTo(BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision()))<0)break;
        }
        return new DecimalComplex(cos,sin);
    }
    private static BigDecimal pi(MathContext mc){return arctanInverse(5,mc).multiply(BigDecimal.valueOf(16),mc).subtract(arctanInverse(239,mc).multiply(BigDecimal.valueOf(4),mc),mc);}
    private static BigDecimal arctanInverse(int inverse,MathContext mc){
        BigDecimal x=BigDecimal.ONE.divide(BigDecimal.valueOf(inverse),mc),x2=x.multiply(x,mc),term=x,sum=x;
        for(int k=1;k<mc.getPrecision()*2;k++){term=term.multiply(x2,mc).negate();BigDecimal add=term.divide(BigDecimal.valueOf(2L*k+1),mc);sum=sum.add(add,mc);if(add.abs().compareTo(BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision()))<0)break;}
        return sum;
    }
    public static final class DecimalComplex implements Serializable {
        private static final long serialVersionUID=1L;
        public final BigDecimal real,imaginary;
        public DecimalComplex(BigDecimal real,BigDecimal imaginary){this.real=real;this.imaginary=imaginary;}
        @Override public String toString(){return "("+real+")+("+imaginary+")i";}
    }
    @Override public boolean equals(Object o){return o instanceof RationalCyclotomic&&order==((RationalCyclotomic)o).order&&representative.equals(((RationalCyclotomic)o).representative);}
    @Override public int hashCode(){return Objects.hash(order,representative);}
    @Override public String toString(){return "Q(zeta_"+order+")"+representative;}
}
