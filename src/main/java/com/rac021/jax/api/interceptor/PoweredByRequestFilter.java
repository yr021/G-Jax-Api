
package com.rac021.jax.api.interceptor ;

/**
 *
 * @author ryahiaoui
 */

import java.io.IOException ;
import javax.ws.rs.ext.Provider ;
import javax.ws.rs.container.PreMatching ;
import javax.ws.rs.container.ContainerRequestFilter ;
import javax.ws.rs.container.ContainerRequestContext ;

@Provider
@PreMatching
public class PoweredByRequestFilter implements ContainerRequestFilter {
    
   @Override
   public void filter(ContainerRequestContext ctx) throws IOException {
   }
} 

