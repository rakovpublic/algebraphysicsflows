package algebra.concrete;

import algebra.IMathToolInitializer;
import algebra.imp.Algebra;
import algebra.imp.MathTool;
import algebraflow.imp.AlgebraFlow;
import algebraflow.imp.ListAlgebraInput;
import mathematics.foundations.Unit;
import java.util.*;

/** Initializes actual Algebra instances with native operations; also usable as an IMathToolInitializer. */
public final class ConcreteMathematics implements IMathToolInitializer {
    private static final long serialVersionUID=1L;
    public final Algebra<Unit> unit=AlgebraFactories.carrier("Unit",Unit.class,"Singleton for algebra constants",u -> true);
    public final BooleanAlgebra booleans=new BooleanAlgebra(unit);
    public final RationalField rationals=new RationalField(booleans);
    public final IntegerRing integers=new IntegerRing(rationals,booleans);
    public final ResidueRing residues=new ResidueRing(java.math.BigInteger.valueOf(6),integers,booleans);
    public final NaturalSemiring naturals=new NaturalSemiring(integers);
    public final SymmetricGroup permutations=new SymmetricGroup(3,naturals,integers,booleans);
    public final FiniteSimplicialAlgebra complexes=new FiniteSimplicialAlgebra(booleans,naturals,integers);
    public final RationalPolynomialRing polynomials=new RationalPolynomialRing(rationals,naturals);
    public final RationalFunctionField rationalFunctions=new RationalFunctionField(polynomials,rationals,booleans);
    public final IntegerSetAlgebra integerSets=new IntegerSetAlgebra(integers,booleans,naturals,rationals,polynomials);
    public final FiniteIntegerRelationAlgebra integerRelations=new FiniteIntegerRelationAlgebra(integers,integerSets,booleans,naturals);
    public final FiniteIntegerFunctionAlgebra integerFunctions=new FiniteIntegerFunctionAlgebra(integers,integerSets,integerRelations,booleans,naturals);
    public final FiniteCategoryAlgebra categories=new FiniteCategoryAlgebra(integers,integerSets,integerRelations,booleans,naturals);
    public final FiniteFunctorAlgebra functors=new FiniteFunctorAlgebra(categories,integerFunctions,integers,booleans);
    public final FiniteNaturalTransformationAlgebra naturalTransformations=new FiniteNaturalTransformationAlgebra(functors,integerFunctions,integers,booleans);
    public final RationalSampleAlgebra samples=new RationalSampleAlgebra(rationals,naturals);
    public final FiniteProbabilityAlgebra<java.math.BigInteger> integerProbabilities=new FiniteProbabilityAlgebra<>("FiniteDistribution(Z)",integers.algebra(),integerSets,rationals,booleans,naturals,mathematics.numbers.Rational::of);
    public final RationalComplexField complexRationals=new RationalComplexField(rationals);
    public final RationalVectorSpace vectors;
    public final RationalMatrixAlgebra matrices;
    public final List<PrimeField> primeFields;
    public final MathTool mathTool=new MathTool("concrete-mathematics");
    private final List<ConcreteAlgebra<?>> algebras;
    private final Map<String,OperationRegistration> operations=new LinkedHashMap<>();

    public ConcreteMathematics() { this(2,5); }
    public ConcreteMathematics(int dimension,int... primes) {
        vectors=new RationalVectorSpace(rationals,dimension);
        matrices=new RationalMatrixAlgebra(rationals,vectors,naturals);
        List<PrimeField> fields=new ArrayList<>();
        Set<Integer> seen=new HashSet<>();
        for(int prime : primes) {
            if(!seen.add(prime)) throw new IllegalArgumentException("Duplicate prime field: "+prime);
            fields.add(new PrimeField(unit,prime));
        }
        primeFields=Collections.unmodifiableList(fields);
        List<ConcreteAlgebra<?>> values=new ArrayList<>(Arrays.asList(booleans,naturals,integers,rationals,complexRationals,vectors,matrices,polynomials,rationalFunctions,integerSets,samples,integerProbabilities,complexes,integerRelations,permutations,residues,integerFunctions,categories,functors,naturalTransformations));
        values.addAll(fields); algebras=Collections.unmodifiableList(values);
        for(ConcreteAlgebra<?> algebra : algebras) {
            algebra.register(mathTool);
            for(OperationRegistration entry : algebra.operations().values()) {
                if(operations.putIfAbsent(entry.id,entry)!=null) throw new IllegalArgumentException("Duplicate operation id: "+entry.id);
            }
        }
    }
    public MathTool initialize() { return mathTool; }
    public List<ConcreteAlgebra<?>> algebras() { return algebras; }
    /** Descriptive registry only. Execution goes through Algebra and AlgebraFlow. */
    public Map<String,OperationRegistration> operations() { return Collections.unmodifiableMap(operations); }
    public <T> AlgebraFlow<T> flow(ConcreteAlgebra<T> algebra,List<T> input) {
        String name=algebra.algebra().getAlgebraName();
        if(mathTool.getAlgebra(name)!=algebra.algebra()) throw new IllegalArgumentException("The algebra belongs to a different MathTool");
        return new AlgebraFlow<>(new ListAlgebraInput<>(algebra.algebra(),input),this,name);
    }
}
