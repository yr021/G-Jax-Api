
package com.rac021.jax.api.crypto ;

import java.util.List ;
import java.util.ArrayList ;
import java.util.stream.Collectors ;

/**
 *
 * @author ryahiaoui
 */

public enum AcceptType {
    
    XML_PLAIN      ("xml/plain"     ) ,
    XML_ENCRYPTED  ("xml/encrypted" ) ,
    JSON_PLAIN     ("json/plain"    ) ,
    JSON_ENCRYPTED ("json/encrypted") ;
    
    private final String name         ;

    private AcceptType( String name) {
        this.name = name ;
    }
    
    public static List<AcceptType> toList( List<String> list ) {
     
       try {
         return list.stream()
                    .map( accept_t -> AcceptType.valueOf( accept_t.trim()
                                                .replace("/", "_")))
                    .collect(Collectors.toList()) ;
        } catch( Exception ex ) {
            System.out.println( " ************************************** ") ;
            System.out.println(" Exception in CipherTypes List : " + list ) ;
            System.out.println( ex.getCause()                             ) ;
            System.out.println( " ************************************** ") ;
        }
        
       return new ArrayList() ;
    }
}
