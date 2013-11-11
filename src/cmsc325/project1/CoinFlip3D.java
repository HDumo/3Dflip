/*
 * Filename:    CoinFlip.java
 * Author:      Eduardo R. Rivas, Bryan J. VerHoven, KC
 * Class:       CMSC 325
 * Date:        5 Nov 2013
 * Assignment:  Project 1
 */

package cmsc325.project1;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.ActionListener;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.Spatial;
import com.jme3.light.DirectionalLight;
import com.jme3.math.FastMath;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.system.AppSettings;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.elements.render.TextRenderer;

public class CoinFlip3D extends SimpleApplication {
    
    public static boolean isDebug = false;
    //Declare variables
    private BulletAppState bulletAppState;
    Material tableMaterial;
    private RigidBodyControl coinPhysics;
    private RigidBodyControl tablePhysics;
    Spatial coin;
    boolean guessedCorrectly = false;
    public boolean isHeads;
    public int amountOfBet;
    public double luckyCharm = 0.0;
    public String bet = "None";
        
    // flipRoundState tracks where a coin is at in
    // its sacred journey to becoming a statistic.
    // null = not initialized yet
    // ready = all dependancies and visual updates complete
    // flipstart = its up in the air
    // fliplanded = table collision
    // resolved = updates complete
    // error = coin fell off the table
    // broke = no money
    private enum flipRoundState {NULL, READY, FLIPSTART, FLIPLANDED, RESOLVED, ERROR, BROKE};
    private flipRoundState flipState;
    private Integer count = 0;
    
    private String display_flipState;
    
    private static Box table;
    Nifty nifty;
    public int money = 1000;
    public String displayPlayerName = "Player 1";
    public boolean isRunning = false;
    
    public static void main(String[] args){
        CoinFlip3D coinFlip = new CoinFlip3D();
        
        // Setup the launch screen
        AppSettings flip3d = new AppSettings(true);
        flip3d.setResolution(1360,768);
        flip3d.setSettingsDialogImage("Interface/Images/Coin.jpg");
        flip3d.setTitle("Flip3D - Team Asteroids");
        
        coinFlip.setSettings(flip3d);
        coinFlip.setDisplayFps(isDebug);
        coinFlip.setDisplayStatView(isDebug);
            
        coinFlip.start();
    }
    
