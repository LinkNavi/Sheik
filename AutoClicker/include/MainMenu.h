#pragma once

#include <QMainWindow>
#include <QPushButton>
#include <QVBoxLayout>
#include <QWidget>
#include <QLabel>
#include <QTimer>
#include "IPC.h"

class MainMenu : public QMainWindow {
    Q_OBJECT

public:
    explicit MainMenu(QWidget *parent = nullptr);

private:
    QWidget *centralWidget;
    QVBoxLayout *layout;
    QHBoxLayout *topBarLayout;
    QHBoxLayout *optionsLayout;
    QLabel *connectedLabel;
    QPushButton *btnStart;
    QPushButton *btnSettings;
    QPushButton *btnQuit;
    QPushButton *btnDebugMenu;
    QTimer *updateTimer;
    SheikIPC ipc;

    signals:
    void goToDebug();

private slots:
    void updateLoop();
};
