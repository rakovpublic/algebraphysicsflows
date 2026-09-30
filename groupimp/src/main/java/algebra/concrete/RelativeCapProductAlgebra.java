package algebra.concrete;

import mathematics.topology.RelativeCapProduct;

/** General relative cap products through the original binary and unary operation interfaces. */
public final class RelativeCapProductAlgebra extends ConcreteAlgebra<RelativeCapProduct> {
    public RelativeCapProductAlgebra(RelativeSimplicialChainAlgebra chains,RelativeSimplicialAlgebra pairs,
                                    RelativeSimplicialCochainAlgebra cochains,IntegerMatrixFamily matrices,
                                    AbelianGroupElementAlgebra elements,AbelianGroupHomomorphismAlgebra maps,BooleanAlgebra truth) {
        super(carrier("RelativeCap",RelativeCapProduct.class,"A relative chain and an explicit relative cap target",c -> true),chains.unit());
        binary("on",chains.algebra(),pairs.algebra(),algebra(),true,RelativeCapProduct::new);
        unary("chain",algebra(),chains.algebra(),false,RelativeCapProduct::chain);
        unary("target-pair",algebra(),pairs.algebra(),false,RelativeCapProduct::targetPair);
        binary("with-chain",algebra(),chains.algebra(),algebra(),true,RelativeCapProduct::withChain);
        unary("boundary",algebra(),algebra(),false,RelativeCapProduct::boundary);
        binary("equal",algebra(),algebra(),truth.algebra(),false,RelativeCapProduct::equals);
        binary("cap",algebra(),cochains.algebra(),chains.algebra(),true,RelativeCapProduct::cap);
        binary("cap-class",algebra(),cochains.algebra(),elements.algebra(),true,RelativeCapProduct::capClass);
        binary("cap-matrix",algebra(),cochains.algebra(),matrices.algebra(),true,RelativeCapProduct::capMatrix);
        binary("cap-homology-map",algebra(),cochains.algebra(),maps.algebra(),true,RelativeCapProduct::capHomologyMap);
        binary("cap-cohomology-matrix",algebra(),cochains.algebra(),matrices.algebra(),true,RelativeCapProduct::capCohomologyMatrix);
        binary("cap-cohomology-map",algebra(),cochains.algebra(),maps.algebra(),true,RelativeCapProduct::capCohomologyMap);
        law("For D=A union B, a chain on (X,D) caps a cochain on (X,A) to a chain on (X,B). All full labelled pairs are retained and checked.");
        law("The signed boundary identity makes the product descend to integral homology and cohomology, naturally on classes under maps preserving A and B.");
        law("A empty recovers absolute-cochain action on relative chains; B empty recovers relative-cochain action into absolute chains.");
    }
}
