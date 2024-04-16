# Java-Based-Blockchain

This project is a full Java-based blockchain implementation that demonstrates various basic blockchain concepts including distributed ledger, immutable records, consensus mechanisms, and more. It consists of two main projects:

[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.java.com/)
[![Spring](https://img.shields.io/badge/Spring-3.2.4-brightgreen)](https://spring.io/)
[![MongoDB](https://img.shields.io/badge/MongoDB-Database-darkgreen)](https://www.mongodb.com/)
[![Hazelcast](https://img.shields.io/badge/Hazelcast-5.3.6-blue)](https://hazelcast.com/)


## 1. Server-App

The Blockchain-Server project is responsible for managing communication with clients, maintaining the blockchain, and handling consensus mechanisms.

### Features:
- Listens to incoming messages from clients and broadcasts them using web sockets.
- Stores all client data and blockchain records in MongoDB.
- Stores all blockchain records in Hazelcast for caching and detecting the DB changes or hackes.
- Utilizes a combination of Proof of Stake (POS) and Proof of Work (POW) to select the block maker and resolve conflicting transactions.
- Implements a distributed ledger ensuring the immutability of records.
  
## 2. Client-App

The Blockchain-Client project serves as the interface for users to interact with the blockchain network.

### Features:
- Allows users to create accounts, which are recorded as transactions on the blockchain.
- Supports bank transactions such as deposit, withdrawal, and transfer.
- Provides functionality to retrieve the full blockchain and ledger from the server.
- Enables users to print their account information.

## Technologies Used:
- Java 17
- Spring Framework 3.2.4
- MongoDB
- Hazelcast 5.3.6
  
## Getting Started:

### Prerequisites:
  (*) Means its must..
  
-  Java Development Kit (JDK) 17 or higher *
-  MongoDB installed and running locally or accessible remotely *
-  Hazelcast 5.3.6 *
- Hazelcast Management Center 5.3.3 (Optional) for tracking your memory usage
  
### Installation:

1. Clone the repository:
  `git clone https://github.com/AbdullahAbdelglil/Java-Based-Blockchain`

2. Navigate to the cloned directory:
  `cd Java-Based-Blockchain`

3. Compile and run the Blockchain-Server project:
  `cd Blockchain-Server` , 
  `mvn spring-boot:run`

4. Compile and run the Blockchain-Client project:
  `cd Blockchain-Client` , 
  `mvn spring-boot:run`

5. Access the client application via a web browser or REST client.

## License:
This project is licensed under the [MIT License](LICENSE).

## Contributing
We welcome contributions to enhance the functionality and features of this project. If you're interested in contributing, please fork the repository, make your changes, and submit a pull request. Together, we can make this project even better! 
