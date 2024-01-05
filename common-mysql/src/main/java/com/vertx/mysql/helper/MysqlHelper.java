package com.vertx.mysql.helper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.log.StaticLog;
import com.vertx.common.core.enums.EnvEnum;
import com.vertx.common.core.utils.StrUtil;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.sqlclient.Query;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.*;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.conf.ParamType;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

import static com.vertx.common.core.config.VertxLoadConfig.active;
import static com.vertx.common.core.utils.StrUtil.underlineName;
import static com.vertx.mysql.client.MysqlClient.mysqlClient;
import static com.vertx.mysql.utils.TableUtil.getTableName;
import static io.vertx.core.Future.await;

public class MysqlHelper {

    private static final HashMap<String, LocalDateTime> lastWarnNoIdLogTimeMap = new HashMap<>();


    public static Long install(Object data) {
        return installDsl(data, null);
    }

    public static Long install(Object data, SqlConnection sqlConnection) {
        return installDsl(data, sqlConnection);
    }


    public static int insertBatch(List<Object> data) {
        return insertDsl(data, 100, null);
    }

    public static int insertBatch(List<Object> data, SqlConnection sqlConnection) {
        return insertDsl(data, 100, sqlConnection);
    }

    public static int insertBatch(List<Object> data, int batchSize) {
        return insertDsl(data, batchSize, null);
    }

    public static int insertBatch(List<Object> data, int batchSize, SqlConnection sqlConnection) {
        return insertDsl(data, batchSize, sqlConnection);
    }

    public static int update(Object data, Condition where) {
        return updateDsl(data, where, false, null);
    }

    public static int update(Object data, Condition where, SqlConnection sqlConnection) {
        return updateDsl(data, where, false, sqlConnection);
    }

    public static int update(Object data, Condition where, Boolean isNll) {
        return updateDsl(data, where, isNll, null);
    }

    public static int update(Object data, Condition where, Boolean isNll, SqlConnection sqlConnection) {
        return updateDsl(data, where, isNll, sqlConnection);
    }

    public static int updateBatch(List<Map<Object, Condition>> dataList, Boolean isNll, int batchSize, SqlConnection sqlConnection) {
        return updateBatchDsl(dataList, isNll, batchSize, sqlConnection);
    }

    public static int updateBatch(List<Map<Object, Condition>> dataList) {
        return updateBatchDsl(dataList, false, 100, null);
    }

    public static int updateBatch(List<Map<Object, Condition>> dataList, Boolean isNll) {
        return updateBatchDsl(dataList, isNll, 100, null);
    }

    public static int updateBatch(List<Map<Object, Condition>> dataList, Boolean isNll, SqlConnection sqlConnection) {
        return updateBatchDsl(dataList, isNll, 100, sqlConnection);
    }

    public static int updateBatch(List<Map<Object, Condition>> dataList, Boolean isNll, int batchSize) {
        return updateBatchDsl(dataList, isNll, batchSize, null);
    }

    public static int delete(Class<?> c, Condition where) {
        return deleteDsl(c, where, null);
    }

    public static int delete(Class<?> c, Condition where, SqlConnection sqlConnection) {
        return deleteDsl(c, where, sqlConnection);
    }

    public static <T> List<T> select(Class<T> c, Condition where) {
        return selectDsl(c, where, null, null);
    }

    public static <T> List<T> select(Class<T> c, Condition where, List<String> fields) {
        return selectDsl(c, where, fields, null);
    }


    public static <T> List<T> select(Class<T> c, Condition where, List<String> fields, String lastSql) {
        return selectDsl(c, where, fields, lastSql);
    }

    public static <T> List<T> select(Class<T> c, Condition where, String lastSql) {
        return selectDsl(c, where, null, lastSql);
    }


    private static Long installDsl(Object data, SqlConnection sqlConnection) {
        if (data == null) {
            StaticLog.warn("插入数据为空");
            return 0L;
        }
        if (data instanceof String) {
            StaticLog.warn("插入数据为空");
            return 0L;
        }
        final String sql = buildInsertSql(List.of(data));
        final Query<RowSet<Row>> query;
        if (sqlConnection != null) {
            query = sqlConnection.query(sql);
        } else {
            query = mysqlClient.query(sql);
        }
        final RowSet<Row> rows = await(query.execute());
        try {
            data.getClass().getDeclaredField("id");
            return rows.property(MySQLClient.LAST_INSERTED_ID);
        } catch (Exception e) {
            final String name = data.getClass().getName();
            final LocalDateTime now = LocalDateTime.now();
            //检查最后一次警告时间和现在事件是否相差超过一小时,一小时内只会报警一次
            if (!lastWarnNoIdLogTimeMap.containsKey(name) || !now.isBefore(lastWarnNoIdLogTimeMap.get(name).plusHours(1))) {
                lastWarnNoIdLogTimeMap.put(name, now);
                StaticLog.warn("insert data: {} without id", name);
            }
            return 0L;
        }
    }

