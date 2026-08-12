# Stack Trace

[한국어](stacktrace.md) | [English](stacktrace.en.md)

**Module:** `:toolkit:stacktrace`  
**Artifact:** `io.github.beez:stacktrace`  
**Status:** Experimental

## 목적

현재 코드가 어떤 호출 경로를 거쳐 실행됐는지 제한된 값과 함께 수집한다. 일반 로깅이나 예외 처리를 대체하지 않으며 중복 호출, 예상하지 못한 상태 변경, Navigation과 lifecycle 경로 진단에 사용한다.

## 설치

```kotlin
dependencies {
    implementation("io.github.beez:stacktrace:<version>")
}
```

개발·진단 용도로만 사용할 경우 호스트 앱에서 `debugImplementation`으로 제한할 수 있다.

```kotlin
debugImplementation("io.github.beez:stacktrace:<version>")
```

## 기본 API

```kotlin
BeezKitStackTrace.log(userId)

BeezKitStackTrace.log(
    value = userId,
    tag = "user-update",
)
```

- 전달받은 객체는 호출 시점에 문자열로 변환한다.
- 원본 객체와 `StackTraceElement`는 record나 history에 저장하지 않는다.
- 반환된 `BeezKitStackTraceRecord?`는 즉시 확인용이며 비활성 상태에서는 `null`이다.

비용 있는 값은 lazy API를 사용한다.

```kotlin
BeezKitStackTrace.log(tag = "state-change") {
    "state=${state::class.simpleName}"
}
```

- 비활성 상태에서는 lambda와 stack collector를 실행하지 않는다.
- lambda의 `Exception`은 전파하지 않고 `<value-provider-error: Type>`으로 기록한다.
- 예외 메시지는 민감정보와 크기 문제로 기록하지 않는다.

## 호출별 옵션

```kotlin
BeezKitStackTrace.log(
    value = route,
    tag = "navigation",
    maxFrames = 12,
    skipFrames = 1,
    includeFrameworkFrames = false,
)
```

| 옵션 | 의미 |
| --- | --- |
| `tag` | 최대 64자의 선택적 구분 이름 |
| `maxFrames` | 이번 호출에서 유지할 최대 프레임 수 |
| `skipFrames` | 필터링 후 추가로 제외할 상단 프레임 수 |
| `includeFrameworkFrames` | Android, Compose와 coroutine 프레임 포함 여부 |

`maxFrames`는 1~256, `skipFrames`는 0 이상이어야 한다. 지정하지 않은 옵션은 전역 설정을 사용한다.

## 결과 모델

```kotlin
data class BeezKitStackTraceRecord(
    val tag: String?,
    val value: String,
    val frames: List<BeezKitStackFrame>,
)

data class BeezKitStackFrame(
    val className: String,
    val methodName: String,
    val fileName: String?,
    val lineNumber: Int?,
)
```

- frames는 수정할 수 없는 snapshot이다.
- 사용할 수 없는 파일명과 0 이하의 줄 번호는 `null`로 표현한다.
- 필터 후 프레임이 없어도 빈 frames record를 생성한다.

## 설정

```kotlin
BeezKitStackTrace.configure {
    enabled = BuildConfig.DEBUG
    historyCapacity = 50
    defaultMaxFrames = 8
    maxValueLength = 512
    includeFrameworkFrames = false

    excludePackage("com.example.common.logging")
    reporter(appReporter)
}
```

| 옵션 | 기본값 |
| --- | ---: |
| `enabled` | `true` |
| `historyCapacity` | `0` |
| `defaultMaxFrames` | `8` |
| `maxValueLength` | `512` |
| `includeFrameworkFrames` | `false` |
| reporter | 없음 |

- `historyCapacity`는 최대 10,000, `maxValueLength`는 최대 16,384다.
- reporter와 제외 package prefix는 각각 최대 32개다.
- 설정 변경은 이후 호출부터 적용하며 기존 history를 제거한다.
- history가 `0`이면 전역 메모리에 record를 보관하지 않는다.
- 자동 Logcat 출력은 제공하지 않는다.
- reporter에는 Activity, View 또는 Composable callback을 등록하지 않는다.

## 값 변환

즉시 값은 `null`이면 `"null"`, 아니면 `toString()` 결과를 사용한다. `toString()`의 `Exception`은 `<value-format-error: Type>`으로 격리한다.

문자열이 `maxValueLength`를 초과하면 전체 결과 길이를 제한하면서 `…[truncated]`를 붙인다. 비밀번호, 인증 토큰과 개인정보는 전달하지 않으며 민감한 객체에는 lazy API를 사용한다.

## 프레임 필터

다음 순서로 적용한다.

1. BeezKit StackTrace 및 `Thread` 내부 프레임 제외
2. reflection 프레임 제외
3. 사용자 package prefix 제외
4. 기본적으로 Android, Compose와 coroutine 프레임 제외
5. `skipFrames` 적용
6. `maxFrames` 적용
7. 공개 immutable 모델로 변환

framework 기본 제외 prefix는 `android.`, `androidx.compose.`, `kotlin.coroutines.`, `kotlinx.coroutines.`이다. reflection prefix는 항상 제외한다. 필요한 호출만 `includeFrameworkFrames = true`로 포함한다.

사용자 필터는 정규식이 아닌 최대 32개의 package prefix를 사용한다. 각 prefix는 공백일 수 없으며 최대 200자다.

## Reporter와 History

```kotlin
fun interface BeezKitStackTraceReporter {
    fun report(record: BeezKitStackTraceRecord)
}

val records = BeezKitStackTrace.records()
BeezKitStackTrace.clear()
```

- reporter는 호출 스레드에서 동기 실행한다.
- 별도 coroutine이나 worker를 생성하지 않는다.
- 한 reporter의 `Exception`은 호스트와 다른 reporter에 영향을 주지 않는다.
- history는 고정 용량 ring buffer이며 초과 시 가장 오래된 record를 제거한다.
- `records()`는 오래된 순서의 수정 불가능한 snapshot을 반환한다.

## 비활성 상태 및 안전성

비활성 상태에서는 값 변환, lazy lambda, stack 수집, 필터링, record 생성, reporter와 history 저장을 모두 건너뛴다.

모든 공개 API는 여러 스레드에서 호출할 수 있다. 설정과 history 접근은 동기화하며 활성 호출을 전역 목록에 보관하지 않는다. 값 길이, 프레임 수, 제외 package 수와 history 용량은 항상 제한한다.

## 카탈로그 및 테스트

카탈로그는 기본 기록, lazy wrapper와 `skipFrames`, framework 포함, bounded history와 비활성 fast path를 실제 API로 보여준다.

단위 테스트는 내부/framework 필터, package 제외, skip/limit, 비활성 모드, 값 변환 실패, lazy 실패, 문자열 제한, reporter 격리, history eviction, immutable snapshot과 동시 기록을 검증한다.

## 1차 구현 제외 범위

- TAG별 rate limit과 sampling
- 동일 stack 병합 및 횟수 집계
- 디스크 또는 네트워크 저장
- Timber, Firebase, OpenTelemetry adapter
- Inspector UI 연동
- R8/ProGuard mapping과 소스 링크 생성
