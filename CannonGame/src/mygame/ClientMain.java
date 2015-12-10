package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import static mygame.ServerMain.*;
import mygame.enums.CanSize;
import mygame.enums.GameState;
import mygame.gameobjects.Cannon;
import mygame.gameobjects.Cannonball;
import mygame.gameobjects.Can;
import mygame.helpers.NetUtils;
import mygame.helpers.NetUtils.Activate;
import mygame.helpers.NetUtils.Alive;
import mygame.helpers.NetUtils.Award;
import static mygame.helpers.NetUtils.CLIENT_UPDATE_RATE;
import mygame.helpers.NetUtils.Change;
import mygame.helpers.NetUtils.Congratulate;
import mygame.helpers.NetUtils.Connect;
import mygame.helpers.NetUtils.Disconnect;
import mygame.helpers.NetUtils.Disconnected;
import mygame.helpers.NetUtils.FireInput;
import mygame.helpers.NetUtils.Inactivate;
import mygame.helpers.NetUtils.LaserInput;
import mygame.helpers.NetUtils.LaserToggled;
import mygame.helpers.NetUtils.Move;
import mygame.helpers.NetUtils.NewClientAccepted;
import mygame.helpers.NetUtils.Prepare;
import mygame.helpers.NetUtils.Reject;
import mygame.helpers.NetUtils.Rotate;
import mygame.helpers.NetUtils.RotateInput;
import mygame.helpers.NetUtils.Start;
import mygame.helpers.StopWatch;

/**
 * Lab 3. The client application
 *
 * @author Bjorn van der Laan (bjovan-5)
 */
public class ClientMain extends SimpleApplication {
    // Integer constants

    public static final int PLAYINGFIELD_RESOLUTION = 100;	//The resolution of the playing field
    public static final int CAN_RESOLUTION = 100;	 //The resolution of each can (no matter what kind)
    public static final int CANNONBALL_NUM = 5;	 //Maximal number of cannonballs that can be active on the playing field at the same time
    public static final int CANNONBALL_RESOLUTION = 100;	 //The resolution of a cannonball
    public static final int LARGECAN_NUM = 10;	 //Number of large cans
    public static final int LARGECAN_VALUE = 10;	 //Points awarded by hitting a large can
    public static final int MEDIUMCAN_NUM = 6;	 //Number of medium-sized cans
    public static final int MEDIUMCAN_VALUE = 20;	 //Points awarded by hitting a medium-sized can
    public static final int SMALLCAN_NUM = 3;	 //Number of small cans
    public static final int SMALLCAN_VALUE = 40;	 //Points awarded by hitting a small can
    public static final int CANS_NUM = LARGECAN_NUM + MEDIUMCAN_NUM + SMALLCAN_NUM;	 //Total number of cans on the playing field at all time
    // Float constants
    public static final float DEAD_MARGIN = 1f;// The distance outside the rim of the playing field beyond which a cannonball is no longer active and part of the game
    public static final float START_TIME = 30f;// Amount of time a game lasts
    public static final float PLAYINGFIELD_RADIUS = 200f;// The radius of the playing field
    public static final float SMALLCAN_RADIUS = 3f;// The radius of a small can
    public static final float SMALLCAN_HEIGHT = 10f;// The height of a small can
    public static final float MEDIUMCAN_RADIUS = 4f;// The radius of a medium-sized can
    public static final float MEDIUMCAN_HEIGHT = 15;// The height of a medium-sized can
    public static final float LARGECAN_RADIUS = 5f;// The radius of a large can
    public static final float LARGECAN_HEIGHT = 20f;//	 The height of a large can
    public static final float MAXIMAL_CAN_RADIUS = LARGECAN_RADIUS;// The largest of all radius of cans
    public static final float CANNON_SAFETYDISTANCE = 20f;//	 Extra distance to cannon when placing cans
    public static final float SAFETY_MARGIN = 2f * MAXIMAL_CAN_RADIUS + CANNON_SAFETYDISTANCE;//	 Maximal inward distance a can can be placed at from the muzzle of the cannon
    public static final float CANNONBALL_RADIUS = 1.1f * MAXIMAL_CAN_RADIUS;// The radius of a cannonball
    public static final float CANNONBALL_SPEED = 80f; //The (constant) speed of a cannonball
    public static final float CANNON_BARREL_RADIUS = CANNONBALL_RADIUS;//	 The radius of the barrel of the cannon
    public static final float CANNON_BARREL_LENGTH = MAXIMAL_CAN_RADIUS + CANNON_SAFETYDISTANCE;	// The length of the barrel of the cannon
    public static final float CANNON_SUPPORT_RADIUS = 3f * CANNON_BARREL_RADIUS;	 // The radius of the support plate of the cannon
    public static final float CANNON_SUPPORT_HEIGHT = 3f * CANNON_BARREL_RADIUS;   //The height of the support plate of the cannon
    public static final float CANNON_BASE_RADIUS = 2.4f * CANNON_BARREL_RADIUS;	  //The radius of the base of the cannon
    public static final float CANNON_BASE_HEIGHT = 2.1f * CANNON_BARREL_RADIUS;	  //The height of the base of the cannon
    public static final float CANNON_ROTATION_SPEED = 20f;
    public static final float CANNON_LASER_RANGE = 50f; //the length of the laser
    //
    private static String[] nicknames = new String[MAX_PLAYERS];
    private static float time;
    private static Node game, board, balls, targets, cannons;
    private static ClientMain app;
    private static boolean[] lasers = new boolean[MAX_PLAYERS];
    private static Can[] cans = new Can[CANS_NUM];
    private static Cannon[] cannons_ar = new Cannon[MAX_PLAYERS];
    private static Cannonball[] cannonballs = new Cannonball[MAX_PLAYERS * CANNONBALL_NUM];
    private static int[] scores = new int[MAX_PLAYERS];
    private static GameState state;
    private static Timer sendTimer;

