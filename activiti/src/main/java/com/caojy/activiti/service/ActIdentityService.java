package com.caojy.activiti.service;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;

import java.util.List;

/**
 * create by caojy on 2019/1/10
 */
public interface ActIdentityService {

    List<User> selectUserList();
    List<Group> selectGroupList();

}
