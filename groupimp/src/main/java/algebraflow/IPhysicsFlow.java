package algebraflow;

import algebra.imp.Algebra;

import java.util.HashMap;
import java.util.List;

public interface IPhysicsFlow extends IAlgebraFlow{
List<HashMap<Algebra<?>,Integer>> getAllPossibleResultSetDescriptions();

}
