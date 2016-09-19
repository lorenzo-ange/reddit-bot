
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