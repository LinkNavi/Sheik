#include "DebugMenu.h"

DebugMenu::DebugMenu(QWidget *parent) : QMainWindow(parent) {
    centralWidget = new QWidget(this);
    setCentralWidget(centralWidget);

topBarLayout = new QHBoxLayout();
    btnBack = new QPushButton("Back", this);
    topBarLayout->setContentsMargins(0, 1, 0, 1);
    topBarLayout->addWidget(btnBack, 0 , Qt::AlignLeft);

    layout = new QVBoxLayout(centralWidget);
    layout->setSpacing(8);
    layout->setContentsMargins(20, 20, 20, 20);
    layout->addLayout(topBarLayout);

    // Create labels
    lblYaw = new QLabel("Yaw: --", this);
    lblPitch = new QLabel("Pitch: --", this);
    lblTarget = new QLabel("Target: --", this);
    lblHealth = new QLabel("Health: --", this);
    lblBlock = new QLabel("Block: --", this);
    lblGui = new QLabel("GUI: --", this);

    // Style them
    for (auto* lbl : {lblYaw, lblPitch, lblTarget, lblHealth, lblBlock, lblGui}) {
        lbl->setStyleSheet("font-family: monospace; font-size: 14px;");
        layout->addWidget(lbl);
    }

    layout->addStretch();

connect(btnBack, &QPushButton::clicked, this, [this]() {
    qDebug() << "Back button clicked";
    emit goBack();
});

    // Setup IPC polling timer (100ms = 10Hz, same as your Python sleep)
    timer = new QTimer(this);
    connect(timer, &QTimer::timeout, this, &DebugMenu::updateLoop);
    timer->start(100);
}

void DebugMenu::updateLoop() {
    if (!ipc.valid()) return;

    ipc.update();
    const auto* s = ipc.state();
    std::cout << "DebugMenu updateLoop: yaw=" << s->yaw << " pitch=" << s->pitch
              << " targetId=" << s->targetId << " targetHealth=" << s->targetHealth
              << " lookingAtBlock=" << s->lookingAtBlock << " inGui=" << (int)s->inGui
              << std::endl;
    lblYaw->setText(QString("Yaw: %1").arg(s->yaw, 0, 'f', 1));
    lblPitch->setText(QString("Pitch: %1").arg(s->pitch, 0, 'f', 1));
    lblTarget->setText(QString("Target: %1").arg(s->targetId));
    lblHealth->setText(QString("Health: %1").arg(s->targetHealth, 0, 'f', 1));
    lblBlock->setText(QString("Block: %1").arg(s->lookingAtBlock ? "Yes" : "No"));
    lblGui->setText(QString("GUI: %1").arg(s->inGui ? "Yes" : "No"));
}