package operations;

import algebra.concrete.ConcreteMathematics;
import mathematics.foundations.*;
import mathematics.structures.FiniteCategory;
import java.math.BigInteger;
import java.util.*;

/** Small independently specified categories shared by native functor/coherence tests. */
final class FiniteCategoryFixtures {
    private FiniteCategoryFixtures() { }
    static BigInteger n(int value) { return BigInteger.valueOf(value); }
    static Map<BigInteger,BigInteger> map(int... entries) {
        if(entries.length%2!=0) throw new IllegalArgumentException("Expected key/value pairs");
        Map<BigInteger,BigInteger> result=new LinkedHashMap<>();
        for(int i=0;i<entries.length;i+=2) result.put(n(entries[i]),n(entries[i+1]));
        return result;
    }
    static FiniteSet<BigInteger> set(int... values) {
        List<BigInteger> result=new ArrayList<>(); for(int value : values) result.add(n(value));
        return new FiniteSet<>(result);
    }
    static FiniteCategory discrete(int... values) { return FiniteCategory.discrete(set(values)); }
    static FiniteCategory cyclic(int size) {
        int[][] table=new int[size][size];
        for(int i=0;i<size;i++) for(int j=0;j<size;j++) table[i][j]=(i+j)%size;
        return monoid(table,0);
    }
    static FiniteCategory monoid(int[][] table,int identity) {
        Map<BigInteger,Pair<BigInteger,BigInteger>> arrows=new LinkedHashMap<>();
        Map<Pair<BigInteger,BigInteger>,BigInteger> composition=new LinkedHashMap<>();
        for(int i=0;i<table.length;i++) {
            arrows.put(n(i),new Pair<>(n(0),n(0)));
            for(int j=0;j<table.length;j++) composition.put(new Pair<>(n(i),n(j)),n(table[i][j]));
        }
        return new FiniteCategory(set(0),arrows,map(0,identity),composition);
    }
    static FiniteCategory relationCategory(ConcreteMathematics m,boolean indiscrete,int... objects) {
        List<Pair<BigInteger,BigInteger>> pairs=new ArrayList<>();
        for(int i=0;i<objects.length;i++) for(int j=0;j<objects.length;j++)
            if(indiscrete || i<=j) pairs.add(new Pair<>(n(objects[i]),n(objects[j])));
        return FiniteCategory.fromPreorder(new FiniteRelation<>(m.integers.algebra(),m.integers.algebra(),new FiniteSet<>(pairs)));
    }
}
