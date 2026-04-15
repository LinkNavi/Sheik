#pragma once

#include <QObject>
#include <QString>
#include <QTimer>

class QNetworkAccessManager;

// Handles Microsoft Device Code Flow → XBL → XSTS → Minecraft auth.
//
// CLIENT_ID uses the well-known Xbox public client (00000000402b5328).
// If Microsoft revokes it, register your own Azure app at
// https://portal.azure.com and replace CLIENT_ID in AuthController.cpp.
class AuthController : public QObject {
    Q_OBJECT
    Q_PROPERTY(bool    loggedIn        READ loggedIn        NOTIFY loggedInChanged)
    Q_PROPERTY(QString username        READ username        NOTIFY usernameChanged)
    Q_PROPERTY(QString uuid            READ uuid            NOTIFY uuidChanged)
    Q_PROPERTY(QString accessToken     READ accessToken     NOTIFY accessTokenChanged)
    Q_PROPERTY(QString status          READ status          NOTIFY statusChanged)
    Q_PROPERTY(QString userCode        READ userCode        NOTIFY userCodeChanged)
    Q_PROPERTY(QString verificationUrl READ verificationUrl NOTIFY verificationUrlChanged)
    Q_PROPERTY(bool    polling         READ polling         NOTIFY pollingChanged)

public:
    explicit AuthController(QObject *parent = nullptr);

    Q_INVOKABLE void startLogin();
    Q_INVOKABLE void cancelLogin();
    Q_INVOKABLE void logout();
    Q_INVOKABLE void copyToClipboard(const QString &text);

    bool    loggedIn()        const;
    QString username()        const;
    QString uuid()            const;
    QString accessToken()     const;
    QString status()          const;
    QString userCode()        const;
    QString verificationUrl() const;
    bool    polling()         const;

signals:
    void loggedInChanged();
    void usernameChanged();
    void uuidChanged();
    void accessTokenChanged();
    void statusChanged();
    void userCodeChanged();
    void verificationUrlChanged();
    void pollingChanged();

    void loginSuccess();
    void loginFailed(const QString &message);
    void deviceCodeReady();

private:
    QNetworkAccessManager *m_nam;
    QTimer                *m_pollTimer;

    bool    m_loggedIn  = false;
    bool    m_polling   = false;
    QString m_username;
    QString m_uuid;
    QString m_accessToken;
    QString m_status;
    QString m_userCode;
    QString m_verificationUrl;

    // Transient auth-chain state
    QString m_deviceCode;
    int     m_pollInterval = 5;
    QString m_msaToken;
    QString m_xblToken;
    QString m_userHash;
    QString m_xstsToken;

    void setStatus(const QString &s);
    void setLoggedIn(bool v);
    void setPolling(bool v);
    void fail(const QString &message);

    void startDeviceFlow();
    void pollToken();
    void fetchXBL();
    void fetchXSTS();
    void fetchMCToken();
    void fetchProfile();

    void    loadSavedAuth();
    void    saveAuth();
    void    clearSavedAuth();
    QString authFilePath() const;
    QString launcherConfigPath() const;
    QString loadClientId() const;
};
