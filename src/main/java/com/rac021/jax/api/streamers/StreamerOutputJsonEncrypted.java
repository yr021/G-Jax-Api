
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
import com.rac021.jax.api.crypto.ICryptor ;
import com.rac021.jax.api.security.ISignOn ;
import org.apache.commons.lang3.ArrayUtils ;
import javax.ws.rs.WebApplicationException ;
import com.rac021.jax.api.manager.IResource ;
import com.rac021.jax.api.crypto.EncDecRyptor ;
import com.rac021.jax.api.crypto.FactoryCipher ;
import com.rac021.jax.api.exceptions.BusinessException ;
import static com.rac021.jax.api.streamers.DefaultStreamerConfigurator.* ;

/**
 *
 * @author yahiaoui
 */

public class StreamerOutputJsonEncrypted extends Streamer implements StreamingOutput {

    public StreamerOutputJsonEncrypted() { }

    @Override
    public void write(OutputStream output) throws IOException {
        
        System.out.println(" Processing data in StreamerOutputJsonEncrypted ... ") ;
      
        if ( ISignOn.ENCRYPTION_KEY == null ) 
          throw new WebApplicationException(" Error Key can't be NULL " ) ;
        
        configureStreamer() ;

        /* Submit Producers */
        poolProducer.submit( () -> producerScheduler() ) ;      

        Writer writer  = new BufferedWriter( new OutputStreamWriter(output, "UTF8")) ;
      
        ICryptor crypt = null ;
      
        try {
            
          crypt = FactoryCipher.getCipher ( ISignOn.CIPHER.get()           , 
                                            ISignOn.ENCRYPTION_KEY.get() ) ;
          
        } catch( Exception ex ) {
           writer.write(" Exception  : Something went wrong // " + ex.getMessage()        ) ;
           writer.write(" \n" ) ;
           writer.write(" MediaType  : JSON/ENCRYPTED "                                   ) ;
           writer.write(" \n" ) ;
           writer.write(" Specify accept header in the Request if it's not already done " ) ;
           writer.write(" \n" ) ;
           writer.flush() ; 
           writer.close() ;
        }
         
        crypt.setOperationMode(EncDecRyptor._Operation.Encrypt )             ;
        
        ByteArrayOutputStream baoStream        = new ByteArrayOutputStream() ;
        Queue<Byte>           qeueBytes        = new LinkedList<>()          ;
        StringBuilder         plainTextBuilder = new StringBuilder()         ;
        ByteArrayOutputStream outString        = new ByteArrayOutputStream() ;
        
        /* Send in response the Vector Initialization */
        if( crypt.getIvBytesEncoded64() != null ) {
            writer.write(new String( crypt.getIvBytesEncoded64() ) + "." ) ;
            writer.flush()  ;
        }
        
        int nbrBlocks = 0   ;

        try {
            
            Marshaller marshaller = getMashellerWithJSONProperties() ;

            int        iteration  = 0                                ;
            
            while ( ! isFinishedProcess || !dtos.isEmpty()) {
                
                   IDto poll = dtos.poll( 50, TimeUnit.MILLISECONDS) ;
       
                   if( poll != null ) {
                       
                      JAXBElement<IDto> je2 = new JAXBElement<>( new QName("Data") , 
                                                                 IDto.class        , 
                                                                 poll            ) ;
                      
                      marshaller.marshal(je2.getValue(), baoStream) ;

                      plainTextBuilder.append(baoStream.toString()) ;
                      iteration ++                                  ;
                      baoStream.reset()                             ;
                            
                      if (iteration % responseCacheSize == 0 ) {
                          
                        nbrBlocks = (plainTextBuilder.length() / blockSize) ;

                        if ((plainTextBuilder.length() % blockSize > 0 ) && (nbrBlocks >= 1)) {

                            qeueBytes.addAll(Arrays.asList ( ArrayUtils.toObject (
                                                             crypt.process ( plainTextBuilder
                                                                  .substring( 0 , nbrBlocks * blockSize ) , 
                                                                   EncDecRyptor._CipherOperation.update ) ) 
                                                            ) ) ;
                            
                            plainTextBuilder.delete(0, nbrBlocks * blockSize) ;
                            
                        } else if ( nbrBlocks > 1 ) {
                            
                                    qeueBytes.addAll(Arrays.asList (
                                            ArrayUtils.toObject (
                                            crypt.process ( 
                                                     plainTextBuilder.substring(0, (nbrBlocks - 1) * blockSize) ,
                                                     EncDecRyptor._CipherOperation.update ) ) 
                                     ) ) ;
                                    
                                    plainTextBuilder.delete(0, (nbrBlocks - 1) * blockSize) ;
                        }

                        while ((qeueBytes.size() / 3) >= 1)      {
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

            qeueBytes.addAll ( Arrays.asList( ArrayUtils.toObject(crypt.process (
                                              plainTextBuilder.toString() ,
                                              EncDecRyptor._CipherOperation.dofinal ) ) 
            ) ) ;
 
            while (!qeueBytes.isEmpty())          {
                outString.write(qeueBytes.poll()) ;
            }

            writer.write(new String(Base64.getEncoder().encode(outString.toByteArray() ) ) ) ;

            writer.flush()    ;
            writer.close()    ;

            baoStream.close() ;
            outString.close() ;

        } catch (JAXBException | IOException ex) {
            
            if (ex.getClass().getName().endsWith(".ClientAbortException")) {
                
                try {
                    writer.close()    ;
                    baoStream.close() ;
                    outString.close() ;
                    throw new BusinessException("ClientAbortException !! " + ex.getMessage(), ex) ;
                } catch (IOException | BusinessException ex1) {
                    System.out.println( ex1.getMessage() ) ; 
                }
            } else {
                try {
                    writer.close()    ;
                    baoStream.close() ;
                    outString.close() ;
                    throw new BusinessException("Exception : " + ex.getMessage()) ;
                } catch (IOException | BusinessException ex1) {
                    System.out.println( ex1.getMessage() )    ;
                }
            }
            
            isFinishedProcess = true ;
            
        } catch (InterruptedException ex) {
            Logger.getLogger(StreamerOutputJsonEncrypted.class.getName()).log(Level.SEVERE, null, ex) ;
        }
    }

    public ResourceWraper getResource() {
        return resource ;
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

}
