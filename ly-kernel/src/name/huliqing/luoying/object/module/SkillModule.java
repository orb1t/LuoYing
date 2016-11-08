/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.luoying.object.module;

import com.jme3.scene.control.Control;
import com.jme3.util.SafeArrayList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import name.huliqing.luoying.Config;
import name.huliqing.luoying.Factory;
import name.huliqing.luoying.constants.SkillConstants;
import name.huliqing.luoying.data.SkillData;
import name.huliqing.luoying.data.ModuleData;
import name.huliqing.luoying.layer.service.SkillService;
import name.huliqing.luoying.object.Loader;
import name.huliqing.luoying.object.attribute.Attribute;
import name.huliqing.luoying.object.attribute.BooleanAttribute;
import name.huliqing.luoying.object.attribute.ValueChangeListener;
import name.huliqing.luoying.object.entity.Entity;
import name.huliqing.luoying.object.skill.Skill;

/**
 * @author huliqing
 */
public class SkillModule extends AbstractModule implements ValueChangeListener<Boolean>{
    private static final Logger LOG = Logger.getLogger(SkillModule.class.getName());
    private final SkillService skillService = Factory.get(SkillService.class);
    
    // 被锁定的技能列表，二进制表示，其中每1个二进制位表示一个技能类型。
    // 如果指定的位为1，则表示这个技能类型被锁定，则这类技能将不能执行。
    // 默认值0表示没有任何锁定。
    private long lockedSkillTags;
    
    // 默认的技能: “空闲”"等待"，“受伤”，“死亡”。。
    private long idleSkillTags;
    private long waitSkillTags;
    private long walkSkillTags;
    private long runSkillTags;
    private long hurtSkillTags;
    private long deadSkillTags;
    
    // 绑定角色的“死亡”属性，当角色在死亡的时候自动执行死亡技能
    private String bindDeadAttribute;
    
    // ---- inner
    private final Control updateControl = new AdapterControl() {
        @Override
        public void update(float tpf) {skillUpdate(tpf);}
    };
    
    // 所有可用的技能处理器
    private final List<Skill> skills = new ArrayList<Skill>();
    // 一个map,用于优化获取查找技能的速度，skillMap中的内容与skills中的是一样的。
    private final Map<String, Skill> skillMap = new HashMap<String, Skill>();
    // 临听角色技能的执行和结束 
    private List<SkillListener> skillListeners;
    private List<SkillPlayListener> skillPlayListeners;
    
    // 当前正在执行的技能列表,
    // 如果runningSkills都执行完毕,则清空.但是lastSkill仍保持最近刚刚执行的技能的引用.
    private final SafeArrayList<Skill> playingSkills = new SafeArrayList<Skill>(Skill.class);
    // 当前正在运行的所有技能的类型，其中每一个二进制位表示一个技能标记。
    private long playingSkillTags;
    // 当前正在执行中的技能中优先级最高的值。
    private int playingPriorMax = Integer.MIN_VALUE;
    
    // 最近一个执行的技能,这个技能可能正在执行，也可能已经停止。
    private Skill lastSkill;

    private BooleanAttribute deadAttribute;
    
    @Override
    public void setData(ModuleData data) {
        super.setData(data);
        lockedSkillTags = data.getAsLong("lockedSkillTags", 0);
        idleSkillTags = skillService.convertSkillTags(data.getAsArray("idleSkillTags"));
        waitSkillTags = skillService.convertSkillTags(data.getAsArray("waitSkillTags"));
        walkSkillTags = skillService.convertSkillTags(data.getAsArray("walkSkillTags"));
        runSkillTags = skillService.convertSkillTags(data.getAsArray("runSkillTags"));
        hurtSkillTags = skillService.convertSkillTags(data.getAsArray("hurtSkillTags"));
        deadSkillTags = skillService.convertSkillTags(data.getAsArray("deadSkillTags"));
        bindDeadAttribute = data.getAsString("bindDeadAttribute");
    }
    
    @Override
    public void updateDatas() {
        data.setAttribute("lockedSkillTags", lockedSkillTags);
    }

