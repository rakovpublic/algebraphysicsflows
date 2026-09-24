package mathematics;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import algebra.imp.MathTool;
import algebraflow.IAlgebraFlow;
import algebra.concrete.*;
import mathematics.calculus.Polynomial;
import mathematics.calculus.MultivariatePolynomial;
import mathematics.calculus.PolynomialMap;
import mathematics.calculus.PolynomialDifferentialForm;
import mathematics.calculus.PolynomialCell;
import mathematics.calculus.PolynomialChain;
import mathematics.core.MathFailure;
import operations.simple.*;
import operations.flat.*;
import mathematics.foundations.*;
import mathematics.linear.*;
import mathematics.numbers.*;
import mathematics.structures.FiniteCategory;
import mathematics.structures.FiniteFunctor;
import mathematics.structures.FiniteNaturalTransformation;
import mathematics.structures.FiniteEquivalence;
import mathematics.structures.FiniteAdjunction;
import mathematics.structures.FiniteCone;
import mathematics.structures.FiniteCocone;
import mathematics.structures.AbelianGroupType;
import mathematics.structures.PresentedAbelianGroup;
import mathematics.examples.ConcreteAlgebrasExample;
import mathematics.probability.FiniteMarkovKernel;
import mathematics.probability.FiniteDistribution;
import org.junit.Test;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import static org.junit.Assert.*;

