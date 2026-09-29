package com.example.data.network

import com.example.data.model.ChannelEntity
import com.example.data.model.EpgProgramEntity
import com.example.data.model.PlaylistProfileEntity

object PublicDemoData {

    const val DEMO_PROFILE_ID = 1L

    val defaultDemoProfile = PlaylistProfileEntity(
        id = DEMO_PROFILE_ID,
        name = "Global Free-To-Air & Open Streams",
        type = "M3U",
        serverUrl = "https://iptv-org.github.io/iptv/index.m3u",
        username = "guest",
        isActive = true,
        channelCount = 14,
        createdAt = System.currentTimeMillis()
    )

    fun getDemoChannels(profileId: Long = DEMO_PROFILE_ID): List<ChannelEntity> {
        return listOf(
            ChannelEntity(
                id = "${profileId}_nasa_tv",
                profileId = profileId,
                name = "NASA TV HD",
                streamUrl = "https://ntv1.akamaized.net/hls/live/2014075/NASA-NTV1-HLS/master.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/e/e5/NASA_logo.svg",
                groupTitle = "Science & Tech",
                tvgId = "nasa.hd",
                tvgName = "NASA TV",
                channelNumber = 1,
                isFavorite = true,
                watchlistNames = "Favorites,Documentary",
                streamFormat = "m3u8"
            ),
            ChannelEntity(
                id = "${profileId}_bloomberg",
                profileId = profileId,
                name = "Bloomberg TV+",
                streamUrl = "https://liveprodupmx.akamaized.net/us/live.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/5/56/Bloomberg_Television_logo.svg",
                groupTitle = "News",
                tvgId = "bloomberg.us",
                tvgName = "Bloomberg",
                channelNumber = 2,
                isFavorite = true,
                watchlistNames = "Favorites,News",
                streamFormat = "m3u8"
            ),
            ChannelEntity(
                id = "${profileId}_dw_news",
                profileId = profileId,
                name = "DW English HD",
                streamUrl = "https://dwamdstream102.akamaized.net/hls/live/2015525/dwstream102/index.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/7/75/Deutsche_Welle_symbol_2012.svg",
                groupTitle = "News",
                tvgId = "dw.en",
                tvgName = "Deutsche Welle",
                channelNumber = 3,
                isFavorite = false,
                watchlistNames = "News",
                streamFormat = "m3u8"
            ),
            ChannelEntity(
                id = "${profileId}_france24",
                profileId = profileId,
                name = "France 24 English",
                streamUrl = "https://static.france24.com/live/F24_EN_LO_HLS/live_tv.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/6/65/France_24_logo.svg",
                groupTitle = "News",
                tvgId = "france24.en",
                tvgName = "France 24",
                channelNumber = 4,
                isFavorite = false,
                watchlistNames = "News",
                streamFormat = "m3u8"
            ),
            ChannelEntity(
                id = "${profileId}_redbull_tv",
                profileId = profileId,
                name = "Red Bull TV",
                streamUrl = "https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/en/f/f5/Red_Bull_TV_logo.svg",
                groupTitle = "Sports",
                tvgId = "redbull.tv",
                tvgName = "Red Bull TV",
                channelNumber = 5,
                isFavorite = true,
                watchlistNames = "Favorites,Sports",
                streamFormat = "m3u8"
            ),
            ChannelEntity(
                id = "${profileId}_aljazeera",
                profileId = profileId,
                name = "Al Jazeera English HD",
                streamUrl = "https://live-hls-web-aje.getaj.net/AJE/01.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/en/f/f2/Al_Jazeera_English_logo.svg",
                groupTitle = "News",
                tvgId = "aljazeera.en",
                tvgName = "Al Jazeera English",
                channelNumber = 6,
                isFavorite = false,
                watchlistNames = "News",
                streamFormat = "m3u8"
            ),
            ChannelEntity(
                id = "${profileId}_big_buck_bunny",
                profileId = profileId,
                name = "Open Cinema: Big Buck Bunny 4K",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                logoUrl = "https://peach.blender.org/wp-content/uploads/title_bbb_render.jpg",
                groupTitle = "Movies & Cinema",
                tvgId = "bbb.cinema",
                tvgName = "Open Cinema",
                channelNumber = 7,
                isFavorite = true,
                watchlistNames = "Favorites,Movies",
                streamFormat = "mp4"
            ),
            ChannelEntity(
                id = "${profileId}_sintel_stream",
                profileId = profileId,
                name = "Blender Studio: Sintel HD",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                logoUrl = "https://durian.blender.org/wp-content/themes/durian/images/sintel_header.jpg",
                groupTitle = "Movies & Cinema",
                tvgId = "sintel.film",
                tvgName = "Sintel Cinema",
                channelNumber = 8,
                isFavorite = false,
                watchlistNames = "Movies",
                streamFormat = "mp4"
            ),
            ChannelEntity(
                id = "${profileId}_tears_of_steel",
                profileId = profileId,
                name = "Sci-Fi Showcase: Tears of Steel",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                logoUrl = "https://mango.blender.org/wp-content/themes/mango/images/logo_tos.png",
                groupTitle = "Movies & Cinema",
                tvgId = "tos.scifi",
                tvgName = "Sci-Fi Showcase",
                channelNumber = 9,
                isFavorite = false,
                watchlistNames = "Movies",
                streamFormat = "mp4"
            ),
            ChannelEntity(
                id = "${profileId}_nature_stream",
                profileId = profileId,
                name = "Earth & Ocean 4K Nature",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                logoUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=200",
                groupTitle = "Documentary",
                tvgId = "nature.4k",
                tvgName = "Earth 4K",
                channelNumber = 10,
                isFavorite = false,
                watchlistNames = "Documentary",
                streamFormat = "mp4"
            ),
            ChannelEntity(
                id = "${profileId}_elephants_dream",
                profileId = profileId,
                name = "Animation Classic: Elephant's Dream",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                logoUrl = "https://orange.blender.org/wp-content/themes/orange/images/header.jpg",
                groupTitle = "Kids & Animation",
                tvgId = "elephants.anim",
                tvgName = "Animation Classic",
                channelNumber = 11,
                isFavorite = false,
                watchlistNames = "Kids",
                streamFormat = "mp4"
            ),
            ChannelEntity(
                id = "${profileId}_lofi_chill",
                profileId = profileId,
                name = "Lofi Beats & Chill Live",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                logoUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=200",
                groupTitle = "Music",
                tvgId = "lofi.chill",
                tvgName = "Lofi Beats",
                channelNumber = 12,
                isFavorite = true,
                watchlistNames = "Favorites,Music",
                streamFormat = "mp4"
            )
        )
    }

