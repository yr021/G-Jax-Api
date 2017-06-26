
package com.rac021.jax.api.streamers ;

import java.io.Writer ;
import java.io.IOException ;
import java.io.OutputStream ;
import java.io.BufferedWriter ;
import java.util.logging.Level ;
import java.util.logging.Logger ;
import javax.xml.bind.Marshaller ;
import javax.xml.namespace.QName ;
import java.io.OutputStreamWriter ;
import javax.xml.bind.JAXBElement ;
import javax.xml.bind.JAXBException ;
import java.io.ByteArrayOutputStream ;
import java.util.concurrent.TimeUnit ;
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

public class StreamerOutputXml extends Streamer implements StreamingOutput {

    public StreamerOutputXml() {
    }

    @Override
    public void write(OutputStream output) throws IOException {

      System.out.println(" Processing data in StreamerOutputXml ... ") ;
      
      configureStreamer() ;

      /* Prepare THread Producers */
      poolProducer.submit( () -> producerScheduler() ) ;      

      Writer writer = new BufferedWriter( new OutputStreamWriter(output, "UTF8") ) ;
      
      try (ByteArrayOutputStream baoStream = new ByteArrayOutputStream()) {
          
          Marshaller marshaller = getMashellerWithXMLProperties()       ;
               
          writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")    ;
          writer.write("\n<Root>") ;
               
          int iteration = 0 ;
           
          while (!isFinishedProcess || !dtos.isEmpty() )         {

            IDto poll = dtos.poll( 50 , TimeUnit.MILLISECONDS  ) ;
                   
            if(poll != null) {
                
              JAXBElement<IDto> je2 = new JAXBElement<>( new QName("Data") , 
                                                         resource.getDto() , 
                                                         poll )            ;
              marshaller.marshal(je2, baoStream)       ;
                       
              writer.write(baoStream.toString("UTF8")) ;
              baoStream.reset()                        ;
              iteration ++                             ;
              
              if (iteration % responseCacheSize == 0 ) {
                  writer.flush() ;
                  iteration = 0  ;
              }
            } 
          }
               
          writer.write("\n</Root>") ;
          writer.write("\n")        ;
          writer.flush()            ;
          writer.close()            ;
          
        } catch (IOException | JAXBException ex) {
            if (ex.getClass().getName().endsWith(".ClientAbortException")) {
                try {
                    writer.close() ;
                    throw new BusinessException("ClientAbortException !! " + ex.getMessage()) ;
                } catch (IOException | BusinessException ex1) {
                }
            } else {
                try {
                    writer.close();
                    throw new BusinessException(" Exception : " + ex.getMessage()) ;
                } catch (IOException | BusinessException ex1) {
                    ex1.printStackTrace() ;
                }
            }
            
            isFinishedProcess = true ;
            
        } catch (InterruptedException ex) {
            Logger.getLogger(StreamerOutputXml.class.getName()).log(Level.SEVERE, null, ex) ;
        }
    }

    public ResourceWraper getResource() {
        return resource ;
    }
    
    public StreamerOutputXml wrapResource( IResource resource    ,
                                           Class dto             ,
                                           String filteredIndexs ,
                                           MultivaluedMap<String , String> ... sqlParams ) {

      rootResourceWraper( resource, dto, filteredIndexs, sqlParams) ;
      return this ;
    }
    
    public StreamerOutputXml wrapResource( IResource resource , Class dto ) {

      rootResourceWraper( resource, dto, null ) ;        
      return this ;
    }

}
