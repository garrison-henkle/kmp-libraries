package dev.henkle.stytch.otp

import dev.henkle.stytch.model.StytchResult
import dev.henkle.stytch.model.otp.OTPAuthenticateParameters
import dev.henkle.stytch.model.otp.OTPEmailParameters
import dev.henkle.stytch.model.otp.OTPPhoneParameters
import dev.henkle.stytch.model.otp.request.OTPAuthenticateRequest
import dev.henkle.stytch.model.otp.request.OTPEmailRequest
import dev.henkle.stytch.model.otp.request.OTPPhoneRequest
import dev.henkle.stytch.model.otp.response.OTPAuthenticateResponseData
import dev.henkle.stytch.model.otp.response.OTPSendResponseData

interface OTP {
    val sms: PhoneProvider
    val whatsApp: PhoneProvider
    val email: EmailProvider

    suspend fun authenticate(
        methodId: String,
        code: String,
        sessionDurationMin: UInt? = null,
    ): StytchResult<OTPAuthenticateResponseData>

    suspend fun authenticate(
        parameters: OTPAuthenticateParameters,
    ): StytchResult<OTPAuthenticateResponseData>

    interface EmailProvider {
        suspend fun send(
            email: String,
            expirationMin: UInt? = null,
            loginTemplateId: String? = null,
            signupTemplateId: String? = null,
        ): StytchResult<OTPSendResponseData>

        suspend fun send(
            parameters: OTPEmailParameters,
        ): StytchResult<OTPSendResponseData>

        suspend fun loginOrCreate(
            email: String,
            expirationMin: UInt? = null,
            loginTemplateId: String? = null,
            signupTemplateId: String? = null,
        ): StytchResult<OTPSendResponseData>

        suspend fun loginOrCreate(
            parameters: OTPEmailParameters,
        ): StytchResult<OTPSendResponseData>
    }

    interface PhoneProvider {
        suspend fun send(
            phoneNumber: String,
            expirationMin: UInt? = null,
        ): StytchResult<OTPSendResponseData>

        suspend fun send(
            parameters: OTPPhoneParameters,
        ): StytchResult<OTPSendResponseData>

        suspend fun loginOrCreate(
            phoneNumber: String,
            expirationMin: UInt? = null,
        ): StytchResult<OTPSendResponseData>

        suspend fun loginOrCreate(
            parameters: OTPPhoneParameters,
        ): StytchResult<OTPSendResponseData>
    }
}