    public static void main(String[] args) {
        app = new ClientMain();
        app.start();
    }
    //Networking
    private Client client = null;
    private ConcurrentLinkedQueue<AbstractMessage> messageQueue;
    private ClientStateListener clientStateListener = new ClientStateListener() {
        public void clientConnected(Client c) {
            //Set timer for sending update messages.
            sendTimer = new Timer();
            sendTimer.schedule(new SenderTask(), 0, CLIENT_UPDATE_RATE);

            System.out.println("Client: connected");
        }

        public void clientDisconnected(Client c, ClientStateListener.DisconnectInfo info) {
            client.send(new Disconnect());
            sendTimer.cancel();
            System.out.println("Client: disconnected");
        }
    };
    private MessageListener messageListener = new MessageListener<Client>() {
        public void messageReceived(final Client source, final Message m) {
            if (m instanceof NewClientAccepted) {
                final NewClientAccepted msg = (NewClientAccepted) m;
                if (nicknames[msg.id] != null && cannons_ar[msg.id] == null) {
                    try {
                        app.enqueue(new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                createCannon(msg.orientation, msg.id); //position is no longer needed?
                                nicknames[msg.id] = msg.n;
                                System.out.println("Client: connected with nickname " + msg.n);
                                return null;
                            }
                        });
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
            } else if (m instanceof Reject) {
                Reject msg = (Reject) m;
                System.out.println("Client: unable to connect. " + msg.reason);
                createNicknameWindow(msg.reason);
            } else if (m instanceof Disconnected) {
                Disconnected msg = (Disconnected) m;
                if (msg.id == client.getId()) {
                    client.close();
                    changeGameState(GameState.Stopped);
                    ((BitmapText) guiNode.getChild(1)).setText("Disconnected by server");
                }
                System.out.println("Client: player " + msg.id + " left the game");
            } else if (m instanceof Prepare) {
                final Prepare msg = (Prepare) m;
                scores[client.getId()] = 0;
                StopWatch.stopTime();
                try {
                    app.enqueue(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            nicknames = msg.na;
                            createCans(msg.pa);
                            createCannons(msg.na, msg.ra);
                            return null;
                        }
                    });
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else if (m instanceof Start) {
                StopWatch.startTime();
                allCannonsNormalLook();
                changeGameState(GameState.Running);
            } else if (m instanceof Activate) {
                final Activate msg = (Activate) m;
                try {
                    app.enqueue(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            createCannonBall(msg.i, msg.c, msg.d);
                            return null;
                        }
                    });
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else if (m instanceof Inactivate) {
                final Inactivate msg = (Inactivate) m;
                try {
                    app.enqueue(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            removeCannonBall(msg.c);
                            return null;
                        }
                    });
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else if (m instanceof Move) {
                final Move msg = (Move) m;
                try {
                    app.enqueue(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            cans[msg.c].setLocalTranslation(msg.p);
                            return null;
                        }
                    });
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else if (m instanceof Change) {
                final Change msg = (Change) m;
                try {
                    app.enqueue(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            if (cannonballs[msg.c] != null) {
                                cannonballs[msg.c].dir = msg.d;
                                cannonballs[msg.c].pos = msg.p;
                            }
                            return null;
                        }
                    });
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else if (m instanceof Award) {
                final Award msg = (Award) m;
                scores[msg.i] = msg.s;
                if (cannons_ar[msg.i] != null) {
                    cannons_ar[msg.i].setScore(msg.s);
                }
            } else if (m instanceof Rotate) {
                final Rotate msg = (Rotate) m;
                if (msg.i != client.getId()) {
                    try {
                        app.enqueue(new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                if (msg.b) {
                                    ((Cannon) cannons.getChild("Cannon" + msg.i)).rotate(0, 0, msg.a);
                                } else {
                                    ((Cannon) cannons.getChild("Cannon" + msg.i)).rotate(0, 0, -msg.a);
                                }
                                return null;
                            }
                        });
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
            } else if (m instanceof LaserToggled) {
                final LaserToggled msg = (LaserToggled) m;
                lasers[msg.i] = msg.b;
                try {
                    app.enqueue(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            ((Cannon) cannons.getChild("Cannon" + msg.i)).activateLaser(lasers[msg.i]);
                            return null;
                        }
                    });
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else if (m instanceof Congratulate) {
                changeGameState(GameState.Stopped);
                Congratulate msg = (Congratulate) m;
                int num_of_winners = msg.n;

                for (int i = 0; i < num_of_winners - 1; i++) {
                    cannons_ar[msg.w[i]].winnerLook(true);
                    if (msg.w[i] == client.getId()) {
                        System.out.println("You won!");
                    }
                }

                ((BitmapText) guiNode.getChild(1)).setText("TIME'S UP!");
            }
        }
    };
    //
    private ActionListener cannonActionListener = new ActionListener() {
        public void onAction(String name, boolean isPressed, float tpf) {

            if (name.equals("Shoot") && isPressed) {
                if (balls.getChildren().size() < CANNONBALL_NUM && state == GameState.Running) {
                    sendInputMessage(new FireInput());
                }
            } else if (name.equals("Laser") && !isPressed) {
                sendInputMessage(new LaserInput());
                Cannon player_cannon = cannons_ar[client.getId()];
                if (player_cannon.barrel.getChild("Laser") != null) {
                    player_cannon.activateLaser(false);
                } else {
                    player_cannon.activateLaser(true);
                }
            }
        }
    };
    private AnalogListener cannonAnalogListener = new AnalogListener() {
        public void onAnalog(String name, float value, float tpf) {
            Cannon player_cannon = cannons_ar[client.getId()];
            if (name.equals("Left")) {
                sendInputMessage(new RotateInput(false, tpf));
                player_cannon.cannon.rotate(0, tpf, 0);
            } else if (name.equals("Right")) {
                sendInputMessage(new RotateInput(true, tpf));
                player_cannon.cannon.rotate(0, -tpf, 0);
            }
        }
    };

