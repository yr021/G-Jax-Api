
package com.rac021.jax.api.streamers ;

import java.io.Writer ;
import java.io.IOException ;
import java.io.OutputStream ;
import java.io.BufferedWriter ;
import java.util.logging.Level ;
import java.util.logging.Logger ;
import javax.xml.namespace.QName ;
import javax.xml.bind.Marshaller ;
import java.io.OutputStreamWriter ;
import javax.xml.bind.JAXBElement ;
import javax.xml.bind.JAXBException ;
import java.util.concurrent.TimeUnit ;
import java.io.ByteArrayOutputStream ;
import javax.ws.rs.core.MultivaluedMap ;
import com.rac021.jax.api.manager.IDto ;
import javax.ws.rs.core.StreamingOutput ;
import com.rac021.jax.api.manager.IResource ;
import com.rac021.jax.api.exceptions.BusinessException ;
import static com.rac021.jax.api.streamers.DefaultStreamerConfigurator.* ;

/**
 *
 * @author yahiaoui
 */

public class StreamerOutputJson extends Streamer implements StreamingOutput //, IStreamer 
{

    public StreamerOutputJson() {
    }

    @Override
    public void write(OutputStream output) throws IOException {
        
       System.out.println(" Processing data in StreamerOutputJson ... ") ;
       
       configureStreamer() ;

       /* Submit Producers */
       poolProducer.submit( () -> producerScheduler() ) ;      
      
       Writer writer = new BufferedWriter ( new OutputStreamWriter(output, "UTF8")) ;

       ByteArrayOutputStream baoStream = new ByteArrayOutputStream() ;

       try {

            System.setProperty( "javax.xml.bind.context.factory" ,
                                "org.eclipse.persistenputce.jaxb.JAXBContextFactory") ;

            Marshaller marshaller  =  getMashellerWithJSONProperties() ;
            int iteration          =  0                                ;

            while (!isFinishedProcess || !dtos.isEmpty())          {

                IDto poll = dtos.poll( 50 , TimeUnit.MILLISECONDS)  ;
                   
                if( poll != null ) {
                    
                      JAXBElement<IDto> je2 = new JAXBElement<> ( new QName("Data") , 
                                                                  resource.getDto() , 
                                                                  poll            ) ;
                      
                      marshaller.marshal(je2, baoStream)       ;

                      writer.write(baoStream.toString("UTF8")) ;
                      baoStream.reset()                        ;
                      iteration ++                             ;
                      
                      if (iteration % responseCacheSize == 0 )  writer.flush() ;
                }
             }

             writer.flush()    ;
             writer.close()    ;
             baoStream.close() ;

        } catch (JAXBException | IOException ex) {
            
            if (ex.getClass().getName().endsWith(".ClientAbortException")) {
                try {
                    writer.close()    ;
                    baoStream.close() ;
                    throw new BusinessException("ClientAbortException !! " + ex.getMessage()) ;
                } catch (IOException | BusinessException ex1) {
                      Logger.getLogger(StreamerOutputJson.class.getName()).log(Level.SEVERE, null, ex1) ;
                }
            } else {
                try {
                    writer.close()    ;
                    baoStream.close() ;
                    throw new BusinessException("Exception : " + ex.getMessage(), ex) ;
                } catch (IOException | BusinessException ex1) {
                    Logger.getLogger(StreamerOutputJson.class.getName()).log(Level.SEVERE, null, ex1) ;
                }
            }
            
            isFinishedProcess = true ;
            
        }   catch (InterruptedException ex) {
            Logger.getLogger(StreamerOutputJson.class.getName()).log(Level.SEVERE, null, ex) ;
        }
    }

    public ResourceWraper getResource() {
        return resource ;
    }
 
    public StreamerOutputJson wrapResource( IResource resource       , 
                                            Class     dto            ,
                                            String    filteredNmames ,
                                            MultivaluedMap<String, String> ... sqlParams ) {

        rootResourceWraper( resource, dto, filteredNmames, sqlParams ) ;
        
        return this ;
    }
    
    public StreamerOutputJson wrapResource( IResource resource , Class dto ) {

        rootResourceWraper( resource, dto, null ) ;        
        return this                               ;
    }
}
