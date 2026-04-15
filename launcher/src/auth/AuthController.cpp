#include "AuthController.h"

#include <QNetworkAccessManager>
#include <QNetworkRequest>
#include <QNetworkReply>
#include <QJsonDocument>
#include <QJsonObject>
#include <QJsonArray>
#include <QUrlQuery>
#include <QUrl>
#include <QFile>
#include <QDir>
#include <QStandardPaths>
#include <QCoreApplication>
#include <QGuiApplication>
#include <QClipboard>

// ---------------------------------------------------------------------------
// Client ID — loaded from ~/.config/sheik/launcher.json { "client_id": "..." }
//
// To get a client_id:
//   1. Go to https://portal.azure.com → Azure Active Directory → App registrations
//   2. New registration → any name, account type: "Personal Microsoft accounts only"
//   3. Platform: "Mobile and desktop application"
//      Redirect URI: https://login.microsoftonline.com/native/client  (or leave blank)
//   4. Authentication → enable "Allow public client flows" (Device code flow)
//   5. API permissions → Add → Microsoft Graph or APIs my organisation uses →
//      search "XboxLive.signin", add it (delegated).
//   6. Copy the "Application (client) ID" and put it in ~/.config/sheik/launcher.json:
//        { "client_id": "<your-guid>" }
// ---------------------------------------------------------------------------
static const QString DEVICE_CODE_URL = QStringLiteral("https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode");
static const QString TOKEN_URL       = QStringLiteral("https://login.microsoftonline.com/consumers/oauth2/v2.0/token");
static const QString XBL_URL         = QStringLiteral("https://user.auth.xboxlive.com/user/authenticate");
static const QString XSTS_URL        = QStringLiteral("https://xsts.auth.xboxlive.com/xsts/authorize");
static const QString MC_AUTH_URL     = QStringLiteral("https://api.minecraftservices.com/authentication/login_with_xbox");
static const QString MC_PROFILE_URL  = QStringLiteral("https://api.minecraftservices.com/minecraft/profile");

AuthController::AuthController(QObject *parent)
    : QObject(parent)
    , m_nam(new QNetworkAccessManager(this))
    , m_pollTimer(new QTimer(this))
{
    m_pollTimer->setSingleShot(false);
    connect(m_pollTimer, &QTimer::timeout, this, &AuthController::pollToken);
    loadSavedAuth();
}

bool    AuthController::loggedIn()        const { return m_loggedIn; }
QString AuthController::username()        const { return m_username; }
QString AuthController::uuid()            const { return m_uuid; }
QString AuthController::accessToken()     const { return m_accessToken; }
QString AuthController::status()          const { return m_status; }
QString AuthController::userCode()        const { return m_userCode; }
QString AuthController::verificationUrl() const { return m_verificationUrl; }
bool    AuthController::polling()         const { return m_polling; }

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

void AuthController::setStatus(const QString &s) {
    if (m_status == s) return;
    m_status = s;
    emit statusChanged();
}

void AuthController::setLoggedIn(bool v) {
    if (m_loggedIn == v) return;
    m_loggedIn = v;
    emit loggedInChanged();
}

void AuthController::setPolling(bool v) {
    if (m_polling == v) return;
    m_polling = v;
    emit pollingChanged();
}

void AuthController::fail(const QString &message) {
    m_pollTimer->stop();
    setPolling(false);
    setStatus(message);
    emit loginFailed(message);
}

void AuthController::copyToClipboard(const QString &text) {
    QGuiApplication::clipboard()->setText(text);
}

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

void AuthController::startLogin() {
    m_pollTimer->stop();
    setPolling(false);
    m_userCode.clear();
    m_verificationUrl.clear();
    m_deviceCode.clear();
    m_msaToken.clear();
    m_xblToken.clear();
    m_userHash.clear();
    m_xstsToken.clear();
    emit userCodeChanged();
    emit verificationUrlChanged();
    setStatus("Requesting device code...");
    startDeviceFlow();
}

void AuthController::cancelLogin() {
    m_pollTimer->stop();
    setPolling(false);
    setStatus("");
}

