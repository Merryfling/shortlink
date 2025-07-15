package dev.chanler.shortlink.admin.dao.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import dev.chanler.shortlink.admin.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接分组实体
 * @author: Chanler
 */
@Data
@TableName("t_group")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupDO extends BaseDO {
    /**
    * ID
    */
    private Long id;

    /**
    * 分组标识
    */
    private String gid;

    /**
    * 分组名称
    */
    private String name;

    /**
    * 创建分组用户名
    */
    private String username;

    /**
    * 分组排序
    */
    private Integer sortOrder;
}
