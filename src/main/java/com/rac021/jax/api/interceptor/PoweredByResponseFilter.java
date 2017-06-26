
package com.rac021.jax.api.interceptor; 

/**
 *
 * @author ryahiaoui
 */

import java.io.IOException ;
import javax.ws.rs.ext.Provider ;
import javax.ws.rs.container.PreMatching ;
import javax.ws.rs.container.ContainerRequestContext ;
import javax.ws.rs.container.ContainerResponseFilter ;
import javax.ws.rs.container.ContainerResponseContext ;

@Provider
@PreMatching
public class PoweredByResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter( ContainerRequestContext requestContext, 
                        ContainerResponseContext responseContext) throws IOException {
    }
} 

