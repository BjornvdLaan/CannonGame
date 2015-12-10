package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.ConnectionListener;
import com.jme3.network.Filters;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.system.JmeContext;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import static mygame.ClientMain.*;
import mygame.enums.CanSize;
import mygame.enums.GameState;
import mygame.gameobjects.Cannonball;
import mygame.helpers.NetUtils;
import mygame.helpers.NetUtils.Activate;
import mygame.helpers.NetUtils.Alive;
import mygame.helpers.NetUtils.Award;
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
import static mygame.helpers.NetUtils.SERVER_UPDATE_RATE;
import mygame.helpers.NetUtils.Start;
import mygame.helpers.StopWatch;

/**
 * Lab 3.
 * The server application.
 * @author Bjorn van der Laan (bjovan-5)
 */
public class ServerMain extends SimpleApplication {

    public final static float TIMEOUT = 5.0f; //Minimum time between packets sent from a client to the server in order to keep the client connected.
    public final static int MAX_CHARS = 8; //Maximum number of characters allowed in a nickname.
    public final static int MAX_PLAYERS = 20; //Maximum number of players accepted to connect. 
    //
    private static Server server;
    protected static GameState server_state = GameState.Stopped;
    protected static ServerMain app;
    private static ConcurrentLinkedQueue<AbstractMessage> messageQueue;
    private static long[] lastActivities = new long[MAX_PLAYERS];
    //
    private static Vector3f[] cans = new Vector3f[CANS_NUM];
    private static Vector3f[] cannon_positions = loadCannonPositions();
    private static boolean[] players = new boolean[MAX_PLAYERS];
    private static String[] nicknames = new String[MAX_PLAYERS];
    private static boolean[] lasers = new boolean[MAX_PLAYERS];
    private static Cannonball[] cannonballs = new Cannonball[MAX_PLAYERS * CANNONBALL_NUM];
    private static float[] rotations = new float[MAX_PLAYERS];
    private static int[] scores = new int[MAX_PLAYERS];
    private static Random r = new Random();

    public static void main(String[] args) {
        app = new ServerMain();
        app.start(JmeContext.Type.Headless); // headless type for servers!
    }

    private static void running() {
        StopWatch.startTime();
        broadcastMessage(new Start());
    }

    private static void stopped() {
        StopWatch.stopTime();
    }

    protected static void changeState(GameState new_state) {
        server_state = new_state;
        GuiControl.setStateLabel(new_state);

        if (new_state == GameState.Running) {
            running();
        } else if (new_state == GameState.Stopped) {
            stopped();
        }
    }

    public static void broadcastMessage(AbstractMessage msg) {
        messageQueue.add(msg);
    }

    public static Vector3f getCannonPositionById(int i) {
        float r = ((2 * FastMath.PI) / MAX_PLAYERS) * i;
        int x = (int) (FastMath.cos(r) * PLAYINGFIELD_RADIUS);
        int y = (int) (FastMath.sin(r) * PLAYINGFIELD_RADIUS);
        return new Vector3f(x, y, 0);
    }

    private static Vector3f[] loadCannonPositions() {
        Vector3f[] cannon_pos = new Vector3f[MAX_PLAYERS];
        float rads = ((2 * FastMath.PI) / MAX_PLAYERS);
        for (int i = 0; i < MAX_PLAYERS; i++) {
            float r = rads * i;
            int x = (int) (FastMath.cos(r) * PLAYINGFIELD_RADIUS);
            int y = (int) (FastMath.sin(r) * PLAYINGFIELD_RADIUS);
            cannon_pos[i] = new Vector3f(x, y, 0);
        }
        return cannon_pos;
    }

    private static void clearPlayerData(int id) {
        players[id] = false;
        nicknames[id] = null;
        lasers[id] = false;
        rotations[id] = 0;
        scores[id] = 0;
    }

    /**
     *
     */
    public static Vector3f computeCanPosition() {
        // Generate left offset
        int left_range = (int) PLAYINGFIELD_RADIUS - (int) SAFETY_MARGIN;
        float left_off = (float) r.nextInt(2 * left_range) - left_range;

        // Compute maximal up offset
        int up_range = (int) Math.sqrt(Math.pow(PLAYINGFIELD_RADIUS, 2) - Math.pow(left_off, 2)) - (int) SAFETY_MARGIN;

        // Generate up offset
        float up_off;
        if (up_range == 0) {
            up_off = 0;
        } else {
            up_off = (float) r.nextInt(2 * up_range) - up_range;
            if (up_off < -1 * PLAYINGFIELD_RADIUS + SAFETY_MARGIN) {
                up_off = -1 * PLAYINGFIELD_RADIUS + SAFETY_MARGIN; //margin for cannon
            }
        }

        return new Vector3f(left_off, up_off, 0);
    }

