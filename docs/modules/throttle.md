# Throttle

[한국어](throttle.md) | [English](throttle.en.md)

**Module:** `:toolkit:throttle`
**Artifact:** `io.github.beez:throttle`
**Status:** Experimental

## 목적

Compose에서 짧은 시간 동안 반복되는 클릭을 제한해 중복 제출, 중복 Navigation, 여러 번의 결제 요청과 같은 실수를 방지한다. 첫 클릭은 즉시 실행하고 지정한 간격 안의 후속 클릭은 무시하는 leading-edge 방식이다.

Throttle은 비즈니스 작업이나 coroutine을 실행하지 않는다. 호스트가 제공한 `onClick` 호출 시점만 제한하며, 작업 성공·실패·취소 상태는 호스트가 관리한다.

## 설치

```kotlin
dependencies {
    implementation("io.github.beez:throttle:<version>")
}
```

## 기본 API

기본 제한 간격은 `500.milliseconds`다.

일반적으로 자체 클릭 처리가 없는 `Box`, `Surface` 또는 사용자 정의 컴포넌트에 Modifier를 적용한다.

```kotlin
Surface(
    modifier = Modifier.bkThrottledClickable {
        openDetail()
    },
) {
    ProductCardContent()
}
```

`Button`처럼 이미 `onClick`을 제공하는 컴포넌트에는 이중 클릭 semantics가 생기지 않도록 컴포넌트 자체의 상태 관리 방식을 우선한다.

## 상세 옵션

```kotlin
Modifier.bkThrottledClickable(
    interval = 750.milliseconds,
    enabled = true,
    onClickLabel = "주문 제출",
    role = Role.Button,
) {
    submitOrder()
}
```

| 옵션 | 기본값 | 의미 |
| --- | --- | --- |
| `interval` | `500.milliseconds` | 첫 클릭 이후 후속 클릭을 제한할 시간 |
| `enabled` | `true` | `false`이면 클릭과 접근성 click action 비활성화 |
| `onClickLabel` | `null` | 접근성 서비스에 제공할 동작 설명 |
| `role` | `null` | Button 등 접근성 역할 |
| `onClick` | 필수 | 허용된 클릭에서 실행할 최신 callback |

기본 간격은 `BeezKitDefaultThrottleInterval`로 공개한다.

```kotlin
val interval = BeezKitDefaultThrottleInterval // 500.milliseconds
```

## 클릭 처리 규칙

```text
첫 클릭                 -> 즉시 허용
interval 안의 후속 클릭 -> 무시
interval 경계 이후 클릭 -> 허용하고 새 구간 시작
```

- 각 Modifier 인스턴스가 독립적인 제한 상태를 가진다.
- 같은 화면의 두 버튼은 서로의 클릭을 제한하지 않는다.
- recomposition 후에도 마지막으로 허용한 클릭 시각을 유지한다.
- `onClick`이 변경되면 다음 허용 클릭은 최신 callback을 실행한다.
- `interval`이 변경되면 다음 클릭부터 새 값을 적용한다.
- 음수 간격은 `IllegalArgumentException`을 발생시킨다.
- 0 간격은 모든 클릭을 허용한다.

## 비활성화

```kotlin
Modifier.bkThrottledClickable(
    enabled = uiState.canSubmit,
) {
    submit()
}
```

`enabled = false`이면 Compose `clickable`의 비활성 동작을 따른다. 시각적 indication, pointer 입력, click semantics와 접근성 상태도 Compose 기본 동작을 유지한다. 다시 활성화해도 기존 Modifier 인스턴스의 throttle 기록은 유지된다.

## 시간, 생명주기 및 메모리

- 경과 시간은 `SystemClock.elapsedRealtimeNanos()` monotonic clock으로 계산한다.
- 시스템 날짜나 시간대 변경의 영향을 받지 않는다.
- 클릭이 발생할 때만 시간을 읽으며 coroutine, timer 또는 worker를 만들지 않는다.
- composed Modifier 인스턴스마다 작은 gate 하나만 유지한다.
- Activity, View, Context 또는 Composable callback을 process-global 상태에 저장하지 않는다.
- Modifier가 Composition에서 제거되면 gate와 callback 참조도 함께 해제된다.
- 시스템 monotonic clock이 이전 값보다 작아진 경우 새 실행 환경으로 보고 다음 클릭을 허용한다.

## 접근성

Compose `clickable`을 사용하므로 indication, interaction, click semantics와 접근성 동작을 그대로 제공한다. 아이콘처럼 의미가 불분명한 대상에는 `onClickLabel`과 `role`을 지정한다.

```kotlin
Icon(
    imageVector = Icons.Default.Refresh,
    contentDescription = null,
    modifier = Modifier.bkThrottledClickable(
        onClickLabel = "새로고침",
        role = Role.Button,
    ) {
        refresh()
    },
)
```

Throttle은 실행 중 상태를 표현하거나 클릭 대상의 시각적 비활성화를 자동으로 관리하지 않는다. 장시간 작업은 호스트가 `enabled`와 로딩 UI를 함께 제어한다.

## 카탈로그 및 검증

카탈로그는 기본 500ms 제한과 사용자 지정 2초 제한을 실제 공개 API로 보여주며 허용된 클릭 횟수를 표시한다.

단위 테스트는 첫 클릭, 제한 구간의 클릭, 경계 시각, interval 변경과 0 간격을 검증한다. Compose 통합 검증은 독립 Modifier, enabled 변경, recomposition, 최신 callback과 접근성 semantics를 다룬다. clock 역행 동작은 Stable 전환 전에 추가 검증한다.

## 1차 구현 제외 범위

- trailing-edge 실행과 debounce
- 여러 Modifier가 공유하는 전역 throttle key
- coroutine 또는 suspend 작업 실행
- 작업 완료까지 자동 비활성화
- 클릭 횟수 기록과 분석
