-- 创建组与用户关联视图
DROP table IF EXISTS `act_id_membership`;
create or replace view act_id_membership
(user_id_, group_id_)
as
select user_id as user_id_,role_id as group_id_
from sys_user_role ;


-- 创建组视图
DROP table IF EXISTS `act_id_group`;
create   or replace view act_id_group
(id_, rev_, name_, type_)
as
select t.role_id as id_ , 1 as rev_,t.role_name ,t.role_key as type_
from sys_role t;



-- 创建用户视图
DROP table IF EXISTS `act_id_user`;
CREATE OR REPLACE VIEW act_id_user
(id_, rev_, first_, last_, email_, pwd_, picture_id_)
AS
SELECT user_id as id_,1 as rev_ ,login_name as first_,user_name as last_,email as email_,`password` as pwd_ ,null as picture_id_
FROM sys_user

-- 创建用户信息视图
-- DROP table IF EXISTS `act_id_info`;
-- CREATE OR REPLACE VIEW act_id_user
-- (ID_, REV_, USER_ID_, TYPE_,KEY_,VALUE_, PASSWORD_,PARENT_ID_)
-- AS
-- SELECT user_id as id_,1 as rev_ ,login_name as first_,user_name as last_,email as email_,`password` as pwd_ ,null as picture_id_
-- FROM sys_user




