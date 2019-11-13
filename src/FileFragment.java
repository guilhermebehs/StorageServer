/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author 0151194
 */
public class FileFragment {
    
    private int fileFragmentLocationId;
    private int sequence;
    private byte[] bytes;
   
    
    /**
     * @return the bytes
     */
 
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * @param bytes the bytes to set
     */
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * @return the fileFragmentLocationId
     */
    public int getFileFragmentLocationId() {
        return fileFragmentLocationId;
    }

    /**
     * @param fileFragmentLocationId the fileFragmentLocationId to set
     */
    public void setFileFragmentLocationId(int fileFragmentLocationId) {
        this.fileFragmentLocationId = fileFragmentLocationId;
    }

    /**
     * @return the sequency
     */
    
      /**
     * @return the sequence
     */
    public int getSequence() {
        return sequence;
    }

    /**
     * @param sequence the sequence to set
     */
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
    
   
    @Override
    public boolean equals(Object obj){
        FileFragment fragment = (FileFragment)obj;
        return fragment.getFileFragmentLocationId() == this.fileFragmentLocationId;
    }

  
}
