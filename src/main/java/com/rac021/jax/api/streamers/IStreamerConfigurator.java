
package com.rac021.jax.api.streamers ;

/**
 *
 * @author yahiaoui
 */

public interface IStreamerConfigurator {

    public int  getRatio() ;

    public int  getNbrCores() ;
    
    public int  getLoopFlush() ;

    public int  getBlockSize() ;

    public int  getRecorderLenght() ;
    
    public void setRatio( int ratio ) ;
    
    public void setNbrCores( int cores ) ;
    
    public void setLoopFlush( int LoopFlush ) ;

    public void setBlockSize( int BlockSize ) ;

    public void setRecorderLenght( int recorderLenght ) ;
    
}
