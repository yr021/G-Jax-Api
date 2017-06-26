
package com.rac021.jax.api.crypto.algos ;

import java.util.Base64 ;
import java.util.Objects ;
import javax.crypto.Cipher ;
import javax.crypto.spec.SecretKeySpec ;
import javax.crypto.spec.IvParameterSpec ;
import com.rac021.jax.api.crypto.EncDecRyptor ;

/**
 *
 * @author ryahiaoui
 */

public class Aes  extends EncDecRyptor {
  
    private final int IV_SIZE = 16 ; // Bits
            
    public Aes( _CIPHER_MODE cipher_type , _CIPHER_SIZE cipher_size, String password ) {
        init(cipher_type, cipher_size, password) ;
     }
     
    private void init( _CIPHER_MODE cipher_type ,
                       _CIPHER_SIZE cipher_size, String password ) {
      
        try {
              Objects.requireNonNull( password ) ;

              CIPHER_TYPE  = cipher_type         ;
              CIPHER_SIZE  = cipher_size         ;

              SIZE_BYTE_KEY = Integer.parseInt( cipher_size.name().replace("_","") ) / 8   ;

              if( cipher_type == _CIPHER_MODE.CBC )            {
                   ivBytes     = randomInitIV( IV_SIZE )       ;
                   this.ivSpec =  new IvParameterSpec(ivBytes) ;
              }

              KEY           = getKeyFromPasswordUsingSha256( password )                    ;

              secretKeySpec = new SecretKeySpec( KEY , _CIPHER_NAME.AES.name() )           ;

              cipher        = Cipher.getInstance( "AES/" + CIPHER_TYPE + "/PKCS5Padding" ) ;
        
        } catch( Exception ex )               {
            ex.printStackTrace()              ;
            System.out.println(ex.getCause()) ;
        }
        
    }
    
    @Override
    public void setOperationMode( _Operation op ) {

       this.OPERATION = op ;
       
       try {
           
            if( CIPHER_TYPE == CIPHER_TYPE.CBC ) {
                 cipher.init( op ==  _Operation.Encrypt ? Cipher.ENCRYPT_MODE : 
                                     Cipher.DECRYPT_MODE , secretKeySpec , ivSpec ) ;
            }
            else if(  CIPHER_TYPE == CIPHER_TYPE.ECB ) {
                 cipher.init( op ==  _Operation.Encrypt ? Cipher.ENCRYPT_MODE : 
                                     Cipher.DECRYPT_MODE , secretKeySpec )    ;
           }
            
       } catch( Exception ex )                 {
           ex.printStackTrace()                ;
           System.out.println( ex.getCause() ) ;
       }
       
   }
    
    
    @Override
    public byte[] process ( String message, _CipherOperation cipherOperation ) {
     
         try {  return OPERATION == _Operation.Decrypt ?
                      decrypt( message, cipherOperation ) :
                       cipherOperation == _CipherOperation.dofinal   ?
                                           cipher.doFinal( message.getBytes()) :
                                           cipher.update(message.getBytes())   ;
            
         } catch( Exception ex )  {
             ex.printStackTrace() ;
             return null          ;
         }
    }
    
    @Override
    public byte[] decrypt( String encryptedMessage , 
                          _CipherOperation cipherOperation ) throws Exception {
        return super.decrypt(encryptedMessage, cipherOperation) ;
    }

    @Override
    public byte[] getIvBytesEncoded64() {
        return ivBytes != null ?
               Base64.getEncoder().encode(ivBytes)  : null ;
    }
 
}
