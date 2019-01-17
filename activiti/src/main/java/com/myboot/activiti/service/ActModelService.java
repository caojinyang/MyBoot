package com.myboot.activiti.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.myboot.activiti.domain.ModelVO;
import org.activiti.engine.repository.Model;
import org.apache.batik.transcoder.TranscoderException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * create by caojy on 2019/1/10
 */
public interface ActModelService {

    List<Model> selectModelList(ModelVO modelVo);

    ObjectNode selectWrapModelById(String id);

    Model selectModelById(String id);

    String saveModel(Model model) throws UnsupportedEncodingException;

    void update(Model model, String json_xml, String svg_xml) throws IOException, TranscoderException;

    String deleteModel(String id);

    String deleteModels(String ids);

    String deployProcess(String modelId) throws IOException;

    byte[] getModelEditorSource(String id);

}
