import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class Post(val title: String, val url: String, val postedAt: ZonedDateTime) {
    companion object {
        fun getLatest(subreddit: String): List<Post> {
            fun parseThing(div: Element): Post {
                val titleLink = div.select("a.title").first()
                val title = titleLink.text()

                fun completeUrl(url: String) = if (url.startsWith("http")) url else "https://www.reddit.com$url"
                val url = completeUrl(titleLink.attr("href"))

                val rawDatetime = div.select("time").first().attr("datetime")
                val datetime = ZonedDateTime.parse(rawDatetime, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

                return Post(title, url, datetime)
            }

            val doc = Jsoup
                    .connect("https://www.reddit.com/r/$subreddit/new/")
                    // Sorry Reddit for faking my user agent but unless you are not allowing me to work ;)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get()

            return doc.select("#siteTable .thing").map { parseThing(it) }
        }
    }

    fun postedInLastFiveMins() =
            ZonedDateTime.now()
                    // Correction factor: the script takes some time to execute
                    .minusSeconds(5)
                    .minusMinutes(5)
                    .isBefore(postedAt)

}