
package com.rac021.jax.api.configuration ;

/**
 *
 * @author ryahiaoui
 */
public interface IConfigurator {
    
    public default Long getValidRequestTimeout() {
        return 5l ; // in Seconds
    }
}
