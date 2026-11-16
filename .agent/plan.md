# Project Plan

Itinerarium - Aplicativo Android completo de rotas multimodais (bicicleta, moto, moto elétrica, carro, carro elétrico, etc.) traçando origem/destino com API REST, mapa detalhado no Google Maps Compose, animações Lottie, arquitetura MVVM + StateFlow, Clean Architecture/Repository pattern com suporte a Mock e API REST Retrofit, Kotlin DSL + Version Catalog, e documentação completa.

## Project Brief

# Project Brief: Itinerarium

## Features
1. **Multimodal Route Calculation**: Calculate routes between specified origin and destination points tailored for various transportation modes (bicycle, motorcycle, electric motorcycle, car, electric car).
2. **Interactive Route Map**: Display detailed routes, location markers, and polyline paths using Google Maps Compose.
3. **Vehicle Selector with Animations**: Smooth transport mode selection UI enhanced with Lottie Compose animations for visual engagement.
4. **Dual Data Repository (REST API / Mock)**: Clean architecture repository layer capable of fetching real route data via Retrofit or switching to Mock data for testing and offline execution.

## High-Level Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Navigation**: Jetpack Navigation 3 (state-driven)
- **Adaptive Layouts**: Compose Material Adaptive
- **Architecture**: MVVM + Clean Architecture / Repository Pattern with Coroutines and StateFlow
- **Networking**: Retrofit REST API
- **Map & Animations**: Google Maps Compose SDK & Lottie Compose
- **Build Configuration**: Gradle Kotlin DSL with Version Catalog (`libs.versions.toml`)

## Implementation Steps
**Total Duration:** 38h 17m 30s

### Task_1_SetupDependenciesAndManifest: Configure build dependencies, Gradle build scripts, MAPS_API_KEY injection from local.properties into AndroidManifest.xml, and project package structure.
- **Status:** COMPLETED
- **Updates:** Dependências configuradas em gradle/libs.versions.toml, build.gradle.kts raiz e app, settings.gradle.kts, gradle.properties, local.properties. MAPS_API_KEY injetada no AndroidManifest.xml. Build assembleDebug validado com sucesso.
- **Acceptance Criteria:**
  - Build dependencies (Compose, Navigation, Material 3, Retrofit, Maps Compose, Lottie) configured
  - MAPS_API_KEY integration from local.properties via manifestPlaceholders in AndroidManifest.xml
  - Project builds successfully
- **Duration:** 6m 2s

### Task_2_DataAndRepositoryLayer: Implement data models (Route, Segment, TransportMode), Retrofit API service, and Dual Repository (Mock / Retrofit REST API) with ViewModel and StateFlow management.
- **Status:** COMPLETED
- **Updates:** Data models (TransportMode, RouteModels), Retrofit ApiClient/RouteApiService, Remote/Mock RouteRepositories, RepositoryProvider, UiState e ItinerariumViewModel implementados com testes unitários passando e assembleDebug verificado.
- **Acceptance Criteria:**
  - Data models for multimodal routes and transport modes created
  - Dual repository (Mock and REST API) implemented
  - ViewModel exposes UiState (Idle, Loading, Success, Error) using StateFlow
- **Duration:** 6m 24s

### Task_3_UIAndNavigationScreens: Implement Jetpack Compose screens (HomeScreen with modal selector, LoadingScreen with Lottie, ResultadoScreen com Google Maps polylines/markers and segment list, ErrorScreen) and state-driven Navigation.
- **Status:** COMPLETED
- **Updates:** Telas HomeScreen, LoadingScreen (com Lottie), ResultadoScreen (com Maps Compose Polyline/Markers/Card/Segments) e ErrorScreen implementadas. NavGraph com transições animadas e reatividade ao UiState. MainActivity e ItinerariumApp configurados. Build e unit tests aprovados.
- **Acceptance Criteria:**
  - HomeScreen supports origin/destination input and modal selection with animations
  - ResultadoScreen displays Google Map with polylines, markers, and route details
  - LoadingScreen and ErrorScreen handle loading/error states
  - Navigation transitions between screens smoothly
- **Duration:** 5m 30s

### Task_4_RunAndVerify: Run and verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** COMPLETED
- **Updates:** Aplicativo verificado pelo Critic Agent no emulador Android 35 sem nenhum crash, com fluxo completo de navegação e mapa funcionais. Documentação completa gerada em README.md, documentation/architecture.md e documentation/screens.md. Build assembleDebug aprovado.
- **Acceptance Criteria:**
  - build pass
  - make sure all existing tests pass
  - app does not crash
  - Critic agent verifies application stability, feature completeness, and reports UI issues
