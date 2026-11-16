# Especificação das Telas do Aplicativo - Itinerarium

O **Itinerarium** possui uma interface nativa desenvolvida inteiramente com **Jetpack Compose** e **Material Design 3**, projetada para oferecer uma experiência de navegação rápida, acessível e focada na mobilidade urbana sustentável.

O aplicativo integra-se 100% ONLINE com a API do **OpenRouteService (ORS)** para cálculo de direções e a API do **Nominatim (OpenStreetMap)** para geocodificação mundial via **Retrofit 2**, exibindo os trajetos geograficamente no **MapLibre Native Android SDK** alimentado pelo estilo vetorial remoto `https://demotiles.maplibre.org/style.json`.

Este documento descreve detalhadamente cada uma das telas da aplicação: **HomeScreen**, **LoadingScreen**, **ResultadoScreen** e **ErrorScreen**.

---

## 📱 1. HomeScreen (Tela Principal e Planejamento)

### 🎯 Propósito
A `HomeScreen` é o ponto de entrada da aplicação. Ela permite que o usuário informe qualquer localização ou endereço no mundo para os pontos de origem e destino (ex.: cidades, bairros, ruas, estabelecimentos comerciais, pontos turísticos internacionais ou coordenadas brutas `latitude, longitude`), respaldado pela geocodificação dinâmica mundial via **Nominatim (OpenStreetMap)** e **OpenRouteService**, além de selecionar um modal de transporte preferencial ou optar pelo cálculo multimodal comparativo.

```
+-------------------------------------------------------+
|  TopBar: Itinerarium (Navegação Multimodal)          |
+-------------------------------------------------------+
|  [Card Eco: Escolha o melhor caminho!]                |
|  Compare tempo, custo e pegada de carbono             |
+-------------------------------------------------------+
|  [Card Formulário com Autocomplete & Sugestões]       |
|  * Ponto de Origem: [ Busca com Debounce 500ms ]      |
|    +-----------------------------------------------+  |
|    | 📍 Recife, PE, Brasil                         |  |
|    | 📍 Rua do Hospício, Recife - PE               |  |
|    +-----------------------------------------------+  |
|            [ ⇅ Botão Inverter ]                      |
|  * Ponto de Destino: [ Busca com Debounce 500ms ]     |
|    +-----------------------------------------------+  |
|    | 📍 Olinda, PE, Brasil                         |  |
|    +-----------------------------------------------+  |
+-------------------------------------------------------+
|  Modal de Transporte                                  |
|  [Todos] [Carro] [Carro Elétrico (ECO)] [Bike (ECO)]  |
+-------------------------------------------------------+
|  [ BOTÃO: Traçar Rota Multimodal ]                    |
+-------------------------------------------------------+
```

### 🧩 Componentes e Layout
- **TopAppBar**: Exibe a marca do aplicativo ("Itinerarium") e o subtítulo "Navegação Multimodal Sustentável" em destaque com cores do tema primário.
- **Banner Introdutório Sustentável**: Card com fundo em tom de verde ecológico (`EcoGreenContainer`) contendo ícone de folha (`Icons.Default.Eco`) destacando o foco em sustentabilidade e pegada de carbono.
- **Card de Formulário de Entrada com Autocomplete de Endereços**:
  - `OutlinedTextField` de **Origem**: Campo de texto flexível com ícone de marcador local (`Place`), botão de limpeza rápida (`Clear`) e suporte a busca com **debounce de 500ms**. Dispara `onOriginQueryChanged` e consulta o Nominatim API com `countrycodes=br` e `accept-language=pt-BR`.
  - **Cards de Sugestões Reativos (Origem)**: Exibidos dinamicamente (`AnimatedVisibility`) logo abaixo do campo quando `originSuggestions` contém resultados. Cada linha apresenta um ícone de local e o nome completo formatado (`displayName`). Ao clicar em uma sugestão, o texto do campo é atualizado, as coordenadas exatas (`"lon,lat"`) são armazenadas no ViewModel (`selectedOriginCoords`) e a lista é ocultada.
  - **Botão de Inversão (`SwapVert`)**: Botão circular centralizado que troca instantaneamente os valores dos campos de Origem e Destino e notifica a busca de autocomplete.
  - `OutlinedTextField` de **Destino**: Campo de texto flexível com suporte a autocomplete com debounce de 500ms.
  - **Cards de Sugestões Reativos (Destino)**: Exibidos dinamicamente quando `destinationSuggestions` contém resultados. Armazena as coordenadas em `selectedDestinationCoords` ao ser selecionado.
