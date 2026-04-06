package com.example.smartpodcast.data.remote

import com.example.smartpodcast.data.local.EpisodeEntity
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream

class RssParser {
    fun parse(inputStream: InputStream): List<EpisodeEntity> {
        val episodes = mutableListOf<EpisodeEntity>()
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var eventType = parser.eventType
        var currentEpisode = mutableMapOf<String, String>()
        var text = ""

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            when (eventType) {
                XmlPullParser.TEXT -> text = parser.text
                XmlPullParser.END_TAG -> {
                    when {
                        tagName.equals("title", true) -> currentEpisode["title"] = text
                        tagName.equals("description", true) -> currentEpisode["desc"] = text
                        tagName.equals("pubDate", true) -> currentEpisode["date"] = text
                        tagName.equals("guid", true) -> currentEpisode["url"] = text
                        tagName.equals("item", true) -> {
                            episodes.add(EpisodeEntity(
                                id = currentEpisode["url"] ?: "",
                                title = currentEpisode["title"] ?: "No Title",
                                description = currentEpisode["desc"] ?: "",
                                audioUrl = currentEpisode["url"] ?: "",
                                imageUrl = "https://picsum.photos/seed/${currentEpisode["title"].hashCode()}/200",
                                pubDate = currentEpisode["date"] ?: ""
                            ))
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return episodes
    }
}