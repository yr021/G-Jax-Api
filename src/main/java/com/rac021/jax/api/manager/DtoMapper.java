
package com.rac021.jax.api.manager ;

import java.util.List ;
import java.util.Arrays ;
import java.util.ArrayList ;
import java.lang.reflect.Field ;
import com.rac021.jax.api.qualifiers.ResultColumn ;

/**
 *
 * @author ryahiaoui
 */

public class DtoMapper {

    public static <T> List<T> map( List<Object[]> objectArrayList , 
                                   Class<T> genericType           , 
                                   List<String> filterdIndex   )  {
        
        List<T> ret = new ArrayList<>()                               ;
        if(objectArrayList.isEmpty()) return ret                      ;
        List<Field> mappingFields = getAnnotatedFields( genericType ) ;
        
        try {
            for (Object[] objectArr : objectArrayList) {
                T t = genericType.newInstance();
                for (int i = 0; i < objectArr.length; i++) {
                    if( i < mappingFields.size() ) {
                        Field field = t.getClass()
                                       .getDeclaredField( mappingFields.get(i)
                                       .getName()) ;
                        if( filterdIndex  != null   &&
                            !filterdIndex.isEmpty() && 
                            !filterdIndex.contains(field.getName())
                            )  continue ;
                        
                           // if(field.getAnnotation(Public.class) != null ) {
                           field.setAccessible(true)    ;
                           field.set( t , objectArr[i]) ; 
                           //  }
                    }
                }
                ret.add(t) ;
            }
        } catch (Exception ex)  {
           ex.printStackTrace() ;
           ret.clear()          ;
        }
        
        return ret ;
    }

    private static <T> List<Field> getAnnotatedFields (Class<T> genericType ) {
        
        Field[] fields            = genericType.getDeclaredFields()         ;
        
        List<Field> orderedFields = Arrays.asList(new Field[fields.length]) ;
        
        for (int i = 0; i < fields.length; i++)  {
            if (fields[i].isAnnotationPresent( ResultColumn.class )) {
                ResultColumn nqrc = fields[i].getAnnotation(ResultColumn.class) ;
                orderedFields.set(nqrc.index(), fields[i])                      ;
            }
        }
        return orderedFields ;
    }
}
