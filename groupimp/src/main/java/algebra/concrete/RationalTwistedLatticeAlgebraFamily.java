package algebra.concrete;

import mathematics.numbers.*;

/** Registered rank-three finite-support H(Q)-valued twisted lattice algebra. */
public final class RationalTwistedLatticeAlgebraFamily extends ConcreteAlgebra<RationalTwistedLatticeAlgebra> {
    public static final int RANK=3;
    public RationalTwistedLatticeAlgebraFamily(RationalQuaternionAlgebra quaternions,BooleanAlgebra truth,RationalField rationals){
        super(carrier("HMN(H(Q);Z^3,sigma,tau)",RationalTwistedLatticeAlgebra.class,
                "Finite-support skew group algebra over H(Q), lexicographically ordered Z^3, with sigma and a normalized sign 2-cocycle",x -> x.rank()==RANK),rationals.unit());
        closed("add",false,RationalTwistedLatticeAlgebra::add);
        closed("subtract",false,RationalTwistedLatticeAlgebra::subtract);
        closed("multiply",false,RationalTwistedLatticeAlgebra::multiply);
        unary("dark-matter-endomorphism",algebra(),algebra(),false,RationalTwistedLatticeAlgebra::darkMatterEndomorphism);
        unary("has-dark-matter-residue",algebra(),truth.algebra(),false,RationalTwistedLatticeAlgebra::hasOddFirstCoordinateSupport);
        unary("leading-coefficient",algebra(),quaternions.algebra(),true,RationalTwistedLatticeAlgebra::leadingCoefficient);
        unary("to-quaternion",algebra(),quaternions.algebra(),true,RationalTwistedLatticeAlgebra::toQuaternion);
        unary("to-boolean",algebra(),truth.algebra(),true,RationalTwistedLatticeAlgebra::toBoolean);
        binary("equal",algebra(),algebra(),truth.algebra(),false,RationalTwistedLatticeAlgebra::equals);
        unary("embed-quaternion",quaternions.algebra(),algebra(),false,q -> RationalTwistedLatticeAlgebra.fromQuaternion(RANK,q));
        unary("basis-e1",rationals.algebra(),algebra(),false,q -> RationalTwistedLatticeAlgebra.basis(RANK,new RationalTwistedLatticeAlgebra.Exponent(1,0,0),RationalQuaternion.scalar(q)));
        unary("basis-e2",rationals.algebra(),algebra(),false,q -> RationalTwistedLatticeAlgebra.basis(RANK,new RationalTwistedLatticeAlgebra.Exponent(0,1,0),RationalQuaternion.scalar(q)));
        unary("basis-e3",rationals.algebra(),algebra(),false,q -> RationalTwistedLatticeAlgebra.basis(RANK,new RationalTwistedLatticeAlgebra.Exponent(0,0,1),RationalQuaternion.scalar(q)));
        constant("zero",RationalTwistedLatticeAlgebra.zero(RANK));
        law("The carrier is finite-support, not the full Hahn completion. Lexicographic Z^3 is ordered and support finite, so valuation and leading coefficient are defined for every nonzero member.");
        law("sigma_g=Ad(i)^(g_1); tau(g,h)=(-1)^(g_1*h_2). The normalized bilinear sign cocycle makes the basis rule associative.");
        law("The BSM1 map g_0 -> 2g_0 is a non-surjective group endomorphism and induces an algebra endomorphism because sigma and tau are invariant under it. Odd g_0 support is the residue sector sent into the even subgroup.");
        law("H(Q) embeds at the identity exponent. Projection to H(Q) is partial and defined only for identity-supported elements; Boolean conversion is defined only at 0 and 1.");
    }
}
