package com.xxx.xcloud.module.application.entity;

import java.util.Map;

import lombok.Data;

/**
 *
 * @author mengaijun
 * @Description: TODO
 * @date: 2019年11月11日 下午3:30:05
 */
@Data
public class HttpData {

    private Map<String, String> httpHeade;

    private String path;

    private Integer port;

}
