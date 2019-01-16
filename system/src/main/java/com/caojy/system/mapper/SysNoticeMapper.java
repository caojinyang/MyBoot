package com.caojy.system.mapper;

import java.util.List;
import com.caojy.system.domain.SysNotice;

/**
 * 公告 数据层
 * 
 * @author caojy
 */
public interface SysNoticeMapper
{
    /**
     * 查询公告信息
     * 
     * @param noticeId 公告ID
     * @return 公告信息
     */
    public SysNotice selectNoticeById(Long noticeId);

    /**
     * 查询公告列表
     * 
     * @param notice 公告信息
     * @return 公告集合
     */
    public List<SysNotice> selectNoticeList(SysNotice notice);

    /**
     * 新增公告
     * 
     * @param notice 公告信息
     * @return 结果
     */
    public int insertNotice(SysNotice notice);

    /**
     * 修改公告
     * 
     * @param notice 公告信息
     * @return 结果
     */
    public int updateNotice(SysNotice notice);

    /**
     * 批量删除公告
     * 
     * @param noticeIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteNoticeByIds(String[] noticeIds);

    /**
     * @Author caojy
     * @Description 查询最新的10条公告根据更新时间降序查找
     * @Param
     * @return
     **/
    List<SysNotice> selectLastTenNotice();
}