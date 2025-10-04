package ecoColeta;

// Importações do JavaFX para criar a interface gráfica, animações, layout e efeitos
import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.*;

// Classe principal do cliente com interface gráfica
public class ClienteGUI extends Application {

    // Variáveis para comunicação com o servidor
    private Socket socket;           // Socket para conectar ao servidor
    private BufferedReader entrada;  // Para receber mensagens do servidor
    private PrintWriter saida;       // Para enviar mensagens ao servidor

    private Stage stage;             // Janela principal do JavaFX
    private Scene cena;              // Cena atual que será exibida
    private boolean isAdmin = false; // Controle de tipo de usuário (Cidadão ou Administrador)

    // Paleta de cores utilizada na interface
    private final String corTitulo = "#2c3e50";
    private final String corBotao = "#27ae60";
    private final String corBotaoHover = "#1e8449";
    private final String corTexto = "#2c3e50";
    private final String corCampo = "#ecf0f1";
    private final String corErro = "#e74c3c";
    private final String corSucesso = "#16a085";
    private final String corFocoCampo = "#27ae60";
    private final String corFundoAdmin = "#d0f0c0"; // fundo específico do menu admin

    // Fonte utilizada em toda a aplicação
    private final String fontePrincipal = "Poppins";

    // Método principal que inicia a aplicação JavaFX
    public static void main(String[] args) {
        launch(args); // Lança a aplicação JavaFX
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage; // Armazena a referência da janela principal
        conectarServidor();   // Conecta o cliente ao servidor

        cena = new Scene(new VBox(), 480, 520); // Inicializa a cena com VBox e tamanho definido
        stage.setScene(cena);                  // Define a cena na janela
        stage.show();                           // Exibe a janela

        telaLogin(); // Mostra a tela de login ao iniciar
    }

    // Método que conecta ao servidor
    private void conectarServidor() {
        try {
            socket = new Socket("localhost", 12345); // Conecta na máquina local, porta 12345
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Recebe dados
            saida = new PrintWriter(socket.getOutputStream(), true); // Envia dados
        } catch (IOException _ignore) {
            showError("Não foi possível conectar ao servidor. Verifique se ele está em execução.");
        }
    }

    // ---------------- Telas ----------------

    // Tela de login
    private void telaLogin() {
        VBox root = criarRootBase(40); // Cria o layout base da tela

        // Título da aplicação
        Label lblTitulo = new Label("🌿 EcoColeta");
        lblTitulo.setFont(Font.font(fontePrincipal, 34));
        lblTitulo.setTextFill(Color.web(corTitulo));

        // Texto informativo
        Label lblTipo = new Label("Selecione o tipo de usuário:");
        lblTipo.setFont(Font.font(fontePrincipal, 16));
        lblTipo.setTextFill(Color.web(corTexto));

        // Combobox para selecionar tipo de usuário
        ComboBox<String> tipoBox = new ComboBox<>();
        tipoBox.getItems().addAll("Cidadão", "Administrador");
        tipoBox.setPrefWidth(220);
        tipoBox.setStyle(campoStyle());
        adicionarEfeitoHoverCampo(tipoBox);

        // Campo para digitar o usuário
        TextField usuarioField = new TextField();
        usuarioField.setPromptText("Usuário");
        usuarioField.setFont(Font.font(fontePrincipal, 14));
        usuarioField.setStyle(campoStyle());
        adicionarEfeitoHoverCampo(usuarioField);

        // Campo para digitar a senha
        PasswordField senhaField = new PasswordField();
        senhaField.setPromptText("Senha");
        senhaField.setFont(Font.font(fontePrincipal, 14));
        senhaField.setStyle(campoStyle());
        adicionarEfeitoHoverCampo(senhaField);

        // Botão de login
        Button btnLogin = criarBotao("🔑 Entrar");

        // Label para mostrar mensagens de erro ou sucesso
        Label lblMsg = new Label();
        lblMsg.setFont(Font.font(fontePrincipal, 12));

        // Ação do botão de login
        btnLogin.setOnAction(_ -> {
            String tipo = tipoBox.getValue();
            String usuario = usuarioField.getText().trim();
            String senha = senhaField.getText().trim();

            // Valida se o tipo de usuário foi selecionado
            if (tipo == null) {
                lblMsg.setTextFill(Color.web(corErro));
                lblMsg.setText("Selecione o tipo de usuário.");
                return;
            }

            // Verifica usuário e senha
            if ((tipo.equals("Cidadão") && usuario.equals("usuario") && senha.equals("12345")) ||
                (tipo.equals("Administrador") && usuario.equals("admin") && senha.equals("admin"))) {
                isAdmin = tipo.equals("Administrador"); // Marca se é admin
                lblMsg.setTextFill(Color.web(corSucesso));
                lblMsg.setText("Login realizado com sucesso!");
                fadeSlideTransition(this::telaMenu); // Vai para tela do menu
            } else {
                lblMsg.setTextFill(Color.web(corErro));
                lblMsg.setText("Usuário ou senha incorretos. Tente novamente.");
            }
        });

        // Adiciona todos os elementos no layout
        root.getChildren().addAll(lblTitulo, lblTipo, tipoBox, usuarioField, senhaField, btnLogin, lblMsg);
        aplicarFade(root); // Aplica efeito de fade
        cena.setRoot(root); // Atualiza a cena
        stage.setTitle("EcoColeta - Login"); // Título da janela
    }

