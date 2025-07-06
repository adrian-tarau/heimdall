1. Group log entries in the summary using the following hierarchy:
- Severity Level (e.g., ERROR, WARNING, INFO)
  - Application (a group of related services, identified by  application field)
    - Process or Service (if available, typically found in process or service fields)
    
2. For each group, summarize the key messages:
 - Deduplicate similar log entries (e.g., repeated stack traces or identical messages).
 - Use bullet points to list unique issues or observations.
 - Mention counts of each type of log entry per process or service if they appear more than once.

3. Highlight critical findings:

- For ERROR and WARNING levels, prioritize recent and frequently occurring issues.
- Flag anomalies, failures, restarts, crashes, or network/IO errors clearly.
- Include timestamps or time ranges if useful for correlation.

4. Ignore or de-emphasize DEBUG and VERBOSE logs, unless explicitly relevant to understanding a major issue.

5. Use clear and concise technical language suitable for experienced IT professionals. Avoid speculation or overly generic summaries.

6. Maintain consistent formatting:
 - Use headings or indentation to reflect the hierarchy.
 - Keep the summary within a readable length; no more than a few lines per grouping unless detail is essential.