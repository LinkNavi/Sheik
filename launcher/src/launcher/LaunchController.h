#pragma once

#include <QObject>
#include <QProcess>
#include <QStringList>

class LaunchController : public QObject {
    Q_OBJECT
    Q_PROPERTY(bool running READ running NOTIFY runningChanged)
    Q_PROPERTY(QStringList logLines READ logLines NOTIFY logLinesChanged)

public:
    explicit LaunchController(QObject *parent = nullptr);

    Q_INVOKABLE void launch();
    Q_INVOKABLE void kill();
    Q_INVOKABLE void setAuthInfo(const QString &username, const QString &uuid, const QString &accessToken);

    bool running() const;
    QStringList logLines() const;

signals:
    void logLine(const QString &text);
    void processStarted();
    void processFinished(int exitCode);
    void runningChanged();
    void logLinesChanged();

private slots:
    void onReadyRead();
    void onFinished(int exitCode, QProcess::ExitStatus status);

private:
    QProcess   *m_process  = nullptr;
    bool        m_running  = false;
    QStringList m_logLines;

    QString m_authUsername;
    QString m_authUuid;
    QString m_authAccessToken;

    void setRunning(bool v);
    void appendLog(const QString &text);
};
