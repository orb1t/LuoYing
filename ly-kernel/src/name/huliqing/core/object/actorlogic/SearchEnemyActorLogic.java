/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.object.actorlogic;

import name.huliqing.core.Factory;
import name.huliqing.core.object.actor.Actor;
import name.huliqing.core.data.ActorLogicData;
import name.huliqing.core.mvc.network.ActorNetwork;
import name.huliqing.core.mvc.service.ActorService;
import name.huliqing.core.mvc.service.PlayService;
import name.huliqing.core.object.module.ActorModule;
import name.huliqing.core.object.module.LogicModule;

/**
 * 逻辑：
 * 1.每隔一定时间自动搜寻可视范围内的敌人
 * 该逻辑不会有战斗行为或IDLE行为，需要和其它逻辑配合才有意义。
 * @author huliqing
 * @param <T>
 */
public class SearchEnemyActorLogic<T extends ActorLogicData> extends ActorLogic<T> {
    private final PlayService playService = Factory.get(PlayService.class);
    private final ActorService actorService = Factory.get(ActorService.class);
    private final ActorNetwork actorNetwork = Factory.get(ActorNetwork.class);
    
    private ActorModule actorModule;
    private LogicModule logicModule;
    
    // 自动频率，
    private boolean autoInterval = true;
    private float maxInterval = 3;
    private float minInterval = 1;

    @Override
    public void setData(T data) {
        super.setData(data); 
        this.autoInterval = data.getProto().getAsBoolean("autoInterval", autoInterval);
        this.maxInterval = data.getProto().getAsFloat("maxInterval", maxInterval);
        this.minInterval = data.getProto().getAsFloat("minInterval", minInterval);
    }
    
    @Override
    public void setActor(Actor actor) {
        super.setActor(actor); 
        actorModule = actor.getModule(ActorModule.class);
        logicModule = actor.getModule(LogicModule.class);
    }
  
    @Override
    protected void doLogic(float tpf) {
        // 只有打开了自动侦察功能才执行逻辑
        if (!logicModule.isAutoDetect()) {
            return;
        }
        
        // 增加自动频率的逻辑
        Actor target = actorService.getTarget(actor);
        
        if (target == null || actorService.isDead(target) || !actorService.isEnemy(target, actor) 
                || !playService.isInScene(target)) {
            
            Actor enemy = actorService.findNearestEnemyExcept(actor, actorModule.getViewDistance(), null);
            if (enemy != null) {
                actorNetwork.setTarget(actor, enemy);
            }
            // 如果是自动间隔，则在有敌人时频率降低，在没有敌人时频率加大 
            if (autoInterval) {
                setInterval(enemy != null ? maxInterval : minInterval);
            }
        }
    }
    
}
