/*
 * Filename:    CoinFlip.java
 * Author:      Eduardo R. Rivas, Bryan J. VerHoven, KC
 * Class:       CMSC 325
 * Date:        5 Nov 2013
 * Assignment:  Project 1
 */

package cmsc325.project1;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
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
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.system.AppSettings;
import com.jme3.light.SpotLight;
import com.jme3.scene.Node;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.render.TextRenderer;

import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;


public class CoinFlip3D extends SimpleApplication {
    
    public static boolean isDebug = false;
    public boolean colorCoinState = false;
    private static float colorMult = 8f;
    
    Texture west;
    Texture east;
    Texture north;
    Texture south;
    Texture up;
    Texture down;
    Spatial sky;

    
    //Declare variables
    private BulletAppState bulletAppState;
    Material tableMaterial;
    public String imageName = "table.jpg";
    private RigidBodyControl coinPhysics;
    private RigidBodyControl tablePhysics;
    Spatial coin;
    Geometry tableGeometry;
    boolean guessedCorrectly = false;
    public boolean isHeads;
    public int amountOfBet;
    public double luckyCharm = 0.0;
    public String bet = "None";
    public boolean resetCoin = false;
    
    // Audio objects declarations
    private AudioNode natureAudio;
    private AudioNode coinFlipAudio;
    
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
    public SpotLight light;
    MyStartScreen screenControl;
    private RigidBodyControl landscape;
    private CharacterControl playerk;
    private Vector3f walkDirection = new Vector3f();
    private boolean pleft = false, pright = false, pup = false, pdown = false;
    
      private Vector3f camDir = new Vector3f();
  private Vector3f camLeft = new Vector3f();
    
    
    public static void main(String[] args){
        CoinFlip3D coinFlip = new CoinFlip3D();
        
        // Setup the launch screen
        AppSettings flip3d = new AppSettings(true);
        flip3d.setResolution(1360,768);
        flip3d.setSettingsDialogImage("Interface/Images/splash.png");
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
        screenControl = (MyStartScreen) nifty.getScreen("start").getScreenController();
        stateManager.attach((AppState) screenControl);
        guiViewPort.addProcessor(display);       
    }
    
