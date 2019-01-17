package com.myboot.web.controller.activiti;


import com.myboot.activiti.domain.TaskVO;
import com.myboot.activiti.service.ActTaskService;
import com.myboot.common.annotation.Log;
import com.myboot.common.base.AjaxResult;
import com.myboot.common.enums.BusinessType;
import com.myboot.common.page.TableDataInfo;
import com.myboot.framework.util.ShiroUtils;
import com.myboot.framework.web.base.BaseController;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/***
 * 代办任务
 */
@RequestMapping("activiti/task")
@Controller
public class TaskController extends BaseController {

    @Autowired
    ActTaskService actTaskService;

    private String prefix = "activiti/task";
    /**
     * 进入代办任务页
     * @return
     */
    @RequiresPermissions("activiti:task:view")
    @GetMapping
    public   String task() {
        return prefix + "/tasks";
    }

    /**
     * 查看我的任务  admin 查看所有任务
     * @return
     */
    @Log(title = "查询任务", businessType = BusinessType.OTHER)
    @RequiresPermissions("activiti:task:view")
    @RequestMapping("/list")
    @ResponseBody
    TableDataInfo list(TaskVO taskVO) {
        startPage(taskVO);
        if ("admin".equalsIgnoreCase(ShiroUtils.getLoginName())) {
            List<TaskVO> taskVOS = actTaskService.selectTaskList(taskVO);
            return getDataTable(taskVOS);
        }
        taskVO.setAssignee(String.valueOf(ShiroUtils.getUserId()));
        List<TaskVO> taskVOS = actTaskService.selectTaskList(taskVO);
        TableDataInfo dataTable = getDataTable(taskVOS);
        dataTable.setTotal(taskVO.getCount());
        return dataTable;
    }

    @RequiresPermissions("activiti:task:add")
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }


    @RequiresPermissions("activiti:task:edit")
    @PostMapping("/edit/{taskId}")
    @ResponseBody
    public String edit(@PathVariable("taskId") String taskId, ModelMap map) {
        TaskVO taskVO = actTaskService.selectOneTask(taskId);
        map.put("task", taskVO);
        return prefix + "/edit";
    }


    @Log(title = "更新任务", businessType = BusinessType.OTHER)
    @RequiresPermissions("activiti:task:edit")
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(TaskVO task, ModelMap map) {
        actTaskService.completeTask(task.getId(), map);
        return toAjax(1);
    }

    @Log(title = "代办任务", businessType = BusinessType.OTHER)
    @RequiresPermissions("activiti:task:view")
    @PostMapping("/todo")
    @ResponseBody
    public TableDataInfo todo(TaskVO task,ModelMap map) {
        startPage(task);
        task.setAssignee(ShiroUtils.getLoginName());
        List<TaskVO> taskVOs = actTaskService.selectTodoTask(task);
        TableDataInfo dataTable = getDataTable(taskVOs);
        dataTable.setTotal(task.getCount());
        return dataTable;
    }
    @Log(title = "受邀任务", businessType = BusinessType.OTHER)
    @RequiresPermissions("activiti:task:view")
    @PostMapping("/involved")
    @ResponseBody
    public TableDataInfo selectInvolvedTask(TaskVO task,ModelMap map) {
        startPage(task);
        task.setInvolvedUser(ShiroUtils.getLoginName());
        List<TaskVO> taskVOs = actTaskService.selectInvolvedTask(task);
        TableDataInfo dataTable = getDataTable(taskVOs);
        dataTable.setTotal(task.getCount());
        return dataTable;
    }
    @Log(title = "归档任务", businessType = BusinessType.OTHER)
    @RequiresPermissions("activiti:task:view")
    @PostMapping("/archived")
    @ResponseBody
    public TableDataInfo selectArchivedTask(TaskVO task,ModelMap map) {
        startPage(task);
        task.setOwner(ShiroUtils.getLoginName());
        List<TaskVO> taskVOs = actTaskService.selectArchivedTask(task);
        TableDataInfo dataTable = getDataTable(taskVOs);
        dataTable.setTotal(task.getCount());
        return dataTable;
    }

    @Log(title = "查询完成的任务", businessType = BusinessType.OTHER)
    @RequiresPermissions("activiti:task:view")
    @PostMapping("/finishedTask")
    public TableDataInfo finishedTask(TaskVO task, ModelMap map) {
        startPage(task);
        task.setOwner(ShiroUtils.getLoginName());
        List<TaskVO> taskVOs = actTaskService.selectFinishedTask(task);
        TableDataInfo dataTable = getDataTable(taskVOs);
        dataTable.setTotal(task.getCount());
        return dataTable;
    }


    @GetMapping("/form/{procDefId}")
    public void startForm(@PathVariable("procDefId") String procDefId, HttpServletResponse response) throws IOException {
        String formKey = actTaskService.getFormKey(procDefId, null);
        response.sendRedirect(formKey);
    }

    @GetMapping("/form/{procDefId}/{taskId}")
    public void form(@PathVariable("procDefId") String procDefId, @PathVariable("taskId") String taskId, HttpServletResponse response) throws IOException {
        // 获取流程XML上的表单KEY
        String formKey = actTaskService.getFormKey(procDefId, taskId);

        response.sendRedirect(formKey + "/" + taskId);
    }



    /**
     * 读取带跟踪的图片
     */
    @RequestMapping(value = "/trace/photo/{procDefId}/{execId}")
    public void traceTaskPhoto(@PathVariable("procDefId") String procDefId, @PathVariable("execId") String execId, HttpServletResponse response) throws Exception {
        InputStream imageStream = actTaskService.traceTaskPhoto(procDefId, execId);
        // 输出资源内容到相应对象
        byte[] b = new byte[1024];
        int len;
        while ((len = imageStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }


}
