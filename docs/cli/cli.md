# Kafka CLI

카프카를 운영할 때 가장 많이 접하는 도구이며, 카프카 브로커 운영에 필요한 다양한 명령을 내릴 수 있다.
필수 옵션과 선택 옵션으로 나눌 수 있다. 선택 옵션의 경우 지정하지 않을 시, 기본 설정값 또는 CLI 의 기본값으로 설정된다.

## 로컬에서 카프카 브로커 실행

1. 카프카 바이너리 파일 다운로드
2. 바이너리 압축 해제
3. 주키퍼 실행
   - 상용 환경에서는 앙상블로 따로 설치해서 진행되는게 일반적
   - 주키퍼에서는 일반적으로 3개 이상의 서버에서 앙상블로 묶어서 운영하는게 일반적
   - `bin/zookeeper-server-start.sh config/zookeeper.properties`
4. 카프카 바이너리 실행
   - `bin/kafka-server-start.sh config/server.properties`
   - 카프카 서버라는 것은 브로커다
- 카프카 정상 실행 여부확인
  - `bin/kafka-broker-api-versions.sh --bootstrap-server localhost:9092`
  - `bin/kafka-topics.sh --bootstrap-server localhost:9092 --list`
    - 로컬호스트에 실행된 카프카 브로커에서 카프카 토픽의 리스트를 가져옴

### server.properties

broker.id=0  
num.network.threads=3  
num.io.threads=8  
listeners=PLAINTEXT://localhost:9092 # 카프카 브로커가 통신을 통해서 우리가 받을 ip  
socket.send.buffer.bytes=102400  
socket.receive.buffer.bytes=102400  
socket.request.max.bytes=104857600  
log.dirs=/C:\kafka_2.12-2.8.2\data  # 브로커에 데이터가 파일 시스템에 지정되 부분  
num.partitions=1  # 파티션을 만들 때 기본적으로 만들 갯수  
num.recovery.threads.per.data.dir=1  
offsets.topic.replication.factor=1  
transaction.state.log.replication.factor=1  
transaction.state.log.min.isr=1  
log.retention.hours=168  # 세그먼트 삭제 시간   
log.segment.bytes=1073741824  # 세그먼트 삭제 바이트  
log.retention.check.interval.ms=300000  # 세그먼트 체크 시간  
zookeeper.connect=localhost:2181  # 주키퍼 관련 설정  
zookeeper.connection.timeout.ms=18000  
group.initial.rebalance.delay.ms=0  

## kafka-topics.sh

클러스터와 정보와 토픽 이름만으로 토픽을 생성할 수 있다.

- 필수 값
  - 클러스터 정보
  - 토픽 이름
- 선택 값
  - 파티션 갯수
  - 복제 갯수 등
- 필수값만을 이용한 생성
  - `bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --topic hello.kafka`
- 정보 확인
  - `bin/kafka-topic.sh --bootstrap-server localhost:9092 --topic hello.decribe --describe`
- 파티션 갯수 늘리기
  - 데이터 처리량을 늘리기 위한 방법(+ 컨슈머 갯수 늘리기) 
  - `bin/kafka-topic.sh --bootstrap-server localhost:9092 --topic hello.kafka --alter --partitions 4`
  - 단 파티션 갯수는 늘릴 수 있지만 줄일 수 없으므로, 토픽을 새로 만드는 방법이 좋다
  - 따라서 파티션 갯수를 줄여야 할 경우가 없는지를 항상 고려하고 늘려야한다

## kafka-configs.sh

토픽의 일부 옵션을 설정하기 위해선 `config.sh`를 통해 가능하다. `--alter`와 `--add-config` 옵션을 통해
`min.insync.replicas`와 같은 옵션을 토픽별로 설정 가능하다

- 브로커에 저장되어 있는 `server.properties`를 각종 기본값을 --broker, --all, --describe 옵션을 사용해 조회 가능
  - `/bin/kafka-config.sh --bootstrap-server localhost:9092 --borker 0 --all --describe`
- min.insync.replicas
  - 프로듀셔로 데이터를 보낼 때, 컨슈머가 데이터를 읽을 때 워터마크 용도로 사용
  - 얼마나 안전하게 데이터를 보내야하는지에 대해서 설정할 떄 사용
- 기존 토픽에 설정 추가
  - `/bin/kafka-config.sh --bootstrap-server localhost:9092 --alter --add-config min.insync.replicas=2 --topic test`

## kafka-console-producer.sh

카프카에 있는 토픽에 데이터를 테스트 용도로 사용하는데 많이 사용된다. 키보드를 통해 문자를 작성하고 엔터를 누르면 메시지 값이 전송된다

- 전송
  - `bin/kafka-conosle-producer.sh --bootstrap-server localhost:9092 --topic hello.kafka`
- 메시지 키도 같이 보내는 방법
  - `key.separator`와 함께 몇가지 추가 옵션이 필요
  - ```shell
    bin/kafka-conosle-producer.sh --bootstrap-server localhost:9092 \
    --topic hello.kafka \
    --property parse.key=true \
    --property key.separator=":"
    ```

### 메시지 키와 메시지 값이 포함된 레코드가 파티션에 전송될 때

<p align="center"><img src="img/1.png" width="80%"></p>

