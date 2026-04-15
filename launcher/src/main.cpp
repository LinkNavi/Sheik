#include <QGuiApplication>
#include <QQmlApplicationEngine>
#include <QQmlContext>
#include <QSslConfiguration>
#include <QSslCertificate>
#include <QSslSocket>
#include <QFile>
#include <QDir>
#include <QStandardPaths>
#include "launcher/LaunchController.h"
#include "auth/AuthController.h"

// Mozilla CA bundle URL for fallback (you can bundle this as a resource instead)
// Download from: https://curl.se/ca/cacert.pem
static const char *kMozillaBundle = ":/certs/cacert.pem";

int main(int argc, char *argv[]) {
    // ── Phase 1: set OpenSSL env vars BEFORE any Qt objects ──────────
    // OpenSSL reads SSL_CERT_FILE at context creation time.  We must set it
    // before QGuiApplication because that can trigger plugin loading which
    // initialises the OpenSSL context.
    
    [[maybe_unused]] bool foundSystemCerts = false;
    static constexpr const char * const kBundles[] = {
        "/etc/ssl/cert.pem",                  // Arch Linux, Alpine, macOS
        "/etc/ssl/certs/ca-certificates.crt", // Debian / Ubuntu
        "/etc/pki/tls/certs/ca-bundle.crt",   // Fedora / RHEL
        "/etc/ssl/certs/ca-bundle.crt",       // openSUSE
        "/etc/pki/ca-trust/extracted/pem/tls-ca-bundle.pem", // CentOS/RHEL alt
        "/usr/local/share/certs/ca-root-nss.crt", // FreeBSD
        nullptr
    };
    
    for (int i = 0; kBundles[i]; ++i) {
        if (QFile::exists(QLatin1String(kBundles[i]))) {
            qputenv("SSL_CERT_FILE", kBundles[i]);
            qputenv("SSL_CERT_DIR",  "/etc/ssl/certs");
            foundSystemCerts = true;
            break;
        }
    }

    // Windows/macOS fallback: check if Qt can find any certs at all
    // If not, we'll need to bundle them
    #if defined(Q_OS_WIN) || defined(Q_OS_MACOS)
    if (!foundSystemCerts) {
        // Try Qt's bundled certs or app-specific location
        const QString appDir = QCoreApplication::applicationDirPath();
        const QString bundledPath = appDir + "/certs/cacert.pem";
        if (QFile::exists(bundledPath)) {
            qputenv("SSL_CERT_FILE", bundledPath.toUtf8());
        }
    }
    #endif

    QGuiApplication app(argc, argv);
    app.setApplicationName("sheik");
    app.setOrganizationName("sheik");

    // ── Phase 2: patch Qt's default SSL config AFTER the app exists ──
    // Qt's SSL plugin is now loaded.  Merge the explicit bundle on top of
    // whatever systemCaCertificates() returned so we don't lose anything.
    {
        QSslConfiguration cfg = QSslConfiguration::defaultConfiguration();
        QList<QSslCertificate> all = cfg.caCertificates();
        QSet<QByteArray> existing;
        for (const auto &c : all) {
            existing.insert(c.digest(QCryptographicHash::Sha256));
        }

        // Helper lambda to add certs from a path
        auto addCertsFromPath = [&](const QString &path, bool isResource = false) {
            if (isResource) {
                QFile f(path);
                if (!f.open(QIODevice::ReadOnly)) return;
                const QList<QSslCertificate> certs = QSslCertificate::fromData(f.readAll(), QSsl::Pem);
                for (const auto &c : certs) {
                    if (!existing.contains(c.digest(QCryptographicHash::Sha256))) {
                        all.append(c);
                        existing.insert(c.digest(QCryptographicHash::Sha256));
                    }
                }
            } else {
                const QList<QSslCertificate> certs = QSslCertificate::fromPath(path, QSsl::Pem,
                                              QSslCertificate::PatternSyntax::FixedString);
                for (const auto &c : certs) {
                    if (!existing.contains(c.digest(QCryptographicHash::Sha256))) {
                        all.append(c);
                        existing.insert(c.digest(QCryptographicHash::Sha256));
                    }
                }
            }
        };

        // Add from SSL_CERT_FILE if set
        const QByteArray bundle = qgetenv("SSL_CERT_FILE");
        if (!bundle.isEmpty()) {
            addCertsFromPath(QString::fromUtf8(bundle));
        }

        // Always merge bundled Mozilla CA certs to fill any gaps
        // in the system store (deduplication prevents duplicates)
        addCertsFromPath(QLatin1String(kMozillaBundle), true);

        // Final fallback: If still no certs, reload system certs
        if (all.isEmpty()) {
            all = QSslConfiguration::systemCaCertificates();
            addCertsFromPath(QLatin1String(kMozillaBundle), true);
        }

        cfg.setCaCertificates(all);
        QSslConfiguration::setDefaultConfiguration(cfg);
        
        // Debug output (remove in production)
        qDebug() << "SSL Certs loaded:" << all.count();
        qDebug() << "SSL Support:" << QSslSocket::supportsSsl();
        qDebug() << "SSL Library:" << QSslSocket::sslLibraryVersionString();
    }

    LaunchController controller;
    AuthController   auth;

    QQmlApplicationEngine engine;
    engine.rootContext()->setContextProperty("Launcher", &controller);
    engine.rootContext()->setContextProperty("Auth",     &auth);
    engine.load(QUrl(QStringLiteral("qrc:/qml/main.qml")));
    
    if (engine.rootObjects().isEmpty())
        return -1;
        
    return app.exec();
}