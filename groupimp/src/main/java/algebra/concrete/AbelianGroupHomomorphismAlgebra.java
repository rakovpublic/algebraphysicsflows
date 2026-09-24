package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.foundations.Pair;
import mathematics.structures.AbelianGroupHomomorphism;
import mathematics.structures.PresentedAbelianGroup;
import java.math.BigInteger;

/** Explicit abelian maps, installed through the original native operation interfaces. */
public final class AbelianGroupHomomorphismAlgebra extends ConcreteAlgebra<AbelianGroupHomomorphism> {
    public final Algebra<Pair<PresentedAbelianGroup,PresentedAbelianGroup>> boundaries;
    public AbelianGroupHomomorphismAlgebra(PresentedAbelianGroupAlgebra groups,AbelianGroupElementAlgebra elements,
                                          IntegerMatrixFamily matrices,IntegerRing integers,BooleanAlgebra truth) {
        super(carrier("AbelianGroupHomomorphism",AbelianGroupHomomorphism.class,"Homomorphisms between retained abelian presentations",h -> true),integers.unit());
        boundaries=boundaryCarrier(groups.algebra());
        closed("compose",true,AbelianGroupHomomorphism::compose);
        closed("add",true,AbelianGroupHomomorphism::add);
        closed("subtract",true,(a,b) -> a.add(b.scale(BigInteger.ONE.negate())));
        unary("negate",algebra(),algebra(),false,h -> h.scale(BigInteger.ONE.negate()));
        binary("scale",algebra(),integers.algebra(),algebra(),false,AbelianGroupHomomorphism::scale);
        unary("source",algebra(),groups.algebra(),false,AbelianGroupHomomorphism::source);
        unary("target",algebra(),groups.algebra(),false,AbelianGroupHomomorphism::target);
        unary("smith-matrix",algebra(),matrices.algebra(),false,AbelianGroupHomomorphism::smithMatrix);
        unary("matrix-lift",algebra(),matrices.algebra(),false,AbelianGroupHomomorphism::matrixLift);
        binary("apply",algebra(),elements.algebra(),elements.algebra(),true,AbelianGroupHomomorphism::apply);
        unaryFlat("generator-images",algebra(),elements.algebra(),false,AbelianGroupHomomorphism::generatorImages);
        unary("is-zero",algebra(),truth.algebra(),false,AbelianGroupHomomorphism::isZero);
        binary("equal",algebra(),algebra(),truth.algebra(),false,AbelianGroupHomomorphism::equals);
        unary("identity-on",groups.algebra(),algebra(),false,AbelianGroupHomomorphism::identity);
        binary("zero-between",groups.algebra(),groups.algebra(),algebra(),false,AbelianGroupHomomorphism::zero);
        unary("zero-like",algebra(),algebra(),false,h -> AbelianGroupHomomorphism.zero(h.source(),h.target()));
        unary("kernel",algebra(),groups.algebra(),false,AbelianGroupHomomorphism::kernel);
        unary("kernel-inclusion",algebra(),algebra(),false,AbelianGroupHomomorphism::kernelInclusion);
        unary("image",algebra(),groups.algebra(),false,AbelianGroupHomomorphism::image);
        unary("image-inclusion",algebra(),algebra(),false,AbelianGroupHomomorphism::imageInclusion);
        unary("image-projection",algebra(),algebra(),false,AbelianGroupHomomorphism::imageProjection);
        unary("cokernel",algebra(),groups.algebra(),false,AbelianGroupHomomorphism::cokernel);
        unary("cokernel-projection",algebra(),algebra(),false,AbelianGroupHomomorphism::cokernelProjection);
        unary("is-injective",algebra(),truth.algebra(),false,AbelianGroupHomomorphism::isInjective);
        unary("is-surjective",algebra(),truth.algebra(),false,AbelianGroupHomomorphism::isSurjective);
        unary("is-isomorphism",algebra(),truth.algebra(),false,AbelianGroupHomomorphism::isIsomorphism);
        unary("inverse",algebra(),algebra(),true,AbelianGroupHomomorphism::inverse);
        binary("has-preimage",algebra(),elements.algebra(),truth.algebra(),true,AbelianGroupHomomorphism::hasPreimage);
        binary("preimage",algebra(),elements.algebra(),elements.algebra(),true,AbelianGroupHomomorphism::preimage);
        binary("from-matrix",matrices.algebra(),boundaries,algebra(),true,(m,b) -> AbelianGroupHomomorphism.fromMatrix(b.first,b.second,m));
        binary("from-smith-matrix",matrices.algebra(),boundaries,algebra(),true,(m,b) -> AbelianGroupHomomorphism.fromSmith(b.first,b.second,m));
        binary("scaling-on",groups.algebra(),integers.algebra(),algebra(),false,AbelianGroupHomomorphism::scaling);
        law("Maps respect every source relation. Equality compares both presentations and normalized images of their full Smith-coordinate generators.");
        law("compose applies the right operand first and requires equal middle presentations; addition requires equal source and target presentations.");
        law("Image inclusion composed with image projection equals the original map. Kernel inclusion and cokernel projection compose with the map to zero.");
        law("A preimage is one representative; its coset by the kernel is the complete fiber. Inverse is defined exactly for isomorphisms.");
    }
    @SuppressWarnings("unchecked")
    private static Algebra<Pair<PresentedAbelianGroup,PresentedAbelianGroup>> boundaryCarrier(Algebra<PresentedAbelianGroup> groups) {
        Class<Pair<PresentedAbelianGroup,PresentedAbelianGroup>> type=(Class<Pair<PresentedAbelianGroup,PresentedAbelianGroup>>)(Class<?>)Pair.class;
        return carrier("PresentedAbelianGroup.pair",type,"Source and target presentations for a homomorphism",p ->
                groups.getParamClass().isInstance(p.first) && groups.getParamClass().isInstance(p.second)
                && groups.validate(p.first) && groups.validate(p.second));
    }
}
