/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package name.huliqing.ly.layer.service;

import name.huliqing.ly.data.env.EnvData;
import name.huliqing.ly.xml.DataFactory;
import name.huliqing.ly.object.env.Env;

/**
 *
 * @author huliqing
 */
public class EnvServiceImpl implements EnvService {

    @Override
    public void inject() {
        // ignore
    }

    @Override
    public Env loadEnv(String envId) {
        EnvData envData = DataFactory.createData(envId);
        return loadEnv(envData);
    }

    @Override
    public EnvData loadEnvData(String envId) {
        return DataFactory.createData(envId);
    }

    @Override
    public Env loadEnv(EnvData envData) {
        return DataFactory.createProcessor(envData);
    }
    
}