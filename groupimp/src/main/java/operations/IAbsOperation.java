package operations;

import algebra.imp.Algebra;
import algebra.imp.SuperAlgebra;

/**
 * Created by Rakovskyi Dmytro on 02.04.2017.
 */
public interface IAbsOperation  {
   String getDescription();
    String getAlgebraName();
    Class getResultBaseClass();

}
