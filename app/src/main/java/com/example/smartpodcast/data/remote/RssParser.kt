package com.example.smartpodcast.data.remote

import com.example.smartpodcast.data.local.EpisodeEntity
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

class RssParser {
    fun parse(inputStream: InputStream): List<EpisodeEntity> {
        val episodes = mutableListOf<EpisodeEntity>()
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true // Bắt buộc để đọc ảnh itunes:image
        val parser = factory.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var eventType = parser.eventType
        var title = ""; var audio = ""; var image = ""; var desc = ""
        var text = ""

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (tagName == "enclosure") {
                        audio = parser.getAttributeValue(null, "url") ?: ""
                    }
                    if (tagName == "image" && parser.prefix == "itunes") {
                        image = parser.getAttributeValue(null, "href") ?: ""
                    }
                }
                XmlPullParser.TEXT -> text = parser.text.trim()
                XmlPullParser.END_TAG -> {
                    when (tagName) {
                        "title" -> title = text
                        "description" -> desc = text
                        "item" -> {
                            if (audio.isNotEmpty()) {
                                episodes.add(EpisodeEntity(audio, title, desc, audio, image, "Podcast"))
                            }
                            // Reset biến tạm
                            title = ""; audio = ""; image = ""; desc = ""
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return episodes
    }
}