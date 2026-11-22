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
    /** One left-handed Weyl field, including right-handed fields written as conjugates. */
    public static final class State implements Serializable {
        private static final long serialVersionUID=1L;
        private final String name;
        private final Rational t3,hypercharge,electricCharge;
        private State(String name,Rational t3,Rational hypercharge){
            this.name=name;this.t3=t3;this.hypercharge=hypercharge;this.electricCharge=t3.add(hypercharge);
        }
        public String name(){return name;}
        public Rational t3(){return t3;}
        public Rational hypercharge(){return hypercharge;}
        public Rational electricCharge(){return electricCharge;}
        @Override public String toString(){return name+" (T3="+t3+", Y="+hypercharge+", Q="+electricCharge+")";}
    }
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
    private static final List<State> STATES;
    static {
        List<State> states=new ArrayList<>();
        states.add(new State("nu_L",Rational.of(1,2),Rational.of(-1,2)));
        states.add(new State("e_L",Rational.of(-1,2),Rational.of(-1,2)));
        for(String color:Arrays.asList("r","g","b"))states.add(new State("u_L^"+color,Rational.of(1,2),Rational.of(1,6)));
        for(String color:Arrays.asList("r","g","b"))states.add(new State("d_L^"+color,Rational.of(-1,2),Rational.of(1,6)));
        states.add(new State("e_R^c",Rational.ZERO,Rational.ONE));
        states.add(new State("nu_R^c",Rational.ZERO,Rational.ZERO));
        for(String color:Arrays.asList("r","g","b"))states.add(new State("u_R^{c,"+color+"}",Rational.ZERO,Rational.of(-2,3)));
        for(String color:Arrays.asList("r","g","b"))states.add(new State("d_R^{c,"+color+"}",Rational.ZERO,Rational.of(1,3)));
        STATES=Collections.unmodifiableList(states);
    }
    public StandardModelWeylGeneration(){}
    public static List<Multiplet> multiplets(){return MULTIPLETS;}
    public static List<State> states(){return STATES;}
    public static int stateCount(){return MULTIPLETS.stream().mapToInt(Multiplet::multiplicity).sum();}
    public static Rational t3SquaredTrace(){return STATES.stream().map(s->s.t3().pow(2)).reduce(Rational.ZERO,Rational::add);}
    public static Rational electricChargeSquaredTrace(){return STATES.stream().map(s->s.electricCharge().pow(2)).reduce(Rational.ZERO,Rational::add);}
    /** Exact charge-trace normalization; equals 3/8 for this one-generation assignment. */
    public static Rational treeLevelGUTWeinbergRatio(){return t3SquaredTrace().divide(electricChargeSquaredTrace());}
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
