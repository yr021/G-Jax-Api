
package com.rac021.jax.api.streamers ;

import java.util.Set ;
import java.util.List ;
import java.util.HashSet ;
import java.util.ArrayList ;
import javax.inject.Inject ;
import javax.xml.bind.Marshaller ;
import javax.xml.bind.JAXBContext ;
import javax.annotation.PreDestroy ;
import javax.xml.bind.JAXBException ;
import javax.annotation.PostConstruct ;
import javax.persistence.EntityManager ;
import javax.ws.rs.core.MultivaluedMap ;
import com.rac021.jax.api.manager.IDto ;
import java.util.concurrent.BlockingQueue ;
import java.util.concurrent.CountDownLatch ;
import javax.persistence.PersistenceContext ;
import com.rac021.jax.api.manager.IResource ;
import javax.enterprise.context.RequestScoped ;
import java.util.concurrent.ArrayBlockingQueue ;
import com.rac021.jax.api.analyzer.SqlAnalyzer ;
import com.rac021.jax.api.root.ServicesManager ;
import org.eclipse.persistence.jaxb.MarshallerProperties ;
import static com.rac021.jax.api.streamers.DefaultStreamerConfigurator.* ;

/**
 *
 * @author yahiaoui
 */

@RequestScoped
public class Streamer implements IStreamer {
 
    @PersistenceContext  ( unitName  = "MyPU" )
    private EntityManager entityManager       ;

    @Inject 
    protected ServicesManager servicesManager ;
    
    protected     BlockingQueue<IDto> dtos                                   ;
    
    protected     CountDownLatch      latch                                  ;
    
    protected     int                 maxThreads                             ;
    
    private       int                 indexRequest                           ;
    
    protected     ResourceWraper      resource                               ;
    
    private       List<String>        filterdNames           = null          ;
    
    protected     boolean             isFinishedProcess      = false         ;
    
    private final Object              lock                   = new Object()  ;
   
    private final Set<Integer>        producerIndexIteration = new HashSet() ;
    
    
    @PreDestroy   
    public void cleanup() {
      if( maxConcurrentUsers > 0 ) {
          releaseSemaphore() ;
      }
    }
     
    @PostConstruct
    public void init() {
      if( maxConcurrentUsers > 0 ) {
          acquireSemaphore() ;
      }
    }
    
    public Streamer() { }

    public void producerScheduler()                 {
        
        latch  = new CountDownLatch( maxThreads )   ;
        
        List<Producer> producers = new ArrayList()  ;

        resource.initResource( selectSize * ratio ) ;

        for ( int i = 0 ; i < maxThreads ; i++ )    {
            producers.add(new Producer()) ;
        }

        try {
            
             producers.stream().forEach((producer) -> {
                poolProducer.submit(producer) ;
             }) ;
            
             latch.await() ;

             isFinishedProcess = true ;
              
        } catch ( InterruptedException ex )        {
            System.out.println ( ex.getMessage() ) ;
        }
    }

   
    public synchronized void getNextRequestWithIndex( int index, int sizeResult ) {
        synchronized (lock) {
            if (!producerIndexIteration.contains(index) && sizeResult == 0 ) {
                 producerIndexIteration.add(index)       ;
                 indexRequest ++                         ; 
                 resource.setOffset(-selectSize * ratio) ;
            }
        }
    }

    public synchronized int getIndexRequest() {
        synchronized (lock)     {
            return indexRequest ;
        }
    }

    protected void configureStreamer() {
  
      if( DefaultStreamerConfigurator.maxThreads != null )         {
          this.maxThreads = DefaultStreamerConfigurator.maxThreads ;
       } else {
         this.maxThreads = servicesManager.getOrDefaultMaxThreadsFor ( 
                               resource.getResource().getClass().getName() 
                           ) ;
      }
     
      dtos = new ArrayBlockingQueue<>( ratio * selectSize * this.maxThreads ) ;
       
    }

