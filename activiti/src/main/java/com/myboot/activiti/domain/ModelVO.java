package com.myboot.activiti.domain;

import com.myboot.common.base.BaseEntity;
import org.activiti.engine.ProcessEngineConfiguration;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * create by caojy on 2019/1/10
 */
public class ModelVO extends BaseEntity {

    private static final long serialVersionUID = 1L;

    protected String id;
    protected int revision = 1;
    protected String name;
    protected String key;
    protected String category;
    protected Date createTime;
    protected Date lastUpdateTime;
    protected Integer version = 1;
    protected String metaInfo;
    protected String deploymentId;
    protected String editorSourceValueId;
    protected String editorSourceExtraValueId;
    protected String tenantId = ProcessEngineConfiguration.NO_TENANT_ID;



    public Object getPersistentState() {
        Map<String, Object> persistentState = new HashMap<String, Object>();
        persistentState.put("name", this.name);
        persistentState.put("key", key);
        persistentState.put("category", this.category);
        persistentState.put("createTime", this.createTime);
        persistentState.put("lastUpdateTime", lastUpdateTime);
        persistentState.put("version", this.version);
        persistentState.put("metaInfo", this.metaInfo);
        persistentState.put("deploymentId", deploymentId);
        persistentState.put("editorSourceValueId", this.editorSourceValueId);
        persistentState.put("editorSourceExtraValueId", this.editorSourceExtraValueId);
        return persistentState;
    }

    // getters and setters //////////////////////////////////////////////////////

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(String metaInfo) {
        this.metaInfo = metaInfo;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getEditorSourceValueId() {
        return editorSourceValueId;
    }

    public void setEditorSourceValueId(String editorSourceValueId) {
        this.editorSourceValueId = editorSourceValueId;
    }

    public String getEditorSourceExtraValueId() {
        return editorSourceExtraValueId;
    }

    public void setEditorSourceExtraValueId(String editorSourceExtraValueId) {
        this.editorSourceExtraValueId = editorSourceExtraValueId;
    }

    public int getRevision() {
        return revision;
    }

    public int getRevisionNext() {
        return revision + 1;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public boolean hasEditorSource() {
        return this.editorSourceValueId != null;
    }

    public boolean hasEditorSourceExtra() {
        return this.editorSourceExtraValueId != null;
    }

}
