package com.jianghao.oplog.aspect;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.jianghao.oplog.annotation.DataLog;
import com.jianghao.oplog.handle.CompareParam;
import com.jianghao.oplog.handle.CompareResult;
import com.jianghao.oplog.orm.po.OpLogInfo;
import com.jianghao.oplog.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.SqlCommandType;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ：jh
 * @date ：Created in 2020/8/5 17:32
 * @description：
 */

@Aspect
@Order(99)
@Component
@AllArgsConstructor
@Slf4j
public class DataLogAspect {

    private final DataSource dataSource;

    /**
     *数据缓存，根据线程id
     */
    private static final Map<Long, List<DataCache>> TEM_MAP = new ConcurrentHashMap<>();

    private static final Map<Long, Integer> LOG_MAP = new ConcurrentHashMap<>();


    /**
     * @param threadName
     * @return boolean
     * @Description: 判断线程是否需要记录日志
     */
    public static boolean hasThread(Long threadName) {
        return TEM_MAP.containsKey(threadName);
    }

    /**
     * @param threadName
     * @param dataCache
     * @return void
     * @Description: 增加线程数据库操作
     */
    public static void put(Long threadName, DataCache dataCache) {
        if (TEM_MAP.containsKey(threadName)) {
            TEM_MAP.get(threadName).add(dataCache);
        }
    }

    /**
     * @param dataLog
     * @return void
     * @Description:
     */
    @SneakyThrows
    @Before("@annotation(dataLog)")
    public void before(DataLog dataLog) {
        // 获取线程名，使用线程名作为同一次操作记录
        //todo 记录请求的基础信息并入库，操作人员、时间、部门、ip、菜单、功能、路径、方法、入参、出参等

        Long threadName = Thread.currentThread().getId();
        TEM_MAP.put(threadName, new LinkedList<>());
    }