    protected class Producer implements Runnable {

        @Override
        
        public void run() {
            
           int localIndexRequest = 0 ;

           while ( ! resource.isFinished() )   {

              localIndexRequest = getIndexRequest() ;
              
              List<IDto> dtoIterable = resource.getDtoIterable( entityManager      , 
                                                                localIndexRequest  , 
                                                                selectSize * ratio ,  
                                                                filterdNames     ) ;
                
              if (dtoIterable == null || dtoIterable.isEmpty() ) {
                    
                  resource.setIsFinished(true) ;
                  break                        ;
                    
              } else {
                  
                  dtoIterable.stream().forEach((localDto) -> {
                         try {   
                             dtos.put(localDto)   ;
                         } catch (Exception ex)   {
                             System.out.println( ex.getMessage()) ;
                         } 
                  }) ;
              }

              getNextRequestWithIndex(localIndexRequest, dtoIterable.size()) ;
           }

           latch.countDown() ; 
        }
    }
    
    public void rootResourceWraper( IResource resource        ,
                                    Class     dto             , 
                                    String     filteredNames  , 
                                    MultivaluedMap <String ,String> ... sqlParams ) {
        
        this.filterdNames     = toListNames(filteredNames) ;
        List<String>  filters =  new ArrayList<>()         ;
        
        if(sqlParams != null ) {
           filters = SqlAnalyzer.buildFilters ( servicesManager.getQueriesByResourceName ( 
                                                       resource.getClass().getName() )   , 
                                                sqlParams[0] ) ;
        }

        this.resource = new ResourceWraper( resource, dto, filters ) ;
    }
    
    
    protected List<String> toListNames( String names ) {
        
      if( names != null ) {
         List<String> l = new ArrayList<>()  ;
         String[] split = names.split("-")   ;
         for( String fieldName : split)  {
             l.add( fieldName.trim().replaceAll(" +", "")) ;
         }
         return l ;
      }
      
      return null ;
    }
    
   
    protected Marshaller getMashellerWithJSONProperties() {
        
        Marshaller localMarshaller= null ;
        
        try {
            JAXBContext jc  = JAXBContext.newInstance(resource.getDto(), EmptyPojo.class)      ;
            localMarshaller = jc.createMarshaller()                                            ;
            localMarshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json")   ;
            localMarshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, Boolean.FALSE) ;
            localMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE)        ;
        } catch (JAXBException ex) {
            System.out.println( ex.getMessage() ) ;
        }
        
        return localMarshaller ;
    }
    
    
    protected Marshaller getMashellerWithXMLProperties() {
       
        Marshaller localMarshaller = null ;
        try {
            JAXBContext jxbContect = JAXBContext.newInstance( resource.getDto(), EmptyPojo.class) ;
            localMarshaller        = jxbContect.createMarshaller()                                ;
            localMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE)           ;
            localMarshaller.setProperty("com.sun.xml.bind.xmlHeaders", "")                        ;
            localMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true)                           ;
            
        } catch (JAXBException ex) {
            System.out.println( ex.getMessage() ) ;
        }
        return localMarshaller ;
    }
    
    public static void releaseSemaphore() {
        
     try {
            semaphoreMaxConcurrentUsers.release() ;
        } catch( Exception x )                    {
            System.out.println( x.getMessage())   ;
            semaphoreMaxConcurrentUsers.release() ;
        }
     }
    public static void acquireSemaphore() {
        
      try {
            semaphoreMaxConcurrentUsers.acquire() ;
        } catch( InterruptedException x )         {
            System.out.println( x.getMessage())   ;
            semaphoreMaxConcurrentUsers.release() ;
        } 
     }
    
    @Override
    public void setStreamerConfigurator(IStreamerConfigurator iStreamerConfigurator) {
        this.maxThreads = iStreamerConfigurator.getMaxThreads() ;
    }
    
}
