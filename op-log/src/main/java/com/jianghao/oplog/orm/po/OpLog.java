package com.jianghao.oplog.orm.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

/**
* 操作日志
*/
@ApiModel(value="操作日志")
@Data
public class OpLog {
    /**
    * 主键
    */
    @ApiModelProperty(value="主键")
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
    * 请求源ip
    */
    @ApiModelProperty(value="请求源ip")
    private String requestIp;

    /**
    * 请求路径
    */
    @ApiModelProperty(value="请求路径")
    private String requestUri;

    /**
    * 请求方法
    */
    @ApiModelProperty(value="请求方法")
    private String requestMethod;

    /**
    * 请求时间
    */
    @ApiModelProperty(value="请求时间")
    private Date requestTime;

    /**
    * 菜单
    */
    @ApiModelProperty(value="菜单")
    private String menu;

    /**
    * 操作功能
    */
    @ApiModelProperty(value="操作功能")
    private String function;

    /**
    * 0-正常 1-异常
    */
    @ApiModelProperty(value="0-正常 1-异常")
    private Integer status;

    /**
    * 异常信息
    */
    @ApiModelProperty(value="异常信息")
    private String errorMsg;

    @ApiModelProperty(value="")
    private Date createTime;

    @ApiModelProperty(value="")
    private Date updateTime;
}