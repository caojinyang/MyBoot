package com.myboot.activiti.service;

import com.myboot.activiti.domain.TaskVO;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * create by caojy on 2019/1/10
 */
public interface ActTaskService {

    List<TaskVO> selectTaskList(TaskVO taskVO);

    List<TaskVO> selectTodoTask(TaskVO taskVO);

    List<TaskVO>  selectInvolvedTask(TaskVO taskVO);

    List<TaskVO>  selectArchivedTask(TaskVO taskVO);

    /** key 组名  value 代办任务*/
    Map<String, List<TaskVO>> selectGroupQueueTask(TaskVO taskVO);

    TaskVO  selectOneTask(String taskId);

    void completeTask(String taskId, Map<String, Object> variables);

    void complete(String taskId, String procInsId, String comment, String title, Map<String, Object> vars);


    String startProcess(String procDefKey, String businessTable, String businessId, String title, String userId, Map<String, Object> vars);

    String getFormKey(String procDefId, String taskDefKey);

    InputStream traceTaskPhoto(String processDefinitionId, String executionId);


    List<TaskVO> selectFinishedTask(TaskVO task);

}
