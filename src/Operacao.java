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
    
    
     UPLOAD(1), DOWNLOAD(2);
    
     public int valor;
     
     Operacao(int valor){
        this.valor = valor;
    }
    
}