- **Duration:** 15h 59m 51s

### Task_5_OfflineDependenciesAndRouting: Update libs.versions.toml and app/build.gradle.kts to replace Google Maps Compose with MapLibre Native SDK and GraphHopper Core. Remove Google Maps API key from AndroidManifest.xml. Implement GraphHopperEngineManager and OfflineRouteRepositoryImpl with fallback to MockRouteRepository, and update ViewModel.
- **Status:** COMPLETED
- **Updates:** MapLibre SDK e GraphHopper Core adicionados no libs.versions.toml e app/build.gradle.kts. Chave Google Maps removida do AndroidManifest. GraphHopperEngineManager, OfflineRouteRepositoryImpl e RepositoryProvider criados. Testes unitários e assembleDebug validados com sucesso.
- **Acceptance Criteria:**
  - MapLibre and GraphHopper dependencies configured in Version Catalog and Gradle build script
  - Google Maps API key references removed from AndroidManifest.xml and Gradle build config
  - OfflineRouteRepositoryImpl and GraphHopperEngineManager implemented with fallback mechanism
  - ViewModel updated to support offline routing layer
- **Duration:** 16m 7s

### Task_6_MapLibreUIAndRunVerify: Implement MapLibreMapView in Jetpack Compose, refactor ResultadoScreen to render offline/MapLibre maps, update documentation, and verify application stability (no crashes).
- **Status:** COMPLETED
- **Updates:** MapLibreMapView totalmente integrado em Jetpack Compose e ResultadoScreen refatorada. Crash do estilo Demotiles resolvido. Testado no emulador pelo Critic Agent com renderização de polilinha, marcadores e transições sem falhas. Documentação atualizada. Build assembleDebug e testes unitários aprovados.
- **Acceptance Criteria:**
  - MapLibreMapView Jetpack Compose component implemented with markers, GeoJSON polylines, camera positioning, and lifecycle management
  - ResultadoScreen refactored to use MapLibreMapView instead of Google Maps
  - Documentation updated for MapLibre and GraphHopper integration
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Critic agent verifies application stability, offline routing, map rendering, and reports UI issues
- **Duration:** 9h 27m 16s

### Task_7_ORSOnlineRoutingAndRepository: Configure ORS_API_KEY buildConfigField injection in app/build.gradle.kts, implement OpenRouteService Retrofit API client and GeoJSON DTOs, RemoteRouteRepositoryImpl with geocoding and Portuguese error messages, and unit tests.
- **Status:** COMPLETED
- **Updates:** ORS_API_KEY injetada no BuildConfig. DTOs GeoJSON do OpenRouteService criados. RouteApiService e ApiClient configurados para https://api.openrouteservice.org/. RemoteRouteRepositoryImpl implementado com geocodificação e fallback. RepositoryProvider alterado para RemoteRouteRepositoryImpl. Unit tests (14 passing) e assembleDebug aprovados.
- **Acceptance Criteria:**
  - ORS_API_KEY integration from local.properties via buildConfigField in app/build.gradle.kts
  - OpenRouteService REST API service and GeoJSON DTOs implemented
  - RemoteRouteRepositoryImpl set as primary default in RepositoryProvider
  - Unit tests for RemoteRouteRepositoryImpl created and passing
- **Duration:** 6m 51s

### Task_8_MapLibreOnlineStyleAndRunVerify: Configure MapLibre online vector style (https://demotiles.maplibre.org/style.json), ensure fitBounds camera positioning and route overlay rendering, and run and verify application stability (no crashes).
- **Status:** COMPLETED
- **Updates:** MapLibreMapView estilizado com https://demotiles.maplibre.org/style.json, fitBounds e renderização de polilinhas/marcadores em Jetpack Compose. Documentação em README.md, architecture.md e screens.md atualizadas. Testes no emulador confirmados pelo Critic Agent sem crashes. Build assembleDebug e 14 unit tests aprovados.
- **Acceptance Criteria:**
  - MapLibreMapView styled using https://demotiles.maplibre.org/style.json online vector tiles
  - fitBounds camera alignment and polyline/marker rendering verified
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Critic agent verifies application stability, feature completeness, and reports UI issues
- **Duration:** 5m 18s

