import com.twilio.sdk.TwilioRestClient
import com.twilio.sdk.resource.instance.Message
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import java.util.*

class NotificationSender {
    private val props = Props.retrieve()

    fun send(text: String): Message {
        val client = TwilioRestClient(props.twilioAccountSid, props.twilioAuthToken)

        val params = ArrayList<NameValuePair>()
        params.add(BasicNameValuePair("To", props.myPhone))
        params.add(BasicNameValuePair("From", props.twilioTrialPhone))
        params.add(BasicNameValuePair("Body", text))

        val messageFactory = client.account.getMessageFactory()
        return messageFactory.create(params)
    }
}
