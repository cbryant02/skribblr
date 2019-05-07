package com.github.cbryant02.skribblr;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
        System.exit(0);
    }

    @Override
    public void start(Stage stage) throws IOException, NativeHookException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/main.fxml"));
        loader.setController(new MainController(stage));
        loader.load();

        // Set up and show main stage
        Parent root = loader.getRoot();
        stage.setTitle("Skribblr");
        stage.setScene(new Scene(root, root.prefWidth(-1), root.prefHeight(-1)));
        stage.setResizable(false);
        stage.show();

        // Move stage out of the way
        stage.setX(Screen.getPrimary().getBounds().getWidth() - stage.getWidth() - 10);
        stage.setY(Screen.getPrimary().getBounds().getHeight() - stage.getHeight() - 50);

        // Disable JNativeHook logger because it nukes stdout from orbit
        Logger jnhLogger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        jnhLogger.setLevel(Level.OFF);
        jnhLogger.setUseParentHandlers(false);

        // Add F1 listener
        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeKeyListener(new HaltListener());
    }

    private class HaltListener implements NativeKeyListener {
        @Override
        public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {}

        @Override
        public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
            if(nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_F1) {
                try {
                    GlobalScreen.unregisterNativeHook();
                } catch (NativeHookException e) { e.printStackTrace(); }

                // If we were drawing, the mouse is probably still pressed down; release it
                Robot r = null;
                try {
                    r = new Robot();
                } catch (AWTException e) { e.printStackTrace(); }
                assert r != null;
                r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

                System.exit(0);
            }
        }

        @Override
        public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {}
    }
}