    @Override
    public void initialize(Entity actor) {
        super.initialize(actor);
        
        // 技能的更新支持
        this.entity.getSpatial().addControl(updateControl);
        
        // 载入技能
        List<SkillData> skillDatas = actor.getData().getObjectDatas(SkillData.class, null);
        if (skillDatas != null && !skillDatas.isEmpty()) {
            for (SkillData sd : skillDatas) {
                addSkill((Skill) Loader.load(sd));
            }
        }
        
        // 监听角色是否死亡或复活
        if (bindDeadAttribute != null) {
            deadAttribute = entity.getAttributeManager().getAttribute(bindDeadAttribute, BooleanAttribute.class);
            if (deadAttribute != null) {
                deadAttribute.addListener(this);
            }
        }
    }
    
    private void skillUpdate(float tpf) {
        // 更新并尝试移除已经结束的技能
        if (!playingSkills.isEmpty()) {

            int oldSize = playingSkills.size();
            for (Skill skill : playingSkills.getArray()) {
                if (skill.isEnd()) {
                    playingSkills.remove(skill);
                    skill.cleanup();
                    // 执行侦听器
                    if (skillPlayListeners != null && !skillPlayListeners.isEmpty()) {
                        for (int i = 0; i < skillPlayListeners.size(); i++) {
                            skillPlayListeners.get(i).onSkillEnd(skill);
                        }
                    }
                } else {
                    skill.update(tpf);
                }
            }
            
            // 如果有技能执行完了，并被移除了.
            // 1.重新缓存runningSkillTags
            // 2.重新缓存技能中最高优先级的值。
            // 3.修复、重启部分被覆盖的动画通道的动画，比如在走动时取武器后双手应该重新回到走动时的协调运动。
            if (playingSkills.size() != oldSize) {
                playingSkillTags = 0;
                playingPriorMax = Integer.MIN_VALUE;
                for (Skill playSkill : playingSkills.getArray()) {
                    playingSkillTags |= playSkill.getData().getTags();
                    if (playSkill.getData().getPrior() > playingPriorMax) {
                        playingPriorMax = playSkill.getData().getPrior();
                    }
                    playSkill.restoreAnimation();
                }
            }
            
//            if (Config.debug) {
//                Log.get(getClass()).log(Level.INFO, "skillProcessor playingSkills.size={0}", playingSkills.size());
//            }
        }
    }
    
    @Override
    public void cleanup() {
        for (Skill skill : playingSkills.getArray()) {
            if (!skill.isEnd()) {
                skill.cleanup();
            }
        }
        playingSkills.clear();
        skills.clear();
        skillMap.clear();
        playingSkillTags = 0;
        entity.getSpatial().removeControl(updateControl);
        if (deadAttribute != null) {
            deadAttribute.removeListener(this);
        }
        super.cleanup();
    }

    @Override
    public void onValueChanged(Attribute attribute) {
        if (attribute == deadAttribute) {
            Skill playSkill;
            if (deadAttribute.getValue()) {
                playSkill = getSkillByTags(deadSkillTags);
            } else {
                playSkill = getSkillByTags(waitSkillTags);
            }
            if (playSkill != null) {
                playSkill(playSkill, false, null);
            }
        }
    }
    
