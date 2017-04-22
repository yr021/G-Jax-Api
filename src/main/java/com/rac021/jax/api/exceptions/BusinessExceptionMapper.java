
package com.rac021.jax.api.exceptions ;

import javax.ws.rs.ext.Provider ;
import javax.ws.rs.core.Response ;
import javax.ws.rs.core.MediaType ;
import javax.ws.rs.ext.ExceptionMapper ;

/**
 *
 * @author yahiaoui
 */

@Provider
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {

    @Override
    public Response toResponse(BusinessException exception) {
        return Response.status(Response.Status.OK).header("x-reason",
                exception.getMessage()).entity(
                        "<status>" + exception.getLocalizedMessage() + "</status>").
                type(MediaType.APPLICATION_XML).build() ;
    }
}