    fun getDemoEpgPrograms(currentTime: Long = System.currentTimeMillis()): List<EpgProgramEntity> {
        val hour = 3600_000L
        val currentSlotStart = (currentTime / hour) * hour
        val list = mutableListOf<EpgProgramEntity>()

        val scheduleMap = mapOf(
            "nasa.hd" to listOf(
                "ISS Expedition Live Feed" to "Live views of Earth from the International Space Station with crew radio communication.",
                "Artemis Deep Space Update" to "Engineers discuss lunar gateway modules and Orion spacecraft telemetry.",
                "Hubble & James Webb Deep Sky Survey" to "High resolution cosmic imagery analysis from deep infrared observatories.",
                "Astronaut Training Centrifuge" to "High-G physiological testing and emergency egress simulation."
            ),
            "bloomberg.us" to listOf(
                "Bloomberg Daybreak Global" to "Market opening bell, futures analysis, bond yields, and commodity movements.",
                "Wall Street Week" to "In-depth conversations with institutional hedge fund managers and tech CEOs.",
                "Bloomberg Technology" to "Silicon Valley venture capital rounds, semiconductor roadmap, and AI chip battles.",
                "The Close & After Hours" to "Closing stock prices, equity index trends, and macro Federal Reserve expectations."
            ),
            "dw.en" to listOf(
                "DW Global News Bulletin" to "International headlines, European diplomatic summits, and global security reports.",
                "Focus on Europe" to "Stories that shape European communities and transcontinental developments.",
                "Tomorrow Today: Science" to "Innovations in green energy, carbon capture, and synthetic biology.",
                "Documentary Special" to "Award-winning investigative journalism across five continents."
            ),
            "france24.en" to listOf(
                "Live International Edition" to "Paris newsroom live dispatch covering diplomatic dialogues and geopolitics.",
                "The 51 Percent" to "Stories of women breaking barriers in politics, journalism, and enterprise.",
                "Reporters in the Field" to "Behind the frontline dispatches and longform documentary essays.",
                "Culture & Cinema Lounge" to "Cannes, European film retrospectives, and global contemporary art."
            ),
            "redbull.tv" to listOf(
                "Downhill Mountain Bike World Cup" to "High-velocity downhill technical trails from Val di Sole.",
                "Cliff Diving World Series" to "30-meter precision acrobatics off coastal cliffs in Polignano a Mare.",
                "Extreme Enduro Erzbergrodeo" to "Unforgiving motorcycle hillclimb over brutal Austrian iron quarries.",
                "Wingsuit Pro Line Flight" to "Aerodynamic proximity flight through Swiss alpine canyons."
            ),
            "aljazeera.en" to listOf(
                "News Hour Live" to "Unrivaled global coverage with specialized insight into emerging markets and regional crises.",
                "Counting the Cost" to "Economic trends impacting daily lives from inflation to energy transition.",
                "Fault Lines Investigation" to "Deep investigative journalism into accountability and corporate oversight.",
                "Witness: Global Shorts" to "Poignant human stories documented by independent filmmakers worldwide."
            ),
            "bbb.cinema" to listOf(
                "Big Buck Bunny: The Original 4K" to "Classic Blender open source animated adventure of the giant gentle rabbit.",
                "Blender Animation Archives" to "Retrospective on open-source cinematic rendering techniques and shaders.",
                "Behind the 3D Render pipeline" to "Lighting, character rigging, and fur simulation deep-dive.",
                "Community Spotlight" to "Open movie festival winners and indie digital short films."
            ),
            "sintel.film" to listOf(
                "Sintel: The Dragon Quest" to "A lonely young warrior searches across barren snowfields for a lost dragon friend.",
                "Durian Studio Chronicles" to "Developing character dynamics and emotional storytelling with open tools.",
                "Orchestral Scoring Suite" to "Live symphony recording sessions and score arrangements for animated films.",
                "VFX Breakdown" to "Compositing, smoke simulations, and texture mapping."
            ),
            "tos.scifi" to listOf(
                "Tears of Steel: Amsterdam 2048" to "A dystopian future battle in Amsterdam reconstructed with live action and VFX.",
                "Mango Project VFX Breakdown" to "Camera tracking, green screen keying, and giant robotic tentacles modeling.",
                "Sci-Fi Sound Design Lab" to "Synthesizing futuristic electromagnetic weapons and mechanical movement audio.",
                "Future FX Masterclass" to "Procedural destruction systems and cinematic grading techniques."
            ),
            "nature.4k" to listOf(
                "Coral Reef Ecosystems in Ultra-HD" to "Bioluminescent marine organisms across the Pacific Indo-Trench.",
                "Apex Predators of the Serengeti" to "High-speed camera tracking lions and cheetahs during migration seasons.",
                "Deep Amazon Rainforest Canopy" to "Rare flora and uncatalogued avian species deep in untouched river basins.",
                "Glacier Dynamics: Arctic Ice" to "Time-lapse documentation of calving icebergs and polar geography."
            ),
            "elephants.anim" to listOf(
                "Elephants Dream (Remastered)" to "Proog and Emo explore the surreal, mechanical labyrinth of an infinite machine.",
                "Open Source Digital Art" to "Pioneering the open media movement and collaborative worldwide digital cinema.",
                "Surrealist Animation Heritage" to "Influence of cybernetic aesthetics in digital art.",
                "Voice Acting & Script Readings" to "Original vocal performances for mechanical dreamscapes."
            ),
            "lofi.chill" to listOf(
                "Midnight Study Sessions" to "Smooth jazz chords, vinyl crackle, and soothing mellow downtempo beats.",
                "Tokyo Neon Rain Chillout" to "Ambient synth pads and gentle percussion for coding and relaxation.",
                "Coffee Shop Acoustic Vibe" to "Warm guitar loops and relaxed instrumental lo-fi textures.",
                "Deep Focus Horizon" to "Minimalist downtempo rhythms designed for immersion and concentration."
            )
        )

        scheduleMap.forEach { (tvgId, programs) ->
            programs.forEachIndexed { index, (title, desc) ->
                val start = currentSlotStart + (index - 1) * hour
                val end = start + hour
                list.add(
                    EpgProgramEntity(
                        id = "${tvgId}_${start}",
                        channelTvgId = tvgId,
                        title = title,
                        description = desc,
                        startTimeEpoch = start,
                        endTimeEpoch = end,
                        category = "General"
                    )
                )
            }
        }

        return list
    }

