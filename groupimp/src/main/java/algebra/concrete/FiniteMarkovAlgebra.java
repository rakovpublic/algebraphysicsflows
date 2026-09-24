package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.foundations.*;
import mathematics.linear.RationalMatrix;
import mathematics.probability.*;
import java.math.BigInteger;
import java.util.*;

/** Native exact finite stochastic kernels, sharing the registered integer and probability carriers. */
public final class FiniteMarkovAlgebra extends ConcreteAlgebra<FiniteMarkovKernel> {
    private final Algebra<BigInteger> integers;
    public final Algebra<Pair<BigInteger,BigInteger>> transitions;
    public final Algebra<Pair<FiniteDistribution<BigInteger>,BigInteger>> iterationInputs;
    public FiniteMarkovAlgebra(IntegerRing integers,IntegerSetAlgebra sets,FiniteProbabilityAlgebra<BigInteger> probabilities,
                               FiniteIntegerFunctionAlgebra functions,RationalMatrixFamily matrices,RationalVectorFamily vectors,
                               RationalField rationals,NaturalSemiring naturals,BooleanAlgebra truth) {
        super(kernelCarrier(integers.algebra()),integers.unit()); this.integers=integers.algebra();
        if(probabilities.outcomes!=this.integers || sets.elements!=this.integers)
            throw new IllegalArgumentException("Markov kernels, sets and distributions must share one integer Algebra");
        transitions=transitionCarrier(this.integers); iterationInputs=iterationCarrier(probabilities,naturals);
        closed("compose",true,FiniteMarkovKernel::compose);
        unary("domain",algebra(),sets.algebra(),false,FiniteMarkovKernel::domain);
        unary("codomain",algebra(),sets.algebra(),false,FiniteMarkovKernel::codomain);
        unary("domain-size",algebra(),naturals.algebra(),false,k -> BigInteger.valueOf(k.domain().size()));
        unary("codomain-size",algebra(),naturals.algebra(),false,k -> BigInteger.valueOf(k.codomain().size()));
        unary("is-chain",algebra(),truth.algebra(),false,FiniteMarkovKernel::isChain);
        binary("equal",algebra(),algebra(),truth.algebra(),false,FiniteMarkovKernel::equals);
        binary("row",algebra(),this.integers,probabilities.algebra(),true,FiniteMarkovKernel::row);
        unaryFlat("rows",algebra(),probabilities.algebra(),false,FiniteMarkovKernel::rows);
        binary("apply",algebra(),probabilities.algebra(),probabilities.algebra(),true,FiniteMarkovKernel::apply);
        binary("transition-probability",algebra(),transitions,rationals.algebra(),true,(k,p) -> k.probability(p.first,p.second));
        unary("to-matrix",algebra(),matrices.algebra(),true,FiniteMarkovKernel::toMatrix);
        binary("from-matrix",matrices.algebra(),functions.boundaries,algebra(),true,
                (matrix,boundaries) -> FiniteMarkovKernel.fromMatrix(this.integers,matrix,boundaries.first,boundaries.second));
        unary("from-function",functions.algebra(),algebra(),false,FiniteMarkovKernel::fromFunction);
        unary("to-function",algebra(),functions.algebra(),true,FiniteMarkovKernel::toFunction);
        unary("is-deterministic",algebra(),truth.algebra(),false,FiniteMarkovKernel::isDeterministic);
        unary("identity-on",sets.algebra(),algebra(),false,states -> FiniteMarkovKernel.identity(this.integers,states));
        unary("identity-on-domain",algebra(),algebra(),false,k -> FiniteMarkovKernel.identity(this.integers,k.domain()));
        unary("identity-on-codomain",algebra(),algebra(),false,k -> FiniteMarkovKernel.identity(this.integers,k.codomain()));
        binary("power",algebra(),naturals.algebra(),algebra(),true,FiniteMarkovKernel::power);
        flat("orbit",algebra(),iterationInputs,probabilities.algebra(),true,(k,input) -> k.orbit(input.first,input.second));
        unaryFlat("communicating-classes",algebra(),sets.algebra(),true,FiniteMarkovKernel::communicatingClasses);
        unaryFlat("recurrent-classes",algebra(),sets.algebra(),true,FiniteMarkovKernel::recurrentClasses);
        unary("transient-states",algebra(),sets.algebra(),true,FiniteMarkovKernel::transientStates);
        unary("absorbing-states",algebra(),sets.algebra(),true,FiniteMarkovKernel::absorbingStates);
        unary("is-irreducible",algebra(),truth.algebra(),true,FiniteMarkovKernel::isIrreducible);
        unaryFlat("stationary-extremes",algebra(),probabilities.algebra(),true,FiniteMarkovKernel::stationaryExtremes);
        unary("stationary",algebra(),probabilities.algebra(),true,FiniteMarkovKernel::stationary);
        binary("is-stationary",algebra(),probabilities.algebra(),truth.algebra(),true,FiniteMarkovKernel::isStationary);
        binary("reverse",algebra(),probabilities.algebra(),algebra(),true,FiniteMarkovKernel::reverse);
        binary("is-reversible",algebra(),probabilities.algebra(),truth.algebra(),true,FiniteMarkovKernel::isReversible);
        binary("absorbing-on",algebra(),sets.algebra(),algebra(),true,FiniteMarkovKernel::absorbingOn);
        binary("hitting-probabilities",algebra(),sets.algebra(),vectors.algebra(),true,FiniteMarkovKernel::hittingProbabilities);
        binary("mean-hitting-times",algebra(),sets.algebra(),vectors.algebra(),true,FiniteMarkovKernel::meanHittingTimes);
        law("Rows are exact probability measures on the declared codomain. compose applies the right operand first; identical middle boundaries are required.");
        law("Distributions act as row vectors: application is pi*P. Stationary extremes correspond to closed communicating classes; their rational convex mixtures give every rational stationary distribution.");
        law("First hitting times include time zero. Infinite expected hitting times cannot be represented by Q and are reported as undefined.");
    }
    public FiniteMarkovKernel member(FiniteSet<BigInteger> domain,FiniteSet<BigInteger> codomain,
                                     Map<BigInteger,FiniteDistribution<BigInteger>> rows) {
        return new FiniteMarkovKernel(integers,domain,codomain,rows);
    }
    public FiniteMarkovKernel fromMatrix(RationalMatrix matrix,FiniteSet<BigInteger> domain,FiniteSet<BigInteger> codomain) {
        return FiniteMarkovKernel.fromMatrix(integers,matrix,domain,codomain);
    }
    private static Algebra<FiniteMarkovKernel> kernelCarrier(Algebra<BigInteger> integers) {
        return carrier("FiniteMarkov(Z)",FiniteMarkovKernel.class,"Exact stochastic kernels between finite integer sets",k -> k.outcomes()==integers);
    }
    @SuppressWarnings("unchecked")
    private static Algebra<Pair<BigInteger,BigInteger>> transitionCarrier(Algebra<BigInteger> integers) {
        Class<Pair<BigInteger,BigInteger>> type=(Class<Pair<BigInteger,BigInteger>>)(Class<?>)Pair.class;
        return carrier("ZxZ.markov",type,"Source and destination state for a transition probability",p ->
                integers.getParamClass().isInstance(p.first) && integers.getParamClass().isInstance(p.second)
                && integers.validate(p.first) && integers.validate(p.second));
    }
    @SuppressWarnings("unchecked")
    private static Algebra<Pair<FiniteDistribution<BigInteger>,BigInteger>> iterationCarrier(FiniteProbabilityAlgebra<BigInteger> probabilities,NaturalSemiring naturals) {
        Class<Pair<FiniteDistribution<BigInteger>,BigInteger>> type=(Class<Pair<FiniteDistribution<BigInteger>,BigInteger>>)(Class<?>)Pair.class;
        return carrier("FiniteDistribution(Z)xN.markov",type,"Initial distribution and nonnegative step count",p ->
                probabilities.algebra().getParamClass().isInstance(p.first) && naturals.algebra().getParamClass().isInstance(p.second)
                && probabilities.algebra().validate(p.first) && naturals.algebra().validate(p.second));
    }
}
