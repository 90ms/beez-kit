# BeezKit Sample Catalog

## 목적

`samples:catalog`는 BeezKit의 모든 공개 모듈을 호스트 앱 관점에서 발견하고 실행하며 상태와 결과를 확인하는 Android 애플리케이션이다. 카탈로그는 라이브러리 배포물이 아니며 샘플 전용 UI와 의존성을 라이브러리 모듈에 전파하지 않는다.

## 기본 원칙

- 모든 공개 모듈은 하나 이상의 카탈로그 항목을 갖는다.
- 구현된 항목은 해당 모듈의 실제 공개 API를 직접 호출한다.
- 기본 사용법, 주요 옵션, 경계 상태와 실행 결과를 화면에서 확인할 수 있어야 한다.
- 구현되지 않은 모듈도 `Planned` 상태와 목표를 표시해 전체 범위를 탐색할 수 있게 한다.
- 화면 문구는 한국어를 기본으로 한다.
- 샘플 전용 상태, 데모 데이터와 UI는 `samples:catalog` 안에 둔다.
- Material 3는 카탈로그 shell에만 사용하며 BeezKit 공개 API에 노출하지 않는다.
- Beez Design은 공개 Maven artifact가 준비되고 사용자가 전환을 요청하기 전까지 참조하지 않는다.

## 정보 구조

카탈로그 항목은 다음 세 카테고리로 분류한다.

| 카테고리 | 대상 |
| --- | --- |
| Toolkit | Throttle, Stack Trace, Measure |
| Debug | Inspector Core, Network, Event, WebView |
| Component | Toast, Snackbar, Tooltip, Skeleton |

각 항목은 안정적인 `id`, Gradle module path, 이름, 설명, 상태와 샘플 content를 가진다. 목록은 카테고리 순서와 registry 선언 순서를 유지한다.

## 상세 화면 계약

각 상세 화면은 적용 가능한 범위에서 다음 내용을 제공한다.

1. 모듈 이름, 상태, 설명
2. 기본 사용 예제
3. 옵션을 변경할 수 있는 Playground
4. 실행 결과 또는 현재 상태
5. 오류, 비활성화, 빠른 반복 입력 등 경계 시나리오
6. lifecycle, 메모리 또는 접근성 관련 관찰 포인트

`Planned` 항목은 아직 API가 없다는 사실을 명확히 표시하며 가짜 구현을 만들지 않는다.

## 상태

| 상태 | 의미 |
| --- | --- |
| Planned | 스펙과 모듈 골격만 존재하며 공개 API가 없음 |
| Experimental | 최소 API, 구현, 테스트와 실행 가능한 샘플이 존재함 |
| Stable | 호환성 정책과 전체 릴리스 검증을 통과함 |

README와 카탈로그의 상태는 같은 변경에서 함께 갱신한다.

## 등록 규칙

- registry의 module path는 `settings.gradle.kts`의 경로와 정확히 일치해야 한다.
- registry의 id는 소문자 kebab-case를 사용하고 공개 후 의미 없이 변경하지 않는다.
- Inspector collector처럼 하나의 통합 Playground를 공유할 수 있지만 각 공개 artifact는 별도 entry로 발견 가능해야 한다.
- 새 모듈을 추가할 때 Gradle 등록, 상세 스펙, README 표, catalog registry와 sample dependency를 한 변경으로 추가한다.

## 검증

로컬에서는 링크, registry, Gradle wiring과 스킬 구조만 정적으로 검사한다. Gradle build, test, lint와 APK assemble은 GitHub Actions에서만 실행한다.
