#pragma once

#include <QMainWindow>
#include <QPushButton>
#include <QLabel>
#include <QVBoxLayout>
#include <QTimer>
#include "IPC.h"

class DebugMenu : public QMainWindow {
    Q_OBJECT

public:
    explicit DebugMenu(QWidget *parent = nullptr);

signals:
    void goBack();

private slots:
    void updateLoop();

private:
    QWidget *centralWidget;
    QVBoxLayout *layout;
    QHBoxLayout *topBarLayout;
    QPushButton *btnBack;
    QLabel *lblYaw;
    QLabel *lblPitch;
    QLabel *lblTarget;
    QLabel *lblHealth;
    QLabel *lblBlock;
    QLabel *lblGui;
    
    QTimer *timer;
    SheikIPC ipc;
};