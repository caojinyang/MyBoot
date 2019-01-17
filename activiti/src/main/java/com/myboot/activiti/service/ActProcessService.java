package com.myboot.activiti.service;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.myboot.activiti.domain.ProcessVO;
import org.activiti.engine.repository.Model;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * create by caojy on 2019/1/10
 */
public interface ActProcessService {

    Model convertToModel(String procDefId) throws Exception;

    ArrayNode selectProcessDefinition(Map<String, String> variable);

    ArrayNode deployedProcessDefinition(Map<String, String> variable);

    InputStream resourceRead(String id, String resType) throws Exception;

    List<ProcessVO> selectProcessDefinitionList(ProcessVO processVo);

    String deployProcessDefinition(InputStream is, String fileName, String category);

    int deleteDeployment(String id);
    int deleteDeployments(String ids);

}
