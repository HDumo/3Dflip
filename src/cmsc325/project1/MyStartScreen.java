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
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.TextField;
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
        app.money = 1000;
    }
    
    public void resumeGame(String nextScreen) {
        nifty.gotoScreen(nextScreen);
        app.isRunning = true;
    }
    
    //Set bid amount based on value entered by player in text field
    public void setBidAmount() {
        Screen currentScreen = nifty.getCurrentScreen();
        TextField bidAmount = currentScreen.findNiftyControl("bidAmount",TextField.class);
        app.amountOfBid = Integer.parseInt(bidAmount.getText());
    }
    
    //Set bid to Heads or Tails based on button that player clicks
    public void setBid(String bidType) {
        app.bid = bidType;
        System.out.println(bidType);
        System.out.println(app.bid);
    }
 
    public void quitGame() {
        app.stop();
    }
}
