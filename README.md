# skjo.retrica
## 조성광: [레트리카] 확장 가능한 카메라 필터 엔진 설계

### TODO LIST
- [x] App Permission logic 
- [ ] CameraX 
  - [x] Camera Preview UI
  - [x] Filter 적용 된 UI
  - [ ] Filter 선택/변경/해제 UI
  - [x] OpenGL ES를 사용하여 간단한 Grayscale 셰이더 작성
- [ ] 프레임 드랍 모니터링
  - [ ] 실시간 모니터링 
  - [ ] log 로 추출(?)
- [ ] lifeCycle 최적화
  - [ ] Camera HW
  - [ ] 그외 (view, data 등)
- [ ] 문서화 
  - [ ] markdown (컨플루언스?) 
  - [ ] PDF
  - [ ] 프레젠테이션
---
### Link
- [git-hub repository](https://github.com/Anthony-Jo/skjo.retrica)
- [Confluence](https://zamake.atlassian.net/wiki/x/BQCw)
- [PDF download]()
---
### App Preview
- 앱 언어 (Language): `Kotlin`
- 최소 SDK (Min SDK): API 24 (Android 7.0 Nougat)
- 타겟 SDK (Target SDK): API 34 (Android 14)
- 디자인 패턴 (Architecture):
  - MVVM (Model-View-ViewModel)
  - Repository/UseCase 패턴을 적용하여 데이터 및 비즈니스 로직 분리
  - Hilt를 사용한 DI (Dependency Injection)
- 주요 사용 라이브러리 (Key Libraries):
  - Jetpack CameraX: 카메라 구현의 복잡성을 줄이고 안정성을 높이기 위해 사용. Preview, ImageAnalysis UseCase 활용.
  - Kotlin Coroutines: 이미지 처리와 같은 비동기 작업을 효율적으로 관리하고 UI 스레드의 부하를 줄이기 위해 사용.
  - Hilt: 의존성 주입을 통해 모듈화된 코드를 작성하고, 테스트 용이성 및 확장성을 확보. 싱글톤 관리를 위해 사용.
  - Android Jetpack: ViewModel, Lifecycle, Activity-KTX 등 AAC(Android Architecture Components)를 적극 활용하여 생명주기를 안전하게 관리.
  - ViewBinding: Null-safe 방식으로 View에 접근하여 런타임 에러 방지.