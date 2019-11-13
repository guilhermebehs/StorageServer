/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author guilherme.behs
 */
public enum Operacao {
    
    
     ENVIAR_BYTES(1), RETORNAR_BYTES(2);
    
     public int valor;
     
     Operacao(int valor){
        this.valor = valor;
    }
    
}
