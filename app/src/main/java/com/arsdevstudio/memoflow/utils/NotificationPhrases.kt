package com.arsdevstudio.memoflow.utils

object NotificationPhrases {
    val dailyReminder = listOf(
        "O dia está quase no fim. Que tal eternizar um momento?",
        "Suas memórias são o seu maior tesouro. Guardou a de hoje?",
        "O papel digital está em branco. Qual a história de hoje?",
        "Um minuto de reflexão pode mudar seu amanhã. Escreva.",
        "Não deixe o dia passar sem deixar sua marca. ✍️",
        "Como foi o seu dia? O Memo Flow quer te ouvir."
    )

    val timeCapsule = listOf(
        "O gelo derreteu! Uma memória do passado acaba de abrir. ❄️",
        "Sua cápsula do tempo está pronta. O que você diria para o seu 'eu' de hoje?",
        "Mensagem do passado recebida! Venha ler o que você guardou.",
        "O tempo voa... e sua memória acaba de aterrissar. Abra agora!",
        "Nostalgia em 3... 2... 1... Sua nota congelada está disponível. ⏳",
        "Lembra disso? Sua cápsula do tempo foi aberta."
    )

    val gratitudeReminder = listOf(
        "O que fez seu coração sorrir hoje? Coloque no pote. ☀️",
        "Gratidão atrai coisas boas. Já agradeceu por algo hoje?",
        "O pote está sentindo falta de luz. O que te trouxe alegria?",
        "Um pequeno detalhe pode ser uma grande gratidão. Registre agora.",
        "Alimente seu pote de gratidão e veja sua semana brilhar.",
        "Brilhe mais hoje. Adicione uma nova gratidão ao seu mural. ✨"
    )

    val lockedNotes = listOf(
        "Há um segredo guardado no cadeado... quer relembrar? 🔒",
        "Ecos do passado: revisite uma nota trancada e veja sua evolução.",
        "Suas notas trancadas guardam quem você era. Quem você é hoje?",
        "Um momento de privacidade e reflexão. Releia seus segredos.",
        "O que você sentia há meses? A resposta está trancada aqui. 🔑",
        "Não deixe seus segredos esquecidos. Eles são sua jornada."
    )

    val weeklyInsight = listOf(
        "Sua semana em um gráfico. Venha ver seu resumo de humores. 📊",
        "Domingo é dia de balanço. Como foi o fluxo da sua semana?",
        "Você esteve mais vibrante esta semana! Veja seus dados.",
        "Uma visão clara da sua jornada. Seu relatório está pronto.",
        "Feche a semana com consciência. Confira suas estatísticas.",
        "De segunda a domingo: veja como suas emoções fluíram. ⚡"
    )

    val newYearCapsule = listOf(
        "O Memo Flow parou para te mostrar sua jornada. Feliz Ano Novo! 🎆",
        "365 dias em um resumo. Sua Cápsula da Virada está pronta.",
        "Quem você era em Janeiro passado? Veja seu fluxo de evolução.",
        "Sua retrospectiva emocional acaba de aterrissar. Abra o portal.",
        "Um novo ciclo começa, mas o que você viveu está guardado aqui. ✨",
        "Gratidões, segredos e humores: o balanço do seu ano chegou."
    )

    val donationPhrases = listOf(
        "Gostando do app? Considere pagar um pão com ovo ao dev! 🍳",
        "Dê uma força para o Memo Flow! Pague um café ao dev. ☕",
        "Ajude a manter o app no ar! Considere uma pequena doação. ✨"
    )

    val streakPhrases = listOf(
        "O fluxo está parando... Que tal registrar o momento de hoje para não perder sua sequência? ⚡",
        "Faz 2 dias que você não escreve. Suas futuras memórias estão sentindo sua falta! ✍️",
        "Não deixe sua jornada ter um buraco. Escreva algo breve hoje! ✨",
        "A constância é a chave da evolução. Volte ao fluxo hoje! 🌊"
    )

    fun getRandomPhrase(category: List<String>): String = category.random()
}

