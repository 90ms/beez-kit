# Skeleton

[한국어](skeleton.md) | [English](skeleton.en.md)

**Module:** `:components:skeleton`  
**Artifact:** `io.github.beez:skeleton`  
**Status:** Experimental

## 목적

Compose 컴포넌트의 측정된 형태를 유지한 채 로딩 placeholder를 표시하고, 화면 단위에서는 호스트가 정의한 구조화된 skeleton과 실제 콘텐츠를 안전하게 전환한다.

Skeleton은 호스트 앱의 데이터 요청, 성공, 실패, 재시도 또는 캐시를 관리하지 않는다. 호스트가 로딩 상태의 유일한 소유자이며 BeezKit은 전달받은 상태로 표시 여부만 결정한다. 임의의 화면이나 아직 합성되지 않은 데이터를 분석해 skeleton 구조를 자동 생성하지 않는다.

## 설치

호스트 앱 모듈에 Skeleton artifact를 추가한다.

```kotlin
dependencies {
    implementation("io.github.beez:skeleton:<version>")
}
```

Skeleton은 Compose UI 타입만 공개 API에 사용하며 Material 또는 Material 3에 의존하지 않는다. 호스트 앱은 사용 중인 디자인 시스템의 색상과 shape를 그대로 전달할 수 있다.

## 어떤 API를 사용해야 하나요?

| 사용 상황 | API | 선택 기준 |
| --- | --- | --- |
| Text, Image, Card 하나 | `Modifier.bkSkeleton` | 실제 컴포넌트의 크기와 위치를 그대로 유지하고 싶을 때 |
| 한 영역의 여러 placeholder | `BeezKitSkeletonScope` | 로딩 상태, 스타일과 animation clock을 함께 공유할 때 |
| 화면 전체 또는 실제 콘텐츠와 구조가 다른 로딩 UI | `BeezKitSkeletonContainer` | 명시적인 skeleton 슬롯과 실제 content 슬롯을 전환할 때 |

가장 작은 API부터 사용한다. 단일 요소에는 Modifier만 적용하고, 목록처럼 여러 요소가 함께 움직일 때 Scope로 묶으며, 실제 화면과 skeleton 화면의 구조가 다를 때만 Container를 사용한다.

호스트의 상태는 일반적으로 `ViewModel`에서 lifecycle-aware 방식으로 수집한다.

```kotlin
@Composable
fun ProfileRoute(viewModel: ProfileViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreen(uiState)
}
```

BeezKit은 `uiState.isLoading`만 입력받으며 repository나 suspend 함수를 직접 실행하지 않는다.

## 기본 API

단일 컴포넌트는 `Modifier.bkSkeleton`으로 실제 측정 크기와 shape를 사용한다.

```kotlin
Text(
    text = user?.name.orEmpty(),
    modifier = Modifier
        .widthIn(min = 120.dp)
        .bkSkeleton(visible = uiState.isLoading),
)

AsyncImage(
    model = user?.imageUrl,
    contentDescription = null,
    modifier = Modifier
        .size(64.dp)
        .bkSkeleton(
            visible = uiState.isLoading,
            shape = CircleShape,
        ),
)
```

```kotlin
fun Modifier.bkSkeleton(
    visible: Boolean,
    shape: Shape = BeezKitSkeletonDefaults.shape,
    colors: BeezKitSkeletonColors = BeezKitSkeletonDefaults.colors(),
    animation: BeezKitSkeletonAnimation = BeezKitSkeletonDefaults.animation,
): Modifier
```

- `visible = false`이면 측정, 그리기, semantics와 입력을 변경하지 않는다.
- `visible = true`이면 콘텐츠의 측정 크기를 유지하고 그 위에 skeleton을 그린다.
- Modifier는 임의의 빈 콘텐츠에 너비나 높이를 추론하지 않는다. 빈 `Text`처럼 0으로 측정될 수 있는 콘텐츠는 호스트가 `size`, `widthIn` 또는 `height`를 지정한다.
- `shape`는 skeleton 그리기와 clipping에만 사용하며 호스트 콘텐츠의 shape를 변경하지 않는다.
- 로딩 중 장식된 콘텐츠의 입력과 semantics를 차단한다.
- Modifier 호출 하나는 자체 표시 상태를 가지지만 별도의 coroutine scope, timer 또는 process-global 상태를 만들지 않는다.

