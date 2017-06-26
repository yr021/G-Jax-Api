
package com.rac021.jax.api.root ;

import javax.ws.rs.PathParam ;
import javax.ws.rs.HeaderParam ;
import com.rac021.jax.api.exceptions.BusinessException ;

/**
 *
 * @author ryahiaoui
 */

public interface IRootService {

    Object subResourceLocators(
            @HeaderParam("API-key-Token") String token  ,
            @HeaderParam("accept")        String accept ,
            @HeaderParam("cipher")        String cipher ,
            @PathParam("codeService")     String codeService) throws BusinessException ;

    Object authenticationCheck(
            @PathParam("login") String login         ,
            @PathParam("signature") String signature ,
            @PathParam("timeStamp") String timeStamp) throws BusinessException ;

}
