FROM thyrlian/android-sdk:9.1 as builder

WORKDIR /app
COPY gradlew gradlew
COPY gradle gradle
RUN ./gradlew --version
COPY . .

ENTRYPOINT ["./gradlew"]
RUN ./gradlew build -Dskip.tests=true
