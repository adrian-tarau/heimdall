# Docker

Build Heildall container (if needed) with the following command (executed in `deploy` module):

`mvn jib:dockerBuild`

The `compose.yaml` should be changed if needed (passwords, volumes, etc). By default, it deploys the latest version:

```bash
docker compose -p heimdall up
```