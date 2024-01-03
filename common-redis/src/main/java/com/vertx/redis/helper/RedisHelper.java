package com.vertx.redis.helper;

import static com.vertx.redis.client.RedisClient.redisClient;
import static io.vertx.core.Future.await;

import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.convert.Convert;
import cn.hutool.log.StaticLog;
import io.vertx.redis.client.Response;

public class RedisHelper {

  public static class Str {
    public static Boolean set(String key, String value) {
      try {
        await(redisClient.set(java.util.List.of(key, value)));
        return true;
      } catch (Throwable e) {
        StaticLog.error(e, "redis set error");
        return false;
      }
    }

    public static Boolean set(String key, String value, Long seconds) {
      try {
        await(redisClient.setex(key, seconds.toString(), value));
        return true;
      } catch (Throwable e) {
        StaticLog.error(e, "redis setex error");
        return false;
      }
    }

    public static String get(String key) {
      try {
        Response value = await(redisClient.get(key));
        return value != null ? value.toString() : null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis get error");
        return null;
      }
    }

    public static Boolean del(String key) {
      try {
        await(redisClient.del(java.util.List.of(key)));
        return true;
      } catch (Throwable e) {
        StaticLog.error(e, "redis del error");
        return false;
      }
    }

    public static Boolean expire(String key, Long seconds) {
      if (seconds == null || seconds <= 0) {
        StaticLog.warn("redis expire seconds is null or less than 0");
        return false;
      }
      try {
        final long time = seconds * 1000;
        await(redisClient.expire(java.util.List.of(key, Long.toString(time))));
        return true;
      } catch (Throwable e) {
        StaticLog.error(e, "redis expire error");
        return false;
      }
    }

    /**
     * setnx
     *
     * @param key
     * @param value
     */

    public static Boolean setnx(String key, String value) {
      try {
        Response value1 = await(redisClient.setnx(key, value));
        return value1 != null && value1.toLong() == 1;
      } catch (Throwable e) {
        StaticLog.error(e, "redis setnx error");
        return false;
      }
    }

    /**
     * incr
     *
     * @param key
     * @return
     */
    public static Long incr(String key) {
      try {
        Response value = await(redisClient.incr(key));
        return value != null ? value.toLong() : null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis incr error");
        return null;
      }
    }

    /**
     * decr
     *
     * @param key
     * @return
     */
    public static Long decr(String key) {
      try {
        Response value = await(redisClient.decr(key));
        return value != null ? value.toLong() : null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis decr error");
        return null;
      }
    }
  }

  public static class Hash {
    /**
     * hset
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    public static Boolean hset(String key, String field, String value) {
      try {
        await(redisClient.hset(java.util.List.of(key, field, value)));
        return true;
      } catch (Throwable e) {
        StaticLog.error(e, "redis hset error");
        return false;
      }
    }

    /**
     * hget
     *
     * @param key
     * @param field
     * @return
     */
    public static String hget(String key, String field) {
      try {
        Response value = await(redisClient.hget(key, field));
        return value != null ? value.toString() : null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis hget error");
        return null;
      }
    }

    /**
     * hdel
     *
     * @param key
     * @param field
     * @return
     */
    public static Boolean hdel(String key, String field) {
      try {
        await(redisClient.hdel(java.util.List.of(key, field)));
        return true;
      } catch (Throwable e) {
        StaticLog.error(e, "redis hdel error");
        return false;
      }
    }

    /**
     * hgetall
     *
     * @param key
     * @return
     */
    public static Map<String, String> hgetall(String key) {
      try {
        Response value = await(redisClient.hgetall(key));
        if (value != null) {
          return value.stream().map(String::valueOf).collect(Collectors.toMap(
              String::toString,
              String::toString));
        }
        return null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis hgetall error");
        return null;
      }
    }

