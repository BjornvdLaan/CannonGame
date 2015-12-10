package mygame.helpers;

import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.jme3.network.serializing.Serializer;

/**
 * Network utilities. Stores common settings and message definitions.
 * @author Bjorn van der Laan (bjovan-5)
 */
public class NetUtils {

    public static final int PORT = 34439;
    public static final String HOST = "127.0.0.1";
    public static final int CLIENT_UPDATE_RATE = 50;
    public static final int SERVER_UPDATE_RATE = 50;

    public static void initMessages() {
        Serializer.registerClass(Connect.class);
        Serializer.registerClass(Disconnect.class);
        Serializer.registerClass(Alive.class);
        Serializer.registerClass(LaserInput.class);
        Serializer.registerClass(RotateInput.class);
        Serializer.registerClass(FireInput.class);
        Serializer.registerClass(NewClientAccepted.class);
        Serializer.registerClass(Reject.class);
        Serializer.registerClass(Disconnected.class);
        Serializer.registerClass(Prepare.class);
        Serializer.registerClass(Start.class);
        Serializer.registerClass(Activate.class);
        Serializer.registerClass(Inactivate.class);
        Serializer.registerClass(Move.class);
        Serializer.registerClass(Change.class);
        Serializer.registerClass(Award.class);
        Serializer.registerClass(Rotate.class);
        Serializer.registerClass(LaserToggled.class);
        Serializer.registerClass(Congratulate.class);
    }

    //Client -> Server
    /**
     * Connect.
     */
    @Serializable
    public static class Connect extends AbstractMessage {

        public String n;

        public Connect() {
        }

        public Connect(String n) {
            this.n = n;
        }
    }

    /**
     * Disconnect.
     */
    @Serializable
    public static class Disconnect extends AbstractMessage {

        public Disconnect() {
        }
    }

    /**
     * Alive.
     */
    @Serializable
    public static class Alive extends AbstractMessage {

        public Alive() {
        }
    }

    /**
     * LaserInput.
     */
    @Serializable
    public static class LaserInput extends AbstractMessage {

        public LaserInput() {
        }
    }

    /**
     * RotateInput.
     */
    @Serializable
    public static class RotateInput extends AbstractMessage {

        public boolean r;
        public float a;

        public RotateInput() {
        }

        public RotateInput(boolean r, float a) {
            this.r = r;
            this.a = a;
        }
    }

    /**
     * FireInput.
     */
    @Serializable
    public static class FireInput extends AbstractMessage {

        public FireInput() {
        }
    }

    //Server -> Client
    /**
     * NewClientAccepted.
     */
    @Serializable
    public static class NewClientAccepted extends AbstractMessage {

        public String n;
        public int id;
        public Vector3f position;
        public Vector3f orientation;

        public NewClientAccepted() {
        }

        public NewClientAccepted(String n, int id, Vector3f position, Vector3f orientation) {
            this.n = n;
            this.id = id;
            this.position = position;
            this.orientation = orientation;
        }
    }

    /**
     * Reject.
     */
    @Serializable
    public static class Reject extends AbstractMessage {

        public String reason;

        public Reject() {
        }

        public Reject(String reason) {
            this.reason = reason;
        }
    }

    /**
     * Disconnected.
     */
    @Serializable
    public static class Disconnected extends AbstractMessage {

        public int id;

        public Disconnected() {
        }

        public Disconnected(int id) {
            this.id = id;
        }
    }

    /**
     * Prepare.
     */
    @Serializable
    public static class Prepare extends AbstractMessage {

        public Vector3f[] pa;
        public String[] na;
        public float[] ra;

        public Prepare() {
        }

        public Prepare(Vector3f[] pa, String[] na, float[] ra) {
            this.pa = pa;
            this.na = na;
            this.ra = ra;
        }
    }

    /**
     * Start.
     */
    @Serializable
    public static class Start extends AbstractMessage {

        public Start() {
        }
    }

    /**
     * Activate.
     */
    @Serializable
    public static class Activate extends AbstractMessage {

        public int i;
        public int c;
        public Vector3f p;
        public Vector3f d;

        public Activate() {
        }

        public Activate(int i, int c, Vector3f p, Vector3f d) {
            this.i = i;
            this.c = c;
            this.p = p;
            this.d = d;
        }
    }

    /**
     * Inactivate.
     */
    @Serializable
    public static class Inactivate extends AbstractMessage {

        public int i;
        public int c;

        public Inactivate() {
        }

        public Inactivate(int i, int c) {
            this.i = i;
            this.c = c;
        }
    }

    /**
     * Move.
     */
    @Serializable
    public static class Move extends AbstractMessage {

        public int c;
        public Vector3f p;

        public Move() {
        }

        public Move(int c, Vector3f p) {
            this.c = c;
            this.p = p;
        }
    }

    /**
     * Change.
     */
    @Serializable
    public static class Change extends AbstractMessage {

        public int i;
        public int c;
        public Vector3f p;
        public Vector3f d;

        public Change() {
        }

        public Change(int i, int c, Vector3f p, Vector3f d) {
            this.i = i;
            this.c = c;
            this.p = p;
            this.d = d;
        }
    }

    /**
     * Award.
     */
    @Serializable
    public static class Award extends AbstractMessage {

        public int i;
        public int s;

        public Award() {
        }

        public Award(int i, int s) {
            this.i = i;
            this.s = s;
        }
    }

    /**
     * Rotate.
     */
    @Serializable
    public static class Rotate extends AbstractMessage {

        public int i;
        public boolean b;
        public float a;

        public Rotate() {
        }

        public Rotate(int i, boolean b, float a) {
            this.i = i;
            this.b = b;
            this.a = a;
        }
    }

    /**
     * LaserToggled.
     */
    @Serializable
    public static class LaserToggled extends AbstractMessage {

        public int i;
        public boolean b;

        public LaserToggled() {
        }

        public LaserToggled(int i, boolean b) {
            this.i = i;
            this.b = b;
        }
    }

    /**
     * Congratulate.
     */
    @Serializable
    public static class Congratulate extends AbstractMessage {

        public int[] w;
        public int n;

        public Congratulate() {
        }

        public Congratulate(int[] w, int n) {
            this.w = w;
            this.n = n;
        }
    }
}