    public void LoadMainGame() {
        flyCam.setEnabled(true);
        flyCam.setMoveSpeed(10f);
        //flyCam.setDragToRotate(true);
       // Setup Physics 
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        MyStartScreen screenControl3 = (MyStartScreen) nifty.getScreen("hud").getScreenController();
        stateManager.attach((AppState) screenControl3);
        
        MyStartScreen screenControl4 = (MyStartScreen) nifty.getScreen("shop").getScreenController();
        stateManager.attach( (AppState) screenControl4);
        
        // configure the camera to look at scene
        cam.setLocation(new Vector3f(0, 4.0f, 6.0f));
        cam.lookAt(new Vector3f(0.0f, 1.0f, 0.0f), Vector3f.UNIT_Y);
        
        //inputManager.setCursorVisible(false);
        // add InputManager action: spacebar flips the coin
        inputManager.addMapping("Flip the Coin", new KeyTrigger(KeyInput.KEY_SPACE)); // also now the jump
        inputManager.addListener(actionListener, "Flip the Coin");
        inputManager.deleteMapping(INPUT_MAPPING_EXIT);
        inputManager.addMapping("Escape", new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener(actionListener, "Escape");
        
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        
        inputManager.addListener(actionListener, "Left");
        inputManager.addListener(actionListener, "Right");
        inputManager.addListener(actionListener, "Up");
        inputManager.addListener(actionListener, "Down");

        //initialize the coin flip state 
        flipState = flipRoundState.NULL;
        
        Spatial gameLevel = assetManager.loadModel("Scenes/town.j3o");
        
        // Make the terrain rigid    
        CollisionShape sceneShape = CollisionShapeFactory.createMeshShape((Node) gameLevel);
        landscape = new RigidBodyControl(sceneShape, 0);
        gameLevel.addControl(landscape);
        gameLevel.setLocalTranslation(0, -5.2f, -10f);

        // Initialize the kool player - or playerk 
         CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
        playerk = new CharacterControl(capsuleShape, 0.05f);
        playerk.setJumpSpeed(20);
        playerk.setFallSpeed(30);
        playerk.setGravity(30);
        playerk.setPhysicsLocation(new Vector3f(5, 10, 5 ));

        rootNode.attachChild(gameLevel);
        bulletAppState.getPhysicsSpace().add(landscape);
        bulletAppState.getPhysicsSpace().add(playerk);
        
        //gameLevel.setLocalScale(2);
        rootNode.attachChild(gameLevel);
        
        // initialize materials, table
        initCoin();
        initTableMaterial();      
        initLight();
        initTable();
        initAudio();
        initSky();
    }


    //action listener
    private ActionListener actionListener = new ActionListener() {
      public void onAction(String name, boolean keyPressed, float tpf) {
        if (name.equals("Escape")) {
          isRunning = false;
          nifty.gotoScreen("pause");
          MyStartScreen screenControl2 = (MyStartScreen) nifty.getScreen("pause").getScreenController();
          stateManager.attach((AppState) screenControl2);
        }
        if (name.equals("Flip the Coin")) {
          
          if (playerk.getPhysicsLocation().distance(coin.getLocalTranslation()) < 20  ) {
             flipCoin();
          } else {
              playerk.jump();
          }
            
        }
        
        if (name.equals("Left")) {
      pleft = keyPressed;
    } else if (name.equals("Right")) {
      pright= keyPressed;
    } else if (name.equals("Up")) {
      pup = keyPressed;
    } else if (name.equals("Down")) {
      pdown = keyPressed;
    } else if (name.equals("Jump")) {
      if (keyPressed) { playerk.jump(); }
    }
        
      }
    };
    
    // initialize the materials
    public void initTableMaterial(){
        tableMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        tableMaterial.setTexture("ColorMap", assetManager.loadTexture("Textures/table/" + imageName));
    }
    
    // light
    public void initLight(){
        // sun 
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White.mult(.7f));
        sun.setDirection(new Vector3f(-.5f,-.5f,-.5f).normalizeLocal());
        rootNode.addLight(sun); 
  
        // Spotlight basic setup from jMonkey wiki
        // blew wayyy too much time on trying to get cam.getLocation()/cam.getDirection()
        // changed to regular Vector3f - grrr - ended up just widening the Outer angle to compensate
        light = new SpotLight();
        light.setSpotRange(8f);                           
        light.setSpotInnerAngle(10f * FastMath.DEG_TO_RAD); 
        light.setSpotOuterAngle(50f * FastMath.DEG_TO_RAD);         
        light.setPosition(cam.getLocation());               
        light.setDirection(cam.getDirection());            

        rootNode.addLight(light);
    }
    
    public void initSky(){
        west = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_west.jpg");
        east = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_east.jpg");
        north = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_north.jpg");
        south = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_south.jpg");
        up = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_up.jpg");
        down = assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_down.jpg");
        
        sky = SkyFactory.createSky(assetManager, west, east, north, south, up, down, Vector3f.UNIT_XYZ);
        rootNode.attachChild(sky);
    }
    
    // coin
    public void initCoin(){
        // detach coin from scene - only if player has changed!
        if (resetCoin) {
            rootNode.detachChild(coin);
        }
        // load the coin
        coin = assetManager.loadModel("Models/penny/penny.j3o");
        
        coin.setLocalTranslation(0, 5f, 0);
        
        // make the coin physical with a mass > 0.0f
        coinPhysics = new RigidBodyControl(2.0f);
        
        
        // add physical coin to physics space
        coin.addControl(coinPhysics);
        coinPhysics.setFriction(5f);
        coinPhysics.setDamping(.1f, .1f);
        
        // display coin
        rootNode.attachChild(coin);
        
        bulletAppState.getPhysicsSpace().add(coinPhysics);
        resetCoin = false;
    }
    