void AuthController::logout() {
    m_pollTimer->stop();
    setPolling(false);
    m_username.clear();
    m_uuid.clear();
    m_accessToken.clear();
    emit usernameChanged();
    emit uuidChanged();
    emit accessTokenChanged();
    setLoggedIn(false);
    setStatus("");
    clearSavedAuth();
}

// ---------------------------------------------------------------------------
// Step 1 — Request device code
// ---------------------------------------------------------------------------

void AuthController::startDeviceFlow() {
    const QString clientId = loadClientId();
    if (clientId.isEmpty()) {
        fail(QString(
            "No Azure client_id configured.\n"
            "Create ~/.config/sheik/launcher.json with:\n"
            "{\"client_id\": \"<your-app-guid>\"}\n"
            "See the comment at the top of AuthController.cpp for setup steps."));
        return;
    }

    QNetworkRequest req(QUrl{DEVICE_CODE_URL});
    req.setHeader(QNetworkRequest::ContentTypeHeader, "application/x-www-form-urlencoded");

    QUrlQuery body;
    body.addQueryItem("client_id", clientId);
    body.addQueryItem("scope", "XboxLive.signin offline_access");

    QNetworkReply *reply = m_nam->post(req, body.toString(QUrl::FullyEncoded).toUtf8());
    connect(reply, &QNetworkReply::finished, this, [this, reply]() {
        reply->deleteLater();
        if (reply->error() != QNetworkReply::NoError) {
            fail("Network error: " + reply->errorString());
            return;
        }

        const QJsonObject obj = QJsonDocument::fromJson(reply->readAll()).object();
        m_deviceCode      = obj["device_code"].toString();
        m_userCode        = obj["user_code"].toString();
        m_verificationUrl = obj["verification_uri"].toString();
        m_pollInterval    = obj.value("interval").toInt(5);

        emit userCodeChanged();
        emit verificationUrlChanged();
        emit deviceCodeReady();
        setStatus("Waiting for you to sign in at the URL below...");
        setPolling(true);
        m_pollTimer->start(m_pollInterval * 1000);
    });
}

// ---------------------------------------------------------------------------
// Step 2 — Poll for MSA token
// ---------------------------------------------------------------------------

void AuthController::pollToken() {
    m_pollTimer->stop(); // pause while waiting for reply

    const QString clientId = loadClientId();

    QNetworkRequest req(QUrl{TOKEN_URL});
    req.setHeader(QNetworkRequest::ContentTypeHeader, "application/x-www-form-urlencoded");

    QUrlQuery body;
    body.addQueryItem("grant_type", "urn:ietf:params:oauth:grant-type:device_code");
    body.addQueryItem("client_id",  clientId);
    body.addQueryItem("device_code", m_deviceCode);

    QNetworkReply *reply = m_nam->post(req, body.toString(QUrl::FullyEncoded).toUtf8());
    connect(reply, &QNetworkReply::finished, this, [this, reply]() {
        reply->deleteLater();
        const QJsonObject obj = QJsonDocument::fromJson(reply->readAll()).object();

        if (obj.contains("error")) {
            const QString err = obj["error"].toString();
            if (err == "authorization_pending") {
                m_pollTimer->start(m_pollInterval * 1000);
                return;
            }
            if (err == "slow_down") {
                m_pollInterval += 5;
                m_pollTimer->start(m_pollInterval * 1000);
                return;
            }
            if (err == "expired_token") {
                fail("Login timed out. Please try again.");
            } else {
                fail("Auth error: " + obj["error_description"].toString());
            }
            return;
        }

        // MSA token received — dismiss code UI and proceed
        setPolling(false);
        m_userCode.clear();
        m_verificationUrl.clear();
        emit userCodeChanged();
        emit verificationUrlChanged();

        m_msaToken = obj["access_token"].toString();
        setStatus("Signing in to Xbox Live...");
        fetchXBL();
    });
}

// ---------------------------------------------------------------------------
// Step 3 — Exchange MSA token for XBL token
// ---------------------------------------------------------------------------

