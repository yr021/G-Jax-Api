
package com.rac021.jax.api.streamers ;

import java.util.List ;
import java.util.ArrayList ;
import javax.persistence.EntityManager ;
import com.rac021.jax.api.manager.IDto ;
import com.rac021.jax.api.manager.IResource ;

/**
 *
 * @author ryahiaoui
 */

public class ResourceWraper extends com.rac021.jax.api.manager.ResourceManager {
   
    private   final IResource     resource    ;
    private   final Class         dto         ;
    protected final List<String>  queries     ;
    
    
    public ResourceWraper( IResource resource , 
                           Class dto          ,  
                           List<String> queries ) {
        
        this.resource    = resource    ;
        this.dto         = dto         ;
        this.queries     = queries     ;
    }

    public IResource getResource() {
        return resource ;
    }

    public Class getDto() {
        return dto ;
    }
    
    public List<IDto> getDtoIterable( EntityManager em , 
                                      int indexRequest , 
                                      int limit        , 
                                      List<String> filterdIndex)  {
     
        if( indexRequest < queries.size() ) {
            
            return executeSQLQuery( em                        ,
                                    queries.get(indexRequest) , 
                                    limit                     , 
                                    dto                       , 
                                    filterdIndex            ) ;
        }
        return new ArrayList<>() ;
    }


    public void initResource( int limit ) {
        decOffset(limit)     ;
        initQueryParameter() ;
    }

    public synchronized void setOffset( int offset ) {
        this.offset = offset ;
    }

    public boolean isFinished() {
        return finish ;
    }
    public void setIsFinished( boolean status ) {
        finish = status ;
    }

    private void initQueryParameter() {
    }

}
