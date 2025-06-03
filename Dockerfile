# Ubuntu 기반으로 변경 (폰트 지원이 더 좋음)
FROM bellsoft/liberica-openjdk-debian:21

# 한글 폰트 설치
RUN apt-get update && apt-get install -y \
    fonts-nanum \
    fonts-nanum-coding \
    fonts-nanum-extra \
    fontconfig \
    && rm -rf /var/lib/apt/lists/*

# 폰트 캐시 갱신
RUN fc-cache -fv

VOLUME /tmp

ARG JAR_FILE=build/libs/*.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]