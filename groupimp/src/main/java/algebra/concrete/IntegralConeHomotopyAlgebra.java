package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.topology.IntegralConeHomotopy;
import mathematics.topology.IntegralConeHomotopySolver;
import mathematics.topology.IntegralConeInverseSolver;

/** Validated cone homotopies using the original scalar, unary and flat operation contracts. */
public final class IntegralConeHomotopyAlgebra extends ConcreteAlgebra<IntegralConeHomotopy> {
    public final Algebra<IntegralConeHomotopy.Data> inputs;
    public IntegralConeHomotopyAlgebra(IntegralChainConeMapAlgebra maps,IntegralChainMappingConeAlgebra cones,
            IntegralConeChainAlgebra chains,IntegralConeCochainAlgebra cochains,IntegerMatrixFamily matrices,
            IntegerRing integers,NaturalSemiring naturals,BooleanAlgebra truth,AbelianGroupHomomorphismAlgebra homomorphisms) {
        super(carrier("ConeHomotopy",IntegralConeHomotopy.class,"Retained integral witnesses D K + K D = to - from between exact cone maps",h -> true),cones.unit());
        inputs=carrier("ConeHomotopy.data",IntegralConeHomotopy.Data.class,"Two cone maps and immutable degree-raising matrices awaiting validation",d -> true);
        unary("from-data",inputs,algebra(),true,IntegralConeHomotopy::new);
        unary("stationary",maps.algebra(),algebra(),false,IntegralConeHomotopy::stationary);
        unary("contract-isomorphism",cones.algebra(),algebra(),true,IntegralConeHomotopy::contractIsomorphism);
        unary("from",algebra(),maps.algebra(),false,IntegralConeHomotopy::from);
        unary("to",algebra(),maps.algebra(),false,IntegralConeHomotopy::to);
        unary("source",algebra(),cones.algebra(),false,IntegralConeHomotopy::source);
        unary("target",algebra(),cones.algebra(),false,IntegralConeHomotopy::target);
        unary("data",algebra(),inputs,false,IntegralConeHomotopy::data);
        unary("reverse",algebra(),algebra(),false,IntegralConeHomotopy::reverse);
        closed("then",true,IntegralConeHomotopy::then);
        closed("add",true,IntegralConeHomotopy::add);
        binary("scale",algebra(),integers.algebra(),algebra(),false,IntegralConeHomotopy::scale);
        binary("precompose",algebra(),maps.algebra(),algebra(),true,IntegralConeHomotopy::precompose);
        binary("postcompose",algebra(),maps.algebra(),algebra(),true,IntegralConeHomotopy::postcompose);
        binary("equal",algebra(),algebra(),truth.algebra(),false,IntegralConeHomotopy::equals);
        binary("chain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,IntegralConeHomotopy::chainMatrix);
        binary("cochain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,IntegralConeHomotopy::cochainMatrix);
        unaryFlat("chain-matrices",algebra(),matrices.algebra(),false,IntegralConeHomotopy::chainMatrices);
        unaryFlat("cochain-matrices",algebra(),matrices.algebra(),false,IntegralConeHomotopy::cochainMatrices);
        binary("on-chain",algebra(),chains.algebra(),chains.algebra(),true,IntegralConeHomotopy::onChain);
        binary("on-cochain",algebra(),cochains.algebra(),cochains.algebra(),true,IntegralConeHomotopy::onCochain);
        flat("homology-maps",algebra(),naturals.algebra(),homomorphisms.algebra(),false,IntegralConeHomotopy::homologyMaps);
        flat("cohomology-maps",algebra(),naturals.algebra(),homomorphisms.algebra(),false,IntegralConeHomotopy::cohomologyMaps);
        binary("are-homotopic",maps.algebra(),maps.algebra(),truth.algebra(),true,IntegralConeHomotopySolver::areHomotopic);
        binary("between",maps.algebra(),maps.algebra(),algebra(),true,IntegralConeHomotopySolver::between);
        flat("solution-generators",maps.algebra(),maps.algebra(),algebra(),true,IntegralConeHomotopySolver::solutionGenerators);
        unary("is-contractible",cones.algebra(),truth.algebra(),false,IntegralConeHomotopySolver::isContractible);
        unary("contract",cones.algebra(),algebra(),true,IntegralConeHomotopySolver::contract);
        unaryFlat("inverse-homotopies",maps.algebra(),algebra(),true,IntegralConeInverseSolver::inverseHomotopies);
        law("Bounded integral Smith solving couples every witness degree. A solution family is one particular witness plus integer combinations of zero-to-zero kernel generators. Solved contraction requires a zero-to-identity witness, not invertibility of the defining chain map.");
        law("D K + K D = to - from on total cone coordinates. Witnesses need not preserve the cone block decomposition; endpoint squares and actual witness matrices are retained.");
        law("Concatenation adds witnesses and requires the exact joining map; reversal negates them. Precomposition uses K_n B_n; postcomposition uses A_(n+1) K_n. Both endpoints induce equal integral homology and cohomology maps.");
        law("For an integral chain isomorphism F, Cone(F) contracts by K(t,s)=(0,F^-1 t). Typed actions raise chain degree or lower positive cochain degree and return the second carrier's actual wrapper.");
    }
}
