package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.topology.IntegralChainConeMap;

/** Supplied homotopy-commutative cone squares in the original scalar and flat operation model. */
public final class IntegralChainConeMapAlgebra extends ConcreteAlgebra<IntegralChainConeMap> {
    public final Algebra<IntegralChainConeMap.Data> inputs;
    public IntegralChainConeMapAlgebra(IntegralChainMappingConeAlgebra cones,SimplicialChainMapAlgebra maps,SimplicialChainHomotopyAlgebra homotopies,
                                      IntegerMatrixFamily matrices,AbelianGroupHomomorphismAlgebra homomorphisms,NaturalSemiring naturals,BooleanAlgebra truth) {
        super(carrier("ChainConeMap",IntegralChainConeMap.class,"Maps of integral cones with retained square maps and a chosen integral homotopy",m -> true),cones.unit());
        inputs=carrier("ChainConeMap.data",IntegralChainConeMap.Data.class,"Individually valid cone contexts, square maps and homotopy awaiting compatibility checks",d -> true);
        unary("from-data",inputs,algebra(),true,IntegralChainConeMap::new);
        unary("data",algebra(),inputs,false,IntegralChainConeMap::data);
        unary("identity-on",cones.algebra(),algebra(),false,IntegralChainConeMap::identity);
        unary("from-homotopy",homotopies.algebra(),algebra(),false,IntegralChainConeMap::fromHomotopy);
        unary("source",algebra(),cones.algebra(),false,IntegralChainConeMap::source);
        unary("target",algebra(),cones.algebra(),false,IntegralChainConeMap::target);
        unary("source-map",algebra(),maps.algebra(),false,IntegralChainConeMap::sourceMap);
        unary("target-map",algebra(),maps.algebra(),false,IntegralChainConeMap::targetMap);
        unary("homotopy",algebra(),homotopies.algebra(),false,IntegralChainConeMap::homotopy);
        binary("equal",algebra(),algebra(),truth.algebra(),false,IntegralChainConeMap::equals);
        closed("compose",true,IntegralChainConeMap::compose);
        binary("chain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,IntegralChainConeMap::chainMatrix);
        binary("cochain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,IntegralChainConeMap::cochainMatrix);
        unaryFlat("chain-matrices",algebra(),matrices.algebra(),false,IntegralChainConeMap::chainMatrices);
        unaryFlat("cochain-matrices",algebra(),matrices.algebra(),false,IntegralChainConeMap::cochainMatrices);
        binary("homology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,IntegralChainConeMap::homologyMap);
        binary("cohomology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,IntegralChainConeMap::cohomologyMap);
        unaryFlat("homology-maps",algebra(),homomorphisms.algebra(),false,IntegralChainConeMap::homologyMaps);
        unaryFlat("cohomology-maps",algebra(),homomorphisms.algebra(),false,IntegralChainConeMap::cohomologyMaps);
        flat("homology-naturality-maps",algebra(),naturals.algebra(),homomorphisms.algebra(),false,IntegralChainConeMap::homologyNaturalityMaps);
        flat("cohomology-naturality-maps",algebra(),naturals.algebra(),homomorphisms.algebra(),false,IntegralChainConeMap::cohomologyNaturalityMaps);
        law("For F:S->T, G:S'->T', a:S->S', b:T->T', retain H with dH+Hd=Ga-bF. The cone map in degree n is [[b_n,-H_(n-1)],[0,a_(n-1)]].");
        law("Composition applies the right operand first and retains b_after H_before + H_after a_before. It is associative and unital on actual witnesses. Chosen homotopies can change the induced cone homology maps.");
        law("Integral homology is covariant, cohomology is contravariant, and both long exact sequences are natural with exact retained presentations. No canonical choice from homotopy classes alone is asserted.");
    }
    public IntegralChainConeMapAlgebra(IntegralChainMappingConeAlgebra cones,SimplicialChainMapAlgebra maps,SimplicialChainHomotopyAlgebra homotopies,
            IntegerMatrixFamily matrices,AbelianGroupHomomorphismAlgebra homomorphisms,NaturalSemiring naturals,BooleanAlgebra truth,
            IntegralConeChainAlgebra chains,IntegralConeCochainAlgebra cochains) {
        this(cones,maps,homotopies,matrices,homomorphisms,naturals,truth);
        binary("on-chain",algebra(),chains.algebra(),chains.algebra(),true,IntegralChainConeMap::onChain);
        binary("on-cochain",algebra(),cochains.algebra(),cochains.algebra(),true,IntegralChainConeMap::onCochain);
        law("Typed pushforward and contravariant pullback use the actual second carrier's wrapper, check exact cone contexts, commute with differentials and preserve evaluation.");
    }

}
