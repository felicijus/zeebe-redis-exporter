package io.zeebe.redis.exporter;

import io.camunda.zeebe.exporter.api.context.Context;
import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.value.TenantOwned;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class RecordFilter implements Context.RecordFilter {

  private final List<RecordType> enabledRecordTypes;
  private final List<ValueType> enabledValueTypes;
  private final List<String> enabledTenants;

  public RecordFilter(ExporterConfiguration config) {
    final List<String> enabledRecordTypeList = parseAsList(config.getEnabledRecordTypes());
    enabledRecordTypes =
        Arrays.stream(RecordType.values())
            .filter(
                recordType ->
                    enabledRecordTypeList.isEmpty()
                        || enabledRecordTypeList.contains(recordType.name()))
            .collect(Collectors.toList());

    final List<String> enabledValueTypeList = parseAsList(config.getEnabledValueTypes());
    enabledValueTypes =
        Arrays.stream(ValueType.values())
            .filter(
                valueType ->
                    enabledValueTypeList.isEmpty()
                        || enabledValueTypeList.contains(valueType.name()))
            .collect(Collectors.toList());

    final List<String> enabledTenantList = parseAsList(config.getEnabledTenants());
    enabledTenants =
        enabledTenantList.isEmpty()
            ? Collections.singletonList(TenantOwned.DEFAULT_TENANT_IDENTIFIER)
            : enabledTenantList;
  }

  private List<String> parseAsList(String list) {
    return Arrays.stream(list.split(","))
        .map(String::trim)
        .filter(item -> !item.isEmpty())
        .collect(Collectors.toList());
  }

  @Override
  public boolean acceptType(RecordType recordType) {
    return enabledRecordTypes.contains(recordType);
  }

  @Override
  public boolean acceptValue(ValueType valueType) {
    return enabledValueTypes.contains(valueType);
  }

  public boolean acceptTenant(TenantOwned tenantOwnedRecord) {
    final String tenantId = tenantOwnedRecord.getTenantId();
    return enabledTenants.contains(tenantId);
  }
}
