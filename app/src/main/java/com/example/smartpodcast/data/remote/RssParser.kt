package com.example.smartpodcast.data.remote

import com.example.smartpodcast.data.local.EpisodeEntity
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
// Optimized RSS Parser
class RssParser {
    fun parse(inputStream: InputStream): List<EpisodeEntity> {
        val episodes = mutableListOf<EpisodeEntity>()
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var eventType = parser.eventType
        var currentTitle = ""
        var currentAudio = ""
        var currentDesc = ""
        var currentImg = "https://picsum.photos/200" // Ảnh mặc định

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val name = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (name == "enclosure") {
                        currentAudio = parser.getAttributeValue(null, "url") ?: ""
                    }
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text.trim()
                    if (text.isNotEmpty()) {
                        // Logic đơn giản để lấy text
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (name) {
                        "title" -> currentTitle = parser.text ?: currentTitle
                        "item" -> {
                            if (currentAudio.isNotEmpty()) {
                                episodes.add(EpisodeEntity(
                                    id = currentAudio,
                                    title = currentTitle,
                                    description = "",
                                    audioUrl = currentAudio,
                                    imageUrl = "https://picsum.photos/seed/${currentTitle.hashCode()}/300",
                                    pubDate = ""
                                ))
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return episodes
    }
}