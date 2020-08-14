package com.jianghao.oplog.handle;

import cn.hutool.db.Db;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.TableNameParser;
import com.baomidou.mybatisplus.extension.handlers.AbstractSqlParserHandler;
import com.jianghao.oplog.annotation.DataLog;
import com.jianghao.oplog.aspect.DataCache;
import com.jianghao.oplog.aspect.DataLogAspect;
import com.jianghao.oplog.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ：jh
 * @date ：Created in 2020/8/5 15:33
 * @description：数据操作拦截器
 */

@Slf4j
@AllArgsConstructor
@Intercepts({@Signature(type = StatementHandler.class, method = "update", args = {Statement.class})})
public class OpLogInterceptor extends AbstractSqlParserHandler implements Interceptor {
    private final DataSource dataSource;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 获取线程名，使用线程名作为同一次操作记录
        Long threadId = Thread.currentThread().getId();
        // 判断是否需要记录日志
        if (!DataLogAspect.hasThread(threadId)) {
            return invocation.proceed();
        }
        Statement statement;
        Object firstArg = invocation.getArgs()[0];
        if (Proxy.isProxyClass(firstArg.getClass())) {
            statement = (Statement) SystemMetaObject.forObject(firstArg).getValue("h.statement");
        } else {
            statement = (Statement) firstArg;
        }
        MetaObject stmtMetaObj = SystemMetaObject.forObject(statement);
        try {
            statement = (Statement) stmtMetaObj.getValue("stmt.statement");
        } catch (Exception e) {
            log.error("error:{}",e);
        }
        if (stmtMetaObj.hasGetter("delegate")) {
            try {
                statement = (Statement) stmtMetaObj.getValue("delegate");
            } catch (Exception e) {
                log.error("error:{}",e);
            }
        }
        String originalSql = statement.toString();
        originalSql = originalSql.replaceAll("[\\s]+", StringPool.SPACE);
        int index = indexOfSqlStart(originalSql);
        if (index > 0) {
            originalSql = originalSql.substring(index);
        }

        StatementHandler statementHandler = PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        this.sqlParser(metaObject);
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");

        // 获取执行Sql
        String sql = originalSql.replace("where", "WHERE");
        // 插入
        if (SqlCommandType.INSERT.equals(mappedStatement.getSqlCommandType())) {
        }

        // 使用mybatis-plus 工具解析sql获取表名
        Collection<String> tables = new TableNameParser(sql).tables();
        if (CollectionUtils.isEmpty(tables)) {
            return invocation.proceed();
        }

        String tableName = tables.iterator().next();
        // 使用mybatis-plus 工具根据表名找出对应的实体类
        Class<?> entityType = TableInfoHelper.getTableInfos().stream().filter(t -> t.getTableName().equals(tableName))
                .findFirst().orElse(new TableInfo(null)).getEntityType();
        DataCache dataCache = new DataCache();
        // 更新
        if (SqlCommandType.UPDATE.equals(mappedStatement.getSqlCommandType())) {
            try {
                if (entityType == null) {
                    return invocation.proceed();
                }
                //获取所有key的名称
                Set<String> keyNames = new HashSet<>();
                Field[] fields = entityType.getDeclaredFields();
                for (Field field : fields) {
                    DataLog d = field.getAnnotation(DataLog.class);
                    if(d!=null&&d.isKey()==true){
                        keyNames.add(field.getName());
                    }
                }
                keyNames.stream().sorted(Comparator.reverseOrder());
                dataCache.setKeyNames(keyNames);
                dataCache.setOpType(SqlCommandType.UPDATE);
                dataCache.setTableName(tableName);
                dataCache.setEntityType(entityType);
                // 设置sql用于执行完后查询新数据
                List<String> sqlList = Arrays.asList(sql.split(";"));
                // 查询更新前数据
                ArrayList oldData = new ArrayList();
                for (int i = 0; i<sqlList.size(); i++) {
                    String s = "SELECT * FROM " + tableName + " " + sqlList.get(i).substring(sqlList.get(i).lastIndexOf("WHERE"));
                    sqlList.set(i,s);
                    oldData.addAll(Db.use(dataSource).query(s, entityType));
                }
                dataCache.setOldData(oldData);
                dataCache.setSqlList(sqlList);
                DataLogAspect.put(threadId, dataCache);
            } catch (Exception e) {
                log.error("error:{}",e);
            }
            return invocation.proceed();
        }

