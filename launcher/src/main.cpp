#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QQmlContext>
#include "launcher/LaunchController.h"

int main(int argc, char *argv[]) {
    QGuiApplication app(argc, argv);

    LaunchController controller;

    QQmlApplicationEngine engine;
    engine.rootContext()->setContextProperty("Launcher", &controller);
    engine.load(QUrl(QStringLiteral("qrc:/qml/main.qml")));
    return app.exec();
}