void AuthController::fetchXBL() {
    QNetworkRequest req(QUrl{XBL_URL});
    req.setHeader(QNetworkRequest::ContentTypeHeader, "application/json");
    req.setRawHeader("Accept", "application/json");

    QJsonObject props;
    props["AuthMethod"] = "RPS";
    props["SiteName"]   = "user.auth.xboxlive.com";
    props["RpsTicket"]  = "d=" + m_msaToken;

    QJsonObject body;
    body["Properties"]   = props;
    body["RelyingParty"] = "http://auth.xboxlive.com";
    body["TokenType"]    = "JWT";

    QNetworkReply *reply = m_nam->post(req, QJsonDocument(body).toJson(QJsonDocument::Compact));
    connect(reply, &QNetworkReply::finished, this, [this, reply]() {
        reply->deleteLater();
        if (reply->error() != QNetworkReply::NoError) {
            const QString body = QString::fromUtf8(reply->readAll());
            fail("Xbox Live error: " + reply->errorString() + (body.isEmpty() ? "" : " — " + body));
            return;
        }

        const QJsonObject obj = QJsonDocument::fromJson(reply->readAll()).object();
        m_xblToken = obj["Token"].toString();

        const QJsonArray xui = obj["DisplayClaims"].toObject()["xui"].toArray();
        if (!xui.isEmpty())
            m_userHash = xui[0].toObject()["uhs"].toString();

        setStatus("Signing in to Xbox services...");
        fetchXSTS();
    });
}

// ---------------------------------------------------------------------------
// Step 4 — Exchange XBL token for XSTS token
// ---------------------------------------------------------------------------

void AuthController::fetchXSTS() {
    QNetworkRequest req(QUrl{XSTS_URL});
    req.setHeader(QNetworkRequest::ContentTypeHeader, "application/json");
    req.setRawHeader("Accept", "application/json");

    QJsonArray tokens;
    tokens.append(m_xblToken);

    QJsonObject props;
    props["SandboxId"]  = "RETAIL";
    props["UserTokens"] = tokens;

    QJsonObject body;
    body["Properties"]   = props;
    body["RelyingParty"] = "rp://api.minecraftservices.com/";
    body["TokenType"]    = "JWT";

    QNetworkReply *reply = m_nam->post(req, QJsonDocument(body).toJson(QJsonDocument::Compact));
    connect(reply, &QNetworkReply::finished, this, [this, reply]() {
        reply->deleteLater();
        if (reply->error() != QNetworkReply::NoError) {
            const QString body = QString::fromUtf8(reply->readAll());
            fail("XSTS error: " + reply->errorString() + (body.isEmpty() ? "" : " — " + body));
            return;
        }

        const QJsonObject obj = QJsonDocument::fromJson(reply->readAll()).object();
        if (obj.contains("XErr")) {
            const qint64 xerr = static_cast<qint64>(obj["XErr"].toDouble());
            if (xerr == 2148916233LL)
                fail("This Microsoft account has no Xbox profile. Visit xbox.com to create one.");
            else if (xerr == 2148916238LL)
                fail("Child account: parental consent required in Family Settings.");
            else
                fail(QString("Xbox error code: %1").arg(xerr));
            return;
        }

        m_xstsToken = obj["Token"].toString();
        setStatus("Signing in to Minecraft...");
        fetchMCToken();
    });
}

// ---------------------------------------------------------------------------
// Step 5 — Exchange XSTS token for Minecraft access token
// ---------------------------------------------------------------------------

void AuthController::fetchMCToken() {
    QNetworkRequest req(QUrl{MC_AUTH_URL});
    req.setHeader(QNetworkRequest::ContentTypeHeader, "application/json");

    QJsonObject body;
    body["identityToken"] = QString("XBL3.0 x=%1;%2").arg(m_userHash, m_xstsToken);

    QNetworkReply *reply = m_nam->post(req, QJsonDocument(body).toJson(QJsonDocument::Compact));
    connect(reply, &QNetworkReply::finished, this, [this, reply]() {
        reply->deleteLater();
        if (reply->error() != QNetworkReply::NoError) {
            const QString body = QString::fromUtf8(reply->readAll());
            fail("Minecraft auth error: " + reply->errorString() + (body.isEmpty() ? "" : " — " + body));
            return;
        }

        const QJsonObject obj = QJsonDocument::fromJson(reply->readAll()).object();
        m_accessToken = obj["access_token"].toString();
        emit accessTokenChanged();
        setStatus("Fetching Minecraft profile...");
        fetchProfile();
    });
}

