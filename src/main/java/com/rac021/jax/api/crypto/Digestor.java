
package com.rac021.jax.api.crypto ;

import java.math.BigInteger ;
import java.security.MessageDigest ;
import java.nio.charset.StandardCharsets ;
import java.security.NoSuchAlgorithmException ;

/**
 *
 * @author yahiaoui
 */
public class Digestor {
    
    final static String  MD5    = "MD5"    ;
    final static String  SHA1   = "SHA-1"   ;
    final static String  SHA256 = "SHA-256" ;
    
    public static byte[] toMD5(final String password) throws NoSuchAlgorithmException{
        MessageDigest md = MessageDigest.getInstance(MD5)           ;
        return md.digest(password.getBytes(StandardCharsets.UTF_8)) ;
    }
    
    public static byte[] toSHA1(String message) throws NoSuchAlgorithmException {
        MessageDigest md   = MessageDigest.getInstance(SHA1) ;
        return  md.digest(message.getBytes())                ;
    }

    public static byte[] toSHA256(String password) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance(SHA256) ;        
        byte[] passBytes     = password.getBytes()               ;
        return sha256.digest(passBytes)                          ;
    }
    
    public static String toString( byte[] array )    {
       BigInteger    bI   = new BigInteger(1, array) ;
       return bI.toString(16)                        ;
    }

    private Digestor() {
    }

}