    fun getDemoMovies(profileId: Long = DEMO_PROFILE_ID): List<com.example.data.model.MovieEntity> {
        return listOf(
            com.example.data.model.MovieEntity(
                id = "${profileId}_bbb_movie",
                profileId = profileId,
                title = "Big Buck Bunny (4K Remastered)",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&auto=format&fit=crop&q=80",
                genre = "Animation",
                rating = "8.6",
                releaseYear = "2024",
                duration = "10m",
                plot = "A large and lovable gentle giant rabbit deals with bullying forest critters in this groundbreaking open cinema animation masterpiece.",
                cast = "Blender Animation Studio",
                isFavorite = true,
                containerExtension = "mp4"
            ),
            com.example.data.model.MovieEntity(
                id = "${profileId}_tos_movie",
                profileId = profileId,
                title = "Tears of Steel (Sci-Fi VFX)",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                posterUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&auto=format&fit=crop&q=80",
                genre = "Sci-Fi",
                rating = "8.8",
                releaseYear = "2023",
                duration = "12m",
                plot = "Set in a dystopian future Amsterdam, a squad of elite cybernetic warriors and physicists strive to rescue humankind from an autonomous robotic siege.",
                cast = "Derek de Lint, Sergio Hasselbaink, Vanja Rukavina",
                isFavorite = true,
                containerExtension = "mp4"
            ),
            com.example.data.model.MovieEntity(
                id = "${profileId}_sintel_movie",
                profileId = profileId,
                title = "Sintel: Dragon Quest (4K)",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                posterUrl = "https://images.unsplash.com/photo-1514539079130-25950c84af65?w=800&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=1200&auto=format&fit=crop&q=80",
                genre = "Fantasy",
                rating = "8.9",
                releaseYear = "2023",
                duration = "15m",
                plot = "A lonely young warrior traverses treacherous blizzards and mountain peaks in search of an orphaned baby dragon who was abducted by a colossal beast.",
                cast = "Halina Reijn, Thom Hoffman",
                isFavorite = false,
                containerExtension = "mp4"
            ),
            com.example.data.model.MovieEntity(
                id = "${profileId}_elephants_movie",
                profileId = profileId,
                title = "Elephants Dream (Cyberpunk)",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1200&auto=format&fit=crop&q=80",
                genre = "Sci-Fi",
                rating = "8.2",
                releaseYear = "2022",
                duration = "11m",
                plot = "Two travelers journey through the surreal and mechanical inner architecture of an infinite, self-replicating supercomputer labyrinth.",
                cast = "Tygo Gernandt, Cas Jansen",
                isFavorite = false,
                containerExtension = "mp4"
            ),
            com.example.data.model.MovieEntity(
                id = "${profileId}_night_living_dead",
                profileId = profileId,
                title = "Night of the Living Dead",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                posterUrl = "https://images.unsplash.com/photo-1509281373149-e957c6296406?w=800&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=1200&auto=format&fit=crop&q=80",
                genre = "Horror",
                rating = "8.6",
                releaseYear = "2022",
                duration = "1h 36m",
                plot = "A ragtag group of terrified survivors barricade themselves in a rural farmhouse to defend against reanimated dead rising across the country.",
                cast = "Duane Jones, Judith O'Dea, Karl Hardman",
                isFavorite = false,
                containerExtension = "mp4"
            ),
            com.example.data.model.MovieEntity(
                id = "${profileId}_the_general",
                profileId = profileId,
                title = "The General (Restored)",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                posterUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?w=1200&auto=format&fit=crop&q=80",
                genre = "Comedy",
                rating = "8.7",
                releaseYear = "2021",
                duration = "1h 18m",
                plot = "One of cinema's most acclaimed silent comedy masterpieces, featuring iconic locomotive stunts, slapstick comedy, and breathtaking practical effects.",
                cast = "Buster Keaton, Marion Mack",
                isFavorite = false,
                containerExtension = "mp4"
            ),
            com.example.data.model.MovieEntity(
                id = "${profileId}_charade",
                profileId = profileId,
                title = "Charade (4K Restored)",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                posterUrl = "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?w=800&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=1200&auto=format&fit=crop&q=80",
                genre = "Mystery",
                rating = "8.5",
                releaseYear = "2021",
                duration = "1h 53m",
                plot = "A suspense romantic mystery in Paris as a stylish young widow is pursued by dangerous operatives after a stolen fortune.",
                cast = "Cary Grant, Audrey Hepburn, Walter Matthau",
                isFavorite = true,
                containerExtension = "mp4"
            ),
            com.example.data.model.MovieEntity(
                id = "${profileId}_subaru_odyssey",
                profileId = profileId,
                title = "Horizon Expedition (Documentary)",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackSeeTheWorld.mp4",
                posterUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&auto=format&fit=crop&q=80",
                genre = "Action",
                rating = "8.4",
                releaseYear = "2023",
                duration = "45m",
                plot = "An awe-inspiring expedition into extreme wilderness terrains, tracing geological wonders and survival against harsh natural elements.",
                cast = "National Expedition League",
                isFavorite = false,
                containerExtension = "mp4"
            )
        )
    }