    private static int insertDsl(List<Object> data, int batchSize, SqlConnection connection) {
        if (data == null || data.isEmpty()) {
            StaticLog.warn("批量插入数据为空");
            return 0;
        }
        // 影响行数
        int count = 0;
        //批量插入数据
        final List<List<Object>> lists = CollUtil.split(data, batchSize);
        final SqlConnection connect = connection != null ? connection : await(mysqlClient.getConnection());
        //开启事务
        final Transaction transaction = await(connect.begin());
        try {
            for (List<Object> list : lists) {
                final String querySql = buildInsertSql(list);
                final RowSet<Row> rows = await(connect.query(querySql).execute());
                count += rows.rowCount();
            }
            //提交事务
            await(transaction.commit());
        } catch (Throwable e) {
            //回滚事务
            await(transaction.rollback());
            StaticLog.error(e, "批量插入数据失败");
            throw e;
        } finally {
            await(connect.close());
        }
        return count;
    }

    private static int updateDsl(Object data, Condition where, Boolean isNll, SqlConnection sqlConnection) {
        if (data == null) {
            StaticLog.warn("更新数据为空");
            return 0;
        }
        final String sql = buildUpdateSql(data, where, isNll);
        final Query<RowSet<Row>> query;
        if (sqlConnection != null) {
            query = sqlConnection.query(sql);
        } else {
            query = mysqlClient.query(sql);
        }
        final RowSet<Row> rows = await(query.execute());
        return rows.rowCount();
    }


    private static int updateBatchDsl(List<Map<Object, Condition>> dataList, Boolean isNll, int batchSize, SqlConnection sqlConnection) {
        if (dataList.isEmpty()) {
            StaticLog.warn(">>>>>> 批量更新数据为空");
            return 0;
        }
        // 影响行数
        int count = 0;
        final List<String> batchSqlList = new ArrayList<>();
        for (Map<Object, Condition> map : dataList) {
            for (Map.Entry<Object, Condition> item : map.entrySet()) {
                final String querySql = buildUpdateSql(item.getKey(), item.getValue(), isNll);
                batchSqlList.add(querySql);
            }
        }
        final List<List<String>> lists = CollUtil.split(batchSqlList, batchSize);
        final SqlConnection connect = sqlConnection != null ? sqlConnection : await(mysqlClient.getConnection());
        // 开启事务
        final Transaction transaction = await(connect.begin());
        try {
            for (List<String> list : lists) {
                for (String querySql : list) {
                    final RowSet<Row> rows = await(connect.query(querySql).execute());
                    count += rows.rowCount();
                }
            }
            // 提交事务
            await(transaction.commit());
        } catch (Throwable e) {
            // 回滚事务
            await(transaction.rollback());
            StaticLog.error(e, ">>>>>> 批量更新数据失败", lists);
            throw e;
        } finally {
            await(connect.close());
        }
        return count;
    }


    private static int deleteDsl(Class<?> c, Condition where, SqlConnection sqlConnection) {
        if (where == null) {
            StaticLog.warn(">>>>>> 删除数据条件不能为空");
            return 0;
        }
        final String sql = buildDeleteSql(c, where);
        final RowSet<Row> rowRowSet;
        if (sqlConnection != null) {
            rowRowSet = await(sqlConnection.query(sql).execute());
        } else {
            rowRowSet = await(mysqlClient.query(sql).execute());
        }
        return rowRowSet.rowCount();
    }


    private static <T> List<T> selectDsl(Class<T> clazz, Condition where, List<String> fields, String lastSql) {
        if (where == null) {
            StaticLog.warn(">>>>>> 查询数据条件不能为空");
            return null;
        }
        final String sql = buildSelectSql(clazz, where, fields, lastSql);
        final RowSet<Row> rowRowSet;
        if (StrUtil.isBlank(lastSql)) {
            rowRowSet = await(mysqlClient.query(sql).execute());
        } else {
            rowRowSet = await(mysqlClient.query(sql + " " + lastSql + ";").execute());
        }
        final List<T> list = new ArrayList<>(rowRowSet.size());
        for (Row row : rowRowSet) {
            list.add(row.toJson().mapTo(clazz));
        }
        return list;
    }


