/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.core.object.module;

import name.huliqing.core.data.ModuleData;
import name.huliqing.core.object.actor.Actor;
import name.huliqing.core.xml.DataProcessor;

/**
 * Module是用来扩展角色功能的,一个角色可以拥有一个至无穷多个的扩展模块，所有角色扩展模块都应该实现这个
 * 接口.
 * @author huliqing
 * @param <T>
 */
public interface Module<T extends ModuleData> extends DataProcessor<T>{

    @Override
    public void setData(T data);

    @Override
    public T getData();
    
    /**
     * 初始化模块
     * @param actor
     */
    void initialize(Actor actor);
    
    /**
     * 判断模块是否已经初始化
     * @return 
     */
    boolean isInitialized();
    
    /**
     * 清理模块
     */
    void cleanup();
    
}