package mygame.gameobjects;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Line;
import static mygame.ClientMain.*;

/**
 * Represents a canon.
 * @author Bjorn van der Laan (bjovan-5)
 */
public class Cannon extends Node {

    private int id;
    public Node support, base, barrel, muzzle_tip, laser, cannon;
    public String nickname;
    private Material barrel_gray, barrel_gold;
    public BitmapText hoverText;

    public Cannon(int id, String nickname, SimpleApplication app) {
        // Set player id
        this.id = id;
        this.setName("Cannon" + id);
        this.nickname = nickname;

        // Create support plate
        Cylinder support_mesh = new Cylinder(20, 20, CANNON_SUPPORT_RADIUS, CANNON_SUPPORT_HEIGHT, true);
        Geometry support_geom = new Geometry("Support_geom", support_mesh);
        Material support_mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        support_mat.setColor("Color", ColorRGBA.Black);
        support_geom.setMaterial(support_mat);
        support = new Node("Support");
        support.attachChild(support_geom);
        support.move(0,0,0.5f*CANNON_SUPPORT_HEIGHT);
        this.attachChild(support);

        // Create base
        Cylinder base_mesh = new Cylinder(20, 20, CANNON_BASE_RADIUS, CANNON_BASE_HEIGHT, true);
        Geometry base_geom = new Geometry("Base_geom", base_mesh);
        Material base_mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        base_mat.setColor("Color", ColorRGBA.Brown);
        base_geom.setMaterial(base_mat);
        base = new Node("Base");
        Quaternion base_quat = new Quaternion();
        base_quat.fromAngleAxis(FastMath.PI / 2, new Vector3f(0, 1, 0));
        base.setLocalRotation(base_quat);
        base.attachChild(base_geom);

        // Create barrel
        Cylinder barrel_mesh = new Cylinder(20, 20, CANNON_BARREL_RADIUS, CANNON_BARREL_LENGTH, true);
        Geometry barrel_geom = new Geometry("Barrel_geom", barrel_mesh);

        barrel_gray = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        barrel_gray.setColor("Color", ColorRGBA.DarkGray);
        barrel_gold = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        barrel_gold.setColor("Color", ColorRGBA.Yellow);

        barrel_geom.setMaterial(barrel_gray);
        barrel = new Node("Barrel");
        barrel.setLocalTranslation(0, CANNON_BARREL_RADIUS * 1.1f, 0.5f * CANNON_BARREL_LENGTH);
        barrel.attachChild(barrel_geom);

        // Create muzzle tip        
        muzzle_tip = new Node("Tip");
        muzzle_tip.setLocalTranslation(0, 0, 0.5f * CANNON_BARREL_LENGTH);
        barrel.attachChild(muzzle_tip);

        // Create laser
        Line laser_mesh = new Line(new Vector3f(), new Vector3f(0, 0, CANNON_LASER_RANGE));
        Geometry laser_geom = new Geometry("Laser_geom", laser_mesh);
        Material laser_mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        laser_mat.setColor("Color", ColorRGBA.Red);
        laser_geom.setMaterial(laser_mat);
        laser = new Node("Laser");
        laser.setLocalTranslation(0, 0, 0.5f * CANNON_BARREL_LENGTH);
        laser.attachChild(laser_geom);
        //barrel.attachChild(laser); //turned off in beginning

        // Text
        BitmapFont guiFont = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        hoverText = new BitmapText(guiFont, false);
        hoverText.setSize(guiFont.getCharSet().getRenderedSize() * 0.5f);
        setScore(0);
        Node n = new Node("nickname");
        n.attachChild(hoverText);
        BillboardControl bc = new BillboardControl();
        n.addControl(bc);
        n.setLocalTranslation(0, 40, 0);
       
        cannon = new Node();
        cannon.attachChild(base);
        cannon.attachChild(barrel);
        cannon.attachChild(n);

        // Rotate cannon so it points in the xz plane
        Quaternion cannon_quat = new Quaternion();
        cannon_quat.fromAngleAxis(FastMath.PI / 2, new Vector3f(-1, 0, 0));
        cannon.setLocalRotation(cannon_quat);
        this.attachChild(cannon);
    }

    public int getId() {
        return this.id;
    }
    
    public String getNickname() {
        return this.nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public void hideHoverText() {
        cannon.detachChildNamed("nickname");
    }

    public void setId(int id) {
        this.id = id;
    }

    public void winnerLook(boolean won) {
        if(won) {
            barrel.getChild("Barrel_geom").setMaterial(barrel_gold);
        } else {
            barrel.getChild("Barrel_geom").setMaterial(barrel_gray);
        }       
        winnerText(won);
    }

    public final void activateLaser(boolean on) {
        if (on) {
            barrel.attachChild(laser);
        } else {
            barrel.detachChild(laser);
        }
    }
    
    private void winnerText(boolean won) {
        if(won) {
            hoverText.setColor(ColorRGBA.Yellow);
        } else {
            hoverText.setColor(ColorRGBA.White);
        }
    }
    
    public final void setScore(int score) {
        hoverText.setText(nickname + " (" + score + ")");
    }
}
