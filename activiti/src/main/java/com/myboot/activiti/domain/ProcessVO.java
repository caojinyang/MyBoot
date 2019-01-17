package com.myboot.activiti.domain;

import com.myboot.common.base.BaseEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;

/**
 * create by caojy on 2019/1/10
 */
public class ProcessVO extends BaseEntity {

    private String id;
    private String name;
    private String deploymentId;
    private String category;
    private String key;
    private String resourceName;
    private String version;
    private Boolean suspended;
    private Boolean latest;
    private String tenantId;
    private String startableByUser;

    public ProcessVO(Deployment processDefinition) {
        this.setId(processDefinition.getId());
        this.name = processDefinition.getName();
    }

    public ProcessVO(ProcessDefinition processDefinition) {
        this.setId(processDefinition.getId());
        this.name = processDefinition.getName();
        this.deploymentId = processDefinition.getDeploymentId();
    }

    public ProcessVO() {

    }

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

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getSuspended() {
        return suspended;
    }

    public void setSuspended(Boolean suspended) {
        this.suspended = suspended;
    }

    public Boolean getLatest() {
        return latest;
    }

    public void setLatest(Boolean latest) {
        this.latest = latest;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getStartableByUser() {
        return startableByUser;
    }

    public void setStartableByUser(String startableByUser) {
        this.startableByUser = startableByUser;
    }

}
