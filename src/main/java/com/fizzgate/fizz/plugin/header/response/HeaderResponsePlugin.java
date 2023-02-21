package com.fizzgate.fizz.plugin.header.response;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import com.fizzgate.plugin.FizzPluginFilterChain;
import com.fizzgate.plugin.auth.ApiConfig;
import com.fizzgate.plugin.core.filter.AbstractFizzPlugin;

import java.util.List;
import java.util.Set;

/**
 * @author huanghua
 */
@Slf4j
@Component
public class HeaderResponsePlugin extends AbstractFizzPlugin<RouterConfig, PluginConfig> {

    @Override
    public String pluginName() {
        return "header_response_plugin";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Void> doFilter(ServerWebExchange exchange) {
        RouterConfig routerConfig = routerConfig(exchange);
        ApiConfig apiConfig = apiConfig(exchange);
        PluginConfig pluginConfig = pluginConfig(exchange);
        List<PluginConfig.Item> pluginConfigAllList =
                (pluginConfig == null || pluginConfig.getConfigs() == null)
                        ? Lists.newArrayList() : pluginConfig.getConfigs();
        Set<String> gatewayGroups = apiConfig.gatewayGroups;
        gatewayGroups = gatewayGroups == null ? Sets.newHashSet() : gatewayGroups;
        List<PluginConfig.Header> headConfigs = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(pluginConfigAllList)) {
            for (PluginConfig.Item item : pluginConfigAllList) {
                if (gatewayGroups.contains(item.getGwGroup())) {
                    if (CollectionUtils.isNotEmpty(item.getHeaders())) {
                        headConfigs.addAll(item.getHeaders());
                    }
                }
            }
        }

        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = HttpHeaders.writableHttpHeaders(response.getHeaders());
        processHeader(headers, routerConfig, headConfigs);
        return FizzPluginFilterChain.next(exchange);
    }

    private void processHeader(HttpHeaders headers, RouterConfig routerConfig, List<PluginConfig.Header> pluginHeadConfigs) {
        // 因为路由配置优先于插件配置，所以先处理插件配置，再处理路由配置
        for (PluginConfig.Header pluginHeadConfig : pluginHeadConfigs) {
            processHeader(headers, pluginHeadConfig);
        }
        if (routerConfig != null && CollectionUtils.isNotEmpty(routerConfig.getHeaders())) {
            for (PluginConfig.Header head : routerConfig.getHeaders()) {
                processHeader(headers, head);
            }
        }
    }

    private void processHeader(HttpHeaders headers, PluginConfig.Header header) {
        if (header == null) {
            return;
        }
        PluginConfig.Action action = header.getAction();
        String name = header.getName();
        String value = header.getValue();
        if (action == null || StringUtils.isBlank(name)) {
            return;
        }
        switch (action) {
            case OVERRIDE:
                headers.set(name, value);
                break;
            case APPEND:
                List<String> values = headers.get(name);
                if (CollectionUtils.isNotEmpty(values)) {
                    if (values.size() == 1) {
                        headers.set(name, value);
                    } else {
                        headers.add(name, value);
                    }
                } else {
                    headers.set(name, value);
                }
                break;
            case DELETE:
                headers.remove(name);
                break;
            case SKIP:
                if (!headers.containsKey(name)) {
                    headers.set(name, value);
                }
                break;
            case ADD:
                headers.add(name, value);
                break;
        }
    }

}