    // Tela principal do menu
    private void telaMenu() {
        VBox root = criarRootBase(30);

        // Se for admin, muda a cor do fundo
        if (isAdmin) {
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #a8e6cf, " + corFundoAdmin + ");");
        }

        Label lblMenu = new Label("Menu " + (isAdmin ? "Administrador" : "Cidadão"));
        lblMenu.setFont(Font.font(fontePrincipal, 28));
        lblMenu.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d;"); // cinza escuro

        // Botões do menu
        Button btnCadastrar = criarBotao("➕ Cadastrar Ponto de Coleta");
        Button btnListar = criarBotao("📋 Listar Pontos de Coleta");
        Button btnBuscar = criarBotao("🔍 Buscar por Material");
        Button btnAtualizar = criarBotao("✏️ Atualizar Ponto de Coleta");
        Button btnRemover = criarBotao("🗑️ Remover Ponto de Coleta");
        Button btnSair = criarBotao("🚪 Sair");

        // Adiciona apenas os botões relevantes de acordo com tipo de usuário
        if (!isAdmin) {
            root.getChildren().addAll(lblMenu, btnListar, btnBuscar, btnSair);
        } else {
            root.getChildren().addAll(lblMenu, btnCadastrar, btnListar, btnBuscar, btnAtualizar, btnRemover, btnSair);
        }

        // Configura ações dos botões
        btnCadastrar.setOnAction(_ -> fadeSlideTransition(this::telaCadastro));
        btnListar.setOnAction(_ -> fadeSlideTransition(this::telaListagem));
        btnBuscar.setOnAction(_ -> fadeSlideTransition(this::telaBusca));
        btnAtualizar.setOnAction(_ -> fadeSlideTransition(this::telaAtualizar));
        btnRemover.setOnAction(_ -> fadeSlideTransition(this::telaRemover));
        btnSair.setOnAction(_ -> fadeSlideTransition(() -> {
            isAdmin = false;
            telaLogin();
        }));

        aplicarFade(root);
        cena.setRoot(root);
        stage.setTitle("EcoColeta - Menu");
    }

    // ---------------- Telas de funcionalidade ----------------
    // Cada método abaixo cria uma tela para cadastrar, listar, buscar, atualizar e remover pontos de coleta
    // A lógica é semelhante: criar campos, botões, enviar comando ao servidor e exibir resposta

    // Tela de cadastro
    private void telaCadastro() {
        VBox root = criarRootBase(25);

        Label lbl = new Label("Cadastrar Ponto de Coleta");
        lbl.setFont(Font.font(fontePrincipal, 22));
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #145a32;");

        TextField enderecoField = criarCampo("Endereço");
        TextField materiaisField = criarCampo("Materiais Aceitos (ex: papel, vidro)");
        TextField horarioField = criarCampo("Horário de Funcionamento (ex: 08:00-18:00)");

        Button btnSalvar = criarBotao("💾 Salvar");
        Button btnVoltar = criarBotao("↩️ Voltar");

        Label lblMsg = new Label();
        lblMsg.setFont(Font.font(fontePrincipal, 12));

        // Ao clicar em salvar, envia comando "CADASTRAR" para o servidor
        btnSalvar.setOnAction(_ -> {
            String endereco = enderecoField.getText().trim();
            String materias = materiaisField.getText().trim();
            String horario = horarioField.getText().trim();
            if (endereco.isEmpty() || materias.isEmpty() || horario.isEmpty()) {
                lblMsg.setTextFill(Color.web(corErro));
                lblMsg.setText("Preencha todos os campos!");
                return;
            }
            saida.println("CADASTRAR;" + endereco + ";" + materias + ";" + horario);
            lblMsg.setTextFill(Color.web(corSucesso));
            lblMsg.setText(receberResposta());
        });

        btnVoltar.setOnAction(_ -> fadeSlideTransition(this::telaMenu));

        HBox btnBox = new HBox(15, btnSalvar, btnVoltar);
        btnBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(lbl, enderecoField, materiaisField, horarioField, btnBox, lblMsg);
        aplicarFade(root);
        cena.setRoot(root);
        stage.setTitle("EcoColeta - Cadastro");
    }