    @Override
    public void destroy() {
        //client.close();
        super.destroy();
    }

    @Override
    public void simpleInitApp() {
        // Initialize HUD
        initHUD();

        flyCam.setMoveSpeed(80);
        state = GameState.Stopped;
        this.setPauseOnLostFocus(false);
        inputManager.setCursorVisible(true);
        flyCam.setDragToRotate(true);

        // Create a node to contain the whole game
        game = new Node("Game");

        // Create light source
        AmbientLight am = new AmbientLight();
        am.setColor(ColorRGBA.White);
        rootNode.addLight(am);

        // Rotate game into the XZ plane
        Quaternion x90 = new Quaternion();
        x90.fromAngleAxis(FastMath.PI / 2, new Vector3f(1, 0, 0));
        game.rotate(x90);

        // Create board
        createBoard();
        game.attachChild(board);

        // Init targets node
        targets = new Node("Targets");
        game.attachChild(targets);

        // Init cannonballs node
        balls = new Node("Balls");
        game.attachChild(balls);

        // Init cannons node
        cannons = new Node("Cannons");
        game.attachChild(cannons);

        // Initialize user controls
        initKeys();

        // Attach game to rootNode
        rootNode.attachChild(game);

        // Init the networking
        initNetworking();

        //Create window where user can input name
        createNicknameWindow("");
    }

