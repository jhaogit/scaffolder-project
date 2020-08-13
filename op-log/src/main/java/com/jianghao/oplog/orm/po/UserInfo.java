package com.jianghao.oplog.orm.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jianghao.oplog.annotation.DataLog;
import com.jianghao.oplog.annotation.LogReplace;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel(value="用户信息")
@Data
@TableName("tb_user_info")
@LogReplace
public class UserInfo {
    /**
    * 主键
    */
    @ApiModelProperty(value="主键")
    @TableId(value = "id", type = IdType.AUTO)
    @DataLog(isKey = true)
    private Integer id;

    @ApiModelProperty(value="姓名")
    @DataLog(note = "姓名",isLog = true)
    private String name;

    /**
    * 1-男 2-女
    */
    @ApiModelProperty(value="1-男 2-女")
    @DataLog(note = "性别",isLog = true,replace = {"男_1","女_2"})
    private Integer sex;

    /**
    * 年龄
    */
    @ApiModelProperty(value="年龄")
    @DataLog(note = "年龄",isLog = true)
    private Integer age;

    /**
    * 地址
    */
    @ApiModelProperty(value="地址")
    @DataLog(note = "地址",isLog = true)
    private String addr;

    /**
    * 创建时间
    */
    @ApiModelProperty(value="创建时间")
    private Date createTime;

    /**
    * 修改时间
    */
    @ApiModelProperty(value="修改时间")
    private Date updateTime;
}