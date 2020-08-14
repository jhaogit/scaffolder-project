package com.jianghao.oplog.aspect;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.jianghao.oplog.annotation.DataLog;
import com.jianghao.oplog.annotation.LogReplace;
import com.jianghao.oplog.handle.CompareParam;
import com.jianghao.oplog.handle.CompareResult;
import com.jianghao.oplog.orm.dao.OpLogInfoMapper;
import com.jianghao.oplog.orm.dao.OpLogMapper;
import com.jianghao.oplog.orm.po.OpLog;
import com.jianghao.oplog.orm.po.OpLogInfo;
import com.jianghao.oplog.util.IpAddressUtils;
import com.jianghao.oplog.util.StringUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.SqlCommandType;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
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
@Slf4j
public class DataLogAspect {

    private final DataSource dataSource;
    public DataLogAspect(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private OpLogMapper opLogMapper;
    @Autowired
    private OpLogInfoMapper opLogInfoMapper;
    @Value("${log.replace.packageName}")
    private String packageName;

    /**
     *数据缓存，根据线程id
     */
    private static final Map<Long, List<DataCache>> TEM_MAP = new ConcurrentHashMap<>();
    /**
     *oplog缓存，根据线程id，记录基础日志的的主键，用于详细日志的关联
     */
    private static final Map<Long, Integer> LOG_MAP = new ConcurrentHashMap<>();
    /**
     *实体类中，某些字段映射缓存，Map<className-字段名称,Map<字段值,映射值>>  因为枚举变更必然需要修改代码，所以每次启动一次加载即可
     */
    private static final Map<String,Map<String,String>> REPLACE_MAP = new ConcurrentHashMap<>();

    /**
     * @param
     * @return void
     * @Description: 初始化项目中所有需要替换的值映射
     */
    @PostConstruct
    public void replaceInit(){
        /**
         * packageName: 包路径（扫描需要属性映射的实体类）
         * LogReplace.class: 注解，用于标记实体类中有属性值需要映射
         */
        Set<Class<?>> classSet = getReplace(packageName, LogReplace.class);
        classSet.forEach(c->{
            String className = c.getName();
            Field[] declaredFields = c.getDeclaredFields();
            String filedName="";
            for (Field field : declaredFields) {
                DataLog d = field.getAnnotation(DataLog.class);
                if(d!=null){
                    String[] replace = d.replace();
                    if(replace!=null&&StringUtils.isNotBlank(replace[0])){
                        Map<String,String> replaceName = new HashMap<>();
                        filedName = field.getName();
                        try {
                            for (String s : replace) {
                                String[] rs = s.split("_");
                                replaceName.put(rs[1],rs[0]);
                            }
                        } catch (Exception e) {
                            log.error("error:{}",e);
                        }
                        REPLACE_MAP.put(className+"-"+filedName,replaceName);
                    }
                }
            }
        });
    }


    /**
     * @param packageName 需要扫描的包
     * @param annoClass 包上注解
     * @return java.util.Set<java.lang.Class<?>>
     * @Description:
     */
    private Set<Class<?>> getReplace(String packageName,Class annoClass){
        //入参 要扫描的包名
        Reflections f = new Reflections(packageName);
        //入参 目标注解类
        Set<Class<?>> set = f.getTypesAnnotatedWith(annoClass);
        return set;
    }


    /**
     * @param threadId
     * @return boolean
     * @Description: 判断线程是否需要记录日志
     */
    public static boolean hasThread(Long threadId) {
        return TEM_MAP.containsKey(threadId);
    }

    /**
     * @param threadId
     * @param dataCache
     * @return void
     * @Description: 增加线程数据库操作
     */
    public static void put(Long threadId, DataCache dataCache) {
        if (TEM_MAP.containsKey(threadId)) {
            TEM_MAP.get(threadId).add(dataCache);
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
        //todo 记录请求的基础信息并入库，操作人员、时间、部门、ip、菜单、功能、路径、方法、入参、出参等

        OpLog opLog = new OpLog();
        opLog.setRequestIp(IpAddressUtils.getIpAddress(request));
        opLog.setRequestUri(request.getRequestURI());
        opLog.setRequestMethod(request.getMethod());
        opLog.setRequestTime(new Date());

        // 获取线程名，使用线程名作为同一次操作记录
        Long threadId = Thread.currentThread().getId();
        opLogMapper.insertSelective(opLog);
        LOG_MAP.put(threadId,opLog.getId());
        TEM_MAP.put(threadId, new LinkedList<>());
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
        Long threadId = Thread.currentThread().getId();
        List<DataCache> list = TEM_MAP.get(threadId);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        try {
            //获取新数据
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
                    log.error("error:{}",e);
                }
            });
            // 异步对比存库
            this.compareAndSave(list);
        } finally {
            // 移除当前线程
            TEM_MAP.remove(threadId);
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
                Set<String> keyNames  = dataCache.getKeyNames();
                // 若批量修改，新旧数据按id排序，用于后续一一比对
                for(String keyName : keyNames){
                    String methodName = StringUtil.toCamelCase("get_" + keyName);
                    oldData.stream().sorted(Comparator.comparing(d->{
                        Method method = null;
                        try {
                            method = d.getClass().getMethod(methodName);
                            return method.invoke(d).toString();
                        } catch (Exception e) {
                            log.error("error:{}",e);
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
                    opLogInfo.setOpLogId(LOG_MAP.get(Thread.currentThread().getId()));
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
                    opLogInfo.setOpLogId(LOG_MAP.get(Thread.currentThread().getId()));
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
                    opLogInfo.setOpLogId(LOG_MAP.get(Thread.currentThread().getId()));
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
        opLogInfoMapper.insertListSelective(opLogInfos);
        LOG_MAP.remove(Thread.currentThread().getId());
    }

    /**
     * @param obj1 老数据
     * @param obj2 新数据
     * @return CompareResult 对比结果
     * @Description:
     */
    private CompareResult sameClazzDiff(Object obj1, Object obj2) {
        CompareResult compareResult = new CompareResult();
        String className = obj1.getClass().getName();
        List<CompareParam> params = new ArrayList<>();
        Field[] obj1Fields = obj1.getClass().getDeclaredFields();
        Field[] obj2Fields = obj2.getClass().getDeclaredFields();
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < obj1Fields.length; i++) {
            obj1Fields[i].setAccessible(true);
            obj2Fields[i].setAccessible(true);
            Field field = obj1Fields[i];
            String fileName = field.getName();
            DataLog dataLog = field.getAnnotation(DataLog.class);
            if(dataLog!=null&&dataLog.isKey()==true){
                String keyName = field.getName();
                try {
                    Object keyValue = field.get(obj1);
                    keys.add(keyName+":"+keyValue);
                } catch (IllegalAccessException e) {
                    log.error("error:{}",e);
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
                        if(dataLog.replace()!=null&&StringUtils.isNotBlank(dataLog.replace()[0])){
                            String v1 = REPLACE_MAP.get(className + "-" + fileName).get(value1 + "");
                            String v2 = REPLACE_MAP.get(className + "-" + fileName).get(value2 + "");
                            value1 = StringUtils.isNotBlank(v1)?v1:value1;
                            value2 = StringUtils.isNotBlank(v2)?v2:value2;
                        }
                        r.setOldValue(value1);
                        r.setNewValue(value2);
                        params.add(r);
                    }
                } catch (IllegalAccessException e) {
                    log.error("error:{}",e);
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
        String className = obj1.getClass().getName();
        List<CompareParam> params = new ArrayList<>();
        Field[] obj1Fields = obj1.getClass().getDeclaredFields();
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < obj1Fields.length; i++) {
            obj1Fields[i].setAccessible(true);
            Field field = obj1Fields[i];
            String fileName = field.getName();
            DataLog dataLog = field.getAnnotation(DataLog.class);
            if(dataLog!=null&&dataLog.isKey()==true){
                String keyName = field.getName();
                try {
                    Object keyValue = field.get(obj1);
                    keys.add(keyName+":"+keyValue);
                } catch (IllegalAccessException e) {
                    log.error("error:{}",e);
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
                    if(dataLog.replace()!=null&&StringUtils.isNotBlank(dataLog.replace()[0])){
                        String v1 = REPLACE_MAP.get(className + "-" + fileName).get(value1 + "");
                        value1 = StringUtils.isNotBlank(v1)?v1:value1;
                    }
                    r.setOldValue(value1);
                    params.add(r);
                } catch (IllegalAccessException e) {
                    log.error("error:{}",e);
                }
            }
        }
        compareResult.setParams(params);
        return compareResult;
    }

}