package fi.vm.yti.datamodel.api.config;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import fi.vm.yti.datamodel.api.utils.LDHelper;
import javax.annotation.PostConstruct;

@ConfigurationProperties("uri")
@Component
@Validated
public class UriProperties {

    @NotNull
    private String host;

    private String port;

    @NotNull
    private String scheme;

    @NotNull
    private String contextPath;

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(final String port) {
        this.port = port;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(final String scheme) {
        this.scheme = scheme;
    }

    public String getContextPath() {
        return "/" + this.contextPath.replaceAll("^/|/$", "") + "/";
    }

    public void setContextPath(final String contextPath) {
        this.contextPath = contextPath;
    }

    public String getUriHostPathAddress() {
        return this.scheme + "://" + this.host + this.contextPath;
    }

    @PostConstruct
    public void iowToPrefix(){
        LDHelper.PREFIX_MAP.replace("iow", getUriHostPathAddress() + "iow#");
        LDHelper.prefix = LDHelper.prefix + "PREFIX iow: <" + getUriHostPathAddress() +  "iow#>";
    }
}
