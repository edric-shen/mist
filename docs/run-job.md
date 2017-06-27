## Run Mist job

### Configuration

Endpoint is an abstraction over jobs.
Every endpoints is combination of `artifactFile`, `className` of class that implements MistJob and default `namespace`.
They are configured in `./configs/router.conf` or can be passed to mist-master via `--router-conf` argument

Example configuration:

```hocon
my-awesome-job { // object name is name of endpoint
    path = "my-awesome-job.jar" // path to artifact file (jar or py)
    className = "com.company.MyAwesomeJob$" // full class name ob job object
    namespace = "production-namespace"
}
```

### Run

There is several ways to run job on endpoint:

- Http api:
   - Run job - obtain id assciated with that run request:
    ```sh
    curl --header "Content-Type: application/json" \
         -X POST http://localhost:2004/v2/api/endpoints/my-awesome-job?force=true \
         --data '{"arg": "value"}'
    ```
- Send message over Kafka or Mqtt with format:
   ```javascript
    {
      "endpointId": "my-awesome-job",
      "parameters": {
        "arg1": "value"
      }
    }
   ```