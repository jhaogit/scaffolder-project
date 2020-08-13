package com.jianghao.oplog.orm.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jianghao.oplog.annotation.DataLog;
import com.jianghao.oplog.annotation.LogReplace;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

/**
* 测试联合主键表
*/
@ApiModel(value="测试联合主键表实体")
@Data
@TableName("tb_test_two")
@LogReplace
public class TestTwo {
    /**
    * 主键1
    */
    @ApiModelProperty(value="主键1")
    @DataLog(isKey = true,keyColumn = "one_id")
    private Integer oneId;

    @ApiModelProperty(value="主键2")
    @DataLog(isKey = true,keyColumn = "two_id")
    private Integer twoId;

    /**
    * 名称
    */
    @ApiModelProperty(value="名称")
    @DataLog(note = "名称",isLog = true)
    private String name;

    /**
    * 描述
    */
    @ApiModelProperty(value="描述")
    @DataLog(note = "描述",isLog = true)
    private String desc;

    @ApiModelProperty(value="")
    private Date createTime;

    @ApiModelProperty(value="")
    private Date updateTime;
}