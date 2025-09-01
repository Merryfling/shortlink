package dev.chanler.shortlink.toolkit.ipgeo;

/**
 * IP 地理位置查询客户端
 * @author: Chanler
 */
public interface IpGeoClient {

    /**
     * 查询 IP 的地理位置信息
     * @param ip IP 地址
     * @return
     */
    GeoInfo query(String ip);
}
