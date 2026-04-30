#include "SettingsMenu.h"
#include <QFormLayout>

SettingsMenu::SettingsMenu(QWidget *parent)
    : QMainWindow(parent), settings("Sheik", "AutoClicker")
{
    centralWidget = new QWidget(this);
    setCentralWidget(centralWidget);

    layout = new QVBoxLayout(centralWidget);
    layout->setSpacing(16);
    layout->setContentsMargins(20, 20, 20, 20);

    // ── Top bar ──────────────────────────────────────────────────
    topBarLayout = new QHBoxLayout();
    btnBack = new QPushButton("← Back", this);
    btnBack->setObjectName("btnSecondary");
    auto *titleLabel = new QLabel("Settings", this);
    titleLabel->setObjectName("screenTitle");
    topBarLayout->addWidget(btnBack, 0, Qt::AlignLeft);
    topBarLayout->addWidget(titleLabel, 1, Qt::AlignCenter);
    topBarLayout->addSpacing(60);
    layout->addLayout(topBarLayout);

    // ── Left click ───────────────────────────────────────────────
    grpLeft = new QGroupBox("Left Click", this);
    auto *leftLayout = new QFormLayout(grpLeft);
    leftLayout->setSpacing(10);
    leftLayout->setContentsMargins(14, 16, 14, 14);

    chkLeftEnabled = new QCheckBox("Enabled", this);

    spinLeftInterval = new QSpinBox(this);
    spinLeftInterval->setRange(10, 5000);
    spinLeftInterval->setSuffix(" ms");
    spinLeftInterval->setValue(100);

    spinLeftRandMin = new QSpinBox(this);
    spinLeftRandMin->setRange(0, 2000);
    spinLeftRandMin->setSuffix(" ms");
    spinLeftRandMin->setSpecialValueText("Off");

    spinLeftRandMax = new QSpinBox(this);
    spinLeftRandMax->setRange(0, 2000);
    spinLeftRandMax->setSuffix(" ms");
    spinLeftRandMax->setSpecialValueText("Off");

    chkLeftAllowMining = new QCheckBox("Allow mining blocks", this);

    connect(spinLeftRandMin, QOverload<int>::of(&QSpinBox::valueChanged), this, [this](int v) {
        if (spinLeftRandMax->value() < v) spinLeftRandMax->setValue(v);
    });
    connect(spinLeftRandMax, QOverload<int>::of(&QSpinBox::valueChanged), this, [this](int v) {
        if (spinLeftRandMin->value() > v) spinLeftRandMin->setValue(v);
    });

    leftLayout->addRow("",          chkLeftEnabled);
    leftLayout->addRow("Interval:", spinLeftInterval);
    leftLayout->addRow("Jitter min:", spinLeftRandMin);
    leftLayout->addRow("Jitter max:", spinLeftRandMax);
    leftLayout->addRow("",          chkLeftAllowMining);
    layout->addWidget(grpLeft);

    connect(chkLeftEnabled, &QCheckBox::toggled, this, &SettingsMenu::updateLeftEnabled);
    updateLeftEnabled(false);

    // ── Right click ──────────────────────────────────────────────
    grpRight = new QGroupBox("Right Click", this);
    auto *rightLayout = new QFormLayout(grpRight);
    rightLayout->setSpacing(10);
    rightLayout->setContentsMargins(14, 16, 14, 14);

    chkRightEnabled = new QCheckBox("Enabled", this);

    spinRightInterval = new QSpinBox(this);
    spinRightInterval->setRange(10, 5000);
    spinRightInterval->setSuffix(" ms");
    spinRightInterval->setValue(100);

    spinRightRandMin = new QSpinBox(this);
    spinRightRandMin->setRange(0, 2000);
    spinRightRandMin->setSuffix(" ms");
    spinRightRandMin->setSpecialValueText("Off");

    spinRightRandMax = new QSpinBox(this);
    spinRightRandMax->setRange(0, 2000);
    spinRightRandMax->setSuffix(" ms");
    spinRightRandMax->setSpecialValueText("Off");

    chkRightOnlyWithBlock = new QCheckBox("Only while holding a block", this);

    connect(spinRightRandMin, QOverload<int>::of(&QSpinBox::valueChanged), this, [this](int v) {
        if (spinRightRandMax->value() < v) spinRightRandMax->setValue(v);
    });
    connect(spinRightRandMax, QOverload<int>::of(&QSpinBox::valueChanged), this, [this](int v) {
        if (spinRightRandMin->value() > v) spinRightRandMin->setValue(v);
    });

    rightLayout->addRow("",           chkRightEnabled);
    rightLayout->addRow("Interval:",  spinRightInterval);
    rightLayout->addRow("Jitter min:", spinRightRandMin);
    rightLayout->addRow("Jitter max:", spinRightRandMax);
    rightLayout->addRow("",           chkRightOnlyWithBlock);
    layout->addWidget(grpRight);

    connect(chkRightEnabled, &QCheckBox::toggled, this, &SettingsMenu::updateRightEnabled);
    updateRightEnabled(false);

    layout->addStretch();

    // ── Action buttons ───────────────────────────────────────────
    auto *btnRow = new QHBoxLayout();
    btnReset = new QPushButton("Reset Defaults", this);
    btnSave  = new QPushButton("Save", this);
    btnReset->setObjectName("btnSecondary");
    btnSave->setObjectName("btnPrimary");
    btnRow->addWidget(btnReset);
    btnRow->addStretch();
    btnRow->addWidget(btnSave);
    layout->addLayout(btnRow);

    connect(btnBack,  &QPushButton::clicked, this, &SettingsMenu::goBack);
    connect(btnSave,  &QPushButton::clicked, this, &SettingsMenu::saveSettings);
    connect(btnReset, &QPushButton::clicked, this, &SettingsMenu::resetDefaults);

    loadSettings();
}

