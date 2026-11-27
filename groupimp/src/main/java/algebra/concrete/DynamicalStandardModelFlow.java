package algebra.concrete;

import algebra.imp.Algebra;
import algebra.imp.MathTool;
import algebraflow.IAlgebraFlow;
import algebraflow.imp.AlgebraFlow;
import algebraflow.imp.ListAlgebraInput;
import mathematics.numbers.CosmologicalTransfers;
import mathematics.numbers.DixonMinimalLeftIdeal;
import mathematics.numbers.FiniteDiracSpectralAction;
import mathematics.numbers.OneLoopRGEFlow;
import mathematics.numbers.Rational;
import mathematics.numbers.WittGaugeAction;
import operations.simple.OneOperandOperation;
import java.util.Collections;

/** Registers the exact dynamical invariants on a dedicated MathTool, leaving the native catalog unchanged. */
public final class DynamicalStandardModelFlow {
    private DynamicalStandardModelFlow(){}
    public static final String ALGEBRA="DynamicalSM";

    public static MathTool tool(){
        Algebra<Rational> rationals=new Algebra<Rational>(ALGEBRA,Rational.class,"Exact dynamical Standard Model invariants");
        rationals.addOneOperandOperation("b1",new OneOperandOperation<Rational>("b1",rationals,ignored -> OneLoopRGEFlow.B1));
        rationals.addOneOperandOperation("b2",new OneOperandOperation<Rational>("b2",rationals,ignored -> OneLoopRGEFlow.B2));
        rationals.addOneOperandOperation("b3",new OneOperandOperation<Rational>("b3",rationals,ignored -> OneLoopRGEFlow.B3));
        rationals.addOneOperandOperation("ideal-rank",new OneOperandOperation<Rational>("ideal-rank",rationals,ignored -> Rational.of(DixonMinimalLeftIdeal.model().rank())));
        rationals.addOneOperandOperation("quartic-sign",new OneOperandOperation<Rational>("quartic-sign",rationals,
                ignored -> FiniteDiracSpectralAction.model().quarticIsPositive()?Rational.ONE:Rational.ZERO));
        rationals.addOneOperandOperation("su2-casimir-doublet",new OneOperandOperation<Rational>("su2-casimir-doublet",rationals,ignored -> WittGaugeAction.model().su2Casimir(0)));
        rationals.addOneOperandOperation("su3-casimir-triplet",new OneOperandOperation<Rational>("su3-casimir-triplet",rationals,ignored -> WittGaugeAction.model().su3Casimir(2)));
        rationals.addOneOperandOperation("algebraic-depth",new OneOperandOperation<Rational>("algebraic-depth",rationals,ignored -> Rational.of(CosmologicalTransfers.algebraicDepth(8))));
        rationals.addOneOperandOperation("bsm1-doubling",new OneOperandOperation<Rational>("bsm1-doubling",rationals,ignored -> CosmologicalTransfers.algebraicDoubling()));
        MathTool tool=new MathTool("dynamical-sm");
        tool.addAlgebra(rationals);
        return tool;
    }
    public static IAlgebraFlow<Rational> flow(){
        MathTool tool=tool();
        @SuppressWarnings("unchecked") Algebra<Rational> rationals=(Algebra<Rational>)tool.getAlgebra(ALGEBRA);
        return new AlgebraFlow<Rational>(new ListAlgebraInput<Rational>(rationals,Collections.singletonList(Rational.ONE)),() -> tool,ALGEBRA);
    }
}
