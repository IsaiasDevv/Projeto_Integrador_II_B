module Projeto_Integrador_II_B {
    requires java.desktop;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;

    opens ecoColeta to javafx.graphics, javafx.fxml;
}
