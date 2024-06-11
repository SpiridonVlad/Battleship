module battleship.clientaplication {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires jdk.compiler;

    opens battleship to javafx.fxml;
    exports battleship;
}