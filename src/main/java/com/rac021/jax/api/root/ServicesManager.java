
package com.rac021.jax.api.root ;

import java.util.Set ;
import java.util.Map ;
import java.util.List ;
import java.util.Arrays ;
import java.util.HashSet ;
import javax.ejb.Startup ;
import java.util.HashMap ;
import javax.inject.Inject ;
import javax.ejb.Singleton ;
import java.util.ArrayList ;
import java.sql.Connection ;
import javax.ws.rs.Produces ;
import java.util.stream.Stream ;
import java.util.logging.Level ;
import java.lang.reflect.Field ;
import java.lang.reflect.Method ;
import java.util.logging.Logger ; 
import javax.annotation.PostConstruct ;
import com.rac021.jax.api.pojos.Query ;
import javax.persistence.EntityManager ;
import java.lang.annotation.Annotation ;
import javax.enterprise.inject.spi.Bean ;
import javax.persistence.PersistenceContext ;
import com.rac021.jax.api.crypto.AcceptType ;
import com.rac021.jax.api.crypto.CipherTypes ;
import com.rac021.jax.api.qualifiers.SqlQuery ;
import com.rac021.jax.api.analyzer.SqlAnalyzer ;
import javax.enterprise.inject.spi.BeanManager ;
import static java.util.stream.Collectors.toList ;
import javax.enterprise.context.ApplicationScoped ;
import com.rac021.jax.api.qualifiers.ServiceRegistry ;
import com.rac021.jax.api.qualifiers.security.Cipher ;
import com.rac021.jax.api.qualifiers.security.Policy ;
import com.rac021.jax.api.qualifiers.ResourceRegistry ;
import com.rac021.jax.api.qualifiers.security.Secured ;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner ;

/**
 *
 * @author ryahiaoui
 */

@Singleton
@Startup
@ApplicationScoped

public class ServicesManager {
    
    @PersistenceContext  (unitName = "MyPU")
    private EntityManager entityManager ;

    private final Map<String, Object>             publicServices        = new HashMap<>() ;
    private final Map<String, Object>             customSignOnServices  = new HashMap<>() ;
    private final Map<String, Object>             ssoServices           = new HashMap<>() ;
    private final Map<String, List<Query> >       resources             = new HashMap<>() ;
    
    private final Map<String, Set<CipherTypes> >  ciphers               = new HashMap<>() ;
    private final Map<String, Set<AcceptType> >   accept                = new HashMap<>() ;
    
    private final Map<String, Integer >           maxThreadsPerService = new HashMap<>()  ;
   
    @Inject
    private BeanManager bm ;
    
    @PostConstruct
    public void init() {
     scanAndRegisterRealServices() ;
    }
    
    public ServicesManager() {
    }

    
    public void registerService( String id, Object service ) {
        
        if ( service.getClass().getAnnotation(Secured.class) != null ) {
            Secured annotation = service.getClass().getAnnotation(Secured.class);
            String  policy     = annotation.policy().name()  ;
            if ( policy.equalsIgnoreCase(Policy.CustomSignOn.name()) ) {
                this.customSignOnServices.put( id, service ) ;
            }
            else if ( policy.equalsIgnoreCase(Policy.SSO.name()) ) {
                this.ssoServices.put( id, service ) ;
            }
            else {
                this.publicServices.put( id, service ) ;
            }
        }
        else {
            this.publicServices.put( id, service ) ;
        }
    }
    
    public void registerCiphers ( String id, List<CipherTypes> ciphers ) {
         this.ciphers.computeIfAbsent( id, k -> new HashSet(ciphers) ) ;
    }
    
    public void registerAcceptTypes ( String id, List<AcceptType> accepts ) {
         this.accept.computeIfAbsent( id, k -> new HashSet(accepts) ) ;
    }
    
    
            
    public  Object get( String id )  {
      return publicServices.getOrDefault(        id , 
              customSignOnServices.getOrDefault( id , 
                      ssoServices.getOrDefault(  id , null )))  ;
    }
    
    public Policy contains( String idService ) {
      if(publicServices.containsKey(idService)) return Policy.Public             ;
      if(customSignOnServices.containsKey(idService)) return Policy.CustomSignOn ;
      if(ssoServices.containsKey(idService)) return Policy.SSO                   ;
      return null                                                                ;
    }

    public  int total () {
      return publicServices.size() ;
    }

    public Map<String, Object> getMapOfAllSubServices() {
   
       Map<String, Object>    res = new HashMap() ;
       res.putAll( publicServices )               ;
       res.putAll( customSignOnServices )         ;
       res.putAll( ssoServices )                  ;
       return res ;
       
    }

    public Map<String, List<Query>> getMapResources() {
        return resources ;
    }
    
