#include "LaunchController.h"

#include <QCoreApplication>
#include <QDir>
#include <QFile>
#include <QStandardPaths>
#include <QSysInfo>

LaunchController::LaunchController(QObject *parent) : QObject(parent) {}

bool LaunchController::running() const { return m_running; }
QStringList LaunchController::logLines() const { return m_logLines; }

void LaunchController::setRunning(bool v) {
    if (m_running == v) return;
    m_running = v;
    emit runningChanged();
}

void LaunchController::appendLog(const QString &text) {
    m_logLines.append(text);
    emit logLinesChanged();
    emit logLine(text);
}

void LaunchController::launch() {
    if (m_running) return;

    m_logLines.clear();
    emit logLinesChanged();

    // Locate the client directory:
    //   - Installed layout:  <prefix>/bin/sheik-launcher  →  <prefix>/share/sheik/client
    //   - Dev layout:        <repo>/build/launcher/sheik-launcher  →  <repo>/client
    const QString appDir = QCoreApplication::applicationDirPath();
    QString clientDir;
    {
        // Dev tree: go up until we find a "client" sibling
        QDir d(appDir);
        bool found = false;
        for (int i = 0; i < 4; ++i) {
            if (QFile::exists(d.absolutePath() + "/client/build/libs/sheik-1.8.9-all.jar")) {
                clientDir = d.absolutePath() + "/client";
                found = true;
                break;
            }
            d.cdUp();
        }
        if (!found) {
            // Installed layout
            clientDir = QDir::cleanPath(appDir + "/../share/sheik/client");
        }
    }
    clientDir = QDir::cleanPath(clientDir);

    const QString jar = clientDir + "/build/libs/sheik-1.8.9-all.jar";
    if (!QFile::exists(jar)) {
        appendLog("[Launcher] ERROR: Fat jar not found: " + jar);
        appendLog("[Launcher] Run './gradlew fatJar' inside client/ to build it first.");
        return;
    }

    // Find java — prefer JAVA_HOME, then PATH
    QString javaExe = QStringLiteral("java");
    {
        const QString javaHome = qEnvironmentVariable("JAVA_HOME");
        if (!javaHome.isEmpty()) {
#ifdef Q_OS_WIN
            const QString candidate = javaHome + "/bin/java.exe";
#else
            const QString candidate = javaHome + "/bin/java";
#endif
            if (QFile::exists(candidate))
                javaExe = candidate;
        }
    }

    // On Windows the natives subfolder name is the same; just use the right separator
    const QString nativesPath = QDir::toNativeSeparators(clientDir + "/natives");

    appendLog("[Launcher] Working directory: " + clientDir);
    appendLog("[Launcher] Starting Sheik client...");

    m_process = new QProcess(this);
    m_process->setWorkingDirectory(clientDir);
    m_process->setProcessChannelMode(QProcess::MergedChannels);

    connect(m_process, &QProcess::readyRead,
            this, &LaunchController::onReadyRead);
    connect(m_process,
            QOverload<int, QProcess::ExitStatus>::of(&QProcess::finished),
            this, &LaunchController::onFinished);

    const QStringList args = {
        "-Djava.library.path=" + nativesPath,
        "-jar", jar,
        "--username",    m_authUsername.isEmpty()    ? "Player" : m_authUsername,
        "--uuid",        m_authUuid.isEmpty()        ? "0"      : m_authUuid,
        "--accessToken", m_authAccessToken.isEmpty() ? "0"      : m_authAccessToken,
    };
    m_process->start(javaExe, args);

    setRunning(true);
    emit processStarted();
}

void LaunchController::kill() {
    if (m_process) {
        appendLog("[Launcher] Killing process...");
        m_process->kill();
    }
}

void LaunchController::setAuthInfo(const QString &username, const QString &uuid, const QString &accessToken) {
    m_authUsername    = username;
    m_authUuid        = uuid;
    m_authAccessToken = accessToken;
}

void LaunchController::onReadyRead() {
    while (m_process->canReadLine()) {
        const QString line = QString::fromLocal8Bit(m_process->readLine()).trimmed();
        if (!line.isEmpty())
            appendLog(line);
    }
}

void LaunchController::onFinished(int exitCode, QProcess::ExitStatus) {
    onReadyRead(); // drain remaining output
    appendLog(QString("[Launcher] Process exited with code %1").arg(exitCode));
    emit processFinished(exitCode);
    m_process->deleteLater();
    m_process = nullptr;
    setRunning(false);
}
