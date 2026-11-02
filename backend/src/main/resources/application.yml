spring:
  application:
    name: agentic-enterprise-log-analyzer

  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/log_analyzer?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true}
    username: ${SPRING_DATASOURCE_USERNAME:analyzer}
    password: ${SPRING_DATASOURCE_PASSWORD:change_me}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true

  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 6MB

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info
