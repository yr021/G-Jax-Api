
package com.rac021.jax.api.streamers ;

import java.io.Writer ;
import java.util.Queue ;
import java.util.Arrays ;
import java.util.Base64 ;
import java.io.IOException ;
import java.util.LinkedList ;
import java.io.OutputStream ;
import java.io.BufferedWriter ;
import java.util.logging.Level ;
import java.util.logging.Logger ;
import javax.xml.namespace.QName ;
import javax.xml.bind.Marshaller ;
import java.io.OutputStreamWriter ;
import javax.xml.bind.JAXBElement ;
import javax.xml.bind.JAXBException ;
import java.io.ByteArrayOutputStream ;
import java.util.concurrent.TimeUnit ;
import javax.ws.rs.core.MultivaluedMap ;
import com.rac021.jax.api.manager.IDto ;
import javax.ws.rs.core.StreamingOutput ;
import com.rac021.jax.api.security.ISignOn ;
import org.apache.commons.lang3.ArrayUtils ;
import javax.ws.rs.WebApplicationException ;
import com.rac021.jax.api.crypto.Encryptor ;
import com.rac021.jax.api.manager.IResource ;
import com.rac021.jax.api.crypto.CipherOperation ;
import com.rac021.jax.api.exceptions.BusinessException ;

/**
 *
 * @author yahiaoui
 */

public class StreamerOutputJsonEncrypted extends Streamer implements StreamingOutput, IStreamer {

    public StreamerOutputJsonEncrypted() {
    }

    @Override
    public void write(OutputStream output) throws IOException {

        if ( ISignOn.ENCRYPTION_KEY == null ) 
          throw new WebApplicationException(" Error Key can't be NULL " ) ;
        
        Encryptor encryptor = new Encryptor(ISignOn.ENCRYPTION_KEY.get()) ;
        encryptor.initEncryptMode() ;

        ByteArrayOutputStream baoStream        = new ByteArrayOutputStream() ;
        Queue<Byte>           qeueBytes        = new LinkedList<>()      ;
        StringBuilder         plainTextBuilder = new StringBuilder()  ;
        ByteArrayOutputStream outString        = new ByteArrayOutputStream() ;
        Writer                writer           = new BufferedWriter( new OutputStreamWriter( output , 
                                                                                            "UTF8") ) ;
        int nbrBlocks = 0 ;

        /* Prepare Threads */
        producerScheduler() ;

        try {
            
            Marshaller marshaller = getMashellerWithJSONProperties() ;

            int        iteration  = 0                                ;
            
            while (!isFinishedProcess || !dtos.isEmpty()) {
                
                   IDto poll = dtos.poll( 200, TimeUnit.MILLISECONDS) ;
       
                   if( poll != null ) {
                       
                      JAXBElement<IDto> je2 = new JAXBElement<>( new QName("Data"), IDto.class, poll ) ;
                      marshaller.marshal(je2.getValue(), baoStream) ;

                      plainTextBuilder.append(baoStream.toString()) ;
                      iteration ++                                  ;
                      baoStream.reset()                             ;
                            
                      if (iteration % loooFLush == 0 ) {
                          
                        nbrBlocks = (plainTextBuilder.length() / BlockSize) ;

                        if ((plainTextBuilder.length() % BlockSize > 0) && (nbrBlocks >= 1)) {

                            qeueBytes.addAll(Arrays.asList( ArrayUtils.toObject (
                                                            encryptor.aes128CBC7Encrypt(plainTextBuilder
                                                            .substring(0, nbrBlocks * BlockSize).getBytes(), 
                                                            CipherOperation.update  ) ) ) ) ;
                            
                            plainTextBuilder.delete(0, nbrBlocks * BlockSize) ;
                            
                        } else if (nbrBlocks > 1) {
                            
                                    qeueBytes.addAll(Arrays.asList (
                                            ArrayUtils.toObject (
                                            encryptor.aes128CBC7Encrypt ( 
                                                     plainTextBuilder.substring(0, (nbrBlocks - 1) * BlockSize).getBytes() ,
                                                     CipherOperation.update ) ) ) ) ;
                                    
                                    plainTextBuilder.delete(0, (nbrBlocks - 1) * BlockSize) ;
                        }

                        while ((qeueBytes.size() / 3) >= 1) {
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

            qeueBytes.addAll ( Arrays.asList( ArrayUtils.toObject(encryptor.aes128CBC7Encrypt (
                                             plainTextBuilder.toString().getBytes() ,
                                             CipherOperation.dofinal ) ) ) ) ;
 
            while (!qeueBytes.isEmpty()) {
                outString.write(qeueBytes.poll()) ;
            }

            writer.write(new String(Base64.getEncoder().encode(outString.toByteArray() ) ) ) ;

            writer.flush() ;
            writer.close() ;

            baoStream.close() ;
            outString.close() ;

        } catch (JAXBException | IOException ex) {
            if (ex.getClass().getName().endsWith(".ClientAbortException")) {
                try {
                    writer.close() ;
                    baoStream.close() ;
                    outString.close() ;
                    throw new BusinessException("ClientAbortException !! " + ex.getMessage(), ex) ;
                } catch (IOException | BusinessException ex1) {
                    ex1.printStackTrace() ; 
                }
            } else {
                try {
                    writer.close()    ;
                    baoStream.close() ;
                    outString.close() ;
                    throw new BusinessException("Exception : " + ex.getMessage()) ;
                } catch (IOException | BusinessException ex1) {
                    System.out.println(" Exception : " +  ex1.getMessage() ) ;
                }
            }
            isFinishedProcess = true;
        } catch (InterruptedException ex) {
            Logger.getLogger(StreamerOutputJsonEncrypted.class.getName()).log(Level.SEVERE, null, ex) ;
        }
    }

    public ResourceWraper getResource() {
        return resource;
    }

    public StreamerOutputJsonEncrypted wrapResource( IResource resource    , 
                                                     Class dto             ,
                                                     String filteredIndexs ,
                                                     MultivaluedMap<String , String> ... sqlParams ) {
      rootResourceWraper(resource, dto, filteredIndexs, sqlParams) ;
      return this ;
    }

    public StreamerOutputJsonEncrypted wrapResource( IResource resource , Class dto ) {

        rootResourceWraper(resource, dto, null ) ;        
        return this ;
    }

    @Override
    public void setStreamerConfigurator(IStreamerConfigurator iStreamerConfigurator) {
        streamerConfigurator = iStreamerConfigurator ;
    }

}
