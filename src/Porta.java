/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author guilherme.behs
 */
public enum Porta {
    
    PORTA1(8090), PORTA2(8091), PORTA3(8092), PORTA4(8093);
    
     public int valor;
     
     Porta(int valor){
        this.valor = valor;
    }
}
