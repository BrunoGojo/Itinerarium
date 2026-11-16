# Itinerarium - Navegação Multimodal Sustentável (100% Online)

**Itinerarium** é um aplicativo Android nativo desenvolvido em **Kotlin** e **Jetpack Compose**, projetado para planejar, comparar e visualizar rotas multimodais de transporte urbano com foco em mobilidade verde, eficiência e sustentabilidade.

O aplicativo opera de forma **100% ONLINE**, consumindo diretamente a REST API do **OpenRouteService (ORS)** via **Retrofit 2** para cálculo de direções geográficas, utilizando a API do **Nominatim (OpenStreetMap)** para geocodificação dinâmica de endereços em qualquer lugar do mundo, e empregando o **MapLibre Native Android SDK** com tiles vetoriais remotos para renderização interativa do percurso.

O Itinerarium calcula e compara simultaneamente múltiplos modais de transporte (carro a combustão, carro elétrico, moto, moto elétrica, bicicleta, e-bike e caminhada), apresentando métricas essenciais como tempo de viagem, custo estimado e emissões de CO₂ (pegada de carbono).

---

## 📋 Requisitos do Sistema

Para compilar, executar e desenvolver o projeto **Itinerarium**, certifique-se de que seu ambiente atenda aos seguintes pré-requisitos:

- **Android Studio**: Jellyfish (2023.3.1) / Ladybug / Koala (2024.1+) ou superior.
- **Java Development Kit (JDK)**: JDK 17 (configurado como o Gradle JDK no Android Studio).
- **Android SDK Target / Compile**: API Level 35 (Android 15).
- **Android SDK Minimum**: API Level 26 (Android 8.0 Oreo) ou superior.
- **Conexão com a Internet**: Necessária para consumo da API REST do OpenRouteService, busca de sugestões no Nominatim e download de tiles do mapa.
- **Chave de API OpenRouteService (`ORS_API_KEY`)**: Chave válida configurada no arquivo `local.properties`.
- **Renderização de Mapa Detalhado**: Utiliza MapLibre Android SDK com tiles raster/vetoriais Carto Voyager (OpenStreetMap) (`https://basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png`), oferecendo visualização rica e detalhada de ruas, avenidas e rodovias.

---

## 🛠️ Passo a Passo de Configuração e Execução

### 1. Clonar o Repositório
Abra o terminal e clone o repositório para o seu ambiente local:
```bash
git clone https://github.com/seu-usuario/itinerarium.git
cd itinerarium
```

### 2. Configurar a Chave de API do OpenRouteService (`ORS_API_KEY`)
O aplicativo utiliza o serviço **OpenRouteService (ORS)** para cálculo de rotas online e geocodificação de locais.

