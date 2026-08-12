# Measure

[한국어](measure.md) | [English](measure.en.md)

**Module:** `:toolkit:measure`  
**Artifact:** `io.github.beez:measure`  
**Status:** Experimental

## 목적

코드 블록 또는 분리된 시작점과 종료점 사이의 경과 시간을 측정한다. 측정 기능은 호스트 앱의 반환값, 예외, 취소 및 실행 흐름을 변경하지 않는다.

## 설치

```kotlin
dependencies {
    implementation("io.github.beez:measure:<version>")
}
```

개발 빌드에서만 성능 진단이 필요하면 `debugImplementation`을 사용할 수 있다. 릴리스에도 포함하는 경우 `enabled`와 reporter 정책을 명시적으로 설정한다.

## 기본 API

```kotlin
val user = BeezKitMeasure.trace("load-user") {
    repository.loadUser()
}

val user = BeezKitMeasure.traceSuspend("load-user") {
    repository.loadUser()
}
```

- 블록의 반환값을 그대로 반환한다.
- 일반 예외는 `Failure`, coroutine 취소는 `Cancelled`로 기록한다.
- 측정 후 동일한 예외 또는 취소를 그대로 다시 던진다.
- 비활성 상태에서는 입력 검증과 시간 측정 없이 블록만 실행한다.

## 분리된 구간

```kotlin
val span = BeezKitMeasure.start("app-start")
initializeApplication()
span.end()
```

- 모든 활성 span은 프로세스 안에서 고유한 ID를 가진다.
- 동일 TAG의 span을 동시에 사용할 수 있다.
- `end()`는 최초 호출만 결과를 반환하고 이후 호출은 `null`을 반환한다.
- `BeezKitMeasureSpan`은 `AutoCloseable`을 구현하므로 `use`를 지원한다.

TAG 기반 편의 API는 동일한 TAG와 key를 LIFO로 연결한다.

```kotlin
BeezKitMeasure.markStart("load-item", key = itemId)
BeezKitMeasure.markEnd("load-item", key = itemId)
```

- 대응하는 시작점이 없으면 `markEnd()`는 `null`을 반환한다.
- 동시 작업에서는 TAG 방식보다 독립 span을 권장한다.
- 활성 TAG span은 `maxActiveMarks`로 제한하며 한도에 도달한 `markStart()`는 `false`를 반환한다.

## 결과 모델

```kotlin
data class BeezKitMeasureRecord(
    val id: String,
    val tag: String,
    val duration: Duration,
    val status: BeezKitMeasureStatus,
    val attributes: Map<String, String>,
    val error: Throwable?,
)
```

상태는 `Success`, `Failure`, `Cancelled`를 제공한다. attributes는 측정 시작 시 immutable snapshot으로 복사한다. 전역 history에 error가 보관될 수 있으므로 history는 기본적으로 비활성화한다.

## Attributes 제한

- 최대 16개
- TAG 최대 128자이며 공백일 수 없음
- key 최대 64자이며 공백일 수 없음
- value 최대 256자
- 제한 위반은 `IllegalArgumentException`을 발생시킨다.
- 비밀번호, 토큰 및 개인정보는 attributes에 전달하지 않는다.

## 설정

```kotlin
BeezKitMeasure.configure {
    enabled = BuildConfig.DEBUG
    historyCapacity = 100
    maxActiveMarks = 1_024
    reporter(appReporter)
}
```

| 옵션 | 기본값 |
| --- | --- |
| `enabled` | `true` |
| `historyCapacity` | `0` |
| `maxActiveMarks` | `1,024` |
| reporter | 없음 |

- 설정 변경은 이후 시작되는 측정부터 적용한다.
- 설정 변경 시 기존 history와 완료되지 않은 TAG 편의 span을 제거한다.
- 직접 생성한 `BeezKitMeasureSpan`은 시작 시점의 설정으로 완료된다.
- 전역 reporter에는 Activity, View 또는 Composable callback을 등록하지 않는다.

## Reporter와 History

```kotlin
fun interface BeezKitMeasureReporter {
    fun report(record: BeezKitMeasureRecord)
}

val records = BeezKitMeasure.records()
BeezKitMeasure.clear()
```

- reporter는 측정을 완료한 스레드에서 동기적으로 호출한다.
- reporter 호출을 위한 coroutine이나 별도 스레드를 만들지 않는다.
- 한 reporter의 `Exception`은 호스트 코드와 다른 reporter에 영향을 주지 않는다.
- history는 고정 용량 ring buffer이며 초과 시 가장 오래된 결과를 제거한다.
- `records()`는 오래된 순서의 immutable snapshot을 반환한다.

## 시간과 동시성

- `SystemClock.elapsedRealtimeNanos()` monotonic clock을 사용한다.
- 음수 경과 시간은 0으로 보정한다.
- 모든 공개 API는 여러 스레드에서 호출할 수 있다.
- span 완료, history, 설정과 TAG stack 접근을 동기화한다.
- 완료된 TAG span은 활성 stack에서 즉시 제거한다.

## 카탈로그 및 검증

카탈로그는 블록 반환값, 실패 재전파, 독립 span, 동일 TAG 중첩과 최근 10개 history를 실제 공개 API로 보여준다.

단위 테스트는 반환값, 성공, 실패, 취소, 비활성 모드, history eviction, TAG LIFO, 활성 한도, 고유 ID, 동시 종료, reporter 격리와 attributes 제한을 검증한다.

## 1차 구현 제외 범위

- sampling 및 slow threshold
- parent-child span
- percentile 및 통계 집계
- 디스크 저장과 네트워크 전송
- Timber, Firebase, OpenTelemetry adapter
- Inspector UI 연동
