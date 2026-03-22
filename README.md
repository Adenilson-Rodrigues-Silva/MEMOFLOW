# 🌊 Memo Flow

**Memo Flow** é um diário digital inteligente e minimalista projetado para capturar pensamentos, sentimentos e memórias com fluidez. O app combina segurança, geolocalização e inteligência emocional para criar uma linha do tempo única da sua jornada.

## ✨ Funcionalidades Principais

- **📝 Escrita Enriquecida:** Notas com suporte a Rich Text, emojis e seleção de humor.
- **🎙️ Notas de Voz:** Capture a emoção do momento com gravações de áudio integradas.
- **📸 Memórias Visuais:** Adicione fotos às suas anotações para eternizar momentos.
- **❄️ Cápsulas do Tempo:** Congele notas para serem abertas apenas em datas futuras.
- **🔒 Segurança Máxima:** Proteja suas memórias mais íntimas com bloqueio por PIN de 4 dígitos.
- **📍 Mapa de Rastros:** Visualize onde suas memórias foram criadas através de um mapa interativo.
- **📊 Insights de Humor:** Gráficos semanais baseados no seu estado emocional.
- **⚡ Atalhos Rápidos:** Acesso instantâneo via ícone do app (Nova Nota, Relembrar, Gratidão, Mapa).
- **☁️ Backup & Sincronização:** Suporte a exportação local e sincronização com Google Drive (Premium).

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** [Kotlin](https://kotlinlang.org/)
- **UI:** [Jetpack Compose](https://developer.android.com/compose) (Arquitetura Declarativa)
- **Navegação:** Compose Navigation com suporte a Deep Links.
- **Persistência:** [Room Database](https://developer.android.com/training/data-storage/room)
- **Injeção de Dependência:** ViewModel Factory Pattern.
- **Animações:** [Lottie](https://airbnb.design/lottie/) e Compose Animations.
- **Imagens:** [Coil](https://coil-kt.github.io/coil/)
- **Geolocalização:** Google Play Services Location & Google Maps Compose.
- **Editor:** Mohamed Rejeb RichText Editor.

## 🚀 Como Executar o Projeto

1. Clone o repositório:
   ```bash
   git clone https://github.com/seu-usuario/MemoFlow.git
   ```
2. Abra o projeto no **Android Studio (Ladybug ou superior)**.
3. Certifique-se de ter uma chave de API do Google Maps configurada no `AndroidManifest.xml`.
4. Sincronize o Gradle e execute o app em um emulador ou dispositivo físico.

---
*Desenvolvido com carinho para organizar o caos da mente e transformar momentos em memórias eternas.*