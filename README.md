# Distributed Matrix multiplication

Commands for preparing the enviornment (Assuming you are in the main folder e.g. the one with the pom.xml file in it)
1. sudo apt update
2. sudp apt install default-jdk maven
3. (From grpc-server folder) mvn package -D"maven.test.skip"="true"
4. (From grpc-server folder) chmod 777 mvnw
5. (From grpc-server folder) ./mvnw spring-boot:run -D"maven.test.skip"="true"
6. (From grpc-client folder e.g. seperate ssh connection) mvn package -D"maven.test.skip"="true"
7. (From grpc-client folder e.g. seperate ssh connection) chmod 777 mvnw
8. (From grpc-client folder e.g. seperate ssh connection) ./mvnw spring-boot:run -D"maven.test.skip"="true"



The goal of this project is to create a system which will allow the distributed processing of matrix calculations. The project consists of gRPC server and a gRPC client. The gRPC server provides access to the addBlock and multBlock functions. These functions accepts any square matrix whose dimensions are powers of 2. 

Frequently large scale workloads use the notion of a deadline to determine how many servers should be assigned to a workload. This system also has a deadline based scaling function. A footprinting is implemented to determine the time required for multiplying one block and it is used to determine the minimal number of servers required to achieve the deadline. 