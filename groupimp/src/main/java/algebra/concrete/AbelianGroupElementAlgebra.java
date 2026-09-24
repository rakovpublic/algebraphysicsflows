package algebra.concrete;

import mathematics.structures.AbelianGroupElement;
import mathematics.structures.PresentedAbelianGroup;
import java.math.BigInteger;

/** A family of additive abelian groups whose elements retain the actual finite presentation. */
public final class AbelianGroupElementAlgebra extends ConcreteAlgebra<AbelianGroupElement> {
    public AbelianGroupElementAlgebra(PresentedAbelianGroupAlgebra groups,IntegerVectorFamily vectors,IntegerRing integers,
                                     NaturalSemiring naturals,BooleanAlgebra truth) {
        super(carrier("AbelianGroupElement",AbelianGroupElement.class,"Elements of retained finitely presented abelian groups",e -> true),integers.unit());
        closed("add",true,AbelianGroupElement::add);
        closed("subtract",true,(a,b) -> a.add(b.scale(BigInteger.ONE.negate())));
        unary("negate",algebra(),algebra(),false,e -> e.scale(BigInteger.ONE.negate()));
        binary("scale",algebra(),integers.algebra(),algebra(),false,AbelianGroupElement::scale);
        binary("equal",algebra(),algebra(),truth.algebra(),false,AbelianGroupElement::equals);
        unary("group",algebra(),groups.algebra(),false,AbelianGroupElement::group);
        unary("smith-coordinates",algebra(),vectors.algebra(),false,AbelianGroupElement::smithCoordinates);
        unary("representative",algebra(),vectors.algebra(),false,AbelianGroupElement::representative);
        unary("is-zero",algebra(),truth.algebra(),false,AbelianGroupElement::isZero);
        unary("is-torsion",algebra(),truth.algebra(),false,AbelianGroupElement::isTorsion);
        unary("order",algebra(),naturals.algebra(),true,AbelianGroupElement::order);
        unary("zero-like",algebra(),algebra(),false,e -> e.group().zero());
        unaryFlat("cyclic-subgroup",algebra(),algebra(),true,AbelianGroupElement::cyclicSubgroup);
        flat("multiplication-preimages",algebra(),integers.algebra(),algebra(),true,AbelianGroupElement::multiplicationPreimages);
        binary("project",groups.algebra(),vectors.algebra(),algebra(),true,PresentedAbelianGroup::project);
        binary("from-smith",groups.algebra(),vectors.algebra(),algebra(),true,PresentedAbelianGroup::fromSmith);
        unary("zero",groups.algebra(),algebra(),false,PresentedAbelianGroup::zero);
        unaryFlat("generators",groups.algebra(),algebra(),false,PresentedAbelianGroup::generators);
        unaryFlat("smith-generators",groups.algebra(),algebra(),false,PresentedAbelianGroup::smithGenerators);
        unaryFlat("elements",groups.algebra(),algebra(),true,PresentedAbelianGroup::elements);
        binary("reduce",groups.algebra(),vectors.algebra(),vectors.algebra(),true,PresentedAbelianGroup::reduce);
        law("Elements use canonical residues in finite Smith coordinates and arbitrary integers in free coordinates; killed coordinates are zero.");
        law("Addition requires the same presentation and coordinate map; isomorphic presentations do not implicitly identify their elements.");
        law("Projection kills all relation columns and preserves addition; representative returns one vector in the original generator coordinates.");
        law("Multiplication-preimages emits the full finite fiber of integer scaling; no solutions give an empty list and infinite fibers are outside the operation.");
    }
}
