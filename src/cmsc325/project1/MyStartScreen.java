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
 
    // Launch game after clicking on start button
    public void startGame(String nextScreen) {
        nifty.gotoScreen(nextScreen);
        app.LoadMainGame();
        app.isRunning = true;
    }
    
    // Update player name when player button is clicked
    public void playerName(String nameOfPlayer) {
        app.displayPlayerName = nameOfPlayer;
        app.money = 1000;
        app.luckyCharm = 0.0;
    }
    
    // Resume game when player clicks on Resume
    public void resumeGame(String nextScreen) {
        nifty.gotoScreen(nextScreen);
        app.isRunning = true;
    }
    
    // Set bet amount based on value entered by player in text field
    public void setBetAmount() {
        Screen currentScreen = nifty.getCurrentScreen();
        TextField betAmount = currentScreen.findNiftyControl("betAmount",TextField.class);
        app.amountOfBet = Integer.parseInt(betAmount.getText());
    }
    
    // Set bet to Heads or Tails based on button that player clicks
    public void setBet(String betType) {
        app.bet = betType;
    }
 
    // Quit game if player clicks on Quit
    public void quitGame() {
        app.stop();
    }
    
    // Change screen when player clicks on shop
    public void enterShop(String nextScreen) {
        nifty.gotoScreen(nextScreen);
    }
    
    // Change screen when player clicks on exit shop
    public void exitShop(String nextScreen) {
        nifty.gotoScreen(nextScreen);
    }
    
    // Buy lucky charm!
    public void purchaseLuckyCharm() {
        if (app.money > 1000) {
          app.luckyCharm += 1.0;
          app.money -= 1000;
        }
    }
}
