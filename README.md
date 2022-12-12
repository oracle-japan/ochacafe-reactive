![GitHub release (latest by date including pre-releases)](https://img.shields.io/github/v/release/oracle-japan/ochacafe-reactive)

# Reactive Programing デモのソース 

For [OCHaCafe 3 - #4 挑戦! JavaでReactiveプログラミング](https://connpass.com/event/189340/)

ベースを Helidon 3.x 系に更新しました。 準拠する仕様が Reactive Messaging 2.0 に更新されたので、Helidon 2.x ベースのサンプルのソースとは互換性がありません。
Helidon 2.x ベースのソースは helidon-2.x ブランチを参照下さい。


## デモのソース

~~~
.
├── reactive-messaging [各種デモ]
├── messaging-example [MicroProfile Reactive Messagingのシンプルな利用例]
├── file-connector [MicroProfile Reactive Messagingを使ったFile Connector作成例]
└── kafka-connector [Helidon Kafka Connectorの使用例]
~~~

## ビルド方法


各サブディレクトリから `mvn package` でビルドできます。Java 17が必要です。  
`java -jar target/xxx.jar` で(Helidonのプロセスが)起動します。  
ルートディレクりから `mvn package` で全てのサブディレクトリのビルドもできます。

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

# MicroProfile Reactive Messagning で実装
curl localhost:8080/reactive-messaging/basic?str=abc,lmn,xyz

##
# MicroProfile Reactive Messagning - Processor でチャネルを連結
curl localhost:8080/reactive-messaging/process/key1?value=val1

curl -X POST -H "Content-type: application/json" -d '{"key":"key1","value":"val1"}' localhost:8080/reactive-messaging/process

### Kafka Connector
## cd kafka-connector; java -jar target/kafka-connector.jar

# Kafka Connector でメッセージを送信＆受信
curl localhost:8080/kafka/publish?message=Hello+Ochacafe%21

curl -X POST -H "Content-type: application/json"\
 -d '["Ochacafe #1","Ochacafe #2","Ochacafe #3","Ochacafe #4","Ochacafe #5","Ochacafe #6","Ochacafe #7","Ochacafe #8"]'\
 localhost:8080/kafka/publish

### File Connector
## cd file-connector; java -jar target/file-connector.jar

mkdir -p /tmp/file-connector/in
mkdir -p /tmp/file-connector/out
mkdir -p /tmp/file-connector/archive

echo "Hello Ochacafe" > /tmp/file-connector/in/test.dat
cat /tmp/file-connector/out/*
```

## Reactive Messaging チャネルの health 確認

pom.xml に モジュルールを追加するこによって、MicroProfile Health の Readiness を使ったチャネル正常性確認ができます。

```xml
    <dependency>
        <groupId>io.helidon.microprofile.messaging</groupId>
        <artifactId>helidon-microprofile-messaging-health</artifactId>
    </dependency>
```

```bash
# reactive-messaging プロジェクト
$ curl -s localhost:8080/health/ready | jq
{
  "outcome": "UP",
  "status": "UP",
  "checks": [
    {
      "name": "messaging",
      "state": "UP",
      "status": "UP",
      "data": {
        "channel-1": "UP",
        "channel-2": "UP",
        "messaging-demo": "UP"
      }
    }
  ]
}

# kafka-connector プロジェクト
$ curl -s localhost:8080/health/ready | jq
{
  "outcome": "UP",
  "status": "UP",
  "checks": [
    {
      "name": "messaging",
      "state": "UP",
      "status": "UP",
      "data": {
        "kafka-pub": "UP",
        "kafka-sub": "UP"
      }
    }
  ]
}

# file-connector プロジェクト
$ curl -s localhost:8081/health/ready | jq .checks[].data
{
  "file-in": "UP",
  "file-out": "UP",
  "file-process": "UP"
}
```

## その他

Server-Sent Event と MicroProfile Reactive Messaging を組み合わせたデモは、[こちら](https://github.com/oracle/helidon/tree/master/examples/microprofile/messaging-sse) から入手できます。



