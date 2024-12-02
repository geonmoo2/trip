# Java 21 기반 Eclipse Temurin 이미지로 빌드
FROM eclipse-temurin:21-jdk AS build

# 작업 디렉토리 설정
WORKDIR /app

# 프로젝트 복사 및 Gradle 빌드
COPY . /app
RUN chmod +x /app/gradlew
RUN ./gradlew clean build -x test --no-daemon

# 빌드 결과를 런타임 환경에 복사
FROM eclipse-temurin:21-jdk AS runtime
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Java 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
