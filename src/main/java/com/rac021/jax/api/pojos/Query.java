
package com.rac021.jax.api.pojos ;

import java.util.Map ;
import java.util.HashMap ;
import java.util.LinkedHashMap ;

/**
 *
 * @author ryahiaoui
 */

public class Query {
    
    public static final  String FULL_NAME = "FULL" ;
    public static final  String TYPE      = "TYPE" ;
    
    public boolean containsAggregationFunction = false ;
    
    private String query ;
    
    Map<String, Map<String, String> > parameters = new LinkedHashMap<>() ;

    public Query(String query ) {
        this.query = query ;
    }
    public Query() {
    }

    public String getQuery() {
        return query ;
    }

    public void setQuery(String query) {
        this.query = query ;
    }
    
    public void register ( String name, String fullName, String type )   {
          this.parameters.computeIfAbsent( name , 
                                           k -> new HashMap<>()).put(FULL_NAME, fullName) ;
          this.parameters.computeIfAbsent( name , 
                                           k -> new HashMap<>()).put(TYPE, type )         ;
    }
    
    public String getFullName( String name ) {
        return parameters.get(name).getOrDefault(FULL_NAME, null) ;
    }
    
    public String getType( String name ) {
        return parameters.get(name).getOrDefault(TYPE, null) ;
    }
    
    public int size(  )          {
        return parameters.size() ;
    }

    public Map<String, Map<String, String>> getParameters() {
        return parameters ;
    }

    public boolean isContainsAggregationFunction() {
        return containsAggregationFunction ;
    }

    public void setContainsAggregationFunction(boolean containsAggregationFunction) {
        this.containsAggregationFunction = containsAggregationFunction ;
    }

}