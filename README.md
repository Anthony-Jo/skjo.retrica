# skjo.retrica
## 조성광: [레트리카] 확장 가능한 카메라 필터 엔진 설계

## TODO LIST
- [x] App Permission logic 
- [x] CameraX 
  - [x] Camera Preview UI
  - [x] Filter 적용 된 UI
  - [x] Filter 선택/변경/해제 UI
  - [x] OpenGL ES를 사용하여 간단한 Grayscale 셰이더 작성
- [x] 프레임 드랍 모니터링
  - [x] 실시간 모니터링 
- [x] lifeCycle 최적화
  - [x] Camera HW
  - [x] 그외 (view, data 등)
- [x] 문서화 
  - [x] markdown
  - [x] PDF
  - [x] 프레젠테이션 준비

## Link
- [git-hub repository](https://github.com/Anthony-Jo/skjo.retrica)
- [Confluence](https://zamake.atlassian.net/wiki/x/BQCw)
- [PDF download]()

## App Preview
- 개발 언어 : `Kotlin`
- 지원 언어 : 한국어 (`ko`)
- 최소 SDK (Min SDK): API 24 (Android 7.0 Nougat)
- 타겟 SDK (Target SDK): API 34 (Android 14)
- 디자인 패턴 (Architecture):
  - MVVM (Model-View-ViewModel)
  - Repository/UseCase 패턴을 적용하여 데이터 및 비즈니스 로직 분리
  - Hilt를 사용한 DI (Dependency Injection)

## 주요 사용 라이브러리 (Key Libraries)
  - `Jetpack CameraX`: 카메라 구현의 복잡성을 줄이고 안정성을 높이기 위해 사용. Preview, ImageAnalysis UseCase 활용.
  - `OpenGL ES 3.0`: 실시간 카메라 필터 렌더링을 위해 사용. GLSurfaceView와 셰이더(GLSL)를 활용하여 GPU 가속을 통한 고성능 그래픽 처리 구현.
  - `Kotlin Coroutines`: 이미지 처리와 같은 비동기 작업을 효율적으로 관리하고 UI 스레드의 부하를 줄이기 위해 사용.
  - `Hilt`: 의존성 주입을 통해 모듈화된 코드를 작성하고, 테스트 용이성 및 확장성을 확보. 싱글톤 관리를 위해 사용.
  - `Android Jetpack`: ViewModel, Lifecycle, Activity-KTX 등 AAC(Android Architecture Components)를 적극 활용하여 생명주기를 안전하게 관리.
  - `ViewBinding`: Null-safe 방식으로 View에 접근하여 런타임 에러 방지.

## 핵심 구현 기술 (Core Implementation)
- 실시간 필터 파이프라인: CameraX로부터 받은 카메라 프레임(SurfaceTexture)을 OpenGL ES 텍스처로 변환하고, Fragment Shader를 통해 실시간으로 필터(Grayscale, Sepia 등)를 적용하는 렌더링 파이프라인 구축.
- 동적 셰이더 관리: Map을 활용하여 여러 필터 셰이더 코드를 동적으로 교체 및 관리.
- 무한 스크롤 필터 선택 UI: RecyclerView와 LinearSnapHelper를 조합하여 중앙에 필터가 스냅되는 부드러운 UI/UX 구현.

## 개발 환경 및 Tool
- Android Studio Otter 3
- Google Gemini 2.5
- Device
  - Samsung Galaxy Z Flip5 (Android 16)
  - Samsung Galaxy S10 5G (Android 12)
  - Samsung Galaxy A8 (2018) (Android 9)
## 코드 리뷰 (Code Review)

### 1. View