    /**
     * hkeys
     *
     * @param key
     * @return
     */
    public static java.util.List<String> hkeys(String key) {
      try {
        Response value = await(redisClient.hkeys(key));
        if (value != null) {
          return value.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis hkeys error");
        return null;
      }
    }

    /**
     * hvals
     *
     * @param key
     * @return
     */
    public static java.util.List<String> hvals(String key) {
      try {
        Response value = await(redisClient.hvals(key));
        if (value != null) {
          return value.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis hvals error");
        return null;
      }
    }

    /**
     * hlen
     *
     * @param key
     * @return
     */
    public static Long hlen(String key) {
      try {
        Response value = await(redisClient.hlen(key));
        return value != null ? value.toLong() : null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis hlen error");
        return null;
      }
    }

    /**
     * hexists
     *
     * @param key
     * @param field
     * @return
     */
    public static Boolean hexists(String key, String field) {
      try {
        Response value = await(redisClient.hexists(key, field));
        return value != null && value.toLong() == 1;
      } catch (Throwable e) {
        StaticLog.error(e, "redis hexists error");
        return false;
      }
    }
  }

  public static class List {
    /**
     * lpush
     *
     * @param key
     * @param value
     * @return 成功返回true，失败返回false
     */
    public static Boolean lpush(String key, String value) {
      try {
        await(redisClient.lpush(java.util.List.of(key, value)));
        return true;
      } catch (Throwable e) {
        StaticLog.error(e, "redis lpush error");
        return false;
      }
    }

    /**
     * rpush
     *
     * @param key
     * @param value return 成功返回true，失败返回false
     */
    public static Boolean rpush(String key, String value) {
      try {
        await(redisClient.rpush(java.util.List.of(key, value)));
        return true;
      } catch (Throwable e) {
        StaticLog.error(e, "redis rpush error");
        return false;
      }
    }

    /**
     * lpop
     *
     * @param key
     * @param count
     * @return 成功返回value，失败返回null
     */
    public static String lpop(String key, Long count) {
      if (count == null || count <= 0) {
        StaticLog.warn("redis lpop error, count must be greater than 0");
        count = 1L;
      }
      try {
        Response value = await(redisClient.lpop(java.util.List.of(key, Long.toString(count))));
        return value != null ? value.toString() : null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis lpop error");
        return null;
      }
    }

    /**
     * rpop
     *
     * @param key
     * @param count
     * @return 成功返回value，失败返回null
     */
    public static String rpop(String key, Long count) {
      if (count == null || count <= 0) {
        StaticLog.warn("redis rpop error, count must be greater than 0");
        count = 1L;
      }
      try {
        Response value = await(redisClient.rpop(java.util.List.of(key, Long.toString(count))));
        return value != null ? value.toString() : null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis rpop error");
        return null;
      }
    }

    /**
     * llen
     *
     * @param key
     * @return
     */

    public static Long llen(String key) {
      try {
        Response value = await(redisClient.llen(key));
        return value != null ? value.toLong() : null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis llen error");
        return null;
      }
    }

    /**
     * lrange
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public static java.util.List<String> lrange(String key, Long start, Long end) {
      if (start == null || start < 0) {
        StaticLog.error("redis lrange error, start must be greater than 0");
        return null;
      }
      if (end == null || end < 0) {
        StaticLog.error("redis lrange error, end must be greater than 0");
        return null;
      }
      if (start > end) {
        StaticLog.error("redis lrange error, start must be less than end");
        return null;
      }

      try {
        Response value = await(redisClient.lrange(key, Long.toString(start), Long.toString(end)));
        if (value != null) {
          return value.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis lrange error");
        return null;
      }
    }

    /**
     * lindex
     * 
     * @param key
     * @param index
     * @return
     */
    public static String lindex(String key, Long index) {
      if (index == null || index < 0) {
        StaticLog.error("redis lindex error, index must be greater than 0");
        return null;
      }
      try {
        Response value = await(redisClient.lindex(key, Long.toString(index)));
        return value != null ? value.toString() : null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis lindex error");
        return null;
      }
    }

    /**
     * lrem
     * 
     * @param key
     * @param start
     * @param end
     * @return 成功返回true，失败返回false
     */
    public static Boolean lrem(String key, Long start, Long end) {
      if (start == null || start < 0) {
        StaticLog.error("redis lrem error, start must be greater than 0");
        return false;
      }
      if (end == null || end < 0) {
        StaticLog.error("redis lrem error, end must be greater than 0");
        return false;
      }
      if (start > end) {
        StaticLog.error("redis lrem error, start must be less than end");
        return false;
      }
      try {
        Response value = await(redisClient.lrem(key, Long.toString(start), Long.toString(end)));
        return value != null && value.toLong() == 1;
      } catch (Throwable e) {
        StaticLog.error(e, "redis lrem error");
        return false;
      }
    }
  }

  public static class Set {
    /**
     * sadd
     * 
     * @param key
     * @param value
     * @return 成功返回true，失败返回false
     */
    public static Boolean sadd(String key, String value) {
      try {
        await(redisClient.sadd(java.util.List.of(key, value)));
        return true;
      } catch (Throwable e) {
        StaticLog.error(e, "redis sadd error");
        return false;
      }
    }

    /*
     * srem
     * 
     * @param key
     * 
     * @param value
     * 
     * @return 成功返回true，失败返回false
     */
    public static Boolean srem(String key, String value) {
      try {
        await(redisClient.srem(java.util.List.of(key, value)));
        return true;
      } catch (Throwable e) {
        StaticLog.error(e, "redis srem error");
        return false;
      }
    }

    /**
     * scard
     * 
     * @param key
     * @return 成功返回数量，失败返回null
     */
    public static Long scard(String key) {
      try {
        Response value = await(redisClient.scard(key));
        return value != null ? value.toLong() : null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis scard error");
        return null;
      }
    }

    /**
     * sismember
     * 
     * @param key
     * @param value
     * @return 成功返回true，失败返回false
     */
    public static Boolean sismember(String key, String value) {
      try {
        Response response = await(redisClient.sismember(key, value));
        return response != null && response.toLong() == 1;
      } catch (Throwable e) {
        StaticLog.error(e, "redis sismember error");
        return false;
      }
    }

    /**
     * smembers
     * 
     * @param key
     * @return
     */
    public static java.util.List<String> smembers(String key) {
      try {
        Response value = await(redisClient.smembers(key));
        return value != null ? value.stream().map(String::valueOf).collect(Collectors.toList()) : null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis smembers error");
        return null;
      }
    }

    /**
     * srandmember
     * 
     * @param key
     * @param count
     * @return 成功返回list，失败返回null
     */
    public static java.util.List<String> srandmember(String key, Long count) {
      try {
        Response value = await(redisClient.srandmember(java.util.List.of(key, Long.toString(count))));
        return value != null ? value.stream().map(String::valueOf).collect(Collectors.toList()) : null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis srandmember error");
        return null;
      }
    }
  }

  public static class Zset {
    /**
     * zadd
     * 
     * @param key
     * @param score
     * @param value
     * @return 成功返回true，失败返回false
     */
    public static Boolean zadd(String key, Double score, String value) {
      try {
        await(redisClient.zadd(java.util.List.of(key, Double.toString(score), value)));
        return true;
      } catch (Throwable e) {
        StaticLog.error(e, "redis zadd error");
        return false;
      }
    }

    /**
     * zrem
     * 
     * @param key
     * @param value
     * @return 成功返回true，失败返回false
     */
    public static Boolean zrem(String key, String value) {
      try {
        await(redisClient.zrem(java.util.List.of(key, value)));
        return true;
      } catch (Throwable e) {
        StaticLog.error(e, "redis zrem error");
        return false;
      }
    }

    /**
     * zrem
     * 
     * @param key
     * @param value
     * @return 成功返回true，失败返回false
     */
    public static Boolean zrem(String key, Double value) {
      try {
        await(redisClient.zrem(java.util.List.of(key, Double.toString(value))));
        return true;
      } catch (Throwable e) {
        StaticLog.error(e, "redis zrem error");
        return false;
      }
    }

    /**
     * zcard
     * 
     * @param key
     * @return 成功返回数量，失败返回null
     */
    public static Long zcard(String key) {
      try {
        Response value = await(redisClient.zcard(key));
        return value != null ? value.toLong() : null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis zcard error");
        return null;
      }
    }

    /**
     * zrangebyscore
     * 
     * @param key
     * @param min 最小值
     * @param max 最大值
     * @return 成功返回list，失败返回null
     */
    public static java.util.List<String> zrangebyscore(String key, Double min, Double max) {
      try {
        Response value = await(
            redisClient.zrangebyscore(java.util.List.of(key, Double.toString(min), Double.toString(max))));
        return value != null ? value.stream().map(String::valueOf).collect(Collectors.toList()) : null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis zrangebyscore error");
        return null;
      }
    }
  }

  /**
   * Geo
   */
  public static class Geo {
    /**
     * geoadd
     * 
     * @param key
     * @param longitude
     * @param latitude
     * @param member
     * @return 成功返回true，失败返回false
     */
    public static Boolean geoadd(String key, Double longitude, Double latitude, String member) {
      try {
        await(
            redisClient.geoadd(java.util.List.of(key, Double.toString(longitude), Double.toString(latitude), member)));
        return true;
      } catch (Throwable e) {
        StaticLog.error(e, "redis geoadd error");
        return false;
      }
    }

    /**
     * geodist
     * 
     * @param key
     * @param member1
     * @param member2
     * @param unit    m|km|ft|mi
     * @return 成功返回距离，失败返回null
     */
    public static Double geodist(String key, String member1, String member2, String unit) {
      try {
        Response value = await(redisClient.geodist(java.util.List.of(key, member1, member2, unit)));
        return value != null ? value.toDouble() : null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis geodist error");
        return null;
      }
    }

    /**
     * geopos
     * 
     * @param key
     * @param member
     * @return 成功返回经纬度，失败返回null
     */
    public static java.util.List<Double> geopos(String key, String member) {
      try {
        Response value = await(redisClient.geopos(java.util.List.of(key, member)));
        return value != null ? value.stream().map(item -> {
          return Convert.toDouble(item);
        }).collect(Collectors.toList()) : null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis geopos error");
        return null;
      }
    }

    /**
     * geohash
     * 
     * @param key
     * @param member
     * @return 成功返回hash，失败返回null
     */
    public static String geohash(String key, String member) {
      try {
        Response value = await(redisClient.geohash(java.util.List.of(key, member)));
        return value != null ? value.toString() : null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis geohash error");
        return null;
      }
    }

    /**
     * georadius
     * 
     * @param key
     * @param longitude 经度
     * @param latitude  纬度
     * @param radius    范围 单位米
     * @param unit      m|km|ft|mi
     * @return 成功返回list，失败返回null
     */
    public static java.util.List<String> georadius(String key, Double longitude, Double latitude, Double radius,
        String unit) {
      try {
        Response value = await(redisClient.georadius(java.util.List.of(key, Double.toString(longitude),
            Double.toString(latitude), Double.toString(radius), unit)));

        return value != null ? value.stream().map(String::valueOf).collect(Collectors.toList()) : null;
      } catch (Throwable e) {
        StaticLog.error(e, "redis georadius error");
        return null;
      }
    }

    /**
     * georadiusbymember
     * 
     * @param key
     * @param member
     * @param radius
     * @param unit
     * @return 成功返回list，失败返回null
     */
    public static java.util.List<String> georadiusbymember(String key, String member, Double radius, String unit) {
      try {
        Response value = await(
            redisClient.georadiusbymember(java.util.List.of(key, member, Double.toString(radius), unit)));

        return value != null ? value.stream().map(String::valueOf).collect(Collectors.toList()) : null;

      } catch (Throwable e) {
        StaticLog.error(e, "redis georadiusbymember error");
        return null;
      }
    }

  }

  /**
   * Tran
   */
  public static class Tran {
    /**
     * multi
     * 
     * @return 成功返回true，失败返回false
     */
    public static Boolean multi() {
      try {
        await(redisClient.multi());
        return true;
      } catch (Throwable e) {
        StaticLog.error(e, "redis multi error");
        return false;
      }
    }

    /**
     * exec
     * 
     * @return 成功返回true，失败返回false
     */
    public static Boolean exec() {
      try {
        await(redisClient.exec());
        return true;
      } catch (Throwable e) {
        StaticLog.error(e, "redis exec error");
        return false;
      }
    }

    /**
     * discard
     * 
     * @return 成功返回true，失败返回false
     */
    public static Boolean discard() {
      try {
        await(redisClient.discard());
        return true;
      } catch (Throwable e) {
        StaticLog.error(e, "redis discard error");
        return false;
      }
    }

    /**
     * watch
     * 
     * @param key
     * @return 成功返回true，失败返回false
     */
    public static Boolean watch(String key) {
      try {
        await(redisClient.watch(java.util.List.of(key)));
        return true;
      } catch (Throwable e) {
        StaticLog.error(e, "redis watch error");
        return false;
      }
    }

    /**
     * unwatch
     * 
     * @param key
     * @return 成功返回true，失败返回false
     */
    public static Boolean unwatch(String key) {
      try {
        await(redisClient.unwatch());
        return true;
      } catch (Throwable e) {
        StaticLog.error(e, "redis unwatch error");
        return false;
      }
    }
    
  }
}
