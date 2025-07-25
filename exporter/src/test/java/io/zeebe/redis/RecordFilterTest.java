package io.zeebe.redis;

import static org.junit.jupiter.api.Assertions.*;

import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.value.TenantOwned;
import io.zeebe.redis.exporter.ExporterConfiguration;
import io.zeebe.redis.exporter.RecordFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RecordFilterTest {

  private RecordFilter recordFilter;
  private TestExporterConfiguration mockConfig;

  @BeforeEach
  public void setUp() {
    mockConfig = new TestExporterConfiguration();
    mockConfig.setEnabledRecordTypes("EVENT,COMMAND");
    mockConfig.setEnabledValueTypes("USER_TASK,JOB");
    mockConfig.setEnabledTenants("tenantA,tenantB");
    mockConfig.setEnableTenantStreams(true);

    recordFilter = new RecordFilter(mockConfig);
  }

  @Test
  public void testAcceptType() {
    assertTrue(recordFilter.acceptType(RecordType.EVENT));
    assertTrue(recordFilter.acceptType(RecordType.COMMAND));
    assertFalse(recordFilter.acceptType(RecordType.COMMAND_REJECTION));
  }

  @Test
  public void testAcceptValue() {
    assertTrue(recordFilter.acceptValue(ValueType.USER_TASK));
    assertTrue(recordFilter.acceptValue(ValueType.JOB));
    assertFalse(recordFilter.acceptValue(ValueType.VARIABLE));
  }

  @Test
  public void testAcceptTenant() {
    // Test with allowed tenants
    assertTrue(recordFilter.acceptTenant(createTenantOwned("tenantA")));
    assertTrue(recordFilter.acceptTenant(createTenantOwned("tenantB")));
    assertFalse(recordFilter.acceptTenant(createTenantOwned("unknownTenant")));
  }

  @Test
  public void testAcceptTenantWithCaseSensitivity() {
    // Test case sensitivity - tenant IDs should be case-sensitive
    assertTrue(recordFilter.acceptTenant(createTenantOwned("tenantA")));
    assertFalse(recordFilter.acceptTenant(createTenantOwned("tenanta")));
    assertFalse(recordFilter.acceptTenant(createTenantOwned("TENANTA")));
  }

  @Test
  public void testEmptyConfigurationAcceptsDefault() {
    // Test with empty configuration - should accept all types
    TestExporterConfiguration emptyConfig = new TestExporterConfiguration();
    emptyConfig.setEnabledRecordTypes("");
    emptyConfig.setEnabledValueTypes("");
    emptyConfig.setEnabledTenants("");
    emptyConfig.setEnableTenantStreams(false);

    RecordFilter emptyFilter = new RecordFilter(emptyConfig);

    // Should accept all record types when configuration is empty
    assertTrue(emptyFilter.acceptType(RecordType.EVENT));
    assertTrue(emptyFilter.acceptType(RecordType.COMMAND));
    assertTrue(emptyFilter.acceptType(RecordType.COMMAND_REJECTION));

    // Should accept all value types when configuration is empty
    assertTrue(emptyFilter.acceptValue(ValueType.USER_TASK));
    assertTrue(emptyFilter.acceptValue(ValueType.JOB));
    assertTrue(emptyFilter.acceptValue(ValueType.VARIABLE));

    // Should accept default tenants when configuration is empty
    assertTrue(emptyFilter.acceptTenant(createTenantOwned(TenantOwned.DEFAULT_TENANT_IDENTIFIER)));
  }

  @Test
  public void testConfigurationWithWhitespaceHandling() {
    // Test configuration with extra whitespace
    TestExporterConfiguration whitespaceConfig = new TestExporterConfiguration();
    whitespaceConfig.setEnabledRecordTypes(" EVENT , COMMAND ");
    whitespaceConfig.setEnabledValueTypes(" USER_TASK , JOB ");
    whitespaceConfig.setEnabledTenants(" tenantA , tenantB ");
    whitespaceConfig.setEnableTenantStreams(true);

    RecordFilter whitespaceFilter = new RecordFilter(whitespaceConfig);

    assertTrue(whitespaceFilter.acceptType(RecordType.EVENT));
    assertTrue(whitespaceFilter.acceptValue(ValueType.USER_TASK));
    assertTrue(whitespaceFilter.acceptTenant(createTenantOwned("tenantA")));
  }

  // Helper method to create a TenantOwned implementation
  private TenantOwned createTenantOwned(String tenantId) {
    return new TenantOwned() {
      @Override
      public String getTenantId() {
        return tenantId;
      }
    };
  }

  // Test implementation of ExporterConfiguration
  private static class TestExporterConfiguration extends ExporterConfiguration {
    private String enabledRecordTypes = "";
    private String enabledValueTypes = "";
    private String enabledTenants = "";
    private boolean enableTenantStreams = false;

    @Override
    public String getEnabledRecordTypes() {
      return enabledRecordTypes;
    }

    public void setEnabledRecordTypes(String enabledRecordTypes) {
      this.enabledRecordTypes = enabledRecordTypes;
    }

    @Override
    public String getEnabledValueTypes() {
      return enabledValueTypes;
    }

    public void setEnabledValueTypes(String enabledValueTypes) {
      this.enabledValueTypes = enabledValueTypes;
    }

    @Override
    public String getEnabledTenants() {
      return enabledTenants;
    }

    public void setEnabledTenants(String enabledTenants) {
      this.enabledTenants = enabledTenants;
    }

    @Override
    public boolean isEnableTenantStreams() {
      return enableTenantStreams;
    }

    public void setEnableTenantStreams(boolean enableTenantStreams) {
      this.enableTenantStreams = enableTenantStreams;
    }
  }
}