    @Override
    public void simpleInitApp(){
        //disable flycam
        flyCam.setEnabled(false);
        //Initialize GUI
        NiftyJmeDisplay display = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, viewPort); //create jme-nifty-processor
        nifty = display.getNifty();
        nifty.addXml("Interface/xmlNameGoes.xml");
        nifty.gotoScreen("start");
        MyStartScreen screenControl = (MyStartScreen) nifty.getScreen("start").getScreenController();
        stateManager.attach(screenControl);
        guiViewPort.addProcessor(display); //add it to your gui-viewport so that the processor will start working
        
    }
    
    public void LoadMainGame() {
       // Setup Physics 
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        MyStartScreen screenControl3 = (MyStartScreen) nifty.getScreen("hud").getScreenController();
        stateManager.attach(screenControl3);
        
        MyStartScreen screenControl4 = (MyStartScreen) nifty.getScreen("shop").getScreenController();
        stateManager.attach(screenControl4);
        
        // configure the camera to look at scene
        cam.setLocation(new Vector3f(0, 4.0f, 6.0f));
        cam.lookAt(new Vector3f(0.0f, 1.0f, 0.0f), Vector3f.UNIT_Y);
        
        // add InputManager action: spacebar flips the coin
        inputManager.addMapping("Flip the Coin", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(actionListener, "Flip the Coin");
        inputManager.deleteMapping(INPUT_MAPPING_EXIT);
        inputManager.addMapping("Escape", new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener(actionListener, "Escape");
        
        //initialize the coin flip state 
        flipState = flipRoundState.NULL;
        
        // initialize materials, table
        initCoin();
        initTableMaterial();      
        initLight();
        initTable();
    }
    
    //action listener
    private ActionListener actionListener = new ActionListener() {
      public void onAction(String name, boolean keyPressed, float tpf) {
        if (name.equals("Escape")) {
          isRunning = false;
          nifty.gotoScreen("pause");
          MyStartScreen screenControl2 = (MyStartScreen) nifty.getScreen("pause").getScreenController();
          stateManager.attach(screenControl2);
        }
        if (name.equals("Flip the Coin") && !keyPressed) {
          flipCoin();
        }
      }
    };
    
    // initialize the materials
    public void initTableMaterial(){
        tableMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        tableMaterial.setTexture("ColorMap", assetManager.loadTexture("Textures/table/table.jpg"));
    }
    
    // light
    public void initLight(){
        // add a light
        DirectionalLight light = new DirectionalLight();
        light.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
        
        // display light
        rootNode.addLight(light);
    }
    
    // coin
    public void initCoin(){
        // load the coin
        coin = assetManager.loadModel("Models/penny/penny.j3o");
        
        // make the coin physical with a mass > 0.0f
        coinPhysics = new RigidBodyControl(2.0f);
        
        // add physical coin to physics space
        coin.addControl(coinPhysics);
        
        // display coin
        rootNode.attachChild(coin);
        
        bulletAppState.getPhysicsSpace().add(coinPhysics);
    }
    
    // Floor
    public void initTable(){
        table = new Box(5.0f, 0.5f, 5.0f);
        table.scaleTextureCoordinates(new Vector2f(1, 1));
        
        Geometry tableGeometry = new Geometry("table", table);
        tableGeometry.setMaterial(tableMaterial);
        tableGeometry.setLocalTranslation(0.0f, 0.0f, 0.0f);
        this.rootNode.attachChild(tableGeometry);
        
        // make the table with a mass of 0.0f
        tablePhysics = new RigidBodyControl(0.0f);
        tableGeometry.addControl(tablePhysics);
        bulletAppState.getPhysicsSpace().add(tablePhysics);
    }
    
    public void flipCoin(){
        
        // only allow a flip if we are ready for another 
        if (flipState == flipRoundState.READY) {
            
            coinPhysics.setLinearVelocity(Vector3f.UNIT_Y.mult(6));
            coinPhysics.setAngularVelocity(new Vector3f(45.0f, 0.0f, 0.0f));
        
            // initiate new flip with this state
            flipState = flipRoundState.FLIPSTART;
        } 
    }
    
    // update loop
    @Override
    public void simpleUpdate(float tpf) {
       if (isRunning) {

           
        switch (flipState) {
            
            case BROKE:
                display_flipState = "You are out of money! ";
            break;
                
            case NULL:
                display_flipState = "here is a coin..";
                flipState = flipRoundState.READY; 
            break;

            case READY:
                display_flipState = "Press space to play";
            break;

            case FLIPSTART:
                
                // make sure that the coin was flipped up in the air and 
                // has come back down to table
                if (coin.getLocalTranslation().getY() < 1 && count > 250)
                {
                    count = 0;
                    flipState = flipRoundState.FLIPLANDED;
                } else {
                    display_flipState = "waiting for coin" ;
                    count++;
                }
                break;

            case FLIPLANDED:
                // This is to catch an edge case where the coin in rolling around
                
               Boolean isHeadsState = isHeads;
               display_flipState = "watching ...";
               
               // dont move on until the side showing is stable
               if (isHeads == isHeadsState) {count ++; } else { count =0;} 
                
               if (count > 550) {
                flipState = flipRoundState.RESOLVED;
                count = 0;
               }
            break;

            case RESOLVED:
                
                // Show message for 250
                // Compare result to player's guess
                if ((isHeads) && bet.equals("Heads")) {
                    display_flipState = "Guess: Heads Result: Heads Great job! You won $" + (amountOfBet + (amountOfBet*(luckyCharm/10.0)));
                    guessedCorrectly = true;
                } else if ((isHeads) && bet.equals("Tails")) {
                    display_flipState = "Guess: Tails Result: Heads Sorry! You lost $" + amountOfBet;
                    guessedCorrectly = false;
                } else if ((!isHeads) && bet.equals("Tails")) {
                    display_flipState = "Guess: Tails Result: Tails Great job! You won $" + (amountOfBet + (amountOfBet*(luckyCharm/10.0)));
                    guessedCorrectly = true;
                } else if ((!isHeads) && bet.equals("Heads")) {
                    display_flipState = "Guess: Heads Result: Tails Sorry! You lost $" + amountOfBet;
                    guessedCorrectly = false;
                }
                count++;
                
                if (count > 250) {
                  if (guessedCorrectly) {
                    money += (amountOfBet + (amountOfBet*(luckyCharm/10.0)));
                  } else {
                      money -= amountOfBet;
                  }
                  
                  // If you run out of money then don't continue
                  if (money <= 0) {
                      money = 0;
                      flipState = flipRoundState.BROKE;
                  } else {
                      flipState = flipRoundState.READY;
                      count = 0;
                  }

                }
                break;

            case ERROR:
                display_flipState = "you lost your penny .. you lose!";
            break;

            default:
            break;
        } 
         
        // update resources
         nifty.getCurrentScreen().findElementByName("money").getRenderer(TextRenderer.class).setText("Money: $" + money);
         nifty.getCurrentScreen().findElementByName("playerName").getRenderer(TextRenderer.class).setText(displayPlayerName + " (" +display_flipState+" )");
         nifty.getCurrentScreen().findElementByName("luckyCharm").getRenderer(TextRenderer.class).setText("LuckyCharm: " + luckyCharm);
         
        // Get the coins rotation of in relation to vector and convert to degrees
        // Essentially yRot will always be @180 if tails
        float yRot = coin.getWorldRotation().toAngleAxis(new Vector3f(1, 0, 0)) * FastMath.RAD_TO_DEG;
        isHeads = (yRot < 160 || yRot > 200);
        
        // update face in GUI
        //nifty.getCurrentScreen().findElementByName("face").getRenderer(TextRenderer.class).setText("Heads: " + isHeads + " : " + Math.round(yRot) );
       }
    }
}