    // Floor
    public void initTable(){
        table = new Box(5.0f, 0.5f, 5.0f);
        table.scaleTextureCoordinates(new Vector2f(1, 1));
        
        tableGeometry = new Geometry("table", table);
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
            
            // play sound when the coin flips
            coinFlipAudio.playInstance();
        } 
    }
    
    public void initAudio(){
        // coint toss audio will be triggered when the user presses the spacebar to flip coin
        coinFlipAudio = new AudioNode(assetManager, "Sounds/Effects/coinToss.wav", false);
        coinFlipAudio.setPositional(false);
        coinFlipAudio.setLooping(false);
        coinFlipAudio.setVolume(2);
        rootNode.attachChild(coinFlipAudio);
        
        // outdoor ambient sound that plays in a loop
        natureAudio = new AudioNode(assetManager, "Sounds/Environment/outdoors.ogg", false);
        natureAudio.setPositional(false);
        natureAudio.setLooping(true);
        natureAudio.setVolume(3);
        rootNode.attachChild(natureAudio);      
    }
    
    // function to stop the ambient sound
    public void stopAmbientAudio(){
        natureAudio.stop();
    }
    

    // Get coin average movement over a number of samples
    // this function could effect performance if program grows
    // Solution might be to allow a non-blocking calculation
    // for this... keep it on the radar
    public Boolean getAverageMovement(int samples){
    
    float tolerance = .1f;   
    int i = 0;
    float movement = 0;
    
    while (i < samples) {    
        movement  = coinPhysics.getAngularVelocity().x *100 
                    + coinPhysics.getAngularVelocity().y *100
                    + coinPhysics.getAngularVelocity().z *100;
        movement += movement;
        i++;
    }
    
    // 0 is perfectly 
    // I added tolerance because.. well. sucks without it.
    return ( movement/samples< 0+tolerance && movement/samples> 0-tolerance) ? true : false;       
    }
    
    // update loop
    @Override
    public void simpleUpdate(float tpf) {
       if (isRunning) {
        
        // play the ambient sound continuously
        natureAudio.play();
        inputManager.setCursorVisible(false);   
        
        //cam follows
        camDir.set(cam.getDirection()).multLocal(0.6f);
        camLeft.set(cam.getLeft()).multLocal(0.4f);
        walkDirection.set(0, 0, 0);
        
        // walk when not flipping
        if (pleft) {
            walkDirection.addLocal(camLeft);
        }
        if (pright) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (pup) {
            walkDirection.addLocal(camDir);
        }
        if (pdown) {
            walkDirection.addLocal(camDir.negate());
        }
        playerk.setWalkDirection(walkDirection);
        cam.setLocation(playerk.getPhysicsLocation());
    
        
        // take care of the coin
        switch (flipState) {
            
            case BROKE:
                display_flipState = "You are out of money! ";
                if (money > 0) flipState = flipRoundState.READY;
            break;
                
            case NULL:
                display_flipState = "here is a coin..";
                flipState = flipRoundState.READY; 
            break;

            case READY:
                display_flipState = "Press space to play";
                if (colorCoinState) {
                    light.setColor(ColorRGBA.Green.mult(colorMult));
                }
            break;

            case FLIPSTART:
               
                if (colorCoinState) {
                    light.setColor(ColorRGBA.Yellow.mult(colorMult));
                }
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
               if (colorCoinState) {
                   light.setColor(ColorRGBA.Cyan.mult(colorMult));
               } 
               Boolean isHeadsState = isHeads;
               display_flipState = "watching ..." ;
               
               // dont move on until the side showing is stable
               if (isHeads == isHeadsState) {count ++; } else { count =0;} 
                
               if (count > 550 && getAverageMovement(10)) {
                   coinPhysics.clearForces();
                if (colorCoinState) {
                    light.setColor(ColorRGBA.Red.mult(colorMult));
                }   
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
                      if (colorCoinState) light.setColor(ColorRGBA.Black); 
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
         tableGeometry.setMaterial(tableMaterial);
         
        // Get the coins rotation of in relation to vector and convert to degrees
        // Essentially yRot will always be @180 if tails
        float yRot = coin.getWorldRotation().toAngleAxis(new Vector3f(1, 0, 0)) * FastMath.RAD_TO_DEG;
        isHeads = (yRot < 160 || yRot > 200);
        
       }
       else
       {
         // set cursor on screen when game is diabled  
         inputManager.setCursorVisible(true);}
    }
}
