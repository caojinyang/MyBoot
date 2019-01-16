package com.caojy.web.controller.activiti;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.caojy.activiti.domain.ModelVO;
import com.caojy.activiti.service.ActModelService;
import com.caojy.common.annotation.Log;
import com.caojy.common.base.AjaxResult;
import com.caojy.common.enums.BusinessType;
import com.caojy.common.page.TableDataInfo;
import com.caojy.framework.web.base.BaseController;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.persistence.entity.ModelEntity;
import org.activiti.engine.repository.Model;
import org.activiti.rest.editor.model.ModelEditorJsonRestResource;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_DESCRIPTION;
import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_NAME;

/**
 * @author bootdo 1992lcg@163.com
 */
@RequestMapping("/activiti")
@Controller
public class ModelController extends BaseController {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ModelEditorJsonRestResource.class);

    private String prefix = "activiti/model";

    @Autowired
    ActModelService actModelService;


    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping(value = "/editor/stencilset", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getStencilset() {
        InputStream stencilsetStream = this.getClass().getClassLoader().getResourceAsStream("stencilset.json");
        try {
            return IOUtils.toString(stencilsetStream, "utf-8");
        } catch (Exception e) {
            throw new ActivitiException("Error while loading stencil set", e);
        }
    }

    /**
     * 进入模型列表页
     *
     * @return
     */
    @RequiresPermissions("activiti:model:view")
    @RequestMapping("/model")
    public String model() {
        return prefix+"/model";
    }

    @RequiresPermissions("activiti:model:list")
    @ResponseBody
    @RequestMapping("/model/list")
    TableDataInfo list(ModelVO  modelVo) {
        startPage(modelVo);
        TableDataInfo dataTable = getDataTable(actModelService.selectModelList(modelVo));
        dataTable.setTotal(modelVo.getCount());
        return dataTable;
    }

    @RequestMapping("/model/add")
    public void newModel(HttpServletResponse response) throws UnsupportedEncodingException {
        ModelEntity model = new ModelEntity();
        String name = "new-process";
        String description = "新的流程";
        int revision = 1;
        String key = "process";
        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(ModelDataJsonConstants.MODEL_NAME, name);
        modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, revision);
        model.setName(name);
        model.setKey(key);
        model.setMetaInfo(modelNode.toString());
        String id = actModelService.saveModel(model);

        try {
            response.sendRedirect("/modeler.html?modelId=" + id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping(value = "/model/{modelId}/json")
    @ResponseBody
    public ObjectNode getEditorJson(@PathVariable String modelId) {
        ObjectNode modelNode = actModelService.selectWrapModelById(modelId);
        return modelNode;
    }


    @RequestMapping("/model/edit/{id}")
    public void edit(HttpServletResponse response, @PathVariable("id") String id) {
        try {
            response.sendRedirect("/modeler.html?modelId=" + id);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Log(title = "删除模型", businessType = BusinessType.DELETE)
    @RequiresPermissions("activiti:model:remove")
    @PostMapping("/model/remove")
    @ResponseBody
    public AjaxResult remove(String ids) {
        actModelService.deleteModels(ids);
        return success();
    }

    /**
     * 发布流程
     *
     * @param id
     * @return
     * @throws Exception
     */
    @Log(title = "发布流程", businessType = BusinessType.UPDATE)
    @RequiresPermissions("activiti:model:deploy")
    @PostMapping("/model/deploy/{id}")
    @ResponseBody
    public AjaxResult deploy(@PathVariable("id") String id) throws Exception {
        String result = actModelService.deployProcess(id);
        if (result.length() > 0) {
            return error(result);
        }
        return success();
    }


    @RequestMapping(value = "/model/{modelId}/save", method = RequestMethod.PUT)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public void saveModel(@PathVariable String modelId, String name, String description, String json_xml, String svg_xml) {
        try {
            Model model = actModelService.selectModelById(modelId);
            ObjectNode modelJson = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
            modelJson.put(MODEL_NAME, name);
            modelJson.put(MODEL_DESCRIPTION, description);
            model.setMetaInfo(modelJson.toString());
            model.setName(name);
            actModelService.update(model, json_xml, svg_xml);
        } catch (Exception e) {
            LOGGER.error("Error saving model", e);
            throw new ActivitiException("Error saving model", e);
        }
    }

    @Log(title = "导出指定模型", businessType = BusinessType.EXPORT)
    @RequiresPermissions("activiti:model:export")
    @RequestMapping("/model/export/{id}")
    public void exportToXml(@PathVariable("id") String id, HttpServletResponse response) {
        try {
            org.activiti.engine.repository.Model modelData = actModelService.selectModelById(id);
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            JsonNode editorNode = new ObjectMapper().readTree(actModelService.getModelEditorSource(modelData.getId()));
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);
            BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
            byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel);

            ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);
            IOUtils.copy(in, response.getOutputStream());
            String filename = bpmnModel.getMainProcess().getId() + ".bpmn20.xml";
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            response.flushBuffer();
        } catch (Exception e) {
            throw new ActivitiException("导出model的xml文件失败，模型ID=" + id, e);
        }
    }

    // @Log(title = "模型导出", businessType = BusinessType.EXPORT)
    // @RequiresPermissions("activiti:model:export")
    // @PostMapping("/model/export")
    // @ResponseBody
    // public AjaxResult export() {
    //     List<Model> list = repositoryService.createModelQuery().list();
    //     ExcelUtil<CustomData> util = new ExcelUtil<>(CustomData.class);
    //     ArrayList<CustomData> lists = new ArrayList<>();
    //     for (Model model : list) {
    //         CustomData customData = new CustomData();
    //         customData.setId(model.getId());
    //         customData.setName(model.getName());
    //         lists.add(customData);
    //     }
    //     return util.exportExcel(lists, "modelData");
    // }

}
