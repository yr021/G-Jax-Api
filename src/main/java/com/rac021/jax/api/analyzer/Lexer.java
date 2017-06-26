
package com.rac021.jax.api.analyzer ;

import java.util.Map ;
import java.util.List ;
import java.util.ArrayList ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;
import java.util.stream.Stream ;
import java.util.stream.Collectors ;
import com.rac021.jax.api.pojos.Query ;
import javax.ws.rs.core.MultivaluedMap ;
import com.rac021.jax.api.exceptions.BusinessException ;

/**
 *
 * @author ryahiaoui
 */
public enum Lexer {
    
   G   ( "_>_"   ) ,
   L   ( "_<_"   ) ,
   or  ( "_or_"  ) ,
   OR  ( "_OR_"  ) ,
   not ( "_not_" ) ,
   NOT ( "_NOT_" ) ,
   and ( "_and_" ) ,
   AND ( "_AND_" ) ,
   GE  ( "_>=_"  ) ,
   LE  ( "_<=_"  ) ,
   EQ  ( "_=_"   ) ;
   
   static final String  expectStrings  = "= > >= < != "  ;
   
   static final Pattern patternSyntax  = Pattern.compile("(.*?^(_or|_OR|_and|_AND|_NOT|_not))\"") ;
   
   static final String  splitOnSpaceIgnoreSingleQuote = "\'?( |$)(?=(([^\']*\'){2})*[^\']*$)\'?"  ;
           
   static final ThreadLocal<String> expected = new ThreadLocal<>() ;
            
   private final String value ;

   
   private Lexer(String value) {
      this.value = value ;
   }

   @Override
   public String toString()
   {
      return this.value ; 
   }

   private static String decode( Lexer lexer ) {
     
       switch ( lexer )              {
           case OR  :
           case or  : return " OR "  ;
           case NOT : 
           case not : return " NOT"  ;
           case AND :
           case and : return " AND " ;
           case G   : return " > "   ;
           case L   : return " < "   ;
           case LE  : return " <= "  ;
           case GE  : return " >= "  ;
           case EQ  : return " = "   ;
           default  : return null    ;       
       }
   }
   
   public static List<String> decodeExpression(  Query query , 
                                                 MultivaluedMap<String, String> sqlParams ) {
       
        List<String> processedQ = new ArrayList<>() ;
         
         sqlParams.forEach((key, list) -> list.forEach( v -> {
                                            if( query.getParameters().containsKey(key))
                                            processedQ.add( "( " + query.getParameters().get(key)
                                                                        .get( Query.FULL_NAME )  +
                                                                        " " +
                                                                   _process( key                            , 
                                                                             v.replaceAll(" +", " ").trim() , 
                                                                             query.getParameters() 
                                                                           )
                                                          ) ;
                                           }
                          )) ;

         System.out.println("processedQ  = " + processedQ ) ;
         
         return processedQ ;
   }

    
  
   public static String decodeExpression( String key , String expression ) { 
         
       /* 
         code=_=_10_or_>=_20_NOT_"_or_"
         code= = _10 or >= _20 not _"_or_"
       */
         
       Matcher matcher = patternSyntax.matcher(expression) ;
        if (matcher.find()) {
            System.out.println(matcher.group(1)) ;
        }
        
      return expression.replace( G.value  ,  decode( G   ))
                       .replace( L.value   , decode( L   ))
                       .replace( EQ.value  , decode( EQ  ))
                       .replace( OR.value  , decode( OR  ))
                       .replace( or.value  , decode( or  ))
                       .replace( NOT.value , decode( NOT ))
                       .replace( LE.value  , decode( LE  ))
                       .replace( GE.value  , decode( GE  ))
                       .replace( AND.value , decode( AND ))
                       .replace( and.value , decode( and )) ;
   }
   
   
   private static String _process( String key, String value , 
                                    Map<String, Map<String, String>> columnNames ) {
    
       if( ! columnNames.containsKey(key) ) return "" ;
         
       expected.set(expectStrings) ;
         
       if( value.isEmpty()) return " " ;
         
       if( ! value.startsWith("=" )   && 
           ! value.startsWith("_>")   && 
           ! value.startsWith(">")    && 
           ! value.startsWith("<")    && 
           ! value.startsWith("_NOT") && 
           ! value.startsWith("_not") && 
           ! value.startsWith("_<") )  {
           
           value = "_=_" + value ;
       }
         
       if( ! value.startsWith("_")) value = "_"+ value ;
 
       value = value.replace( "_or"  , " OR " )
                    .replace( "_OR"  , " OR " )
                    .replace( "_and" , " AND ")
                    .replace( "_NOT" , " != " )
                    .replace( "_not" , " != " )
                    .replace( "_>="  , " >= " )
                    .replace( "_>"   , " > "  )
                    .replace( "_="   , " = "  ) ;

       return  Stream.of(value.split(splitOnSpaceIgnoreSingleQuote))
                     .map( tok -> {
                              try {
                               return  process( columnNames.get(key).get(Query.FULL_NAME), 
                                                 tok.replaceAll(" +", " ").trim() ) ;                  
                              } catch( Exception x ) {
                                  System.out.println( x.getCause()) ;
                              }
                              return "" ;
                           })
                     .collect(Collectors.joining("")) ;
       
   }
     
   private static String process(String key, String token) throws BusinessException {
         
     if( token.isEmpty()) return " " ;
         
     else if( token.equalsIgnoreCase("=")   && expected.get().contains(token.toLowerCase()) )  { 
         expected.set("value") ; return "= " ;            
     } 
         
     else if( token.equalsIgnoreCase("or")  && expected.get().contains(token.toLowerCase()) )  {  
         expected.set(">= and > < <= != ") ; return " OR (" + key ;   
     }
         
     else if( token.equalsIgnoreCase(">=")  && expected.get().contains(token.toLowerCase()) )  { 
         expected.set("value")  ; return " >= " ;          
     }
         
     else if( token.equalsIgnoreCase("and") && expected.get().contains(token.toLowerCase()) )  { 
         expected.set(">= > = or != ") ; return " AND (" + key  ; 
     }
         
     else if( token.equalsIgnoreCase(">")   && expected.get().contains(token.toLowerCase()) )  { 
         expected.set("value") ; return "> " ;            
     }
        
     else if( token.startsWith( "!=" )      && expected.get().contains(token.toLowerCase()) )  { 
         expected.set("value")  ; return  "!= " ;          
     } 
         
     else if( token.startsWith("_")  &&  expected.get().contains("value") ) { 
                 expected.set("and or ")                 ; 
                 return "'" + token.replaceAll("\"", ""  )
                                   .replaceAll("'", ""   )
                                   .replaceFirst("_", "" )
                                   + "')"  ; 
     }
       
     else if( token.length() > 0 ) { 
         throw new BusinessException ( " token  [ " + token + 
                                       " ] not accepted // Key = " + key ) ;
     }
        
        else return "" ;
   }
  
}
