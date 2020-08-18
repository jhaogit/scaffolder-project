package com.jianghao.oplog.controller;

import com.jianghao.oplog.annotation.DataLog;
import com.jianghao.oplog.orm.dao.TestTwoMapper;
import com.jianghao.oplog.orm.dao.UserInfoMapper;
import com.jianghao.oplog.orm.po.TestTwo;
import com.jianghao.oplog.orm.po.UserInfo;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Random;

/**
 * @author ：jh
 * @date ：Created in 2020/8/6 10:33
 * @description：对比两个属性结果
 */
@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserInfoMapper userInfoMapper;

    @DataLog
    @PutMapping("/update/userInfo")
    @Transactional
    public Integer updateMyUser(@RequestBody UserInfo userInfo){
        return userInfoMapper.updateByPrimaryKeySelective(userInfo);
    }

    private TestTwoMapper testTwoMapper;
    @DataLog
    @PutMapping("/update/testTwo")
    @Transactional
    public Integer update2(@RequestBody TestTwo testTwo){
        return testTwoMapper.updateByPrimaryKeySelective(testTwo);
    }

    @DataLog
    @PutMapping("/update/test3")
    @Transactional
    public Integer update3(@RequestBody TestTwo testTwo){
        UserInfo userInfo = new UserInfo();
        userInfo.setId(1);
        userInfo.setAge(new Random().nextInt(100));
        userInfoMapper.updateByPrimaryKeySelective(userInfo);
        return testTwoMapper.updateByPrimaryKeySelective(testTwo);
    }

    @DataLog
    @PutMapping("/update/test4")
    @Transactional
    public Integer update4(@RequestBody List<TestTwo> testTwos){
        return testTwoMapper.updateListByPrimaryKeySelective(testTwos);
    }

    @DataLog
    @PutMapping("/insert/test1")
    @Transactional
    public Integer insert1(@RequestBody UserInfo userInfo){
        int i = userInfoMapper.insertSelective(userInfo);
        return i;
    }

    @DataLog
    @PutMapping("/insert/test2")
    @Transactional
    public Integer insert2(@RequestBody TestTwo testTwo){
        int i = testTwoMapper.insertSelective(testTwo);
        return i;
    }

    @DataLog
    @PutMapping("/insert/test3")
    @Transactional
    public Integer insert3(@RequestBody List<UserInfo> userInfos){
        int i = userInfoMapper.insertListSelective(userInfos);
        return i;
    }

    @DataLog
    @PutMapping("/insert/test4")
    @Transactional
    public Integer insert4(@RequestBody List<TestTwo> testTwos){
        int i = testTwoMapper.insertListSelective(testTwos);
        return i;
    }

    @DataLog
    @GetMapping("/delete/test1")
    @Transactional
    public Integer delete1(@RequestParam Integer key ){
        int i = userInfoMapper.deleteByPrimaryKey(key);
        return i;
    }
}
