package com.vertx.mysql.helper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.log.StaticLog;
import com.vertx.common.core.entity.db.QueryPageParam;
import com.vertx.common.core.entity.db.QueryPageResponse;
import com.vertx.common.core.enums.EnvEnum;
import com.vertx.common.core.utils.StrUtil;
import com.vertx.mysql.exception.DbException;

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
import java.util.function.Function;

import static com.vertx.common.core.config.VertxLoadConfig.active;
import static com.vertx.common.core.utils.StrUtil.underlineName;
import static com.vertx.mysql.client.MysqlClient.mysqlClient;
import static com.vertx.mysql.utils.TableUtil.getTableName;
import static io.vertx.core.Future.await;

public class MysqlHelper {

  /**
   * 最新的警告日志时间
   */
  private static final HashMap<String, LocalDateTime> lastWarnNoIdLogTimeMap = new HashMap<>();
  private static final DSLContext dslContext = DSL.using(SQLDialect.MYSQL, new Settings());

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

  public static int updateBatch(List<Map<Object, Condition>> dataList, Boolean isNll, int batchSize,
      SqlConnection sqlConnection) {
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

  public static <T> QueryPageResponse<T> selectPage(Class<T> c, Condition where, QueryPageParam<?> queryPageParam) {
    final Integer page = queryPageParam.getPage();
    final Integer size = queryPageParam.getSize();
    final String sort = queryPageParam.getSort();
    final String order = queryPageParam.getOrder();
    if (page == null || page < 1) {
      StaticLog.error(">>>>>> 查询页码不能为空或小于1");
      throw new DbException("查询页码不能为空或小于1");
    }
    if (size == null || size < 1) {
      StaticLog.error(">>>>>> 查询页大小不能为空或小于1");
      throw new DbException("查询页大小不能为空或小于1");
    }
    if (StrUtil.isBlank(sort)) {
      StaticLog.error(">>>>>> 查询排序字段不能为空");
      throw new DbException("查询排序字段不能为空");
    }
    if (StrUtil.isBlank(order)) {
      StaticLog.error(">>>>>> 查询排序顺序不能为空");
      throw new DbException("查询排序顺序不能为空");
    }
    final String lastSql = String.format("order by %s %s limit %d,%d", sort, order, (page - 1) * size, size);
    final List<T> data = selectDsl(c, where, null, lastSql);
    final Integer total = selectCount(c, where);
    final QueryPageResponse<T> queryPageResponse = new QueryPageResponse<>();
    queryPageResponse.setPage(page);
    queryPageResponse.setSize(size);
    queryPageResponse.setData(data);
    queryPageResponse.setTotal(total);
    return queryPageResponse;
  }

  /**
   * 在事务中执行
   *
   * @param func 事务执行函数,参数为数据库连接,返回值表示事务执行结果
   * @return 事务执行结果
   * @throws Throwable 事务执行失败
   */
  public <T> T withTransaction(Function<SqlConnection, T> func) {
    SqlConnection connection = null;
    Transaction transaction = null;
    T result;
    try {
      connection = await(mysqlClient.getConnection());
      transaction = await(connection.begin());
      // 执行事务
      result = func.apply(connection);
      // 提交事务
      await(transaction.commit());
    } catch (Throwable e) {
      // 回滚事务
      StaticLog.error(e, "事务执行失败");
      if (transaction != null) {
        await(transaction.rollback());
        StaticLog.error("事务已回滚");
      }
      throw e;
    } finally {
      // 关闭连接
      if (connection != null) {
        await(connection.close());
      }
    }
    return result;
  }

  private static Long installDsl(Object data, SqlConnection sqlConnection) {
    if (data == null) {
      StaticLog.error("插入数据为空");
      throw new DbException("插入数据为空");
    }
    if (data instanceof String) {
      StaticLog.error("插入数据为空");
      throw new DbException("插入数据为空");
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
      // 检查最后一次警告时间和现在事件是否相差超过一小时,一小时内只会报警一次
      if (!lastWarnNoIdLogTimeMap.containsKey(name) || !now.isBefore(lastWarnNoIdLogTimeMap.get(name).plusHours(1))) {
        lastWarnNoIdLogTimeMap.put(name, now);
        StaticLog.warn("insert data: {} without id", name);
      }
      throw new DbException("插入数据失败", e);
    }
  }

  private static int insertDsl(List<Object> data, int batchSize, SqlConnection connection) {
    if (data == null || data.isEmpty()) {
      StaticLog.error("批量插入数据为空");
      throw new DbException("批量插入数据为空");
    }
    // 影响行数
    int count = 0;
    // 批量插入数据
    final List<List<Object>> lists = CollUtil.split(data, batchSize);
    boolean isHaveConnection = connection != null;
    final SqlConnection connect = connection != null ? connection : await(mysqlClient.getConnection());
    Transaction parentTransaction = connect.transaction();
    boolean isHaveTransaction = parentTransaction != null;
    // 开启事务
    Transaction transaction = null;
    if (!isHaveTransaction) {
      transaction = await(connect.begin());
    }
    try {
      for (List<Object> list : lists) {
        final String querySql = buildInsertSql(list);
        final RowSet<Row> rows = await(connect.query(querySql).execute());
        count += rows.rowCount();
      }
      // 提交事务
      if (!isHaveTransaction && transaction != null) {
        await(transaction.commit());
      }
    } catch (Throwable e) {
      StaticLog.error(e, "批量插入数据失败");
      // 回滚事务
      if (!isHaveTransaction && transaction != null) {
        await(transaction.rollback());
        StaticLog.error("事务已回滚");
      }
      throw new DbException("批量插入数据失败", e);
    } finally {
      if (!isHaveConnection) {
        await(connect.close());
      }
    }
    return count;
  }

  private static int updateDsl(Object data, Condition where, Boolean isNll, SqlConnection sqlConnection) {
    if (data == null) {
      StaticLog.error("更新数据为空");
      throw new DbException("更新数据为空");
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

  private static int updateBatchDsl(List<Map<Object, Condition>> dataList, Boolean isNll, int batchSize,
      SqlConnection sqlConnection) {
    if (dataList.isEmpty()) {
      StaticLog.error(">>>>>> 批量更新数据为空");
      throw new DbException("批量更新数据为空");
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
    // 是否有数据库连接
    boolean isHaveConnection = sqlConnection != null;
    final SqlConnection connect = sqlConnection != null ? sqlConnection : await(mysqlClient.getConnection());
    final Transaction parentTransaction = connect.transaction();
    boolean isHaveTransaction = parentTransaction != null;
    // 开启事务
    Transaction transaction = null;
    // 如果外部传入了事务,则不需要开启事务
    if (!isHaveTransaction) {
      transaction = await(connect.begin());
    }
    try {
      for (List<String> list : lists) {
        for (String querySql : list) {
          final RowSet<Row> rows = await(connect.query(querySql).execute());
          count += rows.rowCount();
        }
      }
      // 提交事务,如果外部传入了事务,则不需要提交事务,由外部提交事务
      if (!isHaveTransaction && transaction != null) {
        await(transaction.commit());
      }
    } catch (Throwable e) {
      // 回滚事务 如果外部传入了事务,则不需要回滚事务,由外部回滚事务
      if (!isHaveTransaction && transaction != null) {
        await(transaction.rollback());
        StaticLog.error("事务已回滚");
      }
      StaticLog.error(e, ">>>>>> 批量更新数据失败", lists);
      throw new DbException("批量更新数据失败", e);
    } finally {
      if (!isHaveConnection) {
        await(connect.close());
      }
    }
    return count;
  }

  private static int deleteDsl(Class<?> c, Condition where, SqlConnection sqlConnection) {
    if (where == null) {
      StaticLog.error(">>>>>> 删除数据条件不能为空");
      throw new DbException("删除数据条件不能为空");
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

  private static <T> List<T> selectDsl(Class<T> clazz, Condition where,
      List<String> fields,
      String lastSql) {
    if (where == null) {
      StaticLog.error(">>>>>> 查询数据条件不能为空");
      throw new DbException("查询数据条件不能为空");
    }
    final String sql = buildSelectSql(clazz, where, fields, lastSql);
    if (StrUtil.isBlank(sql)) {
      throw new DbException("查询数据失败,生成sql为空,请检查查询条件是否正确");
    }
    final RowSet<Row> rowRowSet = await(mysqlClient.query(sql).execute());
    final List<T> list = new ArrayList<>(rowRowSet.size());
    for (Row row : rowRowSet) {
      list.add(row.toJson().mapTo(clazz));
    }
    return list;
  }

  private static String buildInsertSql(List<Object> datas) {
    if (datas.isEmpty()) {
      StaticLog.error(">>>>>> 插入数据为空");
      throw new DbException("插入数据为空");
    }
    final Class<?> aClass = datas.getFirst().getClass();
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
          throw new DbException("插入数据失败", e);
        }
        fileKeyValueList.add(map);
      }
    }
    final InsertValuesStepN<Record> insertValuesStepN = dslContext.insertInto(DSL.table(getTableName(aClass)),
        fileKeyValueList.getFirst().keySet().stream().map(DSL::field).toList());
    for (HashMap<String, Object> map : fileKeyValueList) {
      insertValuesStepN.values(map.values().toArray());
    }

    String sql = insertValuesStepN.getSQL(ParamType.INLINED);
    if (!Objects.equals(active, EnvEnum.PROD.getValue())) {
      StaticLog.debug("insert sql: {}", sql);
    }
    sql += ";";
    return sql;
  }

  private static String buildUpdateSql(Object data, Condition where, Boolean isNll) {
    if (data == null) {
      StaticLog.error(">>>>>> 更新数据为空");
      throw new DbException("更新数据为空");
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
        StaticLog.error(e, "更新数据失败");
        throw new DbException("更新数据失败", e);
      }
    }
    final UpdateConditionStep<Record> updateConditionStep = dslContext.update(DSL.table(getTableName(aClass))).set(map)
        .where(where);
    String sql = updateConditionStep.getSQL(ParamType.INLINED);
    if (!Objects.equals(active, EnvEnum.PROD.getValue())) {
      StaticLog.debug("update sql: {}", sql);
    }
    sql += ";";
    return sql;
  }

  private static String buildDeleteSql(Class<?> c, Condition where) {
    String sql = dslContext.deleteFrom(DSL.table(getTableName(c))).where(where).getSQL(ParamType.INLINED);
    if (!Objects.equals(active, EnvEnum.PROD.getValue())) {
      StaticLog.debug("delete sql: {}", sql);
    }
    sql += ";";
    return sql;
  }

  private static <T> Integer selectCount(Class<T> c, Condition where) {
    String sql = dslContext.selectCount().from(DSL.table(getTableName(c))).where(where).getSQL(ParamType.INLINED);
    if (!Objects.equals(active, EnvEnum.PROD.getValue())) {
      StaticLog.debug("select count sql: {}", sql);
    }
    final RowSet<Row> rowRowSet = await(mysqlClient.query(sql).execute());
    return rowRowSet.iterator().next().getInteger(0);
  }

  private static <T> String buildSelectSql(Class<T> c, Condition where,
      List<String> columns,
      String lastSql) {
    // 检验columns是否都存在在c中
    if (columns != null && !columns.isEmpty()) {
      final Field[] declaredFields = c.getDeclaredFields();
      for (String column : columns) {
        boolean isHave = false;
        for (Field declaredField : declaredFields) {
          if (underlineName(declaredField.getName()).equals(column)) {
            isHave = true;
            break;
          }
        }
        if (!isHave) {
          StaticLog.error(">>>>>> select column: {} not exist in {}", column, c.getName());
          throw new DbException("查询字段不存在" + column + " in " + c.getName());
        }
      }
    }
    List<org.jooq.Field<Object>> fieldList = new ArrayList<>(Arrays.stream(c.getDeclaredFields())
        .map(field -> DSL.field(underlineName(field.getName()))).toList());
    if (columns != null && !columns.isEmpty()) {
      fieldList = columns.stream().map(s -> DSL.field(underlineName(s))).toList();
    }
    final SelectConditionStep<Record> selectConditionStep = dslContext.select(fieldList)
        .from(DSL.table(getTableName(c))).where(where);
    String sql = selectConditionStep.getSQL(ParamType.INLINED);
    if (!StrUtil.isBlank(lastSql)) {
      sql += String.format(" %s", lastSql);
    }
    sql += ";";
    if (!Objects.equals(active, EnvEnum.PROD.getValue())) {
      StaticLog.debug("select sql: {}", sql);
    }
    return sql;
  }
}
