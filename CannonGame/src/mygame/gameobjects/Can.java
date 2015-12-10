package mygame.gameobjects;

import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Cylinder;
import mygame.enums.CanSize;
import static mygame.ClientMain.*;

/**
 * Represents a can.
 * @author Bjorn van der Laan (bjovan-5)
 */
public class Can extends Geometry {
    private int id;
    private CanSize size;
    private int value;
    private Vector2f coords;
    
    public Can(CanSize size) {
        mesh = null;
        if (size == CanSize.Small) {
            mesh = new Cylinder(CAN_RESOLUTION, CAN_RESOLUTION, SMALLCAN_RADIUS, SMALLCAN_HEIGHT, true);
            this.value = SMALLCAN_VALUE;
        } else if (size == CanSize.Medium) {
            mesh = new Cylinder(CAN_RESOLUTION, CAN_RESOLUTION, MEDIUMCAN_RADIUS, MEDIUMCAN_HEIGHT, true);
            this.value = MEDIUMCAN_VALUE;
        } else { //We make large also the default case
            mesh = new Cylinder(CAN_RESOLUTION, CAN_RESOLUTION, LARGECAN_RADIUS, LARGECAN_HEIGHT, true);
            this.value = LARGECAN_VALUE;
        }
        
        
        this.setMesh(mesh);
        this.size = size;
    }
    
    public float getRadius() {
        return ((Cylinder) this.getMesh()).getRadius();
    }
    
    public CanSize getPrizeSize() {
        return size;
    }
    
    public int getValue() {
        return value;
    }
    
    public float getHeight() {
        return ((Cylinder) this.getMesh()).getHeight();
    }
    
    public Vector2f getCoords() {
        return this.coords;
    }
    
    public void setCoords(Vector2f coords) {
        this.coords = coords;
    }
    
    public int getId() {
        return this.id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
}