### 크기가 없는 콘텐츠

Skeleton은 장식된 컴포넌트가 측정한 크기를 사용한다. 로딩 중 빈 문자열을 전달한 `Text`는 너비가 0이 될 수 있으므로 최소 크기를 명시한다.

```kotlin
Text(
    text = user?.name.orEmpty(),
    modifier = Modifier
        .widthIn(min = 120.dp)
        .heightIn(min = 20.dp)
        .bkSkeleton(visible = uiState.isLoading),
)
```

데이터를 아직 모른다는 이유로 가짜 공백 문자열이나 투명한 콘텐츠를 만들어 크기를 맞추기보다는 `width`, `height`, `size` 또는 제약 조건을 명시하는 방식을 권장한다.

## 영역 단위 API

같은 로딩 상태와 애니메이션을 여러 placeholder가 공유할 때 `BeezKitSkeletonScope`를 사용한다.

```kotlin
BeezKitSkeletonScope(
    visible = uiState.isLoading,
    minimumVisibleDuration = 300.milliseconds,
) {
    Row {
        Box(
            Modifier
                .size(64.dp)
                .bkSkeleton(shape = CircleShape),
        )

        Column {
            Box(
                Modifier
                    .width(140.dp)
                    .height(20.dp)
                    .bkSkeleton(),
            )
            Box(
                Modifier
                    .width(200.dp)
                    .height(16.dp)
                    .bkSkeleton(),
            )
        }
    }
}
```

```kotlin
@Composable
fun BeezKitSkeletonScope(
    visible: Boolean,
    modifier: Modifier = Modifier,
    style: BeezKitSkeletonStyle = BeezKitSkeletonDefaults.style(),
    minimumVisibleDuration: Duration = Duration.ZERO,
    loadingDescription: String? = null,
    content: @Composable BeezKitSkeletonScope.() -> Unit,
)
```

- Scope 안의 인자 없는 `Modifier.bkSkeleton()`은 부모의 표시 상태와 style을 사용한다.
- 자식은 `shape`와 `colors`를 개별적으로 재정의할 수 있다. animation과 속도는 공유 clock의 일관성을 위해 Scope 단위로 설정한다.
- Scope는 움직이는 모든 자식에 하나의 animation progress를 공유한다. 자식은 자신의 측정 크기에 맞는 draw 계산만 수행한다.
- Scope가 숨겨졌거나 움직임이 비활성화되면 공유 무한 애니메이션을 실행하지 않는다.
- 중첩 Scope의 자식은 가장 가까운 Scope의 상태와 style을 사용한다.
- `minimumVisibleDuration`은 Scope당 하나의 timer만 사용하며 개별 Modifier에는 제공하지 않는다.

## 화면 단위 API

실제 화면과 placeholder의 구조가 다를 때 `BeezKitSkeletonContainer`를 사용한다.

```kotlin
BeezKitSkeletonContainer(
    loading = uiState.isLoading,
    minimumVisibleDuration = 300.milliseconds,
    skeleton = {
        ProfileScreenSkeleton()
    },
) {
    ProfileScreenContent(uiState.profile)
}
```

```kotlin
@Composable
fun BeezKitSkeletonContainer(
    loading: Boolean,
    modifier: Modifier = Modifier,
    style: BeezKitSkeletonStyle = BeezKitSkeletonDefaults.style(),
    minimumVisibleDuration: Duration = Duration.ZERO,
    transition: BeezKitSkeletonTransition = BeezKitSkeletonTransition.None,
    loadingDescription: String? = null,
    skeleton: @Composable BeezKitSkeletonScope.() -> Unit,
    content: @Composable () -> Unit,
)
```

