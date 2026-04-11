package com.example.smartpodcast.data.remote

import android.util.Log
import com.example.smartpodcast.data.local.EpisodeEntity
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

class RssParser {
    fun parse(inputStream: InputStream): List<EpisodeEntity> {
        val episodes = mutableListOf<EpisodeEntity>()
        val factory = XmlPullParserFactory.newInstance()
<<<<<<< Updated upstream
        factory.isNamespaceAware = true // Bắt buộc để đọc ảnh itunes:image
=======
        factory.isNamespaceAware = true 
>>>>>>> Stashed changes
        val parser = factory.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var eventType = parser.eventType
<<<<<<< Updated upstream
        var title = ""; var audio = ""; var image = ""; var desc = ""
        var text = ""
=======
        var title = ""
        var description = ""
        var audioUrl = ""
        var imageUrl = ""
        var pubDate = ""
        var text = ""
        var isInsideItem = false
        var podcastImageUrl = ""
>>>>>>> Stashed changes

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
<<<<<<< Updated upstream
                    if (tagName == "enclosure") {
                        audio = parser.getAttributeValue(null, "url") ?: ""
                    }
                    if (tagName == "image" && parser.prefix == "itunes") {
                        image = parser.getAttributeValue(null, "href") ?: ""
=======
                    if (tagName == "item") {
                        isInsideItem = true
                    } else if (tagName == "enclosure" && isInsideItem) {
                        audioUrl = parser.getAttributeValue(null, "url") ?: ""
                    } else if (tagName == "image") {
                        // Get image from itunes:image or standard image
                        val href = parser.getAttributeValue(null, "href")
                        if (href != null) {
                            if (!isInsideItem) podcastImageUrl = href
                            else imageUrl = href
                        }
>>>>>>> Stashed changes
                    }
                }
                XmlPullParser.TEXT -> text = parser.text.trim()
                XmlPullParser.END_TAG -> {
<<<<<<< Updated upstream
                    when (tagName) {
                        "title" -> title = text
                        "description" -> desc = text
                        "item" -> {
                            if (audio.isNotEmpty()) {
                                episodes.add(EpisodeEntity(audio, title, desc, audio, image, "Podcast"))
=======
                    if (isInsideItem) {
                        when (tagName) {
                            "title" -> title = text
                            "description", "summary" -> description = text
                            "pubDate" -> pubDate = text
                            "item" -> {
                                if (audioUrl.isNotEmpty()) {
                                    episodes.add(EpisodeEntity(
                                        id = audioUrl,
                                        title = title,
                                        description = description,
                                        audioUrl = audioUrl,
                                        imageUrl = if (imageUrl.isNotEmpty()) imageUrl else podcastImageUrl,
                                        pubDate = pubDate
                                    ))
                                }
                                // Reset for next item
                                title = ""; description = ""; audioUrl = ""; imageUrl = ""; pubDate = ""; isInsideItem = false
>>>>>>> Stashed changes
                            }
                            // Reset biến tạm
                            title = ""; audio = ""; image = ""; desc = ""
                        }
                    } else if (tagName == "title" && title.isEmpty()) {
                        // This is channel title, can be ignored or stored
                    }
                }
            }
            eventType = parser.next()
        }
        Log.d("RSS_PARSER", "Successfully parsed ${episodes.size} episodes")
        return episodes
    }
}
