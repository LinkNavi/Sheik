#pragma once

#include <QMainWindow>
#include <QPushButton>
#include <QLabel>
#include <QVBoxLayout>
#include <QHBoxLayout>
#include <QGridLayout>
#include <QProgressBar>
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
    QWidget      *centralWidget;
    QVBoxLayout  *layout;
    QHBoxLayout  *topBarLayout;
    QPushButton  *btnBack;

    // Grid of stat rows
    QLabel *lblYawVal;
    QLabel *lblPitchVal;
    QLabel *lblTargetVal;
    QLabel *lblHealthVal;
    QLabel *lblBlockVal;
    QLabel *lblGuiVal;
    QLabel *lblHeldVal;

    QProgressBar *hpBar;
    QLabel       *lblRawDump;

    QTimer   *timer;
    SheikIPC  ipc;

    QLabel* makeStatLabel(const QString &text);
};