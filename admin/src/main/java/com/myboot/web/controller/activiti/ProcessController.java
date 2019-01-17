package com.myboot.web.controller.activiti;

import com.myboot.activiti.domain.ProcessVO;
import com.myboot.activiti.service.ActProcessService;
import com.myboot.common.base.AjaxResult;
import com.myboot.common.page.TableDataInfo;
import com.myboot.framework.web.base.BaseController;
import org.activiti.engine.ActivitiException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

@RequestMapping("activiti/process")
@Controller
public class ProcessController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ProcessController.class);

    private String prefix = "activiti/process";
    //
    // @Autowired
    // private RepositoryService repositoryService;

    @Autowired
    private ActProcessService actProcessService;

    // @Autowired
    // private RuntimeService runtimeService;

    /**
     * 进入流程展示页
     */
    @RequiresPermissions("activiti:process:view")
    @RequestMapping
    public  String process() {
        return prefix+"/process";
    }

    @RequiresPermissions("activiti:process:list")
    @RequestMapping("list")
    @ResponseBody
    public TableDataInfo list(ProcessVO processVO) {
        startPage(processVO);
        List<ProcessVO> processDefinitions = actProcessService.selectProcessDefinitionList(processVO);
        TableDataInfo dataTable = getDataTable(processDefinitions);
        dataTable.setTotal(processVO.getCount());
        return dataTable;
    }

    @RequiresPermissions("activiti:process:add")
    @GetMapping("/add")
    public ModelAndView add() {
        return new ModelAndView("/activiti/process/add");
    }

    @RequiresPermissions("activiti:process:save")
    @PostMapping("/save")
    @Transactional(readOnly = false)
    public AjaxResult deploy(String exportDir, @RequestParam String category, @RequestParam("file") MultipartFile file) {
        String fileName = file.getOriginalFilename();
        try {
            InputStream fileInputStream = file.getInputStream();
            String message = actProcessService.deployProcessDefinition(fileInputStream, fileName, category);
            return success(message);
        } catch (Exception e) {
            throw new ActivitiException("部署失败！", e);
        }

        // return error("部署失败！");
    }


    /**
     * 将部署的流程转换为模型
     *
     * @param procDefId
     * @param redirectAttributes
     * @return
     * @throws UnsupportedEncodingException
     * @throws XMLStreamException
     */
    @RequestMapping(value = "/convertToModel/{procDefId}")
    public AjaxResult convertToModel(@PathVariable("procDefId") String procDefId, RedirectAttributes redirectAttributes) throws UnsupportedEncodingException, XMLStreamException {
        org.activiti.engine.repository.Model modelData = null;
        try {
            modelData = actProcessService.convertToModel(procDefId);
            return success("转换模型成功，模型ID=" + modelData.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return success("转换模型失败");
        }

    }

    @RequestMapping(value = "/resource/read/{xml}/{id}")
    public void resourceRead(@PathVariable("xml") String resType, @PathVariable("id") String id, HttpServletResponse response) throws Exception {
        InputStream resourceAsStream = actProcessService.resourceRead(id, resType);
        byte[] b = new byte[1024];
        int len = -1;
        while ((len = resourceAsStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }

    @RequiresPermissions("activiti:process:remove")
    @PostMapping("/remove")
    public AjaxResult remove(String ids) {
        int i = actProcessService.deleteDeployments(ids);
        return success();
    }
}
