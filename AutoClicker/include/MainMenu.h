#pragma once
#include <QMainWindow>
#include <QWidget>
#include <QVBoxLayout>
#include <QHBoxLayout>
#include <QLabel>
#include <QPushButton>
#include <QProgressBar>
#include <QTimer>
#include "IPC.h"
#include "Clicker.h"
#include "SettingsMenu.h"

class MainMenu : public QMainWindow {
    Q_OBJECT
public:
    explicit MainMenu(SettingsMenu *settings, QWidget *parent = nullptr);

signals:
    void goToDebug();
    void goToSettings();

private slots:
    void toggleRunning();
    void updateLoop();
    void clickLoop();
public slots:
    void onSettingsChanged();


private:
    SettingsMenu   *settings;
    SheikIPC        ipc;
    Clicker         clicker;
    bool            running = false;

    QWidget     *centralWidget;
    QVBoxLayout *layout;
    QHBoxLayout *topBarLayout;
    QPushButton *btnSettings;
    QPushButton *btnToggle;
    QPushButton *btnDebugMenu;
    QPushButton *btnQuit;

    QLabel      *lblConnectionDot;
    QLabel      *lblConnectionText;
    QLabel      *lblTargetId;
    QLabel      *lblTargetHP;
    QProgressBar *hpBar;
    QWidget     *infoPanel;

    QTimer *updateTimer;
    QTimer *clickTimer;
};
