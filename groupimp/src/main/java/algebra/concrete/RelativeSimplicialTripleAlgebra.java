package algebra.concrete;

import mathematics.topology.RelativeSimplicialTriple;

/** Native integral homology and cohomology exact sequences of nested simplicial triples. */
public final class RelativeSimplicialTripleAlgebra extends ConcreteAlgebra<RelativeSimplicialTriple> {
    public RelativeSimplicialTripleAlgebra(RelativeSimplicialAlgebra pairs,FiniteSimplicialAlgebra complexes,RelativeSimplicialMapAlgebra pairMaps,
                                         RelativeSimplicialChainAlgebra chains,RelativeSimplicialCochainAlgebra cochains,IntegerMatrixFamily matrices,
                                         AbelianGroupHomomorphismAlgebra maps,NaturalSemiring naturals,BooleanAlgebra truth) {
        super(carrier("RelativeTriple",RelativeSimplicialTriple.class,"Labelled B subset A subset X with integral exact sequences",t -> true),pairs.unit());
        binary("from-pair",pairs.algebra(),complexes.algebra(),algebra(),true,RelativeSimplicialTriple::new);
        unary("outer-pair",algebra(),pairs.algebra(),false,RelativeSimplicialTriple::outerPair);
        unary("total-pair",algebra(),pairs.algebra(),false,RelativeSimplicialTriple::totalPair);
        unary("inner-pair",algebra(),pairs.algebra(),false,RelativeSimplicialTriple::innerPair);
        binary("equal",algebra(),algebra(),truth.algebra(),false,RelativeSimplicialTriple::equals);
        unary("inclusion-map",algebra(),pairMaps.algebra(),false,RelativeSimplicialTriple::inclusionMap);
        unary("quotient-map",algebra(),pairMaps.algebra(),false,RelativeSimplicialTriple::quotientMap);
        binary("inclusion-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,RelativeSimplicialTriple::inclusionMatrix);
        binary("quotient-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,RelativeSimplicialTriple::quotientMatrix);
        binary("lift-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,RelativeSimplicialTriple::liftMatrix);
        binary("connecting-chain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,RelativeSimplicialTriple::connectingChainMatrix);
        binary("inclusion-homology",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialTriple::inclusionHomology);
        binary("quotient-homology",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialTriple::quotientHomology);
        binary("connecting-homology",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialTriple::connectingHomology);
        flat("long-exact-segment",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialTriple::longExactSegment);
        binary("connect-cycle",algebra(),chains.algebra(),chains.algebra(),true,RelativeSimplicialTriple::connectCycle);
        binary("extension-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,RelativeSimplicialTriple::extensionMatrix);
        binary("restriction-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,RelativeSimplicialTriple::restrictionMatrix);
        binary("connecting-cochain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,RelativeSimplicialTriple::connectingCochainMatrix);
        binary("extension-cohomology",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialTriple::extensionCohomology);
        binary("restriction-cohomology",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialTriple::restrictionCohomology);
        binary("connecting-cohomology",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialTriple::connectingCohomology);
        flat("long-exact-cohomology-segment",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialTriple::longExactCohomologySegment);
        binary("connect-cocycle",algebra(),cochains.algebra(),cochains.algebra(),true,RelativeSimplicialTriple::connectCocycle);
        law("The filtered quotient chains form 0 -> C(A,B) -> C(X,B) -> C(X,A) -> 0. The zero-on-A/B section generally is not a chain map.");
        law("Homology and cohomology exact segments retain integral presentations, torsion and the full labelled pairs. Connecting homology lowers degree; connecting cohomology raises degree.");
        law("Consecutive exact-sequence maps compose to zero, with integral images equal to the next kernels; the sequences are natural under maps preserving both nested subcomplexes.");
    }
}
