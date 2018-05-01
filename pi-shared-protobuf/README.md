# Protocol Buffer Definitions

We use [Protocol Buffers version 3](https://developers.google.com/protocol-buffers/docs/proto3) to define the RPC API for our microservices. The services are then implemented and consumed using [gRPC](http://www.grpc.io). Wherever we need to expose parts of the API as REST services, we do so via [grpc-gateway](https://github.com/gengo/grpc-gateway).

## Path of Protobuf src/ Directory

Other projects expect to be able to find the protobuf definitions in the `src/` directory. Build scripts will fail if you move the protobuf definitions to another path.

## Vendors

We depend on some third party protobuf definitions. Vendor protobufs are checked into the repository. To fetch the vendor protocol buffers from their upstream sources, run:

```
make update_vendors
```

## Building for Java

To generate Java code from the protos, run:

```
make
```

The Java code can then be found in the `java_generated/` directory.

## Example Protobuf

See inline comments for rationale behind message format and naming conventions.

```
syntax = "proto3";  // We don't support proto2
option java_package = "io.practiceinsight.licensingalert.alertmanager";
option java_multiple_files = true;
import "google/api/annotations.proto";  // Required for grpc-gateway

package alertmanager;

service AlertManagerService {
  // Not `CreateAlert` because this is already in the context of the `AlertManagerService` type.
  // We don't need to repeat ourselves.
  rpc Create(CreateAlertRequest) returns (AlertCreated) {}

  rpc Get(GetAlertRequest) returns (Alert) {
    // Annotation for grpc-gateway
    option (google.api.http) = {
      get: "/v1/alerts/{alert_id}"
    };
  }

  rpc Find(FindAlertRequest) returns (FindAlertResult) {
    option (google.api.http) = {
      get: "/v1/alerts"
    };
  }

  rpc Move(MoveAlertRequest) returns (AlertMoved) {
    option (google.api.http) = {
      post: "/v1/alerts/{alert_id}/move"
      body: "*"
    };
  }
}

// We are more explicit than simply calling this `CreateRequest` because it type not a
// subtype of `AlertManagerService`.
message CreateAlertRequest {
  string user_id = 1;
  string citation_id = 2;
}

// Notice the past tense. This is an event and the name should make sense if we want to
// log this message, persist it, etc.
message AlertCreated {
  string alert_id = 1;
}

message GetAlertRequest {
  string alert_id = 1;
}

message Alert {
  string alert_id = 1;
}

message FindAlertRequest {
  Folder folder = 1;
  uint64 offset = 2;
  uint32 count = 3;
}

// Not called `FindAlertResponse` because we mean that the request was successful. If
// there's an error, we instead send a gRPC error code as a response to the client.
// List of gRPC error status codes: http://www.grpc.io/docs/guides/error.html
message FindAlertResult {
  repeated Alert alerts = 1;
}

enum Folder {
  INBOX = 0;
  SHORTLIST = 1;
  ARCHIVE = 2;
}

message MoveAlertRequest {
  string alert_id = 1;
  Folder folder = 2;
}

// Empty message for forward compatibility. We don't want to break clients by changing
// the RPC function signatures should we decide to send data with this message in the future.
message AlertMoved {}
```

The resulting service implementation looks like this in Scala:

```
object AlertManagerService extends AlertManagerServiceGrpc.AlertManagerService {

  override def create(request: CreateAlertRequest): Future[AlertCreated] = {
    // TODO
    Future.failed(Status.UNIMPLEMENTED.asException())
  }

  override def get(request: GetAlertRequest): Future[Alert] = {
    // TODO
    Future.failed(Status.UNIMPLEMENTED.asException())
  }

  override def find(request: FindAlertRequest): Future[FindAlertResult] = {
    // TODO
    Future.failed(Status.UNIMPLEMENTED.asException())
  }

  override def move(request: MoveAlertRequest): Future[AlertMoved] = {
    // TODO
    Future.failed(Status.UNIMPLEMENTED.asException())
  }
}
```
