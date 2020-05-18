import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server extends Thread {
    private static Map<String, PrintStream> clientes;
    private Socket conexao;
    private String clientAtual;
    private static List<String> nomes = new ArrayList<String>();
    public Server(Socket socket) {
        this.conexao = socket;
    }
    public boolean armazena(String newName) {
        for (int i = 0; i < nomes.size(); i++) {
            if (nomes.get(i).equals(newName))
                return true;
        }
        nomes.add(newName);
        return false;
    }
    public void remove(String oldName) {
        for (int i = 0; i < nomes.size(); i++) {
            if (nomes.get(i).equals(oldName))
                nomes.remove(oldName);
        }
    }
    public static void main(String args[]) {
        clientes = new HashMap<String, PrintStream>();
        try {
            ServerSocket server = new ServerSocket(2000);
            System.out.println("Server rodando na porta 2000");
            while (true) {
                Socket conexao = server.accept();
                Thread t = new Server(conexao);
                t.start();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
    }
    public void run() {
        try {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(this.conexao.getInputStream()));
            PrintStream saida = new PrintStream(this.conexao.getOutputStream());
            this.clientAtual = entrada.readLine();
            if (armazena(this.clientAtual)) {
                saida.println("Este nome ja existe! Conecte novamente com outro Nome.");
                this.conexao.close();
                return;
            } else {
                System.out.println(this.clientAtual + " : Conectado ao Servidor!");
                saida.println("Conectados: " + nomes.toString());
            }
            if (this.clientAtual == null) {
                return;
            }

            clientes.put(this.clientAtual, saida);
            String[] msg = entrada.readLine().split(":");
            while (msg != null && !(msg[0].trim().equals(""))) {
                send(saida, " escreveu: ", msg);
                msg = entrada.readLine().split(":");
            }
            System.out.println(this.clientAtual + " saiu do bate-papo!");
            String[] out = {" do bate-papo!"};
            send(saida, " saiu", out);
            remove(this.clientAtual);
            clientes.remove(this.clientAtual);
            this.conexao.close();
        } catch (IOException e) {
            System.out.println("Falha na Conexao... .. ." + " IOException: " + e);
        }
    }

    public void send(PrintStream saida, String acao, String[] msg) {
        out:
        for (Map.Entry<String, PrintStream> cliente : clientes.entrySet()) {
            PrintStream chat = cliente.getValue();
            if (chat != saida) {
                if (msg.length == 1) {
                    chat.println(this.clientAtual + acao + msg[0]);
                } else {
                    if (msg[1].equalsIgnoreCase(cliente.getKey())) {
                        chat.println(this.clientAtual + acao + msg[0]);
                        break out;
                    }
                }
            }
        }
    }
}