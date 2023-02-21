package com.fizzgate.fizz.plugin.header.response;

import com.google.common.collect.Lists;
import lombok.Data;
import com.fizzgate.plugin.core.filter.config.FizzConfig;

import java.util.List;

/**
 * @author huanghua
 */
@Data
@FizzConfig
public class RouterConfig {
    private List<PluginConfig.Header> headers = Lists.newArrayList();
}