    // bak20160930
//    /**
//     * 检查技能在当前状态下是否可以执行，如果返回值为 {@link SkillConstants#STATE_OK} 则表示可以执行，
//     * 否则不能执行。
//     * @param skill
//     * @return 
//     */
//    public int checkStateCode(Skill skill) {
//        if (skill == null) {
//            return SkillConstants.STATE_UNDEFINE;
//        }
//        if (skill.getActor() == null) {
//            skill.setActor(actor);
//        }
//        
//        SkillData skillData = skill.getData();
//        
//        // 如果技能被锁定中，则不能执行
//        if (isLockedSkillTags(skillData.getTags())) {
//            return SkillConstants.STATE_SKILL_LOCKED;
//        }
//        
//        // 如果新技能自身判断不能执行，例如加血技能或许就不可能给敌军执行。
//        // 有很多特殊技能是不能对一些特定目标执行的，所以这里需要包含技能自身的判断
//        int stateCode = skill.checkState();
//        if (stateCode != SkillConstants.STATE_OK) {
//            return stateCode;
//        }
//        
//        // 通过钩子来判断是否可以执行, 如果有一个钩子返回不允许执行则后面不再需要判断。
//        if (skillPlayListeners != null && !skillPlayListeners.isEmpty()) {
//            for (SkillPlayListener sl : skillPlayListeners) {
//                if (!sl.onSkillHookCheck(skill)) {
//                    return SkillConstants.STATE_HOOK;
//                }
//            }
//        }
//        
//        // 判断正在执行中的所有技能，如果“正在执行”中的所有技能都可以被覆盖或打断后执行，
//        // 则不需要再判断技能优先级如果其中有任何一个即不能被覆盖，并且也不能被打断，
//        // 则需要判断技能优先级
//        boolean allCanOverlapOrInterrupt = true;
//        long overlaps = skillData.getOverlapTags();
//        long interrupts = skillData.getInterruptTags();
//        for (Skill runSkill : playingSkills.getArray()) {
//            if ((overlaps & runSkill.getData().getTags()) == 0 && (interrupts & runSkill.getData().getTags()) == 0) {
//                allCanOverlapOrInterrupt = false;
//                break;
//            }
//        }
//        if (allCanOverlapOrInterrupt || skillData.getPrior() > playingPriorMax) {
//            return SkillConstants.STATE_OK;
//        }
//        
//        return SkillConstants.STATE_CAN_NOT_INTERRUPT;
//    }
    
    /**
     * 检查技能在当前状态下是否可以执行，如果返回值为 {@link SkillConstants#STATE_OK} 则表示可以执行，
     * 否则不能执行。
     * @param skill
     * @return 
     */
    public int checkStateCode(Skill skill) {
        if (skill == null) {
            return SkillConstants.STATE_SKILL_NOT_FOUND;
        }

        if (skill.getActor() == null) {
            skill.setActor(entity);
        }
        
        SkillData skillData = skill.getData();
        
        // 如果技能被锁定中，则不能执行
        if (isLockedSkillTags(skillData.getTags())) {
            return SkillConstants.STATE_SKILL_LOCKED;
        }
        
        // 如果新技能自身判断不能执行，例如加血技能或许就不可能给敌军执行。
        // 有很多特殊技能是不能对一些特定目标执行的，所以这里需要包含技能自身的判断
        int stateCode = skill.checkState();
        if (stateCode != SkillConstants.STATE_OK) {
            return stateCode;
        }
        
        // 通过钩子来判断是否可以执行, 如果有一个钩子返回不允许执行则后面不再需要判断。
        if (skillPlayListeners != null && !skillPlayListeners.isEmpty()) {
            for (SkillPlayListener sl : skillPlayListeners) {
                if (!sl.onSkillHookCheck(skill)) {
                    return SkillConstants.STATE_HOOK;
                }
            }
        }
        
        // 技能优先级较高可以直接运行
        if ( skillData.getPrior() > playingPriorMax) {
            return SkillConstants.STATE_OK;
        }
        
        // 判断正在执行中的所有技能，如果“正在执行”中的所有技能都可以被覆盖或打断后执行，
        // 则不需要再判断技能优先级如果其中有任何一个即不能被覆盖，并且也不能被打断，
        // 则需要判断技能优先级
        boolean allCanOverlapOrInterrupt = true;
        long overlaps = skillData.getOverlapTags();
        long interrupts = skillData.getInterruptTags();
        for (Skill runSkill : playingSkills.getArray()) {
            if ((overlaps & runSkill.getData().getTags()) == 0 && (interrupts & runSkill.getData().getTags()) == 0) {
                allCanOverlapOrInterrupt = false;
                break;
            }
        }
        if (allCanOverlapOrInterrupt) {
            return SkillConstants.STATE_OK;
        }
        
        return SkillConstants.STATE_CAN_NOT_INTERRUPT;
    }
    
    /**
     * 从当前正在运行的技能中找出不希望被newSkill中断的技能id(唯一ID)， 如果没有找到则返回null或空列表
     * @param newSkill
     * @return 
     */
    public List<Long> checkNotWantInterruptSkills(Skill newSkill) {
        // 如果执行中的技能不希望被newSkill中断则不中断。
        if (playingSkills.isEmpty()) {
            return null;
        }
        List<Long> result = null;
        for (Skill s : playingSkills) {
            if (!s.canInterruptBySkill(newSkill)) {
                if (result == null) {
                    result = new ArrayList<Long>(3);
                }
                result.add(s.getData().getUniqueId());
            }
        }
        return result;   
    }
    
