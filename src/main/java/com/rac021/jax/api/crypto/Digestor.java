
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
    
    public static String digestMD5(final String password) throws NoSuchAlgorithmException{
        
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] thedigest = md.digest(password.getBytes(StandardCharsets.UTF_8)) ;
        BigInteger bI = new BigInteger(1, thedigest) ;
        return bI.toString(16) ;
    }
   
    public static String digestSha1(String message) throws NoSuchAlgorithmException {
        
        MessageDigest md   = MessageDigest.getInstance("SHA-1") ;
        byte[]        hash = md.digest(message.getBytes())      ;
        BigInteger    bI   = new BigInteger(1, hash)           ;

        return bI.toString(16) ;
    }

    public static String generateSignature(String login, String password, String timeStamp) throws NoSuchAlgorithmException {
        return digestSha1( login + password + timeStamp ) ;
    }

    private Digestor() {
    }

}
