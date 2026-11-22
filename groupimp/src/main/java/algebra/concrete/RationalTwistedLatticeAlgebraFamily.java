package algebra.concrete;

import mathematics.numbers.*;
import mathematics.linear.IntegerVector;

/** Registered arbitrary-rank finite-support H(Q)-valued twisted lattice algebra. */
public final class RationalTwistedLatticeAlgebraFamily extends ConcreteAlgebra<RationalTwistedLatticeAlgebra> {
    public static final int RANK=3;
    private final int defaultRank;
    public int defaultRank(){return defaultRank;}
    public RationalTwistedLatticeAlgebraFamily(RationalQuaternionAlgebra quaternions,BooleanAlgebra truth,RationalField rationals){
        this(RANK,quaternions,truth,rationals,null,null,null,null,null,null,null);
    }
    public RationalTwistedLatticeAlgebraFamily(int defaultRank,RationalQuaternionAlgebra quaternions,BooleanAlgebra truth,RationalField rationals,
            RationalComplexField complex,RationalImaginaryAlgebra imaginaries,RationalOctonionAlgebra octonions,
            RationalDixonZ2Algebra dixon,RationalTwistedHahnMalcevNeumannAlgebra rankOne,
            RationalCyclotomicAlgebra cyclotomics,algebra.concrete.IntegerVectorFamily integerVectors){
        super(carrier("HMN(H(Q);Z^N,sigma,tau)",RationalTwistedLatticeAlgebra.class,
                "Finite-support skew group algebra over H(Q), runtime rank N on lexicographic Z^N, with a normalized alternating sign cocycle",x -> x.rank()>0),rationals.unit());
        if(defaultRank<1)throw new IllegalArgumentException("Default lattice rank must be positive");this.defaultRank=defaultRank;
        closed("add",true,RationalTwistedLatticeAlgebra::add);
        closed("subtract",true,RationalTwistedLatticeAlgebra::subtract);
        closed("multiply",true,RationalTwistedLatticeAlgebra::multiply);
        unary("dark-matter-endomorphism",algebra(),algebra(),true,RationalTwistedLatticeAlgebra::darkMatterEndomorphism);
        unary("has-dark-matter-residue",algebra(),truth.algebra(),false,RationalTwistedLatticeAlgebra::hasOddFirstCoordinateSupport);
        unary("leading-coefficient",algebra(),quaternions.algebra(),true,RationalTwistedLatticeAlgebra::leadingCoefficient);
        unary("to-quaternion",algebra(),quaternions.algebra(),true,RationalTwistedLatticeAlgebra::toQuaternion);
        unary("to-boolean",algebra(),truth.algebra(),true,RationalTwistedLatticeAlgebra::toBoolean);
        binary("equal",algebra(),algebra(),truth.algebra(),false,RationalTwistedLatticeAlgebra::equals);
        unary("embed-quaternion",quaternions.algebra(),algebra(),false,q -> RationalTwistedLatticeAlgebra.fromQuaternion(defaultRank,q));
        if(integerVectors!=null)unary("group-basis",integerVectors.algebra(),algebra(),true,RationalTwistedLatticeAlgebra::groupBasis);
        if(complex!=null){
            unary("embed-complex",complex.algebra(),algebra(),false,z -> RationalTwistedLatticeAlgebra.fromQuaternion(defaultRank,RationalQuaternion.fromComplex(z)));
            unary("to-complex",algebra(),complex.algebra(),true,x -> x.toQuaternion().toComplex());
        }
        if(imaginaries!=null){
            unary("embed-imaginary",imaginaries.algebra(),algebra(),false,x -> RationalTwistedLatticeAlgebra.fromQuaternion(defaultRank,x.toQuaternion()));
            unary("to-imaginary",algebra(),imaginaries.algebra(),true,x -> RationalImaginary.fromQuaternion(x.toQuaternion()));
        }
        if(octonions!=null){
            unary("embed-octonion-quaternion-slice",octonions.algebra(),algebra(),true,x -> RationalTwistedLatticeAlgebra.fromQuaternion(defaultRank,x.toQuaternion()));
            unary("to-octonion",algebra(),octonions.algebra(),true,x -> RationalOctonion.fromQuaternion(x.toQuaternion()));
        }
        if(dixon!=null){
            unary("embed-dixon-quaternion-slice",dixon.algebra(),algebra(),true,x -> RationalTwistedLatticeAlgebra.fromQuaternion(defaultRank,x.toQuaternion()));
            unary("to-dixon-quaternion-slice",algebra(),dixon.algebra(),true,x -> RationalDixonZ2.fromQuaternion(x.toQuaternion()));
        }
        if(rankOne!=null){
            unary("embed-rank-one-hmn-constant",rankOne.algebra(),algebra(),true,x -> RationalTwistedLatticeAlgebra.fromQuaternion(defaultRank,x.toQuaternion()));
            unary("to-rank-one-hmn-constant",algebra(),rankOne.algebra(),true,x -> RationalTwistedHahnSeries.fromQuaternion(x.toQuaternion()));
        }
        if(cyclotomics!=null){
            unary("embed-cyclotomic-qi-slice",cyclotomics.algebra(),algebra(),true,x -> RationalTwistedLatticeAlgebra.fromQuaternion(defaultRank,RationalQuaternion.fromComplex(x.toComplex())));
            unary("to-cyclotomic-qi-slice",algebra(),cyclotomics.algebra(),true,x -> RationalCyclotomic.fromComplex(RationalCyclotomicAlgebra.ORDER,x.toQuaternion().toComplex()));
        }
        unary("from-boolean",truth.algebra(),algebra(),false,x -> RationalTwistedLatticeAlgebra.fromQuaternion(defaultRank,x?RationalQuaternion.ONE:RationalQuaternion.ZERO));
        constant("zero",RationalTwistedLatticeAlgebra.zero(defaultRank));
        law("The carrier accepts every positive runtime rank N and finite support in lexicographically ordered Z^N; the registered zero and scalar embeddings use default rank "+defaultRank+". Arbitrary support configurations are created from IntegerVector group exponents or the public rank-and-cocycle constructors.");
        law("A runtime N by N alternating matrix over F_2 defines tau(g,h)=(-1)^(sum_{i<j} S_ij*g_i*h_j). Its commutator bicharacter is (-1)^(g^T*S*h), and the default rank-three matrix has S_12=1.");
        law("The BSM1 map doubles the first vector coordinate g[0]. It is an algebra endomorphism only when the sign cocycle is trivial on coordinate 0, which is checked at runtime. Odd g[0] support is the residue sector sent into the even subgroup; sigma_g=Ad(i)^(g[1]) uses the second coordinate.");
        law("H(Q) embeds at the identity exponent. Projections to Q, Q(i), iQ, O(Q), DixonZ2(Q), rank-one HMN, and Q(zeta_12) are partial and pass through the constant H(Q) or Q(i) slices; no full algebra embedding is asserted. Boolean conversion is defined only at 0 and 1.");
    }
}