public class ConcreteAlgebrasTest {
    @Test public void all781RegisteredOperationsReturnIndependentExpectedValues() {
        ConcreteMathematics math=new ConcreteMathematics();
        Map<String,String> expected=new HashMap<>();
        String integerMatrix="ZMatrix(2x2)[[2, 0], [0, 3]]",integerIdentity="ZMatrix(2x2)[[1, 0], [0, 1]]";
        String presented="PresentedAbelianGroup("+integerMatrix+")";
        String[] element=new String[6]; for(int i=0;i<element.length;i++) element[i]="AbelianElement(group="+presented+", smith=[0, "+i+"])";
        expected.put("PresentedAbelianGroupAlgebra",String.join("|",presented,"PresentedAbelianGroup(ZMatrix(2x1)[[6], [0]])",
                integerMatrix,"AbelianGroup(rank=0, torsion=[6])","2","2","true","6","false","true",
                "PresentedAbelianGroup(ZMatrix(4x4)[[2, 0, 0, 0], [0, 3, 0, 0], [0, 0, 3, 0], [0, 0, 0, 2]])","PresentedAbelianGroup(ZMatrix(0x0)[])"));
        expected.put("AbelianGroupElementAlgebra",String.join("|",element[5],element[5],element[4],element[4],"false",presented,"[0, 2]","[2, -2]",
                "false","true","3",element[0],"["+element[0]+", "+element[2]+", "+element[4]+"]","["+element[1]+", "+element[4]+"]",
                element[1],element[2],element[0],"["+element[3]+", "+element[2]+"]","["+element[1]+"]","["+String.join(", ",element)+"]","[0, 1]"));
        expected.put("IntegerVectorFamily","[6, 9]|[-2, -3]|[-2, -3]|[4, 6]|26|2|[2, 3]|[0, 0]|false|[2, 3]|[1, 2, 3]|[]");
        expected.put("IntegerMatrixFamily",String.join("|","ZMatrix(2x2)[[3, 0], [0, 4]]","ZMatrix(2x2)[[1, 0], [0, 2]]",
                integerMatrix,"ZMatrix(2x2)[[-2, 0], [0, -3]]",integerMatrix,"ZMatrix(2x2)[[4, 0], [0, 6]]","[8, 18]","false","2","2",
                "[[2, 0], [0, 3]]","[[2, 0], [0, 3]]","ZMatrix(2x2)[[0, 0], [0, 0]]",integerIdentity,integerIdentity,"[1, 6]",
                "ZMatrix(2x2)[[1, 0], [0, 6]]","[ZMatrix(2x2)[[1, 1], [3, 2]], ZMatrix(2x2)[[1, 0], [0, 6]], ZMatrix(2x2)[[-1, 3], [1, -2]]]",
                "[]","[[-2, 3], [6, -6]]","AbelianGroup(rank=0, torsion=[6])","2","0","true","[2, 2]","[[2, 2]]",
                "ZMatrix(2x2)[[1, -2], [0, 1]]","[[2, 0], [0, 3]]","ZMatrix(2x3)[[1, 2, 3], [2, 4, 6]]"));
        expected.put("AbelianGroupTypeAlgebra",String.join("|",
                "AbelianGroup(rank=3, torsion=[2, 12, 12])","AbelianGroup(rank=2, torsion=[2, 2, 6, 6, 12, 12])",
                "AbelianGroup(rank=2, torsion=[2, 2, 12, 12])","AbelianGroup(rank=0, torsion=[2, 6])",
                "AbelianGroup(rank=0, torsion=[2, 6, 6, 6])","false","1","1","2","false","false","false","false","6","6",
                "AbelianGroup(rank=0, torsion=[6])","AbelianGroup(rank=1, torsion=[])","[6]",
                "AbelianGroup(rank=2, torsion=[6, 6])","AbelianGroup(rank=6, torsion=[])",
                "AbelianGroup(rank=0, torsion=[6])","AbelianGroup(rank=0, torsion=[])","AbelianGroup(rank=1, torsion=[])"));
        String kernel="Kernel[1, 2]->[1, 2]{1=Distribution{1=1/2, 2=1/2}, 2=Distribution{1=1/4, 2=3/4}}";
        String kernelIdentity="Kernel[1, 2]->[1, 2]{1=Distribution{1=1}, 2=Distribution{2=1}}";
        String kernelPower="Kernel[1, 2]->[1, 2]{1=Distribution{1=3/8, 2=5/8}, 2=Distribution{1=5/16, 2=11/16}}";
        String stationary="Distribution{1=1/3, 2=2/3}";
        expected.put("FiniteMarkovAlgebra",String.join("|",kernel,"[1, 2]","[1, 2]","2","2","true","false",
                "Distribution{1=1/4, 2=3/4}","[Distribution{1=1/2, 2=1/2}, Distribution{1=1/4, 2=3/4}]",
                "Distribution{1=1/2, 2=1/2}","1/2","[[1/2, 1/2], [1/4, 3/4]]",kernel,
                "Kernel[1, 2, 3]->[1, 2, 3]{1=Distribution{2=1}, 2=Distribution{3=1}, 3=Distribution{1=1}}",
                "Function[1, 2]->[1, 2]{1=1, 2=2}","false",kernelIdentity,kernelIdentity,kernelIdentity,kernelPower,
                "[Distribution{1=1}, Distribution{1=1/2, 2=1/2}, Distribution{1=3/8, 2=5/8}]",
                "[[1, 2]]","[[1, 2]]","[]","[]","true","["+stationary+"]",stationary,"false",kernel,"true",
                "Kernel[1, 2]->[1, 2]{1=Distribution{1=1/2, 2=1/2}, 2=Distribution{2=1}}","[1, 1]","[2, 0]"));
        String px="Poly(Q^3){[1, 0, 0]=1}",py="Poly(Q^3){[0, 1, 0]=1}",pz="Poly(Q^3){[0, 0, 1]=1}";
        String pzero="Poly(Q^3){}",pone="Poly(Q^3){[0, 0, 0]=1}",pfirst="Poly(Q^3){[0, 0, 0]=1, [1, 0, 0]=1}";
        String identityMap="PolynomialMap["+px+", "+py+", "+pz+"]",cycleMap="PolynomialMap["+py+", "+pz+", "+px+"]";
        String cellA="Cell(point=[1, 2, 3])",cellB="Cell(point=[3, 2, 1])",chainA="Chain(Q^3, degree=0){"+cellA+"=1}";
        String cellProduct="Cell(point=[1, 2, 3, 3, 2, 1])",negativeChain="Chain(Q^3, degree=-1){}";
        expected.put("PolynomialCellAlgebra",String.join("|",cellProduct,"0","3","Cell(map="+identityMap+")",
                "PolynomialMap[Poly(Q^1){[1]=1}, Poly(Q^1){}, Poly(Q^1){}]",cellA,"[1, 2, 3]",
                "Cell(map=PolynomialMap[Poly(Q^1){[0]=1, [1]=2}, Poly(Q^1){[0]=2}, Poly(Q^1){[0]=3, [1]=-2}])",
                "Cell(point=[0, 0, 0])","Cell(point=[1, 0, 0])","[]","[[1, 2, 3]]","[1, 2, 3]","Cell(point=[2, 3, 1])","2","false"));
        expected.put("PolynomialChainAlgebra",String.join("|","Chain(Q^3, degree=0){"+cellA+"=1, "+cellB+"=1}",
                "Chain(Q^3, degree=0){"+cellA+"=1, "+cellB+"=-1}","Chain(Q^3, degree=0){"+cellA+"=-1}","Chain(Q^3, degree=0){"+cellA+"=2}",
                "Chain(Q^6, degree=0){"+cellProduct+"=1}",negativeChain,"Chain(Q^3, degree=0){Cell(point=[2, 3, 1])=1}","2","3","0","false","1",
                "["+cellA+"]","[1]","0","false",chainA,negativeChain,"Chain(Q^3, degree=0){}"));
        String form="Form(Q^3){1="+py+"}",formZero="Form(Q^3){}";
        expected.put("PolynomialDifferentialFormAlgebra",String.join("|",
                "Form(Q^3){1="+py+", 2="+px+"}","Form(Q^3){1="+py+", 2=Poly(Q^3){[1, 0, 0]=-1}}",
                "Form(Q^3){3=Poly(Q^3){[1, 1, 0]=1}}","Form(Q^3){1=Poly(Q^3){[0, 1, 0]=-1}}",
                "Form(Q^3){1=Poly(Q^3){[0, 1, 0]=2}}","Form(Q^3){1=Poly(Q^3){[0, 2, 0]=1}}",
                "Form(Q^3){3=Poly(Q^3){[0, 0, 0]=-1}}","Form(Q^3){2="+pz+"}","Form(Q^3){0=Poly(Q^3){[0, 2, 0]=1}}",
                "Form(Q^3){1="+pz+", 2="+py+"}","Form(Q^3){6="+py+"}","Form(Q^3){1=Poly(Q^3){[0, 1, 0]=-1}}",
                "3","1","1","[1]","["+form+"]","["+py+"]","[1]",formZero,"Exterior(Q^3){1=2}","false","false",pzero,pfirst,
                "Form(Q^3){0="+pfirst+"}","Form(Q^3){0=Poly(Q^3){[0, 0, 0]=2}, 1="+pone+", 3=Poly(Q^3){[0, 0, 0]=3}}",
                "Exterior(Q^3){1=2}","Form(Q^3){1="+px+", 2="+py+", 4="+pz+"}","PolynomialMap["+py+", "+pzero+", "+pzero+"]",
                formZero,"Form(Q^3){0="+pone+"}","Form(Q^3){7="+pone+"}","[Form(Q^3){1="+pone+"}, Form(Q^3){2="+pone+"}, Form(Q^3){4="+pone+"}]","2"));
        expected.put("RationalMultivariatePolynomialAlgebra",String.join("|",
                "Poly(Q^3){[0, 0, 0]=1, [0, 1, 0]=1, [1, 0, 0]=1}",
                "Poly(Q^3){[0, 0, 0]=1, [0, 1, 0]=-1, [1, 0, 0]=1}",
                "Poly(Q^3){[0, 1, 0]=1, [1, 1, 0]=1}","Poly(Q^3){[0, 0, 0]=-1, [1, 0, 0]=-1}",
                "Poly(Q^3){[0, 0, 0]=2, [1, 0, 0]=2}","3","1","2","false","false","4",pzero,
                "["+pone+", "+pzero+", "+pzero+"]","Poly(Q^3){[0, 0, 0]=3}","[1, 0, 0]",
                "[[0, 0, 0], [0, 0, 0], [0, 0, 0]]",pzero,"Poly(Q^3){[0, 0, 1]=1, [1, 0, 1]=1}",
                "Poly(Q^3){[0, 0, 0]=1, [1, 0, 0]=2, [2, 0, 0]=1}","["+pone+", "+px+"]","[1, 1]",pzero,pone,
                "Poly(Q^1){[0]=1, [1]=2, [2]=1}","Q[x][1, 2, 1]","1","6","["+px+", "+py+", "+pz+"]","3/2"));
        expected.put("RationalPolynomialMapAlgebra",String.join("|",
                "PolynomialMap[Poly(Q^3){[0, 1, 0]=1, [1, 0, 0]=1}, Poly(Q^3){[0, 0, 1]=1, [0, 1, 0]=1}, Poly(Q^3){[0, 0, 1]=1, [1, 0, 0]=1}]",
                "PolynomialMap[Poly(Q^3){[0, 1, 0]=-1, [1, 0, 0]=1}, Poly(Q^3){[0, 0, 1]=-1, [0, 1, 0]=1}, Poly(Q^3){[0, 0, 1]=1, [1, 0, 0]=-1}]",
                "PolynomialMap[Poly(Q^3){[1, 0, 0]=-1}, Poly(Q^3){[0, 1, 0]=-1}, Poly(Q^3){[0, 0, 1]=-1}]",
                "PolynomialMap[Poly(Q^3){[1, 0, 0]=2}, Poly(Q^3){[0, 1, 0]=2}, Poly(Q^3){[0, 0, 1]=2}]",
                cycleMap,"[3, 2, 1]","PolynomialMap["+pzero+", "+pzero+", "+pone+"]","["+px+", "+py+", "+pz+"]",pz,"3","3",
                "[[1, 0, 0], [0, 1, 0], [0, 0, 1]]","Poly(Q^3){[0, 0, 0]=3}","PolynomialMap["+pzero+", "+pzero+", "+pzero+"]",
                "PolynomialMap["+pfirst+"]",px,"PolynomialMap["+pone+", "+pzero+", "+pzero+"]",identityMap,
                "PolynomialMap[Poly(Q^3){[0, 0, 1]=3, [0, 1, 0]=2, [1, 0, 0]=1}, Poly(Q^3){[0, 0, 1]=6, [0, 1, 0]=4, [1, 0, 0]=2}]",
                "[[1, 0, 0], [0, 1, 0], [0, 0, 1]]","[0, 0, 0]","false","Poly(Q^3){[0, 0, 0]=1, [0, 1, 0]=1}"));
        String category12="Category(objects=[1, 2], arrows={1=(1,1), 2=(2,2)}, identities={1=1, 2=2}, composition={(1,1)=1, (2,2)=2})";
        String category123="Category(objects=[1, 2, 3], arrows={1=(1,1), 2=(2,2), 3=(3,3)}, identities={1=1, 2=2, 3=3}, composition={(1,1)=1, (2,2)=2, (3,3)=3})";
        String categoryEmpty="Category(objects=[], arrows={}, identities={}, composition={})";
        String swapFunctor="Functor(source="+category12+", target="+category12+", objects={1=2, 2=1}, arrows={1=2, 2=1})";
        String identityFunctor="Functor(source="+category12+", target="+category12+", objects={1=1, 2=2}, arrows={1=1, 2=2})";
        String cycleFunctor="Functor(source="+category123+", target="+category123+", objects={1=2, 2=3, 3=1}, arrows={1=2, 2=3, 3=1})";
        String emptyFunctor="Functor(source="+categoryEmpty+", target="+categoryEmpty+", objects={}, arrows={})";
        String identityTransformation="NaturalTransformation(source="+identityFunctor+", target="+identityFunctor+", components={1=1, 2=2})";
        String swapIdentityTransformation="NaturalTransformation(source="+swapFunctor+", target="+swapFunctor+", components={1=2, 2=1})";
        String emptyTransformation="NaturalTransformation(source="+emptyFunctor+", target="+emptyFunctor+", components={})";
        String swapEquivalence="Equivalence(forward="+swapFunctor+", backward="+swapFunctor+", unit={1=1, 2=2}, counit={1=1, 2=2})";
        String identityEquivalence="Equivalence(forward="+identityFunctor+", backward="+identityFunctor+", unit={1=1, 2=2}, counit={1=1, 2=2})";
        String emptyEquivalence="Equivalence(forward="+emptyFunctor+", backward="+emptyFunctor+", unit={}, counit={})";
        String swapAdjunction="Adjunction(left="+swapFunctor+", right="+swapFunctor+", unit={1=1, 2=2}, counit={1=1, 2=2})";
        String identityAdjunction="Adjunction(left="+identityFunctor+", right="+identityFunctor+", unit={1=1, 2=2}, counit={1=1, 2=2})";
        String emptyAdjunction="Adjunction(left="+emptyFunctor+", right="+emptyFunctor+", unit={}, counit={})";
        String constantTwo="Functor(source="+category12+", target="+category12+", objects={1=2, 2=2}, arrows={1=2, 2=2})";
        String emptyDiagram="Functor(source="+categoryEmpty+", target="+category12+", objects={}, arrows={})";
        String cone="Cone(diagram="+constantTwo+", vertex=2, legs={1=2, 2=2})";
        String coneTransformation="NaturalTransformation(source="+constantTwo+", target="+constantTwo+", components={1=2, 2=2})";
        expected.put("FiniteConeAlgebra",String.join("|",constantTwo,"2","2","Function[1, 2]->[1, 2]{1=2, 2=2}","[2, 2]",
                coneTransformation,"true","true","2","[2]",cone,cone,cone,"["+cone+"]",cone));
        String cocone="Cocone(diagram="+constantTwo+", vertex=2, legs={1=2, 2=2})";
        expected.put("FiniteCoconeAlgebra",String.join("|",constantTwo,"2","2","Function[1, 2]->[1, 2]{1=2, 2=2}","[2, 2]",
                coneTransformation,"true","true","2","[2]",cocone,cocone,cocone,"["+cocone+"]",cocone,cone,cocone));
        expected.put("FiniteAdjunctionAlgebra",String.join("|",swapAdjunction,swapAdjunction,category12,category12,
                swapFunctor,swapFunctor,identityTransformation,identityTransformation,"true",swapEquivalence,"false",identityAdjunction,
                swapAdjunction,swapAdjunction,swapAdjunction,"1","1","Function[2]->[1]{2=1}",emptyAdjunction));
        expected.put("FiniteEquivalenceAlgebra",String.join("|",swapEquivalence,swapEquivalence,swapEquivalence,category12,category12,
                swapFunctor,swapFunctor,identityTransformation,identityTransformation,"false",identityEquivalence,swapEquivalence,emptyEquivalence));
        expected.put("FiniteNaturalTransformationAlgebra",String.join("|",identityTransformation,identityTransformation,identityTransformation,
                identityFunctor,identityFunctor,"2","true","Function[1, 2]->[1, 2]{1=1, 2=2}","[1, 2]","[2]","true",
                swapIdentityTransformation,identityTransformation,identityTransformation,identityTransformation,emptyTransformation));
        expected.put("FiniteFunctorAlgebra",String.join("|",swapFunctor,swapFunctor,swapFunctor,category12,category12,"1","1",
                "true","true","true","true","true","Function[1, 2]->[1, 2]{1=2, 2=1}","Function[1, 2]->[1, 2]{1=2, 2=1}",
                "[2, 1]","[2, 1]","[1]","[1]","false",identityFunctor,cycleFunctor,emptyFunctor,constantTwo,emptyDiagram));
        expected.put("BooleanAlgebra","false|true|true|false|false|false|false|true");
        expected.put("NaturalSemiring","8|12|7|6|0|1");
        expected.put("IntegerRing","8|4|12|2|6|3|0|-6|6|3|true|false|[3, 0]|0|1");
        expected.put("RationalField","8|4|12|3|-6|1/6|true|false|[8, 4]|0|1");
        expected.put("RationalComplexField","(4)+(3)i|(-2)+(1)i|(1)+(7)i|(1/2)+(1/2)i|(-1)+(-2)i|(1)+(-2)i|5|(6)+(0)i|(0)+(0)i|(1)+(0)i");
        expected.put("RationalQuaternionAlgebra","Quaternion[3, 1, 4, 7]|Quaternion[-1, 3, 2, 1]|Quaternion[-11, 8, -3, 16]|Quaternion[1, 0, 1, 0]|Quaternion[1, 2/3, -1/3, 2/3]|Quaternion[-1, -2, -3, -4]|Quaternion[1, -2, -3, -4]|Quaternion[1/30, -1/15, -1/10, -2/15]|30|1|[2, 3, 4]|[1, 2, 3, 4]|Quaternion[2, 4, 6, 8]|false|false|Quaternion[6, 0, 0, 0]|6|Quaternion[1, 2, 0, 0]|(1)+(2)i|Quaternion[0, 1, 2, 3]|[1, 2, 3]|[-1, 2, 3]|[[-2/3, 2/15, 11/15], [2/3, -1/3, 2/3], [1/3, 14/15, 2/15]]|Quaternion[1, 0, 0, 1]|Quaternion[0, 0, 0, 0]|Quaternion[1, 0, 0, 0]|Quaternion[0, 1, 0, 0]|Quaternion[0, 0, 1, 0]|Quaternion[0, 0, 0, 1]");
        expected.put("RationalVectorSpace","[4, 6]|[-2, -2]|[-1, -2]|[2, 4]|[18, 24]|[[18, 24]]|11|[[2, 4]]|[[2, 4], [-2, -4]]|[0, 0]");
        expected.put("RationalMatrixAlgebra","[[3, 2], [3, 6]]|[[2, 4], [6, 8]]|[[-1, 2], [3, 2]]|[[-1, -2], [-3, -4]]|[[1, 3], [2, 4]]|[[-2, 1], [3/2, -1/2]]|-2|5|[[2, 4], [6, 8]]|[11, 25]|[-2, 5/2]|[[0, 0], [0, 0]]|[[1, 0], [0, 1]]|2");
        expected.put("RationalVectorFamily","[4, 4, 4]|[-2, 0, 2]|[-1, -2, -3]|[2, 4, 6]|10|3|[1, 2, 3]|[0, 0, 0]|[1, 2]|[1, 2]|[]");
        expected.put("RationalMatrixFamily","[[4, 4, 4], [2, 5, 6]]|[[-2, 0, 2], [2, 3, 6]]|[[4, 5], [8, 10]]|[[-1, -2, -3], [-2, -4, -6]]|[[1, 2], [2, 4], [3, 6]]|[[2, 4, 6], [4, 8, 12]]|[10, 20]|[[1, 2, 3], [0, 0, 0]]|1|2|[0]|[[-2, 1, 0], [-3, 0, 1]]|[[1, 2, 3]]|[[1, 2]]|[[1, 2, 3], [2, 4, 6]]|[[1, 2], [2, 4], [3, 6]]|2|3|false|[[1, 2], [3, 4]]|[[1, 2], [3, 4]]|[[0, 0, 0], [0, 0, 0]]|[[-2, 1], [3/2, -1/2]]|-2|5|[[1/70, 1/35], [1/35, 2/35], [3/70, 3/35]]|[[1/5, 2/5], [2/5, 4/5]]|[[1/14, 1/7, 3/14], [1/7, 2/7, 3/7], [3/14, 3/7, 9/14]]|[7/5, 14/5]|[1/10, 1/5, 3/10]|[-2/5, 1/5]|1/5");
        expected.put("RationalAffineSpaceAlgebra","Affine(particular=[1, 0, 0], directions=[[-2, 1, 0], [-3, 0, 1]])|[1, 0, 0]|[[-2, 1, 0], [-3, 0, 1]]|2|3|false|false|false|[-3, -1, 2]|false|Affine(particular=[7/5, 0, 0], directions=[[-2, 1, 0], [-3, 0, 1]])|[33/14, 5/7, -13/14]|[1/14, 1/7, 3/14]");
        expected.put("RationalTensorAlgebra","Tensor(shape=[2, 2], entries=[6, 8, 10, 12])|Tensor(shape=[2, 2], entries=[-4, -4, -4, -4])|Tensor(shape=[2, 2], entries=[5, 12, 21, 32])|Tensor(shape=[2, 2, 2, 2], entries=[5, 6, 7, 8, 10, 12, 14, 16, 15, 18, 21, 24, 20, 24, 28, 32])|Tensor(shape=[2, 2], entries=[-1, -2, -3, -4])|Tensor(shape=[2, 2], entries=[2, 4, 6, 8])|2|4|[2, 2]|[1, 2, 3, 4]|30|70|false|Tensor(shape=[2, 2], entries=[1, 3, 2, 4])|Tensor(shape=[], entries=[5])|Tensor(shape=[], entries=[6])|6|Tensor(shape=[3], entries=[1, 2, 3])|[1, 2, 3]|Tensor(shape=[2, 3], entries=[1, 2, 3, 2, 4, 6])|[[1, 2], [3, 4]]|Tensor(shape=[2, 2], entries=[0, 0, 0, 0])|Tensor(shape=[], entries=[1])");
        expected.put("RationalExteriorAlgebra","Exterior(Q^3){0=3, 1=1, 2=2, 3=3, 4=1}|Exterior(Q^3){0=1, 1=1, 2=-2, 3=3, 4=-1}|Exterior(Q^3){0=2, 1=1, 2=4, 3=5, 4=2, 5=1, 7=3}|Exterior(Q^3){0=-2, 1=-1, 3=-3}|Exterior(Q^3){0=4, 1=2, 3=6}|Exterior(Q^3){0=2, 1=-1, 3=3}|Exterior(Q^3){0=2, 1=1, 3=-3}|Exterior(Q^3){4=3, 6=1, 7=2}|3|3|[0, 1, 2]|[Exterior(Q^3){0=2}, Exterior(Q^3){1=1}, Exterior(Q^3){3=3}]|[2, 1, 3]|[0, 1, 3]|Exterior(Q^3){3=3}|Exterior(Q^3){0=3, 1=-6, 2=9}|2|14|false|false|2|2|[1, 2, 3]|Exterior(Q^3){1=1, 2=2, 4=3}|Exterior(Q^2){0=1, 1=7, 2=14}|Exterior(Q^6){}|Exterior(Q^6){0=1}|Exterior(Q^6){63=1}");
        expected.put("RationalPolynomialRing","Q[x][3, 3, 1]|Q[x][2, 5, 4, 1]|Q[x][-1, 1, 1]|Q[x][-1, -2, -1]|Q[x][2, 2]|9|Q[x][2, 1, 1, 1/3]|7/3|Q[x][0]|Q[x][1]|Q[x][0, 1]|Q[x][1]|Q[x][1, 1]|Q[x][1]|Q[x][9, 6, 1]|Q[x][1, 2, 1]|[Q[x][0, 1], Q[x][1]]|Q[x][2]|4|[0, 1, 4]");
        expected.put("PrimeField","0 (mod 5)|1 (mod 5)|1 (mod 5)|4 (mod 5)|2 (mod 5)|2 (mod 5)|0 (mod 5)|1 (mod 5)");
        expected.put("IntegerSetAlgebra","[1, 2, 3]|[1, 2]|[]|[3]|true|false|true|[1, 2]|[1]|2|[1, 2]|[[], [1], [2], [1, 2]]|[3]|[]|[1]|[2]|3|4|[1]|[2]");
        expected.put("RationalSampleAlgebra","Sample[1, 2, 3, 2, 4, 6]|3|2|2/3|1|Sample[-1, 0, 1]|4/3|2|Sample[2, 4, 6]|[1, 2, 3]|Sample[]");
        expected.put("FiniteProbabilityAlgebra","1|0|Distribution{1=1/4, 3=3/4}|[1, 3]|2|[1, 3]|false|Distribution{6=1}|5/2|3/4");
        expected.put("FiniteSimplicialAlgebra","Complex[[0], [1], [2], [0, 1], [0, 2], [1, 2], [0, 1, 2]]|Complex[[0], [1], [2], [0, 1], [0, 2], [1, 2]]|false|true|1|0|3|0|0|Complex[[0], [1], [2], [0, 1], [0, 2], [1, 2]]|[1, 1]|Complex[]|AbelianGroup(rank=0, torsion=[])|[AbelianGroup(rank=1, torsion=[]), AbelianGroup(rank=1, torsion=[])]|0|[1, 1]|[]|ZMatrix(3x0)[[], [], []]");
        expected.put("FiniteIntegerRelationAlgebra","Relation[(1,2), (2,3), (2,4), (3,5)]|Relation[]|Relation[(1,4), (2,5)]|Relation[(2,1), (3,2)]|Relation[(1,2), (2,3), (1,3)]|false|false|[1, 2]|[2, 3]|2|true|[2, 3]|[1, 2]|false|Relation[]|Relation[(1,1), (2,2)]");
        expected.put("RationalFunctionField","Q(x)(Q[x][1, 1, 1])/(Q[x][0, 1])|Q(x)(Q[x][1, -1, -1])/(Q[x][0, 1])|Q(x)(Q[x][1, 1])/(Q[x][0, 1])|Q(x)(Q[x][1])/(Q[x][0, 1, 1])|Q(x)(Q[x][1])/(Q[x][1, 1])|Q(x)(Q[x][-1])/(Q[x][0, 1])|Q(x)(Q[x][0, 1])/(Q[x][1])|Q(x)(Q[x][-1])/(Q[x][0, 0, 1])|1/2|Q[x][1]|Q[x][0, 1]|Q(x)(Q[x][1, 2, 1])/(Q[x][1])|false|Q(x)(Q[x][0])/(Q[x][1])|Q(x)(Q[x][1])/(Q[x][1])");
        expected.put("SymmetricGroup","Perm[2, 1, 0]|Perm[2, 0, 1]|false|3|1|0|0|Perm[2, 0, 1]|[2, 0, 1]|[Perm[1, 2, 0]]|Perm[0, 1, 2]|[Perm[0, 1, 2], Perm[0, 2, 1], Perm[1, 0, 2], Perm[1, 2, 0], Perm[2, 0, 1], Perm[2, 1, 0]]");
        expected.put("ResidueRing","0 (mod 6)|4 (mod 6)|5 (mod 6)|5 (mod 6)|1 (mod 6)|5 (mod 6)|true|false|5|0 (mod 6)|[5 (mod 6)]|1 (mod 6)|false|0 (mod 6)|1 (mod 6)|[0 (mod 6), 1 (mod 6), 2 (mod 6), 3 (mod 6), 4 (mod 6), 5 (mod 6)]");
        expected.put("FiniteIntegerFunctionAlgebra","Function[1, 2, 3]->[1, 2, 3]{1=3, 2=2, 3=1}|Function[1, 2, 3]->[1, 2, 3]{1=3, 2=1, 3=2}|[1, 2, 3]|[1, 2, 3]|[2, 3, 1]|Relation[(1,2), (2,3), (3,1)]|3|true|true|true|3|[2, 3, 1]|[1, 2, 3]|Function[1, 2, 3]->[1, 2, 3]{1=2, 2=3, 3=1}|[2, 3, 1]|[1]|false|Function[1, 2]->[1, 2]{1=1, 2=2}|Function[1, 2]->[2, 3]{1=2, 2=3}|Function[]->[]{}");
        expected.put("FiniteCategoryAlgebra","Category(objects=[1, 2], arrows={1=(1,1), 2=(2,2)}, identities={1=1, 2=2}, composition={(1,1)=1, (2,2)=2})|[1, 2]|[1, 2]|2|2|true|true|2|2|2|2|[2]|[2]|true|[2]|[1, 2]|false|Category(objects=[1, 2], arrows={1=(1,1), 2=(2,2)}, identities={1=1, 2=2}, composition={(1,1)=1, (2,2)=2})|Category(objects=[1, 2], arrows={0=(1,1), 1=(2,2)}, identities={1=0, 2=1}, composition={(0,0)=0, (1,1)=1})|Relation[(1,1), (2,2)]|[]|[]|Category(objects=[], arrows={}, identities={}, composition={})");
        String spectral="Q[x][6, -5, 1]|Q[x][6, -5, 1]|[[4, 1], [0, 5]]|[[9, 7], [0, 16]]|[2, 3]|[[1, 0]]|[[1, 0]]|1|true|[[[1, 1], [0, 1]], [[2, 0], [0, 3]]]|[[4, 5], [0, 9]]";
        expected.put("RationalMatrixAlgebra",expected.get("RationalMatrixAlgebra")+"|"+spectral);
        expected.put("RationalMatrixFamily",expected.get("RationalMatrixFamily")+"|"+spectral+"|[[0, -1], [1, -2]]");
        expected.put("RationalPolynomialRing",expected.get("RationalPolynomialRing")+"|[-1]|0");
        int count=0;
        for(ConcreteAlgebra<?> algebra : math.algebras()) {
            String[] values=expected.get(algebra.getClass().getSimpleName()).split("\\|",-1);
            assertEquals(algebra.operations().size(),values.length);
            int i=0;
            for(Map.Entry<String,OperationRegistration> entry : algebra.operations().entrySet())
                assertEquals(entry.getValue().id,values[i++],invokeRegistered(math,algebra,entry.getKey(),entry.getValue()));
            count+=i;
        }
        assertEquals(781,count);
    }
    @SuppressWarnings({"unchecked","rawtypes"})
    private String invokeRegistered(ConcreteMathematics math,ConcreteAlgebra<?> owner,String name,OperationRegistration entry) {
        Algebra source=math.mathTool.getAlgebra(entry.first.getAlgebraName());
        IAlgebraItem item=source.buildAlgebraItem(sample(math,source.getAlgebraName(),0));
        if(entry.id.equals("Mat(Z).inverse-unimodular")) item=source.buildAlgebraItem(new IntegerMatrix(new BigInteger[][]{
                {BigInteger.ONE,BigInteger.valueOf(2)},{BigInteger.ZERO,BigInteger.ONE}}));
        if(entry.id.equals("AbelianGroupType.order") || entry.id.equals("AbelianGroupType.exponent"))
            item=source.buildAlgebraItem(AbelianGroupType.cyclic(BigInteger.valueOf(6)));
        if(entry.id.equals("FiniteMarkov(Z).from-matrix")) item=source.buildAlgebraItem(((FiniteMarkovKernel)sample(math,"FiniteMarkov(Z)",0)).toMatrix());
        if(entry.id.equals("FiniteMarkov(Z).to-function")) item=source.buildAlgebraItem(sample(math,"FiniteMarkov(Z)",1));
        List<String> spectralNames=Arrays.asList("characteristic-polynomial","minimal-polynomial","evaluate-polynomial",
                "rational-eigenvalues","eigenspace-basis","generalized-eigenspace-basis","eigenvalue-multiplicity",
                "is-diagonalizable-over-q","diagonalize-over-q","pow");
        if((owner instanceof RationalMatrixAlgebra || owner instanceof RationalMatrixFamily) && spectralNames.contains(name))
            item=source.buildAlgebraItem(new RationalMatrix(new Rational[][]{{Rational.of(2),Rational.ONE},{Rational.ZERO,Rational.of(3)}}));
        if(Arrays.asList("PolynomialCell(Q).to-map","PolynomialCell(Q).lower-face","PolynomialCell(Q).upper-face").contains(entry.id))
            item=source.buildAlgebraItem(PolynomialCell.parameterized(new PolynomialMap(MultivariatePolynomial.variable(1,0),
                    MultivariatePolynomial.constant(1,Rational.ZERO),MultivariatePolynomial.constant(1,Rational.ZERO))));
        if(entry.id.equals("PolynomialForm(Q).integrate-unit-cube")) item=source.buildAlgebraItem(PolynomialDifferentialForm.volume(3).scale(Rational.of(2)));
        if(entry.id.equals("PolynomialForm(Q).to-polynomial")) item=source.buildAlgebraItem(PolynomialDifferentialForm.scalar((MultivariatePolynomial)sample(math,"Poly(Q)",0)));
        if(entry.id.equals("PolynomialForm(Q).to-exterior")) item=source.buildAlgebraItem(PolynomialDifferentialForm.fromExterior(new RationalExterior(3,Collections.singletonMap(1,Rational.of(2)))));
        if(entry.id.equals("Poly(Q).to-univariate")) item=source.buildAlgebraItem(MultivariatePolynomial.fromUnivariate(new Polynomial(Rational.ONE,Rational.of(2),Rational.ONE)));
        if(entry.id.equals("Poly(Q).to-rational")) item=source.buildAlgebraItem(MultivariatePolynomial.constant(3,Rational.of(6)));
        if(entry.id.equals("PolynomialMap(Q).to-polynomial")) item=source.buildAlgebraItem(new PolynomialMap(MultivariatePolynomial.variable(3,0)));
        if(entry.id.equals("Vec(Q).to-fixed")) item=source.buildAlgebraItem(new RationalVector(Rational.ONE,Rational.of(2)));
        if(entry.id.equals("Tensor(Q).to-scalar")) item=source.buildAlgebraItem(RationalTensor.scalar(Rational.of(6)));
        if(entry.id.equals("Tensor(Q).to-vector")) item=source.buildAlgebraItem(RationalTensor.fromVector(new RationalVector(Rational.ONE,Rational.of(2),Rational.of(3))));
        if(entry.id.equals("Exterior(Q).to-scalar")) item=source.buildAlgebraItem(RationalExterior.scalar(3,Rational.of(2)));
        if(entry.id.equals("Exterior(Q).to-vector")) item=source.buildAlgebraItem(RationalExterior.fromVector(new RationalVector(Rational.ONE,Rational.of(2),Rational.of(3))));
        if(entry.id.equals("H(Q).to-rational")) item=source.buildAlgebraItem(RationalQuaternion.scalar(Rational.of(6)));
        if(entry.id.equals("H(Q).to-complex")) item=source.buildAlgebraItem(new RationalQuaternion(Rational.ONE,Rational.of(2),Rational.ZERO,Rational.ZERO));
        if(entry.id.equals("H(Q).to-vector")) item=source.buildAlgebraItem(new RationalQuaternion(Rational.ZERO,Rational.ONE,Rational.of(2),Rational.of(3)));
        if(entry.id.equals("H(Q).from-rotation-matrix")) item=source.buildAlgebraItem(new RationalMatrix(new Rational[][]{
                {Rational.ZERO,Rational.of(-1),Rational.ZERO},{Rational.ONE,Rational.ZERO,Rational.ZERO},{Rational.ZERO,Rational.ZERO,Rational.ONE}}));
        if(Arrays.asList("Mat(Q).to-fixed","Mat(Q).inverse","Mat(Q).determinant","Mat(Q).trace").contains(entry.id))
            item=source.buildAlgebraItem(sample(math,"Mat2(Q)",0));
        if(entry.id.startsWith("FiniteCone.")) {
            FiniteCone cone=(FiniteCone)sample(math,"FiniteCone",0);
            if(source==math.functors.algebra()) item=source.buildAlgebraItem(cone.diagram);
            if(source==math.naturalTransformations.algebra()) item=source.buildAlgebraItem(cone.asTransformation());
        }
        if(entry.id.startsWith("FiniteCocone.")) {
            FiniteCocone cocone=(FiniteCocone)sample(math,"FiniteCocone",0);
            if(source==math.functors.algebra()) item=source.buildAlgebraItem(cocone.diagram);
            if(source==math.naturalTransformations.algebra()) item=source.buildAlgebraItem(cocone.asTransformation());
        }
        if(entry.id.equals("FiniteCategory.from-preorder")) item=source.buildAlgebraItem(new FiniteRelation<>(math.integers.algebra(),math.integers.algebra(),
                FiniteSet.of(new Pair<>(BigInteger.ONE,BigInteger.ONE),new Pair<>(BigInteger.valueOf(2),BigInteger.valueOf(2)))));
        String alias=entry.alias;
        Object operation=entry.operation;
        if(operation instanceof IOneOperandOperation) return item.performOneOperandOperation(alias).perform().getResult().toString();
        if(operation instanceof ITransferOperation) return item.performAlgebraTransfer(alias).perform().getResult().toString();
        Object second=entry.second==null?null:sample(math,entry.second.getAlgebraName(),1);
        if(Arrays.asList("AbelianGroupElement.project","AbelianGroupElement.from-smith","AbelianGroupElement.reduce").contains(entry.id))
            second=new IntegerVector(BigInteger.ONE,BigInteger.valueOf(2));
        if(entry.id.equals("FiniteMarkov(Z).from-matrix")) second=new Pair<>(FiniteSet.of(BigInteger.ONE,BigInteger.valueOf(2)),FiniteSet.of(BigInteger.ONE,BigInteger.valueOf(2)));
        if(entry.id.equals("FiniteMarkov(Z).reverse") || entry.id.equals("FiniteMarkov(Z).is-reversible")) {
            Map<BigInteger,Rational> stationaryMasses=new LinkedHashMap<>();
            stationaryMasses.put(BigInteger.ONE,Rational.of(1,3)); stationaryMasses.put(BigInteger.valueOf(2),Rational.of(2,3));
            second=new FiniteDistribution<>(math.integers.algebra(),stationaryMasses);
        }
        if(Arrays.asList("FiniteMarkov(Z).absorbing-on","FiniteMarkov(Z).hitting-probabilities","FiniteMarkov(Z).mean-hitting-times").contains(entry.id))
            second=FiniteSet.of(BigInteger.valueOf(2));
        if(entry.id.equals("Mat(Q).evaluate-at-matrix") || entry.id.equals("Mat2(Q).evaluate-at-matrix"))
            second=new RationalMatrix(new Rational[][]{{Rational.of(2),Rational.ONE},{Rational.ZERO,Rational.of(3)}});
        if(entry.id.equals("PolynomialCell(Q).evaluate")) second=new RationalVector();
        if(entry.id.equals("PolynomialCell(Q).lower-face") || entry.id.equals("PolynomialCell(Q).upper-face")) second=BigInteger.ZERO;
        if(entry.id.equals("PolynomialCell(Q).integrate") || entry.id.equals("PolynomialChain(Q).integrate"))
            second=PolynomialDifferentialForm.scalar((MultivariatePolynomial)sample(math,"Poly(Q)",0));
        if(entry.id.equals("Mat(Q).multiply")) second=new RationalMatrix(new Rational[][]{{Rational.ONE,Rational.ZERO},{Rational.ZERO,Rational.ONE},{Rational.ONE,Rational.ONE}});
        if(entry.id.equals("Affine(Q).solve")) second=new RationalVector(Rational.ONE,Rational.of(2));
        if(entry.id.equals("Affine(Q).at")) second=new RationalVector(Rational.of(-1),Rational.of(2));
        if(Arrays.asList("Mat(Q).project-column","Mat(Q).least-squares-minimum-norm","Mat(Q).least-squares-residual",
                "Mat(Q).least-squares-error","Affine(Q).least-squares").contains(entry.id)) second=new RationalVector(Rational.ONE,Rational.of(3));
        if(entry.first==math.adjunctions.algebra() && entry.second==math.categories.labelPairs) second=new Pair<>(BigInteger.ONE,BigInteger.valueOf(2));
        if(entry.id.equals("Q[x].divide-exact")) second=new Polynomial(Rational.ONE,Rational.ONE);
        if(entry.flat) {
            List<IAlgebraItem> results;
            if(operation instanceof IOneOperandFlatOperation) results=item.performOneOperandFlatOperation(alias);
            else if(operation instanceof ITransferFlatOperation) results=item.performAlgebraFlatTransfer(alias);
            else if(operation instanceof IFlatOperation) results=item.performFlatOperation(alias,second);
            else if(operation instanceof ICustomMemberFlatOperation) results=item.performCustomMemberFlatOperation(alias,second);
            else if(operation instanceof ILeftProjectionFlatOperation) results=item.performLeftProjectionFlatOperation(alias,second);
            else if(operation instanceof ICustomResultFlatOperation) results=item.performCustomResultFlatOperation(alias,second);
            else results=item.performUnsafeFlatOperation(alias,second);
            List<Object> values=new ArrayList<>();
            for(IAlgebraItem result : results) values.add(result.perform().getResult());
            return values.toString();
        }
        IAlgebraItem result;
        if(operation instanceof IOperation) result=item.performOperation(alias,second);
        else if(operation instanceof ICustomResultOperation) result=item.performCustomResultOperation(alias,second);
        else if(operation instanceof ICustomMemberOperation) result=item.performCustomMemberOperation(alias,second);
        else if(operation instanceof ILeftProjectionOperation) result=item.performLeftProjectionOperation(alias,second);
        else result=item.performUnsafeOperation(alias,second);
        return result.perform().getResult().toString();
    }
    private Object sample(ConcreteMathematics math,String domain,int index) {
        switch(domain) {
            case "PresentedAbelianGroup": return new PresentedAbelianGroup(new IntegerMatrix(new BigInteger[][]{
                    {BigInteger.valueOf(index==0?2:3),BigInteger.ZERO},{BigInteger.ZERO,BigInteger.valueOf(index==0?3:2)}}));
            case "AbelianGroupElement": return ((PresentedAbelianGroup)sample(math,"PresentedAbelianGroup",0))
                    .fromSmith(new IntegerVector(BigInteger.ZERO,BigInteger.valueOf(index==0?2:3)));
            case "Vec(Z)": return new IntegerVector(BigInteger.valueOf(index==0?2:4),BigInteger.valueOf(index==0?3:6));
            case "Mat(Z)": return index==0?new IntegerMatrix(new BigInteger[][]{{BigInteger.valueOf(2),BigInteger.ZERO},{BigInteger.ZERO,BigInteger.valueOf(3)}}):IntegerMatrix.identity(2);
            case "AbelianGroupType": return index==0?new AbelianGroupType(BigInteger.ONE,Collections.singletonList(BigInteger.valueOf(6)))
                    :new AbelianGroupType(BigInteger.valueOf(2),Arrays.asList(BigInteger.valueOf(4),BigInteger.valueOf(12)));
            case "FiniteMarkov(Z)": {
                FiniteSet<BigInteger> labels=FiniteSet.of(BigInteger.ONE,BigInteger.valueOf(2));
                return index==1?FiniteMarkovKernel.identity(math.integers.algebra(),labels):math.markovKernels.fromMatrix(new RationalMatrix(new Rational[][]{
                        {Rational.of(1,2),Rational.of(1,2)},{Rational.of(1,4),Rational.of(3,4)}}),labels,labels);
            }
            case "ZxZ.markov": return new Pair<>(BigInteger.ONE,BigInteger.valueOf(2));
            case "FiniteDistribution(Z)xN.markov": return new Pair<>(new FiniteDistribution<>(math.integers.algebra(),Collections.singletonMap(BigInteger.ONE,Rational.ONE)),BigInteger.valueOf(2));
            case "Unit": return Unit.INSTANCE;
            case "FiniteComplex": return new mathematics.topology.FiniteSimplicialComplex(index==0
                    ?Arrays.asList(FiniteSet.of(0,1),FiniteSet.of(1,2),FiniteSet.of(0,2))
                    :Collections.singletonList(FiniteSet.of(0,1,2)));
            case "Boolean": return index==0;
            case "N": case "Z": return BigInteger.valueOf(index==0?6:2);
            case "Q": return Rational.of(index==0?6:2);
            case "Q(i)": return index==0?new RationalComplex(Rational.ONE,Rational.of(2)):new RationalComplex(Rational.of(3),Rational.ONE);
            case "H(Q)": return index==0?new RationalQuaternion(Rational.ONE,Rational.of(2),Rational.of(3),Rational.of(4))
                    :new RationalQuaternion(Rational.of(2),Rational.of(-1),Rational.ONE,Rational.of(3));
            case "Q^2": return index==0?new RationalVector(Rational.ONE,Rational.of(2)):new RationalVector(Rational.of(3),Rational.of(4));
            case "Vec(Q)": return index==0?new RationalVector(Rational.ONE,Rational.of(2),Rational.of(3)):new RationalVector(Rational.of(3),Rational.of(2),Rational.ONE);
            case "PolynomialCell(Q)": return PolynomialCell.point((RationalVector)sample(math,"Vec(Q)",index));
            case "PolynomialChain(Q)": return PolynomialChain.of((PolynomialCell)sample(math,"PolynomialCell(Q)",index));
            case "Tensor(Q)": return new RationalTensor(new int[]{2,2},Rational.of(1+4*index),Rational.of(2+4*index),Rational.of(3+4*index),Rational.of(4+4*index));
            case "NxN.tensor-axes": return new Pair<>(BigInteger.ZERO,BigInteger.ONE);
            case "Exterior(Q)": {
                Map<Integer,Rational> terms=new TreeMap<>();
                if(index==0) { terms.put(0,Rational.of(2)); terms.put(1,Rational.ONE); terms.put(3,Rational.of(3)); }
                else { terms.put(0,Rational.ONE); terms.put(2,Rational.of(2)); terms.put(4,Rational.ONE); }
                return new RationalExterior(3,terms);
            }
            case "Mat(Q)": return new RationalMatrix(index==0
                    ?new Rational[][]{{Rational.ONE,Rational.of(2),Rational.of(3)},{Rational.of(2),Rational.of(4),Rational.of(6)}}
                    :new Rational[][]{{Rational.of(3),Rational.of(2),Rational.ONE},{Rational.ZERO,Rational.ONE,Rational.ZERO}});
            case "Affine(Q)": return ((RationalMatrix)sample(math,"Mat(Q)",0)).solve(new RationalVector(Rational.of(index==0?1:2),Rational.of(index==0?2:4)));
            case "Mat2(Q)": return new RationalMatrix(index==0
                    ?new Rational[][] {{Rational.ONE,Rational.of(2)},{Rational.of(3),Rational.of(4)}}
                    :new Rational[][] {{Rational.of(2),Rational.ZERO},{Rational.ZERO,Rational.of(2)}});
            case "Q[x]": return index==0?new Polynomial(Rational.ONE,Rational.of(2),Rational.ONE):new Polynomial(Rational.of(2),Rational.ONE);
            case "Poly(Q)": return index==0?MultivariatePolynomial.constant(3,Rational.ONE).add(MultivariatePolynomial.variable(3,0)):MultivariatePolynomial.variable(3,1);
            case "PolynomialForm(Q)": return new PolynomialDifferentialForm(3,Collections.singletonMap(index==0?1:2,MultivariatePolynomial.variable(3,index==0?1:0)));
            case "PolynomialMap(Q)": return index==0?PolynomialMap.identity(3):new PolynomialMap(
                    MultivariatePolynomial.variable(3,1),MultivariatePolynomial.variable(3,2),MultivariatePolynomial.variable(3,0));
            case "FiniteSet(Z)": return index==0?FiniteSet.of(BigInteger.ONE,BigInteger.valueOf(2)):FiniteSet.of(BigInteger.ONE,BigInteger.valueOf(2),BigInteger.valueOf(3));
            case "Sample(Q)": return index==0?mathematics.statistics.RationalSample.of(Rational.ONE,Rational.of(2),Rational.of(3)):mathematics.statistics.RationalSample.of(Rational.of(2),Rational.of(4),Rational.of(6));
            case "FiniteDistribution(Z)":
                Map<BigInteger,Rational> masses=new LinkedHashMap<>();
                if(index==0) { masses.put(BigInteger.ONE,Rational.of(1,4)); masses.put(BigInteger.valueOf(3),Rational.of(3,4)); }
                else masses.put(BigInteger.ONE,Rational.ONE);
                return new mathematics.probability.FiniteDistribution<>(math.integers.algebra(),masses);
            case "Q(x)": return index==0
                    ?new mathematics.calculus.RationalFunction(Polynomial.ONE,new Polynomial(Rational.ZERO,Rational.ONE))
                    :mathematics.calculus.RationalFunction.of(new Polynomial(Rational.ONE,Rational.ONE));
            case "S3": return index==0?new mathematics.structures.Permutation(1,2,0):new mathematics.structures.Permutation(1,0,2);
            case "QxQ.bounds": return new Pair<>(Rational.ZERO,Rational.ONE);
            case "QxN.iteration": return new Pair<>(Rational.ZERO,BigInteger.valueOf(2));
            case "ZxZ.relation": return new Pair<>(BigInteger.ONE,BigInteger.valueOf(2));
            case "ZxZ.category": return new Pair<>(BigInteger.valueOf(2),BigInteger.valueOf(2));
            case "FiniteEquivalence": return FiniteEquivalence.fromFunctor((FiniteFunctor)sample(math,"FiniteFunctor",index));
            case "FiniteAdjunction": return FiniteAdjunction.fromEquivalence((FiniteEquivalence)sample(math,"FiniteEquivalence",index));
            case "FiniteCone":
                FiniteCategory coneCategory=FiniteCategory.discrete(FiniteSet.of(BigInteger.ONE,BigInteger.valueOf(2)));
                Map<BigInteger,BigInteger> coneLegs=new LinkedHashMap<>();
                coneLegs.put(BigInteger.ONE,BigInteger.valueOf(2)); coneLegs.put(BigInteger.valueOf(2),BigInteger.valueOf(2));
                return new FiniteCone(FiniteFunctor.constant(coneCategory,coneCategory,BigInteger.valueOf(2)),BigInteger.valueOf(2),coneLegs);
            case "FiniteCocone": return FiniteCocone.fromOpposite((FiniteCone)sample(math,"FiniteCone",index));
            case "FiniteNaturalTransformation": return FiniteNaturalTransformation.identity(FiniteFunctor.identity(
                    FiniteCategory.discrete(FiniteSet.of(BigInteger.ONE,BigInteger.valueOf(2)))));
            case "FiniteFunctor":
                FiniteCategory category=FiniteCategory.discrete(FiniteSet.of(BigInteger.ONE,BigInteger.valueOf(2)));
                Map<BigInteger,BigInteger> functorMap=new LinkedHashMap<>();
                functorMap.put(BigInteger.ONE,BigInteger.valueOf(index==0?2:1));
                functorMap.put(BigInteger.valueOf(2),BigInteger.valueOf(index==0?1:2));
                return new FiniteFunctor(category,category,functorMap,functorMap);
            case "FiniteCategory": return mathematics.structures.FiniteCategory.discrete(index==0?FiniteSet.of(BigInteger.ONE,BigInteger.valueOf(2))
                    :FiniteSet.of(BigInteger.ONE,BigInteger.valueOf(2),BigInteger.valueOf(3)));
            case "FiniteSet(Z)xFiniteSet(Z).function": return new Pair<>(FiniteSet.of(BigInteger.ONE,BigInteger.valueOf(2)),FiniteSet.of(BigInteger.valueOf(2),BigInteger.valueOf(3)));
            case "FiniteFunction(Z,Z)":
                FiniteSet<BigInteger> points=FiniteSet.of(BigInteger.ONE,BigInteger.valueOf(2),BigInteger.valueOf(3));
                Map<BigInteger,BigInteger> mapping=new LinkedHashMap<>();
                mapping.put(BigInteger.ONE,BigInteger.valueOf(2));
                mapping.put(BigInteger.valueOf(2),BigInteger.valueOf(index==0?3:1));
                mapping.put(BigInteger.valueOf(3),BigInteger.valueOf(index==0?1:3));
                return math.integerFunctions.member(points,points,mapping);
            case "FiniteRelation(Z,Z)": return new FiniteRelation<>(math.integers.algebra(),math.integers.algebra(),index==0
                    ?FiniteSet.of(new Pair<>(BigInteger.ONE,BigInteger.valueOf(2)),new Pair<>(BigInteger.valueOf(2),BigInteger.valueOf(3)))
                    :FiniteSet.of(new Pair<>(BigInteger.valueOf(2),BigInteger.valueOf(4)),new Pair<>(BigInteger.valueOf(3),BigInteger.valueOf(5))));
            case "Z/6Z": return new ModularInteger(index==0?5:1,6);
            case "Z/5Z": return new ModularInteger(index==0?3:2,5);
            default: throw new AssertionError("Missing test operand for "+domain);
        }
    }
    @Test public void runtimeRegistrationsMatchTheCoverageManifest() throws Exception {
        StringBuilder expected=new StringBuilder();
        try(BufferedReader input=new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/mathematics/concrete-catalog.tsv")),"UTF-8"))) {
            String line; while((line=input.readLine())!=null) expected.append(line).append('\n');
        }
        assertEquals(expected.toString(),ConcreteAlgebrasExample.catalogManifest(new ConcreteMathematics()));
    }
    @Test public void registersConcreteLegacyAlgebrasAndKeepsInstancesIsolated() throws Exception {
        ConcreteMathematics first=new ConcreteMathematics(),second=new ConcreteMathematics();
        for(ConcreteAlgebra<?> algebra : first.algebras()) {
            assertSame(algebra.algebra(),first.mathTool.getAlgebra(algebra.algebra().getAlgebraName()));
            assertFalse(algebra.operations().isEmpty());
            assertFalse(algebra.laws().isEmpty());
            assertNotSame(algebra.algebra(),second.mathTool.getAlgebra(algebra.algebra().getAlgebraName()));
        }
        assertTrue(first.integers.algebra().hasOperation("add"));
        assertTrue(first.rationals.algebra().hasFlatOperation("add-subtract"));
        assertThrows(IllegalArgumentException.class,() -> second.integers.register(first.mathTool));
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream output=new ObjectOutputStream(bytes)) { output.writeObject(first.mathTool); }
        MathTool restored;
        try(ObjectInputStream input=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(MathTool)input.readObject(); }
        @SuppressWarnings("unchecked") Algebra<Rational> q=(Algebra<Rational>)restored.getAlgebra("Q");
        assertEquals(Rational.of(5,6),q.buildAlgebraItem(Rational.of(1,2)).performOperation("add",Rational.of(1,3)).perform().getResult());
    }
    @Test public void naturalIntegerRationalBooleanFlowUsesMathToolRegistrations() {
        ConcreteMathematics m=new ConcreteMathematics();
        IAlgebraFlow<Boolean> flow=m.flow(m.naturals,Arrays.asList(BigInteger.ONE,BigInteger.valueOf(2)))
                .performOperation("add",BigInteger.ONE)
                .<BigInteger>performAlgebraTransfer("to-integer")
                .performOperation("subtract",BigInteger.valueOf(4))
                .<Rational>performAlgebraTransfer("to-rational")
                .performOperation("divide",Rational.of(2))
                .<Boolean>performCustomResultOperation("greater",Rational.of(-1))
                .performOneOperandOperation("not");
        assertEquals(Arrays.asList("true","false"),flow.collect());
        assertEquals(Arrays.asList("true","false"),flow.collect());
        assertNull(m.naturals.algebra().buildAlgebraItem(BigInteger.valueOf(-1)));
        assertThrows(exceptions.NotMemberException.class,() -> m.flow(m.naturals,Collections.singletonList(BigInteger.valueOf(-1))));
        assertEquals(Rational.of(3,2),m.integers.algebra().buildAlgebraItem(BigInteger.valueOf(3))
                .<Rational>performCustomResultOperation("divide-rational",BigInteger.valueOf(2)).perform().getResult());
        assertEquals(Arrays.asList("-2","-1"),m.flow(m.integers,Collections.singletonList(BigInteger.valueOf(-7)))
                .performFlatOperation("quotient-remainder",BigInteger.valueOf(3)).collect());
    }
    @Test public void rationalFieldLawsHoldForDeterministicSamplesThroughRegisteredOperations() {
        ConcreteMathematics m=new ConcreteMathematics();
        Algebra<Rational> q=m.rationals.algebra();
        Random random=new Random(1921);
        for(int i=0;i<120;i++) {
            Rational a=Rational.of(random.nextInt(101)-50,random.nextInt(12)+1);
            Rational b=Rational.of(random.nextInt(101)-50,random.nextInt(12)+1);
            Rational c=Rational.of(random.nextInt(101)-50,random.nextInt(12)+1);
            Rational sum=q.buildAlgebraItem(b).performOperation("add",c).perform().getResult();
            Rational left=q.buildAlgebraItem(a).performOperation("multiply",sum).perform().getResult();
            Rational right=q.buildAlgebraItem(a).performOperation("multiply",b)
                    .performOperation("add",q.buildAlgebraItem(a).performOperation("multiply",c).perform().getResult()).perform().getResult();
            assertEquals(left,right);
            if(a.signum()!=0) assertEquals(Rational.ONE,q.buildAlgebraItem(a).performOperation("divide",a).perform().getResult());
        }
        assertEquals(Arrays.asList("2","2"),m.flow(m.rationals,Collections.singletonList(Rational.of(2)))
                .performFlatOperation("add-subtract",Rational.ZERO).collect());
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,
                () -> q.buildAlgebraItem(Rational.ONE).performOperation("divide",Rational.ZERO).perform()).kind());
    }
    @Test public void primeFieldsAreClosedAndRejectWrongModuliAndCompositeParameters() {
        ConcreteMathematics m=new ConcreteMathematics(2,5,7);
        for(PrimeField field : m.primeFields) {
            for(int a=0;a<field.prime;a++) for(int b=0;b<field.prime;b++) {
                IAlgebraItem<ModularInteger> member=field.algebra().buildAlgebraItem(field.member(a));
                assertEquals(field.member(a+b),member.performOperation("add",field.member(b)).perform().getResult());
                assertEquals(field.member(a*b),member.performOperation("multiply",field.member(b)).perform().getResult());
                if(b!=0) assertEquals(field.member(a),member.performOperation("divide",field.member(b)).performOperation("multiply",field.member(b)).perform().getResult());
            }
            assertThrows(MathFailure.class,() -> field.algebra().buildAlgebraItem(field.one()).performOperation("divide",field.zero()).perform());
        }
        assertNull(m.primeFields.get(0).algebra().buildAlgebraItem(new ModularInteger(1,7)));
        assertThrows(exceptions.NotMemberException.class,() -> m.primeFields.get(0).algebra().buildAlgebraItem(new ModularInteger(1,5)).performOperation("add",new ModularInteger(1,7)));
        assertThrows(MathFailure.class,() -> new PrimeField(m.unit,9));
        assertThrows(IllegalArgumentException.class,() -> new ConcreteMathematics(2,5,5));
    }
    @Test public void vectorActionsUseDifferentOperandClassesAndFlatResultsStayInTheVectorDomain() {
        ConcreteMathematics m=new ConcreteMathematics();
        RationalVector v=new RationalVector(Rational.ONE,Rational.of(2));
        assertEquals(Arrays.asList("[6, 12]"),m.flow(m.vectors,Collections.singletonList(v))
                .performCustomMemberOperation("scale",Rational.of(2))
                .performFlatCustomMemberOperation("scale-flat",Rational.of(3)).collect());
        assertEquals(Arrays.asList("5","-5"),m.flow(m.vectors,Collections.singletonList(v))
                .performFlatCustomMemberOperation("scale-signs",Rational.ONE)
                .<Rational>performCustomResultOperation("dot",v)
                .collect());
        // Test actual opposite-order scalar action through a Q -> Q^2 flow.
        assertEquals(Collections.singletonList("[3, 6]"),m.flow(m.rationals,Collections.singletonList(Rational.of(3)))
                .performLeftProjectionOperation("Q^2.scale-left",v).collect());
        assertNull(m.vectors.algebra().buildAlgebraItem(new RationalVector(Rational.ONE)));
    }
    @Test public void matrixAndPolynomialOperationsCrossDomainsWithExactResults() {
        ConcreteMathematics m=new ConcreteMathematics();
        RationalMatrix a=new RationalMatrix(new Rational[][] {{Rational.ONE,Rational.of(2)},{Rational.of(3),Rational.of(4)}});
        assertEquals(Collections.singletonList("-2"),m.flow(m.matrices,Collections.singletonList(a)).<Rational>performAlgebraTransfer("determinant").collect());
        assertEquals(RationalMatrix.identity(2),m.matrices.algebra().buildAlgebraItem(a).performOneOperandOperation("inverse").performOperation("multiply",a).perform().getResult());
        RationalVector v=new RationalVector(Rational.ONE,Rational.of(2));
        assertEquals(Collections.singletonList("[5, 11]"),m.flow(m.matrices,Collections.singletonList(a)).performLeftProjectionOperation("apply",v).collect());
        RationalMatrix b=new RationalMatrix(new Rational[][] {{Rational.ZERO,Rational.ONE},{Rational.ONE,Rational.ZERO}});
        assertNotEquals(m.matrices.algebra().buildAlgebraItem(a).performOperation("multiply",b).perform().getResult(),m.matrices.algebra().buildAlgebraItem(b).performOperation("multiply",a).perform().getResult());
        Polynomial square=new Polynomial(Rational.ZERO,Rational.ZERO,Rational.ONE);
        assertEquals(Collections.singletonList("6"),m.flow(m.polynomials,Collections.singletonList(square))
                .performOneOperandOperation("derivative").performLeftProjectionOperation("evaluate",Rational.of(3)).collect());
        assertEquals(Collections.singletonList("1/3"),m.flow(m.polynomials,Collections.singletonList(square))
                .<Rational,Pair<Rational,Rational>>performAlgebraUnsafe("integrate",new Pair<>(Rational.ZERO,Rational.ONE)).collect());
        assertEquals(square,m.polynomials.algebra().buildAlgebraItem(square).performCustomMemberOperation("primitive",Rational.of(7))
                .performOneOperandOperation("derivative").perform().getResult());
    }
    @Test public void constantsBooleanLawsAndComplexArithmeticAreRegistered() {
        ConcreteMathematics m=new ConcreteMathematics();
        assertEquals(Rational.ZERO,m.unit.buildAlgebraItem(Unit.INSTANCE).<Rational>performAlgebraTransfer("Q.zero").perform().getResult());
        for(boolean a : new boolean[]{false,true}) for(boolean b : new boolean[]{false,true}) {
            Boolean notAnd=m.booleans.algebra().buildAlgebraItem(a).performOperation("and",b).performOneOperandOperation("not").perform().getResult();
            Boolean deMorgan=m.booleans.algebra().buildAlgebraItem(!a).performOperation("or",!b).perform().getResult();
            assertEquals(notAnd,deMorgan);
        }
        RationalComplex i=new RationalComplex(Rational.ZERO,Rational.ONE);
        assertEquals(new RationalComplex(Rational.of(-1),Rational.ZERO),m.complexRationals.algebra().buildAlgebraItem(i).performOperation("multiply",i).perform().getResult());
        assertEquals(Rational.ONE,m.complexRationals.algebra().buildAlgebraItem(i).<Rational>performAlgebraTransfer("norm-squared").perform().getResult());
        assertEquals(Collections.singletonList("(2)+(0)i"),m.flow(m.rationals,Collections.singletonList(Rational.of(2)))
                .<RationalComplex>performAlgebraTransfer("Q(i).embed-rational").collect());
    }
    @Test public void rankSolveAndHigherDerivativesUseNativeOperationsAndRespectTheirDomains() {
        ConcreteMathematics m=new ConcreteMathematics();
        RationalMatrix a=new RationalMatrix(new Rational[][] {{Rational.ZERO,Rational.of(2)},{Rational.of(3),Rational.ONE}});
        RationalVector rhs=new RationalVector(Rational.of(4),Rational.of(5));
        IAlgebraItem<RationalVector> solution=m.matrices.algebra().buildAlgebraItem(a).performLeftProjectionOperation("solve",rhs);
        assertSame(m.vectors.algebra(),solution.getAlgebra());
        assertEquals(new RationalVector(Rational.ONE,Rational.of(2)),solution.perform().getResult());
        assertEquals(rhs,a.multiply(solution.getResult()));
        RationalMatrix singular=new RationalMatrix(new Rational[][] {{Rational.ONE,Rational.of(2)},{Rational.of(2),Rational.of(4)}});
        assertEquals(Arrays.asList("2","1","0"),m.flow(m.matrices,Arrays.asList(a,singular,
                new RationalMatrix(new Rational[][] {{Rational.ZERO,Rational.ZERO},{Rational.ZERO,Rational.ZERO}})))
                .<BigInteger>performAlgebraTransfer("rank").collect());
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,
                () -> m.matrices.algebra().buildAlgebraItem(singular).performLeftProjectionOperation("solve",rhs)).kind());
        Polynomial p=new Polynomial(Rational.ONE,Rational.of(2),Rational.of(3));
        assertEquals(p,m.polynomials.algebra().buildAlgebraItem(p).performCustomMemberOperation("derivative-order",BigInteger.ZERO).getResult());
        assertEquals(Collections.singletonList("Q[x][6]"),m.flow(m.polynomials,Collections.singletonList(p))
                .performCustomMemberOperation("derivative-order",BigInteger.valueOf(2)).collect());
        assertEquals(new Polynomial(Rational.ZERO),m.polynomials.algebra().buildAlgebraItem(p)
                .performCustomMemberOperation("derivative-order",BigInteger.ONE.shiftLeft(100)).getResult());
        assertThrows(exceptions.NotMemberException.class,() -> m.polynomials.algebra().buildAlgebraItem(p)
                .performCustomMemberOperation("derivative-order",BigInteger.valueOf(-1)));
    }
}
