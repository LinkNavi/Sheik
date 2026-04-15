#include "LaunchController.h"

#include <QCoreApplication>
#include <QDir>
#include <QFile>

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

    const QString clientDir = QDir::cleanPath(
        QCoreApplication::applicationDirPath() + "/../../client");

    const QString jar = clientDir + "/build/libs/sheik-1.8.9-all.jar";
    if (!QFile::exists(jar)) {
        appendLog("[Launcher] ERROR: Fat jar not found: " + jar);
        appendLog("[Launcher] Run './gradlew fatJar' inside client/ to build it first.");
        return;
    }

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
        "-Djava.library.path=natives",
        "-jar", jar
    };
    m_process->start("java", args);

    setRunning(true);
    emit processStarted();
}

void LaunchController::kill() {
    if (m_process) {
        appendLog("[Launcher] Killing process...");
        m_process->kill();
    }
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
