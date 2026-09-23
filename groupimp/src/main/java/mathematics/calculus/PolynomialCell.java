package mathematics.calculus;

import mathematics.core.MathFailure;
import mathematics.linear.RationalVector;
import mathematics.numbers.Rational;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** A polynomial parametrization of an oriented unit cube, or an explicit zero-dimensional point. */
public final class PolynomialCell implements Serializable,Comparable<PolynomialCell> {
    private static final long serialVersionUID=1L;
    public static final int MAX_DIMENSION=10,MAX_AMBIENT_DIMENSION=PolynomialDifferentialForm.MAX_DIMENSION;
    private final PolynomialMap map;
    private final RationalVector point;
    private PolynomialCell(PolynomialMap map,RationalVector point) { this.map=map; this.point=point; }
    static void checkAmbient(int dimension) {
        if(dimension<1) throw MathFailure.invalid("Polynomial cells require a positive ambient dimension");
        if(dimension>MAX_AMBIENT_DIMENSION) throw limit("Cell ambient dimension exceeds the coordinate limit");
    }
    private static MathFailure limit(String message) { return new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,message); }
    public static PolynomialCell parameterized(PolynomialMap map) {
        Objects.requireNonNull(map); checkAmbient(map.outputDimension());
        if(map.inputDimension()>MAX_DIMENSION) throw limit("Polynomial cell dimension exceeds the materialization limit");
        return new PolynomialCell(map,null);
    }
    public static PolynomialCell point(RationalVector point) {
        Objects.requireNonNull(point);
        if(point.dimension()==0) throw MathFailure.undefined("Cell points require positive ambient dimension");
        checkAmbient(point.dimension()); return new PolynomialCell(null,point);
    }
    public static PolynomialCell segment(RationalVector start,RationalVector end) {
        if(start.dimension()!=end.dimension()) throw MathFailure.undefined("Segment endpoint dimensions must agree");
        if(start.dimension()==0) throw MathFailure.undefined("Cell segments require positive ambient dimension");
        checkAmbient(start.dimension()); MultivariatePolynomial[] coordinates=new MultivariatePolynomial[start.dimension()];
        for(int i=0;i<coordinates.length;i++) coordinates[i]=MultivariatePolynomial.constant(1,start.get(i))
                .add(MultivariatePolynomial.variable(1,0).scale(end.get(i).subtract(start.get(i))));
        return parameterized(new PolynomialMap(coordinates));
    }
    public int dimension() { return map==null?0:map.inputDimension(); }
    public int ambientDimension() { return map==null?point.dimension():map.outputDimension(); }
    public PolynomialMap parameterization() {
        if(map==null) throw MathFailure.undefined("A point has no positive-dimensional polynomial parametrization");
        return map;
    }
    public RationalVector point() {
        if(point==null) throw MathFailure.undefined("The cell has positive parameter dimension");
        return point;
    }
    public RationalVector evaluate(RationalVector parameters) {
        return evaluate(parameters,new MultivariatePolynomial.Work());
    }
    private RationalVector evaluate(RationalVector parameters,MultivariatePolynomial.Work work) {
        if(parameters.dimension()!=dimension()) throw MathFailure.undefined("Cell evaluation requires one coordinate per parameter");
        for(int i=0;i<parameters.dimension();i++) if(parameters.get(i).compareTo(Rational.ZERO)<0 || parameters.get(i).compareTo(Rational.ONE)>0)
            throw MathFailure.undefined("Cell parameters must lie in the unit cube");
        chargeEvaluation(map,work);
        return map==null?point:map.evaluate(parameters);
    }
    private static void chargeEvaluation(PolynomialMap value,MultivariatePolynomial.Work work) {
        work.consume(1);
        if(value!=null) for(MultivariatePolynomial component : value.components())
            work.consume((long)component.coefficients().size()*value.inputDimension());
    }
    public int axis(BigInteger axis) {
        if(axis.signum()<0 || axis.compareTo(BigInteger.valueOf(dimension()))>=0) throw MathFailure.undefined("Cell face axis is outside its parameter space");
        return axis.intValueExact();
    }
    public PolynomialCell face(int axis,boolean upper) {
        return face(axis,upper,new MultivariatePolynomial.Work());
    }
    PolynomialCell face(int axis,boolean upper,MultivariatePolynomial.Work work) {
        if(axis<0 || axis>=dimension()) throw MathFailure.undefined("Cell face axis is outside its parameter space");
        chargeEvaluation(map,work);
        Rational endpoint=upper?Rational.ONE:Rational.ZERO;
        if(dimension()==1) return point(map.evaluate(new RationalVector(endpoint)));
        MultivariatePolynomial[] coordinates=new MultivariatePolynomial[ambientDimension()];
        for(int i=0;i<coordinates.length;i++) coordinates[i]=map.component(i).restrictCoordinate(axis,endpoint);
        return parameterized(new PolynomialMap(coordinates));
    }
    /** Lower then upper face for each increasing parameter axis, without boundary coefficients. */
    public List<PolynomialCell> faces() {
        List<PolynomialCell> result=new ArrayList<>(); MultivariatePolynomial.Work work=new MultivariatePolynomial.Work();
        for(int axis=0;axis<dimension();axis++) { result.add(face(axis,false,work)); result.add(face(axis,true,work)); }
        return Collections.unmodifiableList(result);
    }
    public List<RationalVector> vertices() {
        List<RationalVector> result=new ArrayList<>(); MultivariatePolynomial.Work work=new MultivariatePolynomial.Work();
        for(int mask=0;mask<(1<<dimension());mask++) {
            Rational[] parameters=new Rational[dimension()]; for(int axis=0;axis<dimension();axis++) parameters[axis]=((mask>>axis)&1)==0?Rational.ZERO:Rational.ONE;
            result.add(evaluate(new RationalVector(parameters),work));
        }
        return Collections.unmodifiableList(result);
    }
    public PolynomialCell pushforward(PolynomialMap outer) { return pushforward(outer,new MultivariatePolynomial.Work()); }
    PolynomialCell pushforward(PolynomialMap outer,MultivariatePolynomial.Work work) {
        if(outer.inputDimension()!=ambientDimension()) throw MathFailure.undefined("Pushforward map inputs must match the cell ambient dimension");
        checkAmbient(outer.outputDimension());
        if(map==null) chargeEvaluation(outer,work); else work.consume(1);
        return map==null?point(outer.evaluate(point)):parameterized(outer.compose(map,work));
    }
    /** Product orientation puts all first-cell parameters before all second-cell parameters. */
    public PolynomialCell product(PolynomialCell other) {
        return product(other,new MultivariatePolynomial.Work());
    }
    PolynomialCell product(PolynomialCell other,MultivariatePolynomial.Work work) {
        int ambient=ambientDimension()+other.ambientDimension(),parameters=dimension()+other.dimension(); checkAmbient(ambient);
        if(parameters>MAX_DIMENSION) throw limit("Cell product dimension exceeds the materialization limit");
        work.consume(1L+ambient);
        if(parameters==0) {
            Rational[] coordinates=new Rational[ambient];
            for(int i=0;i<ambientDimension();i++) coordinates[i]=point.get(i);
            for(int i=0;i<other.ambientDimension();i++) coordinates[ambientDimension()+i]=other.point.get(i);
            return point(new RationalVector(coordinates));
        }
        List<MultivariatePolynomial> coordinates=new ArrayList<>();
        appendCoordinates(coordinates,this,parameters,0,work); appendCoordinates(coordinates,other,parameters,dimension(),work);
        return parameterized(new PolynomialMap(coordinates.toArray(new MultivariatePolynomial[0])));
    }
    private static void appendCoordinates(List<MultivariatePolynomial> output,PolynomialCell cell,int variables,int offset,MultivariatePolynomial.Work work) {
        for(int i=0;i<cell.ambientDimension();i++) {
            if(cell.dimension()==0) { output.add(MultivariatePolynomial.constant(variables,cell.point.get(i))); continue; }
            work.consume((long)cell.map.component(i).coefficients().size()*variables);
            Map<List<Integer>,Rational> coefficients=new HashMap<>();
            for(Map.Entry<List<Integer>,Rational> term : cell.map.component(i).coefficients().entrySet()) {
                List<Integer> powers=new ArrayList<>(Collections.nCopies(variables,0));
                for(int j=0;j<cell.dimension();j++) powers.set(offset+j,term.getKey().get(j));
                coefficients.put(powers,term.getValue());
            }
            output.add(new MultivariatePolynomial(variables,coefficients));
        }
    }
    public PolynomialChain boundary() { return PolynomialChain.of(this).boundary(); }
    public Rational integrate(PolynomialDifferentialForm form) { return integrate(form,new MultivariatePolynomial.Work()); }
    Rational integrate(PolynomialDifferentialForm form,MultivariatePolynomial.Work work) {
        if(form.dimension()!=ambientDimension()) throw MathFailure.undefined("The form and cell ambient dimensions must agree");
        form.requireDegree(BigInteger.valueOf(dimension()));
        work.consume(1);
        if(map==null) work.consume((long)form.scalarPart().coefficients().size()*ambientDimension());
        return map==null?form.scalarPart().evaluate(point):form.pullback(map,work).integrateUnitCube();
    }
    @Override public int compareTo(PolynomialCell other) {
        int comparison=Integer.compare(ambientDimension(),other.ambientDimension()); if(comparison!=0) return comparison;
        comparison=Integer.compare(dimension(),other.dimension()); if(comparison!=0) return comparison;
        if(dimension()==0) {
            for(int i=0;i<ambientDimension();i++) { comparison=point.get(i).compareTo(other.point.get(i)); if(comparison!=0) return comparison; }
            return 0;
        }
        for(int i=0;i<ambientDimension();i++) {
            Iterator<Map.Entry<List<Integer>,Rational>> a=map.component(i).coefficients().entrySet().iterator(),b=other.map.component(i).coefficients().entrySet().iterator();
            while(a.hasNext() && b.hasNext()) {
                Map.Entry<List<Integer>,Rational> first=a.next(),second=b.next();
                for(int j=0;j<dimension();j++) { comparison=Integer.compare(first.getKey().get(j),second.getKey().get(j)); if(comparison!=0) return comparison; }
                comparison=first.getValue().compareTo(second.getValue()); if(comparison!=0) return comparison;
            }
            comparison=Boolean.compare(a.hasNext(),b.hasNext()); if(comparison!=0) return comparison;
        }
        return 0;
    }
    @Override public boolean equals(Object other) {
        return other instanceof PolynomialCell && Objects.equals(map,((PolynomialCell)other).map) && Objects.equals(point,((PolynomialCell)other).point);
    }
    @Override public int hashCode() { return Objects.hash(map,point); }
    @Override public String toString() { return map==null?"Cell(point="+point+")":"Cell(map="+map+")"; }
}