    private void scanAndRegisterRealServices()  {
    
       List<String> classes  = scanRealServices( ServiceRegistry.class    , 
                                                 ResourceRegistry.class   , 
                                                 SqlQuery.class         ) ;
       
       for( String clazzName : classes ) {
        
          try {
              
               Class<?>        service         = Class.forName(clazzName) ;
               ServiceRegistry serviceRegistry = service.getAnnotation(ServiceRegistry.class)           ;
               String          serviceName     = service.getAnnotation( ServiceRegistry.class ).value() ;
             
               Bean<Object> bean = (Bean<Object>) bm.resolve(bm.getBeans(service, serviceRegistry ) )   ;
              
               if(bean != null ) {
                   
                   Object cdiService = (Object) bm.getReference( bean, bean.getBeanClass(), 
                                                                 bm.createCreationalContext(bean) )     ;
                   registerService( serviceName , cdiService ) ;
                   
                   Cipher cipherAnnotation   = getInstanceAnnotationFromClass( cdiService.getClass()    ,
                                                                               Cipher.class )           ;
                   
                   if( cipherAnnotation != null ) {
                       registerCiphers( serviceName , 
                                        Arrays.asList(cipherAnnotation.cipherType()) )                  ;
                   }
                   
                   Method[] methods     = cdiService.getClass().getMethods()                            ;

                   List<String> typeList = new ArrayList()                                              ;
                   boolean existProduces = false                                                        ;
                   
                   for (final Method method : methods) {
                       
                     Annotation[] annotations = method.getAnnotations() ;
                       
                     if (method.isAnnotationPresent(Produces.class))    {
                       
                         List<String> types = Stream.of ( method.getAnnotation(Produces.class).value())
                                                    .map ( type -> type.toUpperCase())
                                                    .collect(toList()) ; 
                         typeList.addAll(types) ;
                           
                        } else {
                           existProduces = true ;                                
                        }
                        
                     }
                   
                     if ( ! typeList.isEmpty() ) {
                         registerAcceptTypes(serviceName, AcceptType.toList(typeList)) ;
                     }
                     
                     else if( existProduces && typeList.isEmpty() ) {
                        registerAcceptTypes(serviceName, Arrays.asList( AcceptType.XML_PLAIN        ,
                                                                        AcceptType.XML_ENCRYPTED    , 
                                                                        AcceptType.JSON_PLAIN       ,
                                                                        AcceptType.JSON_ENCRYPTED)) ;
                     }                                      
               }
          
          } catch(ClassNotFoundException x) {
              x.printStackTrace() ;
           }
       }
    }
    
    private List<String > scanRealServices ( Class serviceAnnotation, Class registryAnnotation , Class queryAnnotation ) {
        
        List<String> namesOfAnnotationsWithMetaAnnotation = new FastClasspathScanner().scan()
                                                                .getNamesOfClassesWithAnnotation(registryAnnotation) ;
        
        namesOfAnnotationsWithMetaAnnotation.forEach ( resource -> {  try {
                 extractAndRegisterQueries( Class.forName(resource),queryAnnotation );
             } catch ( ClassNotFoundException ex) {
                  Logger.getLogger(ServicesManager.class.getName()).log(Level.SEVERE, null, ex) ;
             }
        } ) ;
        
        return new FastClasspathScanner().scan()
                                         .getNamesOfClassesWithAnnotation( serviceAnnotation ) ;
    }
    

    private <T> T getInstanceAnnotationFromClass( Class clazz, Class<T> annotToFind ) {
        
        Annotation[] annotations = clazz.getAnnotations() ;

        for(Annotation annotation : annotations ) {
            
          if ( annotToFind.isAssignableFrom(annotation.getClass())) {
              return (T) annotation ;             
          }
        }
    
       return null ;
    }
    
    public void extractAndRegisterQueries (Class resource, Class queryAnnotation ) {
      
     Connection cnn  = entityManager.unwrap(java.sql.Connection.class )  ;
     Class<?>     c  = resource                                          ;
          
     try {
            Object instance  = c.newInstance()   ;
            while (c != null) {
               for (Field field : c.getDeclaredFields()) {
                   if (field.isAnnotationPresent(queryAnnotation)) {
                       field.setAccessible(true) ;
                       if(field.getType().toString().equals("class java.lang.String")) {
                           String sqlQUery = (String) field.get( instance ) ;
                           if( sqlQUery != null ) 
                           addResource(resource.getName(), SqlAnalyzer.buildQueryObject( cnn, sqlQUery) ) ;
                       }
                   }
               }
               c = c.getSuperclass() ;
           }
     } catch ( Exception x)  {
         x.printStackTrace() ;
     }
    }

    public Field getFieldFor( Class clazz , Class annotation ) {
 
        for( Field field  : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(annotation)) {
                      return field ;
           }
        }
        
        return null ;
    }
    
    public Set<CipherTypes> getCiphersForServiceName( String serviceName ) {
        return ciphers.getOrDefault(serviceName, new HashSet<>() ) ;
    }
    
    public Set<AcceptType> getAcceptForServiceName( String serviceName )   {
        return accept.getOrDefault(serviceName, new HashSet<>() ) ;
    }
    
    public boolean containCiphersForService( String serviceName, String cipher ) {
        return ciphers.containsKey(serviceName) ?
               ciphers.get(serviceName)
                      .contains( CipherTypes.toCipherTypes(cipher)) :
               false ;
    }
    
    public boolean containAcceptForService( String serviceName, String accept )  {
        return this.accept.containsKey(serviceName) ? 
               this.accept.get(serviceName)
                          .contains( AcceptType.toAcceptTypes( 
                                        accept.toUpperCase().replace("/", "_"))) :
               false ;
    }
   
    public void addResource( String resourceName, Query query) {
          this.resources.computeIfAbsent( resourceName , 
                                          k -> new ArrayList<>()).add(query) ;
    }
    
    public List<Query> getQueriesByResourceName( String resourceName ) {
        return resources.getOrDefault(resourceName, null ) ;
    }

    public void apllyMaxThreads(String serviceCode , int maxThreads )  {
       this.maxThreadsPerService.put(serviceCode, maxThreads ) ;
    }
    public int getOrDefaultMaxThreadsFor(String serviceCode ) {
       return this.maxThreadsPerService.getOrDefault(serviceCode , 4 ) ;
    }

}