        // 删除
        if (SqlCommandType.DELETE.equals(mappedStatement.getSqlCommandType())) {
            try {
                if (entityType == null) {
                    return invocation.proceed();
                }
                //获取所有key的名称
                Set<String> keyNames = new HashSet<>();
                Field[] fields = entityType.getDeclaredFields();
                for (Field field : fields) {
                    DataLog d = field.getAnnotation(DataLog.class);
                    if(d!=null&&d.isKey()==true){
                        keyNames.add(field.getName());
                    }
                }
                keyNames.stream().sorted(Comparator.reverseOrder());
                dataCache.setKeyNames(keyNames);

                dataCache.setOpType(SqlCommandType.DELETE);
                dataCache.setTableName(tableName);
                dataCache.setEntityType(entityType);
                // 设置sql用于查询数据
                List<String> sqlList = Arrays.asList(sql.split(";"));
                // 查询删除前数据
                ArrayList oldData = new ArrayList();
                for (int i = 0; i<sqlList.size(); i++) {
                    String s = "SELECT * FROM " + tableName + " " + sqlList.get(i).substring(sqlList.get(i).lastIndexOf("WHERE"));
                    sqlList.set(i,s);
                    oldData.addAll(Db.use(dataSource).query(s, entityType));
                }
                dataCache.setOldData(oldData);
                dataCache.setSqlList(sqlList);
                DataLogAspect.put(threadId, dataCache);
            } catch (Exception e) {
                log.error("error:{}",e);
            }
            return invocation.proceed();
        }

        //新增
        if(SqlCommandType.INSERT.equals(mappedStatement.getSqlCommandType())){
            //获取所有key的名称
            Set<String> keyNames = new HashSet<>();
            Set<String> keyColumnNames = new HashSet<>();
            Field[] fields = entityType.getDeclaredFields();
            for (Field field : fields) {
                DataLog d = field.getAnnotation(DataLog.class);
                if(d!=null&&d.isKey()==true){
                    keyNames.add(field.getName());
                    String keyColumnName = StringUtils.isNotBlank(d.keyColumn())?d.keyColumn(): StringUtil.humpToUnderline(field.getName());
                    keyColumnNames.add(keyColumnName);
                }
            }
            keyNames.stream().sorted(Comparator.reverseOrder());
            dataCache.setKeyNames(keyNames);
            dataCache.setOpType(SqlCommandType.INSERT);
            dataCache.setTableName(tableName);
            dataCache.setEntityType(entityType);

            Object proceed = invocation.proceed();
            //获取新增数据的list列表（包含主键）
            Object target = invocation.getTarget();
            RoutingStatementHandler routingStatementHandler = (RoutingStatementHandler) SystemMetaObject.forObject(target).getValue("h.target");
            PreparedStatementHandler parameterHandler = (PreparedStatementHandler) SystemMetaObject.forObject(routingStatementHandler).getValue("delegate");
            BoundSql boundSql = parameterHandler.getBoundSql();
            Map<String,Object> map = (Map<String,Object>)boundSql.getParameterObject();

            /*
             * 组装执行insert后，获取insert行对应的select语句
             */
            String collect = keyColumnNames.stream().collect(Collectors.joining(","));
            String ks = "("+ collect +")";
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT * FROM " + tableName + " where ").append(ks).append(" in ( ");
            List<String> kNames = new ArrayList<>();
            kNames.addAll(keyNames);
            if(map.get("list")!=null&&map.get("collection")!=null){
                //insert集合
                List<Map> list = JSON.parseArray(JSON.toJSONString(map.get("list")),Map.class);
                String strList = list.stream().map(o->{
                    String s = "(";
                    for(int i=0; i<kNames.size(); i++){
                        if(i==kNames.size()-1){
                            s+=o.get(kNames.get(i));
                        }else{
                            s+=o.get(kNames.get(i))+",";
                        }
                    }
                    return s+")";
                }).collect(Collectors.joining(","));
                sb.append(strList);
            }else{
                String s = "(";
                for(int i=0; i<kNames.size(); i++){
                    if(i==kNames.size()-1){
                        s+=map.get(kNames.get(i));
                    }else{
                        s+=map.get(kNames.get(i))+",";
                    }
                }
                s+=")";
                sb.append(s);
            }
            sb.append(" )");
            List<String> sqls = new ArrayList<>();
            sqls.add(sb.toString());
            dataCache.setSqlList(sqls);
            DataLogAspect.put(threadId, dataCache);
            return proceed;
        }
        return invocation.proceed();
    }


    /**
     * 获取sql语句开头部分
     *
     * @param sql ignore
     * @return ignore
     */
    private int indexOfSqlStart(String sql) {
        String upperCaseSql = sql.toUpperCase();
        Set<Integer> set = new HashSet<>();
        set.add(upperCaseSql.indexOf("SELECT "));
        set.add(upperCaseSql.indexOf("UPDATE "));
        set.add(upperCaseSql.indexOf("INSERT "));
        set.add(upperCaseSql.indexOf("DELETE "));
        set.remove(-1);
        if (CollectionUtils.isEmpty(set)) {
            return -1;
        }
        List<Integer> list = new ArrayList<>(set);
        list.sort(Comparator.naturalOrder());
        return list.get(0);
    }
}