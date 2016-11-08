/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.luoying;

import java.util.HashMap;
import java.util.Map;
import name.huliqing.luoying.layer.network.ActionNetwork;
import name.huliqing.luoying.layer.network.ActionNetworkImpl;
import name.huliqing.luoying.layer.network.ActorNetwork;
import name.huliqing.luoying.layer.network.ActorNetworkImpl;
import name.huliqing.luoying.layer.network.AttributeNetwork;
import name.huliqing.luoying.layer.network.AttributeNetworkImpl;
import name.huliqing.luoying.layer.network.EntityNetwork;
import name.huliqing.luoying.layer.network.EntityNetworkImpl;
import name.huliqing.luoying.layer.network.ItemNetwork;
import name.huliqing.luoying.layer.network.ItemNetworkImpl;
import name.huliqing.luoying.layer.network.PlayNetwork;
import name.huliqing.luoying.layer.network.PlayNetworkImpl;
import name.huliqing.luoying.layer.network.ObjectNetworkImpl;
import name.huliqing.luoying.layer.network.SkillNetwork;
import name.huliqing.luoying.layer.network.SkillNetworkImpl;
import name.huliqing.luoying.layer.network.SkinNetwork;
import name.huliqing.luoying.layer.network.SkinNetworkImpl;
import name.huliqing.luoying.layer.network.StateNetwork;
import name.huliqing.luoying.layer.network.StateNetworkImpl;
import name.huliqing.luoying.layer.network.TalentNetwork;
import name.huliqing.luoying.layer.network.TalentNetworkImpl;
import name.huliqing.luoying.layer.network.TaskNetwork;
import name.huliqing.luoying.layer.network.TaskNetworkImpl;
import name.huliqing.luoying.layer.service.ActionService;
import name.huliqing.luoying.layer.service.ActionServiceImpl;
import name.huliqing.luoying.layer.service.ActorAnimService;
import name.huliqing.luoying.layer.service.ActorAnimServiceImpl;
import name.huliqing.luoying.layer.service.ActorService;
import name.huliqing.luoying.layer.service.ActorServiceImpl;
import name.huliqing.luoying.layer.service.BulletService;
import name.huliqing.luoying.layer.service.BulletServiceImpl;
import name.huliqing.luoying.layer.service.ConfigService;
import name.huliqing.luoying.layer.service.ConfigServiceImpl;
import name.huliqing.luoying.layer.service.SystemServiceImpl;
import name.huliqing.luoying.layer.service.ElService;
import name.huliqing.luoying.layer.service.ElServiceImpl;
import name.huliqing.luoying.layer.service.ItemService;
import name.huliqing.luoying.layer.service.ItemServiceImpl;
import name.huliqing.luoying.layer.service.LogicService;
import name.huliqing.luoying.layer.service.LogicServiceImpl;
import name.huliqing.luoying.layer.service.MagicService;
import name.huliqing.luoying.layer.service.MagicServiceImpl;
import name.huliqing.luoying.layer.service.PlayService;
import name.huliqing.luoying.layer.service.PlayServiceImpl;
import name.huliqing.luoying.layer.service.ObjectServiceImpl;
import name.huliqing.luoying.layer.service.ResistService;
import name.huliqing.luoying.layer.service.ResistServiceImpl;
import name.huliqing.luoying.layer.service.SaveService;
import name.huliqing.luoying.layer.service.SaveServiceImpl;
import name.huliqing.luoying.layer.service.SceneService;
import name.huliqing.luoying.layer.service.SceneServiceImpl;
import name.huliqing.luoying.layer.service.SkillService;
import name.huliqing.luoying.layer.service.SkillServiceImpl;
import name.huliqing.luoying.layer.service.SkinService;
import name.huliqing.luoying.layer.service.SkinServiceImpl;
import name.huliqing.luoying.layer.service.StateService;
import name.huliqing.luoying.layer.service.StateServiceImpl;
import name.huliqing.luoying.layer.service.TalentService;
import name.huliqing.luoying.layer.service.TalentServiceImpl;
import name.huliqing.luoying.layer.service.TaskService;
import name.huliqing.luoying.layer.service.TaskServiceImpl;
import name.huliqing.luoying.layer.service.SystemService;
import name.huliqing.luoying.layer.service.GameLogicService;
import name.huliqing.luoying.layer.service.GameLogicServiceImpl;
import name.huliqing.luoying.layer.service.ObjectService;
import name.huliqing.luoying.layer.network.ObjectNetwork;
import name.huliqing.luoying.layer.service.EntityService;
import name.huliqing.luoying.layer.service.EntityServiceImpl;

