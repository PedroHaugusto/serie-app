package com.serie.app.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * Estatisticas derivadas do historico.
 *
 * Sao funcoes puras sobre uma lista de sessoes: nao tocam banco, relogio nem
 * Android. Isso mantem a regra de "o que conta como sequencia" num lugar so,
 * e testavel sem aparelho.
 */
object Stats {

    /** Quantos bastoes o grafico de volume mostra. */
    const val BAR_COUNT = 12

    /** Quantos rotulos aparecem no eixo. */
    private const val AXIS_LABELS = 4

    /** Abaixo disso, a tela de evolucao mostra o estado de "poucos dados". */
    const val MIN_SESSIONS_FOR_CHARTS = 3

    fun totalWorkouts(history: List<CompletedSession>): Int = history.size

    /**
     * Dias seguidos de treino.
     *
     * Conta para tras a partir de hoje. Se ainda nao houve treino hoje, a
     * contagem comeca em ontem - senao a sequencia de quem treina a noite
     * zeraria toda manha.
     */
    fun streakDays(history: List<CompletedSession>, today: LocalDate): Int {
        val days = history.map { it.date }.toSet()
        if (days.isEmpty()) return 0
        var cursor = if (today in days) today else today.minusDays(1)
        if (cursor !in days) return 0
        var count = 0
        while (cursor in days) {
            count++
            cursor = cursor.minusDays(1)
        }
        return count
    }

    /** Segunda-feira da semana de [today]. */
    fun weekStart(today: LocalDate): LocalDate = today.minusDays((today.dayOfWeek.value - 1).toLong())

    fun weekVolumeKg(history: List<CompletedSession>, today: LocalDate): Double {
        val start = weekStart(today)
        return history.filter { it.date >= start && it.date <= today }.sumOf { it.volumeKg }
    }

    fun sessionsThisWeek(history: List<CompletedSession>, today: LocalDate): Int {
        val start = weekStart(today)
        return history.count { it.date >= start && it.date <= today }
    }

    /**
     * A faixa da semana da tela Hoje.
     *
     * Cada dia e classificado por fato, nao por suposicao: ja treinou, e hoje,
     * tem treino agendado, ou e descanso.
     */
    fun week(
        history: List<CompletedSession>,
        workouts: List<Workout>,
        today: LocalDate,
    ): List<WeekDay> {
        val start = weekStart(today)
        // O ultimo treino de cada dia e o que a faixa mostra: treinar duas
        // vezes no mesmo dia ainda e um dia treinado.
        val doneByDay = history
            .sortedBy { it.startedAtMillis }
            .associate { it.date to it.workoutLetter }
        return (0..6).map { offset ->
            val date = start.plusDays(offset.toLong())
            val scheduled = workouts.firstOrNull { it.days.getOrNull(offset) == true }
            val doneLetter = doneByDay[date]
            val state = when {
                doneLetter != null -> DayState.Done
                date == today -> DayState.Today
                scheduled != null -> DayState.Planned
                else -> DayState.Rest
            }
            WeekDay(
                label = DateFormat.WEEKDAY_INITIALS[offset],
                state = state,
                letter = doneLetter ?: scheduled?.letter.orEmpty(),
                isToday = date == today,
            )
        }
    }

    /**
     * Quantos dias distintos da semana tiveram treino.
     *
     * Dias, nao sessoes: treinar duas vezes na quinta nao sao dois dias de
     * treino, e contar sessoes fazia o progresso da semana saltar sozinho.
     */
    fun trainedDaysThisWeek(week: List<WeekDay>): Int =
        week.count { it.state == DayState.Done }

    /**
     * Quantos dias da semana tem treino agendado.
     *
     * E o denominador honesto do progresso: a meta do perfil e uma intencao,
     * enquanto o agendamento e o plano que a propria faixa da semana desenha.
     */
    fun scheduledDaysThisWeek(workouts: List<Workout>): Int =
        (0..6).count { day -> workouts.any { it.days.getOrNull(day) == true } }

    /**
     * Variacao de peso entre a pesagem mais recente e a mais antiga dentro da
     * janela. Nulo quando nao ha duas pesagens para comparar.
     */
    fun weightChange(
        weights: List<BodyWeight>,
        today: LocalDate,
        windowDays: Long = 90,
    ): Pair<Double, Long>? {
        if (weights.size < 2) return null
        val latest = weights.maxByOrNull { it.atMillis } ?: return null
        val since = today.minusDays(windowDays)
        val earliest = weights
            .filter { it.date >= since && it.atMillis < latest.atMillis }
            .minByOrNull { it.atMillis }
            ?: return null
        val days = ChronoUnit.DAYS.between(earliest.date, latest.date)
        return (latest.kg - earliest.kg) to days
    }