    /**
     * 执行技能，如果成功执行则返回true,否则返回false, <br>
     * 在执行技能之前可以通过 {@link #checkStateCode(Skill) }来查询当前状态下技能是否可以执行。<br>
     * 如果需要强制执行技能，则可以将参数force设置为true,这可以保证技能始终执行。notWantInterruptSkills参数用来指定
     * 哪些特定的正在运行的技能不希望被中断。
     * @param newSkill
     * @param force
     * @param notWantInterruptSkills 指定一些特定的技能不希望被中断，这是一个技能唯一ID列表，可为null.
     * @return 
     */
    public boolean playSkill(Skill newSkill, boolean force, List<Long> notWantInterruptSkills) {
        if (force || checkStateCode(newSkill) == SkillConstants.STATE_OK) {
            playSkillInner(newSkill, notWantInterruptSkills);
            return true;
        }
        return false;
    }
    
    /**
     * 强制执行一个技能,这个方法是强制执行的，如果需要判断技能是否可以合理执行,可以调用 
     * {@link #checkStateCode(Skill, boolean) }来进行判断。<br>
     * 执行逻辑是这样的：<br>
     * 1.如果当前没有任何正在执行的技能则直接执行新技能，否则继续步骤2.<br>
     * 2.把当前正在执行的技能中所有可以重叠执行的进行保留，其余的强制打断。<br>
     * 3.执行新技能。<br>
     * @param newSkill 
     * @param wantNotInterruptSkills 希望不被打断的技能id列表(正在运行中的技能)。
     */
    private void playSkillInner(Skill newSkill, List<Long> notWantInterruptSkills) {
        // 1.如果当前没有任何正在执行的技能则直接执行技能
        if (playingSkills.isEmpty()) {
            startNewSkill(newSkill);
            return;
        }
        
        // 这个方法是强制执行的
        long overlaps = newSkill.getData().getOverlapTags();
        long interrupts = newSkill.getData().getInterruptTags();
        for (Skill playSkill : playingSkills.getArray()) {
            // 由newSkill指定的要覆盖的，则优先使用覆盖，即不要中断。
            if ((overlaps & playSkill.getData().getTags()) != 0) {
                continue;
            }
            // 由newSkill指定的要强制中断的，一定要中断
            if ((interrupts & playSkill.getData().getTags()) != 0) {
                playSkill.cleanup();
                continue;
            }
            // 除了"要求强制中断(上面)"的技能之外，如果指定了一些不希望中断的技能，则这些技能不应该中断(doNotInterruptSkills)
            if (notWantInterruptSkills != null && notWantInterruptSkills.contains(playSkill.getData().getUniqueId())) {
                LOG.log(Level.INFO, "Donot interrupt skill {0}", playSkill.getData().getId());
                continue;
            }
            // 其余一切都要中断
            playSkill.cleanup();
        }
        startNewSkill(newSkill);
    }
    
    private void startNewSkill(Skill newSkill) {
        // 执行技能
        lastSkill = newSkill;
        if (lastSkill.getActor() == null) {
            lastSkill.setActor(entity);
        }
        lastSkill.initialize();
        
//        if (Config.debug) {
//            LOG.log(Level.INFO, "startNewSkill, actor={0}, newSkill={1}"
//                    , new Object[] {lastSkill.getActor().getData().getId(), lastSkill.getData().getId()});
//        }
        
        // 记录当前正在运行的所有技能类型
        if (!playingSkills.contains(lastSkill)) {
            playingSkills.add(lastSkill);
            playingSkillTags |= lastSkill.getData().getTags();
            // 更新当前playing中所有技能的最高优先级的值。
            if (newSkill.getData().getPrior() > playingPriorMax) {
                playingPriorMax = newSkill.getData().getPrior();
            }
        }
        
        // 执行侦听器
        if (skillPlayListeners != null && !skillPlayListeners.isEmpty()) {
            for (int i = 0; i < skillPlayListeners.size(); i++) {
                skillPlayListeners.get(i).onSkillStart(lastSkill);
            }
        }
    }
    
