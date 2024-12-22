## Cleanup Storage

### Cleanup Database

```sql
SET FOREIGN_KEY_CHECKS=0;
truncate table protocol_smtp_attachments;
truncate table protocol_smtp_events;
truncate table protocol_gelf_events;
truncate table protocol_snmp_events;
truncate table protocol_syslog_events;
truncate table protocol_parts;

truncate table database_statements;
truncate table database_snapshots;

truncate table broker_sessions;
truncate table broker_events;
SET FOREIGN_KEY_CHECKS=1;
```

### Cleanup File System