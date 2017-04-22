
package com.rac021.jax.api.crypto ;

/**
 *
 * @author yahiaoui
 */
import java.util.Base64 ;
import javax.crypto.Cipher ;
import javax.crypto.spec.SecretKeySpec ;
import javax.crypto.BadPaddingException ;
import javax.crypto.spec.IvParameterSpec ;
import java.security.InvalidKeyException ;
import javax.crypto.NoSuchPaddingException ;
import java.security.NoSuchAlgorithmException ;
import javax.crypto.IllegalBlockSizeException ;
import java.security.InvalidAlgorithmParameterException ;

/**
 *
 * @author ptcherniati
 */
public class Encryptor {

    private  Cipher          cipher         ;
    private  IvParameterSpec ivSpec         ;
    private  byte[]          ivBytes        ;
    private  SecretKeySpec   secretKeySpec  ;

    private static final String CRYPTO_MODE = "AES/CBC/PKCS5Padding" ;

    public static String generateKeyString(String password) throws NoSuchAlgorithmException {
        return Digestor.digestSha1(password).substring(0, 16) ;
    }

    public static String aes128ECBEncrypt(String password, String message) {
        try {
                String key                  = generateKeyString(password)              ;
                SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES") ;

                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding") ;
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)            ;

                return new String(Base64.getEncoder().encode(cipher.doFinal((message).getBytes()))) ;

        } catch ( InvalidKeyException | NoSuchAlgorithmException |
                  BadPaddingException | IllegalBlockSizeException |
                  NoSuchPaddingException ex) {
            ex.printStackTrace() ;
            return null ;
        }
    }

    public static String aes128ECBDecrypt(String password, String encryptedMessage) throws Exception {
        try {
            String        key           = generateKeyString(password)                ;
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES")   ;
            Cipher        cipher        = Cipher.getInstance("AES/ECB/PKCS5Padding") ;
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec) ;

            return new String(cipher.doFinal(Base64.getDecoder().decode (
                    encryptedMessage.getBytes()))) ;

        } catch ( InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException ex) {
            ex.printStackTrace() ;
            return null ;
        }
    }

    public static String aes128CBC7Encrypt(String password, String message) {
        try {
            String        key           = generateKeyString(password)              ;
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES") ;

            byte[] ivBytes = new byte[] {
                10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10} ;

            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes)               ;
            Cipher          cipher = Cipher.getInstance("AES/CBC/PKCS5Padding") ;

            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec) ;

            return new String(Base64.getEncoder().encode(cipher.doFinal((message).getBytes()))) ;

        } catch ( NoSuchAlgorithmException  | NoSuchPaddingException             |
                  InvalidKeyException       | InvalidAlgorithmParameterException |
                  IllegalBlockSizeException | BadPaddingException ex)            {
            ex.printStackTrace() ; 
            return null;
        }
    }

    public static String aes128CBC7Decrypt(String password, String encryptedMessage) throws Exception {
        
        try {
                String        key           = generateKeyString(password) ;
                SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES") ;
                
                byte[] ivBytes = new byte[] {
                    10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10};

                IvParameterSpec ivSpec = new IvParameterSpec(ivBytes) ;
                Cipher          cipher = Cipher.getInstance("AES/CBC/PKCS5Padding") ;
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec) ;

                return new String( cipher.doFinal(Base64.getDecoder()
                                                        .decode( encryptedMessage.getBytes()))) ;

        } catch ( NoSuchAlgorithmException | NoSuchPaddingException |
                  InvalidKeyException      | InvalidAlgorithmParameterException ex) {
            ex.printStackTrace() ;
            return null ;
        }
    }

    public Encryptor(String password) {
        try {
            ivBytes = new byte[]{10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10} ;
            secretKeySpec = new SecretKeySpec(generateKeyString(password).getBytes(), "AES")     ;
            ivSpec = new IvParameterSpec(ivBytes);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace() ; 
        }
    }

    public void initEncryptMode() {
        try {
            cipher = Cipher.getInstance(CRYPTO_MODE) ;
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec) ;
        } catch ( InvalidAlgorithmParameterException | InvalidKeyException |
                  NoSuchAlgorithmException           | NoSuchPaddingException ex ) {
            ex.printStackTrace() ;
        }
    }

    public void initDecryptMode() {
        try {
            cipher = Cipher.getInstance(CRYPTO_MODE) ;
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec) ;
        } catch ( InvalidAlgorithmParameterException | InvalidKeyException       |
                  NoSuchAlgorithmException           | NoSuchPaddingException ex ) {
            ex.printStackTrace() ;
        }
    }

   
    public byte[] aes128CBC7Encrypt(byte[] toEncrypt, CipherOperation co) {
        try {
            if ( co.equals(CipherOperation.dofinal) ) {
                return cipher.doFinal(toEncrypt) ;
            } else {
                return cipher.update(toEncrypt) ;
            }
        } catch (BadPaddingException | IllegalBlockSizeException ex) {
            ex.printStackTrace() ;
            return null ;
        }
    }

    public byte[] aes128CBC7Decrypt(byte[] encryptedMessage, CipherOperation co) {
        try {
            if (co.equals(CipherOperation.dofinal))     {
                return cipher.doFinal(encryptedMessage) ;
            } else {
                return cipher.update(encryptedMessage)  ;
            }
        } catch (BadPaddingException | IllegalBlockSizeException ex ) {
            ex.printStackTrace() ; 
            return null ;
        }
    }

    public Cipher getCipher() {
        return cipher ;
    }

    public void setCipher(Cipher cipher) {
        this.cipher = cipher ;
    }

    public IvParameterSpec getIvSpec() {
        return ivSpec ;
    }

    public void setIvSpec(IvParameterSpec ivSpec) {
        this.ivSpec = ivSpec ;
    }

}