    /**
     * 添加一个新技能给角色,如果相同ID的技能已经存在，则该方法什么也不会处理。
     * @param skill 
     */
    public void addSkill(Skill skill) {
        if (skills.contains(skill))
            return;
        
        skill.setActor(entity);
        skills.add(skill);
        skillMap.put(skill.getData().getId(), skill);
        entity.getData().addObjectData(skill.getData());
        
        // 通知侦听器
        if (skillListeners != null) {
            for (int i = 0; i < skillListeners.size(); i++) {
                skillListeners.get(i).onSkillAdded(entity, skill);
            }
        }
    }
    
    /**
     * 从角色身上移除一个技能，注：被移除的技能必须是已经存在于角色身上的技能实例，
     * 否则该方法什么也不做，并返回false.<BR>
     * 使用{@link #getSkill(java.lang.String) } 来确保从当前角色身上获得一个存在的技能实例。
     * @param skill
     * @return 
     */
    public boolean removeSkill(Skill skill) {
        if (!skills.contains(skill)) 
            return false;
        
        skills.remove(skill);
        skillMap.remove(skill.getData().getId());
        entity.getData().removeObjectData(skill.getData());
        skill.cleanup();
        
        // 通知侦听器
        if (skillListeners != null) {
            for (int i = 0; i < skillListeners.size(); i++) {
                skillListeners.get(i).onSkillRemoved(entity, skill);
            }
        }
        return true;
    }
    
    /**
     * 从角色身上获取一个技能，如果角色没有指定ID的技能则返回null.
     * @param skillId
     * @return 
     */
    public Skill getSkill(String skillId) {
        return skillMap.get(skillId);
    }
    
    /**
     * 获取角色当前所有的技能，注意：返回的技能列表只能作为<b>只读</b>使用.
     * key=skillId, value=Skill
     * @return 
     */
    public List<Skill> getSkills() {
        return Collections.unmodifiableList(skills);
    }
    
    /**
     * 找出指定的标记的技能，第一个找到的将被直接返回。
     * @param skillTags
     * @return 
     */
    private Skill getSkillByTags(long skillTags) {
        for (Skill s : skills) {
            if ((s.getData().getTags() & skillTags) != 0) {
                return s;
            }
        }
        return null;
    }
    
    /**
     * 查找拥有指定tags的所有技能。
     * @param skillTags
     * @param store 存放结果
     * @return 
     */
    public List<Skill> getSkillByTags(long skillTags, List<Skill> store) {
        if (store == null) {
            store = new ArrayList<Skill>();
        }
        if (skills == null) {
            return store;
        }
        for (Skill s : skills) {
            if ((s.getData().getTags() & skillTags) != 0) {
                store.add(s);
            }
        }
        return store;
    }
    
    /**
     * 获取角色的“空闲”技能
     * @param store
     * @return 
     */
    public List<Skill> getSkillIdle(List<Skill> store) {
        return getSkillByTags(idleSkillTags, store);
    }
    
    /**
     * 获取“等待”的技能
     * @param store
     * @return 
     */
    public List<Skill> getSkillWait(List<Skill> store) {
        return getSkillByTags(waitSkillTags, store);
    }
    
    /**
     * 获取“步行”技能
     * @param store
     * @return 
     */
    public List<Skill> getSkillWalk(List<Skill> store) {
        return getSkillByTags(walkSkillTags, store);
    }
    
    /**
     * 获取“跑步”技能
     * @param store
     * @return 
     */
    public List<Skill> getSkillRun(List<Skill> store) {
        return getSkillByTags(runSkillTags, store);
    }
    
    /**
     * 获取“受伤”的技能，
     * @param store
     * @return 
     */
    public List<Skill> getSkillHurt(List<Skill> store) {
        return getSkillByTags(hurtSkillTags, store);
    }
    
    /**
     * 获取“死亡”的技能
     * @param store
     * @return 
     */
    public List<Skill> getSkillDead(List<Skill> store) {
        return getSkillByTags(deadSkillTags, store);
    }
    
