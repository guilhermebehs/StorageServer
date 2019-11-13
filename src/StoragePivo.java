import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author 0151194
 */
public class StoragePivo implements Runnable {

    private List<FileFragment> fragmentos = new ArrayList();
    
    private static final String FINAL_ARQUIVO = "255,255,255,255,255";

    public void iniciarConexao() {

        ServerSocket s;
        Socket socketComCliente;
        InputStream is;
        OutputStream os;
        int operacao;
        try {
            s = new ServerSocket(Porta.PORTA1.valor);

            while (true) {
                socketComCliente = s.accept();
                is = socketComCliente.getInputStream();
                os = socketComCliente.getOutputStream();

                List<Integer> ints = new ArrayList();
                List<byte[]> bytesPorStorage = new ArrayList();
                operacao = is.read();
                if (operacao == Operacao.ENVIAR_BYTES.valor) {

                    int b = 0;
                    String sinalFimLeitura = "";
                    while (!(sinalFimLeitura.contains(FINAL_ARQUIVO)) && (b = is.read()) != -1) {

                        String[] sinalFimLeituraSplit = sinalFimLeitura.split(",");
                        sinalFimLeitura += "," + b;
                        ints.add(b);
                    }
                    int lengthFinal = ints.size()-5;
                    byte[] bytes = new byte[lengthFinal];

                    for (int i = 0; i <lengthFinal; i++) {
                        bytes[i] = ints.get(i).byteValue();
                    }
       

                    int idNovo = (fragmentos.size()) + 1;
                   
                int lengthForStorage = (int)(lengthFinal / 4);
                int bytesFaltando = lengthFinal - (lengthForStorage * 4); 
            
             
                byte[]  bytesAux = new byte[lengthForStorage];
                byte[]  bytesAux2 = new byte[lengthForStorage+bytesFaltando]; 
                int count = 0;
                
                for(byte byt: bytes){
                    
                    if(bytesPorStorage.size() < 3){
                       bytesAux[count] = byt; 
                       count++;
                        if(count == lengthForStorage){
                           bytesPorStorage.add(bytesAux);
                           bytesAux = new byte[lengthForStorage];
                           count =0; 
                        }
                        
                    }
                    else{
                        bytesAux2[count] = byt; 
                         count++;
                         if(count == (lengthForStorage+bytesFaltando)){
                           bytesPorStorage.add(bytesAux2);
                           bytesAux2 = new byte[lengthForStorage+bytesFaltando];
                           count =0; 
                        }
                    }
                        
                }
     
                
                    FileFragment fragment = new FileFragment();
                    fragment.setBytes(bytesPorStorage.get(3));
                    fragment.setFileFragmentLocationId(idNovo);
                    fragment.setSequence(4);
                    fragmentos.add(fragment);
                    enviarParaStorages(idNovo, bytesPorStorage);
                    os.write(idNovo);

                } else if (operacao == Operacao.RETORNAR_BYTES.valor) {
                    int id = is.read();
                    byte[] bytes = montarBytes(id);
                    os.write(bytes);
                }

                is.close();
                os.close();
                socketComCliente.close();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    

    public void enviarParaStorages(int id, List<byte[]> bytes) {

                  for(byte[] byt: bytes){
                      for(int i=0; i < byt.length; i++)
                          System.out.println(byt[i]);
                      }
        
        enviar(Porta.PORTA2.valor, id,1 ,bytes.get(0));
        enviar(Porta.PORTA3.valor, id,2,bytes.get(1));
        enviar(Porta.PORTA4.valor, id, 3,bytes.get(2));

    }

    public void enviar(int porta, int id, int sequencia ,byte[] bytes) {

        Socket socketServidor;
        OutputStream saida;
        try {
            socketServidor = new Socket("localhost" ,porta);
            saida = socketServidor.getOutputStream();
            saida.write(1);
            saida.write(bytes);
            sinalizarFimArquivo(saida);
            saida.write(id);
            saida.write(sequencia);
            saida.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public List<byte[]> receberDosStorages(int id) {

        List<byte[]> bytes = new ArrayList();
        bytes.add(receber(Porta.PORTA2.valor, id));
        bytes.add(receber(Porta.PORTA3.valor, id));
        bytes.add(receber(Porta.PORTA4.valor, id));
        return bytes;

    }

    public byte[] receber(int porta, int id) {

        Socket socketServidor;
        OutputStream saida;
        InputStream entrada;
        byte[] bytes = new byte[1024];
        try {
            socketServidor = new Socket("localhost", porta);
            saida = socketServidor.getOutputStream();
            entrada = socketServidor.getInputStream();
            List<Integer> ints = new ArrayList();
            saida.write(2);
            saida.write(id);
            int b ;
                    String sinalFimLeitura = "";
                    while (!(sinalFimLeitura.contains(FINAL_ARQUIVO)) && (b = entrada.read()) != -1) {

                        String[] sinalFimLeituraSplit = sinalFimLeitura.split(",");
                        sinalFimLeitura += "," + b;
                        ints.add(b);
                    }
            bytes = new byte[ints.size()-5];

            for (int i = 0; i < ints.size()-5; i++) {
                bytes[i] = ints.get(i).byteValue();
            }

        } catch (IOException ex) {
             ex.printStackTrace();
        }

        return bytes;
    }

     public byte[] montarBytes(int id) {

        List<byte[]> bytesList = receberDosStorages(id);
        for (FileFragment frag : fragmentos) {
                        if (frag.getFileFragmentLocationId() == id) {
                            byte[] bytes = frag.getBytes();
                            bytesList.add(bytes);
                            break;
                        }
                    }

        
        int length = 0;
        for (byte[] b : bytesList) {
            length += b.length;
        }

        byte[] bytes = new byte[length];
       
       int count =0; 
       for(byte[] byt: bytesList)
           for(int i=0; i < byt.length; i++){
               bytes[count] = byt[i];
               count++;
           }
       
        return bytes;

    }
     
     
     public void sinalizarFimArquivo(OutputStream saida){
         
        try {
            saida.write(-1);
            saida.write(-1);
            saida.write(-1);
            saida.write(-1);
            saida.write(-1);        
        } catch (IOException ex) {
            ex.printStackTrace();
        }
         
     }

    @Override
    public void run() {
       iniciarConexao();
    }

}
