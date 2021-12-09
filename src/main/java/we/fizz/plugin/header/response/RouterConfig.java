package we.fizz.plugin.header.response;

import com.google.common.collect.Lists;
import lombok.Data;
import we.plugin.core.filter.config.FizzConfig;

import java.util.List;

import static we.fizz.plugin.header.response.PluginConfig.Header;

/**
 * @author huanghua
 */
@Data
@FizzConfig
public class RouterConfig {
    private List<Header> headers = Lists.newArrayList();
}
