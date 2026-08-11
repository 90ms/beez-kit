# BeezKit

[한국어](README.md) | [English](README.en.md)

BeezKit은 호스트 앱에 작은 유틸리티, 진단 도구, 재사용 가능한 UI 컴포넌트를 최소한의 설정으로 추가하기 위한 Compose 중심 Android 툴킷입니다. 공개 API 타입에는 `BeezKit` 접두사를, Modifier 확장 함수에는 간결한 `bk` 접두사를 사용합니다.

> 현재 상태: 초기 개발 단계입니다. 쓰로틀링 모듈은 실험적으로 사용할 수 있으며 나머지 모듈은 기본 골격만 준비되어 있습니다.

## 제공 라이브러리

| 분류 | 라이브러리 | 아티팩트 | 상태 | 상세 스펙 |
| --- | --- | --- | --- | --- |
| 툴킷 | 쓰로틀링 | `io.github.beez:throttle` | 실험적 | [상세](docs/modules/throttle.md) |
| 툴킷 | 스택 트레이스 | `io.github.beez:stacktrace` | 예정 | [상세](docs/modules/stacktrace.md) |
| 툴킷 | 함수 시간 측정 | `io.github.beez:measure` | 예정 | [상세](docs/modules/measure.md) |
| 디버그 | Inspector Core | `io.github.beez:inspector-core` | 예정 | [상세](docs/modules/inspector.md) |
| 디버그 | Inspector Network | `io.github.beez:inspector-network` | 예정 | [상세](docs/modules/inspector.md#network-collector) |
| 디버그 | Inspector Event | `io.github.beez:inspector-event` | 예정 | [상세](docs/modules/inspector.md#event-collector) |
| 디버그 | Inspector WebView | `io.github.beez:inspector-webview` | 예정 | [상세](docs/modules/inspector.md#webview-collector) |
| 컴포넌트 | Toast | `io.github.beez:toast` | 예정 | [상세](docs/modules/toast.md) |
| 컴포넌트 | Snackbar | `io.github.beez:snackbar` | 예정 | [상세](docs/modules/snackbar.md) |
| 컴포넌트 | Tooltip | `io.github.beez:tooltip` | 예정 | [상세](docs/modules/tooltip.md) |
| 컴포넌트 | Skeleton | `io.github.beez:skeleton` | 예정 | [상세](docs/modules/skeleton.md) |

## 목표 API

BeezKit은 기본 사용법은 한 줄로 끝내고, 필요한 경우에만 옵션을 추가하는 API를 지향합니다.

```kotlin
BeezKitToast.success("저장되었습니다")
BeezKitSnackbar.error("네트워크 오류")
BeezKitMeasure.trace("load-user") { repository.loadUser() }
BeezKitStackTrace.log(userId)

Modifier.bkThrottledClickable { submit() }
Modifier.bkSkeleton(visible = isLoading)
Modifier.bkTooltip("여기에서 프로필을 수정할 수 있어요")
```

쓰로틀링 예제는 현재 사용할 수 있습니다. 나머지 코드는 목표 API이며 해당 모듈이 구현되기 전까지 컴파일되지 않습니다.

## 프로젝트 구조

```text
toolkit/       작은 런타임 유틸리티 및 진단 라이브러리
components/    Compose 중심 UI 컴포넌트
samples/       호스트 앱 통합 예제 및 컴포넌트 카탈로그
build-logic/   공통 Gradle convention plugin
docs/modules/  모듈별 기준 스펙
.agents/skills 저장소 전용 Codex 작업 절차
```

모듈 경계와 설계 원칙은 [아키텍처 문서](docs/architecture.md), 샘플 등록과 화면 구성 규칙은 [카탈로그 문서](docs/sample-catalog.md)를 참고하세요.

## 빌드

Android SDK 36과 JDK 17 이상을 설치한 뒤 실행합니다.

```shell
./gradlew build
```

컴포넌트 카탈로그 애플리케이션은 `:samples:catalog`에서 확인할 수 있습니다.
