import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import com.twilio.sdk.*
import com.twilio.sdk.resource.instance.Message
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


data class Post(val title: String, val url: String, val postedAt: ZonedDateTime)

fun getLatestPosts(subreddit: String): List<Post> {
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

data class Props(val twilioAccountSid: String, val twilioAuthToken: String, val myPhone: String, val twilioTrialPhone: String) {
    companion object {
        fun retrieve(): Props {
            val configFile = Props::class.java.getResourceAsStream("/config.properties")
            val props = configFile
                    .bufferedReader()
                    .readLines()
                    .map { it.split("=") }
                    .associateBy({ it[0] }, { it[1] })

            fun retrieveProp(name: String) = props[name] ?: throw Throwable("$name not found in config.properties")

            val twilioAccountSid = retrieveProp("twilio_account_sid")
            val twilioAuthToken = retrieveProp("twilio_auth_token")
            val myPhone = retrieveProp("my_phone")
            val twilioTrialPhone = retrieveProp("twilio_trial_phone")
            return Props(twilioAccountSid, twilioAuthToken, myPhone, twilioTrialPhone)
        }
    }
}

class NotificationSender {
    private val props = Props.retrieve()

    private fun sendTwilioSms(accountSid: String, authToken: String, to: String, from: String, body: String): Message {

        val client = TwilioRestClient(accountSid, authToken)

        val params = ArrayList<NameValuePair>()
        params.add(BasicNameValuePair("To", to))
        params.add(BasicNameValuePair("From", from))
        params.add(BasicNameValuePair("Body", body))

        val messageFactory = client.account.getMessageFactory()
        return messageFactory.create(params)
    }

    fun send(text: String) {
        sendTwilioSms(props.twilioAccountSid, props.twilioAuthToken, props.myPhone, props.twilioTrialPhone, text)
    }
}

fun postedInLastFiveMins(post: Post) =
        ZonedDateTime.now()
                .minusSeconds(5) // Correction factor: the script takes some time to execute
                .minusMinutes(5)
                .isBefore(post.postedAt)


fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage: RedditBot [subreddit]")
        return
    }

    val subreddit = args[0]
    val notificationSender = NotificationSender()

    val redditBot = Runnable {
        val latestPost = getLatestPosts(subreddit).first()
        if (postedInLastFiveMins(latestPost)) {
            println("New post in '$subreddit'! ${latestPost.title} - ${latestPost.url}")
            notificationSender.send("New post in '$subreddit'! ${latestPost.title} - ${latestPost.url}")
        }
        else {
            println("Nothing new in '$subreddit'...")
        }
    }

    val executor = Executors.newScheduledThreadPool(1)
    executor.scheduleAtFixedRate(redditBot, 0, 5, TimeUnit.MINUTES)
}
