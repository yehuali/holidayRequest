package org.flowable.demo;

import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
                .setJdbcUrl("jdbc:mysql://127.0.0.1:3306/flowable?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC")
                .setJdbcUsername("root")
                .setJdbcPassword("3325853")
                .setJdbcDriver("com.mysql.jdbc.Driver")
                /**
                 * 据库中尚不存在数据库模式时创建数据库模式
                 *  -- Flowable附带一组SQL文件，可用于手动创建包含所有表的数据库模式
                 */
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

        ProcessEngine processEngine = cfg.buildProcessEngine();

        //部署流程定义
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource("holiday-request.bpmn20.xml")
                .deploy();

        //API查询引擎
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        System.out.println("Found process definition : " + processDefinition.getName());


        Scanner scanner= new Scanner(System.in);
        System.out.println("Who are you?");
        String employee = scanner.nextLine();

        System.out.println("How many holidays do you want to request?");
        Integer nrOfHolidays = Integer.valueOf(scanner.nextLine());

        System.out.println("Why do you need them?");
        String description = scanner.nextLine();

        //启动流程实例
        RuntimeService runtimeService = processEngine.getRuntimeService();
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("employee", employee);
        variables.put("nrOfHolidays", nrOfHolidays);
        variables.put("description", description);
        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey("holidayRequest", variables);

        /**
         *。我们希望第一个任务是将管理员组和第二个用户任务分配给假期的原始请求者。为此，请将candidateGroups属性添加到第一个任务：
         * <userTask id="approveTask" name="Approve or reject request" flowable:candidateGroups="managers"/>
         * 为了获得实际的任务列表，通过TaskService创建一个TaskQuery，并将查询配置为仅返回manager组的任务
         */
        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();
        System.out.println("You have " + tasks.size() + " tasks:");
        for (int i=0; i<tasks.size(); i++) {
            System.out.println((i+1) + ") " + tasks.get(i).getName());
        }

        /**
         * 使用任务标识符，我们现在可以获取特定的流程实例变量
         */
        System.out.println("Which task would you like to complete?");
        int taskIndex = Integer.valueOf(scanner.nextLine());
        Task task = tasks.get(taskIndex - 1);
        Map<String, Object> processVariables = taskService.getVariables(task.getId());
        System.out.println(processVariables.get("employee") + " wants " +
                processVariables.get("nrOfHolidays") + " of holidays. Do you approve this?");

        /**
         * 用户提交表单，然后将表单中的数据作为流程变量传递
         */
        boolean approved = scanner.nextLine().toLowerCase().equals("y");
        variables = new HashMap<String, Object>();
        variables.put("approved", approved);
        taskService.complete(task.getId(), variables);


        //使用历史数据
        HistoryService historyService = processEngine.getHistoryService();
        List<HistoricActivityInstance> activities =
                historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(processInstance.getId())
                        .finished()
                        .orderByHistoricActivityInstanceEndTime().asc()
                        .list();

        for (HistoricActivityInstance activity : activities) {
            System.out.println(activity.getActivityId() + " took "
                    + activity.getDurationInMillis() + " milliseconds");
        }

    }
}
