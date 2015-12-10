package mygame.gameobjects;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import static mygame.ClientMain.*;

/**
 *
 * @author Bjorn van der Laan (bjovan-5)
 */
public class Cannonball extends Node {

    public int p_id;
    public int c_id;
    public Vector3f pos;
    public Vector3f dir;

    public Cannonball(int p_id, int c_id, Vector3f pos, Vector3f dir, SimpleApplication app) {
        Sphere ball_mesh = new Sphere(CANNONBALL_RESOLUTION, CANNONBALL_RESOLUTION, CANNONBALL_RADIUS);
        Geometry ball_geom = new Geometry("Ball", ball_mesh);
        Material ball_mat = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        ball_mat.setBoolean("UseMaterialColors", true);
        ball_mat.setTexture("DiffuseMap", app.getAssetManager().loadTexture("Textures/Terrain/Pond/Pond.jpg"));
        ball_mat.setColor("Diffuse", ColorRGBA.Gray);
        ball_mat.setColor("Ambient", ColorRGBA.Gray);
        ball_geom.setMaterial(ball_mat);

        this.p_id = p_id;
        this.c_id = c_id;
        this.pos = pos;
        this.dir = dir;
        
        this.attachChild(ball_geom);

        //Legacy
        ball_geom.setUserData("id", c_id);
        ball_geom.setUserData("dir", dir);

    }
    
    public Cannonball(int p_id, int c_id, Vector3f pos, Vector3f dir) {
        this.p_id = p_id;
        this.c_id = c_id;
        this.pos = pos;
        this.dir = dir;
    }
}
