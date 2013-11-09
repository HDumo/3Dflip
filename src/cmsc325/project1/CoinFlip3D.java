/*
 * Filename:    CoinFlip.java
 * Author:      Eduardo R. Rivas, Bryan J. VerHoven
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
import com.jme3.niftygui.NiftyJmeDisplay;
import de.lessvoid.nifty.Nifty;
//import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;

public class CoinFlip3D extends SimpleApplication {
    
    //Declare variables
    private BulletAppState bulletAppState;
    Material tableMaterial;
    private RigidBodyControl coinPhysics;
    private RigidBodyControl tablePhysics;
    Spatial coin;
    private static Box table;
    Nifty nifty;
    public int score = 0;
    public String displayPlayerName = "Player 1";
    public boolean isRunning = false;
    
    public static void main(String[] args){
        CoinFlip3D coinFlip = new CoinFlip3D();
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
        
        // configure the camera to look at scene
        cam.setLocation(new Vector3f(0, 4.0f, 6.0f));
        cam.lookAt(new Vector3f(0.0f, 1.0f, 0.0f), Vector3f.UNIT_Y);
        
        // add InputManager action: spacebar flips the coin
        inputManager.addMapping("Flip the Coin", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(actionListener, "Flip the Coin");
        inputManager.deleteMapping(INPUT_MAPPING_EXIT);
        inputManager.addMapping("Escape", new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener(actionListener, "Escape");
        
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
        
        // display coin
        rootNode.attachChild(coin);
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
        // make the coin physical with a mass > 0.0f
        coinPhysics = new RigidBodyControl(2.0f);
        
        // add physical coin to physics space
        coin.addControl(coinPhysics);
        bulletAppState.getPhysicsSpace().add(coinPhysics);
        coinPhysics.applyTorque(new Vector3f(10.0f,  0.0f, 0.0f));
        coinPhysics.setLinearVelocity(Vector3f.UNIT_Y.mult(6));
        //if heads
        score += 10;
        //else
        score -= 5;
        // update Score in GUI
        nifty.getCurrentScreen().findElementByName("score").getRenderer(TextRenderer.class).setText("Score: " + score);
        nifty.getCurrentScreen().findElementByName("playerName").getRenderer(TextRenderer.class).setText(displayPlayerName);
        
    }
    
    // update loop
    @Override
    public void simpleUpdate(float tpf) {
       if (isRunning) {
         // update Score in GUI
         nifty.getCurrentScreen().findElementByName("score").getRenderer(TextRenderer.class).setText("Score: " + score);
         nifty.getCurrentScreen().findElementByName("playerName").getRenderer(TextRenderer.class).setText(displayPlayerName);
       }
    }
   
}
