import com.twilio.sdk.TwilioRestClient
import com.twilio.sdk.resource.instance.Message
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import java.util.*

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
