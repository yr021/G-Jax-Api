
package com.rac021.jax.api.crypto ;

import java.util.List ;
import java.util.logging.Level ;
import java.util.logging.Logger ;
import java.util.stream.Collectors ;
import com.rac021.jax.api.exceptions.BusinessException ;

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

    private AcceptType( String name)  {
        this.name = name ;
    }
    
    public static List<AcceptType> toList( List<String> list ) {
     
       try {
           return list.stream()
                      .map( accept_t -> AcceptType.valueOf( accept_t.trim()
                                                  .replace("/", "_")))
                      .collect(Collectors.toList()) ;
       } catch( Exception ex ) {
           try {
           throw new BusinessException( " AcceptType List [ " + list + " ] "
                                        + "not supported ! \n "
                                        + "// Cause :" + ex.getCause()  )    ;
           } catch( BusinessException ex1 ) {
             Logger.getLogger(AcceptType.class.getName())
                                              .log(Level.SEVERE, null, ex1 )  ;   
             Logger.getLogger(AcceptType.class.getName())
                                              .log(Level.SEVERE, null, ex ) ;   
           }
       }
       return null ;
        
    }
    
    public static AcceptType toAcceptTypes( String acceptType )           {
        
      try {
            return  AcceptType.valueOf( acceptType.trim()) ;
      } catch( Exception ex ) {
          try {
              throw new BusinessException( " AcceptType  [ " + acceptType + 
                                           " ] not supported ! " )        ;
          } catch (BusinessException ex1) {
              Logger.getLogger(AcceptType.class.getName())
                                         .log(Level.SEVERE, null, ex1 )   ;
              Logger.getLogger(AcceptType.class.getName())
                                         .log(Level.SEVERE, null, ex )    ;
          }
      }
      
      return null ;
        
    }
}
