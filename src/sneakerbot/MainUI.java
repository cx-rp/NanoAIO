package sneakerbot;

import com.sun.javafx.scene.control.behavior.ButtonBehavior;
import com.sun.javafx.scene.control.behavior.KeyBinding;

import java.io.IOException;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import static javafx.scene.input.KeyEvent.KEY_RELEASED;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import sneakerbot.animation.FadeInUpTransition;

/*
 * Main: #0B132B
 * Secondary: #1C2541
 * Passive: #3A506B, #5BC0BE, #6FFFE9
 */

@SuppressWarnings("restriction")
public class MainUI extends Application {
    private double xOffset = 0;
    private double yOffset = 0;
    
    @Override
    public void start(Stage stage) throws IOException {
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        System.setProperty("webdriver.chrome.logfile", "chromedriver.log");
        System.setProperty("webdriver.chrome.args", "--disable-logging");
        System.setProperty("webdriver.chrome.silentOutput", "true");

       // JigAddress.jigAddress("");
        new EnableButtonEnterKey();
        
        StackPane root = new FXMLLoader(getClass().getResource("/main.fxml")).load();
        
        stage.initStyle(StageStyle.TRANSPARENT);
        
        root.setOnMousePressed(new EventHandler<MouseEvent>() {
           @Override
           public void handle(MouseEvent event) {
               xOffset = event.getSceneX();
               yOffset = event.getSceneY();
           }
       });
        
       root.setOnMouseDragged(new EventHandler<MouseEvent>() {
           @Override
           public void handle(MouseEvent event) {
               stage.setX(event.getScreenX() - xOffset);
               stage.setY(event.getScreenY() - yOffset);
           }
       });

        Scene scene = new Scene(root);

        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();

        StackPane rootLogin = new FXMLLoader(getClass().getResource("/login.fxml")).load();
        Scene sceneLogin = new Scene(rootLogin);

        Stage stageLogin = new Stage(StageStyle.TRANSPARENT);
        
        stageLogin.initModality(Modality.APPLICATION_MODAL);
        stageLogin.initOwner(stage);
        stageLogin.setScene(sceneLogin);
        stageLogin.getScene().getRoot().setStyle("-fx-background-color: transparent;");
        stageLogin.getScene().setFill(null);
        stageLogin.setOnHiding((e) -> root.setEffect(null));
        stageLogin.show();

        rootLogin.setOpacity(0);

        new FadeInUpTransition(rootLogin)
                .setDelayTime(Duration.millis(200))
                .setDuration(Duration.millis(500))
                .play();

    }

    public static void main(String[] args) {
        launch(args);
    }

    public class EnableButtonEnterKey extends ButtonBehavior<Button> {

        public EnableButtonEnterKey() {
            super(new Button());
            BUTTON_BINDINGS.add(new KeyBinding(ENTER, KEY_PRESSED, "Press"));
            BUTTON_BINDINGS.add(new KeyBinding(ENTER, KEY_RELEASED, "Release"));
        }
    }
}
