/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.luoying.mess;

import com.jme3.network.serializing.Serializable;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.data.EntityData;
import name.huliqing.luoying.layer.service.PlayService;
import name.huliqing.luoying.network.GameClient;
import name.huliqing.luoying.object.Loader;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.object.module.LogicModule;

/**
 * 向指定的场景添加实体
 * @author huliqing
 */
@Serializable
public class EntityAddMess extends GameMess {
    
    // 实体数据
    private EntityData entityData;

    public EntityData getEntityData() {
        return entityData;
    }

    public void setEntityData(EntityData entityData) {
        this.entityData = entityData;
    }

    @Override
    public void applyOnClient(GameClient gameClient) {
        super.applyOnClient(gameClient);
        PlayService playService = Factory.get(PlayService.class);
        Entity entity = (Entity) Loader.load(entityData);
        // 注：这里要关闭客户端的逻辑功能，否则会和服务端的逻辑冲突, 客户端是不需要逻辑的。
        LogicModule lm =  entity.getModuleManager().getModule(LogicModule.class);
        if (lm != null) {
            lm.setEnabled(false);
        }
        playService.addEntity(entity);
    }
    
}