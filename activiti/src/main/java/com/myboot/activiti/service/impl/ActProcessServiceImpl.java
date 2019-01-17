package com.myboot.activiti.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.myboot.activiti.domain.ProcessVO;
import com.myboot.activiti.service.ActProcessService;
import com.myboot.activiti.utils.ObjectNodeConverter;
import com.myboot.common.utils.StringUtils;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;

/**
 * create by caojy on 2019/1/10
 */
@Service
public class ActProcessServiceImpl implements ActProcessService {

    @Autowired
    RepositoryService repositoryService;
    @Autowired
    RuntimeService runtimeService;
    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private ObjectNodeConverter objectNodeConverter;

    @Override
    public ArrayNode selectProcessDefinition(Map<String, String> variable) {
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();

        // Populate filter-parameters
        if (variable.containsKey("category")) {
            processDefinitionQuery.processDefinitionCategory(variable.get("category"));
        }
        if (variable.containsKey("categoryLike")) {
            processDefinitionQuery.processDefinitionCategoryLike(variable.get("categoryLike"));
        }
        if (variable.containsKey("categoryNotEquals")) {
            processDefinitionQuery.processDefinitionCategoryNotEquals(variable.get("categoryNotEquals"));
        }
        if (variable.containsKey("key")) {
            processDefinitionQuery.processDefinitionKey(variable.get("key"));
        }
        if (variable.containsKey("keyLike")) {
            processDefinitionQuery.processDefinitionKeyLike(variable.get("keyLike"));
        }
        if (variable.containsKey("name")) {
            processDefinitionQuery.processDefinitionName(variable.get("name"));
        }
        if (variable.containsKey("nameLike")) {
            processDefinitionQuery.processDefinitionNameLike(variable.get("nameLike"));
        }
        if (variable.containsKey("resourceName")) {
            processDefinitionQuery.processDefinitionResourceName(variable.get("resourceName"));
        }
        if (variable.containsKey("resourceNameLike")) {
            processDefinitionQuery.processDefinitionResourceNameLike(variable.get("resourceNameLike"));
        }
        if (variable.containsKey("version")) {
            processDefinitionQuery.processDefinitionVersion(Integer.valueOf(variable.get("version")));
        }
        if (variable.containsKey("suspended")) {
            Boolean suspended = Boolean.valueOf(variable.get("suspended"));
            if (suspended != null) {
                if (suspended) {
                    processDefinitionQuery.suspended();
                } else {
                    processDefinitionQuery.active();
                }
            }
        }
        if (variable.containsKey("latest")) {
            Boolean latest = Boolean.valueOf(variable.get("latest"));
            if (latest != null && latest) {
                processDefinitionQuery.latestVersion();
            }
        }
        if (variable.containsKey("deploymentId")) {
            processDefinitionQuery.deploymentId(variable.get("deploymentId"));
        }
        if (variable.containsKey("startableByUser")) {
            processDefinitionQuery.startableByUser(variable.get("startableByUser"));
        }
        if (variable.containsKey("tenantId")) {
            processDefinitionQuery.processDefinitionTenantId(variable.get("tenantId"));
        }
        if (variable.containsKey("tenantIdLike")) {
            processDefinitionQuery.processDefinitionTenantIdLike(variable.get("tenantIdLike"));
        }
        if (variable.containsKey("pageNum") && variable.containsKey("pageSize")) {
            List<ProcessDefinition> processDefinitions = processDefinitionQuery
                    .listPage(Integer.parseInt(variable.get("pageNum")), Integer.parseInt(variable.get("pageSize")));
            return objectNodeConverter.processDefinitions2ArrayNode(processDefinitions);
        }
        return objectNodeConverter.processDefinitions2ArrayNode(processDefinitionQuery.list());
    }


