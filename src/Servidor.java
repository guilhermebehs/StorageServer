/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author guilherme.behs
 */
public class Servidor {
    
    
    public static void main(String[] args) {
        
        new Thread(new StoragePivo()).start();
        new Thread(new Storage(Porta.PORTA2.valor)).start();
        new Thread(new Storage(Porta.PORTA3.valor)).start();
        new Thread(new Storage(Porta.PORTA4.valor)).start();
        
        
    }
}
