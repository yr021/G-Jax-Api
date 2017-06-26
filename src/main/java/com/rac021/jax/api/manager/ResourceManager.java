
package com.rac021.jax.api.manager ;

import java.util.Map ;
import java.util.List ;
import java.util.HashMap ;
import java.util.ArrayList ;
import javax.persistence.Query ;
import javax.persistence.EntityManager ;

/**
 *
 * @author yahiaoui
 */

public class ResourceManager {

    private final   Object    lockOffset = new Object() ;

    protected int                 offset = 0            ;

    protected volatile boolean    finish = false        ;

    protected Map<String, Object> queryListParameter    ;

    protected Map<String, Class>  addEntityParameter    ;

    protected Map<String, String> addJoinParamater      ;


    public ResourceManager() {

        this.queryListParameter = new HashMap()   ;
        this.addEntityParameter = new HashMap()   ;
        this.addJoinParamater   = new HashMap()   ;

    }

    private synchronized int incOffset(int limit) {
        synchronized (lockOffset) {
            this.offset += limit ;
            return this.offset   ;
        }
    }

    private void setLimitOffsetSQLParameter( Query query, int limit, int offset) {
        query.setParameter( 1 , offset) ;
        query.setParameter( 2 , limit)  ;
    }
  
    protected List<IDto> executeSQLQuery( EntityManager manager      , 
                                          String sqlQuery            ,
                                          int limit, Class dtoClass  ,
                                          List<String> filterdIndex) {
        
        List<IDto> list = new ArrayList()                       ;

        if(sqlQuery == null || sqlQuery.isEmpty() ) return list ;

        try {

            long start    = System.currentTimeMillis()                   ;
            
            Query createSQLQuery = manager.createNativeQuery(sqlQuery)   ;
            
            int incOffset = incOffset(limit) ;
            setLimitOffsetSQLParameter(createSQLQuery, limit, incOffset) ;
           
            list          = DtoMapper.map(createSQLQuery.getResultList(), dtoClass, filterdIndex )   ;
            long duration = System.currentTimeMillis() - start                                       ;
          
            System.out.println( " Size Of the Result List ---> " + list.size() + " // DURATION  --> " 
                                + duration  + " ms // Limit  " + limit + " // Offset " + incOffset ) ;

            System.out.println(" *************************************************************" )    ;

        } catch ( Exception ex)  {
            ex.printStackTrace() ;
        }

        return list ;
    }

    protected synchronized void decOffset(int limit) {
        synchronized (lockOffset) {
            this.offset -= limit ;
        }
    }

}
