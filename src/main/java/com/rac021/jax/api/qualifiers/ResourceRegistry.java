
package com.rac021.jax.api.qualifiers ;

import javax.inject.Qualifier ;
import java.lang.annotation.Target ;
import java.lang.annotation.Retention ;
import static java.lang.annotation.ElementType.TYPE ;
import static java.lang.annotation.ElementType.FIELD ;
import static java.lang.annotation.ElementType.METHOD ;
import static java.lang.annotation.ElementType.PARAMETER ;
import static java.lang.annotation.RetentionPolicy.RUNTIME ;

/**
 *
 * @author ryahiaoui
 */
@Qualifier
@Retention(RUNTIME)
@Target ( { FIELD, TYPE, METHOD, PARAMETER } )
public @interface ResourceRegistry {
    
  public String value() ; 
}
