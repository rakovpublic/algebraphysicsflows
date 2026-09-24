package mathematics.probability;

import algebra.imp.Algebra;
import mathematics.core.MathFailure;
import mathematics.foundations.*;
import mathematics.linear.*;
import mathematics.numbers.Rational;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;
import static operations.OperationMembers.require;

/** Exact stochastic kernels between explicit finite integer sets, ordered by increasing label. */
public final class FiniteMarkovKernel implements Serializable {
    private static final long serialVersionUID=1L;
    public static final int MAX_STATES=64, MAX_STEPS=10000;
    private final Algebra<BigInteger> outcomes;
    private final FiniteSet<BigInteger> domain,codomain;
    private final Map<BigInteger,FiniteDistribution<BigInteger>> rows;
    private static final class Work {
        long remaining=5000000;
        void use(long amount) {
            remaining-=amount;
            if(remaining<0) throw limit("5000000 work units");
        }
    }
    private static MathFailure limit(String detail) {
        return new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Finite Markov calculation exceeds "+detail);
    }
    private static FiniteSet<BigInteger> ordered(Algebra<BigInteger> outcomes,FiniteSet<BigInteger> labels) {
        if(labels.size()>MAX_STATES) throw limit("64 states per boundary");
        for(BigInteger label : labels.members()) require(outcomes,label);
        return new FiniteSet<>(new TreeSet<>(labels.members()));
    }
    public FiniteMarkovKernel(Algebra<BigInteger> outcomes,FiniteSet<BigInteger> domain,FiniteSet<BigInteger> codomain,
                              Map<BigInteger,FiniteDistribution<BigInteger>> rows) {
        this.outcomes=Objects.requireNonNull(outcomes); this.domain=ordered(outcomes,domain); this.codomain=ordered(outcomes,codomain);
        if(!domain.members().equals(rows.keySet())) throw MathFailure.invalid("Kernel rows must cover exactly its declared domain");
        Map<BigInteger,FiniteDistribution<BigInteger>> copy=new LinkedHashMap<>();
        for(BigInteger state : this.domain.members()) {
            FiniteDistribution<BigInteger> row=Objects.requireNonNull(rows.get(state));
            if(row.outcomes()!=outcomes || !codomain.members().containsAll(row.masses().keySet()))
                throw MathFailure.invalid("Kernel row must use the same outcome Algebra and stay in the declared codomain");
            Map<BigInteger,Rational> sorted=new LinkedHashMap<>();
            for(BigInteger target : this.codomain.members()) if(row.masses().containsKey(target)) sorted.put(target,row.masses().get(target));
            copy.put(state,new FiniteDistribution<>(outcomes,sorted));
        }
        this.rows=Collections.unmodifiableMap(copy);
    }
    public Algebra<BigInteger> outcomes() { return outcomes; }
    public FiniteSet<BigInteger> domain() { return domain; }
    public FiniteSet<BigInteger> codomain() { return codomain; }
    public Map<BigInteger,FiniteDistribution<BigInteger>> rowMap() { return rows; }
    public List<FiniteDistribution<BigInteger>> rows() { return Collections.unmodifiableList(new ArrayList<>(rows.values())); }
    public boolean isChain() { return domain.equals(codomain); }
    private void chain() { if(!isChain()) throw MathFailure.undefined("This operation requires equal declared source and target state sets"); }
    public FiniteDistribution<BigInteger> row(BigInteger state) {
        require(outcomes,state);
        if(!domain.contains(state)) throw MathFailure.undefined("State is outside the kernel domain");
        return rows.get(state);
    }
    public Rational probability(BigInteger source,BigInteger target) {
        require(outcomes,target);
        if(!codomain.contains(target)) throw MathFailure.undefined("State is outside the kernel codomain");
        return row(source).masses().getOrDefault(target,Rational.ZERO);
    }
    private void distribution(FiniteDistribution<BigInteger> value) {
        if(value.outcomes()!=outcomes || !domain.members().containsAll(value.masses().keySet()))
            throw MathFailure.undefined("Distribution must use the kernel's outcome Algebra and be supported in its domain");
    }
    private void event(FiniteSet<BigInteger> target) {
        chain();
        if(!target.subsetOf(domain)) throw MathFailure.undefined("Hitting or absorbing event must be contained in the state space");
    }
    public FiniteDistribution<BigInteger> apply(FiniteDistribution<BigInteger> value) { return apply(value,new Work()); }
    private FiniteDistribution<BigInteger> apply(FiniteDistribution<BigInteger> value,Work work) {
        distribution(value); work.use(1L+(long)domain.size()*codomain.size());
        return advanceMasses(value);
    }
    /** Called only after membership and the complete calculation's work have been checked. */
    private FiniteDistribution<BigInteger> advanceMasses(FiniteDistribution<BigInteger> value) {
        Map<BigInteger,Rational> result=new TreeMap<>();
        for(Map.Entry<BigInteger,Rational> mass : value.masses().entrySet())
            for(Map.Entry<BigInteger,Rational> transition : rows.get(mass.getKey()).masses().entrySet())
                result.merge(transition.getKey(),mass.getValue().multiply(transition.getValue()),Rational::add);
        return new FiniteDistribution<>(outcomes,result);
    }
    /** This kernel after before has transition matrix P_before * P_this. */
    public FiniteMarkovKernel compose(FiniteMarkovKernel before) { return compose(before,new Work()); }
    private FiniteMarkovKernel compose(FiniteMarkovKernel before,Work work) {
        if(before.outcomes!=outcomes || !before.codomain.equals(domain))
            throw MathFailure.undefined("Kernel composition requires identical middle state sets and outcome Algebra");
        work.use(1L+(long)before.domain.size()*domain.size()*codomain.size());
        Map<BigInteger,FiniteDistribution<BigInteger>> result=new LinkedHashMap<>();
        for(BigInteger state : before.domain.members()) result.put(state,apply(before.rows.get(state),work));
        return new FiniteMarkovKernel(outcomes,before.domain,codomain,result);
    }
    public static FiniteMarkovKernel identity(Algebra<BigInteger> outcomes,FiniteSet<BigInteger> states) {
        FiniteSet<BigInteger> labels=ordered(outcomes,states); Map<BigInteger,FiniteDistribution<BigInteger>> rows=new LinkedHashMap<>();
        for(BigInteger state : labels.members()) rows.put(state,new FiniteDistribution<>(outcomes,Collections.singletonMap(state,Rational.ONE)));
        return new FiniteMarkovKernel(outcomes,labels,labels,rows);
    }
    public static FiniteMarkovKernel fromFunction(FiniteFunction<BigInteger,BigInteger> function) {
        if(function.source!=function.target) throw MathFailure.undefined("A Markov kernel requires one outcome Algebra for both boundaries");
        ordered(function.source,function.domain); ordered(function.target,function.codomain);
        Map<BigInteger,FiniteDistribution<BigInteger>> rows=new LinkedHashMap<>();
        for(BigInteger state : function.domain.members()) rows.put(state,new FiniteDistribution<>(function.target,Collections.singletonMap(function.apply(state),Rational.ONE)));
        return new FiniteMarkovKernel(function.source,function.domain,function.codomain,rows);
    }
    public boolean isDeterministic() { for(FiniteDistribution<BigInteger> row : rows.values()) if(row.masses().size()!=1) return false; return true; }
    public FiniteFunction<BigInteger,BigInteger> toFunction() {
        if(!isDeterministic()) throw MathFailure.undefined("Only Dirac rows define a deterministic function");
        Map<BigInteger,BigInteger> mapping=new LinkedHashMap<>();
        for(BigInteger state : domain.members()) mapping.put(state,rows.get(state).masses().keySet().iterator().next());
        return new FiniteFunction<>(outcomes,outcomes,domain,codomain,mapping);
    }
    public RationalMatrix toMatrix() {
        if(domain.size()==0 || codomain.size()==0) throw MathFailure.undefined("The matrix carrier requires positive dimensions");
        Rational[][] values=new Rational[domain.size()][codomain.size()]; int r=0;
        for(BigInteger state : domain.members()) { int c=0; for(BigInteger target : codomain.members()) values[r][c++]=probability(state,target); r++; }
        return new RationalMatrix(values);
    }
    public static FiniteMarkovKernel fromMatrix(Algebra<BigInteger> outcomes,RationalMatrix matrix,
                                               FiniteSet<BigInteger> domain,FiniteSet<BigInteger> codomain) {
        FiniteSet<BigInteger> source=ordered(outcomes,domain),target=ordered(outcomes,codomain);
        if(matrix.rows()!=source.size() || matrix.columns()!=target.size()) throw MathFailure.undefined("Matrix shape must match the two finite state sets");
        Map<BigInteger,FiniteDistribution<BigInteger>> rows=new LinkedHashMap<>(); int r=0;
        for(BigInteger state : source.members()) {
            Map<BigInteger,Rational> masses=new LinkedHashMap<>(); Rational sum=Rational.ZERO; int c=0;
            for(BigInteger label : target.members()) {
                Rational mass=matrix.get(r,c++);
                if(mass.signum()<0) throw MathFailure.undefined("A stochastic matrix cannot have negative entries");
                masses.put(label,mass); sum=sum.add(mass);
            }
            if(!sum.equals(Rational.ONE)) throw MathFailure.undefined("Every stochastic matrix row must sum exactly to one");
            rows.put(state,new FiniteDistribution<>(outcomes,masses)); r++;
        }
        return new FiniteMarkovKernel(outcomes,source,target,rows);
    }
    private static int steps(BigInteger value) {
        if(value.signum()<0) throw MathFailure.undefined("Step count must be nonnegative");
        if(value.compareTo(BigInteger.valueOf(MAX_STEPS))>0) throw limit("10000 steps");
        return value.intValueExact();
    }
    public FiniteMarkovKernel power(BigInteger exponent) {
        chain(); int count=steps(exponent); Work work=new Work(); FiniteMarkovKernel result=identity(outcomes,domain),factor=this;
        while(count>0) {
            if((count&1)!=0) result=factor.compose(result,work);
            count>>=1;
            if(count>0) factor=factor.compose(factor,work);
        }
        return result;
    }
    /** Initial distribution followed by each successive time marginal, never random sample paths. */
    public List<FiniteDistribution<BigInteger>> orbit(FiniteDistribution<BigInteger> initial,BigInteger count) {
        chain(); distribution(initial); int length=steps(count); Work work=new Work();
        // Preflight the whole finite trajectory before allocating or computing any of its output.
        work.use((long)length*(1L+(long)domain.size()*domain.size()));
        List<FiniteDistribution<BigInteger>> result=new ArrayList<>(); result.add(initial); FiniteDistribution<BigInteger> current=initial;
        for(int i=0;i<length;i++) { current=advanceMasses(current); result.add(current); }
        return Collections.unmodifiableList(result);
    }
    private final class Graph {
        final List<BigInteger> labels=new ArrayList<>(domain.members());
        final boolean[][] reach=new boolean[labels.size()][labels.size()];
        final List<FiniteSet<BigInteger>> classes=new ArrayList<>(),closed=new ArrayList<>();
        Graph(Work work) {
            chain(); int n=labels.size(); work.use(1L+2L*n*n*n+(long)n*n);
            for(int i=0;i<n;i++) for(int j=0;j<n;j++) reach[i][j]=i==j || probability(labels.get(i),labels.get(j)).signum()>0;
            for(int k=0;k<n;k++) for(int i=0;i<n;i++) for(int j=0;j<n;j++) reach[i][j]|=reach[i][k] && reach[k][j];
            Set<BigInteger> assigned=new HashSet<>();
            for(int i=0;i<n;i++) if(!assigned.contains(labels.get(i))) {
                List<BigInteger> members=new ArrayList<>();
                for(int j=0;j<n;j++) if(reach[i][j] && reach[j][i]) members.add(labels.get(j));
                FiniteSet<BigInteger> component=new FiniteSet<>(members); classes.add(component); assigned.addAll(members);
                boolean recurrent=true;
                for(BigInteger state : members) if(!component.members().containsAll(rows.get(state).masses().keySet())) recurrent=false;
                if(recurrent) closed.add(component);
            }
        }
    }
    public List<FiniteSet<BigInteger>> communicatingClasses() { return Collections.unmodifiableList(new Graph(new Work()).classes); }
    public List<FiniteSet<BigInteger>> recurrentClasses() { return Collections.unmodifiableList(new Graph(new Work()).closed); }
    public FiniteSet<BigInteger> transientStates() {
        Graph graph=new Graph(new Work()); FiniteSet<BigInteger> recurrent=FiniteSet.of();
        for(FiniteSet<BigInteger> component : graph.closed) recurrent=recurrent.union(component);
        return domain.difference(recurrent);
    }
    public boolean isIrreducible() { return communicatingClasses().size()==1; }
    public FiniteSet<BigInteger> absorbingStates() {
        chain(); List<BigInteger> states=new ArrayList<>();
        for(BigInteger state : domain.members()) if(probability(state,state).equals(Rational.ONE)) states.add(state);
        return new FiniteSet<>(states);
    }
    private static RationalVector unique(Rational[][] equations,Rational[] rhs,Work work) {
        int n=rhs.length,variables=equations[0].length; work.use(1L+3L*n*variables*variables);
        RationalAffineSpace solution=new RationalMatrix(equations).solve(new RationalVector(rhs));
        if(!solution.isUnique()) throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Expected a unique finite Markov linear-system solution");
        return solution.particular();
    }
    /** One extreme stationary distribution for each closed communicating class, ordered by its least label. */
    public List<FiniteDistribution<BigInteger>> stationaryExtremes() {
        Work work=new Work(); Graph graph=new Graph(work); List<FiniteDistribution<BigInteger>> result=new ArrayList<>();
        for(FiniteSet<BigInteger> component : graph.closed) {
            List<BigInteger> labels=new ArrayList<>(component.members()); int n=labels.size();
            Rational[][] equations=new Rational[n+1][n]; Rational[] rhs=new Rational[n+1]; Arrays.fill(rhs,Rational.ZERO); rhs[n]=Rational.ONE;
            for(int i=0;i<n;i++) for(int j=0;j<n;j++) equations[i][j]=probability(labels.get(j),labels.get(i)).subtract(i==j?Rational.ONE:Rational.ZERO);
            Arrays.fill(equations[n],Rational.ONE); RationalVector solution=unique(equations,rhs,work);
            Map<BigInteger,Rational> masses=new LinkedHashMap<>(); for(int i=0;i<n;i++) masses.put(labels.get(i),solution.get(i));
            result.add(new FiniteDistribution<>(outcomes,masses));
        }
        return Collections.unmodifiableList(result);
    }
    public FiniteDistribution<BigInteger> stationary() {
        List<FiniteDistribution<BigInteger>> extremes=stationaryExtremes();
        if(extremes.size()!=1) throw MathFailure.undefined("A unique stationary distribution is required");
        return extremes.get(0);
    }
    public boolean isStationary(FiniteDistribution<BigInteger> distribution) { chain(); return apply(distribution).equals(distribution); }
    public boolean isReversible(FiniteDistribution<BigInteger> distribution) {
        chain(); distribution(distribution);
        for(BigInteger i : domain.members()) for(BigInteger j : domain.members())
            if(!distribution.masses().getOrDefault(i,Rational.ZERO).multiply(probability(i,j))
                    .equals(distribution.masses().getOrDefault(j,Rational.ZERO).multiply(probability(j,i)))) return false;
        return true;
    }
    public FiniteMarkovKernel reverse(FiniteDistribution<BigInteger> stationary) {
        chain(); distribution(stationary);
        if(stationary.masses().size()!=domain.size() || !isStationary(stationary))
            throw MathFailure.undefined("Time reversal requires a stationary distribution positive at every declared state");
        Map<BigInteger,FiniteDistribution<BigInteger>> result=new LinkedHashMap<>();
        for(BigInteger i : domain.members()) {
            Map<BigInteger,Rational> masses=new LinkedHashMap<>();
            for(BigInteger j : domain.members()) masses.put(j,stationary.masses().get(j).multiply(probability(j,i)).divide(stationary.masses().get(i)));
            result.put(i,new FiniteDistribution<>(outcomes,masses));
        }
        return new FiniteMarkovKernel(outcomes,domain,codomain,result);
    }
    public FiniteMarkovKernel absorbingOn(FiniteSet<BigInteger> target) {
        event(target); Map<BigInteger,FiniteDistribution<BigInteger>> result=new LinkedHashMap<>(rows);
        for(BigInteger state : target.members()) result.put(state,new FiniteDistribution<>(outcomes,Collections.singletonMap(state,Rational.ONE)));
        return new FiniteMarkovKernel(outcomes,domain,codomain,result);
    }
    public RationalVector hittingProbabilities(FiniteSet<BigInteger> target) { return hittingProbabilities(target,new Work()); }
    private RationalVector hittingProbabilities(FiniteSet<BigInteger> target,Work work) {
        event(target); Graph graph=new Graph(work); List<BigInteger> unknown=new ArrayList<>();
        Rational[] result=new Rational[domain.size()]; Arrays.fill(result,Rational.ZERO);
        for(int i=0;i<graph.labels.size();i++) {
            if(target.contains(graph.labels.get(i))) { result[i]=Rational.ONE; continue; }
            for(int j=0;j<graph.labels.size();j++) if(target.contains(graph.labels.get(j)) && graph.reach[i][j]) { unknown.add(graph.labels.get(i)); break; }
        }
        if(!unknown.isEmpty()) {
            int n=unknown.size(); Rational[][] equations=new Rational[n][n]; Rational[] rhs=new Rational[n];
            for(int i=0;i<n;i++) {
                rhs[i]=Rational.ZERO;
                for(BigInteger state : target.members()) rhs[i]=rhs[i].add(probability(unknown.get(i),state));
                for(int j=0;j<n;j++) equations[i][j]=(i==j?Rational.ONE:Rational.ZERO).subtract(probability(unknown.get(i),unknown.get(j)));
            }
            RationalVector solution=unique(equations,rhs,work);
            for(int i=0;i<n;i++) result[graph.labels.indexOf(unknown.get(i))]=solution.get(i);
        }
        return new RationalVector(result);
    }
    /** E[T_target], where T includes time zero. Undefined if any starting state has infinite expectation. */
    public RationalVector meanHittingTimes(FiniteSet<BigInteger> target) {
        event(target); Work work=new Work(); RationalVector probabilities=hittingProbabilities(target,work);
        for(int i=0;i<probabilities.dimension();i++) if(!probabilities.get(i).equals(Rational.ONE))
            throw MathFailure.undefined("At least one state has infinite mean hitting time; Q cannot represent infinity");
        List<BigInteger> labels=new ArrayList<>(domain.members()),unknown=new ArrayList<>(domain.difference(target).members());
        Rational[] result=new Rational[labels.size()]; Arrays.fill(result,Rational.ZERO);
        if(!unknown.isEmpty()) {
            int n=unknown.size(); Rational[][] equations=new Rational[n][n]; Rational[] rhs=new Rational[n]; Arrays.fill(rhs,Rational.ONE);
            for(int i=0;i<n;i++) for(int j=0;j<n;j++) equations[i][j]=(i==j?Rational.ONE:Rational.ZERO).subtract(probability(unknown.get(i),unknown.get(j)));
            RationalVector solution=unique(equations,rhs,work);
            for(int i=0;i<n;i++) result[labels.indexOf(unknown.get(i))]=solution.get(i);
        }
        return new RationalVector(result);
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof FiniteMarkovKernel)) return false; FiniteMarkovKernel kernel=(FiniteMarkovKernel)other;
        return outcomes==kernel.outcomes && domain.equals(kernel.domain) && codomain.equals(kernel.codomain) && rows.equals(kernel.rows);
    }
    @Override public int hashCode() { return Objects.hash(outcomes,domain,codomain,rows); }
    @Override public String toString() { return "Kernel"+domain+"->"+codomain+rows; }
}
