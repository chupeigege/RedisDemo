package vip.aquan.redisdemo.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author wcp
 * @date: 2020/12/06
 * @description: redis工具类
 */
@Component
public class RedisTemplateUtil {

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public String increment(String k) {
        return String.valueOf(redisTemplate.opsForValue().increment(k, 1L));
    }

    public void set(String key, Object value) {
        stringRedisTemplate.opsForValue().set(key, String.valueOf(value));
    }

    public void set2Seconds(String key, Object value, int seconds) {
        stringRedisTemplate.opsForValue().set(key, String.valueOf(value), seconds, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     *
     * @param key
     * @return boolean
     */
    public boolean hasKey(String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        return redisTemplate.hasKey(key);
    }

    /**
     * set redis:list类型
     *
     * @param key   key
     * @param value value
     * @return long
     */
    public long setList2LeftPush(String key, String value) {
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        return listOperations.leftPush(key, value);
    }

    /**
     * set redis:list类型
     *
     * @param key   key
     * @param value value
     * @return long
     */
    public long setList2RightPush(String key, String value) {
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        return listOperations.rightPush(key, value);
    }

    /**
     * set redis:list类型  批量新增
     *
     * @param key  key
     * @param list value
     * @return long
     */
    public long setList2RightPushAll(String key, List list) {
        return redisTemplate.opsForList().rightPushAll(key, list);
    }

    /**
     * get redis:list类型
     *
     * @param key   key
     * @param start start
     * @param end   end
     * @return
     */
    public List<String> getList(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * put redis:hash类型 批量插入
     *
     * @param key key
     * @param map Map<hk,hv>
     */
    public void putAllHash(String key, Map map) {
        BoundHashOperations<String, String, Object> boundHashOps = redisTemplate.boundHashOps(key);
        boundHashOps.putAll(map);
    }

    /**
     * get redis:hash类型 批量获取
     *
     * @param key key
     * @param hkList list<hk>
     */
    public List<Object> getHashBatch(String key,List<String> hkList) {
        BoundHashOperations<String, String, Object> boundHashOps = redisTemplate.boundHashOps(key);
        return boundHashOps.multiGet(hkList);
    }

    public Object get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void expireMinutes(String key, int minutes) {
        stringRedisTemplate.expire(key, minutes, TimeUnit.MINUTES);
    }

    public void expireSeconds(String key, int seconds) {
        stringRedisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    public Boolean setNX(byte[] key, byte[] value) {
        return redisTemplate.getConnectionFactory().getConnection().setNX(key, value);
    }

    /**
     * add redis:geo
     * 添加地理位置的坐标。
     *
     * @param key   key
     * @param point point
     * @param pName pName
     * @return Long
     */
    public Long geoAdd(String key, Point point, String pName) {
        return redisTemplate.opsForGeo().add(key, point, pName);
    }

    /**
     * geo批量插入
     *
     * @param key   key
     * @param point point
     * @return
     */
    public Long geoBatchAdd(String key, Map point) {
        return redisTemplate.boundGeoOps(key).add(point);
    }

    /**
     * get redis:geo
     * 获取地理位置的坐标。可根据坐标名称批量获取
     *
     * @param key     key
     * @param memList memList
     * @return List<Point>
     */
    public List<Point> geoGet(String key, List<String> memList) {
        return redisTemplate.opsForGeo().position(key, memList);
    }


    /**
     * nearByXY redis:geo
     * 根据给定的经纬度坐标来获取指定范围内的地理位置集合。
     * --------------------------------------------------------------------------------------
     * georadius key longtitude latitude radius m|km|ft|mi [WITHCOORD] [WITHDIST] [WITHHASH] [COUNT count] [ASC|DESC]
     * georadius命令参数:
     * radius 半径长度，必选项。后面的 m 、 km 、 ft 、 mi 、是长度单位选项，四选一。
     * WITHCOORD 将位置元素的经度和维度也一并返回，非必选。
     * WITHDIST 在返回位置元素的同时， 将位置元素与中心点的距离也一并返回。 距离的单位和查询单位一致，非必选。
     * WITHHASH 返回位置的52位精度的 Geohash 值，非必选。可能其它一些偏向底层的 LBS 应用服务需要这个。
     * COUNT 返回符合条件的位置元素的数量，非必选。比如返回前10个，以避免出现符合的结果太多而出现性能问题。
     * ASC / DESC 排序方式，非必选。默认情况下返回未排序，但是大多数我们需要进行排序。参照中心位置，从近到远使用 ASC ，从远到近使用 DESC 。
     * --------------------------------------------------------------------------------------
     *
     * @param key    key
     * @param circle 面积对象 (坐标,范围) new Circle(new Point(x,y), new Distance(5, Metrics.KILOMETERS)) 半径5km的范围
     * @return GeoResults
     */
    public GeoResults<RedisGeoCommands.GeoLocation<String>> nearByXY(String key, Circle circle) {
        /*
         * 使用 GeoRadiusCommandArgs 封装 GEORADIUS 的一些可选命令参数
         * includeDistance 包含距离
         * includeCoordinates 包含经纬度
         * sortAscending 正序排序
         * limit 限定返回的记录数
         */
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().includeCoordinates().sortAscending();
        return (GeoResults<RedisGeoCommands.GeoLocation<String>>) redisTemplate.opsForGeo().radius(key, circle, args);
    }


    /**
     * 根据指定的地点查询半径在指定范围内的位置
     * <p>
     * redis命令：georadiusbymember key 北京 100 km WITHDIST WITHCOORD ASC COUNT 5
     */
    public GeoResults<RedisGeoCommands.GeoLocation<String>> nearByPlace(String key, String member, Distance distance, long count) {
        // includeDistance 包含距离
        // includeCoordinates 包含经纬度
        // sortAscending 正序排序
        // limit 限定返回的记录数
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance().includeCoordinates().sortAscending().limit(count);
        return redisTemplate.opsForGeo().radius(key, member, distance, args);
    }


}