    // Tela de listagem
    private void telaListagem() {
        VBox root = criarRootBase(25);

        Label lbl = new Label("Lista de Pontos de Coleta");
        lbl.setFont(Font.font(fontePrincipal, 22));
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #145a32;");

        Button btnVoltar = criarBotao("↩️ Voltar");
        TextArea txt = new TextArea();
        txt.setEditable(false);
        txt.setWrapText(true);
        txt.setFont(Font.font(fontePrincipal, 14));
        txt.setStyle(campoTextAreaStyle());
        adicionarEfeitoHoverCampo(txt);

        // Envia comando "LISTAR" ao servidor e recebe resposta
        saida.println("LISTAR");
        txt.setText(receberRespostaMultilinha());

        btnVoltar.setOnAction(_ -> fadeSlideTransition(this::telaMenu));

        root.getChildren().addAll(lbl, txt, btnVoltar);
        aplicarFade(root);
        cena.setRoot(root);
        stage.setTitle("EcoColeta - Listagem");
    }

    // Tela de busca
    private void telaBusca() {
        VBox root = criarRootBase(25);

        Label lbl = new Label("Buscar por Material");
        lbl.setFont(Font.font(fontePrincipal, 22));
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #145a32;");

        TextField materialField = criarCampo("Digite o material para buscar");
        adicionarEfeitoHoverCampo(materialField);

        Button btnBuscar = criarBotao("🔍 Buscar");
        Button btnVoltar = criarBotao("↩️ Voltar");

        TextArea txt = new TextArea();
        txt.setEditable(false);
        txt.setWrapText(true);
        txt.setFont(Font.font(fontePrincipal, 14));
        txt.setStyle(campoTextAreaStyle());
        adicionarEfeitoHoverCampo(txt);

        // Ao clicar em buscar, envia comando "BUSCAR;material"
        btnBuscar.setOnAction(_ -> {
            String material = materialField.getText().trim();
            if (material.isEmpty()) {
                txt.setText("Digite um material!");
                return;
            }
            saida.println("BUSCAR;" + material);
            txt.setText(receberRespostaMultilinha());
        });

        btnVoltar.setOnAction(_ -> fadeSlideTransition(this::telaMenu));

        root.getChildren().addAll(lbl, materialField, btnBuscar, txt, btnVoltar);
        aplicarFade(root);
        cena.setRoot(root);
        stage.setTitle("EcoColeta - Busca");
    }

    // Tela de atualização de ponto de coleta
    private void telaAtualizar() {
        VBox root = criarRootBase(25);

        Label lbl = new Label("Atualizar Ponto de Coleta");
        lbl.setFont(Font.font(fontePrincipal, 22));
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #145a32;");

        TextField idxField = criarCampo("Índice do Ponto");
        TextField enderecoField = criarCampo("Novo Endereço");
        TextField materiaisField = criarCampo("Novos Materiais");
        TextField horarioField = criarCampo("Novo Horário de Funcionamento");

        Button btnAtualizar = criarBotao("✏️ Atualizar");
        Button btnVoltar = criarBotao("↩️ Voltar");

        Label lblMsg = new Label();
        lblMsg.setFont(Font.font(fontePrincipal, 12));

        // Ao clicar em atualizar, envia comando "ATUALIZAR;indice;endereco;materiais;horario"
        btnAtualizar.setOnAction(_ -> {
            String idx = idxField.getText().trim();
            String endereco = enderecoField.getText().trim();
            String materias = materiaisField.getText().trim();
            String horario = horarioField.getText().trim();
            if (idx.isEmpty() || endereco.isEmpty() || materias.isEmpty() || horario.isEmpty()) {
                lblMsg.setTextFill(Color.web(corErro));
                lblMsg.setText("Preencha todos os campos!");
                return;
            }
            saida.println("ATUALIZAR;" + idx + ";" + endereco + ";" + materias + ";" + horario);
            lblMsg.setTextFill(Color.web(corSucesso));
            lblMsg.setText(receberResposta());
        });

        btnVoltar.setOnAction(_ -> fadeSlideTransition(this::telaMenu));

        root.getChildren().addAll(lbl, idxField, enderecoField, materiaisField, horarioField, btnAtualizar, btnVoltar, lblMsg);
        aplicarFade(root);
        cena.setRoot(root);
        stage.setTitle("EcoColeta - Atualizar");
    }

