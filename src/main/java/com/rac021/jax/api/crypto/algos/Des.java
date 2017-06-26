
package com.rac021.jax.api.crypto.algos ;

import java.util.Base64 ;
import javax.crypto.Cipher ;
import javax.crypto.SecretKey ;
import javax.crypto.spec.DESKeySpec ;
import javax.crypto.SecretKeyFactory ;
import javax.crypto.spec.IvParameterSpec ;
import com.rac021.jax.api.crypto.EncDecRyptor ;

/**
 *
 * @author ryahiaoui
 */
public class Des extends EncDecRyptor {
   
    private SecretKey DES_KEY ;
    
    public Des( _CIPHER_MODE cipher_type , _CIPHER_SIZE cipher_size, String password ) {
        init(cipher_type, cipher_size, password) ;
     }
     
    private void init( _CIPHER_MODE cipher_type , 
                      _CIPHER_SIZE cipher_size, String password )   {

        try {
             CIPHER_TYPE  = cipher_type ;
             CIPHER_SIZE  = cipher_size ; 

             SIZE_BYTE_KEY = Integer.parseInt( cipher_size.name().replace("_","") ) / 8 ;

             if( cipher_type == CIPHER_TYPE.CBC )            {
                  ivBytes     = randomInitIV ( 8 )           ; 
                  this.ivSpec = new IvParameterSpec(ivBytes) ;
             }

             KEY = getKeyFromPasswordUsingSha256( password ) ; 

             DESKeySpec dks = new DESKeySpec( KEY )                       ;
             SecretKeyFactory skf = SecretKeyFactory.getInstance( "DES" ) ;
             DES_KEY = skf.generateSecret(dks)                            ;

            cipher = Cipher.getInstance( "DES/" + CIPHER_TYPE + "/PKCS5Padding" ) ;
        
        } catch( Exception ex ) {
           System.out.println(ex.getCause()) ;
        }
    }
    
    @Override
    public void setOperationMode( _Operation op ) {

       this.OPERATION = op ;
       
       try {
           
            if( CIPHER_TYPE == CIPHER_TYPE.CBC  ) {
                this.cipher.init( op ==  _Operation.Encrypt ? Cipher.ENCRYPT_MODE : 
                                          Cipher.DECRYPT_MODE , DES_KEY, ivSpec ) ;
            }
            else if( CIPHER_TYPE == CIPHER_TYPE.ECB ) {
                this.cipher.init( op ==  _Operation.Encrypt ? Cipher.ENCRYPT_MODE : 
                                          Cipher.DECRYPT_MODE , DES_KEY ) ;
            }
            
       } catch( Exception ex ) {
            ex.printStackTrace() ;
           System.out.println( ex.getCause() ) ;
       }
   }
    
    @Override
    public byte[] process ( String message, _CipherOperation cipherOperation ) {
     
         try {  return OPERATION == _Operation.Decrypt ?
                      decrypt( message, cipherOperation ) :
                      cipherOperation == _CipherOperation.dofinal ?
                                          cipher.doFinal( message.getBytes()) :
                                          cipher.update(message.getBytes()) ;
            
         } catch( Exception ex )  {
             ex.printStackTrace() ;
             return null          ;
         }
    }
 
    @Override
    public byte[] getIvBytesEncoded64() {
        return ivBytes != null ?
               Base64.getEncoder().encode(ivBytes)  : null ;
    }
}
