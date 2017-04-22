
package com.rac021.jax.api.qualifiers ;

import java.lang.annotation.Target ;
import java.lang.annotation.Retention ;
import java.lang.annotation.ElementType ;
import java.lang.annotation.RetentionPolicy ;

/**
 *
 * @author ryahiaoui
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResultColumn {
    int index() ;
}