- **Seletor de Modais de Transporte**:
  - Implementado utilizando `FlowRow` responsivo que se adapta à largura da tela.
  - Exibe chips customizados (`ModeChip`) para todas as opções do enum `TransportMode` (Carro, Carro Elétrico, Moto, Moto Elétrica, Bicicleta, Bicicleta Elétrica e Caminhada).
  - Modais ecológicos recebem uma tag distintiva **"ECO"** verde e borda diferenciada quando selecionados.
  - Opção "Todos (Multimodal)" ativada por padrão quando nenhum modal específico é filtrado.
- **Botão Ação Principal ("Traçar Rota Multimodal")**: Botão elevado de altura 56dp com ícone de rota (`Icons.Outlined.Route`) que valida as entradas e inicia o cálculo de rotas.

### 🧪 Validações e Regras de Negócio
- Se o usuário tentar submeter o formulário com a **Origem** ou **Destino** vazios, o campo correspondente entra em estado de erro (`isError = true`) e exibe uma mensagem de ajuda ("A origem é obrigatória" / "O destino é obrigatório").
- Se a **Origem** e o **Destino** forem idênticos (ignorando maiúsculas/minúsculas e espaços em branco nas extremidades), uma mensagem de erro é exibida informando que os locais devem ser diferentes.
- **Resolução de Coordenadas Globais**: O formulário suporta qualquer endereço no mundo. O `RemoteRouteRepositoryImpl` resolve a pesquisa em 3 etapas: parsing de coordenadas numéricas, geocodificação no ORS (`/geocode/search`) e fallback dinâmico mundial via **Nominatim API (OpenStreetMap)** em `https://nominatim.openstreetmap.org/search` enviando o cabeçalho obrigatório `User-Agent: ItinerariumApp/1.0`. Se nenhum resultado for encontrado em ambos os serviços, a aplicação redireciona amigavelmente para a `ErrorScreen`.

### ⚡ Interações e Comportamentos
- Ao preencher o formulário e tocar em **"Traçar Rota Multimodal"**, o foco do teclado é removido e a função `calculateRoute` é disparada na ViewModel, alterando o estado da UI para `UiState.Loading` e acionando o repositório remoto `RemoteRouteRepositoryImpl`.

---

## ⏳ 2. LoadingScreen (Tela de Processamento)

### 🎯 Propósito
A `LoadingScreen` fornece um feedback visual agradável e dinâmico enquanto as requisições assíncronas HTTP REST estão sendo processadas pelo OpenRouteService (`/geocode/search` para geocodificação e `/v2/directions/{profile}` para direções e polilinhas GeoJSON).

```
+-------------------------------------------------------+
|                                                       |
|                     (  ( 🚴 )  )                      |
|             Animação Lottie / Pulsação                |
|                                                       |
|        Buscando as melhores rotas multimodais...      |
|  Avaliando tempo, emissões de CO₂ e custos...        |
|                                                       |
+-------------------------------------------------------+
```

