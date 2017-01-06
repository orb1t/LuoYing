/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.editor.edit.spatial;

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.logging.Level;
import java.util.logging.Logger;
import name.huliqing.editor.Editor;
import name.huliqing.editor.edit.SimpleEditForm;
import name.huliqing.editor.manager.Manager;
import name.huliqing.editor.toolbar.EditToolbar;

/**
 * 编辑单个Spatial的编辑器
 * @author huliqing
 */ 
public class SpatialEditForm extends SimpleEditForm {

    private static final Logger LOG = Logger.getLogger(SpatialEditForm.class.getName());
    private final Node root = new Node();

    // Spatia文件绝对路径
    private String fileFullPath;
    private String fileInAssets;
    // 编辑中的模型
    private Spatial spatial;
    
    @Override
    public void initialize(Editor editor) {
        super.initialize(editor);
        editRoot.attachChild(root);
        editRoot.addLight(new DirectionalLight());
        editRoot.addLight(new AmbientLight());
        
        if (fileInAssets != null) {
            load();
        }
        setToolbar(new EditToolbar());
    }

    @Override
    public void cleanup() {
        root.removeFromParent();
        if (spatial != null) {
            spatial.removeFromParent();
        }
        super.cleanup(); 
    }
    
    public void setFilePath(String fileFullPath) {
        String assetPath = Manager.getConfigManager().getMainAssetDir();
        this.fileFullPath = fileFullPath;
        fileInAssets = fileFullPath.replace(assetPath, "").replace("\\", "/");
        if (spatial != null) {
            spatial.removeFromParent();
        }
        if (isInitialized()) {
            load();
        }
    }
    
    private void load() {
        if (fileInAssets == null || fileInAssets.isEmpty())
            return;
        try {
            spatial = editor.getAssetManager().loadModel(fileInAssets);
            root.attachChild(spatial);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Could not load mode, filePath={0}", fileInAssets);
        }
    }
    
}