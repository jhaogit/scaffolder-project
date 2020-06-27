package com.jianghao.controlleradvice;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author ：jh
 * @date ：Created in 2020/6/26 23:36
 * @description：
 */

@Data
public class TestReq {
    @NotBlank(message = "姓名不能为空！")
    private String name;
    @NotNull(message = "年龄不能为空！")
    private Integer age;
    @NotBlank(message = "地址不能为空！")
    private String addr;
}