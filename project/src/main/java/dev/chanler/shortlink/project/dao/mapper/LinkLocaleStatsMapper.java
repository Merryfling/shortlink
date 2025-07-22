package dev.chanler.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import dev.chanler.shortlink.project.dao.entity.LinkLocaleStatsDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * 地区访问统计持久层
 * @author: Chanler
 */
public interface LinkLocaleStatsMapper extends BaseMapper<LinkLocaleStatsDO> {

    /**
     * 短链接地区访问统计
     * @param linkLocaleStatsDO 地区访问统计实体
     */
    @Insert("INSERT INTO " +
            "t_link_locale_stats (full_short_url, date, cnt, country, province, city, adcode, create_time, update_time, del_flag) " +
            "VALUES( #{linkLocaleStats.fullShortUrl}, #{linkLocaleStats.date}, #{linkLocaleStats.cnt}, #{linkLocaleStats.country}, #{linkLocaleStats.province}, #{linkLocaleStats.city}, #{linkLocaleStats.adcode}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE cnt = cnt +  #{linkLocaleStats.cnt};")
    void shortLinkLocaleStats(@Param("linkLocaleStats") LinkLocaleStatsDO linkLocaleStatsDO);
}
