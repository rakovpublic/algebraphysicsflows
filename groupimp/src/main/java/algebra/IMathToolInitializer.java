package algebra;

import algebra.imp.MathTool;

import java.io.Serializable;

/**
 * Created by Rakovskyi Dmytro on 02.04.2017.
 */
public interface IMathToolInitializer extends Serializable {
    MathTool initialize();
}
