
package com.rac021.jax.api.streamers ;

import java.util.Set ;
import java.util.List ;
import java.util.HashSet ;
import java.util.ArrayList ;
import javax.inject.Inject ;
import javax.xml.bind.Marshaller ;
import javax.xml.bind.JAXBContext ;
import javax.xml.bind.JAXBException;
import java.util.concurrent.Executors ;
import javax.persistence.EntityManager ;
import javax.transaction.Transactional ;
import javax.ws.rs.core.MultivaluedMap ;
import com.rac021.jax.api.manager.IDto ;
import java.util.concurrent.BlockingQueue ;
import java.util.concurrent.CountDownLatch ;
import java.util.concurrent.ExecutorService ;
import javax.persistence.PersistenceContext ;
import com.rac021.jax.api.manager.IResource ;
import javax.enterprise.context.RequestScoped ;
import java.util.concurrent.ArrayBlockingQueue ;
import com.rac021.jax.api.analyzer.SqlAnalyzer ;
import com.rac021.jax.api.root.ServicesManager ;
import org.eclipse.persistence.jaxb.MarshallerProperties ;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner ;

/**
 *
 * @author yahiaoui
 */
@RequestScoped
public class Streamer {

    @PersistenceContext  (unitName = "MyPU")
    private EntityManager entityManager    ;

    @Inject 
    protected ServicesManager servicesManager ;
    
    protected int loooFLush   ;

    protected int BlockSize   ;

    /* Runtime.getRuntime().availableProcessors() */

    protected int cores ;
    
    protected int ratio ;

    protected int recorderLenght ;
    
    private   int indexRequest ;

    private       CountDownLatch latch  ;
    private final Object         lock                   = new Object()   ;
    private final Set<Integer>   producerIndexIteration = new HashSet()  ;
    
    protected IStreamerConfigurator streamerConfigurator ;

    List<String>       filterdNames        = null        ;
    
    protected boolean  isFinishedProcess   = false       ;
    
    protected          BlockingQueue<IDto> dtos          ;

    protected volatile ResourceWraper      resource      ;


    public Streamer() {
    }

    public void producerScheduler() {

        configureStreamer() ;

        ExecutorService poolProducer = Executors.newFixedThreadPool(cores) ;
        latch                        = new CountDownLatch(cores)           ;

        List<Producer> producers     = new ArrayList()                     ;

        resource.initResource(recorderLenght * ratio) ;
        for (int i = 0; i < cores; i++) {
            producers.add(new Producer()) ;
        }
        try {
            producers.stream().forEach((producer) -> {
                poolProducer.execute(producer);
            });
        } catch (Exception ex) {
            System.out.println(" Exception : " + ex.getMessage()) ;
        }
    }

   
    public synchronized void getNextRequestWithIndex(int index, int sizeResult)  {
        synchronized (lock) {
            if (!producerIndexIteration.contains(index) && sizeResult == 0 ) {
                 producerIndexIteration.add(index) ;
                 indexRequest ++ ; 
                 resource.setOffset(-recorderLenght * ratio) ;
            }
        }
    }

    public synchronized int getIndexRequest() {
        synchronized (lock) {
            return indexRequest ;
        }
    }

    private void configureStreamer() {

        if ( streamerConfigurator == null ) {
            
            Class overrideConfig = getOverrideOrElseDefaultStreamerConfigurator() ;
            
            if( overrideConfig != null ) {
                try {
                  this.streamerConfigurator = (IStreamerConfigurator) overrideConfig.newInstance() ;
                } catch( Exception x )  {
                    this.streamerConfigurator = new DefaultStreamerConfigurator() ;
                }
            }
            else {
              this.streamerConfigurator = new DefaultStreamerConfigurator() ;
            }
        }
        
        this.cores          = streamerConfigurator.getNbrCores()       ;
        this.ratio          = streamerConfigurator.getRatio()          ;
        this.recorderLenght = streamerConfigurator.getRecorderLenght() ;
        this.loooFLush      = streamerConfigurator.getLoopFlush()      ;
        this.BlockSize      = streamerConfigurator.getBlockSize()      ;

        dtos   = new ArrayBlockingQueue<>(this.recorderLenght)         ;
    }

    private Class getOverrideOrElseDefaultStreamerConfigurator()         {
  
      String interfConfig  = IStreamerConfigurator.class.getName()       ;
      String defaultConfig = DefaultStreamerConfigurator.class.getName() ;
      
      List<String> overrConfigList = 
          new FastClasspathScanner().scan().getNamesOfClassesImplementing ( interfConfig ) ;
          
      Class override = overrConfigList.stream()
                                      .filter( conf -> ! conf.equals(defaultConfig) )
                                      .findFirst()
                                      .map( fullName -> { try {
                                              return  Class.forName( fullName ) ; 
                                            } catch (Exception ex ) {
                                              return null ;
                                            } } )
                                    .orElse( null ) ;
         
      return override ;
    }

    protected class Producer implements Runnable {

        @Override
        @Transactional
        public void run() {
            
            int localIndexRequest = 0 ;

            while (!resource.isFinished())            {
                localIndexRequest = getIndexRequest() ;
                List<IDto> dtoIterable = resource.getDtoIterable( entityManager          , 
                                                                  localIndexRequest      , 
                                                                  recorderLenght * ratio ,  
                                                                  filterdNames         ) ;
                if (dtoIterable == null || dtoIterable.isEmpty() ) {
                    resource.setIsFinished(true) ;
                    break ;
                } else {
                    dtoIterable.stream().forEach((localDto) -> {
                        try {
                            dtos.put(localDto);
                        } catch (InterruptedException ex) {
                        }
                    });
                }
                getNextRequestWithIndex(localIndexRequest, dtoIterable.size());
            }
            latch.countDown() ;
            try {
                latch.await() ;
            } catch (InterruptedException ex) {
                ex.printStackTrace() ; 
            }
            isFinishedProcess = true ;
        }
    }
    
    public void rootResourceWraper( IResource resource     ,
                                    Class dto              , 
                                    String filteredNames   , 
                                    MultivaluedMap <String ,String> ... sqlParams ) {
        
        this.filterdNames     = toListNames(filteredNames) ;
        List<String>  filters =  new ArrayList<>()         ;
        
        if(sqlParams != null ) {
           filters = SqlAnalyzer.buildFilters ( servicesManager.getQueriesByResourceName ( 
                                                  resource.getClass().getName() )   , 
                                                sqlParams[0] ) ;
        }

        this.resource = new ResourceWraper(resource, dto, filters ) ;
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
            ex.printStackTrace() ;
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
            ex.printStackTrace() ;
        }
        return localMarshaller ;
    }
}

