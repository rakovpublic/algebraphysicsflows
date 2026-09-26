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
import mathematics.structures.AbelianGroupHomomorphism;
import mathematics.topology.IntegralHomology;
import mathematics.topology.FiniteSimplicialComplex;
import mathematics.topology.FiniteSimplicialMap;
import mathematics.topology.RelativeSimplicialComplex;
import mathematics.topology.RelativeSimplicialMap;
import mathematics.topology.SimplicialCover;
import mathematics.topology.SimplicialCoverMap;
import mathematics.topology.SimplicialCochain;
import mathematics.topology.SimplicialChain;
import mathematics.topology.RelativeSimplicialChain;
import mathematics.topology.RelativeCapProduct;
import mathematics.topology.RelativeSimplicialTriple;
import mathematics.topology.RelativeSimplicialTripleMap;
import mathematics.topology.SimplicialHomotopy;
import mathematics.topology.SimplicialHomotopyPath;
import mathematics.topology.SimplicialHomotopyEquivalence;
import mathematics.topology.RelativeSimplicialCochain;
import mathematics.examples.ConcreteAlgebrasExample;
import mathematics.probability.FiniteMarkovKernel;
import mathematics.probability.FiniteDistribution;
import org.junit.Test;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import static org.junit.Assert.*;

public class ConcreteAlgebrasTest {
    @Test public void all1279RegisteredOperationsReturnIndependentExpectedValues() {
        ConcreteMathematics math=new ConcreteMathematics();
        Map<String,String> expected=new HashMap<>();
        String discrete="Complex[[0], [1]]",emptyComplex="Complex[]";
        String swap=simplicialString(discrete,discrete,"{0=1, 1=0}"),simplicialIdentity=simplicialString(discrete,discrete,"{0=0, 1=1}");
        String freeTwo="PresentedAbelianGroup(ZMatrix(2x0)[[], []])",emptyGroup="PresentedAbelianGroup(ZMatrix(0x0)[])";
        String swapMatrix="ZMatrix(2x2)[[0, 1], [1, 0]]",emptyMatrix="ZMatrix(0x0)[]";
        String emptyHomology="IntegralHomology(outgoing="+emptyMatrix+", incoming="+emptyMatrix+")";
        String edge="Complex[[0], [1], [0, 1]]",freeOne="PresentedAbelianGroup(ZMatrix(1x0)[[]])";
        String point="Complex[[0]]",pointCover="SimplicialCover(left="+point+", right="+point+")";
        String pointHomology="IntegralHomology(outgoing=ZMatrix(0x1)[], incoming=ZMatrix(1x0)[[]])";
        String sumPointHomology="IntegralHomology(outgoing=ZMatrix(0x2)[], incoming=ZMatrix(2x0)[[], []])";
        String alphaMatrix="ZMatrix(2x1)[[1], [-1]]",betaMatrix="ZMatrix(1x2)[[1, 1]]";
        String alpha=homString(freeOne,freeTwo,alphaMatrix),beta=homString(freeTwo,freeOne,betaMatrix),delta=homString(freeOne,emptyGroup,"ZMatrix(0x1)[]");
        String diagonalPoint="RelativeComplex(ambient="+point+", subcomplex="+point+")";
        String pointCoverMap="CoverMap(source="+pointCover+", target="+pointCover+", vertices={0=0})",pointMap=simplicialString(point,point,"{0=0}");
        String identityTwo="ZMatrix(2x2)[[1, 0], [0, 1]]",identityOneMap=homString(freeOne,freeOne,"ZMatrix(1x1)[[1]]"),identityTwoMap=homString(freeTwo,freeTwo,identityTwo);
        String relativePointMap=relativeMapString(diagonalPoint,diagonalPoint,"{0=0}");
        String zeroCochain="SimplicialCochain(complex="+point+", degree=0, coordinates=[0])",unitCochain="SimplicialCochain(complex="+point+", degree=0, coordinates=[1])";
        String zeroClass="AbelianElement(group="+freeOne+", smith=[0])";
        String zeroChain="SimplicialChain(complex="+point+", degree=0, coordinates=[0])",unitChain="SimplicialChain(complex="+point+", degree=0, coordinates=[1])";
        String zeroChainMap=homString(freeOne,freeOne,"ZMatrix(1x1)[[0]]");
        expected.put("SimplicialChainAlgebra",String.join("|",zeroChain,"["+unitChain+"]",zeroChain,zeroChain,zeroChain,zeroChain,"true",point,"0","[0]",
                "SimplicialChain(complex="+point+", degree=0, coordinates=[3])","true","SimplicialChain(complex="+point+", degree=-1, coordinates=[])",
                "true","true","[]","true",pointHomology,zeroClass,zeroChain,"["+unitChain+"]",zeroChain,"0",zeroChain,zeroClass,
                "ZMatrix(1x1)[[0]]",zeroChainMap,"ZMatrix(1x1)[[0]]",zeroChainMap,"0"));
        String absolutePoint="RelativeComplex(ambient="+point+", subcomplex="+emptyComplex+")";
        String relativeZeroChain="RelativeChain(pair="+absolutePoint+", degree=0, coordinates=[0])",relativeUnitChain="RelativeChain(pair="+absolutePoint+", degree=0, coordinates=[1])";
        String relativeCap="RelativeCap(chain="+relativeZeroChain+", target="+absolutePoint+")";
        expected.put("RelativeCapProductAlgebra",String.join("|",relativeCap,relativeZeroChain,absolutePoint,relativeCap,
                "RelativeCap(chain=RelativeChain(pair="+absolutePoint+", degree=-1, coordinates=[]), target="+absolutePoint+")","true",
                relativeZeroChain,zeroClass,"ZMatrix(1x1)[[0]]",zeroChainMap,"ZMatrix(1x1)[[0]]",zeroChainMap));
        expected.put("RelativeSimplicialChainAlgebra",String.join("|",relativeZeroChain,"["+relativeUnitChain+"]",relativeZeroChain,relativeZeroChain,
                relativeZeroChain,relativeZeroChain,relativeZeroChain,relativeZeroChain,"true",absolutePoint,"0","[0]",
                "RelativeChain(pair="+absolutePoint+", degree=0, coordinates=[3])","true","RelativeChain(pair="+absolutePoint+", degree=-1, coordinates=[])",
                "true","true","[]","true",pointHomology,zeroClass,relativeZeroChain,"["+relativeUnitChain+"]",relativeZeroChain,"0",zeroChain,
                "SimplicialChain(complex="+emptyComplex+", degree=-1, coordinates=[])",relativeZeroChain,zeroClass,zeroChain,zeroClass,
                "ZMatrix(1x1)[[0]]",zeroChainMap,"ZMatrix(1x1)[[0]]",zeroChainMap,"ZMatrix(1x1)[[0]]",zeroChainMap,"ZMatrix(1x1)[[0]]",zeroChainMap));
        String relativeZeroCochain="RelativeCochain(pair="+absolutePoint+", degree=0, coordinates=[0])",relativeUnit="RelativeCochain(pair="+absolutePoint+", degree=0, coordinates=[1])";
        String relativeNext="RelativeCochain(pair="+absolutePoint+", degree=1, coordinates=[])",zeroNextGroup="PresentedAbelianGroup(ZMatrix(0x1)[])";
        String cohomologyConnecting=homString(emptyGroup,zeroNextGroup,emptyMatrix),emptyIdentity=homString(emptyGroup,emptyGroup,emptyMatrix);
        String pointTriple="RelativeTriple(outer="+diagonalPoint+", base="+emptyComplex+")",tripleExtension=homString(emptyGroup,freeOne,"ZMatrix(1x0)[[]]");
        String triplePointMap="TripleMap(source="+pointTriple+", target="+pointTriple+", vertices={0=0})",absolutePointMap=relativeMapString(absolutePoint,absolutePoint,"{0=0}");
        String pointHomotopy="SimplicialHomotopy(from="+absolutePointMap+", to="+absolutePointMap+")";
        String stationaryPoint="HomotopyPath(stages=["+absolutePointMap+"])",pointStep="HomotopyPath(stages=["+absolutePointMap+", "+absolutePointMap+"])";
        String pointEquivalence="HomotopyEquivalence(forward="+absolutePointMap+", backward="+absolutePointMap+", source-homotopy="+stationaryPoint+", target-homotopy="+stationaryPoint+")";
        String collapsePair="RelativeComplex(ambient=Complex[[0], [1], [0, 1]], subcomplex=Complex[])",collapseForward=relativeMapString(collapsePair,absolutePoint,"{0=0, 1=0}"),collapseBackward=relativeMapString(absolutePoint,collapsePair,"{0=0}");
        String collapseIdentity=relativeMapString(collapsePair,collapsePair,"{0=0, 1=1}"),collapseConstant=relativeMapString(collapsePair,collapsePair,"{0=0, 1=0}");
        String edgeCollapse="HomotopyEquivalence(forward="+collapseForward+", backward="+collapseBackward+", source-homotopy=HomotopyPath(stages=["+collapseIdentity+", "+collapseConstant+"]), target-homotopy="+stationaryPoint+")";
        expected.put("SimplicialHomotopyEquivalenceAlgebra",String.join("|","("+absolutePointMap+","+absolutePointMap+")",pointEquivalence,pointEquivalence,pointEquivalence,
                pointEquivalence,pointEquivalence,absolutePointMap,absolutePointMap,absolutePoint,absolutePoint,stationaryPoint,stationaryPoint,"true",
                identityOneMap,identityOneMap,"["+identityOneMap+", "+identityOneMap+"]",identityOneMap,identityOneMap,"["+identityOneMap+", "+identityOneMap+"]",
                "[]","[]","true",edgeCollapse,pointEquivalence,pointEquivalence));
        expected.put("SimplicialHomotopyPathAlgebra",String.join("|",pointStep,stationaryPoint,pointStep,stationaryPoint,stationaryPoint,absolutePointMap,absolutePointMap,
                absolutePoint,absolutePoint,"0","["+absolutePointMap+"]","[]","true","ZMatrix(0x1)[]","ZMatrix(0x1)[]",
                "[ZMatrix(0x1)[]]","[ZMatrix(0x1)[], ZMatrix(1x0)[[]]]","RelativeChain(pair="+absolutePoint+", degree=1, coordinates=[])",relativeZeroCochain,
                "SimplicialChain(complex="+point+", degree=1, coordinates=[])",zeroCochain,stationaryPoint,stationaryPoint));
        expected.put("SimplicialHomotopyAlgebra",String.join("|",pointHomotopy,pointHomotopy,absolutePointMap,absolutePointMap,absolutePoint,absolutePoint,
                pointHomotopy,"true","ZMatrix(0x1)[]","ZMatrix(0x1)[]","[ZMatrix(0x1)[]]","[ZMatrix(0x1)[], ZMatrix(1x0)[[]]]",
                "RelativeChain(pair="+absolutePoint+", degree=1, coordinates=[])",relativeZeroCochain,
                "SimplicialChain(complex="+point+", degree=1, coordinates=[])",zeroCochain));
        expected.put("RelativeSimplicialTripleMapAlgebra",String.join("|",triplePointMap,triplePointMap,triplePointMap,pointTriple,pointTriple,pointMap,
                relativePointMap,absolutePointMap,absolutePointMap,"true","true",triplePointMap,triplePointMap,"true",pointTriple,triplePointMap,triplePointMap,
                emptyIdentity,identityOneMap,identityOneMap,"["+identityOneMap+", "+identityOneMap+", "+emptyIdentity+", "+homString(zeroNextGroup,zeroNextGroup,emptyMatrix)+"]",
                emptyIdentity,identityOneMap,identityOneMap,"["+emptyIdentity+", "+identityOneMap+", "+identityOneMap+", "+emptyIdentity+"]"));
        expected.put("RelativeSimplicialTripleAlgebra",String.join("|",pointTriple,diagonalPoint,absolutePoint,absolutePoint,"true",
                relativeMapString(absolutePoint,absolutePoint,"{0=0}"),relativeMapString(absolutePoint,diagonalPoint,"{0=0}"),
                "ZMatrix(1x1)[[1]]","ZMatrix(0x1)[]","ZMatrix(1x0)[[]]",emptyMatrix,identityOneMap,delta,cohomologyConnecting,
                "["+identityOneMap+", "+delta+", "+cohomologyConnecting+"]","RelativeChain(pair="+absolutePoint+", degree=-1, coordinates=[])",
                "ZMatrix(1x0)[[]]","ZMatrix(1x1)[[1]]","ZMatrix(0x1)[]",tripleExtension,identityOneMap,delta,
                "["+tripleExtension+", "+identityOneMap+", "+delta+"]","RelativeCochain(pair="+diagonalPoint+", degree=1, coordinates=[])"));
        String restriction=homString(freeOne,freeTwo,"ZMatrix(2x1)[[1], [1]]"),difference=homString(freeTwo,freeOne,"ZMatrix(1x2)[[1, -1]]");
        String coverConnecting=homString(freeOne,zeroNextGroup,"ZMatrix(0x1)[]");
        expected.put("RelativeSimplicialCochainAlgebra",String.join("|",relativeZeroCochain,"["+relativeUnit+"]",relativeZeroCochain,relativeZeroCochain,relativeZeroCochain,relativeZeroCochain,relativeZeroCochain,relativeZeroCochain,relativeZeroCochain,
                "true",absolutePoint,"0","[0]","RelativeCochain(pair="+absolutePoint+", degree=0, coordinates=[3])",relativeNext,"true","true","true","true",pointHomology,
                zeroClass,relativeZeroCochain,"[]","["+relativeUnit+"]",zeroClass,"0",relativeZeroCochain,zeroCochain,relativeNext,pointHomology,"["+pointHomology+"]",
                "ZMatrix(1x1)[[1]]","ZMatrix(0x1)[]",emptyMatrix,identityOneMap,delta,cohomologyConnecting,"["+identityOneMap+", "+delta+", "+cohomologyConnecting+"]",
                identityOneMap,"["+identityOneMap+"]","["+identityOneMap+", "+identityOneMap+", "+emptyIdentity+", "+homString(zeroNextGroup,zeroNextGroup,emptyMatrix)+"]"));
        expected.put("SimplicialCochainAlgebra",String.join("|",zeroCochain,unitCochain,"["+unitCochain+"]",zeroCochain,zeroCochain,zeroCochain,zeroCochain,zeroCochain,
                "true",point,"0","[0]","SimplicialCochain(complex="+point+", degree=0, coordinates=[3])","SimplicialCochain(complex="+point+", degree=1, coordinates=[])",
                "true","true","true","true",pointHomology,zeroClass,zeroCochain,"[]","["+unitCochain+"]",zeroClass,"0",zeroCochain,pointHomology,"["+pointHomology+"]",identityOneMap,"["+identityOneMap+"]"));
        expected.put("SimplicialCoverMapAlgebra",String.join("|",pointCoverMap,pointCoverMap,pointCoverMap,pointCover,pointCover,pointMap,pointMap,pointMap,pointMap,
                "true","true",pointCoverMap,pointCoverMap,pointCoverMap,identityTwo,"["+identityTwo+"]",identityTwoMap,"["+identityTwoMap+"]",
                sumPointHomology,sumPointHomology,identityOneMap,identityOneMap,identityOneMap,identityOneMap,
                "["+identityOneMap+", "+identityTwoMap+", "+identityOneMap+", "+homString(emptyGroup,emptyGroup,emptyMatrix)+"]",
                relativePointMap,relativePointMap,"["+relativePointMap+", "+relativePointMap+"]","true",pointCover,pointCoverMap,pointCoverMap,
                identityTwo,"["+identityTwo+"]",identityTwoMap,"["+identityTwoMap+"]",sumPointHomology,sumPointHomology,
                identityOneMap,identityOneMap,identityOneMap,identityOneMap,"["+identityOneMap+", "+identityTwoMap+", "+identityOneMap+", "+homString(zeroNextGroup,zeroNextGroup,emptyMatrix)+"]"));
        expected.put("SimplicialCoverAlgebra",String.join("|",pointCover,point,point,point,point,pointCover,"false","1","ZMatrix(0x2)[]","[ZMatrix(0x2)[]]",
                sumPointHomology,"["+sumPointHomology+"]",pointHomology,pointHomology,pointHomology,pointHomology,
                alphaMatrix,betaMatrix,"ZMatrix(2x1)[[1], [0]]","ZMatrix(0x1)[]",alpha,beta,delta,"["+alpha+", "+beta+", "+delta+"]",
                homString(freeOne,freeTwo,"ZMatrix(2x1)[[1], [0]]"),homString(freeOne,freeTwo,"ZMatrix(2x1)[[0], [1]]"),
                homString(freeTwo,freeOne,"ZMatrix(1x2)[[1, 0]]"),homString(freeTwo,freeOne,"ZMatrix(1x2)[[0, 1]]"),relativeMapString(diagonalPoint,diagonalPoint,"{0=0}"),
                "ZMatrix(0x2)[]","[ZMatrix(0x2)[]]",sumPointHomology,"["+sumPointHomology+"]",pointHomology,pointHomology,pointHomology,pointHomology,
                "ZMatrix(2x1)[[1], [1]]","ZMatrix(1x2)[[1, -1]]","ZMatrix(0x1)[]",restriction,difference,coverConnecting,"["+restriction+", "+difference+", "+coverConnecting+"]",
                homString(freeOne,freeTwo,"ZMatrix(2x1)[[1], [0]]"),homString(freeOne,freeTwo,"ZMatrix(2x1)[[0], [1]]"),
                homString(freeTwo,freeOne,"ZMatrix(1x2)[[1, 0]]"),homString(freeTwo,freeOne,"ZMatrix(1x2)[[0, 1]]")));
        String intervalPair="RelativeComplex(ambient="+edge+", subcomplex="+discrete+")",connectingMatrix="ZMatrix(2x1)[[-1], [1]]";
        String relativeH0="IntegralHomology(outgoing=ZMatrix(0x0)[], incoming=ZMatrix(0x1)[])";
        String relativeH1="IntegralHomology(outgoing=ZMatrix(0x1)[], incoming=ZMatrix(1x0)[[]])";
        String inclusionH1=homString(emptyGroup,emptyGroup,emptyMatrix),quotientH1=homString(emptyGroup,freeOne,"ZMatrix(1x0)[[]]");
        String connectingH1=homString(freeOne,freeTwo,connectingMatrix);
        String relativeReflection=relativeMapString(intervalPair,intervalPair,"{0=1, 1=0}"),relativeIdentity=relativeMapString(intervalPair,intervalPair,"{0=0, 1=1}");
        String absolutePair="RelativeComplex(ambient="+edge+", subcomplex="+emptyComplex+")",diagonalPair="RelativeComplex(ambient="+edge+", subcomplex="+edge+")";
        String reflectedH1=homString(freeOne,freeOne,"ZMatrix(1x1)[[-1]]"),relativeZero="PresentedAbelianGroup(ZMatrix(0x1)[])";
        expected.put("RelativeSimplicialMapAlgebra",String.join("|",relativeReflection,relativeReflection,relativeReflection,intervalPair,intervalPair,
                simplicialString(edge,edge,"{0=1, 1=0}"),swap,"false","true",relativeIdentity,relativeIdentity,
                relativeMapString(absolutePair,absolutePair,"{0=1, 1=0}"),relativeMapString(diagonalPair,diagonalPair,"{0=1, 1=0}"),
                "ZMatrix(1x1)[[-1]]","[ZMatrix(0x0)[], ZMatrix(1x1)[[-1]]]",reflectedH1,"["+homString(relativeZero,relativeZero,emptyMatrix)+", "+reflectedH1+"]",
                relativeH1,relativeH1,inclusionH1,inclusionH1,"["+inclusionH1+", "+inclusionH1+", "+reflectedH1+", "+homString(freeTwo,freeTwo,swapMatrix)+"]",
                "false",intervalPair,relativeReflection,relativeReflection));
        expected.put("RelativeSimplicialAlgebra",String.join("|",intervalPair,edge,discrete,"false","1","-1","1","[[0, 1]]","ZMatrix(0x1)[]",
                "[ZMatrix(0x0)[], ZMatrix(0x1)[]]",relativeH1,"["+relativeH0+", "+relativeH1+"]","AbelianGroup(rank=1, torsion=[])",
                "[AbelianGroup(rank=0, torsion=[]), AbelianGroup(rank=1, torsion=[])]","1","false","ZMatrix(1x1)[[1]]","ZMatrix(1x1)[[1]]","ZMatrix(1x0)[[]]",
                connectingMatrix,inclusionH1,quotientH1,connectingH1,"["+inclusionH1+", "+quotientH1+", "+connectingH1+"]",
                "RelativeComplex(ambient="+edge+", subcomplex="+emptyComplex+")","RelativeComplex(ambient="+edge+", subcomplex="+edge+")",simplicialString(discrete,edge,"{0=0, 1=1}")));
        expected.put("FiniteSimplicialMapAlgebra",String.join("|",swap,swap,discrete,discrete,"Function[0, 1]->[0, 1]{0=1, 1=0}","1","[1, 0]",
                discrete,"true","true","true","true","false",simplicialIdentity,simplicialIdentity,swap,emptyMatrix,"["+swapMatrix+"]",
                homString(emptyGroup,emptyGroup,emptyMatrix),"["+homString(freeTwo,freeTwo,swapMatrix)+"]",emptyHomology,emptyHomology,
                "false","[1]",swap,swap,"[1]","[]",simplicialString(discrete,discrete,"{0=0, 1=0}"),simplicialString(emptyComplex,discrete,"{}")));
        String cyclic="PresentedAbelianGroup(ZMatrix(1x1)[[6]])",trivial="PresentedAbelianGroup(ZMatrix(1x1)[[1]])";
        String quotient="PresentedAbelianGroup(ZMatrix(1x2)[[6, 5]])";
        String[] hom=new String[6]; for(int i=0;i<hom.length;i++) hom[i]=homString(cyclic,cyclic,"ZMatrix(1x1)[["+i+"]]");
        String cyclicElement="AbelianElement(group="+cyclic+", smith=[3])";
        expected.put("AbelianGroupHomomorphismAlgebra",String.join("|",hom[4],hom[1],hom[3],hom[1],hom[4],cyclic,cyclic,
                "ZMatrix(1x1)[[5]]","ZMatrix(1x1)[[5]]",cyclicElement,"[AbelianElement(group="+cyclic+", smith=[5])]",
                "false","false",hom[1],hom[0],hom[0],trivial,homString(trivial,cyclic,"ZMatrix(1x1)[[0]]"),cyclic,hom[5],hom[1],
                quotient,homString(cyclic,quotient,"ZMatrix(1x1)[[0]]"),"true","true","true",hom[5],"true",cyclicElement,hom[5],hom[5],hom[2]));
        expected.put("IntegralHomologyAlgebra",String.join("|",
                "IntegralHomology(outgoing=ZMatrix(0x1)[], incoming=ZMatrix(1x1)[[6]])",
                "IntegralHomology(outgoing=ZMatrix(3x0)[[], [], []], incoming=ZMatrix(0x0)[])",
                "ZMatrix(0x1)[]","ZMatrix(1x1)[[6]]","ZMatrix(1x1)[[1]]","ZMatrix(1x1)[[6]]",cyclic,
                "AbelianGroup(rank=0, torsion=[6])","1","1","1","0","false","false","[[1]]","[[6]]","[[1]]","true","false",
                "AbelianElement(group="+cyclic+", smith=[2])","[2]","[2]","[2]","[2]",
                homString("PresentedAbelianGroup(ZMatrix(1x0)[[]])",cyclic,"ZMatrix(1x1)[[1]]"),hom[2],"AbelianElement(group="+cyclic+", smith=[0])"));
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
        assertEquals(1279,count);
    }
    private static String homString(String source,String target,String matrix) { return "AbelianHom(source="+source+", target="+target+", smith="+matrix+")"; }
    private static String simplicialString(String source,String target,String vertices) { return "SimplicialMap(source="+source+", target="+target+", vertices="+vertices+")"; }
    private static String relativeMapString(String source,String target,String vertices) { return "RelativeMap(source="+source+", target="+target+", vertices="+vertices+")"; }
    private static FiniteSimplicialComplex simplicialTestComplex() { return new FiniteSimplicialComplex(Arrays.asList(FiniteSet.of(0),FiniteSet.of(1))); }
    private static FiniteSimplicialComplex relativeTestEdge() { return new FiniteSimplicialComplex(Collections.singletonList(FiniteSet.of(0,1))); }
    private static FiniteSimplicialComplex coverTestPoint() { return new FiniteSimplicialComplex(Collections.singletonList(FiniteSet.of(0))); }
    private static SimplicialCover coverMapTestCover() { return new SimplicialCover(coverTestPoint(),coverTestPoint()); }
    private static RelativeSimplicialComplex relativeCochainTestPair() { return RelativeSimplicialComplex.absolute(coverTestPoint()); }
    private static RelativeSimplicialComplex relativeTestPair() { return new RelativeSimplicialComplex(relativeTestEdge(),simplicialTestComplex()); }
    private static RelativeSimplicialMap relativeTestMap(int index) {
        return new RelativeSimplicialMap(relativeTestPair(),relativeTestPair(),new FiniteSimplicialMap(relativeTestEdge(),relativeTestEdge(),simplicialTestMap(index).vertexMap()));
    }
    private static FiniteSimplicialMap simplicialTestMap(int index) {
        Map<BigInteger,BigInteger> vertices=new TreeMap<>(); vertices.put(BigInteger.ZERO,BigInteger.valueOf(index==0?1:0)); vertices.put(BigInteger.ONE,BigInteger.valueOf(index==0?0:1));
        return new FiniteSimplicialMap(simplicialTestComplex(),simplicialTestComplex(),vertices);
    }
    private static PresentedAbelianGroup homomorphismTestGroup() {
        return new PresentedAbelianGroup(new IntegerMatrix(new BigInteger[][]{{BigInteger.valueOf(6)}}));
    }
    private static IntegralHomology homologyTestValue(long order) {
        return new IntegralHomology(IntegerMatrix.zero(0,1),new IntegerMatrix(new BigInteger[][]{{BigInteger.valueOf(order)}}));
    }
    @SuppressWarnings({"unchecked","rawtypes"})
    private String invokeRegistered(ConcreteMathematics math,ConcreteAlgebra<?> owner,String name,OperationRegistration entry) {
        Algebra source=math.mathTool.getAlgebra(entry.first.getAlgebraName());
        IAlgebraItem item=source.buildAlgebraItem(sample(math,source.getAlgebraName(),0));
        if(owner instanceof SimplicialHomotopyEquivalenceAlgebra && source==math.relativeComplexes.algebra()) item=source.buildAlgebraItem(relativeCochainTestPair());
        if(owner instanceof SimplicialHomotopyEquivalenceAlgebra && source==math.complexes.algebra()) item=source.buildAlgebraItem(coverTestPoint());
        if(entry.id.equals("HomotopyEquivalence.collapse-vertex")) item=source.buildAlgebraItem(RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(Collections.singletonList(FiniteSet.of(0,1)))));
        if(owner instanceof SimplicialHomotopyAlgebra || owner instanceof SimplicialHomotopyPathAlgebra || owner instanceof SimplicialHomotopyEquivalenceAlgebra) {
            if(source==math.simplicialMaps.algebra()) item=source.buildAlgebraItem(FiniteSimplicialMap.identity(coverTestPoint()));
            if(source==math.relativeMaps.algebra()) item=source.buildAlgebraItem(RelativeSimplicialMap.identity(relativeCochainTestPair()));
        }
        if(owner instanceof RelativeSimplicialTripleMapAlgebra && source==math.simplicialMaps.algebra()) item=source.buildAlgebraItem(FiniteSimplicialMap.identity(coverTestPoint()));
        if(owner instanceof RelativeSimplicialTripleAlgebra && source==math.relativeComplexes.algebra()) item=source.buildAlgebraItem(RelativeSimplicialComplex.diagonal(coverTestPoint()));
        if(owner instanceof RelativeSimplicialCochainAlgebra) {
            if(source==math.relativeComplexes.algebra()) item=source.buildAlgebraItem(relativeCochainTestPair());
            if(source==math.relativeMaps.algebra()) item=source.buildAlgebraItem(RelativeSimplicialMap.identity(relativeCochainTestPair()));
            if(entry.id.equals("RelativeCochain.connect-cocycle")) item=source.buildAlgebraItem(SimplicialCochain.zero(new FiniteSimplicialComplex(Collections.emptyList()),BigInteger.ZERO));
        }
        if(owner instanceof SimplicialChainAlgebra && source==math.complexes.algebra()) item=source.buildAlgebraItem(coverTestPoint());
        if(owner instanceof RelativeSimplicialChainAlgebra && source==math.relativeComplexes.algebra()) item=source.buildAlgebraItem(relativeCochainTestPair());
        if(owner instanceof SimplicialCochainAlgebra) {
            if(source==math.complexes.algebra()) item=source.buildAlgebraItem(coverTestPoint());
            if(source==math.simplicialMaps.algebra()) item=source.buildAlgebraItem(FiniteSimplicialMap.identity(coverTestPoint()));
        }
        if(owner instanceof SimplicialCoverAlgebra && source==math.complexes.algebra()) item=source.buildAlgebraItem(coverTestPoint());
        if(owner instanceof SimplicialCoverMapAlgebra && source==math.simplicialMaps.algebra()) item=source.buildAlgebraItem(FiniteSimplicialMap.identity(coverTestPoint()));
        if(owner instanceof RelativeSimplicialAlgebra && source==math.complexes.algebra()) item=source.buildAlgebraItem(relativeTestEdge());
        if(owner instanceof RelativeSimplicialMapAlgebra && source==math.simplicialMaps.algebra()) item=source.buildAlgebraItem(relativeTestMap(0).ambientMap());
        if(owner instanceof FiniteSimplicialMapAlgebra) {
            if(source==math.complexes.algebra()) item=source.buildAlgebraItem(simplicialTestComplex());
            if(source==math.integerFunctions.algebra()) item=source.buildAlgebraItem(math.integerFunctions.member(
                    FiniteSimplicialMap.vertexSet(simplicialTestComplex()),FiniteSimplicialMap.vertexSet(simplicialTestComplex()),simplicialTestMap(0).vertexMap()));
        }
        if(owner instanceof IntegralHomologyAlgebra && source==math.integerMatrices.algebra())
            item=source.buildAlgebraItem(IntegerMatrix.zero(0,1));
        if(owner instanceof AbelianGroupHomomorphismAlgebra) {
            if(source==math.presentedAbelianGroups.algebra()) item=source.buildAlgebraItem(homomorphismTestGroup());
            if(source==math.integerMatrices.algebra()) item=source.buildAlgebraItem(new IntegerMatrix(new BigInteger[][]{{BigInteger.valueOf(5)}}));
        }
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
        if(entry.id.equals("HomotopyEquivalence.dominators")) second=BigInteger.ZERO;
        if(owner instanceof SimplicialHomotopyAlgebra || owner instanceof SimplicialHomotopyPathAlgebra || owner instanceof SimplicialHomotopyEquivalenceAlgebra) {
            if(entry.second==math.naturals.algebra()) second=BigInteger.ZERO;
            if(entry.second==math.simplicialMaps.algebra()) second=FiniteSimplicialMap.identity(coverTestPoint());
            if(entry.second==math.relativeMaps.algebra()) second=RelativeSimplicialMap.identity(relativeCochainTestPair());
            if(entry.second==math.relativeCochains.algebra()) second=RelativeSimplicialCochain.zero(relativeCochainTestPair(),BigInteger.ONE);
            if(entry.second==math.cochains.algebra()) second=SimplicialCochain.zero(coverTestPoint(),BigInteger.ONE);
        }
        if(owner instanceof RelativeSimplicialTripleMapAlgebra && entry.second==math.naturals.algebra()) second=BigInteger.ZERO;
        if(owner instanceof RelativeSimplicialTripleAlgebra) {
            if(entry.second==math.complexes.algebra()) second=new FiniteSimplicialComplex(Collections.emptyList());
            if(entry.second==math.naturals.algebra()) second=BigInteger.ZERO;
            if(entry.second==math.relativeChains.algebra()) second=RelativeSimplicialChain.zero(RelativeSimplicialComplex.diagonal(coverTestPoint()),BigInteger.ZERO);
        }
        if(owner instanceof RelativeCapProductAlgebra && entry.second==math.relativeComplexes.algebra()) second=relativeCochainTestPair();
        if(owner instanceof RelativeSimplicialChainAlgebra) {
            if(entry.second==math.integers.algebra() || entry.second==math.naturals.algebra()) second=BigInteger.ZERO;
            if(entry.second==math.integerVectors.algebra()) second=new IntegerVector(BigInteger.valueOf(3));
            if(entry.second==math.abelianGroupElements.algebra()) second=RelativeSimplicialChain.zero(relativeCochainTestPair(),BigInteger.ZERO).homology().group().zero();
            if(entry.second==math.relativeComplexes.algebra()) second=relativeCochainTestPair();
            if(entry.second==math.relativeMaps.algebra()) second=RelativeSimplicialMap.identity(relativeCochainTestPair());
        }
        if(owner instanceof SimplicialChainAlgebra) {
            if(entry.second==math.integers.algebra() || entry.second==math.naturals.algebra()) second=BigInteger.ZERO;
            if(entry.second==math.integerVectors.algebra()) second=new IntegerVector(BigInteger.valueOf(3));
            if(entry.second==math.abelianGroupElements.algebra()) second=SimplicialChain.zero(coverTestPoint(),BigInteger.ZERO).homology().group().zero();
            if(entry.second==math.simplicialMaps.algebra()) second=FiniteSimplicialMap.identity(coverTestPoint());
        }
        if(owner instanceof RelativeSimplicialCochainAlgebra) {
            if(entry.second==math.relativeComplexes.algebra()) second=relativeCochainTestPair();
            if(entry.second==math.naturals.algebra()) second=BigInteger.ZERO;
            if(entry.second==math.integerVectors.algebra()) second=new IntegerVector(BigInteger.valueOf(3));
            if(entry.second==math.abelianGroupElements.algebra()) second=RelativeSimplicialCochain.cohomology(relativeCochainTestPair(),BigInteger.ZERO).group().zero();
            if(entry.second==math.relativeMaps.algebra()) second=RelativeSimplicialMap.identity(relativeCochainTestPair());
        }
        if(owner instanceof SimplicialCochainAlgebra) {
            if(entry.second==math.naturals.algebra()) second=BigInteger.ZERO;
            if(entry.second==math.integerVectors.algebra()) second=new IntegerVector(BigInteger.valueOf(3));
            if(entry.second==math.abelianGroupElements.algebra()) second=SimplicialCochain.cohomology(coverTestPoint(),BigInteger.ZERO).group().zero();
            if(entry.second==math.simplicialMaps.algebra()) second=FiniteSimplicialMap.identity(coverTestPoint());
        }
        if(owner instanceof SimplicialCoverMapAlgebra) {
            if(entry.second==math.simplicialCovers.algebra()) second=coverMapTestCover();
            if(entry.second==math.naturals.algebra()) second=BigInteger.ZERO;
        }
        if(owner instanceof SimplicialCoverAlgebra) {
            if(entry.second==math.complexes.algebra()) second=coverTestPoint();
            if(entry.second==math.naturals.algebra()) second=BigInteger.ZERO;
        }
        if(owner instanceof RelativeSimplicialMapAlgebra) {
            if(entry.second==math.relativeComplexes.algebra()) second=relativeTestPair();
            if(entry.second==math.naturals.algebra()) second=BigInteger.ONE;
        }
        if(owner instanceof RelativeSimplicialAlgebra) {
            if(entry.second==math.complexes.algebra()) second=simplicialTestComplex();
            if(entry.second==math.naturals.algebra()) second=BigInteger.ONE;
        }
        if(owner instanceof FiniteSimplicialMapAlgebra) {
            if(entry.second==math.complexes.algebra()) second=simplicialTestComplex();
            if(entry.second==math.integers.algebra()) second=BigInteger.ZERO;
            if(entry.second==math.integerSets.algebra()) second=FiniteSet.of(BigInteger.ZERO);
        }
        if(owner instanceof IntegralHomologyAlgebra) {
            if(entry.second==math.integerMatrices.algebra()) second=new IntegerMatrix(new BigInteger[][]{{BigInteger.valueOf(6)}});
            if(entry.second==math.integerVectors.algebra()) second=new IntegerVector(BigInteger.valueOf(name.equals("bounding-chain")?12:2));
            if(entry.second==math.abelianGroupElements.algebra()) second=homologyTestValue(6).classOf(new IntegerVector(BigInteger.valueOf(2)));
        }
        if(owner instanceof AbelianGroupHomomorphismAlgebra) {
            if(entry.second==math.presentedAbelianGroups.algebra()) second=homomorphismTestGroup();
            if(entry.second==math.abelianGroupElements.algebra()) second=homomorphismTestGroup().fromSmith(new IntegerVector(BigInteger.valueOf(3)));
        }
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
            case "RelativeCochain": return RelativeSimplicialCochain.zero(relativeCochainTestPair(),BigInteger.ZERO);
            case "SimplicialCochain": return SimplicialCochain.zero(coverTestPoint(),BigInteger.ZERO);
            case "SimplicialChain": return SimplicialChain.zero(coverTestPoint(),BigInteger.ZERO);
            case "RelativeChain": return RelativeSimplicialChain.zero(relativeCochainTestPair(),BigInteger.ZERO);
            case "RelativeCap": return new RelativeCapProduct(RelativeSimplicialChain.zero(relativeCochainTestPair(),BigInteger.ZERO),relativeCochainTestPair());
            case "RelativeTriple": return new RelativeSimplicialTriple(RelativeSimplicialComplex.diagonal(coverTestPoint()),new FiniteSimplicialComplex(Collections.emptyList()));
            case "RelativeTriple.pair": return new Pair<>(sample(math,"RelativeTriple",0),sample(math,"RelativeTriple",0));
            case "TripleMap": return RelativeSimplicialTripleMap.identity((RelativeSimplicialTriple)sample(math,"RelativeTriple",0));
            case "SimplicialHomotopy": return SimplicialHomotopy.absolute(FiniteSimplicialMap.identity(coverTestPoint()),FiniteSimplicialMap.identity(coverTestPoint()));
            case "HomotopyPath": return SimplicialHomotopyPath.stationary(RelativeSimplicialMap.identity(relativeCochainTestPair()));
            case "RelativeMap.pair": return new Pair<>(RelativeSimplicialMap.identity(relativeCochainTestPair()),RelativeSimplicialMap.identity(relativeCochainTestPair()));
            case "HomotopyPath.pair": return new Pair<>(sample(math,"HomotopyPath",0),sample(math,"HomotopyPath",0));
            case "HomotopyEquivalence": return SimplicialHomotopyEquivalence.identity(relativeCochainTestPair());
            case "StrongCollapse.vertices": return new Pair<>(BigInteger.ONE,BigInteger.ZERO);
            case "CoverMap": return SimplicialCoverMap.identity(coverMapTestCover());
            case "SimplicialCover.pair": return new Pair<>(coverMapTestCover(),coverMapTestCover());
            case "SimplicialCover": return new SimplicialCover(coverTestPoint(),index==0?coverTestPoint():new FiniteSimplicialComplex(Collections.emptyList()));
            case "RelativeMap": return relativeTestMap(index);
            case "RelativeComplex.pair": return new Pair<>(relativeTestPair(),relativeTestPair());
            case "RelativeComplex": return index==0?new RelativeSimplicialComplex(relativeTestEdge(),simplicialTestComplex()):RelativeSimplicialComplex.absolute(relativeTestEdge());
            case "SimplicialMap": return simplicialTestMap(index);
            case "FiniteComplex.pair": return new Pair<>(simplicialTestComplex(),simplicialTestComplex());
            case "IntegralHomology": return homologyTestValue(index==0?6:4);
            case "IntegralHomology.map-input": return new Pair<>(homologyTestValue(6),new IntegerMatrix(new BigInteger[][]{{BigInteger.valueOf(2)}}));
            case "AbelianGroupHomomorphism": return AbelianGroupHomomorphism.scaling(homomorphismTestGroup(),BigInteger.valueOf(index==0?5:2));
            case "PresentedAbelianGroup.pair": return new Pair<>(homomorphismTestGroup(),homomorphismTestGroup());
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
