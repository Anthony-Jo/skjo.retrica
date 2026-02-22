# 조성광: [레트리카] 확장 가능한 카메라 필터 엔진 설계

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
#### 1.1. `BaseActivity`
- 모든 Activity 가 상속받는 부모 class
- ViewBinding 수행
- 상단 상태바 투명한 UI 로 적용하고 그 height 만큼 padding 적용
```kotlin
   /**
     * 상태 표시줄을 투명하게 만들고, 콘텐츠를 상태 표시줄 뒤로 확장 (Edge-to-Edge)
     */
    private fun setTransparentStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
    }

    /**
     * 시스템 UI(상태 표시줄 등)가 차지하는 영역을 가져와, 해당 영역만큼 패딩 적용
     */
    private fun setStatusBarPadding() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(left = systemBars.left, top = systemBars.top, right = systemBars.right, bottom = systemBars.bottom)
            insets
        }
    }
```
- 권한 허용 필요 안내 dialog 노출
<img width="270" height="760" alt="Screenshot_20260223_000138" src="https://github.com/user-attachments/assets/1eed3862-bdd4-4dbe-97fa-84dbb584dbc3" />


#### 1.2. `SplashActivity`
- 앱 실행시 진입
<img width="270" height="760" alt="Screenshot_20260223_000053" src="https://github.com/user-attachments/assets/63b00d02-9ebe-4f25-88db-f22ea3dda979" />

- permission (Camera) 체크 후 2초간 delay 후 `MainActivity` 진입
#### 1.3 `MainActivity`
- View 상단 CameraX Preview
  - 카메라 및 렌더러 초기화
  - 실시간 FPS, 현재 적용 Filter 모니터링 
- View 하단 컨트롤 UI
  - Front/Back Camera 전환 button 
  - 무한 스크롤 필터 UI

<img width="270" height="760" alt="Screenshot_20260223_000216" src="https://github.com/user-attachments/assets/e89e3ca0-5ee3-46e0-9d53-4fca722c67d9" />

### 2. Model
#### 2.1 `FilterType`
- Camera filter type Enum 엔드리로 구성
- type name, 썸네일 정의
#### 2.2 `CameraType`
- 전면 / 후면 카메라 타입 정의
- 휴먼 에러 방지를 위해 열거한 카메라 타입만 받도록 함
### 3. ViewModel
#### 3.1 `MainViewModel`
- `Coroutine` 사용하여 데이터 비동기 처리
```kotlin   
    fun saveLastFilter(filterType: FilterType) {
        viewModelScope.launch {
            sharedPrefWrapper.setLastFilter(filterType)
            _currentFilter.postValue("Filter: ${filterType.name}")
        }
    }
```
- 사용자가 최근 사용한 상태 저장/불러오기 (카메라 타입, 필터)
```kotlin
  private fun loadInitialData() {
        viewModelScope.launch {
            // 여러 비동기 로딩이 있다면 여기서 한 번에 처리
            val lastCamera = sharedPrefWrapper.getLastCamera()
            val lastFilter = sharedPrefWrapper.getLastFilter()

            _lastUsedCamera.value = lastCamera
            _lastSelectedFilter.value = lastFilter

            // 모든 데이터 로딩이 끝나면 상태를 true로 변경
            _isInitialized.value = true
        }
    }
```
### 4. 기타
#### 4.1 `GLRenderer`
- OpenGL ES 렌더링의 모든 핵심 로직
- `CameraX` 로 부터 `Surface` 를 제공 받음
- 카메라 프레임을 `SurfaceTexture`로 받고, `onDrawFrame`에서 이 텍스처를 GPU에 업로드
- 동적으로 선택된 Fragment Shader를 통해 필터를 적용하고 화면에 렌더링
- 다양한 필터의 GLSL 코드를 Map으로 관리
- setFilter() 메서드를 통해 런타임에 GPU 프로그램을 교체함
```kotlin
  override fun setFilter(type: FilterType) {
        glSurfaceView.queueEvent {
            currentFilterType = type
        }
    }
```