package mathematics.structures;

import mathematics.core.MathFailure;
import mathematics.foundations.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** A labelled finite category whose complete table is checked at construction. */
public final class FiniteCategory implements Serializable {
    private static final long serialVersionUID=1L;
    public static final int MAX_ARROWS=128;
    private final FiniteSet<BigInteger> objects;
    private final Map<BigInteger,Pair<BigInteger,BigInteger>> arrows;
    private final Map<BigInteger,BigInteger> identities;
    private final Map<Pair<BigInteger,BigInteger>,BigInteger> composition;

    /** A composition key (f,g) means first f, then g, i.e. g after f. */
    public FiniteCategory(FiniteSet<BigInteger> objects,Map<BigInteger,Pair<BigInteger,BigInteger>> arrows,
            Map<BigInteger,BigInteger> identities,Map<Pair<BigInteger,BigInteger>,BigInteger> composition) {
        if(Objects.requireNonNull(objects).size()>MAX_ARROWS || Objects.requireNonNull(arrows).size()>MAX_ARROWS)
            throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Finite category validation is limited to 128 arrows and objects");
        this.objects=new FiniteSet<>(new TreeSet<>(objects.members()));
        this.arrows=Collections.unmodifiableMap(new TreeMap<>(arrows));
        this.identities=Collections.unmodifiableMap(new TreeMap<>(Objects.requireNonNull(identities)));
        Objects.requireNonNull(composition);
        for(Pair<BigInteger,BigInteger> endpoints : this.arrows.values()) {
            if(endpoints==null || !objects.contains(endpoints.first) || !objects.contains(endpoints.second))
                throw MathFailure.invalid("Every arrow needs source and target objects in the category");
        }
        if(!objects.members().equals(this.identities.keySet())) throw MathFailure.invalid("Every object needs exactly one designated identity");
        for(BigInteger object : objects.members())
            if(this.identities.get(object)==null || !new Pair<>(object,object).equals(this.arrows.get(this.identities.get(object))))
                throw MathFailure.invalid("An identity must be an endomorphism of its object");
        for(Map.Entry<Pair<BigInteger,BigInteger>,BigInteger> entry : composition.entrySet()) {
            Pair<BigInteger,BigInteger> key=Objects.requireNonNull(entry.getKey());
            Pair<BigInteger,BigInteger> first=this.arrows.get(key.first),second=this.arrows.get(key.second);
            Pair<BigInteger,BigInteger> result=entry.getValue()==null?null:this.arrows.get(entry.getValue());
            if(first==null || second==null || result==null || !first.second.equals(second.first)
                    || !result.equals(new Pair<>(first.first,second.second)))
                throw MathFailure.invalid("Composition table contains an unknown arrow or incompatible endpoints");
        }
        List<BigInteger> labels=new ArrayList<>(this.arrows.keySet());
        Map<BigInteger,Integer> indexes=new HashMap<>();
        for(int i=0;i<labels.size();i++) indexes.put(labels.get(i),i);
        int[][] table=new int[labels.size()][labels.size()];
        Map<Pair<BigInteger,BigInteger>,BigInteger> copy=new LinkedHashMap<>();
        for(int i=0;i<labels.size();i++) for(int j=0;j<labels.size();j++) {
            BigInteger first=labels.get(i),second=labels.get(j); Pair<BigInteger,BigInteger> key=new Pair<>(first,second);
            table[i][j]=-1;
            if(this.arrows.get(first).second.equals(this.arrows.get(second).first)) {
                BigInteger result=composition.get(key);
                if(result==null) throw MathFailure.invalid("Composition table must contain every composable pair");
                copy.put(key,result); table[i][j]=indexes.get(result);
            }
        }
        for(int i=0;i<labels.size();i++) {
            Pair<BigInteger,BigInteger> ends=this.arrows.get(labels.get(i));
            if(table[indexes.get(this.identities.get(ends.first))][i]!=i
                    || table[i][indexes.get(this.identities.get(ends.second))]!=i)
                throw MathFailure.invalid("Composition violates an identity law");
        }
        for(int i=0;i<labels.size();i++) for(int j=0;j<labels.size();j++) if(table[i][j]>=0)
            for(int k=0;k<labels.size();k++) if(table[j][k]>=0)
                if(table[table[i][j]][k]!=table[i][table[j][k]])
                    throw MathFailure.invalid("Composition violates associativity");
        this.composition=Collections.unmodifiableMap(copy);
    }
    public FiniteSet<BigInteger> objects() { return objects; }
    public FiniteSet<BigInteger> arrowLabels() { return new FiniteSet<>(arrows.keySet()); }
    public Map<BigInteger,Pair<BigInteger,BigInteger>> arrows() { return arrows; }
    public Map<BigInteger,BigInteger> identities() { return identities; }
    public Map<Pair<BigInteger,BigInteger>,BigInteger> composition() { return composition; }
    private Pair<BigInteger,BigInteger> arrow(BigInteger label) {
        Pair<BigInteger,BigInteger> result=arrows.get(Objects.requireNonNull(label));
        if(result==null) throw MathFailure.undefined("Unknown arrow label");
        return result;
    }
    private void object(BigInteger label) {
        if(!objects.contains(Objects.requireNonNull(label))) throw MathFailure.undefined("Unknown object label");
    }
    public BigInteger source(BigInteger label) { return arrow(label).first; }
    public BigInteger target(BigInteger label) { return arrow(label).second; }
    public BigInteger identity(BigInteger label) { object(label); return identities.get(label); }
    public BigInteger compose(BigInteger first,BigInteger second) {
        arrow(first); arrow(second);
        BigInteger result=composition.get(new Pair<>(first,second));
        if(result==null) throw MathFailure.undefined("Arrow endpoints do not compose");
        return result;
    }
    public List<BigInteger> hom(BigInteger source,BigInteger target) {
        object(source); object(target); List<BigInteger> result=new ArrayList<>();
        Pair<BigInteger,BigInteger> ends=new Pair<>(source,target);
        for(Map.Entry<BigInteger,Pair<BigInteger,BigInteger>> entry : arrows.entrySet())
            if(ends.equals(entry.getValue())) result.add(entry.getKey());
        return Collections.unmodifiableList(result);
    }
    /** Empty for a non-isomorphism; otherwise the unique two-sided inverse. */
    public List<BigInteger> inverseOf(BigInteger label) {
        Pair<BigInteger,BigInteger> ends=arrow(label);
        for(BigInteger candidate : hom(ends.second,ends.first))
            if(compose(label,candidate).equals(identity(ends.first)) && compose(candidate,label).equals(identity(ends.second)))
                return Collections.singletonList(candidate);
        return Collections.emptyList();
    }
    public boolean isIsomorphism(BigInteger label) { return !inverseOf(label).isEmpty(); }
    public List<BigInteger> isomorphisms() {
        List<BigInteger> result=new ArrayList<>();
        for(BigInteger label : arrows.keySet()) if(isIsomorphism(label)) result.add(label);
        return Collections.unmodifiableList(result);
    }
    public boolean isGroupoid() { return isomorphisms().size()==arrows.size(); }
    public boolean isThin() { return new HashSet<>(arrows.values()).size()==arrows.size(); }
    public List<BigInteger> initialObjects() { return universalObjects(true); }
    public List<BigInteger> terminalObjects() { return universalObjects(false); }
    private List<BigInteger> universalObjects(boolean initial) {
        List<BigInteger> result=new ArrayList<>();
        for(BigInteger candidate : objects.members()) {
            boolean unique=true;
            for(BigInteger other : objects.members())
                if((initial?hom(candidate,other):hom(other,candidate)).size()!=1) { unique=false; break; }
            if(unique) result.add(candidate);
        }
        return Collections.unmodifiableList(result);
    }
    public FiniteCategory opposite() {
        Map<BigInteger,Pair<BigInteger,BigInteger>> reversed=new LinkedHashMap<>();
        for(Map.Entry<BigInteger,Pair<BigInteger,BigInteger>> entry : arrows.entrySet())
            reversed.put(entry.getKey(),new Pair<>(entry.getValue().second,entry.getValue().first));
        Map<Pair<BigInteger,BigInteger>,BigInteger> table=new LinkedHashMap<>();
        for(Map.Entry<Pair<BigInteger,BigInteger>,BigInteger> entry : composition.entrySet())
            table.put(new Pair<>(entry.getKey().second,entry.getKey().first),entry.getValue());
        return new FiniteCategory(objects,reversed,identities,table);
    }
    public static FiniteCategory discrete(FiniteSet<BigInteger> objects) {
        Map<BigInteger,Pair<BigInteger,BigInteger>> arrows=new LinkedHashMap<>();
        Map<BigInteger,BigInteger> identities=new LinkedHashMap<>();
        Map<Pair<BigInteger,BigInteger>,BigInteger> table=new LinkedHashMap<>();
        for(BigInteger object : objects.members()) {
            arrows.put(object,new Pair<>(object,object)); identities.put(object,object); table.put(new Pair<>(object,object),object);
        }
        return new FiniteCategory(objects,arrows,identities,table);
    }
    /** The objects are precisely the relation's support; diagonal pairs must be explicit. */
    public static FiniteCategory fromPreorder(FiniteRelation<BigInteger,BigInteger> relation) {
        if(relation.source!=relation.target) throw MathFailure.undefined("A preorder must use one actual Algebra");
        if(relation.pairs.size()>MAX_ARROWS) throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Preorder category exceeds the 128-arrow limit");
        FiniteSet<BigInteger> objects=relation.domain().union(relation.range());
        for(BigInteger object : objects.members()) if(!relation.pairs.contains(new Pair<>(object,object)))
            throw MathFailure.undefined("Preorder relation must be reflexive on its support");
        List<Pair<BigInteger,BigInteger>> pairs=new ArrayList<>(relation.pairs.members());
        pairs.sort((a,b) -> a.first.equals(b.first)?a.second.compareTo(b.second):a.first.compareTo(b.first));
        Map<Pair<BigInteger,BigInteger>,BigInteger> labels=new HashMap<>();
        Map<BigInteger,Pair<BigInteger,BigInteger>> arrows=new LinkedHashMap<>();
        Map<BigInteger,BigInteger> identities=new LinkedHashMap<>();
        for(int i=0;i<pairs.size();i++) {
            Pair<BigInteger,BigInteger> pair=pairs.get(i); BigInteger label=BigInteger.valueOf(i);
            labels.put(pair,label); arrows.put(label,pair);
            if(pair.first.equals(pair.second)) identities.put(pair.first,label);
        }
        Map<Pair<BigInteger,BigInteger>,BigInteger> table=new LinkedHashMap<>();
        for(Pair<BigInteger,BigInteger> first : pairs) for(Pair<BigInteger,BigInteger> second : pairs)
            if(first.second.equals(second.first)) {
                BigInteger result=labels.get(new Pair<>(first.first,second.second));
                if(result==null) throw MathFailure.undefined("Preorder relation must be transitive");
                table.put(new Pair<>(labels.get(first),labels.get(second)),result);
            }
        return new FiniteCategory(objects,arrows,identities,table);
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof FiniteCategory)) return false;
        FiniteCategory c=(FiniteCategory)other;
        return objects.equals(c.objects) && arrows.equals(c.arrows) && identities.equals(c.identities) && composition.equals(c.composition);
    }
    @Override public int hashCode() { return Objects.hash(objects,arrows,identities,composition); }
    @Override public String toString() { return "Category(objects="+objects+", arrows="+arrows+", identities="+identities+", composition="+composition+")"; }
}