    private static final DSLContext dslContext = DSL.using(SQLDialect.MYSQL, new Settings());

    private static String buildCountSql(Class<?> c, Condition where) {
        String sql = dslContext.selectCount().from(DSL.table(getTableName(c))).where(where).getSQL(ParamType.INLINED);
        if (!Objects.equals(active, EnvEnum.PROD.getValue())) {
            StaticLog.info("count sql: {}", sql);
        }
        sql += ";";
        return sql;
    }

    private static String buildInsertSql(List<Object> datas) {
        if (datas.isEmpty()) {
            StaticLog.warn(">>>>>> insert data is empty");
            return null;
        }
        final Class<?> aClass = datas.get(0).getClass();
        final Field[] declaredFields = aClass.getDeclaredFields();
        final LinkedList<HashMap<String, Object>> fileKeyValueList = new LinkedList<>();
        for (Object data : datas) {
            final HashMap<String, Object> map = new HashMap<>();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                final String name = underlineName(field.getName());
                try {
                    final Object value = field.get(data);
                    if (!name.equals("id")) {
                        map.put(name, value);
                    }
                } catch (IllegalAccessException e) {
                    StaticLog.error(e, "insert data error");
                    return null;
                }
                fileKeyValueList.add(map);
            }
        }
        final InsertValuesStepN<Record> insertValuesStepN = dslContext.insertInto(DSL.table(getTableName(aClass)), fileKeyValueList.get(0).keySet().stream().map(DSL::field).toList());
        for (HashMap<String, Object> map : fileKeyValueList) {
            insertValuesStepN.values(map.values().toArray());
        }

        String sql = insertValuesStepN.getSQL(ParamType.INLINED);
        if (!Objects.equals(active, EnvEnum.PROD.getValue())) {
            StaticLog.info("insert sql: {}", sql);
        }
        sql += ";";
        return sql;
    }


    private static String buildUpdateSql(Object data, Condition where, Boolean isNll) {
        if (data == null) {
            StaticLog.warn(">>>>>> update data is null");
            return null;
        }
        final Class<?> aClass = data.getClass();
        final Field[] declaredFields = aClass.getDeclaredFields();
        final HashMap<String, Object> map = new HashMap<>();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            final String name = underlineName(field.getName());
            try {
                final Object value = field.get(data);
                if (value != null && !name.equals("id")) {
                    map.put(name, value);
                } else if (isNll) {
                    map.put(name, null);
                }
            } catch (IllegalAccessException e) {
                StaticLog.error(e, "update data error");
                return null;
            }
        }
        final UpdateConditionStep<Record> updateConditionStep = dslContext.update(DSL.table(getTableName(aClass))).set(map).where(where);
        String sql = updateConditionStep.getSQL(ParamType.INLINED);
        if (!Objects.equals(active, EnvEnum.PROD.getValue())) {
            StaticLog.info("update sql: {}", sql);
        }
        sql += ";";
        return sql;
    }

    private static String buildDeleteSql(Class<?> c, Condition where) {
        String sql = dslContext.deleteFrom(DSL.table(getTableName(c))).where(where).getSQL(ParamType.
                INLINED);
        if (!Objects.equals(active, EnvEnum.PROD.getValue())) {
            StaticLog.info("delete sql: {}", sql);
        }
        sql += ";";
        return sql;
    }

    private static String buildSelectSql(Class<?> c, Condition where, List<String> colums, String lastSql) {
        List<org.jooq.Field<Object>> fieldList = Arrays.stream(c.getDeclaredFields()).map(field -> DSL.field(underlineName(field.getName()))).toList();
        if (colums != null && !colums.isEmpty()) {
            fieldList = colums.stream().map(DSL::field).toList();
        }
        final SelectConditionStep<Record> selectConditionStep = dslContext.select(fieldList).from(DSL.table(getTableName(c))).where(where);
        String sql = selectConditionStep.getSQL(ParamType.INLINED);
        if (!StrUtil.isBlank(lastSql)) {
            sql += String.format(" %s", lastSql);
        }
        sql += ";";
        if (!Objects.equals(active, EnvEnum.PROD.getValue())) {
            StaticLog.info("select sql: {}", sql);
        }
        return sql;
    }
}
