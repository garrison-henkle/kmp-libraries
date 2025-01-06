package dev.henkle.stytch.otp

import dev.henkle.stytch.model.StytchAuthResponseData
import dev.henkle.stytch.model.StytchResult
import dev.henkle.stytch.model.otp.OTPAuthenticateParameters
import dev.henkle.stytch.model.otp.OTPEmailParameters
import dev.henkle.stytch.model.otp.OTPPhoneParameters
import dev.henkle.stytch.model.otp.request.OTPAuthenticateRequest
import dev.henkle.stytch.model.otp.request.OTPEmailRequest
import dev.henkle.stytch.model.otp.request.OTPPhoneRequest
import dev.henkle.stytch.model.otp.response.OTPAuthenticateResponseData
import dev.henkle.stytch.model.otp.response.OTPSendResponseData
import dev.henkle.stytch.model.sdk.Config
import dev.henkle.stytch.utils.StytchHTTPClient
import dev.henkle.stytch.utils.ext.ifSuccessfulAuth

internal class OTPImpl(
    private val client: StytchHTTPClient,
    private val config: Config,
    private val onAuthenticate: (StytchAuthResponseData) -> Unit,
) : OTP {
    override val email = EmailOTPProvider(client = client, config = config)
    override val sms = SMSOTPProvider(client = client, config = config)
    override val whatsApp = WhatsAppOTPProvider(client = client, config = config)

    override suspend fun authenticate(
        methodId: String,
        code: String,
        sessionDurationMin: UInt?,
    ): StytchResult<OTPAuthenticateResponseData> = authenticate(
        parameters = OTPAuthenticateParameters(
            methodId = methodId,
            token = code,
            sessionDurationMin = sessionDurationMin ?: config.sessionDurationMin,
        ),
    )

    override suspend fun authenticate(
        parameters: OTPAuthenticateParameters,
    ): StytchResult<OTPAuthenticateResponseData> = client.post<OTPAuthenticateRequest, OTPAuthenticateResponseData>(
        path = PATH_AUTHENTICATE,
        body = OTPAuthenticateRequest(
            methodId = parameters.methodId,
            token = parameters.token,
            sessionDurationMin = parameters.sessionDurationMin,
        ),
    ).ifSuccessfulAuth(perform = onAuthenticate)

    internal class EmailOTPProvider(
        private val client: StytchHTTPClient,
        private val config: Config,
    ) : OTP.EmailProvider {
        override suspend fun loginOrCreate(
            email: String,
            expirationMin: UInt?,
            loginTemplateId: String?,
            signupTemplateId: String?,
        ): StytchResult<OTPSendResponseData> = loginOrCreate(
            parameters = OTPEmailParameters(
                email = email,
                expirationMin = expirationMin ?: config.otpExpirationMinutes,
                loginTemplateId = loginTemplateId,
                signupTemplateId = signupTemplateId,
            ),
        )

        override suspend fun loginOrCreate(
            parameters: OTPEmailParameters,
        ): StytchResult<OTPSendResponseData> = client.post(
            path = PATH_EMAIL_LOGIN_OR_CREATE,
            body = OTPEmailRequest(
                email = parameters.email,
                expirationMin = parameters.expirationMin,
                loginTemplateId = parameters.loginTemplateId,
                signupTemplateId = parameters.signupTemplateId,
            ),
        )

        override suspend fun send(
            email: String,
            expirationMin: UInt?,
            loginTemplateId: String?,
            signupTemplateId: String?,
        ): StytchResult<OTPSendResponseData> = send(
            parameters = OTPEmailParameters(
                email = email,
                expirationMin = expirationMin ?: config.otpExpirationMinutes,
                loginTemplateId = loginTemplateId,
                signupTemplateId = signupTemplateId,
            ),
        )

        override suspend fun send(
            parameters: OTPEmailParameters,
        ): StytchResult<OTPSendResponseData> = client.post(
            path = PATH_EMAIL_SEND,
            body = OTPEmailRequest(
                email = parameters.email,
                expirationMin = parameters.expirationMin,
                loginTemplateId = parameters.loginTemplateId,
                signupTemplateId = parameters.signupTemplateId,
            ),
        )
    }

    internal class SMSOTPProvider(
        private val client: StytchHTTPClient,
        private val config: Config,
    ) : OTP.PhoneProvider {
        override suspend fun loginOrCreate(
            phoneNumber: String,
            expirationMin: UInt?,
        ): StytchResult<OTPSendResponseData> = loginOrCreate(
            parameters = OTPPhoneParameters(
                phoneNumber = phoneNumber,
                expirationMin = expirationMin ?: config.otpExpirationMinutes,
            ),
        )

        override suspend fun loginOrCreate(
            parameters: OTPPhoneParameters,
        ): StytchResult<OTPSendResponseData> = client.post(
            path = PATH_SMS_LOGIN_OR_CREATE,
            body = OTPPhoneRequest(
                phoneNumber = parameters.phoneNumber,
                expirationMin = parameters.expirationMin,
            ),
        )

        override suspend fun send(
            phoneNumber: String,
            expirationMin: UInt?,
        ): StytchResult<OTPSendResponseData> = send(
            parameters = OTPPhoneParameters(
                phoneNumber = phoneNumber,
                expirationMin = expirationMin ?: config.otpExpirationMinutes,
            ),
        )

        override suspend fun send(
            parameters: OTPPhoneParameters,
        ): StytchResult<OTPSendResponseData> = client.post(
            path = PATH_SMS_SEND,
            body = OTPPhoneRequest(
                phoneNumber = parameters.phoneNumber,
                expirationMin = parameters.expirationMin,
            ),
        )
    }

    internal class WhatsAppOTPProvider(
        private val client: StytchHTTPClient,
        private val config: Config,
    ) : OTP.PhoneProvider {
        override suspend fun loginOrCreate(
            phoneNumber: String,
            expirationMin: UInt?,
        ): StytchResult<OTPSendResponseData> = loginOrCreate(
            parameters = OTPPhoneParameters(
                phoneNumber = phoneNumber,
                expirationMin = expirationMin ?: config.otpExpirationMinutes,
            ),
        )

        override suspend fun loginOrCreate(
            parameters: OTPPhoneParameters,
        ): StytchResult<OTPSendResponseData> = client.post(
            path = PATH_WHATSAPP_LOGIN_OR_CREATE,
            body = OTPPhoneRequest(
                phoneNumber = parameters.phoneNumber,
                expirationMin = parameters.expirationMin,
            ),
        )

        override suspend fun send(
            phoneNumber: String,
            expirationMin: UInt?,
        ): StytchResult<OTPSendResponseData> = send(
            parameters = OTPPhoneParameters(
                phoneNumber = phoneNumber,
                expirationMin = expirationMin ?: config.otpExpirationMinutes,
            ),
        )

        override suspend fun send(
            parameters: OTPPhoneParameters,
        ): StytchResult<OTPSendResponseData> = client.post(
            path = PATH_WHATSAPP_SEND,
            body = OTPPhoneRequest(
                phoneNumber = parameters.phoneNumber,
                expirationMin = parameters.expirationMin,
            ),
        )
    }

    companion object {
        private const val PATH_SMS_SEND = "otps/sms/send"
        private const val PATH_SMS_LOGIN_OR_CREATE = "otps/sms/login_or_create"
        private const val PATH_WHATSAPP_SEND = "otps/whatsapp/send"
        private const val PATH_WHATSAPP_LOGIN_OR_CREATE = "otps/whatsapp/login_or_create"
        private const val PATH_EMAIL_SEND = "otps/email/send"
        private const val PATH_EMAIL_LOGIN_OR_CREATE = "otps/email/login_or_create"
        private const val PATH_AUTHENTICATE = "otps/authenticate"
    }
}
