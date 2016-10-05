/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.ly.layer.network;

import name.huliqing.ly.network.Network;
import name.huliqing.ly.Factory;
import name.huliqing.ly.layer.service.StateService;
import name.huliqing.ly.mess.MessStateAdd;
import name.huliqing.ly.mess.MessStateRemove;
import name.huliqing.ly.object.actor.Actor;

/**
 * 重要：对于客户端和服务端的同步，一般应先广播到所有客户端，然后再在服务端自
 * 身执行逻辑。这可避免在服务端执行逻辑（未广播之前），修改了角色属性或其它，
 * 而这些修改又广播到了客户端，这会导致这个操作在广播到客户端之后，状态已经
 * 与服务端执行这个操作之前的状态发生了变化。
 * @author huliqing
 */
public class StateNetworkImpl implements StateNetwork {
    private final static Network NETWORK = Network.getInstance(); 
    private StateService stateService;

    @Override
    public void inject() {
        stateService = Factory.get(StateService.class);
    }
    
    @Override
    public float checkAddState(Actor actor, String stateId) {
        return stateService.checkAddState(actor, stateId);
    }
    
    @Override
    public boolean addState(Actor actor, String stateId, Actor sourceActor) {
        if (!NETWORK.isClient()) {
            float resist = stateService.checkAddState(actor, stateId);
            if (resist < 1) {
                if (NETWORK.hasConnections()) {
                    // 先广播
                    MessStateAdd mess = new MessStateAdd();
                    mess.setActorId(actor.getData().getUniqueId());
                    mess.setStateId(stateId);
                    mess.setResist(resist);
                    mess.setSourceActorId(sourceActor != null ? sourceActor.getData().getUniqueId() : -1);
                    NETWORK.broadcast(mess);
                }
                
                // 再执行服务端添加逻辑
                stateService.addStateForce(actor, stateId, resist, sourceActor);
            } else {
                // TODO:添加状态被抵抗，需要把提示信息广播到客户端
            }
        }
        return false;
    }

    @Override
    public void addStateForce(Actor actor, String stateId, float resist, Actor sourceActor) {
        if (!NETWORK.isClient()) {
            if (NETWORK.hasConnections()) {
                // 先广播
                MessStateAdd mess = new MessStateAdd();
                mess.setActorId(actor.getData().getUniqueId());
                mess.setStateId(stateId);
                mess.setResist(resist);
                mess.setSourceActorId(sourceActor != null ? sourceActor.getData().getUniqueId() : -1);
                NETWORK.broadcast(mess);
            }

            // 再执行服务端添加逻辑
            stateService.addStateForce(actor, stateId, resist, sourceActor);
        }
    }

    @Override
    public boolean removeState(Actor actor, String stateId) {
        if (!NETWORK.isClient()) {
            if (NETWORK.hasConnections()) {
                // 先广播
                MessStateRemove mess = new MessStateRemove();
                mess.setActorId(actor.getData().getUniqueId());
                mess.setStateId(stateId);
                NETWORK.broadcast(mess);
            }
            stateService.removeState(actor, stateId);
            return true;
        }
        return false;
    }

}