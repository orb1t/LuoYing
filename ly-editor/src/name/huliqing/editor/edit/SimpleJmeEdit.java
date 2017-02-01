/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.editor.edit;

import com.jme3.scene.Node;
import com.jme3.util.SafeArrayList;
import java.util.ArrayList;
import java.util.List;
import name.huliqing.editor.edit.controls.ControlTile;
import name.huliqing.editor.toolbar.BaseEditToolbar;
import name.huliqing.editor.toolbar.Toolbar;

/**
 * 3D模型编辑器窗口
 * @author huliqing 
 * @param <T> 
 */
public abstract class SimpleJmeEdit<T extends ControlTile> extends JmeAbstractEdit {
    
    // 侦听器
    protected final List<SimpleJmeEditListener> editFormListeners = new ArrayList<SimpleJmeEditListener>();
    
    // 变换模式
    protected Mode mode = Mode.GLOBAL;
    
    // 当前选择的物体
    protected final SafeArrayList<ControlTile> cts = new SafeArrayList<ControlTile>(ControlTile.class);
    protected T selectObj;

    @Override
    protected Toolbar createToolbar() {
        return new BaseEditToolbar(this);
    }

    /**
     * 默认情况下SimpleJmeEdit不会创建扩展工具栏，子类可以根据实际情况来创建并返回一个扩展工具栏列表
     * @return 
     */
    @Override
    protected List<Toolbar> createExtToolbars() {
        return null;
    }
    
    public Mode getMode() {
        return mode;
    }
    
    public void setMode(Mode mode) {
        boolean changed = this.mode != mode;
        this.mode = mode;
        if (changed) {
            editFormListeners.forEach(l -> {l.onModeChanged(mode);});
        }
    }
    
    public T getSelected() {
        return selectObj;
    }
    
    /**
     * 把一个物体设置为当前的选择的主物体
     * @param selectObj 
     */
    public void setSelected(T selectObj) {
        this.selectObj = selectObj;
        editFormListeners.forEach(l -> {l.onSelect(selectObj);});
    }
    
    /**
     * 获取编辑窗口根节点。
     * @return 
     */
    public Node getEditRoot() {
        return editRoot;
    }
    
    public void addSimpleEditListener(SimpleJmeEditListener listener) {
        if (!editFormListeners.contains(listener)) {
            editFormListeners.add(listener);
        }
    }
    
    public boolean removeEditFormListener(SimpleJmeEditListener listener) {
        return editFormListeners.remove(listener);
    }
    
//    /**
//     * 通过射线方式从场景中选择一个可选择的物体，如果存在这样一个物体则返回，否则返回null.
//     * @param ray
//     * @return 
//     */
//    public abstract T doPick(Ray ray);
    
    // --------------------------------------------------------------------------------------------------------------------------------
    
    public void addControlTile(T ct) {
        if (!cts.contains(ct)) {
            cts.add(ct);
        }
        if (!ct.isInitialized()) {
            ct.initialize(editRoot);
        }
    }
    
    public boolean removeControlTile(T ct) {
        if (ct.isInitialized()) {
            ct.cleanup();
        }
        return cts.remove(ct);
    }
    
    public <T extends ControlTile> SafeArrayList<T> getControlTiles() {
        return (SafeArrayList<T>) cts;
    }
}
