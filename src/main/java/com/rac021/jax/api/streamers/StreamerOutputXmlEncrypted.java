
package com.rac021.jax.api.streamers ;

import java.io.Writer ;
import java.util.Queue ;
import java.util.Arrays ;
import java.util.Base64 ;
import java.io.IOException ;
import java.io.OutputStream ;
import java.util.LinkedList ;
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
import javax.ws.rs.core.StreamingOutput ; 
import com.rac021.jax.api.manager.IDto ;
import com.rac021.jax.api.security.ISignOn ;
import javax.ws.rs.WebApplicationException ;
import org.apache.commons.lang3.ArrayUtils ;
import com.rac021.jax.api.crypto.Encryptor ;
import com.rac021.jax.api.manager.IResource ;
import com.rac021.jax.api.crypto.CipherOperation ;
import com.rac021.jax.api.exceptions.BusinessException ;

/**
 *
 * @author yahiaoui
 */

public class StreamerOutputXmlEncrypted extends Streamer implements StreamingOutput, IStreamer {

    public StreamerOutputXmlEncrypted() {
    }

    @Override
    
    public void write(OutputStream output) throws IOException {

      if( ISignOn.ENCRYPTION_KEY == null ) 
         throw new WebApplicationException(" Error Key can't be NULL " ) ;
        
      String    key       = ISignOn.ENCRYPTION_KEY.get() ;
      Encryptor encryptor = new Encryptor(key)           ;
        
      encryptor.initEncryptMode() ;

      Writer writer = new BufferedWriter( new OutputStreamWriter(output, "UTF8")) ;
        
      ByteArrayOutputStream baoStream        = new ByteArrayOutputStream() ;
      Queue<Byte>           qeueBytes        = new LinkedList<>()          ;
      StringBuilder         plainTextBuilder = new StringBuilder()         ;
      ByteArrayOutputStream outString        = new ByteArrayOutputStream() ;
      
      int nbrBlocks = 0   ;

      /* Prepare Thread Producers */
      producerScheduler() ;

        try {
            
            Marshaller marshaller = getMashellerWithXMLProperties() ;

            baoStream.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes());
            baoStream.write("\n<Root>".getBytes());

            int iteration = 0 ;
            
            while (!isFinishedProcess || !dtos.isEmpty()) {

               IDto poll = dtos.poll(200, TimeUnit.MILLISECONDS) ;
                    
               if( poll != null ) {
                       
                  JAXBElement<IDto> je2 = new JAXBElement<>(new QName("Data"), resource.getDto(), poll) ;
                  marshaller.marshal(je2 , baoStream) ;

                  plainTextBuilder.append(baoStream.toString()) ;

                  baoStream.reset() ;
                        
                  iteration ++ ;
                        
                  if (iteration % loooFLush == 0) {
                      
                      nbrBlocks = (plainTextBuilder.length() / BlockSize) ;

                      if ((plainTextBuilder.length() % BlockSize > 0) && (nbrBlocks >= 1)) {

                           qeueBytes.addAll(Arrays.asList(
                                   ArrayUtils.toObject( encryptor.aes128CBC7Encrypt(
                                                        plainTextBuilder.substring(0, nbrBlocks * BlockSize).getBytes(),
                                                        CipherOperation.update)
                                        ))) ;
                           
                           plainTextBuilder.delete(0, nbrBlocks * BlockSize) ;
                                
                      } else if (nbrBlocks > 1) {
                          
                           qeueBytes.addAll(Arrays.asList (
                                                            ArrayUtils.toObject( encryptor.aes128CBC7Encrypt(
                                                            plainTextBuilder.substring(0, (nbrBlocks - 1) * BlockSize).
                                                            getBytes(), CipherOperation.update )))) ;

                           plainTextBuilder.delete(0, (nbrBlocks - 1) * BlockSize ) ;
                      }

                      while ((qeueBytes.size() / 3) >= 1)   {
                          
                          outString.write(qeueBytes.poll()) ;
                          outString.write(qeueBytes.poll()) ;
                          outString.write(qeueBytes.poll()) ;
                      }
                         
                      writer.write(new String(Base64.getEncoder().encode(outString.toByteArray()))) ;
                      writer.flush()    ;
                      baoStream.reset() ;
                      outString.reset() ;
                      iteration = 0     ;
                  }

               }
            }

            plainTextBuilder.append(baoStream.toString()) ;
            qeueBytes.addAll(Arrays.asList(ArrayUtils.toObject( encryptor
                                                     .aes128CBC7Encrypt( (plainTextBuilder
                                                     .append("\n</Root>").append("\n"))
                                                     .toString().getBytes(), CipherOperation.dofinal)))) ;
            while (!qeueBytes.isEmpty()) {
               outString.write(qeueBytes.poll()) ;
            }

            writer.write ( new String(Base64.getEncoder().encode(outString.toByteArray()))) ;

            writer.flush() ;
            writer.close() ;

            baoStream.close() ;
            outString.close() ;

        } catch (IOException | JAXBException ex) {
            if (ex.getClass().getName().endsWith(".ClientAbortException")) {
                try {
                    writer.close() ;
                    baoStream.close() ;
                    throw new BusinessException("ClientAbortException !! " + ex.getMessage()) ;
                } catch (IOException | BusinessException ex1) {
                }
            } else {
                try {
                    writer.close() ;
                    baoStream.close() ;
                    throw new BusinessException("Exception : " + ex.getMessage()) ;
                } catch (IOException | BusinessException ex1) {
                    ex1.printStackTrace() ;
                }
            }
            
            isFinishedProcess = true ;
            
        } catch (InterruptedException ex) {
            Logger.getLogger(StreamerOutputXmlEncrypted.class.getName()).log(Level.SEVERE, null, ex) ;
        }
    }

    public ResourceWraper getResource() {
        return resource;
    }

  
    public StreamerOutputXmlEncrypted wrapResource( IResource resource    ,
                                                    Class dto             ,
                                                    String filteredIndexs ,
                                                    MultivaluedMap <String, String> ... sqlParams ) {

      rootResourceWraper(resource, dto, filteredIndexs, sqlParams) ;
      return this ;
    }

    public StreamerOutputXmlEncrypted wrapResource( IResource resource , Class dto ) {

      rootResourceWraper(resource, dto, null ) ;        
      return this ;
    }

    @Override
    public void setStreamerConfigurator(IStreamerConfigurator iStreamerConfigurator) {
        streamerConfigurator = iStreamerConfigurator ;
    }
}
