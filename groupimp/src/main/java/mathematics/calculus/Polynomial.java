package mathematics.calculus;

import mathematics.numbers.Rational;
import java.io.Serializable;
import java.util.*;

/** Univariate Q[x]. Polynomial smoothness is structural; arbitrary callbacks are not accepted. */
public final class Polynomial implements Serializable {
    private static final long serialVersionUID=1L;
    private final List<Rational> coefficients;
    public Polynomial(Rational... input) {
        List<Rational> values=new ArrayList<>(Arrays.asList(input));
        for(Rational value : values) Objects.requireNonNull(value);
        if(values.isEmpty()) values.add(Rational.ZERO);
        while(values.size()>1 && values.get(values.size()-1).signum()==0) values.remove(values.size()-1);
        coefficients=Collections.unmodifiableList(values);
    }
    public int degree() { return coefficients.size()==1 && coefficients.get(0).signum()==0 ? -1 : coefficients.size()-1; }
    public Rational coefficient(int power) {
        if(power<0) throw new IllegalArgumentException("Negative exponent");
        return power<coefficients.size()?coefficients.get(power):Rational.ZERO;
    }
    public Rational evaluate(Rational x) {
        Rational result=Rational.ZERO;
        for(int i=coefficients.size()-1;i>=0;i--) result=result.multiply(x).add(coefficients.get(i));
        return result;
    }
    public Polynomial add(Polynomial b) {
        Rational[] values=new Rational[Math.max(coefficients.size(),b.coefficients.size())];
        for(int i=0;i<values.length;i++) values[i]=coefficient(i).add(b.coefficient(i));
        return new Polynomial(values);
    }
    public Polynomial multiply(Polynomial b) {
        Rational[] values=new Rational[coefficients.size()+b.coefficients.size()-1]; Arrays.fill(values,Rational.ZERO);
        for(int i=0;i<coefficients.size();i++) for(int j=0;j<b.coefficients.size();j++) values[i+j]=values[i+j].add(coefficient(i).multiply(b.coefficient(j)));
        return new Polynomial(values);
    }
    public Polynomial derivative() {
        Rational[] values=new Rational[Math.max(1,coefficients.size()-1)]; Arrays.fill(values,Rational.ZERO);
        for(int i=1;i<coefficients.size();i++) values[i-1]=coefficient(i).multiply(Rational.of(i));
        return new Polynomial(values);
    }
    public Polynomial derivative(int order) {
        if(order<0) throw new IllegalArgumentException("Negative derivative order");
        Polynomial result=this;
        for(int i=0;i<order && result.degree()>=0;i++) result=result.derivative();
        return result;
    }
    public Polynomial primitive(Rational integrationConstant) {
        Rational[] values=new Rational[coefficients.size()+1]; values[0]=Objects.requireNonNull(integrationConstant);
        for(int i=0;i<coefficients.size();i++) values[i+1]=coefficient(i).divide(Rational.of(i+1));
        return new Polynomial(values);
    }
    public Polynomial anchoredPrimitive(Rational anchor, Rational valueAtAnchor) {
        Polynomial normalized=primitive(Rational.ZERO);
        return primitive(valueAtAnchor.subtract(normalized.evaluate(anchor)));
    }
    public Rational integrate(Rational lower,Rational upper) {
        Polynomial primitive=primitive(Rational.ZERO);
        return primitive.evaluate(upper).subtract(primitive.evaluate(lower));
    }
    @Override public boolean equals(Object b) { return b instanceof Polynomial && coefficients.equals(((Polynomial)b).coefficients); }
    @Override public int hashCode() { return coefficients.hashCode(); }
    @Override public String toString() { return "Q[x]" + coefficients; }
}