    /** "78,4" - peso sem a unidade, que a tela escreve a parte. */
    fun formatWeight(kg: Double): String = "%.1f".format(kg).replace('.', ',')

    /**
     * Variacao de peso dentro de uma janela de dias.
     *
     * Compara a pesagem mais recente com a mais antiga da janela. Nulo quando
     * nao ha duas pesagens dentro dela - sem duas, nao houve variacao medida.
     */
    fun weightChangeIn(weights: List<BodyWeight>, today: LocalDate, days: Long): Double? {
        val latest = weights.maxByOrNull { it.atMillis } ?: return null
        val since = today.minusDays(days)
        val earliest = weights
            .filter { it.date >= since && it.atMillis < latest.atMillis }
            .minByOrNull { it.atMillis }
            ?: return null
        return latest.kg - earliest.kg
    }

    /** As janelas que a tela de evolucao mostra lado a lado. */
    val WEIGHT_WINDOWS = listOf(
        "1 mês" to 30L,
        "3 meses" to 90L,
        "6 meses" to 180L,
        "1 ano" to 365L,
    )

    /**
     * Pesagens de uma janela normalizadas entre 0 e 100, da mais antiga para a
     * mais recente, prontas para desenhar.
     */
    fun weightSeries(weights: List<BodyWeight>, today: LocalDate, days: Long): List<Float> {
        val since = today.minusDays(days)
        val inWindow = weights.filter { it.date >= since }.sortedBy { it.atMillis }
        if (inWindow.size < 2) return emptyList()
        val values = inWindow.map { it.kg }
        val min = values.min()
        val max = values.max()
        val span = max - min
        return values.map { if (span <= 0.0) 50f else (((it - min) / span) * 80 + 10).toFloat() }
    }

    // ------------------------------------------------------- progressao dupla

    /** Quanto a carga sobe quando a faixa de repeticoes e fechada. */
    const val LOAD_STEP_KG = 2.5

    /**
     * Le a faixa prescrita de um texto livre como "10-12", "8–10" ou "12".
     *
     * O editor aceita texto, entao a leitura precisa tolerar tracos diferentes
     * e espacos. Sem numero nenhum, nao ha faixa e o exercicio fica de fora da
     * recomendacao em vez de receber um palpite.
     */
    fun parseRepRange(text: String): RepRange? {
        val numbers = Regex("[0-9]+").findAll(text).map { it.value.toInt() }.toList()
        return when {
            numbers.isEmpty() -> null
            numbers.size == 1 -> RepRange(numbers[0], numbers[0])
            else -> RepRange(minOf(numbers[0], numbers[1]), maxOf(numbers[0], numbers[1]))
        }
    }

    /**
     * Exercicios prontos para subir carga.
     *
     * Progressao dupla: so recomenda quando **todas** as series da ultima
     * sessao fecharam o topo da faixa. Uma serie boa entre tres fracas nao e
     * progresso, e subir a carga ali faria o usuario regredir na proxima.
     *
     * Exercicios sem carga - peso corporal - ficam de fora: neles a
     * progressao e em repeticoes, nao em quilos, e sugerir "+2,5 kg" seria
     * um conselho sem acao possivel.
     */
    fun progressionAdvices(
        history: List<CompletedSession>,
        workouts: List<Workout>,
    ): List<ProgressionAdvice> {
        val prescriptions = workouts
            .flatMap { it.exercises }
            .associateBy({ it.exerciseId }, { it })

        return prescriptions.values.mapNotNull { prescription ->
            val range = parseRepRange(prescription.reps) ?: return@mapNotNull null

            val lastSets = history
                .filter { session -> session.sets.any { it.exerciseId == prescription.exerciseId } }
                .maxByOrNull { it.startedAtMillis }
                ?.sets
                ?.filter { it.exerciseId == prescription.exerciseId }
                .orEmpty()

            if (lastSets.isEmpty()) return@mapNotNull null
            if (lastSets.any { it.loadKg <= 0.0 }) return@mapNotNull null
            if (lastSets.any { it.reps < range.max }) return@mapNotNull null

            val load = lastSets.maxOf { it.loadKg }
            ProgressionAdvice(
                exerciseId = prescription.exerciseId,
                exerciseName = prescription.name,
                currentLoadKg = load,
                suggestedLoadKg = load + LOAD_STEP_KG,
                range = range,
                achievedReps = lastSets.minOf { it.reps },
                sets = lastSets.size,
            )
        }.sortedBy { it.exerciseName }
    }

    /** As sessoes concluidas numa data. */
    fun sessionsOn(history: List<CompletedSession>, date: LocalDate): List<CompletedSession> =
        history.filter { it.date == date }

    /** Quantos dias cada periodo do grafico cobre. */
    private fun periodDays(periodIndex: Int): Long = when (periodIndex) {
        0 -> 28
        1 -> 90
        2 -> 180
        else -> 365
    }

