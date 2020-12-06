package vip.aquan.redisdemo.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: wcp
 * @date: 2020/12/6 21:45
 * @Description:
 */
@Data
@Accessors(chain = true)
public class Location {
    private double x;//经度
    private double y;//纬度
    private String xy;//经纬度
}
