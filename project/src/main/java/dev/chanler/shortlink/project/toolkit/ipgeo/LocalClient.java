package dev.chanler.shortlink.project.toolkit.ipgeo;

import org.lionsoul.ip2region.xdb.Searcher;

/**
 * IP 地理位置查询本地客户端(ip2region xdb, 全内存模式)
 * @author: Chanler
 */
public class LocalClient implements IpGeoClient {

    /**
     * 完全基于内存的查询对象
     */
    private final Searcher searcher;

    public LocalClient(String dbPath) {
        try {
            byte[] cBuff = Searcher.loadContentFromFile(dbPath);
            this.searcher = Searcher.newWithBuffer(cBuff); // 全内存，无 IO
        } catch (Exception e) {
            throw new IllegalStateException("Init ip2region Searcher failed. dbPath=" + dbPath, e);
        }
    }

    @Override
    public GeoInfo query(String ip) {
        try {
            String region = searcher.search(ip);
            String[] a = region == null ? new String[0] : region.split("\\|", -1);
            String country  = a.length > 0 ? a[0] : null;
            String province = a.length > 2 ? a[2] : null;
            String city     = a.length > 3 ? a[3] : null;
            String isp      = a.length > 4 ? a[4] : null;
            return GeoInfo.builder()
                    .country(country)
                    .province(province)
                    .city(city)
                    .isp(isp)
                    .build();
        } catch (Exception e) {
            System.out.printf("failed to search(%s): %s\n", ip, e);
        }
        return null;
    }
}
