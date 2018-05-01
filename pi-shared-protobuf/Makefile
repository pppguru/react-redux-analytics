build_delete: 
	make gen_all && rm -rf java_generated && echo "compile success"

gen_all: clean
	protoc -Isrc/ \
		-Isrc/google/api/ \
		-Isrc/scalapb/ \
		--java_out=java_generated \
		src/*.proto

update_vendors:
	rm -rf src/google/protobuf/
	mkdir -p src/google/protobuf/

	# grpc-gateway depends on descriptor.proto
	wget -O src/google/protobuf/descriptor.proto https://raw.githubusercontent.com/google/protobuf/master/src/google/protobuf/descriptor.proto

	# wrappers.proto can be used to provide optional values
	wget -O src/google/protobuf/wrappers.proto https://raw.githubusercontent.com/google/protobuf/master/src/google/protobuf/wrappers.proto

	# grpc-gateway depends on annotations.proto and http.proto
	wget -O src/google/api/annotations.proto https://raw.githubusercontent.com/googleapis/googleapis/master/google/api/annotations.proto
	wget -O src/google/api/http.proto https://raw.githubusercontent.com/googleapis/googleapis/master/google/api/http.proto

	# Support for Option[T] for our Scala projects
	rm -rf src/scalapb/
	mkdir -p src/scalapb/
	wget -O src/scalapb/scalapb.proto https://raw.githubusercontent.com/trueaccord/ScalaPB/master/protobuf/scalapb/scalapb.proto

clean:
	mkdir -p java_generated && rm -rf java_generated/*
