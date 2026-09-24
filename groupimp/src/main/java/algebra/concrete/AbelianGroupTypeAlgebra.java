package algebra.concrete;

import mathematics.structures.AbelianGroupType;
import java.math.BigInteger;

/** Native arithmetic on canonical finitely generated abelian-group isomorphism types. */
public final class AbelianGroupTypeAlgebra extends ConcreteAlgebra<AbelianGroupType> {
    public AbelianGroupTypeAlgebra(NaturalSemiring naturals,BooleanAlgebra truth) {
        super(carrier("AbelianGroupType",AbelianGroupType.class,"Finitely generated abelian groups up to isomorphism",g -> true),naturals.unit());
        closed("direct-sum",false,AbelianGroupType::directSum);
        closed("tensor-product",false,AbelianGroupType::tensorProduct);
        closed("hom-group",false,AbelianGroupType::homGroup);
        closed("tor1",false,AbelianGroupType::tor1);
        closed("ext1",false,AbelianGroupType::ext1);
        binary("equal",algebra(),algebra(),truth.algebra(),false,AbelianGroupType::equals);
        unary("free-rank",algebra(),naturals.algebra(),false,AbelianGroupType::freeRank);
        unary("torsion-factor-count",algebra(),naturals.algebra(),false,g -> BigInteger.valueOf(g.invariantFactors().size()));
        unary("minimal-generators",algebra(),naturals.algebra(),false,AbelianGroupType::minimalGenerators);
        unary("is-finite",algebra(),truth.algebra(),false,AbelianGroupType::isFinite);
        unary("is-torsion-free",algebra(),truth.algebra(),false,AbelianGroupType::isTorsionFree);
        unary("is-cyclic",algebra(),truth.algebra(),false,AbelianGroupType::isCyclic);
        unary("is-trivial",algebra(),truth.algebra(),false,AbelianGroupType::isTrivial);
        unary("order",algebra(),naturals.algebra(),true,AbelianGroupType::order);
        unary("exponent",algebra(),naturals.algebra(),true,AbelianGroupType::exponent);
        unary("torsion-part",algebra(),algebra(),false,AbelianGroupType::torsionPart);
        unary("free-part",algebra(),algebra(),false,AbelianGroupType::freePart);
        unaryFlat("invariant-factors",algebra(),naturals.algebra(),false,AbelianGroupType::invariantFactors);
        binary("repeat",algebra(),naturals.algebra(),algebra(),false,AbelianGroupType::repeat);
        unary("free-on",naturals.algebra(),algebra(),false,AbelianGroupType::free);
        unary("cyclic",naturals.algebra(),algebra(),false,AbelianGroupType::cyclic);
        constant("zero",AbelianGroupType.ZERO); constant("one",AbelianGroupType.Z);
        law("Isomorphism types form a commutative semiring under direct sum and tensor product, with trivial group zero and Z one.");
        law("Invariant factors are greater than one and each divides the next; free rank is separate and exact.");
        law("Hom and Ext^1 take the source first and target second. Results classify groups, without chosen generators, maps or extension witnesses.");
    }
}
