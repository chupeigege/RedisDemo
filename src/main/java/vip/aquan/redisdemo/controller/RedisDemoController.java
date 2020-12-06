package vip.aquan.redisdemo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.aquan.redisdemo.entity.Location;
import vip.aquan.redisdemo.util.RedisTemplateUtil;

import java.util.ArrayList;
import java.util.List;

import static vip.aquan.redisdemo.util.Constants.COORDINATE_DATA_KEY;

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
     * 获取缓存经纬度数据
     * @return
     */
    @GetMapping("/getData")
    public Object coordinateData(Integer start, Integer end) {
        if (start == null) {
            start = 0;
        }
        if (end == null) {
            end = -1;
        }
        List list = redisTemplateUtil.getList(COORDINATE_DATA_KEY, start, end);
        return list;

    }

    @GetMapping("/redisCache")
    public Object redisCache(){
        if (redisTemplateUtil.hasKey(COORDINATE_DATA_KEY)) {
            redisTemplateUtil.delete(COORDINATE_DATA_KEY);
        }
        List<Location> locations = new ArrayList<>();
        Location location1 = new Location().setX(116.411573).setY(39.897958);
        Location location2 = new Location().setX(117.199842).setY(39.081914);
        locations.add(location1);
        locations.add(location2);

        //首次得先在redis创建key，否则会空指针异常？
        long total = redisTemplateUtil.setList2RightPushAll(COORDINATE_DATA_KEY, locations);

        return "成功缓存了"+total+"条数据";
    }

}