    fun getDemoSeries(profileId: Long = DEMO_PROFILE_ID): List<com.example.data.model.SeriesEntity> {
        return listOf(
            com.example.data.model.SeriesEntity(
                id = "${profileId}_pioneer_one",
                profileId = profileId,
                title = "Pioneer One",
                posterUrl = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=800&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=1200&auto=format&fit=crop&q=80",
                genre = "Sci-Fi",
                rating = "8.9",
                releaseYear = "2023",
                plot = "An anomalous object deorbits over North America, carrying high levels of radiation and revealing the return of a forgotten Cold War Soviet spacecraft.",
                cast = "James Rich, Alexandra Turshen, Jack Turner",
                seasonsCount = 1,
                isFavorite = true
            ),
            com.example.data.model.SeriesEntity(
                id = "${profileId}_sherlock_holmes",
                profileId = profileId,
                title = "Sherlock Holmes Classic",
                posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=800&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?w=1200&auto=format&fit=crop&q=80",
                genre = "Mystery",
                rating = "8.7",
                releaseYear = "2022",
                plot = "Sherlock Holmes and Dr. John H. Watson unravel Victorian London's most baffling and enigmatic crimes from their parlor at 221B Baker Street.",
                cast = "Ronald Howard, Howard Marion-Crawford",
                seasonsCount = 1,
                isFavorite = false
            ),
            com.example.data.model.SeriesEntity(
                id = "${profileId}_space_odyssey",
                profileId = profileId,
                title = "Cosmic Horizons: Space Odyssey",
                posterUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=80",
                backdropUrl = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=1200&auto=format&fit=crop&q=80",
                genre = "Documentary",
                rating = "9.2",
                releaseYear = "2024",
                plot = "Ultra-high-definition journeys across planetary rings, supermassive black holes, and the search for habitable exoplanets.",
                cast = "Astrophysics Observatory Team",
                seasonsCount = 1,
                isFavorite = true
            )
        )
    }

