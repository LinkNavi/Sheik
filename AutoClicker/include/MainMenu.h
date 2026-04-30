#pragma once

#include <QMainWindow>
#include <QPushButton>
#include <QVBoxLayout>
#include <QHBoxLayout>
#include <QWidget>
#include <QLabel>
#include <QTimer>
#include <QProgressBar>
#include "IPC.h"
#include <QStyle>
class MainMenu : public QMainWindow {
    Q_OBJECT

public:
    explicit MainMenu(QWidget *parent = nullptr);

    bool isRunning() const { return running; }

signals:
    void goToDebug();
    void goToSettings();

private slots:
    void updateLoop();
    void toggleRunning();

private:
    QWidget     *centralWidget;
    QVBoxLayout *layout;
    QHBoxLayout *topBarLayout;
    QHBoxLayout *optionsLayout;

    // Status bar
    QLabel      *lblConnectionDot;
    QLabel      *lblConnectionText;

    // Info panel
    QWidget     *infoPanel;
    QLabel      *lblTargetId;
    QLabel      *lblTargetHP;
    QProgressBar *hpBar;

    // Buttons
    QPushButton *btnToggle;
    QPushButton *btnSettings;
    QPushButton *btnDebugMenu;
    QPushButton *btnQuit;

    QTimer      *updateTimer;
    SheikIPC     ipc;
    bool         running = false;
};