/**
 *
 * @author huliqing
 */
public class Factory {
    
    private final static Map<Class, Class> CLASS_MAP = new HashMap<Class, Class>();
    private final static Map<Class, Object> INSTANCE_MAP = new HashMap<Class, Object>();
    
    static {
        // network
        CLASS_MAP.put(ActionNetwork.class, ActionNetworkImpl.class);
        CLASS_MAP.put(ActorNetwork.class, ActorNetworkImpl.class);
        CLASS_MAP.put(AttributeNetwork.class, AttributeNetworkImpl.class);
        CLASS_MAP.put(EntityNetwork.class, EntityNetworkImpl.class);
        CLASS_MAP.put(ItemNetwork.class, ItemNetworkImpl.class);
        CLASS_MAP.put(PlayNetwork.class, PlayNetworkImpl.class);
        CLASS_MAP.put(ObjectNetwork.class, ObjectNetworkImpl.class);
        CLASS_MAP.put(SkillNetwork.class, SkillNetworkImpl.class);
        CLASS_MAP.put(SkinNetwork.class, SkinNetworkImpl.class);
        CLASS_MAP.put(StateNetwork.class, StateNetworkImpl.class);
        CLASS_MAP.put(TalentNetwork.class, TalentNetworkImpl.class);
        CLASS_MAP.put(TaskNetwork.class, TaskNetworkImpl.class);
        
        // service
        CLASS_MAP.put(ActionService.class, ActionServiceImpl.class);
        CLASS_MAP.put(ActorAnimService.class, ActorAnimServiceImpl.class);
        CLASS_MAP.put(ActorService.class, ActorServiceImpl.class);
        CLASS_MAP.put(BulletService.class, BulletServiceImpl.class);
        CLASS_MAP.put(ConfigService.class, ConfigServiceImpl.class);
        CLASS_MAP.put(ElService.class, ElServiceImpl.class);
        CLASS_MAP.put(EntityService.class, EntityServiceImpl.class);
        CLASS_MAP.put(GameLogicService.class, GameLogicServiceImpl.class);
        CLASS_MAP.put(ItemService.class, ItemServiceImpl.class);
        CLASS_MAP.put(LogicService.class, LogicServiceImpl.class);
        CLASS_MAP.put(MagicService.class, MagicServiceImpl.class);
        CLASS_MAP.put(PlayService.class, PlayServiceImpl.class);
        CLASS_MAP.put(ObjectService.class, ObjectServiceImpl.class);
        CLASS_MAP.put(ResistService.class, ResistServiceImpl.class);
        CLASS_MAP.put(SaveService.class, SaveServiceImpl.class);
        CLASS_MAP.put(SceneService.class, SceneServiceImpl.class);
        CLASS_MAP.put(SkillService.class, SkillServiceImpl.class);
        CLASS_MAP.put(SkinService.class, SkinServiceImpl.class);
        CLASS_MAP.put(StateService.class, StateServiceImpl.class);      
        CLASS_MAP.put(SystemService.class, SystemServiceImpl.class);
        CLASS_MAP.put(TalentService.class, TalentServiceImpl.class);
        CLASS_MAP.put(TaskService.class, TaskServiceImpl.class);
        
    }
    
    public static <T extends Object> void register(Class<T> c, Class ins) {
        if (!c.isAssignableFrom(ins)) {
            throw new IllegalArgumentException("Could not register service. service class=" + c + ", object class=" + ins);
        }
        CLASS_MAP.put(c, ins);
    }
    
    public static <T extends Object> T get(Class<T> cla) {
        T ins = (T) INSTANCE_MAP.get(cla);
        if (ins != null) {
            return ins;
        }
        Class clazz = CLASS_MAP.get(cla);
        if (clazz != null) {
            try {
                // 1.实例化
                ins = (T) clazz.newInstance();
            } catch (InstantiationException ex) {
                throw new RuntimeException("Could not instance for class=" + cla + ", error=" + ex.getMessage());
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Could not instance for class=" + cla + ", error=" + ex.getMessage());
            }
            // 2.存入缓存
            INSTANCE_MAP.put(cla, ins);
            // 3.注入其它引用
            ((Inject) ins).inject();
        } else {
            throw new NullPointerException("No instance register for class:" + cla);
        }
        return ins;
    }
    
}