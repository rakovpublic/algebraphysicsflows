package algebra;

import algebra.imp.SuperAlgebra;

import java.io.Serializable;

/**
 * Created by Rakovskyi Dmytro on 02.04.2017.
 */
public interface ISuperAlgebraInitializer extends Serializable {
    SuperAlgebra initialize();
}
