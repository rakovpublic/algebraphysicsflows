package algebra.concrete;

import mathematics.linear.IntegerMatrix;
import mathematics.structures.PresentedAbelianGroup;
import java.math.BigInteger;

/** Presentations and their structure, installed in the original Algebra registry. */
public final class PresentedAbelianGroupAlgebra extends ConcreteAlgebra<PresentedAbelianGroup> {
    public PresentedAbelianGroupAlgebra(IntegerMatrixFamily matrices,AbelianGroupTypeAlgebra types,NaturalSemiring naturals,BooleanAlgebra truth) {
        super(carrier("PresentedAbelianGroup",PresentedAbelianGroup.class,"Presented quotients of finite free integer modules",g -> true),naturals.unit());
        unary("from-matrix",matrices.algebra(),algebra(),false,PresentedAbelianGroup::new);
        unary("from-type",types.algebra(),algebra(),false,PresentedAbelianGroup::fromType);
        unary("relation-matrix",algebra(),matrices.algebra(),false,PresentedAbelianGroup::relations);
        unary("as-type",algebra(),types.algebra(),false,PresentedAbelianGroup::type);
        unary("generator-count",algebra(),naturals.algebra(),false,g -> BigInteger.valueOf(g.generatorCount()));
        unary("relation-count",algebra(),naturals.algebra(),false,g -> BigInteger.valueOf(g.relationCount()));
        unary("is-finite",algebra(),truth.algebra(),false,PresentedAbelianGroup::isFinite);
        unary("order",algebra(),naturals.algebra(),true,PresentedAbelianGroup::order);
        binary("equal",algebra(),algebra(),truth.algebra(),false,PresentedAbelianGroup::equals);
        binary("isomorphic",algebra(),algebra(),truth.algebra(),false,(a,b) -> a.type().equals(b.type()));
        closed("direct-sum",false,PresentedAbelianGroup::directSum);
        constant("trivial",new PresentedAbelianGroup(IntegerMatrix.zero(0,0)));
        law("Relations are matrix columns; the presented group is Z^rows modulo their image.");
        law("Presentation equality includes the relation matrix and stored Smith-coordinate map; isomorphism compares only the classified group types.");
        law("Direct sum retains the first presentation's generators and relations before the second in a block-diagonal matrix.");
    }
}
