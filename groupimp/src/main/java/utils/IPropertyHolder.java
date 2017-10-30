package utils;

/**
 * Created by Rakovskyi Dmytro on 08.10.2017.
 */
public interface IPropertyHolder {

    /**
     * @param propertyName name of requested property
     * @return value of requested property
     */
    String getProperty(String propertyName);
    /**
     * @param propertyName
     * @param propertyValue
     * */
    String setProperty(String propertyName, String propertyValue);

}
