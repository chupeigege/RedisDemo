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
    /**编号*/
    private String id;
    /**地址*/
    private String address;
    /**电话*/
    private String phone;
    /**经度*/
    private double x;
    /**纬度*/
    private double y;
    /**经纬度(格式x,y)*/
    private String xy;
}
