package mathematics.numbers;

import java.io.Serializable;
import java.util.*;

/**
 * Conventional one-generation left-handed Weyl charge signature under Q=T3+Y.
 * Right-handed Standard Model fields are represented by their left-handed conjugates.
 * This records the physical charge assignment; it deliberately does not identify these
 * states with a minimal left ideal or a Dixon coordinate basis.
 */
public final class StandardModelWeylGeneration implements Serializable {
    private static final long serialVersionUID=1L;
    public static final class Multiplet implements Serializable {
        private static final long serialVersionUID=1L;
        private final String name;
        private final int multiplicity;
        private final Rational hypercharge;
        private Multiplet(String name,int multiplicity,Rational hypercharge){this.name=name;this.multiplicity=multiplicity;this.hypercharge=hypercharge;}
        public String name(){return name;}
        public int multiplicity(){return multiplicity;}
        public Rational hypercharge(){return hypercharge;}
        @Override public String toString(){return name+" x "+multiplicity+" (Y="+hypercharge+")";}
    }
    private static final List<Multiplet> MULTIPLETS=Collections.unmodifiableList(Arrays.asList(
            new Multiplet("Q_L",6,Rational.of(1,6)),
            new Multiplet("u_R^c",3,Rational.of(-2,3)),
            new Multiplet("d_R^c",3,Rational.of(1,3)),
            new Multiplet("L_L",2,Rational.of(-1,2)),
            new Multiplet("e_R^c",1,Rational.ONE),
            new Multiplet("nu_R^c",1,Rational.ZERO)));
    public StandardModelWeylGeneration(){}
    public static List<Multiplet> multiplets(){return MULTIPLETS;}
    public static int stateCount(){return MULTIPLETS.stream().mapToInt(Multiplet::multiplicity).sum();}
    public static Rational hyperchargeTrace(){return MULTIPLETS.stream().map(m->m.hypercharge().multiply(Rational.of(m.multiplicity()))).reduce(Rational.ZERO,Rational::add);}
    public static Rational cubicHyperchargeTrace(){return MULTIPLETS.stream().map(m->m.hypercharge().pow(3).multiply(Rational.of(m.multiplicity()))).reduce(Rational.ZERO,Rational::add);}
    /** Dynkin-index-normalized SU(2)_L^2 U(1)_Y trace, with T(fundamental)=1/2. */
    public static Rational mixedSu2HyperchargeTrace(){
        Rational index=Rational.of(1,2);
        return Rational.of(3).multiply(Rational.of(1,6)).multiply(index)
                .add(Rational.of(-1,2).multiply(index));
    }
    /** Dynkin-index-normalized SU(3)_C^2 U(1)_Y trace; conjugate fundamentals have the same index. */
    public static Rational mixedSu3HyperchargeTrace(){
        Rational index=Rational.of(1,2);
        return Rational.of(2).multiply(Rational.of(1,6)).multiply(index)
                .add(Rational.of(-2,3).multiply(index))
                .add(Rational.of(1,3).multiply(index));
    }
    /** SU(3)^3 anomaly index: two left-handed fundamentals and two conjugate fundamentals. */
    public static int cubicSu3AnomalyIndex(){return 2-1-1;}
    /** Number of SU(2) doublets mod two; the global Witten anomaly vanishes when this is zero. */
    public static int su2DoubletParity(){return (3+1)%2;}
    public static boolean anomalyFreeHyperchargeSignature(){
        return stateCount()==16&&hyperchargeTrace().equals(Rational.ZERO)&&cubicHyperchargeTrace().equals(Rational.ZERO)
                &&mixedSu2HyperchargeTrace().equals(Rational.ZERO)&&mixedSu3HyperchargeTrace().equals(Rational.ZERO)
                &&cubicSu3AnomalyIndex()==0&&su2DoubletParity()==0;
    }
}