1. Acesse o portal oficial [OpenRouteService Developer](https://openrouteservice.org/dev/#/signup) e crie uma conta gratuita.
2. Gere um token/chave de API (API Key) para o tipo de serviço *Free*.
3. Na raiz do projeto, crie ou edite o arquivo `local.properties` e adicione a seguinte linha:
   ```properties
   ORS_API_KEY=SUA_CHAVE_ORS_AQUI
   ```

> **Nota de Arquitetura**: O arquivo `app/build.gradle.kts` lê dinamicamente a propriedade `ORS_API_KEY` de `local.properties` e a injeta como campo de compilação tipo-seguro `BuildConfig.ORS_API_KEY`, garantindo que chaves privadas não sejam commitadas no controle de versão.

### 3. Modo de Operação Remoto (100% Online) vs Offline (Mock)
Por padrão, o aplicativo está configurado para operar em **modo 100% ONLINE** (`RepositoryProvider.USE_MOCK = false`), enviando requisições REST via Retrofit ao backend do OpenRouteService (`https://api.openrouteservice.org/`).

Se desejar alterar a fonte de dados:
1. Abra o arquivo `com.itinerarium.data.repository.RepositoryProvider.kt`.
2. O parâmetro `USE_MOCK` define qual repositório será injetado na aplicação:
   ```kotlin
   object RepositoryProvider {
       var USE_MOCK: Boolean = false // false = Modo 100% Online (RemoteRouteRepositoryImpl via ORS REST API)
       ...
   }
   ```
3. No modo online (`USE_MOCK = false`), a aplicação:
   - Apresenta **autocomplete inteligente de endereços** com debounce de 500ms durante a digitação na `HomeScreen`, utilizando `NominatimApiService` com `countrycodes=br` e `accept-language=pt-BR`. Ao selecionar uma sugestão, as coordenadas exatas (`"lon,lat"`) são armazenadas no ViewModel (`selectedOriginCoords`, `selectedDestinationCoords`), eliminando geocodificações redundantes.
   - Resolve endereços não pré-selecionados por meio de geocodificação dinâmica via `RouteApiService.searchGeocode` (ORS) com fallback automático para a API do **Nominatim (OpenStreetMap)** (`https://nominatim.openstreetmap.org/search`).
   - Realiza requisições HTTP GET ao endpoint `/v2/directions/{profile}` do ORS com o parâmetro `language=pt`, obtendo a geometria GeoJSON da rota e as **instruções de navegação nativas em português**.
   - O `MapLibreMapView` carrega o estilo **Carto Voyager (OpenStreetMap)** (`https://basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png`), renderizando um mapa rico em detalhes com ruas, avenidas e rodovias, desenhando a polilinha destacada e enquadrando suavemente o percurso com `fitBounds`.

---

## 🔍 Autocomplete de Endereços & Retenção de Coordenadas

O **Itinerarium** implementa um sistema reativo de autocomplete de endereços e locais na `HomeScreen`:

- **Debounce de 500ms**: Evita requisições excessivas enquanto o usuário digita. A busca é acionada somente após 500ms de pausa na digitação para consultas com 3 ou mais caracteres (`onOriginQueryChanged` e `onDestinationQueryChanged`).
- **Filtro Nacional e Idioma**: Consulta o Nominatim API utilizando `countrycodes=br` (priorizando locais no Brasil) e `accept-language=pt-BR` (retornando nomes formatados em português).
- **Cards de Sugestões Reativos**: As sugestões são emitidas via `StateFlow` (`originSuggestions` e `destinationSuggestions`) e exibidas em cards flutuantes abaixo dos campos de entrada.
- **Retenção de Coordenadas no ViewModel**: Ao clicar em uma sugestão, o local exato em coordenadas `(longitude, latitude)` é retido em `selectedOriginCoords` ou `selectedDestinationCoords` no `ItinerariumViewModel`. No momento do cálculo da rota, essas coordenadas são repassadas diretamente ao repositório, garantindo máxima precisão e agilidade.

---

## 🗺️ Estilo de Mapa Detalhado Carto Voyager (OSM) e Instruções Nativas em Português (`language=pt`)

- **Estilo de Mapa Rico (Carto Voyager)**: A renderização do mapa via `MapLibreMapView` utiliza tiles de alta resolução do Carto Voyager derivados de dados OpenStreetMap (`https://basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png`). O mapa exibe detalhes minuciosos de arruamento urbano, avenidas, rodovias, ciclovias e pontos de interesse.
- **Instruções Nativas em Português (`language=pt`)**: A API de direções do OpenRouteService é invocada com o parâmetro `language=pt`. Com isso, os trechos e etapas da rota (*steps/segments*) trazem manobras e orientações escritas nativamente em português (ex.: *"Vire à esquerda na Rua da Consolação"*, *"Siga em frente na Avenida Paulista"*), que são exibidas de forma clara e legível no painel expansível da `ResultadoScreen`.

---

## 🌍 Geocodificação Dinâmica Mundial via Nominatim (OpenStreetMap)

O **Itinerarium** possui suporte completo a busca e resolução de qualquer endereço no mundo (cidades, ruas, pontos turísticos ou coordenadas brutas) utilizando a geocodificação dinâmica via **Nominatim (OpenStreetMap) API**.

### Estrutura de Resolução de Coordenadas
O processo de conversão de texto de origem/destino em coordenadas geográficas `(longitude, latitude)` no `RemoteRouteRepositoryImpl` opera em 3 camadas de resiliência:

1. **Leitura de Coordenadas Brutas**: Interpreta entradas diretas no formato `latitude, longitude` ou `longitude, latitude`.
2. **Geocodificação Primária ORS**: Consulta o endpoint `/geocode/search` da API OpenRouteService.
3. **Fallback Dinâmico Mundial Nominatim (OSM)**: Caso a consulta primária não retorne coordenadas válidas, a aplicação dispara uma requisição HTTP GET para a API REST do Nominatim OpenStreetMap:
   - **URL Base / Endpoint**: `https://nominatim.openstreetmap.org/search`
   - **Parâmetros de Consulta**: `q={query}&format=json&limit=1&countrycodes=br&accept-language=pt-BR`
   - **Cabeçalho HTTP Obrigatório**: `User-Agent: ItinerariumApp/1.0` (atendendo às políticas formais de uso da infraestrutura OpenStreetMap para identificação da aplicação).
   - **DTO de Resposta**: Mapeamento seguro via `NominatimPlaceDto` (`lat`, `lon`, `display_name`, `place_id`).

### 4. Compilação e Testes Unitários via Terminal

Para verificar a compilação completa e 100% limpa do projeto:
```bash
./gradlew assembleDebug
```

Para executar e validar a suíte de testes unitários da aplicação (`ItinerariumViewModelTest`, `RemoteRouteRepositoryTest`, `OfflineRouteRepositoryTest`):
```bash
./gradlew testDebugUnitTest
```

Para instalar e executar o aplicativo em um emulador ou dispositivo conectado via USB:
```bash
./gradlew installDebug
```

---

## 📚 Bibliotecas e Versões Utilizadas

A tabela abaixo detalha as principais dependências, bibliotecas e plugins configurados no catálogo de versões (`gradle/libs.versions.toml`):

| Categoria / Biblioteca | Dependência Gradle | Versão | Descrição |
| :--- | :--- | :---: | :--- |
| **Android Gradle Plugin** | `com.android.application` | `8.5.2` | Plugin de compilação do Android. |
| **Kotlin** | `org.jetbrains.kotlin.android` | `2.0.21` | Linguagem de programação e suporte a Compose Compiler. |
| **AndroidX Core KTX** | `androidx.core:core-ktx` | `1.13.1` | Extensões Kotlin para APIs do Android Framework. |
| **Compose BOM** | `androidx.compose:compose-bom` | `2024.12.01` | Bill of Materials para versionamento unificado do Compose. |
| **Compose Material3** | `androidx.compose.material3:material3` | BOM | Componentes de interface no padrão Material Design 3. |
| **Material Icons Extended** | `androidx.compose.material:material-icons-extended` | BOM | Ícones vetoriais estendidos para os modais de transporte. |
| **Activity Compose** | `androidx.activity:activity-compose` | `1.9.3` | Integração do Compose com o ciclo de vida das Activities. |
| **Navigation Compose** | `androidx.navigation:navigation-compose` | `2.8.5` | Navegação declarativa por rotas e grafos no Compose. |
| **Lifecycle & ViewModel** | `androidx.lifecycle:lifecycle-viewmodel-compose` | `2.8.7` | Gerenciamento de estado acoplado ao ciclo de vida e ViewModel. |
| **MapLibre SDK** | `org.maplibre.gl:android-sdk` | `11.5.1` | Renderização de mapas interativos via estilo vetorial JSON MapLibre. |
| **Retrofit 2** | `com.squareup.retrofit2:retrofit` | `2.11.0` | Cliente HTTP tipo-seguro para consumo de APIs REST. |
| **Gson & Converter** | `com.squareup.retrofit2:converter-gson` | `2.11.0` | Serialização e desserialização de DTOs GeoJSON do OpenRouteService. |
| **OkHttp Logging Interceptor**| `com.squareup.okhttp3:logging-interceptor` | `4.12.0` | Interceptor de logs de requisições/respostas HTTP em Debug. |
| **Kotlinx Coroutines** | `org.jetbrains.kotlinx:kotlinx-coroutines-android` | `1.7.3` | Programação assíncrona com Corrotinas e Flow. |
| **Lottie Compose** | `com.airbnb.android:lottie-compose` | `6.4.0` | Exibição de animações vetoriais interativas em JSON. |
| **Google Material** | `com.google.android.material:material` | `1.12.0` | Componentes e temas clássicos do Material Design. |
| **JUnit 4** | `junit:junit` | `4.13.2` | Framework de testes unitários para ViewModel e Repositórios. |
| **AndroidX JUnit Test** | `androidx.test.ext:junit` | `1.2.1` | Runner de testes instrumentados para Android. |
| **Espresso Core** | `androidx.test.espresso:espresso-core` | `3.6.1` | Framework de testes de UI instrumentados. |

---

## 📁 Documentação Adicional

A documentação detalhada da arquitetura técnica e das telas do projeto está disponível nos seguintes arquivos:

- [Arquitetura do Sistema (`documentation/architecture.md`)](documentation/architecture.md): Detalhamento do padrão MVVM + Repository, diagrama em Mermaid com `RemoteRouteRepositoryImpl`, `RouteApiService`, `NominatimApiService`, DTOs GeoJSON e Nominatim (`NominatimPlaceDto`) e `MapLibreMapView`.
- [Especificação de Telas (`documentation/screens.md`)](documentation/screens.md): Descrição detalhada do layout, interações, suporte a busca global em qualquer endereço no mundo na `HomeScreen`, `LoadingScreen`, `ResultadoScreen` (com MapLibreMapView) e `ErrorScreen`.
