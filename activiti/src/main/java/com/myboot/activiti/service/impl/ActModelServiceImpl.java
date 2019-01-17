package com.myboot.activiti.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.myboot.activiti.domain.ModelVO;
import com.myboot.activiti.service.ActModelService;
import com.myboot.common.utils.StringUtils;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.activiti.rest.editor.model.ModelEditorJsonRestResource;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_ID;
import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_NAME;
/**
 * create by caojy on 2019/1/10
 */
@Service
public class ActModelServiceImpl implements ActModelService {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ModelEditorJsonRestResource.class);


    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public List<Model> selectModelList(ModelVO modelVo) {
        int[] paging = modelVo.paging();
        ModelQuery modelQuery = repositoryService.createModelQuery();

        modelVo.setCount(modelQuery.count());
        return modelQuery.orderByModelId().desc().listPage(paging[0], paging[1]);

    }

    @Override
    public String saveModel(Model model) throws UnsupportedEncodingException {
        // //初始化一个空模型
        // Model model = repositoryService.newModel();
        // //设置一些默认信息
        // String name = "new-process";
        // String description = "";
        // int revision = 1;
        // String key = "process";

        // ObjectNode modelNode = objectMapper.createObjectNode();
        // modelNode.put(ModelDataJsonConstants.MODEL_NAME, name);
        // modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
        // modelNode.put(ModelDataJsonConstants.MODEL_REVISION, revision);
        //
        // model.setName(name);
        // model.setKey(key);
        // model.setMetaInfo(modelNode.toString())


        repositoryService.saveModel(model);
        String id = model.getId();
        //完善ModelEditorSource
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.set("stencilset", stencilSetNode);
        repositoryService.addModelEditorSource(id, editorNode.toString().getBytes("utf-8"));
        return id;
    }

    @Override
    public void update(Model model, String json_xml, String svg_xml) throws IOException, TranscoderException {
        repositoryService.saveModel(model);
        repositoryService.addModelEditorSource(model.getId(), json_xml.getBytes("utf-8"));
        InputStream svgStream = new ByteArrayInputStream(svg_xml.getBytes("utf-8"));
        TranscoderInput input = new TranscoderInput(svgStream);

        PNGTranscoder transcoder = new PNGTranscoder();
        // Setup output
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(outStream);

        // Do the transformation
        transcoder.transcode(input, output);
        final byte[] result = outStream.toByteArray();
        repositoryService.addModelEditorSourceExtra(model.getId(), result);
        outStream.close();
    }

    @Override
    public ObjectNode selectWrapModelById(String modelId) {
        ObjectNode modelNode = null;
        Model model = repositoryService.getModel(modelId);
        if (model != null) {
            try {
                if (StringUtils.isNotEmpty(model.getMetaInfo())) {
                    modelNode = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
                } else {
                    modelNode = objectMapper.createObjectNode();
                    modelNode.put(MODEL_NAME, model.getName());
                }
                modelNode.put(MODEL_ID, model.getId());
                ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(
                        new String(repositoryService.getModelEditorSource(model.getId()), "utf-8"));
                modelNode.set("model", editorJsonNode);

            } catch (Exception e) {
                LOGGER.error("Error creating model JSON", e);
                throw new ActivitiException("Error creating model JSON", e);
            }
        }
        return modelNode;
    }

    @Override
    public Model selectModelById(String id) {
        return repositoryService.getModel(id);
    }

    @Override
    public String deleteModel(String id) {
        repositoryService.deleteModel(id);
        return id;
    }

    @Override
    public String deleteModels(String ids) {
        String[] idlist = StringUtils.split(ids, ",");
        for (String id : idlist) {
            repositoryService.deleteModel(id);
        }
        return ids;
    }

    @Override
    public String deployProcess(String modelId) throws IOException {
        //获取模型
        Model modelData = repositoryService.getModel(modelId);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

        if (bytes == null) {
            return "模型数据为空，请先设计流程并成功保存，再进行发布。";
        }

        JsonNode modelNode = new ObjectMapper().readTree(bytes);

        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        if (model.getProcesses().size() == 0) {
            return "数据模型不符要求，请至少设计一条主线流程。";
        }
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

        //发布流程
        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .name(modelData.getName())
                .addString(processName, new String(bpmnBytes, "UTF-8"))
                .deploy();
        modelData.setDeploymentId(deployment.getId());
        repositoryService.saveModel(modelData);
        return "";
    }

    @Override
    public byte[] getModelEditorSource(String id) {
        return repositoryService.getModelEditorSource(id);
    }
}
