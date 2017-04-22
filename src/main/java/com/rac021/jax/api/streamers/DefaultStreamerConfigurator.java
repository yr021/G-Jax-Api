
package com.rac021.jax.api.streamers ;

/**
 *
 * @author yahiaoui
 */

public class DefaultStreamerConfigurator implements IStreamerConfigurator {

    /** Default Lengh of the extraction **/
    private int recorderLenght = 5000 ;
    
    /** Ratio  of the extraction */
    private int ratio = 1 ;
    
    /*** Default Nbr Threads */
    private int nbrCores   = 2   ;

    /** Default Nbr LOOPS before flush data **/
    private int loopFlush  = 500 ;

    /** Default Block Size for Encryption **/
    private int BlockSize = 16  ;

    
    public DefaultStreamerConfigurator( int nbrCores       , 
                                        int recorderLenght , 
                                        int ratio          , 
                                        int loopFlush      ,
                                        int blockSize   )  {
        this.nbrCores       = nbrCores       ;
        this.recorderLenght = recorderLenght ;
        this.ratio          = ratio          ;
    }

    public DefaultStreamerConfigurator() {
    }

    @Override
    public int getRecorderLenght() {
        return recorderLenght ;
    }

    @Override
    public void setRecorderLenght(int recorderLenght) {
        this.recorderLenght = recorderLenght ;
    }

    @Override
    public int getRatio() {
        return ratio ;
    }

    @Override
    public void setRatio(int ratio) {
        this.ratio = ratio ;
    }

    @Override
    public int getNbrCores() {
        return nbrCores ;
    }

    @Override
    public void setNbrCores(int nbrCores) {
        this.nbrCores = nbrCores ;
    }

    @Override
    public int getLoopFlush() {
        return loopFlush ;
    }

    @Override
    public void setLoopFlush(int LoopFlush) {
        this.loopFlush = LoopFlush ;
    }

    @Override
    public int getBlockSize() {
        return BlockSize ;
    }

    @Override
    public void setBlockSize(int BlockSize) {
        this.BlockSize = BlockSize ;
    }    
}