### 🧩 Componentes e Layout
- **Animação Lottie**: Carrega a animação vetorial localizada em `res/raw/loading.json` (`LottieCompositionSpec.RawRes`) em um ciclo contínuo sem fim.
- **Fallback Gracioso para CircularProgressIndicator**: Caso o arquivo Lottie não esteja disponível ou ocorra falha na decodificação, a tela exibe automaticamente um `CircularProgressIndicator` do Material3 na cor primária.
- **Background Animado de Pulsação**: Círculo translúcido com animação de escala (`rememberInfiniteTransition`) que varia suavemente entre 95% e 105% do tamanho, transmitindo uma sensação de processamento ativo.
- **Textos Informativos**:
  - Título: *"Buscando as melhores rotas multimodais..."* em negrito.
  - Subtítulo: *"Avaliando tempo, emissões de CO₂ e custos de transporte..."* em tom secundário.

### ⚡ Interações e Comportamentos
- Esta tela é reativa e observada via `UiState`.
- Assim que o repositório remoto retorna o resultado da API do OpenRouteService, a ViewModel emite `UiState.Success` (redirecionando para a `ResultadoScreen`) ou `UiState.Error` (redirecionando para a `ErrorScreen`).

---

## 🗺️ 3. ResultadoScreen (Visualização no Mapa e Detalhes)

### 🎯 Propósito
A `ResultadoScreen` é a tela de exibição analítica. Ela combina a renderização geográfica da rota obtida da API do OpenRouteService com um painel de comparação de métricas ecológicas e financeiras entre as alternativas de transporte.

```
+-------------------------------------------------------+
|  TopBar: ←  Praça da Sé → Av. Paulista               |
+-------------------------------------------------------+
| [Chip Bike 16m]  [Chip Carro Elétrico 11m]  [Chip...] | <- Overlay Superior
|=======================================================|
|                                                       |
|             [ MAPA MAPLIBRE DETALHADO ]               |
|      Style: Carto Voyager (OpenStreetMap Tiles)       |
|     (Renderização rica de ruas, avenidas e rodovias)   |
|                ~ Polilinha da Rota ~                  |
|            (📍 Origem) ----> (📍 Destino)             |
|                                                       |
|=======================================================|
|  [CARD FLUTUANTE INFERIOR EXPANSÍVEL]                 |
|  ⚡ Carro Elétrico (ECO)               [▼ Recolher]   |
|  ---------------------------------------------------  |
|  Duração: 11 min | Distância: 4.8 km                  |
|  Custo: R$ 1,20  | CO₂: 0,07 kg                      |
|  ---------------------------------------------------  |
|  Etapas do Percurso (Instruções em Português):       |
|  • Siga em direção à R. da Consolação (2.3 km)        |
|  • Continue na Av. Paulista (2.5 km)                  |
+-------------------------------------------------------+
```

### 🧩 Componentes e Layout
1. **TopAppBar**:
   - Exibe o resumo do trajeto (`Origem → Destino`) e a quantidade de rotas alternativas encontradas.
   - Contém o botão de voltar (`Icons.AutoMirrored.Filled.ArrowBack`) que invoca `onBack` (resetando a UI para o estado `Idle`).

2. **MapLibre Integrado (`MapLibreMapView`)**:
   - Renderizado em tela cheia com interações nativas habilitadas (zoom, pan, rotação) utilizando o estilo de tiles **Carto Voyager (OpenStreetMap)** (`https://basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}.png`).
   - **Visualização Detalhada**: Exibe alto nível de detalhe com ruas, travessas, avenidas principais e rodovias perfeitamente legíveis.
   - **Polilinha da Rota (`PolylineOptions`)**: Desenha a linha exata do trajeto com largura de `6f`. A cor é dinâmica: verde ecológico (`#2E7D32`) para modais sustentáveis e azul vibrante (`#0066FF`) para veículos convencionais.
   - **Marcadores (`MarkerOptions`)**:
     - Marcador de **Origem**: Inserido na primeira coordenada geográfica (`pathPoints.first()`) com o título da origem e snippet *"Origem"*.
     - Marcador de **Destino**: Inserido na última coordenada geográfica (`pathPoints.last()`) com o título do destino e snippet *"Destino"*.
   - **Centralização e Enquadramento Suave (`fitBounds`)**:
     - Utiliza `LatLngBounds.Builder()` para incluir todas as coordenadas do percurso da rota do ORS.
     - Aplica animação suave de câmera com `mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120), 1000)` garantindo que todo o trajeto fique perfeitamente visível na tela.

