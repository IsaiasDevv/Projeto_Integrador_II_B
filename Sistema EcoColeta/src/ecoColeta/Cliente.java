package ecoColeta;

import java.io.*;
import java.net.*;
import java.util.*;

// Classe Cliente que se comunica com o Servidor via console (linha de comando)
public class Cliente {

    // Método auxiliar para ler e imprimir as respostas enviadas pelo servidor
    private static void lerRespostaDoServidor(BufferedReader entrada) throws IOException {
        String resposta;
        while ((resposta = entrada.readLine()) != null) {
            if (resposta.trim().isEmpty()) break; // Sai do loop quando encontra linha em branco
            System.out.println(">> " + resposta); // Imprime a resposta do servidor
            // Se a resposta indicar que a conexão será encerrada, fecha o programa
            if (resposta.contains("Conexão Encerrada.") || resposta.contains("Servidor será encerrado")) {
                System.exit(0);
            }
        }
        // Se o servidor fechar a conexão inesperadamente
        if (resposta == null) {
            System.out.println("O servidor fechou a conexão.");
            System.exit(0);
        }
    }

    // Método principal
    public static void main(String[] args) {
        // Bloco try-with-resources garante que todos os recursos serão fechados automaticamente
        try (
            Socket socket = new Socket("localhost", 12345); // Conecta ao servidor na porta 12345
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Recebe mensagens
            PrintWriter saida = new PrintWriter(socket.getOutputStream(), true); // Envia mensagens
            Scanner scanner = new Scanner(System.in) // Lê entrada do usuário
        ) {
            boolean executando = true; // Controle do loop principal

            // Loop principal da aplicação
            while (executando) {
                // Exibe menu inicial para seleção do tipo de usuário
                System.out.println("╔════════════════════════════════════════╗");
                System.out.println("║      SISTEMA DE GESTÃO DE PONTOS       ║");
                System.out.println("║          DE COLETA SELETIVA            ║");
                System.out.println("╠════════════════════════════════════════╣");
                System.out.println("║======== BEM-VINDO AO ECOCOLETA ========║");
                System.out.println("╠════════════════════════════════════════╣");
                System.out.println("║ Selecione o tipo de Usuário:           ║");
                System.out.println("║                                        ║");
                System.out.println("║ [1] Cidadão                            ║");
                System.out.println("║ [2] Administrador                      ║");
                System.out.println("║ [3] Sair do Sistema                    ║");
                System.out.println("║                                        ║");
                System.out.println("╚════════════════════════════════════════╝");
                System.out.println();

                // Validação do menu principal: garante que a opção digitada seja válida
                String tipo = "";
                while (true) {
                    System.out.print("Escolha: ");
                    tipo = scanner.nextLine();
                    if (Arrays.asList("1", "2", "3").contains(tipo)) {
                        break;
                    }
                    System.out.println("\nOpção Inválida! Digite 1, 2 ou 3.\n");
                }

                // Opção de sair do sistema
                if (tipo.equals("3")) {
                    saida.println("ENCERRAR_TUDO"); // Comando para encerrar o servidor
                    System.out.println("\nOpção escolhida: 3 - Encerrando o sistema...");
                    System.out.println();
                    lerRespostaDoServidor(entrada); // Lê a resposta do servidor
                    executando = false;
                    break;
                }

                boolean isAdmin = tipo.equals("2"); // Identifica se é administrador
                boolean loginValido = false;        // Controle do login

                // Loop de login
                while (!loginValido) {
                    System.out.print("\nDigite o Nome de Usuário: ");
                    String usuario = scanner.nextLine();
                    System.out.print("Digite a Senha: ");
                    String senha = scanner.nextLine();
                    // Verifica credenciais
                    if ((tipo.equals("1") && usuario.equals("usuario") && senha.equals("12345")) ||
                        (tipo.equals("2") && usuario.equals("admin") && senha.equals("admin"))) {
                        loginValido = true;
                    } else {
                        System.out.println("\nUsuário ou senha incorretos. Tente novamente.");
                    }
                }

                System.out.println("\nLogin realizado com sucesso! Bem-vindo, " + (isAdmin ? "Administrador." : "Cidadão."));

                boolean noMenu = true; // Controle do loop do menu principal
                while (noMenu) {
                    // Exibe menu de acordo com o tipo de usuário
                    if (isAdmin) {
                        System.out.println("\n╔════════════════════════════════════╗");
                        System.out.println("║        MENU ADMINISTRADOR          ║");
                        System.out.println("╠════════════════════════════════════╣");
                        System.out.println("║  [1] Cadastrar Ponto de Coleta     ║");
                        System.out.println("║  [2] Listar Pontos de Coleta       ║");
                        System.out.println("║  [3] Buscar por Material           ║");
                        System.out.println("║  [4] Atualizar Ponto de Coleta     ║");
                        System.out.println("║  [5] Remover Ponto de Coleta       ║");
                        System.out.println("║  [6] Sair                          ║");
                        System.out.println("║                                    ║");
                        System.out.println("╚════════════════════════════════════╝");
                        System.out.println();
                        System.out.print("Escolha: ");
                    } else {
                        System.out.println("\n╔════════════════════════════════════╗");
                        System.out.println("║            MENU CIDADÃO            ║");
                        System.out.println("╠════════════════════════════════════╣");
                        System.out.println("║  [1] Listar Pontos de Coleta       ║");
                        System.out.println("║  [2] Buscar por Material           ║");
                        System.out.println("║  [3] Sair                          ║");
                        System.out.println("║                                    ║");
                        System.out.println("╚════════════════════════════════════╝");
                        System.out.println();
                        System.out.print("Escolha: ");
                    }

                    // Lê a opção do usuário
                    String opcao = scanner.nextLine();

                    // Validação da opção digitada
                    if (isAdmin && !Arrays.asList("1","2","3","4","5","6").contains(opcao)) {
                        System.out.println("\nOpção Inválida! Digite de 1 a 6.\n");
                        continue;
                    }
                    if (!isAdmin && !Arrays.asList("1","2","3").contains(opcao)) {
                        System.out.println("\nOpção Inválida! Digite de 1 a 3.\n");
                        continue;
                    }

                    // Processa a opção escolhida
                    switch (opcao) {
                        case "1":
                            if (isAdmin) {
                                // Cadastro de ponto de coleta
                                System.out.println();
                                System.out.print("Digite o Endereço: ");
                                String endereco = scanner.nextLine();
                                System.out.print("Digite os Materiais Aceitos: ");
                                String materiais = scanner.nextLine();
                                System.out.print("Digite o Horário de Funcionamento: ");
                                String horario = scanner.nextLine();
                                System.out.println();
                                saida.println("CADASTRAR;" + endereco + ";" + materiais + ";" + horario);
                                lerRespostaDoServidor(entrada);
                                continue;
                            } else {
                                // Listagem de pontos de coleta para cidadão
                                saida.println("LISTAR");
                                System.out.println();
                                lerRespostaDoServidor(entrada);
                                continue;
                            }

                        case "2":
                            if (isAdmin) {
                                // Listagem para administrador
                                System.out.println();
                                saida.println("LISTAR");
                                lerRespostaDoServidor(entrada);
                                continue;
                            } else {
                                // Busca por material para cidadão
                                System.out.println();
                                System.out.print("Digite o Material a Buscar: ");
                                String material = scanner.nextLine();
                                saida.println("BUSCAR;" + material);
                                System.out.println();
                                lerRespostaDoServidor(entrada);
                                continue;
                            }

                        case "3":
                            if (!isAdmin) {
                                // Sai do menu cidadão e retorna ao menu inicial
                                System.out.println();
                                System.out.println("Saindo do Menu EcoColeta e Voltando ao Menu Inicial...");
                                System.out.println();
                                try { Thread.sleep(500); } catch (InterruptedException e) {}
                                noMenu = false;
                                continue;
                            } else {
                                // Admin pode buscar por material na opção 3
                                System.out.println();
                                System.out.print("Digite o Material a Buscar: ");
                                String mat = scanner.nextLine();
                                saida.println("BUSCAR;" + mat);
                                System.out.println();
                                lerRespostaDoServidor(entrada);
                                continue;
                            }

                        case "4":
                            // Atualizar ponto de coleta (admin)
                            System.out.println();
                            System.out.print("Índice do Ponto: ");
                            String idxA = scanner.nextLine();
                            System.out.println();
                            System.out.print("Novo Endereço: ");
                            String novoEnd = scanner.nextLine();
                            System.out.print("Novos Materiais: ");
                            String novosMat = scanner.nextLine();
                            System.out.print("Novo Horário de Funcionamento: ");
                            String novoHorario = scanner.nextLine();
                            System.out.println();
                            saida.println("ATUALIZAR;" + idxA + ";" + novoEnd + ";" + novosMat + ";" + novoHorario);
                            lerRespostaDoServidor(entrada);
                            continue;

                        case "5":
                            // Remover ponto de coleta (admin)
                            System.out.println();
                            System.out.print("Índice do Ponto: ");
                            String idxR = scanner.nextLine();
                            System.out.println();
                            saida.println("REMOVER;" + idxR);
                            lerRespostaDoServidor(entrada);
                            continue;

                        case "6":
                            if (isAdmin) {
                                // Sai do menu admin e retorna ao menu inicial
                                System.out.println();
                                System.out.println("Saindo do Menu EcoColeta e Voltando ao Menu Inicial...");
                                System.out.println();
                                try { Thread.sleep(500); } catch (InterruptedException e) {}
                                noMenu = false;
                                continue;
                            }
                            break;
                    }
                }
            }
        } catch (IOException e) {
            // Caso não seja possível conectar ao servidor
            System.err.println("Não foi possível conectar ao servidor. Verifique se ele está em execução.");
            e.printStackTrace();
        }
    }
}
