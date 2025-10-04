package ecoColeta;

// Importações necessárias para comunicação via rede, leitura/escrita e manipulação de listas
import java.io.*;
import java.net.*;
import java.util.*;

// Classe que representa um Ponto de Coleta de materiais recicláveis
class PontoColeta {
    private String endereco;   // Endereço do ponto de coleta
    private String materiais;  // Materiais aceitos nesse ponto
    private String horario;    // Horário de funcionamento

    // Construtor da classe que inicializa os atributos
    public PontoColeta(String endereco, String materiais, String horario) {
        this.endereco = endereco;
        this.materiais = materiais;
        this.horario = horario;
    }

    // Método que retorna uma representação em String do objeto (usado para exibir informações)
    @Override
    public String toString() {
        return "Endereço: " + endereco + " | Materiais aceitos: " + materiais + " | Horário: " + horario;
    }

    // Método getter para acessar os materiais aceitos
    public String getMateriais() { return materiais; }

    // Métodos setters para atualizar os atributos do ponto de coleta
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public void setMateriais(String materiais) { this.materiais = materiais; }
    public void setHorario(String horario) { this.horario = horario; }
}

// Classe principal do servidor
public class Servidor {
    // Lista que armazenará todos os pontos de coleta, sincronizada para acesso seguro por múltiplas threads
    private static List<PontoColeta> pontos = Collections.synchronizedList(new ArrayList<>());
    
    // Variável que indica se o servidor está rodando
    private static boolean rodando = true;

    // Método principal que inicia o servidor
    public static void main(String[] args) {
        try (ServerSocket servidor = new ServerSocket(12345)) { // Cria o servidor na porta 12345
            System.out.println("Servidor iniciado na porta 12345...");
            System.out.println("Digite 'SAIR' no console para encerrar o servidor.");

            // Thread separada para ler comandos do console (como "SAIR" para encerrar o servidor)
            new Thread(() -> {
                try {
                    BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
                    while (true) {
                        String comando = console.readLine(); // Lê o comando digitado
                        if (comando != null && comando.equalsIgnoreCase("SAIR")) { // Se for "SAIR"
                            rodando = false; // Para o loop principal
                            try { servidor.close(); } catch (IOException e) {} // Fecha o servidor
                            System.out.println("Servidor encerrado.");
                            System.exit(0); // Encerra o programa
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Loop principal do servidor para aceitar conexões de clientes
            while (rodando) {
                try {
                    Socket socket = servidor.accept(); // Aguarda um cliente se conectar
                    System.out.println("Cliente conectado: " + socket.getInetAddress());

                    // Cria uma nova thread para cada cliente conectado (para não travar o servidor)
                    new Thread(() -> {
                        try (
                            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Leitura do cliente
                            PrintWriter saida = new PrintWriter(socket.getOutputStream(), true) // Envio de mensagens para o cliente
                        ) {
                            String comando;
                            // Loop para receber comandos do cliente
                            while ((comando = entrada.readLine()) != null) {

                                if (comando.equals("ENCERRAR_TUDO")) { // Comando especial (não encerra o servidor neste caso)
                                    saida.println("Servidor será encerrado. Todos os clientes desconectados.");
                                    saida.println();
                                } else if (comando.startsWith("CADASTRAR")) { // Cadastrar um novo ponto de coleta
                                    String[] dados = comando.split(";", 4); // Divide o comando em partes
                                    if (dados.length == 4) {
                                        // Adiciona o novo ponto na lista
                                        pontos.add(new PontoColeta(dados[1], dados[2], dados[3]));
                                        saida.println("Ponto de Coleta Cadastrado com Sucesso!");
                                        saida.println();
                                    } else {
                                        // Mensagem de erro caso o formato esteja incorreto
                                        saida.println("Formato Inválido. Use: CADASTRAR;endereco;materiais;horario");
                                        saida.println();
                                    }
                                } else if (comando.equals("LISTAR")) { // Listar todos os pontos de coleta
                                    if (pontos.isEmpty()) {
                                        saida.println("Nenhum Ponto de Coleta Cadastrado.");
                                        saida.println();
                                    } else {
                                        int i = 1;
                                        synchronized (pontos) { // Sincroniza o acesso à lista
                                            for (PontoColeta p : pontos) {
                                                saida.println(i + " - " + p.toString());
                                                i++;
                                            }
                                        }
                                        saida.println();
                                    }
                                } else if (comando.startsWith("BUSCAR")) { // Buscar pontos por material
                                    String[] dados = comando.split(";", 2);
                                    if (dados.length == 2) {
                                        String material = dados[1];
                                        boolean encontrado = false;
                                        synchronized (pontos) {
                                            for (PontoColeta p : pontos) {
                                                if (p.getMateriais().toLowerCase().contains(material.toLowerCase())) {
                                                    saida.println(p.toString());
                                                    encontrado = true;
                                                }
                                            }
                                        }
                                        if (!encontrado) {
                                            saida.println("Nenhum Ponto Encontrado para o Material: " + material);
                                        }
                                        saida.println();
                                    } else {
                                        saida.println("Formato Inválido. Use: BUSCAR;material");
                                        saida.println();
                                    }
                                } else if (comando.startsWith("ATUALIZAR")) { // Atualizar um ponto de coleta
                                    String[] dados = comando.split(";", 5);
                                    if (dados.length == 5) {
                                        try {
                                            int index = Integer.parseInt(dados[1]) - 1; // Converte índice informado
                                            if (index >= 0 && index < pontos.size()) {
                                                // Atualiza os dados do ponto
                                                pontos.get(index).setEndereco(dados[2]);
                                                pontos.get(index).setMateriais(dados[3]);
                                                pontos.get(index).setHorario(dados[4]);
                                                saida.println("Ponto de Coleta Atualizado com Sucesso!");
                                                saida.println();
                                            } else {
                                                saida.println("Índice Inválido.");
                                                saida.println();
                                            }
                                        } catch (NumberFormatException e) {
                                            saida.println("Formato Inválido. O índice deve ser um Número.");
                                            saida.println();
                                        }
                                    } else {
                                        saida.println("Formato Inválido. Use: ATUALIZAR;indice;novoEndereco;novosMateriais;novoHorario");
                                        saida.println();
                                    }
                                } else if (comando.startsWith("REMOVER")) { // Remover ponto de coleta
                                    String[] dados = comando.split(";", 2);
                                    if (dados.length == 2) {
                                        try {
                                            int index = Integer.parseInt(dados[1]) - 1;
                                            if (index >= 0 && index < pontos.size()) {
                                                pontos.remove(index); // Remove o ponto da lista
                                                saida.println("Ponto de Coleta Removido com Sucesso!");
                                                saida.println();
                                            } else {
                                                saida.println("Índice Inválido.");
                                                saida.println();
                                            }
                                        } catch (NumberFormatException e) {
                                            saida.println("Formato Inválido. O índice deve ser um Número.");
                                            saida.println();
                                        }
                                    } else {
                                        saida.println("Formato Inválido. Use: REMOVER;indice");
                                        saida.println();
                                    }
                                } else if (comando.equals("SAIR")) { // Comando para encerrar a conexão do cliente
                                    saida.println("Conexão Encerrada.");
                                    saida.println();
                                    break;
                                } else { // Caso o comando seja desconhecido
                                    saida.println("Comando Inválido.");
                                    saida.println();
                                }
                            }
                        } catch (SocketException e) {
                            System.out.println("Cliente desconectado."); // Cliente fechou a conexão
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start(); // Fim da thread do cliente

                } catch (IOException e) {
                    if (!rodando) break; // Sai do loop caso o servidor tenha sido encerrado
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