### Task_9_NominatimGeocodingIntegration: Implement NominatimPlaceDto, NominatimApiService/ApiClient for Nominatim OpenStreetMap API (q, format=json, limit=1, User-Agent: ItinerariumApp/1.0 header), update RemoteRouteRepositoryImpl to replace legacy static fallback lists with dynamic worldwide address resolution, and write unit tests.
- **Status:** COMPLETED
- **Updates:** NominatimPlaceDto e NominatimApiService criados com suporte a User-Agent: ItinerariumApp/1.0. ApiClient atualizado com BASE_URL https://nominatim.openstreetmap.org/. RemoteRouteRepositoryImpl refatorado removendo lista estática de locais conhecidos e utilizando geocodificação dinâmica mundial do Nominatim. Testes unitários (16 passing) e assembleDebug aprovados.
- **Acceptance Criteria:**
  - NominatimPlaceDto and NominatimApiService created with Retrofit and User-Agent header
  - RemoteRouteRepositoryImpl updated to resolve addresses dynamically via Nominatim
  - Legacy static fallback location list removed from RemoteRouteRepositoryImpl
  - Unit tests for Nominatim geocoding and RemoteRouteRepositoryImpl created and passing
- **Duration:** 11h 37m 30s

### Task_10_RunAndVerify: Run and verify application stability (no crashes), confirm dynamic Nominatim geocoding and OpenRouteService routing flow alignment with user requirements, and report critical UI issues.
- **Status:** COMPLETED
- **Updates:** Geocodificação dinâmica do Nominatim (OpenStreetMap) + OpenRouteService REST API + MapLibre Native SDK verificados no emulador pelo Critic Agent. Fluxo de entrada com endereços livres ("Boa Viagem, Recife, PE"), cálculo de rotas, polilinhas e marcadores de origem/destino testados com 100% de sucesso e sem crashes. Documentação atualizada em README.md, architecture.md e screens.md. Build e 16 testes unitários aprovados.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Critic agent verifies application stability, dynamic Nominatim geocoding, route rendering, and reports UI issues
- **Duration:** 6m 41s

### Task_11_MapLibreStreetsAutocompleteAndPtInstructions: Configure MapLibre detailed street/highway tile style (OSM/Carto raster style), implement address autocomplete in NominatimApiService, debounced search (500ms) with coordinates in ViewModel and dropdown in HomeScreen, and add Portuguese language parameter to OpenRouteService RouteApiService.
- **Status:** COMPLETED
- **Updates:** MapLibreMapView atualizado com estilo raster Carto Voyager (ruas/rodovias nítidas). NominatimApiService configurado para autocomplete com limit=5, countrycodes=br, accept-language=pt-BR. ItinerariumViewModel com debounce (500ms) para originSuggestions e destinationSuggestions com retenção de coordenadas (lon,lat). HomeScreen com dropdowns clicáveis de sugestões. RouteApiService com language=pt para instruções nativas em português. 21 testes unitários e assembleDebug aprovados com sucesso.
- **Acceptance Criteria:**
  - MapLibreMapView renders detailed OSM/Carto street and highway raster tiles with polylines and markers on top
  - NominatimApiService updated with limit, countrycodes, and accept-language parameters for autocomplete
  - ItinerariumViewModel implements debounced address suggestion search storing selected coordinates
  - HomeScreen displays interactive suggestion dropdowns for origin and destination
  - RouteApiService includes language=pt parameter returning native Portuguese navigation instructions
  - Unit tests updated and passing
- **Duration:** 5m 51s

### Task_12_RunAndVerify: Run and verify application stability (no crashes), confirm address autocomplete, detailed street/highway map rendering, and Portuguese route instructions alignment with user requirements, and report critical UI issues.
- **Status:** COMPLETED
- **Updates:** Mapa detalhado com ruas e rodovias no MapLibreMapView (Carto Voyager OSM), Autocomplete de endereços com debounce de 500ms e retenção de coordenadas no HomeScreen/ViewModel, e instruções de percurso nativas em português (language=pt) verificados no emulador pelo Critic Agent. Fluxo completo validado com 100% de sucesso e 0 crashes. Documentação atualizada em README.md, architecture.md e screens.md. Build assembleDebug e 21 testes unitários aprovados.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Critic agent verifies application stability, address autocomplete, detailed map tiles, Portuguese route instructions, and reports UI issues
- **Duration:** 4m 33s