    fun getDemoEpisodes(profileId: Long = DEMO_PROFILE_ID): List<com.example.data.model.EpisodeEntity> {
        return listOf(
            // Pioneer One episodes
            com.example.data.model.EpisodeEntity(
                id = "p1_s1e1",
                seriesId = "${profileId}_pioneer_one",
                seasonNumber = 1,
                episodeNumber = 1,
                title = "Earthfall",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                thumbnail = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=800&auto=format&fit=crop&q=80",
                plot = "A radioactive anomaly crashes in Montana. Homeland Security uncovers a Soviet space capsule thought lost for decades.",
                duration = "42m"
            ),
            com.example.data.model.EpisodeEntity(
                id = "p1_s1e2",
                seriesId = "${profileId}_pioneer_one",
                seasonNumber = 1,
                episodeNumber = 2,
                title = "The Man From Norilsk",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                thumbnail = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=80",
                plot = "Investigation reveals the mysterious pilot exhibits cellular markers indicating deep cryo-hibernation in interplanetary orbit.",
                duration = "44m"
            ),
            com.example.data.model.EpisodeEntity(
                id = "p1_s1e3",
                seriesId = "${profileId}_pioneer_one",
                seasonNumber = 1,
                episodeNumber = 3,
                title = "Alone in the Night",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4",
                thumbnail = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80",
                plot = "Diplomatic tensions mount worldwide as orbital telemetry pings confirm a second signal origin near the Martian orbit.",
                duration = "46m"
            ),
            com.example.data.model.EpisodeEntity(
                id = "p1_s1e4",
                seriesId = "${profileId}_pioneer_one",
                seasonNumber = 1,
                episodeNumber = 4,
                title = "Triangulation",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
                thumbnail = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800&auto=format&fit=crop&q=80",
                plot = "The science investigation team decrypts telemetry records that reveal the unexpected final coordinates of the Pioneer mission.",
                duration = "48m"
            ),

            // Sherlock Holmes episodes
            com.example.data.model.EpisodeEntity(
                id = "sh_s1e1",
                seriesId = "${profileId}_sherlock_holmes",
                seasonNumber = 1,
                episodeNumber = 1,
                title = "The Cunningham Heritage",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                thumbnail = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=800&auto=format&fit=crop&q=80",
                plot = "Holmes and Watson meet at 221B Baker Street and investigate a high-stakes estate inheritance fraud.",
                duration = "26m"
            ),
            com.example.data.model.EpisodeEntity(
                id = "sh_s1e2",
                seriesId = "${profileId}_sherlock_holmes",
                seasonNumber = 1,
                episodeNumber = 2,
                title = "The Case of Lady Beryl",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4",
                thumbnail = "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?w=800&auto=format&fit=crop&q=80",
                plot = "A notorious jewel theft leads to an escalating web of deception and blackmail inside British high society.",
                duration = "27m"
            ),
            com.example.data.model.EpisodeEntity(
                id = "sh_s1e3",
                seriesId = "${profileId}_sherlock_holmes",
                seasonNumber = 1,
                episodeNumber = 3,
                title = "The Texas Cowgirl",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                thumbnail = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800&auto=format&fit=crop&q=80",
                plot = "An American rodeo performer touring in London turns to Holmes when framed for an international espionage scheme.",
                duration = "28m"
            ),

            // Space Odyssey episodes
            com.example.data.model.EpisodeEntity(
                id = "so_s1e1",
                seriesId = "${profileId}_space_odyssey",
                seasonNumber = 1,
                episodeNumber = 1,
                title = "Birth of Radiant Stars",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                thumbnail = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=80",
                plot = "Traversing stellar nebulae where cosmic gas and gravity ignite thermonuclear fusion to birth radiant suns.",
                duration = "38m"
            ),
            com.example.data.model.EpisodeEntity(
                id = "so_s1e2",
                seriesId = "${profileId}_space_odyssey",
                seasonNumber = 1,
                episodeNumber = 2,
                title = "Rings of Wonder",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                thumbnail = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80",
                plot = "Investigating the ice particles of Saturn's ring system and subterranean oceans on Europa and Enceladus.",
                duration = "41m"
            ),
            com.example.data.model.EpisodeEntity(
                id = "so_s1e3",
                seriesId = "${profileId}_space_odyssey",
                seasonNumber = 1,
                episodeNumber = 3,
                title = "Beyond the Event Horizon",
                streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                thumbnail = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?w=800&auto=format&fit=crop&q=80",
                plot = "Exploring relativistic time dilation, gravitational lensing, and extreme singularities at the edge of known physics.",
                duration = "45m"
            )
        )
    }
}