- 기본 `None` 전환은 skeleton과 content 중 현재 상태 하나만 합성한다.
- 로딩이 끝나면 별도 상태를 복사하거나 전달하지 않고 호스트가 제공한 `content`를 합성한다.
- `Crossfade`는 전환 구간에 두 슬롯을 동시에 합성할 수 있다. 이때 비활성 슬롯의 입력과 semantics를 차단하며 전환이 끝나면 Composition에서 제거한다.
- Skeleton 슬롯 안의 자식은 Container가 제공하는 하나의 Scope와 animation progress를 공유한다.
- Container는 숨긴 실제 콘텐츠를 미리 합성하지 않으므로 호스트 콘텐츠의 `LaunchedEffect`나 비동기 작업을 로딩 중에 시작하지 않는다.

### 완전한 화면 예제

```kotlin
@Composable
fun ProfileScreen(uiState: ProfileUiState) {
    BeezKitSkeletonContainer(
        loading = uiState.isLoading,
        minimumVisibleDuration = 300.milliseconds,
        transition = BeezKitSkeletonTransition.Crossfade(),
        loadingDescription = "프로필을 불러오는 중",
        skeleton = { ProfileSkeleton() },
    ) {
        ProfileContent(profile = uiState.profile)
    }
}

@Composable
private fun BeezKitSkeletonScope.ProfileSkeleton() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier
                .size(64.dp)
                .bkSkeleton(shape = CircleShape),
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(width = 140.dp, height = 20.dp).bkSkeleton())
            Box(Modifier.size(width = 220.dp, height = 16.dp).bkSkeleton())
        }
    }
}
```

## 애니메이션

```kotlin
sealed interface BeezKitSkeletonAnimation {
    data object Static : BeezKitSkeletonAnimation

    data class Pulse(
        val duration: Duration = 1_000.milliseconds,
        val minAlpha: Float = 0.6f,
        val maxAlpha: Float = 1f,
    ) : BeezKitSkeletonAnimation

    data class Shimmer(
        val duration: Duration = 1_200.milliseconds,
        val widthFraction: Float = 0.35f,
        val angleDegrees: Float = 20f,
        val direction: BeezKitSkeletonDirection =
            BeezKitSkeletonDirection.StartToEnd,
    ) : BeezKitSkeletonAnimation
}
```

| 타입 | 동작 |
| --- | --- |
| `Static` | 움직임 없이 base color만 표시한다. |
| `Pulse` | `minAlpha`와 `maxAlpha` 사이를 반복한다. |
| `Shimmer` | base color 위로 highlight band를 반복 이동한다. |

방향은 `StartToEnd`, `EndToStart`, `TopToBottom`, `BottomToTop`을 제공한다. 수평 방향은 현재 `LayoutDirection`을 따르므로 RTL에서 `StartToEnd`의 물리적 방향이 반전된다.

- 움직이는 animation의 `duration`은 1ms 이상, `Int.MAX_VALUE`ms 이하여야 한다.
- `minAlpha`와 `maxAlpha`는 `0f..1f`이고 `minAlpha <= maxAlpha`여야 한다.
- `widthFraction`은 `0f < widthFraction <= 1f`여야 한다.
- `angleDegrees`는 유한한 값이어야 한다.
- 잘못된 공개 입력은 호출 지점에서 `IllegalArgumentException`을 발생시킨다.
- animation 진행은 draw만 무효화하며 매 frame 측정이나 배치를 다시 실행하지 않는다.

## 색상과 Style

```kotlin
@Immutable
data class BeezKitSkeletonColors(
    val base: Color,
    val highlight: Color,
)

@Immutable
data class BeezKitSkeletonStyle(
    val colors: BeezKitSkeletonColors,
    val shape: Shape,
    val animation: BeezKitSkeletonAnimation,
)
```

