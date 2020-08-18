package com.jianghao.oplog.orm.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

/**
* 操作日志详细信息
*/
@ApiModel(value="操作日志详细信息")
@Data
public class OpLogInfo {
    /**
    * 主键
    */
    @ApiModelProperty(value="主键")
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
    * op_log中的主键
    */
    @ApiModelProperty(value="op_log中的主键")
    private Integer opLogId;

    /**
     * 变更类型 UPDATE/INSERT/DALETE
     */
    @ApiModelProperty(value="变更类型 UPDATE/INSERT/DALETE")
    private String opType;

    /**
    * 表名
    */
    @ApiModelProperty(value="表名")
    private String tableName;

    /**
    * 表主键信息
    */
    @ApiModelProperty(value="表主键信息")
    private String tableKeyInfo;

    /**
    * 变更信息，list: [ [名称]：[呵呵6]->[呵呵5]","[描述]：[测试6]->[测试5] ]
    */
    @ApiModelProperty(value="变更信息，list:,[ [名称]：[呵呵6]->[呵呵5]\",\"[描述]：[测试6]->[测试5] ]")
    private String updateInfo;

    @ApiModelProperty(value="")
    private Date createTime;
}