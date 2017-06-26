
package com.rac021.jax.api.crypto ;

import java.util.List ;
import java.util.ArrayList ;
import java.util.stream.Collectors ;

/**
 *
 * @author ryahiaoui
 */

public enum CipherTypes {
    
    AES_128_CBC    ,
    AES_128_ECB    ,
    AES_192_CBC    ,
    AES_256_ECB    ,
    AES_256_CBC    ,
    AES_192_ECB    ,
    DESede_192_CBC ,
    DESede_192_ECB ,
    DES_64_CBC     ,
    DES_64_ECB     ;

    public static List<CipherTypes> toList( List<String> list ) {
        
      try {
        return list.stream()
                   .map( content_t -> CipherTypes.valueOf(content_t.trim()))
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