    /**
     * Creates a screen to choose a nickname.
     *
     * @param err_msg
     */
    private void createNicknameWindow(String err_msg) {
        JFrame frame = new JFrame();
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        final JTextField text = new JTextField();
        text.setColumns(MAX_CHARS);
        JButton button = new JButton("Connect");
        final JLabel error_label = new JLabel(err_msg);

        panel.add(text);
        panel.add(button);
        panel.add(error_label);
        frame.add(panel);

        button.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.send(new Connect(text.getText()));
                ((JButton) e.getSource()).getTopLevelAncestor().setVisible(false);
            }
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getRootPane().setDefaultButton(button);
    }

    private void initNetworking() {
        NetUtils.initMessages();
        try {
            client = Network.connectToServer(NetUtils.HOST, NetUtils.PORT);
            client.start();
            client.addClientStateListener(clientStateListener);
            client.addMessageListener(messageListener);
        } catch (IOException ex) {
            System.out.println("Error in setting up client. " + ex.getMessage());
        }
        messageQueue = new ConcurrentLinkedQueue<AbstractMessage>();
    }

    /**
     * Changes the state and handles the transitions.
     *
     * @param new_state the new state
     */
    private void changeGameState(GameState new_state) {
        //if not the same
        if (!(state == new_state)) {

            //Change the text on HUD
            if (new_state == GameState.Stopped) {
                StopWatch.stopTime();
                ((BitmapText) guiNode.getChild(1)).setText("WAITING");
            } else if (new_state == GameState.Running) {
                StopWatch.startTime();
                ((BitmapText) guiNode.getChild(1)).setText(" ");
            }

            //Transitions
            if (state == GameState.Stopped && new_state == GameState.Running) {
                scores[client.getId()] = 0;
            } else if (state == GameState.Running && new_state == GameState.Stopped) {
                scores[client.getId()] = 0;
                ((BitmapText) guiNode.getChild(0)).setText("30:00" + "\n" + scores[client.getId()]);
            }

            //Update state
            state = new_state;
            System.out.println("My state is: " + state);
        }
    }

    /**
     * Creates the board.
     */
    private void createBoard() {
        Cylinder board_mesh = new Cylinder(PLAYINGFIELD_RESOLUTION, PLAYINGFIELD_RESOLUTION, PLAYINGFIELD_RADIUS, 1, true);
        Geometry board_geom = new Geometry("Board_geom", board_mesh);
        Material board_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        board_mat.setTexture("ColorMap", assetManager.loadTexture("Textures/Sky/Lagoon/lagoon_down.jpg"));
        board_geom.setMaterial(board_mat);
        board = new Node("Board");

        board.attachChild(board_geom);
    }

    /**
     * Creates cans based on positions. Used when Prepare message is received
     *
     * @param position position of cans
     */
    private void createCans(Vector3f[] position) {
        //delete previous cans
        targets.detachAllChildren();
        cans = new Can[CANS_NUM];

        for (int i = 0; i < CANS_NUM; i++) {
            if (i < SMALLCAN_NUM) {
                createCan(i, position[i].getX(), position[i].getY(), CanSize.Small);
            } else if (i < MEDIUMCAN_NUM) {
                createCan(i, position[i].getX(), position[i].getY(), CanSize.Medium);
            } else {
                createCan(i, position[i].getX(), position[i].getY(), CanSize.Large);
            }
        }
    }

    /**
     * Creates a new target.
     *
     * @param size size of the target
     * @param left_off left offset from middle
     * @param up_off up offset from middle
     */
    private void createCan(int id, float left_off, float up_off, CanSize size) {
        // Create new Can
        Can tar_geom = new Can(size);
        tar_geom.setId(id);

        // Define material
        Material tar_mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        tar_mat.setBoolean("UseMaterialColors", true);
        tar_mat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"));

        ColorRGBA can_color = ColorRGBA.Yellow;
        if (size == CanSize.Small) {
            can_color = ColorRGBA.Red;

        } else if (size == CanSize.Medium) {
            can_color = ColorRGBA.Orange;
        }
        tar_mat.setColor("Diffuse", can_color);
        tar_mat.setColor("Ambient", can_color);
        tar_geom.setMaterial(tar_mat);

        tar_geom.move(left_off, up_off, -0.5f * tar_geom.getHeight());

        cans[id] = tar_geom;

        targets.attachChild(tar_geom);
    }

    /**
     * Creates and removes cannons. Used when receiving Prepare messages.
     *
     * @param ca array with player ids.
     */
    private void createCannons(String[] na, float[] ra) {
        for (int i = 0; i < na.length; i++) {
            if (na[i] != null && cannons_ar[i] == null) {
                createCannon(ServerMain.getCannonPositionById(i).negate(), i);
            } else if (na[i] == null && cannons_ar[i] != null) {
                removeCannon(i);
            }
        }
    }

    /**
     * Creates the cannon. memo: x-plus is naar links, y-plus is naar boven (is
     * dit nog waar?)
     */
    private void createCannon(Vector3f orientation, int player_id) {
        //if player does not exist or cannon already exists, do nothing
        if (nicknames[player_id] == null || cannons_ar[player_id] != null) {
            return;
        }

        Cannon cannon = new Cannon(player_id, nicknames[player_id], this);
        cannons_ar[player_id] = cannon;

        if (player_id == client.getId()) {
            cannon.hideHoverText();
        }

        Vector3f position = ServerMain.getCannonPositionById(player_id);
        cannon.move(position);
        cannons.attachChild(cannon);

        Vector3f muzzle = cannons_ar[player_id].muzzle_tip.getWorldTranslation();
        Vector3f barrel = cannons_ar[player_id].barrel.getWorldTranslation();
        Vector3f current = muzzle.subtract(barrel).normalize();
        Vector2f current2d = new Vector2f(current.getX(), current.getZ());
        Vector2f dir = new Vector2f(orientation.getX(), orientation.getY()).normalize();
        double angle = FastMath.acos(current2d.dot(dir));

        if (player_id < 5 || player_id > 15) {
            cannon.rotate(0, 0, (float) angle);
        } else {
            cannon.rotate(0, 0, (float) -angle);
        }


        //position cam
        if (player_id == client.getId()) {

            cam.setLocation(new Vector3f(position.getX() * 2, 100, position.getY() * 2));
            cam.lookAt(cannon.getWorldTranslation(), new Vector3f());
        }



        System.out.println("Cannon created at " + position);
    }

    /**
     * Removes the cannon.
     *
     * @param id player id
     */
    private void removeCannon(int id) {
        cannons.getChild("Cannon" + id).removeFromParent();
        cannons_ar[id] = null;

        System.out.println("Cannon removed with id = " + id);
    }

    private void allCannonsNormalLook() {
        for (Cannon c : cannons_ar) {
            if (c != null) {
                c.winnerLook(false);
            }
        }
    }

    /**
     * Creates cannonball.
     */
    private void createCannonBall(int p_id, int c_id, Vector3f dir) {
        Cannonball ball = new Cannonball(p_id, c_id, ServerMain.getCannonPositionById(p_id), dir, this);

        //Vector3f muzzle = cannons_ar[p_id].muzzle_tip.getWorldTranslation();
        ball.setLocalTranslation(ServerMain.getCannonPositionById(p_id));
        ball.move(new Vector3f(0, 0, -CANNONBALL_RADIUS));
        cannonballs[c_id] = ball;
        balls.attachChild(ball);
    }

    /**
     * Removes cannonball.
     */
    private void removeCannonBall(int id) {
        //remove from scenegraph
        for (Spatial ball_spat : balls.getChildren()) {
            Cannonball ball = (Cannonball) ball_spat;
            if (ball.c_id == id) {
                ball.removeFromParent();
                break;
            }
        }
        //remove from datastructure
        cannonballs[id] = null;
    }

    /**
     * Initializes keybindings.
     */
    private void initKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Laser", new KeyTrigger(KeyInput.KEY_L));
        inputManager.addMapping("Shoot", new KeyTrigger(KeyInput.KEY_SPACE), new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("Start", new KeyTrigger(KeyInput.KEY_U));


        inputManager.addListener(cannonAnalogListener, "Left", "Right");
        inputManager.addListener(cannonActionListener, "Laser", "Shoot", "Start");
    }

    /**
     * Initializes HUD.
     */
    private void initHUD() {
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText text = new BitmapText(guiFont, false);
        text.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        text.setText("30:00" + "\n" + "0");
        text.setLocalTranslation(settings.getWidth() * 0.1f, settings.getHeight(), 0);
        guiNode.attachChild(text);

        BitmapText state_text = new BitmapText(guiFont, false);
        state_text.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        state_text.setText("Waiting..");
        state_text.setLocalTranslation(settings.getWidth() * 0.5f - state_text.getLineWidth() / 2, settings.getHeight() * 0.5f + state_text.getLineHeight(), 0);
        guiNode.attachChild(state_text);
    }

    private void sendInputMessage(AbstractMessage msg) {
        messageQueue.add(msg);
    }

    @Override
    public void simpleUpdate(float tpf) {
        if (state == GameState.Running) {
            // Update time
            time = StopWatch.getTimeLeft();
            ((BitmapText) guiNode.getChild(0)).setText(StopWatch.timeToString(time) + "\n" + scores[client.getId()]);

            for (Spatial ball_spat : balls.getChildren()) {
                Cannonball ball = (Cannonball) ball_spat;

                ball.rotate(2 * tpf, 0, 0);
                ball.pos = ball.pos.add(ball.dir.mult(tpf * CANNONBALL_SPEED));
                ball.move(ball.dir.mult(tpf * CANNONBALL_SPEED));
            }
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    private class SenderTask extends TimerTask {

        public void run() {
            AbstractMessage message = messageQueue.peek();
            if (message == null) {
                client.send(new Alive());
                //System.out.println("Alive");
            }

            while (message != null) {
                //Send the message
                client.send(messageQueue.poll());
                //Look at next message
                message = messageQueue.peek();
            }
        }
    }
}
