package com.chaitin.pandawiki.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * IP地址信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IPAddressInfo {
    
    /**
     * IP地址
     */
    private String ip;
    
    /**
     * 国家
     */
    private String country;
    
    /**
     * 省份
     */
    private String province;
    
    /**
     * 城市
     */
    private String city;
} 