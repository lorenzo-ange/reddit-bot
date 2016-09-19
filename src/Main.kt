import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage: RedditBot [subreddit]")
        return
    }

    val subreddit = args[0]
    val notificationSender = NotificationSender()

    val redditBot = Runnable {
        val latestPost = Post.getLatest(subreddit).first()
        if (latestPost.postedInLastFiveMins()) {
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
