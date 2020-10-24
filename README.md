# Reactive Programing デモのソース 

For [OCHaCafe 3 - #4 挑戦! JavaでReactiveプログラミング](https://connpass.com/event/189340/)


## デモのソース

~~~
.
├── reactive-messaging [各種デモ]
├── messaging-example [MicroProfile Reactive Streams Messagingのシンプルな利用例]
├── file-connector [MicroProfile Reactive Streams Messagingを使ったFile Connector作成例]
└── kafka-connector [Helidon Kafka Connectorの使用例]
~~~

## ビルド方法


各ディレクトリから `mvn package` でビルドできます。Java 11が必要です。  
`java -jar target/xxx.jar` で(Helidonのプロセスが)起動します。


## テストの方法

```bash
## cd reactive-messaging; java -jar target/reactive-messaging.jar

### 非同期呼び出し

# 同期呼び出しパターン
curl localhost:8080/async-test/sync?str=abc,lmn,xyz

# 非同期呼び出しパターン - java.util.stream.Stream のparallel()を使う
curl localhost:8080/async-test/parallel?str=abc,lmn,xyz

# 非同期呼び出しパターン - JDKのスレッドプールを使って非同期処理
curl "localhost:8080/async-test/async1?str=abc,lmn,xyz&nthreads=3"

# 非同期呼び出しパターン - helidonのスレッドプールを使って非同期処理
curl localhost:8080/async-test/async2?str=abc,lmn,xyz

# 非同期呼び出しパターン - MicroProfile Fault Toleranceの非同期処理を利用
curl localhost:8080/async-test/async-ft?str=abc,lmn,xyz


### 非同期RESTサービス

# JAX-RS 同期RESTクライアント
curl localhost:8080/async-client/sync?str=abc,lmn,xyz

# JAX-RS 非同期RESTクライアント
curl localhost:8080/async-client/async?str=abc,lmn,xyz

# JAX-RS JAX-RS Reactive RESTクライアント
curl localhost:8080/async-client/rx?str=abc,lmn,xyz

# MicroProfile 非同期RESTクライアント
curl localhost:8080/async-client/mp?str=abc,lmn,xyz


### Reactive フレームワークを使った非同期処理

# Reactive Streams インターフェースをスクラッチで実装
curl localhost:8080/reactive/rs?str=abc,lmn,xyz

# Reactive Streams インターフェースをスクラッチで実装 - 並列処理
curl localhost:8080/reactive/rs-multi?str=abc,lmn,xyz

# java.concurrent.Flowを使った実装
curl localhost:8080/reactive/flow?str=abc,lmn,xyz

# java.concurrent.Flowを使い、並列処理
curl localhost:8080/reactive/flow-multi?str=abc,lmn,xyz

# MicroProfile Reactive Streams Operators で実装
curl localhost:8080/reactive/operators?str=abc,lmn,xyz

# Helidon 実装の ReactiveOperators を使用
curl localhost:8080/reactive/helidon?str=abc,lmn,xyz

# RxJava を使う - flatMapを使って並列処理にする
curl localhost:8080/reactive/rxjava?str=abc,lmn,xyz

# MicroProfile Reactive Streams Messagning で実装
curl localhost:8080/reactive-messaging/basic?str=abc,lmn,xyz

##
# MicroProfile Reactive Streams Messagning - Processor でチャネルを連結
curl localhost:8080/reactive-messaging/process/key1?value=val1


### Kafka Connector
## cd kafka-connector; java -jar target/kafka-connector.jar

# Kafka Connector でメッセージを送信＆受信
curl localhost:8080/kafka/publish?message=Hello+Ochacafe%21


### File Connector
## cd file-connector; java -jar target/file-connector.jar

mkdir -p /tmp/file-connector/in
mkdir -p /tmp/file-connector/out
mkdir -p /tmp/file-connector/archive

echo "Hello Ochacafe" > /tmp/file-connector/in/test.dat
cat /tmp/file-connector/out/*
```


## その他

Server-Sent Event と MicroProfile Reactive Streams Messaging を組み合わせたデモは、[こちら](https://github.com/oracle/helidon/tree/master/examples/microprofile/messaging-sse) から入手できます。


---
_Copyright © 2020, Oracle and/or its affiliates. All rights reserved._


