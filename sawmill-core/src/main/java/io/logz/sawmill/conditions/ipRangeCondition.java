package io.logz.sawmill.conditions;

import io.logz.sawmill.Condition;
import io.logz.sawmill.Doc;
import io.logz.sawmill.annotations.ConditionProvider;
import io.logz.sawmill.exceptions.ProcessorConfigurationException;
import io.logz.sawmill.parser.ConditionParser;
import io.logz.sawmill.utilities.JsonUtils;

import com.github.seancfoley;


@ConditionProvider(type = "ipRange", factory = ipRangeCondition.Factory.class)
public class IPRangeCondition implements Condition {
    private final String field;
    private final IPAddress rangeStartIP;
    private final IPAddress rangeEndIP;

    public IPRangeCondition(String field, String startIP, String endIP) {
        this.field = field;
        this.rangeStartIP = new IPAddressString(startIP).getAddress();
        this.rangeEndIP = new IPAddressString(endIP).getAddress();

        if (this.rangeStartIP == null || this.rangeEndIP == null) {
            throw new ProcessorConfigurationException("failed to parse ipRange condition, invlid IP provided in rangeStartIP or rangeEndIP");
        }
    }

    @Override
    public boolean evaluate(Doc doc) {
        if (!doc.hasField(field)) return false;
        
        try {
            String value = doc.getField(field);
            return isInRange(value);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isInRange(String ipValue) {
        IPAddress ip = new IPAddressString(ipValue).getAddress();
        if (ip == null) return false;
        IPAddressSeqRange ipRange = rangeStartIP.toSequentialRange(rangeEndIP);

        return ipRange.contains(inputIPAddress);
    }

    public static class Factory implements Condition.Factory {

        @Override
        public Condition create(Map<String, Object> config, ConditionParser conditionParser) {
            IPRangeCondition.Configuration ipRangeConfig = JsonUtils.fromJsonMap(IPRangeCondition.Configuration.class, config);
            return new IPRangeCondition(configuration.getField(), configuration.getStartIP(), configuration.getEndIP());
        }
    }

    public static class Configuration {
        private String field;
        private IPAddress rangeStartIP;
        private IPAddress rangeEndIP;

        public Configuration(String field, String startIP, String endIP) {
            this.field = field;
            this.rangeStartIP = new IPAddressString(startIP).getAddress();
            this.rangeEndIP = new IPAddressString(endIP).getAddress();
        }

        public String getField() {
            return field;
        }

        public String getStartIP() {
            return rangeStartIP.toCompressedString();
        }

        public String getEndIP() {
            return rangeEndIP.toCompressedString();
        }
    }
}