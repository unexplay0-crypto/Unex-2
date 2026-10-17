package com.example.core.service

import com.example.core.model.ChannelCategory
import com.example.core.model.EpgProgram
import com.example.core.model.EpisodeItem
import com.example.core.model.SeriesGroup
import com.example.core.model.StreamItem
import com.example.core.model.StreamType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit

class XtreamCatalogService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    // Pre-curated premium channels, movies and series with verified working HLS/MP4 streams
    val defaultLiveCategories = listOf(
        ChannelCategory("all", "Todos os Canais", StreamType.LIVE, isAdult = false, 12),
        ChannelCategory("cinema_vip", "Cinema & Filmes VIP", StreamType.LIVE, isAdult = false, 4),
        ChannelCategory("esportes_4k", "Esportes & Arena 4K", StreamType.LIVE, isAdult = false, 3),
        ChannelCategory("noticias", "Notícias & Variedades", StreamType.LIVE, isAdult = false, 3),
        ChannelCategory("adulto_18", "Adulto +18 (PIN 0000)", StreamType.LIVE, isAdult = true, 2)
    )

    val defaultVodCategories = listOf(
        ChannelCategory("vod_all", "Todos os Filmes", StreamType.MOVIE, isAdult = false, 8),
        ChannelCategory("vod_action", "Ação & Sci-Fi 4K", StreamType.MOVIE, isAdult = false, 3),
        ChannelCategory("vod_animation", "Animação & Fantasia", StreamType.MOVIE, isAdult = false, 3),
        ChannelCategory("vod_documentary", "Documentários", StreamType.MOVIE, isAdult = false, 2)
    )

    val defaultSeriesCategories = listOf(
        ChannelCategory("series_all", "Todas as Séries", StreamType.SERIES, isAdult = false, 5),
        ChannelCategory("series_scifi", "Ficção & Cyberpunk", StreamType.SERIES, isAdult = false, 3),
        ChannelCategory("series_drama", "Suspense & Drama", StreamType.SERIES, isAdult = false, 2)
    )

    // Preloaded high-quality streams
    fun getInitialCatalog(): Triple<List<StreamItem>, List<StreamItem>, List<SeriesGroup>> {
        val liveList = listOf(
            StreamItem(
                streamId = "live_1",
                name = "UNEX 4K Cinema Premiere",
                type = StreamType.LIVE,
                streamIcon = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=500&auto=format&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                categoryId = "cinema_vip",
                categoryName = "Cinema & Filmes VIP",
                rating = 4.9f,
                plot = "Transmissão ininterrupta dos maiores lançamentos do cinema mundial em som Dolby e HDR.",
                resolution = "4K HDR"
            ),
            StreamItem(
                streamId = "live_2",
                name = "HBO Max Action VIP",
                type = StreamType.LIVE,
                streamIcon = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=500&auto=format&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                categoryId = "cinema_vip",
                categoryName = "Cinema & Filmes VIP",
                rating = 4.8f,
                plot = "Adrenalina pura, perseguições épicas e combates cinematográficos 24 horas.",
                resolution = "FHD"
            ),
            StreamItem(
                streamId = "live_3",
                name = "Cine Retro Sci-Fi",
                type = StreamType.LIVE,
                streamIcon = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=500&auto=format&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                categoryId = "cinema_vip",
                categoryName = "Cinema & Filmes VIP",
                rating = 4.7f,
                plot = "O melhor da ficção científica pós-apocalíptica e efeitos especiais lendários.",
                resolution = "1080p 60fps"
            ),
            StreamItem(
                streamId = "live_4",
                name = "Arena Sports 1 UHD",
                type = StreamType.LIVE,
                streamIcon = "https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=500&auto=format&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                categoryId = "esportes_4k",
                categoryName = "Esportes & Arena 4K",
                rating = 4.9f,
                plot = "Cobertura total dos principais campeonatos de futebol internacional e velocidade.",
                resolution = "4K 60fps"
            ),
            StreamItem(
                streamId = "live_5",
                name = "Extreme Motors & Speed",
                type = StreamType.LIVE,
                streamIcon = "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?w=500&auto=format&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                categoryId = "esportes_4k",
                categoryName = "Esportes & Arena 4K",
                rating = 4.8f,
                plot = "Supercarros, drift e acrobacias nas pistas mais desafiadoras do planeta.",
                resolution = "FHD"
            ),
            StreamItem(
                streamId = "live_6",
                name = "Global News 24/7",
                type = StreamType.LIVE,
                streamIcon = "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=500&auto=format&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                categoryId = "noticias",
                categoryName = "Notícias & Variedades",
                rating = 4.6f,
                plot = "Jornalismo ao vivo, cotações financeiras globais e análises geopolíticas em tempo real.",
                resolution = "1080p"
            ),
            StreamItem(
                streamId = "live_7",
                name = "Discovery Cyber & Tech",
                type = StreamType.LIVE,
                streamIcon = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=500&auto=format&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4",
                categoryId = "noticias",
                categoryName = "Notícias & Variedades",
                rating = 4.9f,
                plot = "Explorando a fronteira da inteligência artificial, robótica e exploração espacial.",
                resolution = "4K HDR"
            ),
            StreamItem(
                streamId = "live_adult_1",
                name = "Venus Midnight Club +18",
                type = StreamType.LIVE,
                streamIcon = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                categoryId = "adulto_18",
                categoryName = "Adulto +18 (PIN 0000)",
                rating = 4.7f,
                plot = "Conteúdo exclusivo para adultos protegido por senha parental.",
                isAdult = true,
                resolution = "FHD"
            )
        )

        val vodList = listOf(
            StreamItem(
                streamId = "vod_1",
                name = "Cyber Odyssey: 2099",
                type = StreamType.MOVIE,
                streamIcon = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=600&auto=format&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                categoryId = "vod_action",
                categoryName = "Ação & Sci-Fi 4K",
                rating = 4.9f,
                plot = "Em uma metrópole futurista iluminada por neons e inteligência artificial, um agente cibernético renegado descobre a verdade sobre a simulação global.",
                releaseDate = "2025",
                durationMinutes = 138,
                resolution = "4K UHD"
            ),
            StreamItem(
                streamId = "vod_2",
                name = "Guardiões do Abismo",
                type = StreamType.MOVIE,
                streamIcon = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                categoryId = "vod_action",
                categoryName = "Ação & Sci-Fi 4K",
                rating = 4.8f,
                plot = "Uma equipe de cientistas em um posto avançado oceânico acidentalmente desperta uma força colossal adormecida há milênios.",
                releaseDate = "2024",
                durationMinutes = 115,
                resolution = "4K HDR"
            ),
            StreamItem(
                streamId = "vod_3",
                name = "A Jornada do Dragão",
                type = StreamType.MOVIE,
                streamIcon = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                categoryId = "vod_animation",
                categoryName = "Animação & Fantasia",
                rating = 4.9f,
                plot = "Uma jovem guerreira viaja por montanhas esquecidas em busca da criatura lendária que pode restaurar o equilíbrio do reino.",
                releaseDate = "2024",
                durationMinutes = 98,
                resolution = "FHD"
            ),
            StreamItem(
                streamId = "vod_4",
                name = "Mistérios do Cosmos Profundo",
                type = StreamType.MOVIE,
                streamIcon = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                categoryId = "vod_documentary",
                categoryName = "Documentários",
                rating = 4.9f,
                plot = "Imagens espetaculares dos telescópios de última geração revelando buracos negros, nebulosas brilhantes e exoplanetas habitáveis.",
                releaseDate = "2025",
                durationMinutes = 88,
                resolution = "4K Dolby Vision"
            )
        )

        val seriesList = listOf(
            SeriesGroup(
                seriesId = "ser_1",
                name = "Neon Nexus: Vigilantes",
                cover = "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=600&auto=format&fit=crop",
                plot = "Nas sombras de uma megacidade vertical, justiceiros aprimorados lutam contra corporações que monopolizam a tecnologia neural.",
                rating = 4.9f,
                genre = "Cyberpunk / Ação",
                seasonsCount = 2,
                episodes = listOf(
                    EpisodeItem(
                        episodeId = "s1_e1",
                        title = "E01 - O Despertar da Rede",
                        seasonNum = 1,
                        episodeNum = 1,
                        streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                        plot = "O primeiro ataque hacker contra o mainframe neural da cidade inicia uma caçada implacável."
                    ),
                    EpisodeItem(
                        episodeId = "s1_e2",
                        title = "E02 - Código Vermelho",
                        seasonNum = 1,
                        episodeNum = 2,
                        streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                        plot = "A equipe deve infiltrar-se em um arranha-céu fortificado para resgatar uma chave criptográfica."
                    ),
                    EpisodeItem(
                        episodeId = "s1_e3",
                        title = "E03 - Fantasmas no Circuito",
                        seasonNum = 1,
                        episodeNum = 3,
                        streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                        plot = "Uma IA senciente oferece uma aliança arriscada em troca de sua liberdade."
                    )
                )
            ),
            SeriesGroup(
                seriesId = "ser_2",
                name = "Estação Lunar Horizon",
                cover = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=600&auto=format&fit=crop",
                plot = "Cientistas na primeira base permanente do polo sul lunar enfrentam sabotagens misteriosas e anomalias gravitacionais.",
                rating = 4.8f,
                genre = "Ficção Científica",
                seasonsCount = 1,
                episodes = listOf(
                    EpisodeItem(
                        episodeId = "s2_e1",
                        title = "E01 - Contato Gravitacional",
                        seasonNum = 1,
                        episodeNum = 1,
                        streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                        plot = "Um tremor sísmico sem precedentes expõe uma caverna com estruturas artificiais desconhecidas."
                    ),
                    EpisodeItem(
                        episodeId = "s2_e2",
                        title = "E02 - Oxigênio Zero",
                        seasonNum = 1,
                        episodeNum = 2,
                        streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                        plot = "Com os sistemas de suporte de vida comprometidos, a tripulação precisa correr contra o tempo."
                    )
                )
            )
        )

        return Triple(liveList, vodList, seriesList)
    }

    // Dynamic EPG generator for channels
    fun getEpgForChannel(channelName: String): List<EpgProgram> {
        return listOf(
            EpgProgram(
                title = "Programa Atual: $channelName Transmissão Ao Vivo",
                startTime = "18:00",
                endTime = "20:00",
                description = "Transmissão principal em alta definição com comentários exclusivos e som imersivo.",
                progressPercent = 0.65f
            ),
            EpgProgram(
                title = "A Seguir: Super Sessão UNEX Cinema",
                startTime = "20:00",
                endTime = "22:30",
                description = "Grandes sucessos de bilheteria e clássicos do entretenimento sem cortes comerciais.",
                progressPercent = 0.0f
            ),
            EpgProgram(
                title = "Madrugada: Arquivos Noturnos",
                startTime = "22:30",
                endTime = "01:00",
                description = "Documentários e matérias especiais sobre avanços científicos e grandes mistérios.",
                progressPercent = 0.0f
            )
        )
    }

    // Xtream Codes live API fetcher (with coroutines)
    suspend fun fetchXtreamLive(serverUrl: String, user: String, pass: String): List<StreamItem> =
        withContext(Dispatchers.IO) {
            try {
                val cleanUrl = serverUrl.trim().removeSuffix("/")
                val url = "$cleanUrl/player_api.php?username=$user&password=$pass&action=get_live_streams"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val arr = JSONArray(body)
                val list = mutableListOf<StreamItem>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val id = obj.optString("stream_id")
                    val name = obj.optString("name")
                    val icon = obj.optString("stream_icon")
                    val catId = obj.optString("category_id", "all")
                    val streamUrl = "$cleanUrl/live/$user/$pass/$id.m3u8"
                    list.add(
                        StreamItem(
                            streamId = id,
                            name = name,
                            type = StreamType.LIVE,
                            streamIcon = icon,
                            streamUrl = streamUrl,
                            categoryId = catId,
                            categoryName = "Canais Xtream",
                            resolution = "FHD"
                        )
                    )
                }
                list
            } catch (e: Exception) {
                emptyList()
            }
        }

    // M3U Playlist Parser Fallback
    suspend fun parseM3U(m3uContent: String): List<StreamItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<StreamItem>()
        val lines = m3uContent.lines()
        var currentName = ""
        var currentIcon = ""
        var currentGroup = "Geral"

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#EXTINF:")) {
                // Parse group-title
                val groupMatch = Regex("""group-title="([^"]+)"""").find(trimmed)
                currentGroup = groupMatch?.groupValues?.get(1) ?: "Geral"

                // Parse tvg-logo
                val logoMatch = Regex("""tvg-logo="([^"]+)"""").find(trimmed)
                currentIcon = logoMatch?.groupValues?.get(1) ?: ""

                // Parse channel name (after last comma)
                val commaIndex = trimmed.lastIndexOf(',')
                if (commaIndex != -1 && commaIndex < trimmed.length - 1) {
                    currentName = trimmed.substring(commaIndex + 1).trim()
                } else {
                    currentName = "Canal M3U"
                }
            } else if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                if (currentName.isNotEmpty()) {
                    val id = "m3u_${items.size + 1}"
                    items.add(
                        StreamItem(
                            streamId = id,
                            name = currentName,
                            type = StreamType.LIVE,
                            streamIcon = currentIcon,
                            streamUrl = trimmed,
                            categoryId = currentGroup.lowercase().replace(" ", "_"),
                            categoryName = currentGroup,
                            resolution = "HD"
                        )
                    )
                    currentName = ""
                    currentIcon = ""
                }
            }
        }
        items
    }
}
