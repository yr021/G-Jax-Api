
package com.rac021.jax.api.streamers ;

import javax.ejb.Singleton ;
import java.util.logging.Level ;
import java.util.logging.Logger ;
import java.util.concurrent.TimeUnit ;
import java.util.concurrent.Semaphore ;
import java.util.concurrent.BlockingQueue ;
import java.util.concurrent.ThreadPoolExecutor ;
import java.util.concurrent.ArrayBlockingQueue ;
import com.rac021.jax.api.executorservice.Worker ; 
import javax.enterprise.context.ApplicationScoped ;
import com.rac021.jax.api.exceptions.BusinessException ;

/**
 *
 * @author yahiaoui
 */


@Singleton
@ApplicationScoped

public class DefaultStreamerConfigurator          {
    
    /* Default Lenght of the request extraction */
    public static int selectSize         = 5000   ;
    
    /** Ratio of the extraction */
    public static int ratio              = 1      ;
    
    /*** Default Nbr Threads */
    public static Integer maxThreads     = null   ;

    /** Default cache size Response for flush data **/
    public static int responseCacheSize  = 500    ;

    /** Default Block Size for Encryption **/
    public static int blockSize          = 16     ;

    /* Thread Pool Size - Shared by all services */
    public static int threadPoolSize     = 4      ;
  
    /* Size of the Queue which is used by Pool   */
    public static int workerQueue        = 5000   ;
    
    /* TimeOut Service */
    public static int workerTimeOut      = 5      ;
     
    /* Manage Concurent Users */
     static Semaphore semaphoreMaxConcurrentUsers ;

    /* ThreadPool Worker */
    static Worker poolProducer                    ;
    
    /* Max Concurrent Users */
    public static int maxConcurrentUsers = Integer.MAX_VALUE ;

    
    static {
      initPoolProducer()             ; 
      initSemaphoreConcurrentUsers() ;
    }

    public DefaultStreamerConfigurator() { }

    public static void initSemaphoreConcurrentUsers() {
        if( maxConcurrentUsers <= 0 ) maxConcurrentUsers = Integer.MAX_VALUE   ;
        semaphoreMaxConcurrentUsers = new Semaphore( getMaxConcurrentUsers() ) ;
    }
    
    public static void initPoolProducer()   {
      
        if( ( maxThreads != null ) && threadPoolSize < maxThreads )  {
            try {
                 throw new BusinessException(" maxPoolSize can't be lower than maxThreads" ) ;
            } catch (BusinessException ex) {
                 Logger.getLogger( DefaultStreamerConfigurator.class.getName())
                               .                            log(Level.SEVERE, null, ex )     ;
            }
        }
        
        BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>( workerQueue )      ;
       
        poolProducer = new Worker( threadPoolSize   ,
                                   threadPoolSize   , 
                                   workerTimeOut    , 
                                   TimeUnit.MINUTES , 
                                   blockingQueue )  ;
     
        poolProducer.setRejectedExecutionHandler( (Runnable r, ThreadPoolExecutor executor) ->  {
        
        System.out.println(" --> Thread ** " + r.toString() + " ** Rejected ") ;
           
        try { 
              Thread.sleep( 500 ) ; 
        } 
        catch (InterruptedException e) { e.printStackTrace() ; }
          
        System.out.println(" Retry Thread **  "+ r.toString() + " ** ") ;
        executor.execute(r)                                             ;
     
        });
    }
    
    public static int getMaxConcurrentUsers() {
        return maxConcurrentUsers ;
    }
    public static int getSelectSize()         {
        return selectSize ;
    }
    public static int getRatio()              {
        return ratio ;
    }
    public static int getMaxThreads()         {
        return maxThreads ;
    }
    public static int getResponseCacheSize()  {
        return responseCacheSize ;
    }
    public static int getBlockSize()          {
        return blockSize ;
    }
    public static int getThreadPoolSize()     {
        return threadPoolSize ;
    }
    
}