    /**
     * @param dataLog
     * @return void
     * @Description: 切面后执行
     */
    @SneakyThrows
    @After("@annotation(dataLog)")
    public void after(DataLog dataLog) {
        // 获取线程名，使用线程名作为同一次操作记录
        Long threadName = Thread.currentThread().getId();
        List<DataCache> list = TEM_MAP.get(threadName);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        try {
            list.forEach(dataCache -> {
                try {
                    if(SqlCommandType.UPDATE.equals(dataCache.getOpType())){
                        List<?> oldData = dataCache.getOldData();
                        if (CollUtil.isEmpty(oldData)) {
                            return;
                        }
                        List<String> sqlList = dataCache.getSqlList();
                        ArrayList newData = new ArrayList();
                        for (int i = 0; i<sqlList.size(); i++) {
                            newData.addAll(Db.use(dataSource).query(sqlList.get(i), dataCache.getEntityType()));
                        }
                        dataCache.setNewData(newData);
                        log.info("oldData:{}",JSONUtil.toJsonStr(dataCache.getOldData()));
                        log.info("newData:{}",JSONUtil.toJsonStr(dataCache.getNewData()));
                    }else if(SqlCommandType.DELETE.equals(dataCache.getOpType())){
                        List<?> oldData = dataCache.getOldData();
                        if (CollUtil.isEmpty(oldData)) {
                            return;
                        }
                        log.info("oldData:{}",JSONUtil.toJsonStr(dataCache.getOldData()));
                    }else if(SqlCommandType.INSERT.equals(dataCache.getOpType())){
                        List<String> sqlList = dataCache.getSqlList();
                        ArrayList newData = new ArrayList();
                        for (int i = 0; i<sqlList.size(); i++) {
                            newData.addAll(Db.use(dataSource).query(sqlList.get(i), dataCache.getEntityType()));
                        }
                        dataCache.setNewData(newData);
                        log.info("newData:{}",JSONUtil.toJsonStr(newData));
                    }else{
                        return;
                    }
                }catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            // 异步对比存库
            this.compareAndSave(list);
        } finally {
            // 移除当前线程
            TEM_MAP.remove(threadName);
        }
    }

    /**
     * @param list
     * @return void
     * @Description: 对比保存
     */
    @Async
    public void compareAndSave(List<DataCache> list) {
        List<OpLogInfo> opLogInfos = new ArrayList<>();
        list.forEach(dataCache -> {
            if(SqlCommandType.UPDATE.equals(dataCache.getOpType())){
                List<?> oldData = dataCache.getOldData();
                List<?> newData = dataCache.getNewData();
                // 按id排序
                Set<String> keyNames  = dataCache.getKeyNames();
                for(String keyName : keyNames){
                    String methodName = StringUtil.toCamelCase("get_" + keyName);
                    oldData.stream().sorted(Comparator.comparing(d->{
                        Method method = null;
                        try {
                            method = d.getClass().getMethod(methodName);
                            return method.invoke(d).toString();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return "";
                        }
                    }));
                    newData.stream().sorted(Comparator.comparing(d->{
                        Method method = null;
                        try {
                            method = d.getClass().getMethod(methodName);
                            return method.invoke(d).toString();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return "";
                        }
                    }));
                }
                for (int i = 0; i < oldData.size(); i++) {
                    final int[] finalI = {0};
                    CompareResult compareResult = sameClazzDiff(oldData.get(i), newData.get(i));
                    Set<String> kyes = compareResult.getKyes();
                    List<CompareParam> params = compareResult.getParams();
                    List<String> updateInfoList = new ArrayList<>();
                    OpLogInfo opLogInfo = new OpLogInfo();
                    params.forEach(r -> {
                        if (finalI[0] == 0) {
                            opLogInfo.setTableName(dataCache.getTableName());
                            opLogInfo.setTableKeyInfo(JSON.toJSONString(kyes));
                        }
                        updateInfoList.add(StrUtil.format("[{}]：[{}]->[{}]", r.getFieldComment(),r.getOldValue(),r.getNewValue()));
                        finalI[0]++;
                    });
                    opLogInfo.setUpdateInfo(JSON.toJSONString(updateInfoList));
                    opLogInfos.add(opLogInfo);
                }
                log.info("修改结果：{}",opLogInfos);
            }else if(SqlCommandType.DELETE.equals(dataCache.getOpType())){
                List<?> oldData = dataCache.getOldData();
                for (int i = 0; i < oldData.size(); i++) {
                    final int[] finalI = {0};
                    CompareResult compareResult = deleteOrInsert(oldData.get(i));
                    Set<String> kyes = compareResult.getKyes();
                    List<CompareParam> params = compareResult.getParams();
                    List<String> updateInfoList = new ArrayList<>();
                    OpLogInfo opLogInfo = new OpLogInfo();
                    params.forEach(r -> {
                        if (finalI[0] == 0) {
                            opLogInfo.setTableName(dataCache.getTableName());
                            opLogInfo.setTableKeyInfo(JSON.toJSONString(kyes));
                        }
                        updateInfoList.add(StrUtil.format("[{}]：[{}]", r.getFieldComment(),r.getOldValue()));
                        finalI[0]++;
                    });
                    opLogInfo.setUpdateInfo(JSON.toJSONString(updateInfoList));
                    opLogInfos.add(opLogInfo);
                }
                log.info("删除结果：{}",opLogInfos);
            }else if(SqlCommandType.INSERT.equals(dataCache.getOpType())){
                List<?> newData = dataCache.getNewData();
                for (int i = 0; i < newData.size(); i++) {
                    final int[] finalI = {0};
                    CompareResult compareResult = deleteOrInsert(newData.get(i));
                    Set<String> kyes = compareResult.getKyes();
                    List<CompareParam> params = compareResult.getParams();
                    List<String> updateInfoList = new ArrayList<>();
                    OpLogInfo opLogInfo = new OpLogInfo();
                    params.forEach(r -> {
                        if (finalI[0] == 0) {
                            opLogInfo.setTableName(dataCache.getTableName());
                            opLogInfo.setTableKeyInfo(JSON.toJSONString(kyes));
                        }
                        updateInfoList.add(StrUtil.format("[{}]：[{}]", r.getFieldComment(),r.getOldValue()));
                        finalI[0]++;
                    });
                    opLogInfo.setUpdateInfo(JSON.toJSONString(updateInfoList));
                    opLogInfos.add(opLogInfo);
                }
                log.info("新增结果：{}",opLogInfos);
            }
        });
        // 存库

    }

    /**
     * @param obj1 老数据
     * @param obj2 新数据
     * @return CompareResult 对比结果
     * @Description:
     */
    private CompareResult sameClazzDiff(Object obj1, Object obj2) {
        CompareResult compareResult = new CompareResult();
        List<CompareParam> params = new ArrayList<>();
        Field[] obj1Fields = obj1.getClass().getDeclaredFields();
        Field[] obj2Fields = obj2.getClass().getDeclaredFields();
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < obj1Fields.length; i++) {
            obj1Fields[i].setAccessible(true);
            obj2Fields[i].setAccessible(true);
            Field field = obj1Fields[i];
            DataLog dataLog = field.getAnnotation(DataLog.class);
            if(dataLog!=null&&dataLog.isKey()==true){
                String keyName = field.getName();
                try {
                    Object keyValue = field.get(obj1);
                    keys.add(keyName+":"+keyValue);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            compareResult.setKyes(keys);
            if(dataLog!=null&&dataLog.isLog()==true){
                try {
                    Object value1 = obj1Fields[i].get(obj1);
                    Object value2 = obj2Fields[i].get(obj2);
                    if (!ObjectUtil.equal(value1, value2)) {
                        CompareParam r = new CompareParam();
                        r.setFieldName(field.getName());
                        // 获取注释
                        if(StringUtils.isNotBlank(dataLog.note())){
                            r.setFieldComment(dataLog.note());
                        }else{
                            r.setFieldComment(field.getName());
                        }
                        r.setOldValue(value1);
                        r.setNewValue(value2);
                        params.add(r);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        compareResult.setParams(params);
        return compareResult;
    }

    /**
     * @param obj1 被删除数据或新增数据
     * @return CompareResult
     * @Description: 删除或新增数据记录
     */
    private CompareResult deleteOrInsert(Object obj1) {
        CompareResult compareResult = new CompareResult();
        List<CompareParam> params = new ArrayList<>();
        Field[] obj1Fields = obj1.getClass().getDeclaredFields();
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < obj1Fields.length; i++) {
            obj1Fields[i].setAccessible(true);
            Field field = obj1Fields[i];
            DataLog dataLog = field.getAnnotation(DataLog.class);
            if(dataLog!=null&&dataLog.isKey()==true){
                String keyName = field.getName();
                try {
                    Object keyValue = field.get(obj1);
                    keys.add(keyName+":"+keyValue);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            compareResult.setKyes(keys);
            if(dataLog!=null&&dataLog.isLog()==true){
                try {
                    Object value1 = obj1Fields[i].get(obj1);
                    CompareParam r = new CompareParam();
                    r.setFieldName(field.getName());
                    // 获取注释
                    if(StringUtils.isNotBlank(dataLog.note())){
                        r.setFieldComment(dataLog.note());
                    }else{
                        r.setFieldComment(field.getName());
                    }
                    r.setOldValue(value1);
                    params.add(r);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        compareResult.setParams(params);
        return compareResult;
    }

}