package mathematics.algebras;

import algebra.imp.MathTool;
import algebraflow.imp.AlgebraFlow;
import mathematics.adapters.DomainInput;
import mathematics.core.*;
import mathematics.foundations.Unit;
import java.util.*;
import static mathematics.core.MathStatus.Membership.MEMBER;

/** Ready-to-use, isolated algebras connected to the existing MathTool and AlgebraFlow APIs. */
public final class ConcreteMathematics {
    public final Domain<Unit> unit=new Domain<>(Metadata.of("Unit","Singleton for algebra constants"),Unit.class,u -> MEMBER);
    public final BooleanAlgebra booleans=new BooleanAlgebra(unit);
    public final RationalField rationals=new RationalField(booleans);
    public final IntegerRing integers=new IntegerRing(rationals,booleans);
    public final NaturalSemiring naturals=new NaturalSemiring(integers);
    public final RationalComplexField complexRationals=new RationalComplexField(rationals);
    public final RationalVectorSpace vectors;
    public final RationalMatrixAlgebra matrices;
    public final RationalPolynomialRing polynomials=new RationalPolynomialRing(rationals);
    public final List<PrimeField> primeFields;
    public final OperationCatalog catalog=new OperationCatalog();
    public final MathTool mathTool=new MathTool("concrete-mathematics");
    private final List<ConcreteAlgebra<?>> algebras;

    public ConcreteMathematics() { this(2,5); }
    public ConcreteMathematics(int dimension,int... primes) {
        vectors=new RationalVectorSpace(rationals,dimension);
        matrices=new RationalMatrixAlgebra(rationals,vectors);
        List<PrimeField> fields=new ArrayList<>();
        Set<Integer> seen=new HashSet<>();
        for(int prime : primes) {
            if(!seen.add(prime)) throw new IllegalArgumentException("Duplicate prime field: "+prime);
            fields.add(new PrimeField(unit,prime));
        }
        primeFields=Collections.unmodifiableList(fields);
        List<ConcreteAlgebra<?>> values=new ArrayList<>(Arrays.asList(booleans,naturals,integers,rationals,complexRationals,vectors,matrices,polynomials));
        values.addAll(fields); algebras=Collections.unmodifiableList(values);
        for(ConcreteAlgebra<?> algebra : algebras) for(Domain<?> carrier : algebra.carriers().values()) catalog.addDomain(carrier);
        for(ConcreteAlgebra<?> algebra : algebras) {
            for(DescribedOperation operation : algebra.operations().values()) catalog.addOperation(operation);
            algebra.register(mathTool);
        }
    }
    public List<ConcreteAlgebra<?>> algebras() { return algebras; }
    public <T> AlgebraFlow<T> flow(ConcreteAlgebra<T> algebra,List<T> input) {
        if(mathTool.getAlgebra(algebra.domain().metadata().id)!=algebra.algebra())
            throw new IllegalArgumentException("The algebra belongs to a different MathTool");
        return new AlgebraFlow<>(new DomainInput<>(algebra.domain(),input),() -> mathTool,algebra.domain().metadata().id);
    }
}
