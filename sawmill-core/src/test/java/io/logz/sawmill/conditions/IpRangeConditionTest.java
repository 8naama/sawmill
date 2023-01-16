package io.logz.sawmill.conditions;

import io.logz.sawmill.exceptions.ProcessorConfigurationException;
import io.logz.sawmill.Doc;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static io.logz.sawmill.utils.DocUtils.createDoc;
import static org.assertj.core.api.Assertions.assertThat;

public class IPRangeConditionTest {
  
  @Test
  public void invalidConfig() {
    IPRangeCondition.Factory factory = new IPRangeCondition.Factory();
    Map<String,Object> config = new HashMap<>();
    config.put("field", "fieldName");
        config.put("rangeStartIP", "5.53.255.255");
    config.put("rangeEndIP", "ffff:200.100.2.2");

    // check invalid endIp
    assertThatThrownBy(() ->factory.create(config, null)).isInstanceOf(ProcessorConfigurationException.class)
                .hasMessageContaining("failed to parse ipRange condition, invlid IP provided in rangeEndIP");

    // check invalid startIP
    config.put("rangeStartIP", "");
    assertThatThrownBy(() ->factory.create(config, null)).isInstanceOf(ProcessorConfigurationException.class)
                .hasMessageContaining("failed to parse ipRange condition, invlid IP provided in rangeStartIP");
    
    // check missing field
    assertThatThrownBy(() -> new IPRangeCondition(null, "ffff:200.100.2.2", "5.53.255.255")).isInstanceOf(ProcessorConfigurationException.class)
                .hasMessageContaining("failed to parse IPRange condition, missing field");
    
    // check missing ip value
    assertThatThrownBy(() -> new IPRangeCondition("path", null, "5.53.255.255")).isInstanceOf(ProcessorConfigurationException.class)
                .hasMessageContaining("failed to parse ipRange condition, invlid IP provided in rangeStartIP");
  }

  @Test
  public void testNotIPValue() {
    FieldTypeCondition isInRange = new FieldTypeCondition("testField", "192.158.0.0", "192.158.255.255");

    assertThat(FieldTypeCondition.evaluate(DocUtils.createDoc("otherField", "otherValue"))).isFalse();
    assertThat(FieldTypeCondition.evaluate(DocUtils.createDoc("testField", "not an IP"))).isFalse();
    assertThat(FieldTypeCondition.evaluate(DocUtils.createDoc("testField", 12345))).isFalse();
    assertThat(FieldTypeCondition.evaluate(DocUtils.createDoc("testField", null))).isFalse();
    assertThat(FieldTypeCondition.evaluate(DocUtils.createDoc("testField", -4.5))).isFalse();
    assertThat(FieldTypeCondition.evaluate(DocUtils.createDoc("testField", 4.5))).isFalse();
    assertThat(FieldTypeCondition.evaluate(DocUtils.createDoc("testField", ""))).isFalse();
    assertThat(FieldTypeCondition.evaluate(DocUtils.createDoc("testField", "12345"))).isFalse();
    assertThat(FieldTypeCondition.evaluate(DocUtils.createDoc("testField", -10))).isFalse();
    assertThat(FieldTypeCondition.evaluate(DocUtils.createDoc("testField", new ArrayList<>()))).isFalse();
    assertThat(FieldTypeCondition.evaluate(DocUtils.createDoc("testField", new HashMap<>()))).isFalse();
    
  }

  @Test
  public void testvalidIPValue() {
    FieldTypeCondition isInRange = new FieldTypeCondition("testField", "192.158.0.0", "192.158.255.255");

    assertThat(FieldTypeCondition.evaluate(DocUtils.createDoc("testField", "192.159.38.4"))).isFalse();
    assertThat(FieldTypeCondition.evaluate(DocUtils.createDoc("testField", "255.255.255.255"))).isFalse();
    assertThat(FieldTypeCondition.evaluate(DocUtils.createDoc("testField", "0.0.0.0"))).isFalse();

    assertThat(isTypeCondition.evaluate(DocUtils.createDoc("testField", "192.158.38.4"))).isTrue();
    assertThat(isTypeCondition.evaluate(DocUtils.createDoc("testField", "192.158.0.0"))).isTrue();
    assertThat(isTypeCondition.evaluate(DocUtils.createDoc("testField", "192.158.255.255"))).isTrue();
  }
}