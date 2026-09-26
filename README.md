<div align="center">

# 🎲 ShakeRoll

### Balance o celular. Role o dado. Simples assim.

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%203-757575?style=for-the-badge&logo=materialdesign&logoColor=white)
![SensorManager](https://img.shields.io/badge/SensorManager-34A853?style=for-the-badge&logo=android&logoColor=white)
![Vibrator](https://img.shields.io/badge/Vibrator-FF6D00?style=for-the-badge&logo=android&logoColor=white)

**ShakeRoll** é um dado digital de 6 lados que substitui o clique no botão pelo movimento físico. Basta balançar o celular para rolar o dado, com animações 3D e feedback tátil ao cair em um número.

[Funcionalidades](#-funcionalidades) • [Tecnologias](#-tecnologias) • [Como Testar](#-como-testar) • [Estrutura](#-estrutura-do-projeto)

</div>

---


---

## ✨ Funcionalidades

- **🎲 Dado Digital de 6 Lados:** Rola automaticamente quando detecta o shake do celular.
- **📳 Detecção de Shake:** Usa o acelerômetro para calcular a magnitude da aceleração (√(x² + y² + z²)) e dispara a rolagem quando ultrapassa o limite.
- **🎬 Animações 3D:** Rotação nos três eixos (X, Y, Z) e efeito de "quicar" com spring bounce.
- **📱 Feedback Tátil (Haptics):** Micro-vibrações durante a rolagem e um "thump" curto ao cair em um número.
- **🎨 Dado Vetorial:** Os pontos (1-6) são desenhados programaticamente com Compose Canvas — sem imagens externas.
- **📜 Histórico de Rolagens:** Exibe as últimas jogadas em badges animados.
- **👆 Fallback por Toque:** Botão para rolar sem balançar (útil em emuladores).
- **🌙 Tema Escuro:** Interface moderna com Material 3.

---

## 🛠️ Tecnologias

### Linguagem e UI
| Tecnologia | Descrição |
|------------|-----------|
| **Kotlin** | Linguagem oficial e moderna do Android. |
| **Jetpack Compose** | Toolkit declarativo para construção da interface. |
| **Material 3** | Sistema de design do Google. |
| **Compose Canvas** | Desenho programático dos pontos do dado. |

### Sensores e Hardware
| Tecnologia | Descrição |
|------------|-----------|
| **SensorManager** | Gerencia os sensores do dispositivo. |
| **Sensor.TYPE_ACCELEROMETER** | Leitura do acelerômetro (eixos X, Y, Z). |
| **Vibrator / VibratorManager** | Feedback tátil (haptics). |

### Arquitetura e Boas Práticas
| Tecnologia | Descrição |
|------------|-----------|
| **DisposableEffect** | Gerenciamento do ciclo de vida do sensor. |
| **LifecycleEventObserver** | Registra/desregistra o sensor em `onResume`/`onPause` (economia de bateria). |
| **Coroutines** | Programação assíncrona para animações e shuffle. |
| **Animatable** | Controle fino de animações (rotação, escala). |

---

## 🏗️ Arquitetura

O app segue uma arquitetura simples baseada em **Compose + State**, com gerenciamento de ciclo de vida via `DisposableEffect`:

```
┌─────────────────────────────────────────────┐
│                  UI Layer                    │
│      (ShakeRollApp + DieFaceCanvas)          │
└────────────────────┬────────────────────────┘
                     │
┌────────────────────▼────────────────────────┐
│              Sensor Layer                    │
│          (ShakeDetector + Lifecycle)         │
└────────────────────┬────────────────────────┘
                     │
┌────────────────────▼────────────────────────┐
│              Animation Layer                 │
│    (Animatable + Haptic Feedback)            │
└─────────────────────────────────────────────┘
```

---

## 📱 Como Testar

### Pré-requisitos
- **Android Studio** (versão mais recente)
- **Dispositivo Android** com acelerômetro (recomendado) ou emulador
- **Android 7.0 (API 24)** ou superior

### Passos

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/luizfabiocode/ShakeRoll.git
   ```

2. **Abra o projeto no Android Studio.**

3. **Sincronize o Gradle** (automático).

4. **Execute o app:**
   - Conecte um dispositivo físico via USB (recomendado para sensores reais).
   - Ou use um emulador e clique no ícone de "shake" na barra lateral.

5. **Teste as funcionalidades:**
   - Balance o celular para rolar o dado.
   - Ou toque no dado para usar o fallback.
   - Veja o histórico de rolagens na parte superior.
   - Sinta a vibração ao cair em um número.

> ⚠️ **Importante:** Emuladores não têm acelerômetro real. Para a experiência completa, teste em um **dispositivo físico**.

---

## 📂 Estrutura do Projeto

```
ShakeRoll/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── ShakeDetector.kt
│   │   │   │   ├── Haptics.kt
│   │   │   │   ├── ShakeRollApp.kt
│   │   │   │   ├── DieFaceCanvas.kt
│   │   │   │   └── ShakeInstructionCard.kt
│   │   │   ├── res/
│   │   │   │   └── values/
│   │   │   │       └── strings.xml
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build.gradle.kts
└── README.md
```

---

## 🔐 Permissões

O app solicita as seguintes permissões:

| Permissão | Motivo |
|-----------|--------|
| `VIBRATE` | Feedback tátil ao rolar o dado. |

> O app **não coleta nem compartilha** seus dados. Tudo é processado localmente no dispositivo.

---

## 🚀 Roadmap (Melhorias Futuras)

- [ ] 🎲 Suporte a dados de 4, 8, 12 e 20 lados
- [ ] 🔊 Efeitos sonoros ao rolar
- [ ] ⚙️ Tela de configurações (ajuste do threshold)
- [ ] 📊 Estatísticas de rolagens (frequência de cada número)
- [ ] 🎨 Temas personalizáveis (Material You)
- [ ] ⌚ Integração com Wear OS

---

## 👨‍💻 Autor

**Luiz Fabio**

[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/luizfabiocode)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/luizfabiocode/)



---




</div>
