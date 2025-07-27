package dev.chanler.shortlink.project.toolkit.ipgeo;

import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

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
            // 文件放在 resources/ip2region.xdb
            ClassPathResource resource = new ClassPathResource("ip2region.xdb");
            byte[] buffer = FileCopyUtils.copyToByteArray(resource.getInputStream());
            this.searcher = Searcher.newWithBuffer(buffer); // 全内存，无 IO
        } catch (Exception e) {
            throw new IllegalStateException("Init ip2region Searcher failed. dbPath=" + dbPath, e);
        }
    }

    @Override
    public GeoInfo query(String ip) {
        try {
            String region = searcher.search(ip);
            String[] a = region == null ? new String[0] : region.split("\\|", -1);
            String country  = a.length > 0 ? a[0] : "Unknown";
            String province = a.length > 2 ? a[2] : "Unknown";
            String city     = a.length > 3 ? a[3] : "Unknown";
            String isp      = a.length > 4 ? a[4] : "Unknown";
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