```kotlin
BeezKitSkeletonScope(
    visible = uiState.isLoading,
    style = BeezKitSkeletonDefaults.style(
        colors = BeezKitSkeletonDefaults.colors(
            base = MaterialTheme.colorScheme.surfaceVariant,
            highlight = MaterialTheme.colorScheme.surface,
        ),
        animation = BeezKitSkeletonAnimation.Shimmer(
            duration = 900.milliseconds,
        ),
    ),
) {
    ProfileSkeleton()
}
```

- 기본 shape는 `RoundedCornerShape(8.dp)`이다.
- 기본 animation은 `Shimmer()`이다.
- 기본 colors는 Material에 의존하지 않는 불투명한 중립색을 제공한다.
- Material theme 연결은 호스트가 `Color` 값으로 전달한다. 공개 API는 Material 타입을 노출하지 않는다.
- colors, style과 animation 모델은 immutable 값이며 Activity, Context, View 또는 callback을 보관하지 않는다.

## 표시 시간과 상태 전이

`minimumVisibleDuration`은 빠른 로딩에서 skeleton이 순간적으로 깜빡이는 것을 방지한다. 실제 로딩을 끝내거나 최대 표시 시간을 제한하지 않는다.

```text
Hidden
  loading=true  -> Visible

Visible
  loading=true  -> Visible
  loading=false + minimum elapsed     -> Hidden
  loading=false + minimum not elapsed -> WaitingToHide

WaitingToHide
  loading=true  -> Visible
  timer elapsed -> Hidden
```

- 로딩이 최소 시간보다 오래 걸리면 로딩이 끝나는 즉시 숨긴다.
- 대기 중 `loading = true`가 다시 들어오면 예정된 hide를 취소한다.
- 이전 timer 완료가 최신 로딩 상태를 덮어쓰지 않는다.
- 경과 시간은 monotonic clock으로 계산하며 시스템 날짜 변경의 영향을 받지 않는다.
- `minimumVisibleDuration`은 0 이상이고 유한해야 한다.
- 최초 합성 시 `loading = true`이면 그 시점을 표시 시작점으로 사용한다.
- Scope 또는 Container가 Composition에서 제거되면 대기 중 timer를 취소한다.
- animation progress, timer와 전환 진행률은 구성 변경이나 프로세스 재생성 시 복원하지 않는다. 비즈니스 로딩 상태의 복원은 호스트 책임이다.

고정 시간이 지나면 실제 로딩 상태와 무관하게 숨기는 `duration`, 비동기 block을 실행하는 `loader`, 명령형 `showFor()`는 기본 API에 포함하지 않는다.

## 전환

```kotlin
sealed interface BeezKitSkeletonTransition {
    data object None : BeezKitSkeletonTransition

    data class Crossfade(
        val duration: Duration = 200.milliseconds,
    ) : BeezKitSkeletonTransition
}
```

- `None`은 기본값이며 로딩 상태 전환 시 즉시 슬롯을 교체한다.
- `Crossfade.duration`은 1ms 이상, `Int.MAX_VALUE`ms 이하여야 한다.
- crossfade 도중 로딩이 다시 시작되면 현재 전환을 취소하고 최신 상태를 향해 전환한다.
- opacity와 무관하게 논리적으로 비활성인 슬롯은 입력과 semantics를 제공하지 않는다.
- 시스템에서 움직임 축소가 요청되면 crossfade를 생략한다.

## 생명주기와 비동기 안전성

- Skeleton은 repository, suspend block, `Flow`, retry 또는 네트워크 요청을 받거나 실행하지 않는다.
- 호스트는 `ViewModel` 등에서 상태를 관리하고 lifecycle-aware 방식으로 `loading`을 전달한다.
- timer와 animation은 현재 Composition 또는 Modifier node가 명시적으로 소유한다.
- Scope, Container 또는 Modifier가 detach되면 관련 timer, animation과 callback을 즉시 취소하거나 해제한다.
- process-global coroutine scope, animation registry 또는 content registry를 만들지 않는다.
- Activity, Context, View, Window, LayoutCoordinates, Composable lambda 또는 `CoroutineScope`를 전역 상태에 보관하지 않는다.
- parameter가 변경되면 기존 node를 갱신하고 불필요하게 교체하지 않는다.
- 빠른 `true -> false -> true` 전환에서 취소된 작업이 최신 표시 상태를 변경하지 않는다.