    val PERIOD_LABELS = listOf("4 sem", "3 meses", "6 meses", "1 ano")

    /**
     * Volume agregado em 12 faixas iguais dentro do periodo.
     *
     * Faixas de largura fixa, e nao "por semana" ou "por mes", para que o
     * mesmo desenho sirva aos quatro periodos sem mudar o numero de barras.
     */
    fun volumeChart(
        history: List<CompletedSession>,
        periodIndex: Int,
        today: LocalDate,
    ): EvolutionPeriod {
        val days = periodDays(periodIndex)
        val start = today.minusDays(days - 1)
        val bucketDays = days.toDouble() / BAR_COUNT

        val totals = DoubleArray(BAR_COUNT)
        history.filter { it.date >= start && it.date <= today }.forEach { session ->
            val offset = ChronoUnit.DAYS.between(start, session.date).toDouble()
            val index = (offset / bucketDays).toInt().coerceIn(0, BAR_COUNT - 1)
            totals[index] += session.volumeKg
        }

        val max = totals.maxOrNull() ?: 0.0
        val bars = totals.map { if (max <= 0.0) 0 else ((it / max) * 100).roundToInt() }

        val axis = (0 until AXIS_LABELS).map { i ->
            val bucket = i * (BAR_COUNT - 1) / (AXIS_LABELS - 1)
            val date = start.plusDays((bucket * bucketDays).toLong())
            if (days <= 31) "${date.dayOfMonth}/${date.monthValue}"
            else DateFormat.shortDate(date).substringAfter(" ").substringAfter(" ")
        }

        val total = totals.sum()
        val previousStart = start.minusDays(days)
        val previous = history
            .filter { it.date >= previousStart && it.date < start }
            .sumOf { it.volumeKg }
        val delta = when {
            previous <= 0.0 && total <= 0.0 -> "sem base anterior"
            previous <= 0.0 -> "1º período"
            else -> {
                val pct = ((total - previous) / previous * 100).roundToInt()
                "${if (pct >= 0) "+" else ""}$pct% vs. anterior"
            }
        }

        return EvolutionPeriod(
            label = PERIOD_LABELS[periodIndex],
            volume = formatVolume(total),
            delta = delta,
            axis = axis,
            bars = bars,
        )
    }

    /** "18,4 t" acima de mil quilos, "840 kg" abaixo. */
    fun formatVolume(kg: Double): String = if (kg >= 1000) {
        "%.1f t".format(kg / 1000).replace('.', ',')
    } else {
        "${kg.roundToInt()} kg"
    }

    /** Volume dos ultimos 30 dias contra os 30 anteriores. */
    fun monthDelta(history: List<CompletedSession>, today: LocalDate): String {
        val thisMonth = history
            .filter { it.date > today.minusDays(30) && it.date <= today }
            .sumOf { it.volumeKg }
        val lastMonth = history
            .filter { it.date > today.minusDays(60) && it.date <= today.minusDays(30) }
            .sumOf { it.volumeKg }
        if (lastMonth <= 0.0) return if (thisMonth > 0.0) "1º mês" else "—"
        val pct = ((thisMonth - lastMonth) / lastMonth * 100).roundToInt()
        return "${if (pct >= 0) "+" else ""}$pct%"
    }

    /** O recorde mais recente, para o card da tela Hoje. */
    fun lastRecord(history: List<CompletedSession>): PersonalRecord? {
        val ordered = history.sortedBy { it.startedAtMillis }
        var last: PersonalRecord? = null
        ordered.forEachIndexed { index, session ->
            recordsOf(session, ordered.take(index)).lastOrNull()?.let { last = it }
        }
        return last
    }

    /** "treinos · 3,2 por semana" a partir do intervalo real do historico. */
    fun workoutsPerWeek(history: List<CompletedSession>, today: LocalDate): String {
        if (history.isEmpty()) return "treinos"
        val first = history.minOf { it.date }
        val weeks = (ChronoUnit.DAYS.between(first, today) + 1) / 7.0
        if (weeks < 1.0) return "treinos · primeira semana"
        val rate = history.size / weeks
        return "treinos · %.1f por semana".format(rate).replace('.', ',')
    }

    /**
     * O grupo mais negligenciado, quando ha um.
     *
     * So acusa abaixo de 10% do volume total e com pelo menos tres grupos
     * treinados - com dois exercicios no historico, qualquer grupo pareceria
     * negligenciado.
     */
    fun neglectedGroup(volumes: List<MuscleVolume>): String? {
        if (volumes.size < 3) return null
        val last = volumes.last()
        val share = last.percent.removeSuffix("%").toIntOrNull() ?: return null
        return if (share < 10) last.name + " abaixo da meta" else null
    }

