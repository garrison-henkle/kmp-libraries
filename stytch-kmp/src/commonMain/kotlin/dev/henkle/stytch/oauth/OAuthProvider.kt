package dev.henkle.stytch.oauth

enum class OAuthProvider {
    Amazon,
    Apple,
    Bitbucket,
    Coinbase,
    Discord,
    Facebook,
    Figma,
    GitHub,
    GitLab,
    Google,
    GoogleOneTap,
    LinkedIn,
    Microsoft,
    Salesforce,
    Slack,
    Snapchat,
    Spotify,
    TikTok,
    Twitch,
    Twitter,
    Yahoo,
    ;

    val lowercaseName: String = name.lowercase()
}