    /**
     * 将流程定义转换成模型
     */
    @Override
    public Model convertToModel(String procDefId) throws Exception {

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefId).singleResult();
        InputStream bpmnStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(),
                processDefinition.getResourceName());
        XMLInputFactory xif = XMLInputFactory.newInstance();
        InputStreamReader in = new InputStreamReader(bpmnStream, "UTF-8");
        XMLStreamReader xtr = xif.createXMLStreamReader(in);
        BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);

        BpmnJsonConverter converter = new BpmnJsonConverter();
        ObjectNode modelNode = converter.convertToJson(bpmnModel);
        org.activiti.engine.repository.Model modelData = repositoryService.newModel();
        modelData.setKey(processDefinition.getKey());
        modelData.setName(processDefinition.getResourceName());
        modelData.setCategory(processDefinition.getCategory());//.getDeploymentId());
        modelData.setDeploymentId(processDefinition.getDeploymentId());
        modelData.setVersion(Integer.parseInt(String.valueOf(repositoryService.createModelQuery().modelKey(modelData.getKey()).count() + 1)));

        ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
        modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, processDefinition.getName());
        modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, modelData.getVersion());
        modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, processDefinition.getDescription());
        modelData.setMetaInfo(modelObjectNode.toString());

        repositoryService.saveModel(modelData);

        repositoryService.addModelEditorSource(modelData.getId(), modelNode.toString().getBytes("utf-8"));

        return modelData;
    }

    /**
     * 查询发布的流程定义
     *
     * @return
     */
    @Override
    public ArrayNode deployedProcessDefinition(Map<String, String> variable) {
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery().latestVersion().active();
        if (variable.containsKey("name")) {
            processDefinitionQuery.processDefinitionName(variable.get("name"));
        }
        if (variable.containsKey("nameLike")) {
            processDefinitionQuery.processDefinitionNameLike(variable.get("nameLike"));
        }
        if (variable.containsKey("pageNum") && variable.containsKey("pageSize")) {
            List<ProcessDefinition> processDefinitions = processDefinitionQuery
                    .listPage(Integer.parseInt(variable.get("pageNum")), Integer.parseInt(variable.get("pageSize")));
            return objectNodeConverter.processDefinitions2ArrayNode(processDefinitions);
        }
        return objectNodeConverter.processDefinitions2ArrayNode(processDefinitionQuery.list());
    }

    @Override
    public InputStream resourceRead(String id, String resType) throws Exception {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(id).singleResult();
        String resourceName = "";
        if (resType.equals("image")) {
            resourceName = processDefinition.getDiagramResourceName();
        } else if (resType.equals("xml")) {
            resourceName = processDefinition.getResourceName();
        }
        InputStream resourceAsStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), resourceName);
        return resourceAsStream;
    }

    @Override
    public List<ProcessVO> selectProcessDefinitionList(ProcessVO processVo) {
        int[] pageing = processVo.paging();
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        if (processVo.getCategory() != null) {
            processDefinitionQuery.processDefinitionCategory(processVo.getCategory());
        }

        if (processVo.getKey() != null) {
            processDefinitionQuery.processDefinitionKey(processVo.getKey());
        }

        if (processVo.getName() != null) {
            processDefinitionQuery.processDefinitionName(processVo.getName());
        }
        if (processVo.getResourceName() != null) {
            processDefinitionQuery.processDefinitionResourceName(processVo.getResourceName());
        }

        if (processVo.getVersion() != null) {
            processDefinitionQuery.processDefinitionVersion(Integer.valueOf(processVo.getVersion()));
        }
        if (processVo.getSuspended() != null) {
            Boolean suspended = Boolean.valueOf(processVo.getSuspended());
            if (suspended != null) {
                if (suspended) {
                    processDefinitionQuery.suspended();
                } else {
                    processDefinitionQuery.active();
                }
            }
        }
        if (processVo.getLatest() != null) {
            Boolean latest = Boolean.valueOf(processVo.getLatest());
            if (latest != null && latest) {
                processDefinitionQuery.latestVersion();
            }
        }
        if (processVo.getDeploymentId() != null) {
            processDefinitionQuery.deploymentId(processVo.getDeploymentId());
        }
        if (processVo.getStartableByUser() != null) {
            processDefinitionQuery.startableByUser(processVo.getStartableByUser());
        }
        if (processVo.getTenantId() != null) {
            processDefinitionQuery.processDefinitionTenantId(processVo.getTenantId());
        }

        processVo.setCount(processDefinitionQuery.count());

        List<ProcessVO> list = processDefinitionQuery.orderByDeploymentId()
                .desc()
                .listPage(pageing[0], pageing[1])
                .stream()
                .map(ProcessVO::new)
                .collect(Collectors.toList());

        return list;
    }

    @Override
    public String deployProcessDefinition(InputStream is, String fileName, String category) {
        Deployment deployment = null;
        String message = "";
        String extension = FilenameUtils.getExtension(fileName);
        if (extension.equals("zip") || extension.equals("bar")) {
            ZipInputStream zip = new ZipInputStream(is);
            deployment = repositoryService.createDeployment().addZipInputStream(zip).deploy();
        } else if (extension.equals("png")) {
            deployment = repositoryService.createDeployment().addInputStream(fileName, is).deploy();
        } else if (fileName.indexOf("bpmn20.xml") != -1) {
            deployment = repositoryService.createDeployment().addInputStream(fileName, is).deploy();
        } else if (extension.equals("bpmn")) { // bpmn扩展名特殊处理，转换为bpmn20.xml
            String baseName = FilenameUtils.getBaseName(fileName);
            deployment = repositoryService.createDeployment().addInputStream(baseName + ".bpmn20.xml", is).deploy();
        } else {
            message = "不支持的文件类型：" + extension;
        }


        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).list();

        // 设置流程分类
        for (ProcessDefinition processDefinition : list) {
            repositoryService.setProcessDefinitionCategory(processDefinition.getId(), category);
            message += "部署成功，流程ID=" + processDefinition.getId() + "<br/>";
        }
        if (list.size() == 0) {
            message = "部署失败，没有流程。";
        }
        return message;
    }

    @Override
    public int deleteDeployment(String id) {
        repositoryService.deleteDeployment(id, true);
        return 1;
    }

    @Override
    public int deleteDeployments(String ids) {
        String[] idarr = StringUtils.split(ids, ",");
        for (String id : idarr) {
            repositoryService.deleteDeployment(id, true);
        }
        return idarr.length;
    }
}
