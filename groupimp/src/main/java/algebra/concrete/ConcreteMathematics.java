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
    public final AbelianGroupTypeAlgebra abelianGroups=new AbelianGroupTypeAlgebra(naturals,booleans);
    public final FiniteSimplicialAlgebra complexes;
    public final RationalPolynomialRing polynomials=new RationalPolynomialRing(rationals,naturals);
    public final RationalFunctionField rationalFunctions=new RationalFunctionField(polynomials,rationals,booleans);
    public final IntegerSetAlgebra integerSets=new IntegerSetAlgebra(integers,booleans,naturals,rationals,polynomials);
    public final FiniteIntegerRelationAlgebra integerRelations=new FiniteIntegerRelationAlgebra(integers,integerSets,booleans,naturals);
    public final FiniteIntegerFunctionAlgebra integerFunctions=new FiniteIntegerFunctionAlgebra(integers,integerSets,integerRelations,booleans,naturals);
    public final FiniteCategoryAlgebra categories=new FiniteCategoryAlgebra(integers,integerSets,integerRelations,booleans,naturals);
    public final FiniteFunctorAlgebra functors=new FiniteFunctorAlgebra(categories,integerFunctions,integers,booleans);
    public final FiniteNaturalTransformationAlgebra naturalTransformations=new FiniteNaturalTransformationAlgebra(functors,integerFunctions,integers,booleans);
    public final FiniteEquivalenceAlgebra equivalences=new FiniteEquivalenceAlgebra(categories,functors,naturalTransformations,booleans);
    public final FiniteAdjunctionAlgebra adjunctions=new FiniteAdjunctionAlgebra(categories,functors,naturalTransformations,equivalences,integerFunctions,integers,booleans);
    public final FiniteConeAlgebra cones=new FiniteConeAlgebra(functors,naturalTransformations,integerFunctions,integers,booleans);
    public final FiniteCoconeAlgebra cocones=new FiniteCoconeAlgebra(cones,functors,naturalTransformations,integerFunctions,integers,booleans);
    public final RationalSampleAlgebra samples=new RationalSampleAlgebra(rationals,naturals);
    public final FiniteProbabilityAlgebra<java.math.BigInteger> integerProbabilities=new FiniteProbabilityAlgebra<>("FiniteDistribution(Z)",integers.algebra(),integerSets,rationals,booleans,naturals,mathematics.numbers.Rational::of);
    public final RationalComplexField complexRationals=new RationalComplexField(rationals);
    public final RationalVectorSpace vectors;
    public final RationalMatrixAlgebra matrices;
    public final RationalVectorFamily finiteVectors;
    public final RationalMatrixFamily rectangularMatrices;
    public final IntegerVectorFamily integerVectors;
    public final IntegerMatrixFamily integerMatrices;
    public final PresentedAbelianGroupAlgebra presentedAbelianGroups;
    public final AbelianGroupElementAlgebra abelianGroupElements;
    public final AbelianGroupHomomorphismAlgebra abelianHomomorphisms;
    public final IntegralHomologyAlgebra integralHomology;
    public final FiniteSimplicialMapAlgebra simplicialMaps;
    public final RelativeSimplicialAlgebra relativeComplexes;
    public final RelativeSimplicialMapAlgebra relativeMaps;
    public final SimplicialCoverAlgebra simplicialCovers;
    public final SimplicialCoverMapAlgebra coverMaps;
    public final SimplicialCochainAlgebra cochains;
    public final RelativeSimplicialCochainAlgebra relativeCochains;
    public final RationalAffineSpaceAlgebra affineSpaces;
    public final FiniteMarkovAlgebra markovKernels;
    public final RationalTensorAlgebra tensors;
    public final RationalExteriorAlgebra exterior;
    public final RationalQuaternionAlgebra quaternions;
    public final RationalMultivariatePolynomialAlgebra multivariatePolynomials;
    public final RationalPolynomialMapAlgebra polynomialMaps;
    public final PolynomialDifferentialFormAlgebra polynomialForms;
    public final PolynomialCellAlgebra polynomialCells;
    public final PolynomialChainAlgebra polynomialChains;
    public final List<PrimeField> primeFields;
    public final MathTool mathTool=new MathTool("concrete-mathematics");
    private final List<ConcreteAlgebra<?>> algebras;
    private final Map<String,OperationRegistration> operations=new LinkedHashMap<>();

    public ConcreteMathematics() { this(2,5); }
    public ConcreteMathematics(int dimension,int... primes) {
        vectors=new RationalVectorSpace(rationals,dimension);
        matrices=new RationalMatrixAlgebra(rationals,vectors,naturals,polynomials,booleans);
        finiteVectors=new RationalVectorFamily(rationals,naturals,vectors);
        rectangularMatrices=new RationalMatrixFamily(rationals,finiteVectors,naturals,booleans,matrices,polynomials);
        integerVectors=new IntegerVectorFamily(integers,naturals,booleans,finiteVectors);
        integerMatrices=new IntegerMatrixFamily(integers,integerVectors,naturals,booleans,rectangularMatrices,abelianGroups);
        presentedAbelianGroups=new PresentedAbelianGroupAlgebra(integerMatrices,abelianGroups,naturals,booleans);
        abelianGroupElements=new AbelianGroupElementAlgebra(presentedAbelianGroups,integerVectors,integers,naturals,booleans);
        abelianHomomorphisms=new AbelianGroupHomomorphismAlgebra(presentedAbelianGroups,abelianGroupElements,integerMatrices,integers,booleans);
        complexes=new FiniteSimplicialAlgebra(booleans,naturals,integers,abelianGroups,integerMatrices);
        integralHomology=new IntegralHomologyAlgebra(complexes,integerMatrices,integerVectors,presentedAbelianGroups,abelianGroups,abelianGroupElements,abelianHomomorphisms,naturals,booleans);
        simplicialMaps=new FiniteSimplicialMapAlgebra(complexes,integerFunctions,integerSets,integers,naturals,booleans,integerMatrices,integralHomology,abelianHomomorphisms);
        relativeComplexes=new RelativeSimplicialAlgebra(complexes,integerSets,integers,naturals,booleans,integerMatrices,integralHomology,abelianGroups,abelianHomomorphisms,simplicialMaps);
        relativeMaps=new RelativeSimplicialMapAlgebra(relativeComplexes,simplicialMaps,integers,naturals,booleans,integerMatrices,integralHomology,abelianHomomorphisms);
        simplicialCovers=new SimplicialCoverAlgebra(complexes,integers,naturals,booleans,integerMatrices,integralHomology,abelianHomomorphisms,relativeMaps);
        coverMaps=new SimplicialCoverMapAlgebra(simplicialCovers,simplicialMaps,relativeMaps,integers,naturals,booleans,integerMatrices,integralHomology,abelianHomomorphisms);
        cochains=new SimplicialCochainAlgebra(complexes,simplicialMaps,integerVectors,integers,naturals,booleans,integralHomology,abelianGroupElements,abelianHomomorphisms);
        relativeCochains=new RelativeSimplicialCochainAlgebra(relativeComplexes,relativeMaps,cochains,integerVectors,integerMatrices,integers,naturals,booleans,integralHomology,abelianGroupElements,abelianHomomorphisms);
        affineSpaces=new RationalAffineSpaceAlgebra(rectangularMatrices,finiteVectors,naturals,booleans);
        markovKernels=new FiniteMarkovAlgebra(integers,integerSets,integerProbabilities,integerFunctions,rectangularMatrices,finiteVectors,rationals,naturals,booleans);
        tensors=new RationalTensorAlgebra(rationals,finiteVectors,rectangularMatrices,naturals,booleans);
        exterior=new RationalExteriorAlgebra(rationals,finiteVectors,rectangularMatrices,naturals,booleans);
        quaternions=new RationalQuaternionAlgebra(rationals,complexRationals,finiteVectors,rectangularMatrices,booleans);
        multivariatePolynomials=new RationalMultivariatePolynomialAlgebra(rationals,naturals,integers,booleans,finiteVectors,rectangularMatrices,polynomials);
        polynomialMaps=new RationalPolynomialMapAlgebra(multivariatePolynomials,rationals,naturals,booleans,finiteVectors,rectangularMatrices);
        polynomialForms=new PolynomialDifferentialFormAlgebra(multivariatePolynomials,polynomialMaps,exterior,rationals,finiteVectors,naturals,booleans);
        polynomialCells=new PolynomialCellAlgebra(polynomialMaps,polynomialForms,finiteVectors,rationals,naturals,booleans);
        polynomialChains=new PolynomialChainAlgebra(polynomialCells,polynomialMaps,polynomialForms,rationals,naturals,integers,booleans);
        List<PrimeField> fields=new ArrayList<>();
        Set<Integer> seen=new HashSet<>();
        for(int prime : primes) {
            if(!seen.add(prime)) throw new IllegalArgumentException("Duplicate prime field: "+prime);
            fields.add(new PrimeField(unit,prime));
        }
        primeFields=Collections.unmodifiableList(fields);
        List<ConcreteAlgebra<?>> values=new ArrayList<>(Arrays.asList(booleans,naturals,integers,rationals,complexRationals,vectors,matrices,finiteVectors,rectangularMatrices,affineSpaces,tensors,exterior,quaternions,polynomials,rationalFunctions,integerSets,samples,integerProbabilities,complexes,integerRelations,permutations,residues,integerFunctions,categories,functors,naturalTransformations,equivalences,adjunctions,cones,cocones));
        values.add(multivariatePolynomials); values.add(polynomialMaps);
        values.add(polynomialForms);
        values.add(polynomialCells); values.add(polynomialChains);
        values.add(markovKernels);
        values.add(abelianGroups);
        values.add(integerVectors); values.add(integerMatrices);
        values.add(presentedAbelianGroups); values.add(abelianGroupElements);
        values.add(abelianHomomorphisms);
        values.add(integralHomology);
        values.add(simplicialMaps);
        values.add(relativeComplexes);
        values.add(relativeMaps);
        values.add(simplicialCovers);
        values.add(coverMaps);
        values.add(cochains);
        values.add(relativeCochains);
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
