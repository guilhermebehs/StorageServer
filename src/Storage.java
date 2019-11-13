
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
    private static final String FINAL_ARQUIVO = "255,255,255,255,255";
    private int porta;
    
    public Storage(int porta){
        this.porta = porta;
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

                List<Integer> ints = new ArrayList();
                int operacao = is.read();
                if(operacao == Operacao.ENVIAR_BYTES.valor){
                     int b = 0;
                    String sinalFimLeitura = "";
                    while (!(sinalFimLeitura.contains(FINAL_ARQUIVO)) && (b = is.read()) != -1) {

                        String[] sinalFimLeituraSplit = sinalFimLeitura.split(",");
                        sinalFimLeitura += "," + b;
                        ints.add(b);
                    }
                    byte[] bytes = new byte[ints.size()-5];

                    for (int i = 0; i < ints.size()-5; i++) {
                        bytes[i] = ints.get(i).byteValue();
                    }
                    
                     int idNovo = is.read();
                     int sequence = is.read();
                     FileFragment fragment = new FileFragment();
                    fragment.setBytes(bytes);
                    fragment.setFileFragmentLocationId(idNovo);
                    fragment.setSequence(sequence);
                    fragmentos.add(fragment);   
                }
                else if(operacao == Operacao.RETORNAR_BYTES.valor){
                    
                    int id = is.read();
                    for (FileFragment frag : fragmentos) {
                        if (frag.getFileFragmentLocationId() == id) {
                            byte[] bytes = frag.getBytes();
                            os.write(bytes);
                            
                            sinalizarFimArquivo(os);
                           
                            os.write(frag.getSequence());
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
