/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.editor.tools;

import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.util.TempVars;
import name.huliqing.editor.action.Picker;
import name.huliqing.editor.events.Event;
import name.huliqing.editor.events.JmeEvent;
import name.huliqing.editor.forms.EditFormListener;
import name.huliqing.editor.forms.Mode;
import name.huliqing.editor.select.SelectObj;
import name.huliqing.editor.tiles.Axis;
import name.huliqing.editor.tiles.LocationObj;
import name.huliqing.luoying.manager.PickManager;

/**
 * 移动编辑工具
 * @author huliqing
 */
public class MoveTool extends EditTool implements EditFormListener{
//    private static final Logger LOG = Logger.getLogger(MoveTool.class.getName());
    
    private final Ray ray = new Ray();
    // 物体选择、操作标记（位置）
    private final LocationObj transformObj = new LocationObj();

    // 当前操作的轴向
    private Axis actionAxis;
    // 行为操作开始时编辑器中的被选择的物体，以及该物体的位置
    private SelectObj selectObj;
    
    private final Picker picker = new Picker();
    private boolean picking;
    
    // 开始变换时物体的位置(local)
    private final Vector3f startSpatialLoc = new Vector3f();
    
    public MoveTool(String name) {
        super(name);
    }
    
    @Override
    public void initialize() {
        super.initialize();
        form.getEditRoot().getParent().attachChild(transformObj);
        form.addListener(this);
        updateMarkerState();
    }
    
    @Override
    public void cleanup() {
        form.removeListener(this);
        transformObj.removeFromParent();
        super.cleanup();
    }
    
    /**
     * 绑定移动按键
     * @return 
     */
    public JmeEvent bindMoveEvent() {
        return bindEvent(name + "moveEvent");
    }
    
    @Override
    protected void onToolEvent(Event e) {
        if (e.isMatch()) {
            onActionStart();
        } else {
            onActionEnd();
        }
    }

    private void onActionStart() {
        PickManager.getPickRay(editor.getCamera(), editor.getInputManager().getCursorPosition(), ray);

        actionAxis = transformObj.pickTransformAxis(ray);
        selectObj = form.getSelected();
        
        if (actionAxis != null && selectObj != null) {
            Quaternion planRotation = Picker.PLANE_XY;
            switch (actionAxis.getType()) {
                case x:
                    planRotation = Picker.PLANE_XY;
                    break;
                case y:
                    planRotation = Picker.PLANE_YZ;
                    break;
                case z:
                    planRotation = Picker.PLANE_XZ;
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
            picker.startPick(selectObj.getSelectedSpatial(), form.getMode()
                    , editor.getCamera(), editor.getInputManager().getCursorPosition(), planRotation);
            transformObj.showDebugLine(actionAxis, true);
            startSpatialLoc.set(selectObj.getSelectedSpatial().getLocalTranslation());
            picking = true;
//            LOG.log(Level.INFO, "StartSpatialLoc={0}", startSpatialLoc);
        }
    }

    private void onActionEnd() {
        if (picking) {
            picker.endPick();
            picking = false;
        }
        transformObj.showDebugLine(actionAxis, false);
        actionAxis = null;
        selectObj = null;
    }
    
    @Override
    public void update(float tpf) {
        if (!picking)
            return;
        
        if (!picker.updatePick(editor.getCamera(), editor.getInputManager().getCursorPosition())) {
            return;
        }
        
        TempVars tv = TempVars.get();
        Vector3f diff = picker.getTranslation(actionAxis.getDirection(tv.vect2));
        
        Spatial parent = selectObj.getSelectedSpatial().getParent();
        if (parent != null) {
            tv.quat1.set(parent.getWorldRotation()).inverseLocal().mult(diff, diff);
            diff.divideLocal(parent.getWorldScale());
        } 
        
        Vector3f finalLocalPos = tv.vect1.set(startSpatialLoc).addLocal(diff);
        selectObj.getSelectedSpatial().setLocalTranslation(finalLocalPos);
        transformObj.setLocalTranslation(selectObj.getSelectedSpatial().getWorldTranslation());
        
        tv.release();
    }

    @Override
    public void onModeChanged(Mode mode) {
        updateMarkerState();
    }

    @Override
    public void onSelectChanged(SelectObj selectObj) {
        updateMarkerState();
    }
    
    private void updateMarkerState() {
        selectObj = form.getSelected();
        if (selectObj == null) {
            transformObj.setVisible(false);
            return;
        }
        transformObj.setVisible(true);
        transformObj.setLocalTranslation(form.getSelected().getSelectedSpatial().getWorldTranslation());
        Mode mode = form.getMode();
        switch (form.getMode()) {
            case GLOBAL:
                transformObj.setLocalRotation(new Quaternion());
                break;
            case LOCAL:
                transformObj.setLocalRotation(form.getSelected().getSelectedSpatial().getWorldRotation());
                break;
            case CAMERA:
                transformObj.setLocalRotation(editor.getCamera().getRotation());
                break;
            default:
                throw new IllegalArgumentException("Unknow mode type=" + mode);
        }
    }


}
