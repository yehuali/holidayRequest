package org.flowable.demo;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;

public class HolidayRequest {
    public static void main(String[] args) {
        /**
         * 实例化ProcessEngine实例
         *  --> 这是一个线程安全的对象，您通常只需在应用程序中实例化一次
         *  ProcessEngineConfiguration : 允许配置和调整设置的流程引擎
         *     --->支持XML文件创建以及编程方式创建
         *     1.在spring环境中，使用SpringProcessEngineConfiguration
         */
        ProcessEngineConfiguration cfg = new StandaloneProcessEngineConfiguration()
                .setJdbcUrl("jdbc:h2:mem:flowable;DB_CLOSE_DELAY=-1")
                .setJdbcUsername("sa")
                .setJdbcPassword("")
                .setJdbcDriver("org.h2.Driver")
                /**
                 * 据库中尚不存在数据库模式时创建数据库模式
                 *  -- Flowable附带一组SQL文件，可用于手动创建包含所有表的数据库模式
                 */
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

        ProcessEngine processEngine = cfg.buildProcessEngine();

    }
}