// ---------------------------------------------------------------------------
// Step 6 — Fetch Minecraft profile (username + UUID)
// ---------------------------------------------------------------------------

void AuthController::fetchProfile() {
    QNetworkRequest req(QUrl{MC_PROFILE_URL});
    req.setRawHeader("Authorization", ("Bearer " + m_accessToken).toUtf8());

    QNetworkReply *reply = m_nam->get(req);
    connect(reply, &QNetworkReply::finished, this, [this, reply]() {
        reply->deleteLater();
        if (reply->error() != QNetworkReply::NoError) {
            fail("Profile error: " + reply->errorString());
            return;
        }

        const QJsonObject obj = QJsonDocument::fromJson(reply->readAll()).object();
        if (obj.contains("error")) {
            fail("This Microsoft account does not own Minecraft Java Edition.");
            return;
        }

        m_username = obj["name"].toString();
        m_uuid     = obj["id"].toString();
        emit usernameChanged();
        emit uuidChanged();
        setLoggedIn(true);
        setStatus("");
        saveAuth();
        emit loginSuccess();
    });
}

// ---------------------------------------------------------------------------
// Token persistence
// ---------------------------------------------------------------------------

static QString sheikConfigDir() {
    // GenericConfigLocation + "/sheik" gives:
    //   Linux:   ~/.config/sheik
    //   Windows: %AppData%\sheik
    //   macOS:   ~/Library/Preferences/sheik
    // AppConfigLocation would append the app name again (sheik/sheik).
    const QString dir = QStandardPaths::writableLocation(QStandardPaths::GenericConfigLocation)
                        + QStringLiteral("/sheik");
    QDir().mkpath(dir);
    return dir;
}

QString AuthController::authFilePath() const {
    return sheikConfigDir() + QStringLiteral("/auth.json");
}

QString AuthController::launcherConfigPath() const {
    // Also check next to the executable for portable installs
    const QString portable = QCoreApplication::applicationDirPath() + "/launcher.json";
    if (QFile::exists(portable))
        return portable;
    return sheikConfigDir() + QStringLiteral("/launcher.json");
}

QString AuthController::loadClientId() const {
    QFile f(launcherConfigPath());
    if (!f.open(QIODevice::ReadOnly))
        return {};
    const QJsonObject obj = QJsonDocument::fromJson(f.readAll()).object();
    return obj["client_id"].toString();
}

void AuthController::saveAuth() {
    QJsonObject obj;
    obj["access_token"] = m_accessToken;
    obj["username"]     = m_username;
    obj["uuid"]         = m_uuid;

    QFile f(authFilePath());
    if (f.open(QIODevice::WriteOnly | QIODevice::Truncate))
        f.write(QJsonDocument(obj).toJson());
}

void AuthController::loadSavedAuth() {
    QFile f(authFilePath());
    if (!f.open(QIODevice::ReadOnly))
        return;

    const QJsonObject saved = QJsonDocument::fromJson(f.readAll()).object();
    const QString token = saved["access_token"].toString();
    const QString user  = saved["username"].toString();
    const QString uuid  = saved["uuid"].toString();

    if (token.isEmpty())
        return;

    // Validate the token is still live before marking as logged in
    QNetworkRequest req(QUrl{MC_PROFILE_URL});
    req.setRawHeader("Authorization", ("Bearer " + token).toUtf8());

    QNetworkReply *reply = m_nam->get(req);
    connect(reply, &QNetworkReply::finished, this, [this, reply, token, user, uuid]() {
        reply->deleteLater();
        if (reply->error() == QNetworkReply::NoError) {
            const QJsonObject profile = QJsonDocument::fromJson(reply->readAll()).object();
            if (!profile.contains("error")) {
                m_accessToken = token;
                m_username    = profile.value("name").toString(user);
                m_uuid        = profile.value("id").toString(uuid);
                emit accessTokenChanged();
                emit usernameChanged();
                emit uuidChanged();
                setLoggedIn(true);
                return;
            }
        }
        // Token expired or invalid — prompt user to log in again
        clearSavedAuth();
    });
}

void AuthController::clearSavedAuth() {
    QFile::remove(authFilePath());
}