    fun muscleVolumes(history: List<CompletedSession>): List<MuscleVolume> {
        val byGroup = history.flatMap { it.sets }
            .groupBy { it.muscleGroup }
            .mapValues { (_, sets) -> sets.sumOf { it.volumeKg } }
            .filterValues { it > 0 }
        if (byGroup.isEmpty()) return emptyList()

        val total = byGroup.values.sum()
        val max = byGroup.values.max()
        return byGroup.entries
            .sortedByDescending { it.value }
            .map { (group, volume) ->
                MuscleVolume(
                    name = group.label,
                    weight = ((volume / max) * 100).roundToInt(),
                    percent = "${((volume / total) * 100).roundToInt()}%",
                )
            }
    }

    /**
     * Progressao de carga por exercicio.
     *
     * Um ponto por sessao, com o melhor 1RM estimado daquele dia. So entram
     * exercicios com pelo menos duas sessoes - um ponto nao e progressao.
     */
    fun exerciseProgress(history: List<CompletedSession>): List<ExerciseProgress> {
        val ordered = history.sortedBy { it.startedAtMillis }
        val byExercise = mutableMapOf<String, MutableList<Pair<CompletedSession, CompletedSet>>>()
        ordered.forEach { session ->
            session.sets.groupBy { it.exerciseId }.forEach { (id, sets) ->
                val best = sets.maxBy { it.estimated1RM }
                byExercise.getOrPut(id) { mutableListOf() }.add(session to best)
            }
        }

        return byExercise.values
            .filter { it.size >= 2 }
            .sortedByDescending { it.size }
            .map { points ->
                val name = points.last().second.exerciseName
                val oneRms = points.map { it.second.estimated1RM }
                val max = oneRms.max()
                val first = points.first()
                val last = points.last()
                ExerciseProgress(
                    name = name,
                    estimated1RM = "${max.roundToInt()} kg",
                    from = "${formatKg(first.second.loadKg)} em ${monthOf(first.first)}",
                    to = "${formatKg(last.second.loadKg)} em ${monthOf(last.first)}",
                    // Normaliza entre o menor e o maior para a linha usar a
                    // altura toda; sem isso, cargas parecidas viram uma reta.
                    points = normalize(oneRms),
                )
            }
    }

    private fun normalize(values: List<Double>): List<Float> {
        val min = values.min()
        val max = values.max()
        val span = max - min
        return values.map {
            if (span <= 0.0) 50f else (((it - min) / span) * 80 + 10).toFloat()
        }
    }

    private fun monthOf(session: CompletedSession): String =
        DateFormat.monthYear(session.date).substringBefore(" de")

    fun formatKg(kg: Double): String =
        if (kg % 1.0 == 0.0) "${kg.roundToInt()} kg"
        else "%.1f kg".format(kg).replace('.', ',')

    /**
     * Recordes batidos numa sessao.
     *
     * Um recorde e uma serie cujo 1RM estimado supera tudo o que aquele
     * exercicio ja registrou antes. Comparar carga crua acusaria falso
     * recorde sempre que o usuario fizesse menos repeticoes com mais peso.
     */
    fun recordsOf(
        session: CompletedSession,
        previousHistory: List<CompletedSession>,
    ): List<PersonalRecord> {
        val bestBefore = previousHistory
            .filter { it.startedAtMillis < session.startedAtMillis }
            .flatMap { it.sets }
            .groupBy { it.exerciseId }
            .mapValues { (_, sets) -> sets.maxOf { it.estimated1RM } }

        return session.sets
            .groupBy { it.exerciseId }
            .mapNotNull { (id, sets) ->
                val best = sets.maxBy { it.estimated1RM }
                // Estreia nao e recorde: sem marca anterior nao houve
                // superacao, e contar a primeira vez faria o numero de
                // recordes saltar junto com o de exercicios experimentados.
                val previous = bestBefore[id] ?: return@mapNotNull null
                if (best.estimated1RM <= previous) return@mapNotNull null
                val gain = best.estimated1RM - previous
                PersonalRecord(
                    best.exerciseName,
                    "${formatKg(best.loadKg)} × ${best.reps} · +${formatKg(gain)} no 1RM",
                )
            }
    }

    /** Total de recordes ao longo de todo o historico. */
    fun totalRecords(history: List<CompletedSession>): Int {
        val ordered = history.sortedBy { it.startedAtMillis }
        var count = 0
        val best = mutableMapOf<String, Double>()
        ordered.forEach { session ->
            session.sets.groupBy { it.exerciseId }.forEach { (id, sets) ->
                val top = sets.maxOf { it.estimated1RM }
                val previous = best[id]
                if (previous == null || top > previous) {
                    if (previous != null) count++
                    best[id] = top
                }
            }
        }
        return count
    }
}
