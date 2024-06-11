package battleship;

import Data.Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PlayerInfo implements SceneInterface {

    private final Player player;
    private final MainMenu mainMenu;

    public PlayerInfo(Player player, MainMenu mainMenu) {
        this.player = player;
        this.mainMenu = mainMenu;
    }

    public Scene showScene(Stage primaryStage) {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        int buttonWidth = 200;

        Button nameButton = new Button("Name: " + player.getName());
        nameButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px 20px;");
        nameButton.setMinWidth(buttonWidth);

        Button scoreButton = new Button("Score: " + player.getScore());
        scoreButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px 20px;");
        scoreButton.setMinWidth(buttonWidth);

        Button gamesPlayedButton = new Button("Games Played: " + player.getGamesPlayed());
        gamesPlayedButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px 20px;");
        gamesPlayedButton.setMinWidth(buttonWidth);

        Button winLossRatioButton = new Button("Win/Loss Ratio: " + player.getWinLossRatio());
        winLossRatioButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px 20px;");
        winLossRatioButton.setMinWidth(buttonWidth);

        Button backButton = createButton("Back", e -> primaryStage.setScene(mainMenu.showScene(primaryStage)));
        backButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px 20px;");
        backButton.setMinWidth(buttonWidth);

        vbox.getChildren().addAll(nameButton, scoreButton, gamesPlayedButton, winLossRatioButton, backButton);

        return new Scene(vbox, 500, 500);
    }

    public BorderPane showPane() {
        BorderPane playerInfoPane = new BorderPane();
        playerInfoPane.setPrefWidth(200);
        String playerName = player.getName();
        if (playerName == null) {
            playerName = "User";
        }
        Button playerInfoButton = new Button(playerName + " [" + player.getScore() + "]");
        playerInfoButton.setStyle("-fx-font-weight: bold;");
        playerInfoButton.setOnAction(event -> System.out.println("MAYBE SOMETHING HERE?"));
        playerInfoPane.setCenter(playerInfoButton);
        return playerInfoPane;
    }
}
