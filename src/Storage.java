
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextArea;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author guilherme.behs
 */
public class Storage implements Runnable {

    private List<FileFragment> fragmentos = new ArrayList();
    private JTextArea logArea;   
    private int porta;
    
    public Storage(int porta,JTextArea logArea){
        this.porta = porta;
        this.logArea = logArea;
    }
    
    
    public void iniciarConexao(){
        
        ServerSocket socketComPivo;
        Socket client;
        InputStream is;
        OutputStream os;
        try {
            while (true) {
                socketComPivo = new ServerSocket(porta);
                client = socketComPivo.accept();
                is = client.getInputStream();
                os = client.getOutputStream();

                int operacao = is.read();
                if(operacao == Operacao.ENVIAR_BYTES.valor){
                   int idNovo = is.read();
                   int sequence = is.read();
                   Thread.sleep(2000);
                  
                    byte[] bytesFinal = retornaArquivoEmBytes(is);
                  
        
                    String log = logArea.getText();
                    log += bytesFinal.length+" bytes recebidos na porta "+porta+"\n";
                    logArea.setText(log);
                    
                   
                     FileFragment fragment = new FileFragment();
                    fragment.setBytes(bytesFinal);
                    fragment.setFileFragmentLocationId(idNovo);
                    fragment.setSequence(sequence);
                    fragmentos.add(fragment);   
                }
                else if(operacao == Operacao.RETORNAR_BYTES.valor){
                    
                    int id = is.read();
                    String log = logArea.getText();
                    log += "Solicitados os bytes do id "+id+" na porta "+porta+"\n";
                    logArea.setText(log);
                    
                    for (FileFragment frag : fragmentos) {
                        if (frag.getFileFragmentLocationId() == id) {
                            byte[] bytes = frag.getBytes();
                            os.write(bytes);
                           
                            break;
                        }
                    }
                }
                 is.close();
                os.close();
                socketComPivo.close();
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        
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
