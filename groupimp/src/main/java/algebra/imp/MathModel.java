package algebra.imp;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Rakovskyi Dmytro on 31.03.2017.
 */
public final class MathModel implements Serializable {
    private final HashMap<String,Algebra<?>>algebras;

    public MathModel() {
        algebras = new HashMap<String,Algebra<?>>();
    }
    public boolean addAlgebra(Algebra<?> algebra){
        if(!algebras.containsKey(algebra.getAlgebraName())){
            algebras.put(algebra.getAlgebraName(),algebra);
            return true;
        }
        return false;
    }
    public Algebra<?> getAlgebra(String name){
        return algebras.get(name);
    }
    public boolean hasAlgebra(String name){
        return algebras.containsKey(name);
    }
}