메시지 키가 있는 경우 동일한 메시지 키의 경우 동일한 파티션에 존재하게 된다. 예를 들어 `K1` 메시지 키는 다른 파티션엔 존재하지 않는다.
하지만, 다른 메시지 키의 경우 다른 메시지 키를 가진 데이터가 존재할 수 있다.

- 중요한 부분은 레코드가 파티션으로 전송될 때 `동일한 메시지 키의 데이터가 한 개의 파티션에 동일하게 들어간다는 점`이다.
  - 동일한 메시지 키를 가지는 레코드에 대해서는 순서를 지킬 수 있다는 게 핵심
  - 컨슈머 입장에서는 파티션을 보통 1:1 관계로 가져가게 되는데, 동일한 메시지 키에 대해서는 순서를 지키며 데이터를 처리할 수 있다
- 메시지 키가 없는 경우 null 로 저장되며, 파티션에 라운드 로빈 방식으로 전송한다

## kafka-console-consumer.sh

특정 토픽에 있는 데이터를 consume 해서 데이터를 조회하기 위한 용도로 사용.

- 필수
  - bootstrap-server
  - topic
- 추가
  - from-beginning
    - 토픽에 저장된 가장 처음부터 데이터 출력
  - property
    - 레코드의 메시지 키와 값을 확인하고 싶을 때 사용
  - --max-messages
    - 최대 컨슘 메시지 갯수 설정
    - 데이터가 수없이 들어오기 떄문에 max 없이 설정하면 계속해서 프린트하게 됨
    - 잠깐 테스트 데이터를 확인할 때 사용
  - --partition
    - 특정 파티션만 consume
  - --group
    - 컨슈머 그룹을 기반으로 consumer 가 동작
    - 특정 목적을 가진 컨슈머들을 묶음으로 사용하는 것
    - 그룹을 넣어주면 특정 그룹이 어디까지 읽었는지 `커밋`이 진행된다
      - `\kafka-topics.bat --bootstrap-server localhost:9092 --list`에 `__consumer_offsets` 토픽이 생긴걸 볼 수 있음
```shell
    bin/kafka-conosle-consumer.sh --bootstrap-server localhost:9092 \
    --topic hello.kafka \
    --property print.key=true \
    --property key.separator="-"
    --from-beginning
```

## kafka-consumer-groups.sh

```shell
    bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
    --list
```

- describe
  - 해당 컨슈머 그룹이` 어떤 토픽을 대상으로 어떤 레코드(오프셋)까지 가져갔는지`
  - 파티션 번호, 중단, 실행 등등을 확인 가능
  - 컨슈머 랙(lag) 또한 확인 가능
    - 마지막 레코드의 오프셋 - 현재 컨슈머 그룹이 가져간 레코드의 오프셋
    - 즉, 지연의 정도를 확인
    - 컨슈머 랙의 모니터링이 중요함
```shell
    bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
    --group hello-group --describe
```

- reset-offsets
  - 특정 오프셋 부터 리셋할지 설정
  - 즉, 어느 오프셋부터 다시 읽을지 정하는 것
  - 옵션
    - --to-earliest : 가장 처음 오프셋(작은 번호)으로 리셋
    - --to-latest: 가장 마지막 오프셋(큰 번호)으로 리셋
    - --to-current: 현 시점 기준 오프셋으로 리셋
    - --to-datetime {}: 특정 일시로 오프셋
    - --to-offset {long}: 특정 오프셋으로 리셋
    - --shift-by {+/- long}: 현재 컨슈머 오프셋에서 앞뒤로 옮겨서 리셋
```shell
    bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
    --group hello-group \
    --topic hello.kafka \
    --reset-offsets --to-earliest -- exeucte
```

## 외에 CLI

### kafka-producer-perf-test.sh

카프카 프로듀서로 퍼포먼스를 측정할 떄 사용한다. 몇 개의 레코드를 어떤사이즈로 전송할건지 등을 정할 수 있다.
운영 중에 네트워크 통신에 이슈가 발생하거나, 데이터가 제대로 전달되지 않는다고 생각될 때 사용할 수 있다.

```shell
    bin/kafka-producer-perf-test.sh --bootstrap-server localhost:9092 \
    --topic hello.kafka \
    --num-records 10 \
    --throughput 1 \
    --record-size 100 \
    --print-metric
```

### kafka-consumer-perf-test.sh

반대로 컨슈머에 대한 퍼포먼스를 측정할 수 있다. 브로커와 컨슈머(해당 퍼포먼스 스크립트를 동작시키는)간의 네트워크 체크가 가능하다

```shell
    bin/kafka-consumer-perf-test.sh --bootstrap-server localhost:9092 \
    --topic hello.kafka \
    --messages 10 \
    --show-detailed-stats
```

### kafka-reassign-partitions.sh

파티션이 핫스팟으로 몰리는 현상에서 재분배하는 스크립트. 리더 파티션과 팔로워 파티션을 분산할 때 사용하면 된다.

- 카프카 브로커에는 `auto.leader.rebalance.enable` 옵션의 기본 값은 true 로써, 클러스터 단위에서
리더 파티션을 자동 리밸런싱하도록 도와준다
- 따라서, 백그라운드 스레드가 일정 간격으로 리더의 위치를 리밸런싱해 준다

### kafka-delete-record.sh

이름만 보면 데이터를 지우는 것 처럼 보이지만 실제로는 워터마크를 표시하는 기능이다.
`low_watermark`를 표시하게되면 해당 오프셋 이전까지의 데이터를 모두 삭제하는 것이다.