void SettingsMenu::updateLeftEnabled(bool enabled) {
    spinLeftInterval->setEnabled(enabled);
    spinLeftRandMin->setEnabled(enabled);
    spinLeftRandMax->setEnabled(enabled);
    chkLeftAllowMining->setEnabled(enabled);
}

void SettingsMenu::updateRightEnabled(bool enabled) {
    spinRightInterval->setEnabled(enabled);
    spinRightRandMin->setEnabled(enabled);
    spinRightRandMax->setEnabled(enabled);
    chkRightOnlyWithBlock->setEnabled(enabled);
}

void SettingsMenu::loadSettings() {
    chkLeftEnabled->setChecked(settings.value("leftEnabled", false).toBool());
    spinLeftInterval->setValue(settings.value("leftInterval", 100).toInt());
    spinLeftRandMin->setValue(settings.value("leftRandMin", 0).toInt());
    spinLeftRandMax->setValue(settings.value("leftRandMax", 0).toInt());
    chkLeftAllowMining->setChecked(settings.value("leftAllowMining", false).toBool());

    chkRightEnabled->setChecked(settings.value("rightEnabled", false).toBool());
    spinRightInterval->setValue(settings.value("rightInterval", 100).toInt());
    spinRightRandMin->setValue(settings.value("rightRandMin", 0).toInt());
    spinRightRandMax->setValue(settings.value("rightRandMax", 0).toInt());
    chkRightOnlyWithBlock->setChecked(settings.value("rightOnlyWithBlock", false).toBool());

    updateLeftEnabled(chkLeftEnabled->isChecked());
    updateRightEnabled(chkRightEnabled->isChecked());
}

void SettingsMenu::saveSettings() {
    settings.setValue("leftEnabled",      chkLeftEnabled->isChecked());
    settings.setValue("leftInterval",     spinLeftInterval->value());
    settings.setValue("leftRandMin",      spinLeftRandMin->value());
    settings.setValue("leftRandMax",      spinLeftRandMax->value());
    settings.setValue("leftAllowMining",  chkLeftAllowMining->isChecked());

    settings.setValue("rightEnabled",       chkRightEnabled->isChecked());
    settings.setValue("rightInterval",      spinRightInterval->value());
    settings.setValue("rightRandMin",       spinRightRandMin->value());
    settings.setValue("rightRandMax",       spinRightRandMax->value());
    settings.setValue("rightOnlyWithBlock", chkRightOnlyWithBlock->isChecked());

    settings.sync();
    emit settingsChanged();
    emit goBack();
}

void SettingsMenu::resetDefaults() {
    chkLeftEnabled->setChecked(false);
    spinLeftInterval->setValue(100);
    spinLeftRandMin->setValue(0);
    spinLeftRandMax->setValue(0);
    chkLeftAllowMining->setChecked(false);

    chkRightEnabled->setChecked(false);
    spinRightInterval->setValue(100);
    spinRightRandMin->setValue(0);
    spinRightRandMax->setValue(0);
    chkRightOnlyWithBlock->setChecked(false);
}

bool SettingsMenu::leftEnabled()      const { return chkLeftEnabled->isChecked(); }
int  SettingsMenu::leftIntervalMs()   const { return spinLeftInterval->value(); }
int  SettingsMenu::leftRandMin()      const { return spinLeftRandMin->value(); }
int  SettingsMenu::leftRandMax()      const { return spinLeftRandMax->value(); }
bool SettingsMenu::leftAllowMining()  const { return chkLeftAllowMining->isChecked(); }

bool SettingsMenu::rightEnabled()        const { return chkRightEnabled->isChecked(); }
int  SettingsMenu::rightIntervalMs()     const { return spinRightInterval->value(); }
int  SettingsMenu::rightRandMin()        const { return spinRightRandMin->value(); }
int  SettingsMenu::rightRandMax()        const { return spinRightRandMax->value(); }
bool SettingsMenu::rightOnlyWithBlock()  const { return chkRightOnlyWithBlock->isChecked(); }
