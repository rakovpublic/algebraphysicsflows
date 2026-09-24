package mathematics.examples;

import algebra.concrete.ConcreteMathematics;
import algebra.concrete.ConcreteAlgebra;
import algebra.concrete.OperationRegistration;
import mathematics.calculus.Polynomial;
import mathematics.calculus.MultivariatePolynomial;
import mathematics.calculus.PolynomialMap;
import mathematics.calculus.PolynomialDifferentialForm;
import mathematics.calculus.PolynomialCell;
import mathematics.calculus.PolynomialChain;
import mathematics.foundations.Pair;
import mathematics.foundations.FiniteSet;
import mathematics.structures.FiniteCategory;
import mathematics.structures.FiniteFunctor;
import mathematics.structures.FiniteNaturalTransformation;
import mathematics.structures.FiniteEquivalence;
import mathematics.structures.FiniteAdjunction;
import mathematics.structures.FiniteCone;
import mathematics.structures.FiniteCocone;
import mathematics.structures.AbelianGroupType;
import mathematics.structures.PresentedAbelianGroup;
import mathematics.structures.AbelianGroupElement;
import mathematics.structures.AbelianGroupHomomorphism;
import mathematics.topology.FiniteSimplicialComplex;
import mathematics.linear.RationalVector;
import mathematics.linear.IntegerVector;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.RationalMatrix;
import mathematics.linear.RationalAffineSpace;
import mathematics.linear.RationalTensor;
import mathematics.linear.RationalExterior;
import mathematics.numbers.Rational;
import mathematics.numbers.RationalQuaternion;
import mathematics.probability.FiniteMarkovKernel;
import mathematics.probability.FiniteDistribution;
import java.math.BigInteger;
import java.util.*;