    /**
     * 添加技能侦听器
     * @param skillListener 
     */
    public void addSkillListener(SkillListener skillListener) {
        if (skillListeners == null) {
            skillListeners = new ArrayList<SkillListener>();
        }
        if (!skillListeners.contains(skillListener)) {
            skillListeners.add(skillListener);
        }
    }
    
    /**
     * 移除技能侦听器
     * @param skillListener
     * @return 
     */
    public boolean removeSkillListener(SkillListener skillListener) {
        return skillListeners != null && skillListeners.remove(skillListener);
    }
    
    /**
     * 添加技能"执行“侦听器
     * @param skillPlayListener 
     */
    public void addSkillPlayListener(SkillPlayListener skillPlayListener) {
        if (Config.debug) {
            LOG.log(Level.INFO, "addSkillPlayListener, actor={0}, skillPlayListener={1}"
                    , new Object[] {entity.getData().getId(), skillPlayListener});
        }
        if (skillPlayListeners == null) {
            skillPlayListeners = new ArrayList<SkillPlayListener>();
        }
        if (!skillPlayListeners.contains(skillPlayListener)) {
            skillPlayListeners.add(skillPlayListener);
        }
    }
    
    /**
     * 移除技能"执行"侦听器
     * @param skillPlayListener
     * @return 
     */
    public boolean removeSkillPlayListener(SkillPlayListener skillPlayListener) {
        return skillPlayListeners != null && skillPlayListeners.remove(skillPlayListener);
    }
    
    /**
     * 获取最近一个执行的技能，如果没有执行过任何技能则返回null.<br>
     * 注：返回的技能有可能正在执行，也有可能已经结束。
     * @return 
     */
    public Skill getLastSkill() {
        return lastSkill;
    }
    
    /**
     * 获取当前正在执行的所有技能。
     * @return 
     */
    public List<Skill> getPlayingSkills() {
        return playingSkills;
    }
    
    /**
     * 获取当前正在执行的技能标记(tags)，返回值中每个二进制位表示一个技能(标记),<br>
     * 如果没有正在执行的任何技能则该值会返回 0. <br>
     * 通过 {@link SkillTagFactory#toStringTags(long) } 来查看tag的字符串描述
     * @return 
     * @see SkillTagFactory#toStringTags(long) 
     */
    public long getPlayingSkillTags() {
        return playingSkillTags;
    }

    /**
     * 获取技能的锁定状态，返回的整数中每一个二进制位表示一个被锁定的技能标记(tag)
     * @return 
     */
    public long getLockedSkillTags() {
        return lockedSkillTags;
    }
    
    /**
     * 判断技能标记是否被锁定, 如果skillTags中存在<b>任何一个</b>标记被锁定则该方法将返回true.
     * @param skillTags
     * @return 
     */
    public boolean isLockedSkillTags(long skillTags) {
        return (lockedSkillTags & skillTags) != 0;
    }

    /**
     * 锁定指定技能标记的技能, 被锁定后的技能将不能执行。
     * @param skillTags 
     * @see #getLockedSkillTags() 
     */
    public void lockSkillTags(long skillTags) {
        this.lockedSkillTags |= skillTags;
        updateDatas();
    }
    
    /**
     * 解锁指定技能标记的技能。 
     * @param skillTags 
     */
    public void unlockSkillTags(long skillTags) {
        lockedSkillTags &= ~skillTags;
        updateDatas();
    }
        
    /**
     * 判断skillTags标记的技能是否正在执行。如果skillTags标记的技能中有<b>任何一个</b>正在执行则该方法返回true.
     * @param skillTags
     * @return 
     */
    public boolean isPlayingSkill(long skillTags) {
        return (playingSkillTags & skillTags) != 0;
    }
    
    /**
     * 判断角色是否处于等待状态
     * @return 
     */
    public boolean isWaiting() {
        return lastSkill == null
                || (lastSkill.getData().getTags() & waitSkillTags) != 0
                || (playingSkillTags & waitSkillTags) != 0;
    }
    
    /**
     * 判断角色是否处于“步行 ”状态
     * @return 
     */
    public boolean isWalking() {
        return (playingSkillTags & walkSkillTags) != 0;
    }
    
    /**
     * 判断角色是否处于“跑步”状态
     * @return 
     */
    public boolean isRunning() {
        return (playingSkillTags & runSkillTags) != 0;
    }
}