    /**
     * Assigns a free cannon position to user.
     */
    public static void assignCannonPosition(HostedConnection conn) {
        if (!players[conn.getId()]) {
            players[conn.getId()] = true;
        }
    }

    /**
     * Removes a player from a cannon position.
     *
     * @param conn player to be removed
     */
    public static void removeCannonPosition(HostedConnection conn) {
        players[conn.getId()] = false;
    }
    //
    private float counter = 0;
    private ConnectionListener connectionListener = new ConnectionListener() {
        public void connectionAdded(Server server, HostedConnection conn) {
            scores[conn.getId()] = 0;
            System.out.println("a Client has been added");
        }

        public void connectionRemoved(Server server, HostedConnection conn) {
            System.out.println(nicknames[conn.getId()] + " has been removed");

            clearPlayerData(conn.getId());
            removeCannonPosition(conn);
        }
    };

    @Override
    public void destroy() {
        server.close();
        super.destroy();
    }

    private boolean uniqueNickname(String name) {
        for (String nick : nicknames) {
            if (name.equals(nick)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void simpleInitApp() {
        initCans();
        NetUtils.initMessages();
        loadCannonPositions();

        try {
            server = Network.createServer(NetUtils.PORT);
            server.start();
            server.addConnectionListener(connectionListener);
            server.addMessageListener(new MessageHandler());
            server_state = GameState.Stopped;

            new GuiControl();

            messageQueue = new ConcurrentLinkedQueue<AbstractMessage>();

            //Set timer for sending update messages.
            Timer sendTimer = new Timer();
            sendTimer.schedule(new BroadcastTask(), SERVER_UPDATE_RATE, SERVER_UPDATE_RATE);
        } catch (Exception ex) {
            Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean activeCannonballsExist() {
        boolean exist = false;
        for (Cannonball c : cannonballs) {
            if (c != null) {
                exist = true;
            }
        }
        return exist;
    }

    /**
     * Announces the winner of the game.
     */
    public void announceWinner() {
        new Thread() {
            @Override
            public void run() {
                while (activeCannonballsExist()) {
                    //waiting for all canonballs to be done.
                    //could be implemented better..
                }
                HostedConnection winner = null;
                int[] winners = new int[MAX_PLAYERS];
                int highest_score = Integer.MIN_VALUE;
                int winners_count = 0;

                //find out the highest score
                for (HostedConnection conn : server.getConnections()) {
                    if (conn != null) {
                        int score = scores[conn.getId()];
                        if (score > highest_score) {
                            highest_score = score;
                            winner = conn;
                        }
                    }
                }

                if (winner != null) {
                    winners[0] = winner.getId();
                    winners_count++;
                    System.out.println(winner.getId());
                }

                //find equals
                for (HostedConnection conn : server.getConnections()) {
                    //if same as first winner
                    if (conn != null && winner != null && conn.getId() != winner.getId()) {
                        continue;
                    } else if (conn != null) {
                        int score = scores[conn.getId()];
                        if (score == highest_score) {
                            winners[winners_count] = conn.getId();
                            winners_count++;
                        }
                    }
                }

                //announce the winner
                System.out.println("Server: Highest score = " + highest_score);
                for (int i = 0; i < winners_count - 1; i++) {
                    System.out.println("Winner: " + nicknames[winners[i]]);
                }

                broadcastMessage(new Congratulate(winners, winners_count));
            }
        }.start();

    }

    /**
     * Sets up or resets positions of the cans.
     */
    private void initCans() {
        for (int i = 0; i < CANS_NUM; i++) {
            cans[i] = computeCanPosition();
        }
    }

    private CanSize getCanSize(int c_id) {
        if (c_id < SMALLCAN_NUM) {
            return CanSize.Small;
        } else if (c_id < MEDIUMCAN_NUM) {
            return CanSize.Medium;
        } else {
            return CanSize.Large;
        }
    }

    private float getCanRadius(CanSize size) {
        if (size == CanSize.Small) {
            return SMALLCAN_RADIUS;
        } else if (size == CanSize.Medium) {
            return MEDIUMCAN_RADIUS;
        } else {
            return LARGECAN_RADIUS;
        }
    }

    private int getCanValue(CanSize size) {
        if (size == CanSize.Small) {
            return SMALLCAN_VALUE;
        } else if (size == CanSize.Medium) {
            return MEDIUMCAN_VALUE;
        } else {
            return LARGECAN_VALUE;
        }
    }

    /**
     * Computes new vectors based on two dimensional elastic collision.
     *
     * @param a
     * @param b
     */
    private Vector2f[] collisionMagic(Vector2f a, Vector2f b) {
        //unit normal and unit tangent
        Vector2f normal_unit = a.subtract(b).normalize();
        Vector2f tangent_unit = new Vector2f(-normal_unit.getY(), normal_unit.getX());

        float a_normal = normal_unit.dot(a);
        float a_tangent = tangent_unit.dot(a);
        float b_normal = normal_unit.dot(b);
        float b_tangent = tangent_unit.dot(b);

        a_normal = b_normal;
        b_normal = a_normal;

        Vector2f a_normal_vector = normal_unit.mult(a_normal);
        Vector2f a_tangent_vector = tangent_unit.mult(a_tangent);
        Vector2f b_normal_vector = normal_unit.mult(b_normal);
        Vector2f b_tangent_vector = tangent_unit.mult(b_tangent);

        Vector2f a_new = a_normal_vector.add(a_tangent_vector);
        Vector2f b_new = b_normal_vector.add(b_tangent_vector);

        Vector2f[] res = new Vector2f[2];
        res[0] = a_new;
        res[1] = b_new;
        return res;

    }

    @Override
    public void simpleUpdate(float tpf) {
        //Send out periodic Prepare while game is stopped. TODO: should be with Timer.
        if (server_state == GameState.Stopped) {
            if (counter > 2f) {
                initCans();
                broadcastMessage(new Prepare(cans, nicknames, rotations));
                counter = 0;
            } else {
                counter += tpf;
            }
        } else {
            counter = 0;
        }

        if (server_state == GameState.Running && StopWatch.isTimeUp()) {
            changeState(GameState.Stopped);
            announceWinner();
        }

        //Not sure if this is needed.
        boolean[] alreadyCollided = new boolean[MAX_PLAYERS * CANNONBALL_NUM];

        for (int i = 0; i < MAX_PLAYERS * CANNONBALL_NUM; i++) {
            if (cannonballs[i] != null) {
                Cannonball ball = cannonballs[i];

                float dist = (float) ball.pos.distance(new Vector3f(0, 0, 0));
                // If outside the board, remove the cannonball
                if (dist > PLAYINGFIELD_RADIUS + DEAD_MARGIN) {
                    cannonballs[i] = null;
                    broadcastMessage(new Inactivate(ball.p_id, ball.c_id));
                }

                //update position
                ball.pos = ball.pos.add(ball.dir.mult(tpf * CANNONBALL_SPEED));

                // Check for collisions with cans
                can_collisions:
                for (int j = 0; j < CANS_NUM; j++) {
                    if (cans[j] == null) {
                        continue;
                    }

                    CanSize can_size = getCanSize(j);
                    float can_radius = getCanRadius(can_size);
                    Vector3f can_pos = cans[j];
                    float distance = can_pos.distance(ball.pos);

                    //if hit, award player and move can.
                    if (distance < CANNONBALL_RADIUS + can_radius) {
                        int player_id = ball.p_id;

                        Vector3f new_pos = computeCanPosition();
                        cans[j] = new_pos;
                        broadcastMessage(new Move(j, new_pos));

                        int old_score = scores[player_id];
                        int new_score = old_score + getCanValue(can_size);
                        scores[player_id] = new_score;
                        broadcastMessage(new Award(player_id, new_score));

                        broadcastMessage(new Inactivate(cannonballs[i].p_id, cannonballs[i].c_id));
                        cannonballs[i] = null;

                        break can_collisions;
                    }


                }

                cannonball_collisions:
                for (int j = 0; j < MAX_PLAYERS * CANNONBALL_NUM; j++) {
                    if (cannonballs[j] == null || ball.equals(cannonballs[j]) || ball.dir.cross(cannonballs[j].dir).equals(new Vector3f(0, 0, 0))) {
                        continue;
                    }

                    //if collision..
                    if (ball.pos.distance(cannonballs[j].pos) <= 2 * CANNONBALL_RADIUS) {
                        Vector2f a = new Vector2f(ball.dir.getX(), ball.dir.getY());
                        Vector2f b = new Vector2f(cannonballs[j].dir.getX(), cannonballs[j].dir.getY());

                        Vector2f[] new_dirs = collisionMagic(a, b);

                        ball.dir = new Vector3f(new_dirs[0].getX(), new_dirs[0].getY(), 0);
                        cannonballs[j].dir = new Vector3f(new_dirs[1].getX(), new_dirs[1].getY(), 0);

                        broadcastMessage(new Change(ball.p_id, ball.c_id, ball.pos, ball.dir)); //why need player id?
                        broadcastMessage(new Change(cannonballs[j].p_id, cannonballs[j].c_id, cannonballs[j].pos, cannonballs[j].dir));

                        alreadyCollided[ball.c_id] = true;
                        alreadyCollided[j] = true;

                        break cannonball_collisions;
                    }
                }
            }
        }

    }

    private class MessageHandler implements MessageListener<HostedConnection> {

        public void messageReceived(HostedConnection source, Message m) {
            int id = source.getId();
            updateLastActivity(id);

            if (m instanceof Connect) {
                Connect msg = (Connect) m;
                String nickname = msg.n;
                if (!(server_state == GameState.Stopped)) {
                    server.broadcast(Filters.equalTo(source), new Reject("Game already running."));
                } else if (server.getConnections().size() >= MAX_PLAYERS || nicknames[MAX_PLAYERS - 1] != null) {
                    server.broadcast(Filters.equalTo(source), new Reject("Maximal number of clients have already connected."));
                } else if (nickname.length() < 1 || nickname.length() > MAX_CHARS) {
                    server.broadcast(Filters.equalTo(source), new Reject("Bad nickname."));
                } else if (!uniqueNickname(nickname)) {
                    server.broadcast(Filters.equalTo(source), new Reject("Nickname already in use."));
                } else {
                    nicknames[id] = nickname;
                    assignCannonPosition(source);
                    if (players[id]) {
                        Vector3f pos = cannon_positions[id];
                        Vector3f dir = pos.negate();
                        broadcastMessage(new NewClientAccepted(nickname, id, pos, dir));

                        System.out.println("Server: " + msg.n + " (id=" + id + ") joined the game");
                    } else {
                        server.broadcast(Filters.equalTo(source), new Reject("Rejected because of bug."));
                    }
                }

            } else if (m instanceof Disconnect) {
                server.getConnection(id).close("Disconnected on request.");
                broadcastMessage(new Disconnected(id));
                System.out.println("Server: player " + nicknames[id] + " has left the game");
            } else if (m instanceof Alive) {
                //Nothing to be processed
                //Note: this 'if' could be removed, but is here for the reference
            } else if (m instanceof LaserInput) {
                lasers[id] = !lasers[id];
                broadcastMessage(new LaserToggled(id, lasers[id]));
            } else if (m instanceof RotateInput) {
                RotateInput msg = (RotateInput) m;
                if (!msg.r) {
                    rotations[id] += msg.a;
                } else {
                    rotations[id] -= msg.a;
                }
                broadcastMessage(new Rotate(id, msg.r, msg.a));
            } else if (m instanceof FireInput && server_state == GameState.Running) {
                int c_id = -1;
                //if inactive cannonball is available
                for (int i = 0; i < CANNONBALL_NUM; i++) {
                    if (cannonballs[id + i * 5] == null) {
                        c_id = id + i * 5;
                        break;
                    }
                }
                if (c_id != -1) {
                    Vector3f pos = cannon_positions[id];

                    Quaternion rotQ = new Quaternion();
                    rotQ.fromAngleAxis(rotations[id], new Vector3f(0, 0, -1));
                    Vector3f dir = rotQ.mult(pos.negate().normalize());

                    cannonballs[c_id] = new Cannonball(id, c_id, pos, dir);
                    broadcastMessage(new Activate(id, c_id, pos, dir));
                }
            }
        }
    }

    private static synchronized void updateLastActivity(int id) {
        lastActivities[id] = System.currentTimeMillis();
    }

    private static synchronized long getLastActivity(int id) {
        return lastActivities[id];
    }

    private class BroadcastTask extends TimerTask {

        @Override
        public void run() {
            AbstractMessage message = messageQueue.peek();
            while (message != null) {
                //Send the message
                server.broadcast(messageQueue.poll());
                //Look at next message
                message = messageQueue.peek();
            }
        }
    }

    private class ClientAliveTask extends Thread {

        int client_id;

        public ClientAliveTask() {
            super();
            //this.client_id = id;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < MAX_PLAYERS; i++) {
                    if (players[i]) {
                        System.out.println("Checking: " + i);
                        if (getLastActivity(i) + TIMEOUT * 1000 < System.currentTimeMillis()) {
                            server.getConnection(i).close("Kicked by server because not alive.");
                            broadcastMessage(new Disconnected(i));
                        } else {
                            System.out.println("Player " + i + " is still alive.");
                        }
                    }
                }

                //sleep for TIMEOUT interval
                Thread.sleep((long) TIMEOUT * 1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
