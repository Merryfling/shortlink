package dev.chanler.shortlink.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import dev.chanler.shortlink.dao.entity.LinkDO;
import dev.chanler.shortlink.dto.req.LinkPageReqDTO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 短链接持久层
 * @author: Chanler
 */
public interface LinkMapper extends BaseMapper<LinkDO> {

    /**
     * 增量更新短链接统计数据
     */
    @Update("""
            UPDATE t_link
            SET total_pv = total_pv + #{totalPv},
                total_uv = total_uv + #{totalUv},
                total_uip = total_uip + #{totalUip}
            WHERE gid = #{gid}
              AND full_short_url = #{fullShortUrl}
            """)
    void incrementStats(@Param("gid") String gid,
                        @Param("fullShortUrl") String fullShortUrl,
                        @Param("totalPv") Integer totalPv,
                        @Param("totalUv") Integer totalUv,
                        @Param("totalUip") Integer totalUip);

    /**
     * 分页统计短链接
     */
    @Select("""
            <script>
            SELECT
                t.*,
                COALESCE(s.today_pv, 0)  AS todayPv,
                COALESCE(s.today_uv, 0)  AS todayUv,
                COALESCE(s.today_uip, 0) AS todayUip
            FROM t_link t
            LEFT JOIN t_link_stats_today s
              ON t.full_short_url = s.full_short_url
             AND s.date = CURDATE()
            WHERE t.gid = #{p.gid}
              AND t.enable_status = 0
              AND t.del_flag = 0
            <choose>
                <when test="p.orderTag == 'todayPv'">ORDER BY todayPv DESC</when>
                <when test="p.orderTag == 'todayUv'">ORDER BY todayUv DESC</when>
                <when test="p.orderTag == 'todayUip'">ORDER BY todayUip DESC</when>
                <when test="p.orderTag == 'totalPv'">ORDER BY t.total_pv DESC</when>
                <when test="p.orderTag == 'totalUv'">ORDER BY t.total_uv DESC</when>
                <when test="p.orderTag == 'totalUip'">ORDER BY t.total_uip DESC</when>
                <otherwise>ORDER BY t.create_time DESC</otherwise>
            </choose>
            </script>
            """)
    IPage<LinkDO> pageLink(@Param("p") LinkPageReqDTO linkPageReqDTO);
}