/** Runnable examples using the actual MathTool-registered algebras and legacy flow API. */
public final class ConcreteAlgebrasExample {
    private ConcreteAlgebrasExample() { }
    public static void main(String[] args) {
        ConcreteMathematics math=new ConcreteMathematics();
        if(args.length==1 && args[0].equals("--catalog")) {
            System.out.print(catalogManifest(math));
            return;
        }
        System.out.println("MathTool: "+math.mathTool.getName()+", concrete algebras: "+math.algebras().size()+", operations: "+math.operations().size());
        IntegerMatrix equation=new IntegerMatrix(new BigInteger[][]{{BigInteger.valueOf(2),BigInteger.valueOf(3)}});
        System.out.println("Integer solutions of 2*x+3*y=1: particular then kernel basis: "+math.flow(math.integerMatrices,Collections.singletonList(equation))
                .performLeftProjectionFlatOperation("solve-generators",new IntegerVector(BigInteger.ONE)).collect());
        IntegerMatrix presentation=new IntegerMatrix(new BigInteger[][]{{BigInteger.valueOf(2),BigInteger.ZERO},
                {BigInteger.ZERO,BigInteger.valueOf(3)},{BigInteger.ZERO,BigInteger.ZERO}});
        System.out.println("Group presented by columns (2,0,0), (0,3,0): "+math.flow(math.integerMatrices,Collections.singletonList(presentation))
                .<AbelianGroupType>performAlgebraTransfer("cokernel").collect());
        IntegerMatrix finitePresentation=new IntegerMatrix(new BigInteger[][]{{BigInteger.valueOf(2),BigInteger.ZERO},{BigInteger.ZERO,BigInteger.valueOf(3)}});
        System.out.println("Kernel type of doubling on Z/2 + Z/3: "+math.flow(math.integerMatrices,Collections.singletonList(finitePresentation))
                .<PresentedAbelianGroup>performAlgebraTransfer("PresentedAbelianGroup.from-matrix")
                .<AbelianGroupHomomorphism,BigInteger>performAlgebraUnsafe("AbelianGroupHomomorphism.scaling-on",BigInteger.valueOf(2))
                .<PresentedAbelianGroup>performAlgebraTransfer("kernel")
                .<AbelianGroupType>performAlgebraTransfer("as-type").collect());
        System.out.println("Orders of all elements in Z/2 + Z/3: "+math.flow(math.integerMatrices,Collections.singletonList(finitePresentation))
                .<PresentedAbelianGroup>performAlgebraTransfer("PresentedAbelianGroup.from-matrix")
                .<AbelianGroupElement>performFlatAlgebraTransfer("AbelianGroupElement.elements")
                .<BigInteger>performAlgebraTransfer("order").collect());
        System.out.println("Smith coordinates of solutions of 2*x=(0,2) in that quotient: "+math.flow(math.integerMatrices,Collections.singletonList(finitePresentation))
                .<PresentedAbelianGroup>performAlgebraTransfer("PresentedAbelianGroup.from-matrix")
                .<AbelianGroupElement,IntegerVector>performAlgebraUnsafe("AbelianGroupElement.from-smith",new IntegerVector(BigInteger.ZERO,BigInteger.valueOf(2)))
                .performFlatCustomMemberOperation("multiplication-preimages",BigInteger.valueOf(2))
                .<IntegerVector>performAlgebraTransfer("smith-coordinates").collect());
        FiniteSimplicialComplex projectivePlane=new FiniteSimplicialComplex(Arrays.asList(
                FiniteSet.of(0,1,2),FiniteSet.of(0,1,3),FiniteSet.of(0,2,4),FiniteSet.of(0,3,5),FiniteSet.of(0,4,5),
                FiniteSet.of(1,2,5),FiniteSet.of(1,3,4),FiniteSet.of(1,4,5),FiniteSet.of(2,3,4),FiniteSet.of(2,3,5)));
        System.out.println("Integral H1 of the projective plane: "+math.flow(math.complexes,Collections.singletonList(projectivePlane))
                .<AbelianGroupType,BigInteger>performAlgebraUnsafe("integral-homology",BigInteger.ONE).collect());
        System.out.println("Its integral torsion factors by degree: "+math.flow(math.complexes,Collections.singletonList(projectivePlane))
                .<AbelianGroupType>performFlatAlgebraTransfer("integral-homology-groups")
                .<BigInteger>performFlatAlgebraTransfer("invariant-factors").collect());
        System.out.println("Its rational Betti numbers: "+math.flow(math.complexes,Collections.singletonList(projectivePlane))
                .<BigInteger>performFlatAlgebraTransfer("rational-betti-numbers").collect());
        FiniteSet<BigInteger> markovStates=FiniteSet.of(BigInteger.ZERO,BigInteger.ONE);
        FiniteMarkovKernel markov=math.markovKernels.fromMatrix(new RationalMatrix(new Rational[][]{
                {Rational.of(1,2),Rational.of(1,2)},{Rational.of(1,4),Rational.of(3,4)}}),markovStates,markovStates);
        System.out.println("Unique exact stationary distribution: "+math.flow(math.markovKernels,Collections.singletonList(markov))
                .<FiniteDistribution<BigInteger>>performAlgebraTransfer("stationary").collect());
        System.out.println("Mean first hitting times of state 1: "+math.flow(math.markovKernels,Collections.singletonList(markov))
                .<RationalVector,FiniteSet<BigInteger>>performAlgebraUnsafe("mean-hitting-times",FiniteSet.of(BigInteger.ONE)).collect());
        RationalMatrix spectralMatrix=new RationalMatrix(new Rational[][]{{Rational.of(2),Rational.ONE},{Rational.ZERO,Rational.of(3)}});
        System.out.println("Rational roots of the characteristic polynomial of [[2,1],[0,3]]: "+math.flow(math.rectangularMatrices,Collections.singletonList(spectralMatrix))
                .<Polynomial>performAlgebraTransfer("characteristic-polynomial").<Rational>performFlatAlgebraTransfer("rational-roots").collect());
        System.out.println("Rational diagonalization [P,D], A*P=P*D: "+math.flow(math.rectangularMatrices,Collections.singletonList(spectralMatrix))
                .performOneOperandFlatOperation("diagonalize-over-q").collect());
        System.out.println("1/2 + 1/3 = "+math.rationals.algebra().buildAlgebraItem(Rational.of(1,2))
                .performOperation("add",Rational.of(1,3)).perform().getResult());
        System.out.println("N -> Z -> Q: "+math.flow(math.naturals,Arrays.asList(BigInteger.ONE,BigInteger.valueOf(2)))
                .performOperation("add",BigInteger.ONE)
                .<BigInteger>performAlgebraTransfer("to-integer")
                .<Rational>performAlgebraTransfer("to-rational")
                .performOperation("divide",Rational.of(2)).collect());
        System.out.println("Vector x scalar -> vector (flat): "+math.flow(math.vectors,Collections.singletonList(new RationalVector(Rational.ONE,Rational.of(2))))
                .performFlatCustomMemberOperation("scale-flat",Rational.of(3)).collect());
        RationalMatrix rectangular=new RationalMatrix(new Rational[][] {
                {Rational.ONE,Rational.of(2),Rational.of(3)},
                {Rational.of(2),Rational.of(4),Rational.of(6)}});
        System.out.println("Rectangular system solution at parameters [-1, 2]: "+math.flow(math.rectangularMatrices,Collections.singletonList(rectangular))
                .<RationalAffineSpace,RationalVector>performAlgebraUnsafe("Affine(Q).solve",new RationalVector(Rational.ONE,Rational.of(2)))
                .performLeftProjectionOperation("at",new RationalVector(Rational.of(-1),Rational.of(2))).collect());
        System.out.println("Minimum-norm least-squares fit for inconsistent right-hand side [1, 3]: "+math.flow(math.rectangularMatrices,Collections.singletonList(rectangular))
                .<RationalAffineSpace,RationalVector>performAlgebraUnsafe("Affine(Q).least-squares",new RationalVector(Rational.ONE,Rational.of(3)))
                .<RationalVector>performAlgebraTransfer("minimum-norm").collect());
        System.out.println("Tensor product and contraction recover trace(A*A^T): "+math.flow(math.rectangularMatrices,Collections.singletonList(rectangular))
                .<RationalTensor>performAlgebraTransfer("Tensor(Q).from-matrix")
                .performOperation("tensor-product",RationalTensor.fromMatrix(rectangular.transpose()))
                .performCustomMemberOperation("contract",new Pair<>(BigInteger.ONE,BigInteger.valueOf(2)))
                .<RationalMatrix>performAlgebraTransfer("to-matrix").<Rational>performAlgebraTransfer("trace").collect());
        System.out.println("Exterior wedge and Hodge star recover the cross product: "+math.flow(math.finiteVectors,
                Collections.singletonList(new RationalVector(Rational.ONE,Rational.of(2),Rational.of(3))))
                .<RationalExterior>performAlgebraTransfer("Exterior(Q).from-vector")
                .performOperation("wedge",RationalExterior.fromVector(new RationalVector(Rational.of(4),Rational.of(5),Rational.of(6))))
                .performOneOperandOperation("hodge-star").<RationalVector>performAlgebraTransfer("to-vector").collect());
        System.out.println("Exact quaternion quarter-turn around z: "+math.flow(math.quaternions,
                Collections.singletonList(new RationalQuaternion(Rational.ONE,Rational.ZERO,Rational.ZERO,Rational.ONE)))
                .<RationalMatrix>performAlgebraTransfer("to-rotation-matrix")
                .<RationalQuaternion>performAlgebraTransfer("H(Q).from-rotation-matrix")
                .performLeftProjectionOperation("rotate",new RationalVector(Rational.ONE,Rational.of(2),Rational.of(3))).collect());
        MultivariatePolynomial multivariate=MultivariatePolynomial.monomial(Rational.ONE,2,1,0)
                .add(MultivariatePolynomial.monomial(Rational.of(3),0,1,2));
        System.out.println("Polynomial gradient at [2, 3, -1]: "+math.flow(math.multivariatePolynomials,Collections.singletonList(multivariate))
                .<PolynomialMap>performAlgebraTransfer("PolynomialMap(Q).gradient")
                .performLeftProjectionOperation("evaluate",new RationalVector(Rational.of(2),Rational.of(3),Rational.of(-1))).collect());
        System.out.println("Jacobian of that gradient is the Hessian: "+math.flow(math.multivariatePolynomials,Collections.singletonList(multivariate))
                .<PolynomialMap>performAlgebraTransfer("PolynomialMap(Q).gradient")
                .<RationalMatrix,RationalVector>performAlgebraUnsafe("jacobian-at",new RationalVector(Rational.of(2),Rational.of(3),Rational.of(-1))).collect());
        PolynomialMap rotationField=new PolynomialMap(MultivariatePolynomial.variable(3,1).negate(),
                MultivariatePolynomial.variable(3,0),MultivariatePolynomial.constant(3,Rational.ZERO));
        System.out.println("Curl via differential forms, d and Hodge star: "+math.flow(math.polynomialMaps,Collections.singletonList(rotationField))
                .<PolynomialDifferentialForm>performAlgebraTransfer("PolynomialForm(Q).from-vector-field")
                .performOneOperandOperation("exterior-derivative").performOneOperandOperation("hodge-star")
                .<PolynomialMap>performAlgebraTransfer("to-vector-field")
                .performLeftProjectionOperation("evaluate",new RationalVector(Rational.of(2),Rational.of(3),Rational.of(4))).collect());
        PolynomialCell rectangle=PolynomialCell.parameterized(new PolynomialMap(MultivariatePolynomial.variable(2,0).scale(Rational.of(2)),
                MultivariatePolynomial.variable(2,1).scale(Rational.of(3))));
        Map<Integer,MultivariatePolynomial> formCoefficients=new TreeMap<>();
        formCoefficients.put(1,MultivariatePolynomial.monomial(Rational.ONE,2,1));
        formCoefficients.put(2,MultivariatePolynomial.monomial(Rational.ONE,1,2));
        PolynomialDifferentialForm omega=new PolynomialDifferentialForm(2,formCoefficients);
        System.out.println("Oriented boundary integral on a 2 by 3 rectangle: "+math.flow(math.polynomialCells,Collections.singletonList(rectangle))
                .<PolynomialChain>performAlgebraTransfer("PolynomialChain(Q).from-cell").performOneOperandOperation("boundary")
                .<Rational,PolynomialDifferentialForm>performAlgebraUnsafe("integrate",omega).collect());
        System.out.println("Integral of its exterior derivative over the rectangle: "+math.flow(math.polynomialCells,Collections.singletonList(rectangle))
                .<Rational,PolynomialDifferentialForm>performAlgebraUnsafe("integrate",omega.exteriorDerivative()).collect());
        System.out.println("Integral of x^2 from 0 to 1: "+math.flow(math.polynomials,Collections.singletonList(new Polynomial(Rational.ZERO,Rational.ZERO,Rational.ONE)))
                .<Rational,Pair<Rational,Rational>>performAlgebraUnsafe("integrate",new Pair<>(Rational.ZERO,Rational.ONE)).collect());
        System.out.println("F5: 3 / 2 = "+math.primeFields.get(0).algebra().buildAlgebraItem(math.primeFields.get(0).member(3))
                .performOperation("divide",math.primeFields.get(0).member(2)).perform().getResult());
        System.out.println("Solutions of 2*x=4 modulo 6: "+math.flow(math.residues,Collections.singletonList(math.residues.member(2)))
                .performFlatOperation("solve-multiply",math.residues.member(4)).<BigInteger>performAlgebraTransfer("lift").collect());
        System.out.println("Finite identity evaluated at 2: "+math.flow(math.integerSets,Collections.singletonList(FiniteSet.of(BigInteger.ONE,BigInteger.valueOf(2))))
                .<mathematics.foundations.FiniteFunction<BigInteger,BigInteger>>performAlgebraTransfer("FiniteFunction(Z,Z).identity-on")
                .performLeftProjectionOperation("apply",BigInteger.valueOf(2)).collect());
        System.out.println("Initial objects of a one-object discrete category: "+math.flow(math.integerSets,Collections.singletonList(FiniteSet.of(BigInteger.TEN)))
                .<FiniteCategory>performAlgebraTransfer("FiniteCategory.discrete-on")
                .<BigInteger>performFlatAlgebraTransfer("initial-objects").collect());
        System.out.println("Identity natural transformation component: "+math.flow(math.categories,
                Collections.singletonList(FiniteCategory.discrete(FiniteSet.of(BigInteger.TEN))))
                .<FiniteFunctor>performAlgebraTransfer("FiniteFunctor.identity-on")
                .<FiniteNaturalTransformation>performAlgebraTransfer("FiniteNaturalTransformation.identity-on")
                .performLeftProjectionOperation("component",BigInteger.TEN).collect());
        System.out.println("Constructed equivalence unit component: "+math.flow(math.categories,
                Collections.singletonList(FiniteCategory.discrete(FiniteSet.of(BigInteger.TEN))))
                .<FiniteFunctor>performAlgebraTransfer("FiniteFunctor.identity-on")
                .<FiniteEquivalence>performAlgebraTransfer("FiniteEquivalence.from-functor")
                .<FiniteNaturalTransformation>performAlgebraTransfer("unit")
                .performLeftProjectionOperation("component",BigInteger.TEN).collect());
        System.out.println("Constructed right adjoint hom correspondence: "+math.flow(math.categories,
                Collections.singletonList(FiniteCategory.discrete(FiniteSet.of(BigInteger.TEN))))
                .<FiniteFunctor>performAlgebraTransfer("FiniteFunctor.identity-on")
                .<FiniteAdjunction>performAlgebraTransfer("FiniteAdjunction.from-left")
                .<BigInteger,Pair<BigInteger,BigInteger>>performAlgebraUnsafe("transpose",new Pair<>(BigInteger.TEN,BigInteger.TEN)).collect());
        System.out.println("Limit vertex of an empty diagram: "+math.flow(math.categories,
                Collections.singletonList(FiniteCategory.discrete(FiniteSet.of(BigInteger.TEN))))
                .<FiniteFunctor>performAlgebraTransfer("FiniteFunctor.empty-diagram")
                .<FiniteCone>performAlgebraTransfer("FiniteCone.limit")
                .<BigInteger>performAlgebraTransfer("vertex").collect());
        System.out.println("Colimit vertex of an empty diagram: "+math.flow(math.categories,
                Collections.singletonList(FiniteCategory.discrete(FiniteSet.of(BigInteger.TEN))))
                .<FiniteFunctor>performAlgebraTransfer("FiniteFunctor.empty-diagram")
                .<FiniteCocone>performAlgebraTransfer("FiniteCocone.colimit")
                .<BigInteger>performAlgebraTransfer("vertex").collect());
    }
    /** Stable manifest used to keep the human/JSON coverage registry aligned with real registrations. */
    public static String catalogManifest(ConcreteMathematics math) {
        StringBuilder result=new StringBuilder("id\tclass\talias\tfirst\tsecond\tresult\tsemantics\tpartiality\tinterface\n");
        for(ConcreteAlgebra<?> algebra : math.algebras()) for(OperationRegistration operation : algebra.operations().values()) {
            result.append(operation.id).append('\t').append(algebra.getClass().getSimpleName()).append('\t').append(operation.alias).append('\t')
                    .append(operation.first.getAlgebraName()).append('\t')
                    .append(operation.second==null?"-":operation.second.getAlgebraName()).append('\t')
                    .append(operation.result.getAlgebraName()).append('\t').append(operation.flat?"LIST":"SCALAR").append('\t')
                    .append(operation.partial?"PARTIAL":"TOTAL").append('\t').append(operation.operation.getClass().getInterfaces()[0].getSimpleName()).append('\n');
        }
        return result.toString();
    }
}
