/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.editor.edit;

import java.util.logging.Logger;
import name.huliqing.editor.Editor;
import name.huliqing.editor.undoredo.UndoRedo;
import name.huliqing.editor.undoredo.UndoRedoManager;
import name.huliqing.fxswing.Jfx;

/**
 * 
 * @author huliqing
 * @param <T>
 */
public abstract class JfxAbstractEdit<T extends JmeEdit> implements JfxEdit<T> {

    private static final Logger LOG = Logger.getLogger(JfxAbstractEdit.class.getName());

    protected Editor editor;
    protected T form;
    protected boolean formInitialized;
    protected boolean jfxInitialized;

    @Override
    public final void initialize(Editor editor) {
        if (formInitialized || jfxInitialized) {
            throw new IllegalArgumentException();
        }
        this.editor = editor;
        // 先初始化3D编辑器,再初始化UI
        Jfx.runOnJme(() -> {
            if (form != null) {
                form.initialize(editor);
                formInitialized = true;
                LOG.info(">>>>JmeFormInitialized");
            }
            Jfx.runOnJfx(() -> {
                jfxInitialize();
                jfxInitialized = true;
                LOG.info(">>>>JFXInitialized");
            });
        });
    }

    @Override
    public void update(float tpf) {
        if (formInitialized) {
            form.update(tpf);
        }
    }

    @Override
    public final void cleanup() {
        // 先清理jfx
        if (jfxInitialized) {
            jfxCleanup();
            LOG.info(">>>>----JFXCleanup");
        }
        // 再清理3d编辑器
        if (formInitialized) {
            Jfx.runOnJme(() -> {
                form.cleanup();
                LOG.info(">>>>----JmeFormCleanup");
            });            
        }
    }
    
    @Override
    public Editor getEditor() {
        return editor;
    }

    @Override
    public UndoRedoManager getUndoRedoManager() {
        if (form != null) {
            return form.getUndoRedoManager();
        }
        return null;
    }

    @Override
    public void addUndoRedo(UndoRedo ur) {
        if (form != null) {
            form.getUndoRedoManager().add(ur);
        }
    }
    
    public void jfxOnDragStarted() {
        // 由子类实现
    }
    
    public void jfxOnDragEnded() {
        // 由子类实现
    }

    /**
     * 初始化JFX界面
     */
    protected abstract void jfxInitialize();
    
    /**
     * 清理JFX界面
     */
    protected abstract void jfxCleanup();
}