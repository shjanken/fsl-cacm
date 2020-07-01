# 这个 docker 只是用来创建 ubarjar 文件
# 使用 java docker 来运行这个应用

FROM clojure:openjdk-8-boot

RUN mkdir /usr/local/app

COPY . /usr/local/app/

WORKDIR /usr/local/app/

RUN boot build --dir target