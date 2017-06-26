
package com.rac021.jax.api.crypto ;

import java.util.Objects ;
import com.rac021.jax.api.crypto.algos.Aes ;
import com.rac021.jax.api.crypto.algos.Des ;
import com.rac021.jax.api.crypto.algos.Desede ;
import com.rac021.jax.api.exceptions.BusinessException;

/**
 *
 * @author ryahiaoui
 */

public class FactoryCipher {

    public static ICryptor getCipher( String cipher, String password ) throws BusinessException {
        
        if( password == null || cipher == null ) {
          throw new BusinessException(" Password Can't be null " ) ;
        }
        
        Objects.requireNonNull( cipher, password ) ;
        
        if( cipher.equalsIgnoreCase(CipherTypes.AES_128_CBC.name())) {
            return new Aes( EncDecRyptor._CIPHER_MODE.CBC  , 
                            EncDecRyptor._CIPHER_SIZE._128 , 
                            password )                     ;
        }
        
        if( cipher.equalsIgnoreCase(CipherTypes.AES_128_ECB.name())) {
            return new Aes( EncDecRyptor._CIPHER_MODE.ECB  , 
                            EncDecRyptor._CIPHER_SIZE._128 , 
                            password )                     ;
        }
        
        if( cipher.equalsIgnoreCase(CipherTypes.AES_192_CBC.name())) {
            return new Aes( EncDecRyptor._CIPHER_MODE.CBC  , 
                            EncDecRyptor._CIPHER_SIZE._192 , 
                            password )                     ;
        }
        
        if( cipher.equalsIgnoreCase(CipherTypes.AES_192_ECB.name())) {
            return new Aes( EncDecRyptor._CIPHER_MODE.ECB  , 
                            EncDecRyptor._CIPHER_SIZE._192 , 
                            password )                     ;
        }
        
        if( cipher.equalsIgnoreCase(CipherTypes.AES_256_CBC.name())) {
            return new Aes( EncDecRyptor._CIPHER_MODE.CBC  , 
                            EncDecRyptor._CIPHER_SIZE._256 , 
                            password )                     ;
        }
        
        if( cipher.equalsIgnoreCase(CipherTypes.AES_256_ECB.name())) {
            return new Aes( EncDecRyptor._CIPHER_MODE.ECB  , 
                            EncDecRyptor._CIPHER_SIZE._256 , 
                            password )                     ;
        }
        
        if( cipher.equalsIgnoreCase(CipherTypes.DES_64_CBC.name())) {
            return new Des( EncDecRyptor._CIPHER_MODE.CBC , 
                            EncDecRyptor._CIPHER_SIZE._64 , 
                            password )                    ;
        }
        
        if( cipher.equalsIgnoreCase(CipherTypes.DES_64_ECB.name())) {
            return new Des( EncDecRyptor._CIPHER_MODE.ECB , 
                            EncDecRyptor._CIPHER_SIZE._64 , 
                            password )                    ;
        }
        
        if( cipher.equalsIgnoreCase(CipherTypes.DESede_192_CBC.name())) {
            return new Desede( EncDecRyptor._CIPHER_MODE.CBC  , 
                               EncDecRyptor._CIPHER_SIZE._192 , 
                               password )                     ;
        }
        
        if( cipher.equalsIgnoreCase(CipherTypes.DESede_192_ECB.name())) {
             return new Desede( EncDecRyptor._CIPHER_MODE.ECB , 
                               EncDecRyptor._CIPHER_SIZE._192 , 
                               password )                     ;
        }
        
        throw  new IllegalArgumentException(" No Cipher found ! ") ;
    } 

}