3. **Carrossel Superior de Seleção Rápida (`LazyRow`)**:
   - Chips flutuantes na parte superior do mapa (`RouteSelectionChip`) permitindo alternar rapidamente entre as rotas dos modais disponíveis.
   - Cada chip exibe o ícone do modal, tempo estimado em minutos e custo aproximado em reais.

4. **Card Flutuante Inferior Expansível**:
   - **Cabeçalho com Selo ECO**: Ícone do modal ativo com destaque em fundo verde ecológico para opções sustentáveis e tag "ECO".
   - **Grid de Métricas Principais (`MetricBadge`)**:
     - ⏱️ **Duração**: Exibido em minutos (ex: `11 min`).
     - 📏 **Distância**: Exibido em quilômetros formatados com uma casa decimal (ex: `4.8 km`).
     - 💰 **Custo Estimado**: Formatado em moeda brasileira (ex: `R$ 1,20` ou `Grátis`).
     - 🍃 **Emissão de CO₂**: Quantidade de gás carbônico em kg (ex: `0.07 kg` ou `Zero`).
   - **Painel Detalhado de Etapas (`RouteSegmentItem`)**:
     - Expansível/Recolhível via animação de tamanho (`animateContentSize`) com botão de alternância (`ExpandMore`/`ExpandLess`).
     - Lista rolável (`LazyColumn`) detalhando as instruções de navegação trecho a trecho obtidas nativamente em português (`language=pt`) da API do OpenRouteService, indicando cada manobra, nome de rua/avenida, distância em km e tempo aproximado.

### ⚡ Interações e Comportamentos
- Clicar em um chip de rota no carrossel superior dispara `onSelectRoute`, atualizando a propriedade `selectedRoute` no estado e reanimando a câmera do mapa MapLibre para o novo percurso.
- Tocar no botão de voltar aciona `viewModel.resetToIdle()`, retornando à tela inicial para nova consulta.

---

## ⚠️ 4. ErrorScreen (Tratamento de Falhas e Erros)

### 🎯 Propósito
A `ErrorScreen` fornece um tratamento gracioso e amigável para exceções de rede ou servidor (como falhas de conexão, endereço não encontrado na geocodificação do ORS ou chave de API inválida), orientando o usuário sobre como proceder.

```
+-------------------------------------------------------+
|                                                       |
|                    [ ⚠️ Ícone Erro ]                   |
|                                                       |
|            Não foi possível calcular a rota           |
|                                                       |
|  "Falha de conexão com a internet ao consultar        |
|   o OpenRouteService. Verifique sua rede."            |
|                                                       |
|              [ BOTÃO: Tentar Novamente ]              |
|              [ BOTÃO: Alterar Pesquisa ]              |
|                                                       |
+-------------------------------------------------------+
```

### 🧩 Componentes e Layout
- **Card Central Elevado**: Card centralizado com cantos arredondados (24dp) e fundo translúcido da paleta de superfície.
- **Ícone Alerta de Erro**: Ícone de erro (`Icons.Outlined.ErrorOutline`) em contêiner circular com cor de fundo do esquema de erros do Material3 (`errorContainer`).
- **Título de Diagnóstico**: *"Não foi possível calcular a rota"* em negrito.
- **Mensagem Detalhada Dinâmica**: Texto descritivo que exibe a razão exata da falha, recuperada do estado `UiState.Error(message)`.
- **Ações de Recuperação**:
  - **Botão Principal ("Tentar Novamente")**: Aciona `onRetry()`, reexecutando a solicitação de cálculo remoto.
  - **Botão Secundário ("Alterar Pesquisa")**: Aciona `onReset()`, resetando a UI para o estado `UiState.Idle` para edição de origem/destino na `HomeScreen`.
