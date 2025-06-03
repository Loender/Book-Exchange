# 📖 Book Exchange

A Spring Boot + JavaFX application designed as a social media platform for book lovers.

## 🎯 Project Overview

This project serves as a study into building full-stack Java applications. It integrates a Spring Boot backend with a JavaFX-based desktop GUI.

Main objectives:
- Explore Spring Boot framework.
- Integrate with a standalone MySQL database.
- Apply secure development practices using Spring Security for authentication and access control.


## 🛠 Installation

### ✅ Prerequisites

**Java Development Kit (JDK)**

- Version: 17 or higher

 **Apache Maven**

- Version: 3.8.0+

- Download: https://maven.apache.org/download.cgi

**Any MySQL Database Server**

- Version: 8.x or higher

- Ensure MySQL is running and a database is created (with connection details configured in application.properties)

 **JavaFX SDK (only required if running outside of Maven)**

- Version: 23.0.2

- Automatically managed by Maven via javafx-maven-plugin

- Download (optional): https://openjfx.io

## 🚀 Setup

1. **Clone Repository**

Go into an IDE of your choice and paste the following into your terminal (into your desired path):

```bash
git clone https://github.com/Loender/Book-Exchange.git
cd Book-Exchange
```

2. **Configure the database** 

Create a file named application.properties in src/main/resources/ and add:

```bash
spring.datasource.url=jdbc:mysql://localhost:3306/YOUR_DB_NAME
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```
(Replace **YOUR_DB_NAME**, **YOUR_USERNAME**, and **YOUR_PASSWORD** with your actual MySQL credentials)

3. **Build the project**

Once that is done, in the terminal, you will need to enter
```bash
mvn clean install
```

4. **Run the application**


And finally, you can safely run the project
```bash
mvn javafx:run
```


## ✨ Features

### 🔐 User Authentication & Registration
Secure login and sign-up system with form validation and password hashing.

### 👥 Role-Based Access Control
UI and functionality dynamically adapt based on user roles (e.g., admin vs. regular user).

### 📚 Book Management System

- Add, edit, and view books available

- Associate books with users

- Table view for listing books

### 🗨️ Messaging, rating, and comments system
- Chat with other users
- Leave ratings
- Give feedback via comments, with an ability to reply

### 💻 JavaFX Desktop Interface

- Built with FXML

- Responsive, data-bound components

- Conditional visibility and interaction logic

### 🛡️ Secure Password Handling
All user passwords are securely hashed before being stored in the database.

### 🗄️ MySQL Database Integration
Uses Spring Data JPA with Hibernate for persistence, schema management, and data querying.

### 🚀 Spring Boot Integration
Uses Spring Boot internally to manage application logic, data persistence (JPA), security, and configuration
## License

[Apache-2.0](https://choosealicense.com/licenses/apache-2.0/)
