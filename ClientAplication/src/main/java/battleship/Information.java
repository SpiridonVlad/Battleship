package battleship;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Information implements SceneInterface {
    private MainMenu mainMenu;

    public Information(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
    }

    public Scene showScene(Stage primaryStage) {
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10, 10, 10, 10));
        vbox.setSpacing(10);
        vbox.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Info");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        titleLabel.setAlignment(Pos.CENTER);

        Label infoLabel = new Label("""
                How to Play Battleship:

                1. Each player places their ships on a 10x10 grid. Ships can be placed horizontally or vertically.
                2. The ships are of the same length: (3).
                3. Players take turns calling out a coordinate on the opponent's grid to attack.
                4. The opponent announces whether the attack is a hit or miss. If a ship is hit, it is marked on the grid.
                5. The goal is to sink all of the opponent's ships by hitting all their coordinates.
                6. The first player to sink all of the opponent's ships wins the game.

                This is a simple Battleship game created by Spiridon Vlad.""");
        infoLabel.setStyle("-fx-font-size: 16px;");
        infoLabel.setWrapText(true);
        infoLabel.setAlignment(Pos.CENTER_LEFT);

        Button backButton = createButton("Back", e -> primaryStage.setScene(mainMenu.showScene(primaryStage)));

        vbox.getChildren().addAll(titleLabel, infoLabel, backButton);

        return new Scene(vbox, 800, 400);
    }
}