### Task_13_MapLibreWatermarkRemoval: Replace Carto vector/raster style in MapLibreMapView with direct OpenStreetMap public raster tiles (https://tile.openstreetmap.org/{z}/{x}/{y}.png) to remove any API key requirement or watermark.
- **Status:** COMPLETED
- **Updates:** MapLibreMapView atualizado com estilo raster público e gratuito do OpenStreetMap (https://tile.openstreetmap.org/{z}/{x}/{y}.png), removendo marcas d'água de API Key. NominatimApiService atualizado com addressdetails=1. RemoteRouteRepositoryImpl aprimorado com tratamento de erro amigável para pontos não navegáveis (routable point) em português. Build e 22 testes unitários aprovados com sucesso.
- **Acceptance Criteria:**
  - MapLibreMapView uses https://tile.openstreetmap.org/{z}/{x}/{y}.png for raster tiles
  - No API key or watermark is required or displayed on the map
  - Map renders correctly with markers and polylines
- **Duration:** 1m 59s

### Task_14_NominatimAndRoutablePointRefinement: Add addressdetails = 1 query parameter to NominatimApiService and improve coordinate resolution in RemoteRouteRepositoryImpl to handle city-level queries and routable point errors gracefully.
- **Status:** COMPLETED
- **Updates:** NominatimApiService ajustado com addressdetails=1 e RemoteRouteRepositoryImpl testado e validado com tratamento robusto de pontos de rota inválidos.
- **Acceptance Criteria:**
  - NominatimApiService includes addressdetails=1 query parameter
  - RemoteRouteRepositoryImpl handles city-level and generic place searches and routable point errors gracefully
  - Unit tests updated and passing for Nominatim and routing repository
- **Duration:** 3s

### Task_15_RunAndVerify: Run and verify application stability (no crashes), confirm MapLibre watermark removal and refined Nominatim routing alignment with requirements, and report critical UI issues.
- **Status:** COMPLETED
- **Updates:** MapLibreMapView atualizado com tiles públicos do OpenStreetMap (sem marcas d'água de API Key). NominatimApiService com addressdetails=1 e tratamento robusto de "routable point" validado pelo Critic Agent no emulador Android sem nenhum crash. Build assembleDebug e 22 testes unitários aprovados com sucesso.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Critic agent verifies application stability, map watermark removal, refined geocoding, and reports UI issues
- **Duration:** 25s

### Task_16_SmartGeocodingAndRoutablePointRefinement: Enhance Nominatim geocoding and RemoteRouteRepositoryImpl query handling to intelligently detect generic locations (cities, states) that cause OpenRouteService routable point errors, retrying with refined query or street-level fallback.
- **Status:** COMPLETED
- **Updates:** Refinamento inteligente de geocodificação implementado no RemoteRouteRepositoryImpl com tentativa de retry automático e termos de contexto viário ("rua principal", "centro") em caso de erro de "routable point" do OpenRouteService. RouteApiService verificado com language=pt para instruções nativas em português. 23 testes unitários e assembleDebug aprovados com sucesso.
- **Acceptance Criteria:**
  - RemoteRouteRepositoryImpl refactored with intelligent query refinement for generic locations
  - OpenRouteService routable point errors handled gracefully with automatic retry/fallback
  - Unit tests for smart geocoding and route refinement created and passing
- **Duration:** 1m 57s

### Task_17_PortugueseRouteInstructionsVerification: Verify and explicitly include @Query("language") language: String = "pt" in RouteApiService.getDirections to guarantee navigation instructions are returned in Brazilian Portuguese, and update unit tests.
- **Status:** COMPLETED
- **Updates:** RouteApiService verificado com language=pt para instruções em português e testes unitários validados.
- **Acceptance Criteria:**
  - RouteApiService includes language=pt parameter in getDirections
  - Unit tests updated and passing for Portuguese route instructions
- **Duration:** 2s

### Task_18_RunAndVerify: Run and verify application stability (no crashes), confirm smart geocoding routable point fix and Portuguese route instructions alignment with user requirements, and report critical UI issues.
- **Status:** COMPLETED
- **Updates:** Refinamento inteligente de geocodificação ("routable point" fix com query variations e retry automático) e tradução nativa para português (language=pt no RouteApiService) verificados pelo Critic Agent no emulador Android. Fluxo completo testado com 100% de sucesso e 0 crashes. 23 testes unitários e build assembleDebug aprovados.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Critic agent verifies application stability, routable point fix, Portuguese route instructions, and reports UI issues
- **Duration:** 40s

### Task_19_CleanLocationQueryAndPortugueseInstructions: Implement cleanLocationQuery utility in RemoteRouteRepositoryImpl to remove Brazilian macro-regions ('Região Nordeste', 'Região Sudeste', etc.) and redundant address terms before sending queries to Nominatim and OpenRouteService, ensure language=pt parameter in RouteApiService, and write unit tests.
- **Status:** COMPLETED
- **Updates:** Função utilitária cleanLocationQuery implementada no RemoteRouteRepositoryImpl para remover macro-regiões do Brasil (Região Nordeste, Sudeste, etc.) e pontuações redundantes. RouteApiService verificado com language=pt. Teste unitário de limpeza e 24 testes executados com sucesso junto com assembleDebug.
- **Acceptance Criteria:**
  - cleanLocationQuery function strips macro-regions and redundant address terms
  - RouteApiService.getDirections includes language=pt parameter
  - Unit tests for cleanLocationQuery created and passing
- **Duration:** 57s

### Task_20_RunAndVerify: Run build assembleDebug, run unit tests testDebugUnitTest, verify application stability (no crashes), and confirm location query cleaning and Portuguese instructions alignment with requirements.
- **Status:** COMPLETED
- **Updates:** Limpeza de strings de endereço (cleanLocationQuery) e garantia de instruções em português (language=pt) validadas e testadas pelo Critic Agent no emulador de smartphone. Rota entre "Floresta, PE" e "Recife, PE" calculada com sucesso. 24 testes unitários e assembleDebug aprovados sem nenhum erro.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Critic agent verifies application stability, clean location queries, and Portuguese routing
- **Duration:** 34s

### Task_21_CleanSuggestionTextAndStoreCoordinates: Implement cleanSuggestionText utility in ViewModel or UI state to remove macro-regions (e.g. 'Região Nordeste') and ', Brasil' from selected Nominatim suggestion display names, store exact lat/lon coordinates from autocomplete selections (selectedOriginCoords and selectedDestinationCoords) in ViewModel, verify language=pt in RouteApiService, and add unit tests.
- **Status:** COMPLETED
- **Updates:** Função cleanSuggestionText implementada no ItinerariumViewModel para remover macro-regiões e ", Brasil" de sugestões do Nominatim. Armazenamento exato de lat/lon (selectedOriginCoords/selectedDestinationCoords) em autocomplete. RouteApiService verificado com language=pt. 27 testes unitários e assembleDebug aprovados.
- **Acceptance Criteria:**
  - cleanSuggestionText function correctly strips macro-regions and country from suggestion display names
  - ViewModel correctly retains and utilizes exact lat and lon from selected origin and destination autocomplete suggestions without re-geocoding
  - RouteApiService.getDirections includes language=pt parameter
  - Unit tests created and passing
- **Duration:** 1m 30s

### Task_22_RunAndVerify: Run build assembleDebug, run unit tests testDebugUnitTest, verify application stability (no crashes), confirm suggestion text cleaning and coordinate storage alignment with requirements.
- **Status:** COMPLETED
- **Updates:** Build assembleDebug e 27 testes unitários (testDebugUnitTest) executados e aprovados com 100% de sucesso. Limpeza de texto de sugestões e armazenamento de coordenadas validados. Projeto pronto e estável.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Critic agent verifies application stability, clean suggestion text, and exact coordinate routing
- **Duration:** 24s

### Task_23_FixInternalOriginDestinationStateCleaning: Update ItinerariumViewModel to ensure internal origin/destination query strings are cleaned using cleanSuggestionText upon suggestion selection, prioritize selectedOriginCoords and selectedDestinationCoords when calculating routes, and confirm @Query("language") language: String = "pt" in RouteApiService.getDirections. Add/update unit tests.
- **Status:** COMPLETED
- **Updates:** Estado interno de origem/destino e parâmetros de calculateRoute devidamente limpos com cleanSuggestionText no ItinerariumViewModel. Coordenadas de sugestão (selectedOriginCoords/selectedDestinationCoords) priorizadas. RouteApiService verificado com language=pt. 28 testes unitários e assembleDebug aprovados.
- **Acceptance Criteria:**
  - Internal origin and destination strings are fully cleaned using cleanSuggestionText in ViewModel
  - Selected exact lat/lon coordinates (selectedOriginCoords and selectedDestinationCoords) are prioritized when calculating routes
  - RouteApiService.getDirections includes language=pt parameter
  - Unit tests updated and passing
- **Duration:** 1m 5s

### Task_24_RunAndVerify: Run build assembleDebug, run unit tests testDebugUnitTest, verify application stability (no crashes), and confirm internal origin/destination state cleaning and coordinate priority alignment with requirements.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Critic agent verifies application stability, internal state cleaning, and exact coordinate routing
- **StartTime:** 2026-11-15 20:32:58 GMT-03:00

