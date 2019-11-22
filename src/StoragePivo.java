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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

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
    private JTextArea logArea;
    private JTable tabela;

    public StoragePivo(JTextArea logArea,JTable tabela){
        
        this.logArea = logArea;
        this.tabela = tabela;
    }
    
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

                List<byte[]> bytesPorStorage = new ArrayList();
                String log = "";
                 
                operacao = is.read();
                if (operacao == Operacao.UPLOAD.valor) {

                   int idNovo = (fragmentos.size()) + 1;
                   os.write(idNovo);
                   Thread.sleep(3000);
                   
                   byte[] bytesFinal = retornaArquivoEmBytes(is);
                    
             
                int lengthForStorage = (int)(bytesFinal.length / 4);
                int bytesFaltando = bytesFinal.length - (lengthForStorage * 4); 
            
             
                byte[]  bytesAux = new byte[lengthForStorage];
                byte[]  bytesAux2 = new byte[lengthForStorage+bytesFaltando]; 
                int count = 0;
                
                for(byte byt: bytesFinal){
                    
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
                    fragmentos.add(fragment);
                    enviarParaStorages(idNovo, bytesPorStorage);
                    Thread.sleep(3000);
                    log = logArea.getText();
                    log += "Upload realizado!\n";
                    logArea.setText(log);
                    //Sinaliza que acabou
                    os.write(0);

                } else if (operacao == Operacao.DOWNLOAD.valor) {
                    int id = is.read();
                    log = logArea.getText();
                    log += "Arquivo com id "+id+" solicitado\n";
                    logArea.setText(log);
                    byte[] bytes = montarBytes(id);
                    log = logArea.getText();
                    log += "Download enviado!\n";
                    logArea.setText(log);
                    os.write(0);
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

        
        DefaultTableModel model = (DefaultTableModel) tabela.getModel();
        model.addRow(new Object[]{id,bytes.get(3).length, bytes.get(0).length, bytes.get(1).length, bytes.get(2).length});
        
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
            saida.write(id);
            saida.write(sequencia);
            saida.write(bytes);
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
        byte[] bytesFinal = new byte[1024];
        try {
            socketServidor = new Socket("localhost", porta);
            saida = socketServidor.getOutputStream();
            entrada = socketServidor.getInputStream();
            saida.write(2);
            saida.write(id);
            Thread.sleep(2000);
          
            bytesFinal = retornaArquivoEmBytes(entrada);
            

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            Logger.getLogger(StoragePivo.class.getName()).log(Level.SEVERE, null, ex);
        }

        return bytesFinal;
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
       for(byte[] byt: bytesList){
           for(int i=0; i < byt.length; i++){
               bytes[count] = byt[i];
               count++;
           }
       }
       
        return bytes;

    }
     
     public byte[] retornaArquivoEmBytes(InputStream entrada) {

        List<byte[]> bytesList = new ArrayList();
        byte[] bytesFinal = new byte[1024];
        int lengthFinal = 0;
        try {
            while (entrada.available() > 0) {
                lengthFinal += entrada.available();
                byte[] bytes = new byte[entrada.available()];
                entrada.read(bytes, 0, entrada.available());
                bytesList.add(bytes);
            }
            bytesFinal = new byte[lengthFinal];
            int y = 0;
            for (byte[] byt : bytesList) {
                for (int i = 0; i < byt.length; i++) {
                    bytesFinal[y] = byt[i];
                    y++;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return bytesFinal;
    }
     
   
    @Override
    public void run() {
       iniciarConexao();
    }

}
