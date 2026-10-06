package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.topology.SimplicialChainMap;
import mathematics.topology.SimplicialChainInverseSolver;

/** Integral chain maps registered through the original scalar and flat operation interfaces. */
public final class SimplicialChainMapAlgebra extends ConcreteAlgebra<SimplicialChainMap> {
    public final Algebra<SimplicialChainMap.Data> inputs;
    public SimplicialChainMapAlgebra(RelativeSimplicialAlgebra pairs,RelativeSimplicialMapAlgebra maps,FiniteSimplicialMapAlgebra absoluteMaps,
                                    SimplicialCollapseAlgebra collapses,SimplicialCollapseSequenceAlgebra sequences,SimplicialSubdivisionAlgebra subdivisions,
                                    IntegerRing integers,NaturalSemiring naturals,BooleanAlgebra truth,IntegerMatrixFamily matrices,
                                    RelativeSimplicialChainAlgebra relativeChains,RelativeSimplicialCochainAlgebra relativeCochains,
                                    SimplicialChainAlgebra chains,SimplicialCochainAlgebra cochains,AbelianGroupHomomorphismAlgebra homomorphisms) {
        super(carrier("ChainMap",SimplicialChainMap.class,"Validated integral chain maps of labelled simplicial pairs",m -> true),pairs.unit());
        inputs=carrier("ChainMap.data",SimplicialChainMap.Data.class,"Two full pairs and all degree matrices, awaiting chain-map validation",d -> true);
        unary("identity-on",pairs.algebra(),algebra(),false,SimplicialChainMap::identity);
        binary("zero",pairs.algebra(),pairs.algebra(),algebra(),false,SimplicialChainMap::zero);
        unary("from-data",inputs,algebra(),true,SimplicialChainMap::new);
        unary("from-simplicial",maps.algebra(),algebra(),false,SimplicialChainMap::fromSimplicial);
        unary("from-absolute-simplicial",absoluteMaps.algebra(),algebra(),false,SimplicialChainMap::fromAbsoluteSimplicial);
        unary("from-collapse",collapses.algebra(),algebra(),false,SimplicialChainMap::fromCollapse);
        unary("from-collapse-sequence",sequences.algebra(),algebra(),false,SimplicialChainMap::fromCollapseSequence);
        unary("from-subdivision",subdivisions.algebra(),algebra(),false,SimplicialChainMap::fromSubdivision);
        unary("source",algebra(),pairs.algebra(),false,SimplicialChainMap::source);
        unary("target",algebra(),pairs.algebra(),false,SimplicialChainMap::target);
        unary("data",algebra(),inputs,false,SimplicialChainMap::data);
        binary("equal",algebra(),algebra(),truth.algebra(),false,SimplicialChainMap::equals);
        unary("is-zero",algebra(),truth.algebra(),false,SimplicialChainMap::isZero);
        unary("is-identity",algebra(),truth.algebra(),false,SimplicialChainMap::isIdentity);
        closed("add",true,SimplicialChainMap::add);
        unary("negate",algebra(),algebra(),false,SimplicialChainMap::negate);
        closed("subtract",true,SimplicialChainMap::subtract);
        binary("scale",algebra(),integers.algebra(),algebra(),false,SimplicialChainMap::scale);
        closed("compose",true,SimplicialChainMap::compose);
        unary("is-isomorphism",algebra(),truth.algebra(),false,SimplicialChainMap::isIsomorphism);
        unary("inverse",algebra(),algebra(),true,SimplicialChainMap::inverse);
        binary("chain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialChainMap::chainMatrix);
        binary("cochain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialChainMap::cochainMatrix);
        unaryFlat("chain-matrices",algebra(),matrices.algebra(),false,SimplicialChainMap::chainMatrices);
        unaryFlat("cochain-matrices",algebra(),matrices.algebra(),false,SimplicialChainMap::cochainMatrices);
        binary("on-chain",algebra(),relativeChains.algebra(),relativeChains.algebra(),true,SimplicialChainMap::onChain);
        binary("on-cochain",algebra(),relativeCochains.algebra(),relativeCochains.algebra(),true,SimplicialChainMap::onCochain);
        binary("on-absolute-chain",algebra(),chains.algebra(),chains.algebra(),true,SimplicialChainMap::onAbsoluteChain);
        binary("on-absolute-cochain",algebra(),cochains.algebra(),cochains.algebra(),true,SimplicialChainMap::onAbsoluteCochain);
        binary("homology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialChainMap::homologyMap);
        binary("cohomology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialChainMap::cohomologyMap);
        unaryFlat("homology-maps",algebra(),homomorphisms.algebra(),false,SimplicialChainMap::homologyMaps);
        unaryFlat("cohomology-maps",algebra(),homomorphisms.algebra(),false,SimplicialChainMap::cohomologyMaps);
        unary("is-homotopy-equivalence",algebra(),truth.algebra(),false,SimplicialChainInverseSolver::isHomotopyEquivalence);
        unary("homotopy-inverse",algebra(),algebra(),true,SimplicialChainInverseSolver::inverse);
        unary("is-quasi-isomorphism",algebra(),truth.algebra(),false,SimplicialChainMap::isQuasiIsomorphism);
        law("Every degree matrix satisfies d_target F = F d_source on integral quotient chains; all full labelled endpoints are retained.");
        law("Parallel maps form abelian groups under addition; composition is bilinear and applies the right operand first. Strict inversion requires degreewise unimodularity over Z; homotopy-inverse solves for a chain inverse and both integral homotopies simultaneously.");
        law("Homology is covariant and additive; transpose pullback on cohomology reverses composition. Arbitrary chain maps need not preserve cup products or arise from vertex maps.");
    }
}
