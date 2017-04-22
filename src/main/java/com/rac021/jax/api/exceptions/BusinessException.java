
package com.rac021.jax.api.exceptions ;

/**
 *
 * @author yahiaoui
 */
public class BusinessException extends Exception {

    public BusinessException(String message) {
        super(message) ;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause) ;
    }

   
}
