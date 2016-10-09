# Counters

_A highly available distributed database...of counters_

### Usage
To start:
```bash
challenge/bin/challenge-executable
```
This will start an HTTP service at 0.0.0.0:7777

### REST API Endpoints

| Verb | Path | Parameters | Description |
| ---- | ---- | ---------- | ----------- |
| GET  | `/ping` | _None_ | A healthcheck to verify that the service is up |
| POST | `/config`| `{ "actors": List[String] }` | Bootstraps remote clients for the cluster. Takes a list of addresses of the remote clients. |
| GET  | `/counter/:name:/value` | _None_ | Retrieve a value for a given counter |
| GET  | `/counter/:name:/consistent_value` | _None_ | Retrieve a consistent value from the cluster for a given counter |
| POST | `/counter/:name:` | `{ "value": Integer }` | Set a value for a counter |

## Design
In accordance with the [CAP theorem](https://en.wikipedia.org/wiki/CAP_theorem), Counters is AP, meaning it is available and tolerant to network partitioning.

In order to counter the lack of consistency, the endpoint `/counter/:name:/consistent_value` synchronously blocks while it performs a consensus on a given counter. 