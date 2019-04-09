package jp.gr.java_conf.uzresk.samples.spark.emr.utils;

import com.amazonaws.ClientConfiguration;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AmazonClientConfigurationBuilder {

    public static Optional<ClientConfiguration> clientConfiguration() {
        String proxy = System.getenv("https_proxy");
        ClientConfiguration cc = null;
        if (proxy != null) {
            String regex = "https?://(.*)(:)(.*)";
            Matcher m = Pattern.compile(regex).matcher(proxy);
            if (m.find()) {
                cc = new ClientConfiguration()
                        .withProxyHost(m.group(1))
                        .withProxyPort(Integer.parseInt(m.group(3)));
            }
        }
        return Optional.ofNullable(cc);
    } 
}
