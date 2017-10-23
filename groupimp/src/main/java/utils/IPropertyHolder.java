package utils;

/**
 * Created by Rakovskyi Dmytro on 08.10.2017.
 */
public interface IPropertyHolder {
    String getProperty(String propertyName);

    String setProperty(String propertyName, String propertyValue);

}