## 접근성 및 입력

- 로딩 중 실제 콘텐츠는 접근성 트리에서 숨기고 클릭, long click과 pointer 입력을 차단한다.
- 개별 skeleton block은 의미 없는 접근성 항목으로 노출하지 않는다.
- `loadingDescription`이 null이 아니면 Scope 또는 Container가 로딩 상태를 한 번만 알린다.
- 리스트의 각 placeholder가 동일한 로딩 설명을 반복하지 않는다.
- skeleton이 사라지면 실제 콘텐츠의 semantics와 입력을 복원한다.
- crossfade 중 두 슬롯이 동시에 읽히거나 동작하지 않는다.
- 시스템의 reduced-motion 설정을 존중한다. 움직임이 비활성화되면 `Pulse`와 `Shimmer`는 `Static`으로, `Crossfade`는 즉시 교체로 대체한다.

## 성능과 메모리

- draw 단계에서 placeholder를 합성하고 animation frame마다 remeasure를 요구하지 않는다.
- Scope 밖의 독립 Modifier는 필요한 경우에만 자체 animation state를 가지며 숨겨지면 중단한다.
- Scope와 Container는 `LazyColumn`을 포함한 모든 자식에 하나의 animation progress를 공유한다.
- brush와 geometry cache는 현재 node 크기에만 연결하며 크기 변경 또는 detach 시 오래된 값을 해제한다.
- queue, history 또는 process-wide cache를 만들지 않는다.
- 숨겨진 상태에서 frame을 생성하거나 coroutine을 유지하지 않는다.
- 여러 화면과 여러 Scope의 상태는 서로 독립적이며 display tag 또는 전역 key로 연결하지 않는다.

## 카탈로그 및 검증

카탈로그는 다음 시나리오를 실제 공개 API로 제공한다.

- 단일 Text, 원형 image와 둥근 card Modifier
- `Static`, `Pulse`, `Shimmer` 전환
- animation 속도, base/highlight color, shape, 방향과 shimmer 폭 조절
- 하나의 Scope를 공유하는 목록 placeholder
- 화면 단위 Container와 `None`/`Crossfade` 전환
- 빠른 로딩 완료와 `minimumVisibleDuration`
- 빠른 `loading` 반복 변경
- reduced-motion과 로딩 설명 확인

단위 및 Compose UI 테스트는 다음을 검증한다.

- 최초 표시/숨김과 parameter recomposition
- 측정 크기 유지, shape와 colors 적용
- 최소 표시 시간, monotonic time과 timer 취소
- `true -> false -> true` 경쟁과 detach/reattach
- Scope의 animation 공유와 숨김 상태 중 animation 중단
- 목록 item 제거와 node detach cleanup
- 로딩 중 semantics 제거 및 click 차단, 종료 후 복원
- crossfade 도중 슬롯 격리와 빠른 상태 반전
- reduced-motion에서 정적 fallback
- 여러 Scope, Container와 owner 간 상태 격리
- 잘못된 duration, alpha, width와 angle 입력 검증

## 1차 구현 제외 범위

- 화면 구조 또는 콘텐츠를 분석하는 자동 skeleton 생성
- XML View 및 Android `View` adapter
- 데이터 로딩, retry, error UI 또는 cache 관리
- 고정 시간 후 자동으로 숨기는 `showFor()` API
- process-global theme 또는 animation 설정
- gradient stop 직접 편집과 사용자 정의 animation callback
- skeleton 레이아웃의 자동 너비 및 높이 추론
- placeholder 이미지 생성 또는 bitmap cache
