# Step 1: Build Stage
FROM eclipse-temurin:21-jdk AS build

# 작업 디렉토리 설정
WORKDIR /app

# Gradle wrapper 및 프로젝트 복사
COPY gradlew /app/gradlew
COPY gradle /app/gradle
COPY build.gradle /app/
COPY settings.gradle /app/

# Gradle Wrapper 실행 권한 부여
RUN chmod +x /app/gradlew

# 의존성 캐싱 (빌드 속도 개선)
RUN ./gradlew --no-daemon dependencies

# 소스 코드 복사 및 Gradle 빌드
COPY . /app
RUN ./gradlew clean build -x test --no-daemon

# Step 2: Runtime Stage
FROM eclipse-temurin:21-jdk AS runtime

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 컨테이너에서 사용할 포트 노출
EXPOSE 8080

# 환경 변수 설정 (필요 시 추가 가능)
ENV JAVA_OPTS=""

# Java 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
