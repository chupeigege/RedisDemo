package vip.aquan.redisdemo.controller;

import ch.qos.logback.core.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.aquan.redisdemo.entity.Location;
import vip.aquan.redisdemo.entity.Result;
import vip.aquan.redisdemo.util.Constants;
import vip.aquan.redisdemo.util.RedisTemplateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author: wcp
 * @date: 2020/12/6 21:39
 * @Description:
 */
@RestController
public class RedisDemoController {

    @Autowired
    private RedisTemplateUtil redisTemplateUtil;

    /**
     * 获取list缓存经纬度数据
     *
     * @return
     */
    @GetMapping("/getDataByList")
    public Object getDataByList(Integer start, Integer end) {
        if (start == null) {
            start = 0;
        }
        if (end == null) {
            end = -1;
        }
        List list = redisTemplateUtil.getList(Constants.COORDINATE_DATA_LIST_KEY, start, end);
        return list;

    }

    /**
     * 缓存list经纬度数据
     *
     * @return
     */
    @GetMapping("/redisCacheByList")
    public Object redisCacheByList() {
        if (redisTemplateUtil.hasKey(Constants.COORDINATE_DATA_LIST_KEY)) {
            redisTemplateUtil.delete(Constants.COORDINATE_DATA_LIST_KEY);
        }
        List<Location> locations = new ArrayList<>();
        Location location1 = new Location().setX(116.411573).setY(39.897958);
        Location location2 = new Location().setX(117.199842).setY(39.081914);
        locations.add(location1);
        locations.add(location2);

        //首次得先在redis创建key，否则会空指针异常？
        long total = redisTemplateUtil.setList2RightPushAll(Constants.COORDINATE_DATA_LIST_KEY, locations);

        return "成功缓存了" + total + "条数据";
    }

    /**
     * 根据定点位置，使用geo算法获取缓存经纬度数据
     * 解析：为什么要存geo数据+hash数据，因为geo数据得值，猜测只能是Point，才能支持定理查询
     * 而查出来得数据并不详细，因此，另用一个hash存详细信息，从geo拿到需要得数据才去hash根据key筛查
     *
     * @return
     */
    @GetMapping("/getDataByGeo")
    public Object getDataByGeo(Double radius, Double x, Double y) {
        // 半径 (单位 km)
        if (radius == null) {
            radius = 0.5d;
        }
        Distance distance = new Distance(radius, Metrics.KILOMETERS);
        //目标点,后边坐标应该改成可传
        Point point = new Point(116.411573, 39.897958);
        // 封装覆盖的面积
        Circle circle = new Circle(point, distance);
        // 根据指定坐标查询附近范围其他坐标
        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = redisTemplateUtil.nearByXY(Constants.COORDINATE_GEO_DATA_SET_KEY, circle);
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> resultsContent = geoResults.getContent();
        List<String> locationIdList = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> geoLocationGeoResult : resultsContent) {
            RedisGeoCommands.GeoLocation<String> geoLocation = geoLocationGeoResult.getContent();
            // 编号
            String locationId = geoLocation.getName();
            locationIdList.add(locationId);
        }
        // 获取Location详情信息
        List<Object> locationList = redisTemplateUtil.getHashBatch(Constants.COORDINATE_DATA_HASH_KEY, locationIdList);
        return locationList;
    }

    /**
     * 基于geo缓存经纬度数据
     *
     * @return
     */
    @GetMapping("/redisCacheByGeo")
    public Object redisCacheByGeo() {
        Result b1 = this.cacheGeo();
        Result b2 = this.cacheHash();
        return Result.ok(b1.getData() + "    " + b2.getData());
    }

    public Result cacheHash() {
        if (redisTemplateUtil.hasKey(Constants.COORDINATE_DATA_HASH_KEY)) {
            redisTemplateUtil.delete(Constants.COORDINATE_DATA_HASH_KEY);
        }
        Map<String, Object> hashMap = new HashMap<>();
        Location location = new Location().setId("1").setAddress("北京天安门").setPhone("123123456")
                .setX(116.411573).setY(39.897958).setXy("116.411573,39.897958");
        Location location2 = new Location().setId("2").setAddress("广州塔").setPhone("56456464")
                .setX(117.199842).setY(39.081914).setXy("117.199842,39.081914");
        hashMap.put(location.getId(), location);
        hashMap.put(location2.getId(), location2);
        redisTemplateUtil.putAllHash(Constants.COORDINATE_DATA_HASH_KEY, hashMap);
        return Result.ok("缓存hash经纬度行数 : " + hashMap.size());
    }

    public Result cacheGeo() {
        if (redisTemplateUtil.hasKey(Constants.COORDINATE_GEO_DATA_SET_KEY)) {
            redisTemplateUtil.delete(Constants.COORDINATE_GEO_DATA_SET_KEY);
        }
        Map<String, Object> pointMap = new HashMap();
        pointMap.put("1", new Point(116.411573, 39.897958));
        pointMap.put("2", new Point(117.199842, 39.081914));
        Long geoBatchAddCount = redisTemplateUtil.geoBatchAdd(Constants.COORDINATE_GEO_DATA_SET_KEY, pointMap);
        return Result.ok("缓存至GEO经纬度行数 : " + geoBatchAddCount);
    }


}
