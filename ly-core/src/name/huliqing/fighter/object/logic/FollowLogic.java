/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.fighter.object.logic;

import com.jme3.math.FastMath;
import name.huliqing.fighter.Factory;
import name.huliqing.fighter.object.actor.Actor;
import name.huliqing.fighter.object.action.FollowAction;
import name.huliqing.fighter.data.LogicData;
import name.huliqing.fighter.game.service.ActionService;
import name.huliqing.fighter.game.service.PlayService;
import name.huliqing.fighter.object.action.Action;
import name.huliqing.fighter.utils.MathUtils;

/**
 * 跟随逻辑
 * @author huliqing
 * @param <T>
 */
public class FollowLogic<T extends LogicData> extends ActorLogic<T> {
//    private final static Logger logger = Logger.getLogger(FollowLogic.class.getName());
    private final PlayService playService = Factory.get(PlayService.class);
    private final ActionService actionService = Factory.get(ActionService.class);
    
    // 距离的最近点和最远点
    private float minFollow = 3f;
    private float maxFollow = 7f;
    
//    // remove20160310,考虑不要这个参数,这会导致角色在跟随和拔刀之间不停来回切换，很难看。
//    // 绝对的跟随距离限制,在距离达到这个值之后将转为跟随,不论角色是否在战斗.
//    private float distanceLimit = 25;
//    // 绝对的最远跟踪距离，当跟随者与被跟随者距离大于这个数值（平方）时，将不管任
//    // 何条件，直接切换到跟随模式
//    private float distanceLimitSquared;
    
    private FollowAction followAction;
    private Actor target;
    // 最近一次跟随到最近的距离
    private float lastFollowUsed;
    
    @Override
    public void setData(T data) {
        super.setData(data); 
        followAction = (FollowAction) actionService.loadAction(data.getAttribute("followAction"));
        maxFollow = data.getAsFloat("maxFollow", maxFollow);
        minFollow = data.getAsFloat("minFollow", minFollow);
        
        maxFollow = MathUtils.clamp(maxFollow, 0, maxFollow);
        minFollow = MathUtils.clamp(minFollow, 0, maxFollow);
        
        // remove20160310,考虑不要这个参数,这会导致角色在跟随和拔刀之间不停来回切换，很难看。
//        distanceLimit = data.getAsFloat("distanceLimit", distanceLimit);
//        distanceLimitSquared = distanceLimit * distanceLimit;
    }
    
    @Override
    protected void doLogic(float tpf) {
        long ft = actor.getData().getFollowTarget(); 
        
        // 如果角色没有设置跟随的目标,则停止当前的跟随行为(注:只停止当前逻辑启动的followAction).
        if (ft <= 0) {
            target = null;
            Action current = actionService.getPlayingAction(actor);
            if (current == followAction) {
                actionService.playAction(actor, null);
            }
            return;
        }
        
        // 如果跟随的目标发生变化则重新查找目标进行跟随.找不到指定目标则不处理
        if (target == null || target.getData().getUniqueId() != ft) {
            target = playService.findActor(ft);
            if (target == null || target == actor) {
                return;
            }
        }
        
        // 如果距离超过MaxFollowDistance则直接转到跟随,不管是否在战斗
        if (actionService.isPlayingFight(actor) 
                
                 // remove20160310,不要这个参数,这会导致角色在跟随和拔刀之间不停来回切换，很难看。
//                && target.getDistanceSquared(self) < distanceLimitSquared
                
                ) {
            return;
        }
        
        // 跟随
        doFollow();
    }
    
    private void doFollow() {
        if (followAction.isEnd() && actor.getDistance(target.getModel().getWorldTranslation()) > maxFollow) {
            lastFollowUsed = FastMath.nextRandomFloat() * (maxFollow - minFollow) + minFollow;
            followAction.setFollow(target.getModel());
            followAction.setNearest(lastFollowUsed);
            playAction(followAction);
        }
    }
    
}