    // Tela de remoção de ponto de coleta
    private void telaRemover() {
        VBox root = criarRootBase(25);

        Label lbl = new Label("Remover Ponto de Coleta");
        lbl.setFont(Font.font(fontePrincipal, 22));
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #145a32;");

        TextField idxField = criarCampo("Índice do Ponto");
        adicionarEfeitoHoverCampo(idxField);

        Button btnRemover = criarBotao("🗑️ Remover");
        Button btnVoltar = criarBotao("↩️ Voltar");

        Label lblMsg = new Label();
        lblMsg.setFont(Font.font(fontePrincipal, 12));

        // Ao clicar em remover, envia comando "REMOVER;indice"
        btnRemover.setOnAction(_ -> {
            String idx = idxField.getText().trim();
            if (idx.isEmpty()) {
                lblMsg.setTextFill(Color.web(corErro));
                lblMsg.setText("Preencha o índice!");
                return;
            }
            saida.println("REMOVER;" + idx);
            lblMsg.setTextFill(Color.web(corSucesso));
            lblMsg.setText(receberResposta());
        });

        btnVoltar.setOnAction(_ -> fadeSlideTransition(this::telaMenu));

        root.getChildren().addAll(lbl, idxField, btnRemover, btnVoltar, lblMsg);
        aplicarFade(root);
        cena.setRoot(root);
        stage.setTitle("EcoColeta - Remover");
    }

    // ---------------- Métodos auxiliares ----------------

    // Recebe resposta de uma linha do servidor
    private String receberResposta() {
        try {
            StringBuilder sb = new StringBuilder();
            String resposta;
            while ((resposta = entrada.readLine()) != null) {
                if (resposta.trim().isEmpty()) break;
                sb.append(resposta).append("\n");
            }
            return sb.toString().trim();
        } catch (IOException _ignore) {
            return "Erro ao receber resposta do servidor.";
        }
    }

    // Recebe resposta multilinha (reutiliza receberResposta)
    private String receberRespostaMultilinha() {
        return receberResposta();
    }

    // Mostra mensagem de erro e encerra a aplicação
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
        System.exit(0);
    }

    // Cria botão com estilo e efeitos
    private Button criarBotao(String texto) {
        Button b = new Button(texto);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setFont(Font.font(fontePrincipal, 14));
        b.setStyle(botaoStyle(corBotao));
        b.setCursor(javafx.scene.Cursor.HAND);

        // Efeito de sombra
        DropShadow sombra = new DropShadow();
        sombra.setColor(Color.rgb(0,0,0,0.2));
        sombra.setRadius(5);
        b.setEffect(sombra);

        // Animação ao passar o mouse
        ScaleTransition stEnter = new ScaleTransition(Duration.millis(150), b);
        stEnter.setToX(1.05);
        stEnter.setToY(1.05);

        ScaleTransition stExit = new ScaleTransition(Duration.millis(150), b);
        stExit.setToX(1);
        stExit.setToY(1);

        b.setOnMouseEntered(_ -> {
            b.setStyle(botaoStyle(corBotaoHover));
            stEnter.playFromStart();
        });

        b.setOnMouseExited(_ -> {
            b.setStyle(botaoStyle(corBotao));
            stExit.playFromStart();
        });

        return b;
    }

    private String botaoStyle(String cor) {
        return "-fx-background-color: " + cor + "; " +
               "-fx-text-fill: white; " +
               "-fx-background-radius: 12px; " +
               "-fx-border-radius: 12px;";
    }

    private TextField criarCampo(String placeholder) {
        TextField tf = new TextField();
        tf.setPromptText(placeholder);
        tf.setFont(Font.font(fontePrincipal, 14));
        tf.setStyle(campoStyle());
        adicionarEfeitoHoverCampo(tf);
        return tf;
    }

    private String campoStyle() {
        return "-fx-background-color: " + corCampo + "; " +
               "-fx-background-radius: 12px; " +
               "-fx-border-radius: 12px; " +
               "-fx-border-color: #bdc3c7; " +
               "-fx-padding: 6px;";
    }

    private String campoTextAreaStyle() {
        return "-fx-background-color: #ffffff; " +
               "-fx-font-size: 14px; " +
               "-fx-background-radius: 12px; " +
               "-fx-border-radius: 12px; " +
               "-fx-border-color: #bdc3c7; " +
               "-fx-padding: 6px;";
    }

    private void adicionarEfeitoHoverCampo(Control campo) {
        campo.setOnMouseEntered(_ -> campo.setStyle(campoStyle() + "-fx-border-color: " + corFocoCampo + ";"));
        campo.setOnMouseExited(_ -> campo.setStyle(campoStyle()));
    }

    private VBox criarRootBase(double padding) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(padding));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #a8e6cf, #dcedc1);");
        return root;
    }

    // Animação de transição entre telas
    private void fadeSlideTransition(Runnable acao) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), cena.getRoot());
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(_ -> acao.run());
        ft.play();
    }

    // Aplica fade-in em uma tela
    private void aplicarFade(Pane pane) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), pane);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }
}
