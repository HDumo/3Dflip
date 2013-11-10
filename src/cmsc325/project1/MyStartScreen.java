/*
 * Filename:    MyStartScreen.java
 * Author:      Eduardo R. Rivas, Bryan J. VerHoven, KC
 * Class:       CMSC 325
 * Date:        5 Nov 2013
 * Assignment:  Project 1
 */

package cmsc325.project1;
 
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.system.AppSettings;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
 
public class MyStartScreen extends AbstractAppState implements ScreenController {
 
    Nifty nifty;
    private Screen screen;
    private CoinFlip3D app;
        
    public MyStartScreen() {
    }
 
    public void bind(Nifty nifty, Screen screen) {
        this.nifty = nifty;
        this.screen = screen;
    }
 
    public void onStartScreen() {
        //Unused
    }
 
    public void onEndScreen() {
        //Unused
    }
 
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        
        this.app = (CoinFlip3D) app;
    }
 
    //@Override
    //public void update(float tpf) {
    //}
 
    public void startGame(String nextScreen) {
        nifty.gotoScreen(nextScreen);
        app.LoadMainGame();
        app.isRunning = true;
    }
    
    public void playerName(String nameOfPlayer) {
        app.displayPlayerName = nameOfPlayer;
        app.score = 0;
    }
    
    public void resumeGame(String nextScreen) {
        nifty.gotoScreen(nextScreen);
        app.isRunning = true;
    }
 
    public void quitGame() {
        app.stop();
    }
}
