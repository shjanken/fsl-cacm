# 使用 multi-stage 来创建镜像

## 这个镜像使用 boot-clj 来创建 uberjar 文件
## uberjar 文件的路径是 
##   /usr/local/app/target/fsl-cacm-0.1.0-SNAPSHOT-standalone
FROM clojure:openjdk-8-boot as builder

RUN mkdir /usr/local/app

COPY . /usr/local/app/

WORKDIR /usr/local/app/

RUN boot build --dir target

## 使用 openjdk 镜像来构建运行环境
FROM openjdk:8

COPY --from=builder /usr/local/app/target/fsl-cacm-0.1.0-SNAPSHOT-standalone /usr/local/fsl-cacm.jar

CMD ["java" "-jar" "/usr/local/fsl-cacm.jar"]