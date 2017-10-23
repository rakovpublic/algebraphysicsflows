package algebra.imp;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Rakovskyi Dmytro on 31.03.2017.
 */
public final class MathTool implements Serializable {
    private final HashMap<String, Algebra<?>> algebras;
    private String name;

    public MathTool(String name) {
        this.name = name;
        algebras = new HashMap<String, Algebra<?>>();
    }

    /**
     * add algebra to math tool
     *
     * @param algebra
     * @return true if algebra added, false if algebra with such name already exists
     */
    public boolean addAlgebra(Algebra<?> algebra) {
        if (!algebras.containsKey(algebra.getAlgebraName())) {
            algebras.put(algebra.getAlgebraName(), algebra);
            return true;
        }
        return false;
    }

    /**
     * extract algebra by algebra name
     *
     * @param name algebra name
     * @return algebra instance
     * @see Algebra
     */
    public Algebra<?> getAlgebra(String name) {
        return algebras.get(name);
    }

    /**
     * @param name algebra name
     * @return true if algebra with such name exists in mathtool
     * @see Algebra
     */
    public boolean hasAlgebra(String name) {
        return algebras.containsKey(name);
    }

    /**
     * @return mathtool name
     */
    public String getName() {
        return this.name;
    }
}
