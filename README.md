# Counters

_A highly available distributed database...of counters_

##### REST API Endpoints

| Verb | Path | Parameters | Description |
| ---- | ---- | ---------- | ----------- |
| GET  | `/ping` | _None_ | A health check endpoint to verify that the service is up for load balancing purposes |
| POST | `/config`| `{ "actors": List[String] }` | Bootstraps remote clients for the cluster. Takes a list of addresses of the remote clients. |
| GET  | `/counter/:name:/value` | _None_ | Retrieve a value for a given counter |
| GET  | `/counter/:name:/consistent_value` | _None_ | Retrieve a consistent value from the cluster for a given counter<sup><a href="#fn1">1</a></sup> |
| POST | `/counter/:name:` | `{ "value": Integer }` | Set a value for a counter |

#### Dependencies
- [Spray](http://spray.io)
- [Akka](http://akka.io)
- [pablosmedina/ckite](https://github.com/pablosmedina/ckite)

### Build / Execution
To build the database run:
```
./build.sh
```
The fully assembled result will be available as `target/challenge-${pacakge.version}-assembly.tar.gz` and as a docker image called `mattgates5/challenge:1.0`

To run the cluster, you will need to set the environment variable `BOOTSTRAP` to `true` for the first node as per the design notes below, in order to get the cluster running<sup><a href="#fn2">2</a></sup>. 

```bash
BOOTSTRAP=true challenge/bin/challenge-executable
```

All other nodes can be run just with:
```bash
challenge/bin/challenge-executable
```
### Docker
As I mentioned above, the first node will be bootstrapped. You will need to pass `-e BOOTSTRAP='true'` to the docker run command to enable the first node.

## Design
In accordance with the [CAP theorem](https://en.wikipedia.org/wiki/CAP_theorem), Counters is AP, meaning it is available and tolerant to network partitioning.

<sup>[1](#fn1)</sup>In order to counter the lack of consistency, the endpoint `/counter/:name:/consistent_value` synchronously blocks while it performs a consensus on a given counter. 

### Scalability and Availability
I chose to use [Pablo Medina's ckite package](https://github.com/pablosmedina/ckite), which is a native Scala implementation of the [Raft Algorithm](https://raft.github.io/) for cluster consensus. I chose this over Paxos due to its simplicity and the fact that it rapidly sped-up development time. Once bootstrapped, the cluster will perform regular leader elections and forwards the [counter store's](https://github.com/pablosmedina/kvstore) replay log between actors.

In case of network outage, so long as the service is behind a load balancer it will continue to serve both local and cluster-consistent values for counters. The loss of one or more nodes will force a leader election.

### Compromises
As in all deadline-bound software projects, I had to make a few compromises.
- The config endpoint forces the thread to sleep for 1s to allow time enough for each actor in the cluster to join and begin the leader election process. Without the delay the cluster tended to destabilize.
- Each time a node is started, its address will have to be sent to the `/config` endpoint of one of the existing nodes.
- I felt compelled to add the health check endpoint so as to provide a way for load balancers to validate membership in the pool, despite not being a design requirement.
- Per the design requirements, the `/counter/:name:` endpoint was to take an ASCII character but for the purposes of strongly enforcing the type I require a JSON object that contains the value, e.g. `{"value": 12}`.
- <sup>[2](#fn2)</sup>Most importantly, **the first node must be bootstrapped ahead of the rest of the cluster**. This is to give the cluster an initial leader to which the rest of the nodes may refer when they initially join the cluster. This could obviously complicate the deployment process, but for the purposes of illustrating a highly available system, it was compromise I was willing to make.
